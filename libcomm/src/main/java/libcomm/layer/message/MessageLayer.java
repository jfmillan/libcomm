package libcomm.layer.message;

import libcomm.context.ConnectionContext;
import libcomm.exception.CommErrorType;
import libcomm.exception.CommunicationException;
import libcomm.layer.AbstractLayer;
import libcomm.layer.ILayerCommands;
import libcomm.layer.ILayerObserver;
import libcomm.layer.rfc1006.Rfc1006Layer;
import libcomm.message.Message;
import libcomm.message.MessageAK;
import libcomm.message.MessageFactory;
import libcomm.message.MessageType;
import libcomm.message.SequenceNumber;
import libcomm.message.rfc1006.DataTsdu;
import libcomm.message.rfc1006.IBytes;

import commons.log.Log;
import commons.util.PrintUtils;
import commons.util.StrUtils;

/**
 * Capa de gestión de mensajes. Establece los números de secuencia y se encarga
 * del reenvío automático en caso de no recibir AK de cada mensaje enviado. Cada
 * mensaje se puede llegar a reenviar hasta un máximo de 3 veces en intervalos
 * de 5 segundos. Si aun así no se recibe su AK se produce una desconexión que
 * forzará a revisar el problema.
 * <p>
 * 21/02/2016 14:18:23
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class MessageLayer extends AbstractLayer<DataTsdu, Message<?>> implements AKObserver<SequenceNumber, Message<?>> {
									/* Observador de la capa RFC1006, recibirá un DataTsdu como mensaje. */
									/* Recibe mensajes de la capa superior. */
	
	/* Número de secuencia reservado para AKs */
	private static final SequenceNumber AK_SEQUENCE_NUMBER = new SequenceNumber(0x00);
	
	/* Observador a quien notifica la capa de mensajes */
	private final ILayerObserver<Message<?>> observer;
	
	/* Comandos a la capa inferior. */
	private final ILayerCommands<IBytes> commands;
	
	/* Número de secuencia actual a asignar al siguiente mensaje a enviar.*/
	private final SequenceNumber sequenceNumber;
	
	/* Controlador de mensajes de confirmación o AK */
	private final AKController<SequenceNumber, Message<?>> akController;
	
	/*
	 * Indica si la espera/envío de AKs está habilitada o no. El comportamiento
	 * por defecto es habilitado. El modo deshabilitado está pensado únicamente
	 * para tests por fallo de AK.
	 */
	private final boolean akEnabled;

	/* Error para desconexiónes por fallo de AK. */
	private volatile String akFailureError;
	
	/**
	 * Constructor de clase.
	 * 
	 * @param context
	 *            Contexto de la conexión.
	 * @param observer
	 *            Observador a quien notificar los eventos de conexión y
	 *            desconexión, y recepción de mensajes.
	 */
	public MessageLayer(final ConnectionContext context, final ILayerObserver<Message<?>> observer) {
		this.observer = observer;
		this.commands = new Rfc1006Layer(context, this);
		this.akEnabled = context.isAKEnabled();
		this.akController = akEnabled ? new AKController<>(this, context.getConnectionMode()) : null;
		this.sequenceNumber = new SequenceNumber(0x00);
		akFailureError = null;
	}

	@Override
	public void connect() {
		Log.info(this, (akEnabled ? "AK habilitado" : "AK deshabilitado, no se esperará ni se enviará confirmación a los mensajes"));
		commands.connect();
	}

	@Override
	public void disconnect() {
		commands.disconnect();
	}
	
	@Override
	public void connected() {
		if (akEnabled) {
			akController.start();
		}
		observer.connected();
	}

	/* Evento de desconexión, simplemente lo notificamos a la capa superior. */
	@Override
	public void disconnected() {
		CommunicationException akFailureCause = null;
		if(akEnabled) {
			akFailureCause = checkAkFailureCause(akFailureError, null);
			akFailureError = null;
			akController.stop();
		}
		if (akFailureCause != null) {
			observer.disconnected(akFailureCause);
		} else {
			observer.disconnected();
		}
	}

	/* Evento de desconexión con error, simplemente lo notificamos a la capa superior. */
	@Override
	public void disconnected(final CommunicationException cause) {
		CommunicationException akFailureCause = null;
		if (akEnabled) {
			akFailureCause = checkAkFailureCause(akFailureError, cause);
			akFailureError = null;
			akController.stop();
		}
	
		final CommunicationException __cause = akFailureCause != null ? akFailureCause : cause;
		Log.error(this, 
			PrintUtils.format("Recibido evento de desconexión en capa de mensajes. Error: '%s'", __cause.getMessage())
		);
		observer.disconnected(__cause);
	}

	/*
	 * Comprueba si la desconexión procede realmente de un fallo por AK. En caso
	 * de recibirse otro motivo y existir también el motivo por fallo de AK, se
	 * crea un nuevo error que encapsule a ambos.
	 */
	private CommunicationException checkAkFailureCause(final String error, final CommunicationException cause) {
		if (!StrUtils.hasChars(error, Boolean.TRUE)) {
			return null;
		}
		
		return cause != null ? new CommunicationException(CommErrorType.AK_FAILURE, error, cause)
			: new CommunicationException(CommErrorType.AK_FAILURE, error);
	}

	/* Evento de error, lo notificamos a la capa superior. */
	@Override
	public void error(final String error, final CommunicationException cause) {
		observer.error(error, cause);
	}

	/* Envia un mensaje, asignando primero un número de secuencia en caso de que aun no lo tenga definido. */
	@Override
	public void send(final Message<?> message) {
		if (message.getSequenceNumber().isNotDefined()) {
			message.setSequenceNumber(sequenceNumber.incrementAndGet());
		}
		if (akEnabled) { /* nos aseguramos de que el mensaje espera ak antes de enviarlo */
			akController.messageSent(message.getSequenceNumber(), message);	
		}
		Log.debug(this, PrintUtils.format("Enviando mensaje: '%s'", message));
		this.commands.send(message); 
	}

	/**
	 * Recibe un DataTsdu de RFC1006. Se transforma en un mensaje que se notifica a la capa superior.
	 */
	@Override
	public void receive(final DataTsdu tsdu) {
		final Message<?> message = extractMessage(tsdu);
		if (message != null) {
			if (!akEnabled) {
				/* Si no hay AK configurado simplemente se informa del mensaje recibido */
				observer.receive(message);
				return;
			}
			
			/* Con AK activado, comportamiento normal */
			if (isAk(message)) {
				/* Si es AK no se notifica a capa superior sino al controlador. */
				final MessageAK ak = (MessageAK) message;
				akController.receiveAK(ak.getConfirmedSequenceNumber());
			} else {
				/* Si es un mensaje normal confirmamos con un AK y notificamos a la capa superior. */
				observer.receive(message);
				sendAk(message);
			}
		}
	}

	/* Envía un mensaje de confirmación con número de secuencia cero y cuyo contenido es el número de secuencia del mensaje confirmado. */
	private void sendAk(final Message<?> message) {
		final SequenceNumber confirmedSN = new SequenceNumber(message.getSequenceNumber());
		final SequenceNumber akSN = new SequenceNumber(AK_SEQUENCE_NUMBER);
		final MessageAK ak = MessageFactory.getMessage(akSN, MessageType.AK);
		
		ak.setConfirmedSequenceNumber(confirmedSN);
		
		Log.info(this, PrintUtils.format("Enviando AK, confirma recepción de mensaje %s: '%s'", confirmedSN, ak));
		this.commands.send(ak);
	}

	/* Indica si un mensaje es una confirmación, un AK. */
	private boolean isAk(final Message<?> message) {
		return MessageType.AK.equals(message.getMessageType());
	}

	/* Extrae el mensaje del DataTsdu. En caso de error, devuelve null. */
	private Message<?> extractMessage(final DataTsdu tsdu) {
		Message<?> message = null;
		try {
			message = MessageFactory.getMessage(tsdu);
		} catch (final Exception e) {
			final String error = PrintUtils.format("Error recibiendo DataTsdu, bytes '%s'.",  
				(tsdu != null && tsdu.getBytes() != null ? PrintUtils.print(tsdu.getBytes()) : StrUtils.NULL_STRING));
			Log.error(this, error, e);
			error(error, new CommunicationException(CommErrorType.PARSE_MESSAGE, e));
		}
		return message;
	}

	/** Se notifica orden de reenvío de un mensaje. */
	@Override
	public void resendMessage(final Message<?> message) {
		Log.debug(this, PrintUtils.format("Se reenvía mensaje '%s'", message));
		send(message); /* Se respetará su número de secuencia. */
	}

	/** Se notifica un error por no recibir AK tras el máximo de intentos. Se desconecta la librería. */
	@Override
	public void akFailure(final SequenceNumber key, final Message<?> message) {
		final String error = 
			PrintUtils.format("Mensaje '%s' con id '%s' no ha recibido AK. Se produce desconexión.", message, key);
		Log.fatal(this, error);
		
		if (akFailureError == null) {
			akFailureError = error;
		}
		disconnect();
	}
}
