package libcomm.layer.rfc1006;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import libcomm.connection.ConnectionMode;
import libcomm.context.ConnectionContext;
import libcomm.exception.CommErrorType;
import libcomm.exception.CommunicationException;
import libcomm.exception.MalformedMessageException;
import libcomm.layer.AbstractLayer;
import libcomm.layer.ILayerCommands;
import libcomm.layer.ILayerObserver;
import libcomm.layer.socket.SocketLayer;
import libcomm.message.rfc1006.CcTsdu;
import libcomm.message.rfc1006.CrTsdu;
import libcomm.message.rfc1006.DataTsdu;
import libcomm.message.rfc1006.IBytes;
import libcomm.message.rfc1006.ITsdu;
import libcomm.message.rfc1006.Tpkt;
import libcomm.util.MessageUtils;

import commons.log.Log;
import commons.util.ColUtils;
import commons.util.PrintUtils;
import commons.util.StrUtils;

/**
 * Capa de comunicación RFC1006. Se encarga de establecer conexión a nivel de
 * protocolo RFC1006 una vez se establece la comunicación por sockets. También
 * se encarga de traducir los mensajes a telegramas RFC1006 y viceversa.
 * <p>
 * 31/01/2016 13:06:22
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class Rfc1006Layer extends AbstractLayer<IBytes, IBytes> {
									/* Observador de la capa de sockets, recibirá un IBytes como mensaje. */ 
									/* Recibe mensajes de la capa superior en forma de bytes. */
	
	/* Observador a quien notifica la capa de RFC1006 */
	private final ILayerObserver<DataTsdu> observer;
	
	/* Comandos a la capa inferior. */
	private final ILayerCommands<IBytes> commands;
	
	/* Contexto de la conexión.*/
	private final ConnectionContext context;
	
	/* Operaciones con mensajes RFC1006 */
	private final Rfc1006IOMessage ioMessage;

	/* Flags para saber si hemos enviado CR y recibido CC (en modo cliente) o recibido CR y enviado CC (en modo servidor) */
	private final AtomicBoolean flagCR;
	private final AtomicBoolean flagCC;
	
	/* Configuración de tsap esperada. */
	private byte[] expectedCallingTsap;
	private byte[] expectedCalledTsap;

	private final boolean clientMode;
	
	/*
	 * La rececepción siempre será desde el hilo que recibe, pero el envío
	 * podría ser desde varios hilos si varios hilos distintos compartiesen el
	 * objeto de que controla la conexión. No es lo habitual, pero nos
	 * protegemos sincronizando el envío.
	 */
	private final Object sendLock;
	
	/* Hora a la que comienza la conexión, ayudará a identificar timeout de conexión a nivel de rfc1006. */
	private long startConnectionTime;
	
	
	/**
	 * Constructor de clase.
	 * 
	 * @param context
	 *            Contexto de la conexión.
	 * @param observer
	 *            Observador a quien notificar los eventos de conexión y
	 *            desconexión, y recepción de mensajes.
	 */
	public Rfc1006Layer(final ConnectionContext context, final ILayerObserver<DataTsdu> observer) {
		this.context = context;
		this.observer = observer;
		this.flagCR = new AtomicBoolean(Boolean.FALSE);
		this.flagCC = new AtomicBoolean(Boolean.FALSE);
		this.commands = new SocketLayer(context, this);
		this.ioMessage = new Rfc1006IOMessage();
		this.clientMode = ConnectionMode.CLIENT.equals(context.getConnectionMode());
		this.sendLock = new Object();
	}
	

	@Override
	public void connect() {
		Log.debug(this, PrintUtils.format("Recibida orden de conexión en capa RFC1006, '%s'", context.printConnection()));
		startConnectionTime = System.currentTimeMillis();
		commands.connect();
	}

	@Override
	public void disconnect() {
		Log.debug(this, PrintUtils.format("Recibida orden de desconexión en capa RFC1006, '%s'", context.printConnection()));
		commands.disconnect();
	}

	
	@Override
	public void connected() {
		Log.debug(this, PrintUtils.format("Recibido evento de conexión en capa RFC1006, %s",
			(clientMode ? "se solicita conexión RFC1006 (CR)" : "se esperan solicitudes de conexión RFC1006 (CR)"))
		);

		try {
			this.expectedCallingTsap = MessageUtils.encode(context.getCallingTsap());
			this.expectedCalledTsap = MessageUtils.encode(context.getCalledTsap());
		} catch (MalformedMessageException e) {
			final String err = "Error conectando en capa RFC1006, TSAP configurados incorrectamente";
			Log.error(this, err, e);
			disconnected(CommunicationException.createException(CommErrorType.TSAPS, err, context, e));
			return;
		}
		
		if (clientMode) {
			connectionRequest();
		}
	}

	private void connectionRequest() {
		if(!flagCR.getAndSet(Boolean.TRUE)) {
			try {
				final Tpkt<CrTsdu> cr = ioMessage.createCr(expectedCallingTsap, expectedCalledTsap);
				send(cr);
				waitForCC();
			} catch (Exception e) {
				Log.error(this, "Error enviando solicitud de conexión RFC1006 (CR)", e);
				connectionReset();
			}
		}
	}

	private void waitForCC() {
		final Thread waitCc = new Thread(new Runnable () {
			@Override
			public void run() {
				long remainingTime = context.getConnectionTimeout() - (System.currentTimeMillis() - startConnectionTime);
				while (!connectionStablished() && remainingTime > 0) {
					final long loopTime = System.currentTimeMillis();
					try {
						Thread.sleep(200L);
					} catch (InterruptedException e) {
						break;
					} finally {
						remainingTime -= System.currentTimeMillis() - loopTime;
					}
				}
				
				if (!connectionStablished()) {
					disconnected(
						new CommunicationException(CommErrorType.TIMEOUT, 
							"No se ha recibido CC en el tiempo necesario (timeout)")
					);
				}
			}
		});
		
		waitCc.setDaemon(Boolean.TRUE);
		waitCc.start();
	}


	/* Comprueba si se ha establecido la conexión. */
	private final boolean connectionStablished() {
		return flagCR.get() && flagCC.get();
	}

	/* Resetea la conexión a falso en caso de desconexión. */
	private void connectionReset() {
		Log.debug(this, "Se resetea establecimiento de conexión RFC1006, necesario intercambiar CR/CC de nuevo");
		flagCR.set(Boolean.FALSE);
		flagCC.set(Boolean.FALSE);
	}

	/* Evento de desconexión, simplemente lo notificamos a la capa superior. */
	@Override
	public void disconnected() {
		Log.debug(this, "Recibido evento de desconexion en capa RFC1006");
		connectionReset();
		observer.disconnected();
	}

	/* Evento de desconexión con error, simplemente lo notificamos a la capa superior. */
	@Override
	public void disconnected(final CommunicationException cause) {
		Log.error(this, 
			PrintUtils.format("Recibido evento de desconexión en capa RFC1006. Error: '%s'", cause.getMessage())
		);
		connectionReset();
		observer.disconnected(cause);
	}

	/* Evento de error, lo notificamos a la capa superior. */
	@Override
	public void error(final String error, final CommunicationException cause) {
		connectionReset();
		observer.error(error, cause);
	}

	/* Envia un mensaje IBytes, debe transformarlo antes a un formato entendible para el protocolo RFC1006. */
	@Override
	/* se sincroniza dado que puede recibirse desde varios hilos (envío normal y envío de AK) */
	public synchronized void send(final IBytes message) { 
		try {
			final Tpkt<DataTsdu> tpkt = ioMessage.createDt(message);
			send(tpkt);
		} catch (Exception e) {
			final String error = PrintUtils.format("Error enviando mensaje '%s' bytes '%s'.", message, 
				(message != null ? PrintUtils.print(message.getBytes()) : StrUtils.NULL_STRING));
			Log.error(this, error, e);
			this.error(error, new CommunicationException(CommErrorType.SENDING, e));
		}
	}

	/* Envía un mensaje RFC1006 a la capa de sockets. */
	private void send(final Tpkt<?> tpkt) {
		synchronized (sendLock) {
			commands.send(tpkt);
		}
	}
	
	/**
	 * Recibe un flujo de bytes codificado para RFC1006. Se separan los mensajes
	 * para procesarlos independientemente.
	 */
	@Override
	public void receive(final IBytes message) {
		final List<ITsdu> messages = ioMessage.readTSDUs(message);
		messages.iterator().forEachRemaining(tsdu -> receiveTsdu(tsdu));
	}
	
	/* Recibe un TSDU, según el tipo concreto de TSDU se tratará de un modo u otro. */
	private void receiveTsdu(final ITsdu tsdu) {
		try {
			if (tsdu instanceof DataTsdu) { /* DT, lo más habitual */
				receiveDT((DataTsdu) tsdu);
			} else if (tsdu instanceof CrTsdu) { /* solicitud de conexión */
				receiveCR((CrTsdu) tsdu);
			} else if (tsdu instanceof CcTsdu) { /* confirmación de conexión */
				receiveCC((CcTsdu) tsdu);
			} else {
				Log.error(this, 
					PrintUtils.format("Tipo de TSDU recibido no soportado, se ignora el mensaje recibido '%s'", tsdu)
				);
			}
		} catch (Exception e) {
			Log.error(this, PrintUtils.format("Error procesando TSDU RFC1006 recibido '%s'. Se descarta.", tsdu), e);
		}
	}

	/* Procesa un DT recibido, debe estar la conexión establecida a nivel RFC1006 o será descartado. */
	private void receiveDT(final DataTsdu tsdu) {
		if (!connectionStablished()) {
			Log.error(this, 
				PrintUtils.format("Recibido teletrama DT antes de establecer conexión, se ignora.")
			);
			return;
		}
		observer.receive(tsdu);
	}
	
	/* Procesa un CR recibido, debe estar en modo servidor pues el CR sólo lo envían los clientes. */
	private void receiveCR(final CrTsdu tsdu) throws CommunicationException {
		if (clientMode) {
			Log.error (this, PrintUtils.format("Recibido telegrama CR en modo cliente, se ignora"));
			return;
		}
		
		if (connectionStablished()) {
			Log.error(this, 
				PrintUtils.format("Recibido teletrama CR con la conexión RFC1006 ya establecida, se ignora.")
			);
			return;
		}

		if (!checkTSAPs("CR", tsdu.getCallingTsap(), tsdu.getCalledTsap())) {
			return;
		}
		
		if (!flagCR.getAndSet(Boolean.TRUE) && !flagCC.getAndSet(Boolean.TRUE)) {
			final Tpkt<CcTsdu> cc = ioMessage.createCc(expectedCallingTsap, expectedCalledTsap);
			send(cc);
		}
		
		if (connectionStablished()) {
			Log.debug(this, "Recibida solicitud de conexión RFC1006 que ha sido confirmada, se notifica a las capas superiores");
			observer.connected();
		} else {
			connectionReset();
		}
	}

	/*
	 * Procesa un CC recibido, debe estar en modo cliente pues el CC sólo lo
	 * envían los servidores. Además, debemos haber enviado el CR previamente.
	 */
	private void receiveCC(final CcTsdu tsdu) {
		if (!clientMode) {
			Log.error (this, PrintUtils.format("Recibido telegrama CC en modo servidor, se ignora"));
			return;
		}
		
		if (connectionStablished()) {
			Log.error(this, 
				PrintUtils.format("Recibido teletrama CC con la conexión RFC1006 ya establecida, se ignora.")
			);
			return;
		}
		
		if (!flagCR.get()) {
			Log.error(this, 
				PrintUtils.format("Recibido teletrama CC sin haber enviado solicitud de conexión, se ignora.")
			);
			return;
		}

		final boolean tsapOk = checkTSAPs("CC", tsdu.getCallingTsap(), tsdu.getCalledTsap()); 
		if (!flagCC.getAndSet(tsapOk)) {
			if (connectionStablished()) {
				Log.debug(this, "Recibida solicitud de conexión en RFC1006, se notifica a las capas superiores");
				observer.connected();
			} else {
				connectionReset();
			}
		}
	}

	private boolean checkTSAPs(final String message, final byte[] callingTsap, final byte[] calledTsap) {
		if (!ColUtils.equals(expectedCallingTsap, callingTsap)) {
			final String err = getTsapError(message, "calling.tsap", expectedCallingTsap, callingTsap);
			Log.fatal(this, err);
			disconnected(CommunicationException.createException(CommErrorType.TSAPS, err, context));
			return Boolean.FALSE;
		}
				
		if (!ColUtils.equals(expectedCalledTsap, calledTsap)) {
			final String err = getTsapError(message, "called.tsap", expectedCalledTsap, calledTsap);
			Log.fatal(this, err);
			disconnected(CommunicationException.createException(CommErrorType.TSAPS, err, context));
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
	
	/* Obtiene error de configuración en TSAP. */
	private String getTsapError(final String message, final String param, final byte[] expected, final byte[] received) {
		String error = null;
		try {
			error = PrintUtils.format(
				"Error estableciendo conexión RFC1006, %s recibido con '%s' incorrecto. Esperado '%s' Recibido '%s'",
				message, param, MessageUtils.decode(expected), MessageUtils.decode(received)
			);
		} catch (MalformedMessageException e) {
			Log.error(this, PrintUtils.format("Error decodificando parámetro '%s'", param), e);
		}
		return error;
	}
}
