package libcomm.layer.socket;

import libcomm.connection.ConnectionMode;
import libcomm.context.ConnectionContext;
import libcomm.exception.CommErrorType;
import libcomm.exception.CommunicationException;
import libcomm.layer.AbstractLayer;
import libcomm.layer.ILayerObserver;
import libcomm.message.rfc1006.IBytes;

import commons.log.Log;
import commons.util.PrintUtils;

/**
 * Capa de comunicación a nivel de sockets.
 * <p>
 * 31/01/2016 13:01:23
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class SocketLayer extends AbstractLayer<IBytes, IBytes> {
	/* Observador de los hilos de sockets. Recibirá un IBytes como mensaje. */
	/* Recibe mensajes de la capa superior en forma de IBytes */

	/* Observador a quien notifica la capa de sockets */
	private final ILayerObserver<IBytes> observer;

	/* Contexto de la conexión. */
	private final ConnectionContext context;

	/* Modo cliente o servidor indicado en context */
	private final ConnectionMode mode;

	/* Manejador de los canales, distinto según modo cliente o servidor. */
	private AbstractChannelHandler channelHandler;

	/* Procesador-enviador de mensajes. */
	private SocketMessageProcessor sender;

	/* Procesador-recibidor de mensajes. */
	private SocketMessageProcessor receiver;

	/**
	 * Constructor de clase.
	 * 
	 * @param context
	 *            Contexto de la conexión.
	 * @param observer
	 *            Observador a quien notificar los eventos de conexión y
	 *            desconexión, y recepción de mensajes.
	 */
	public SocketLayer(final ConnectionContext context,	final ILayerObserver<IBytes> observer) {
		this.context = context;
		this.mode = context.getConnectionMode();
		this.observer = observer;
	}

	@Override
	public void connect() {
		final ConnectionMode connectionMode = context.getConnectionMode();

		channelHandler = null;
		if (ConnectionMode.CLIENT.equals(connectionMode)) {
			channelHandler = new ClientChannelHandler(context, this);
		} else if (ConnectionMode.SERVER.equals(connectionMode)) {
			channelHandler = new ServerChannelHandler(context, this);
		} else {
			notifyErrorDuringConnection(
				getText("No se permite conexión en modo distinto a cliente o servidor '%s'", context.printConnection())
			);
		}
		Log.debug(this, getText("Iniciando conexión '%s'", context.printConnection()));

		channelHandler.connect();
		startMessageProcessors();
	}

	/* Inicia los procesadores de mensajes, enviador y recibidor. */
	private void startMessageProcessors() {
		final String threadName = channelHandler.getThreadName();
		sender = new SocketMessageProcessor(PrintUtils.format("Tx %s", threadName), new SocketMessageSender(this.channelHandler));
		receiver = new SocketMessageProcessor(PrintUtils.format("Rx %s", threadName), new SocketMessageReceiver(this.observer));

		sender.start();
		receiver.start();
	}

	/* Inicia los procesadores de mensajes, enviador y recibidor. */
	private void stopMessageProcessors() {
		if (sender != null) {
			sender.stop();
		}

		if (receiver != null) {
			receiver.stop();
		}
	}

	@Override
	public void receive(final IBytes message) {
		Log.debug(this, getText("Recibidos bytes de mensaje '%s'", PrintUtils.print(message.getBytes())));
		receiver.addMessage(message);
	}

	@Override
	public void send(final IBytes message) {
		Log.debug(this, getText("Enviando bytes de mensajes '%s'", PrintUtils.print(message.getBytes())));
		sender.addMessage(message);
	}

	@Override
	public void disconnect() {
		Log.debug(this, getText("Iniciando desconexión '%s'", context.printConnection()));
		channelHandler.disconnect(null);
	}

	/* Notifica al observador un error durante una desconexión. */
	private void notifyErrorDuringConnection(final String message) {
		notifyErrorDuringConnection(message, null);
	}

	/*
	 * Notifica al observador un error durante una desconexión. Se puede
	 * acompañar de una excepción motivo del error (opcional).
	 */
	private void notifyErrorDuringConnection(final String message, final Throwable cause) {
		this.observer.error(message, new CommunicationException(CommErrorType.CONNECTION_ERROR, cause));
	}

	@Override
	public void connected() {
		Log.debug(this, getText("Establecida conexión '%s'", context.printConnection()));
		observer.connected();
	}

	@Override
	public void disconnected() {
		Log.debug(this, getText("Desconexión de '%s'", context.printConnection()));
		stopMessageProcessors();
		observer.disconnected();
	}

	@Override
	public void disconnected(final CommunicationException cause) {
		Log.error(this, getText("Desconexión inesperada de '%s'", context.printConnection()));
		stopMessageProcessors();
		observer.disconnected(cause);
	}

	@Override
	public void error(final String message, final CommunicationException cause) {
		Log.error(this, getText("Error durante el establecimiento de conexión '%s'", context.printConnection()));
		observer.error(message, cause);
	}

	/* Devuelve el texto facilitado con sus parámetros y siempre precedido de "[SOCKET] " */
	private String getText(final String message, final Object... params) {
		return PrintUtils.format("[%s] %s", mode, PrintUtils.format(message, params));
	}
}
