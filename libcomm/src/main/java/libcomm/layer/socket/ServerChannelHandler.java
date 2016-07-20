package libcomm.layer.socket;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import libcomm.connection.IConnection;
import libcomm.context.ConnectionContext;
import libcomm.layer.ILayerObserver;
import libcomm.message.rfc1006.IBytes;

import commons.log.Log;
import commons.util.PrintUtils;

/**
 * Establece la conexión de los canales en modo servidor, haciendo que esperen y acepten
 * conexiones. La gestión de mensajes enviados y recibidos se hace en la superclase.
 * <p>
 * 11/01/2016 01:39:58
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class ServerChannelHandler extends AbstractChannelHandler {
	
	/* Formato para el nombre de la conexión: nombre [contador] (SERVER, puerto) */
	private final static String THREAD_NAME_FORMAT = "%s [%s] (%s, %s)";

	/* Integer con operaciones atómicas para establecer el contador. */
	private final static AtomicInteger counter = new AtomicInteger(0);
	
	/* Contador para distiguir un intento de conexión de otro en el nombre del hilo. */
	private static int count = 0;
	
	/* Flag para controlar si se ha forzado una desconexión durante tiempo de conexión */
	private volatile boolean connectionInterrupted;
	
	/* Flag para controlar si está conectando. */
	private volatile boolean isConnecting;
	
	/* Objeto que encapsula los canales */
	private SocketData socketData;
	
	/* Objeto que encapsula los datos de conexión */
	private IConnection connection;

	private ServerSocketChannel serverChannel;
	
	/* Constructor, recibe contexto y observador de conexión. */
	ServerChannelHandler(final ConnectionContext context, final ILayerObserver<IBytes> observer) {
		super(context, ServerChannelHandler.class, observer);
		this.connection = getContext();
		this.isConnecting = Boolean.FALSE;
		this.connectionInterrupted = Boolean.FALSE;
		count = counter.incrementAndGet();
	}

	private void prepareForConnection() {
		this.isConnecting = Boolean.TRUE;
		this.connectionInterrupted = false;
		this.serverChannel = null;
		this.socketData = getSocketData();
	}

	/**
	 * Conecta dos canales y se queda esperando eventos una vez conectados. Si se produce algún error se notifica, salvo que sea una desconexión.
	 */
	@Override
	public void run() {
		prepareForConnection();

		Log.debug(this, PrintUtils.format("Iniciando hilo servidor de espera de conexión para '%s'", connection.printConnection()));
		
		SocketChannel inputChannel = null, outputChannel = null;
		boolean connected = false;
		try {
			serverChannel = ServerSocketChannel.open();
			serverChannel.socket().bind(getAddress());
			
			inputChannel = serverChannel.accept();
			if (inputChannel == null) {
				final String error = PrintUtils.format("No se ha establecido el canal de entrada, '%s'", connection.printConnection());
				Log.error(this, error);
				notifyErrorDuringConnection(error);
			}
			
			outputChannel = serverChannel.accept();
			if (outputChannel == null) {
				final String error = PrintUtils.format("No se ha establecido el canal de salida, '%s'", connection.printConnection());
				Log.error(this, error);
				notifyErrorDuringConnection(error);
			}

			socketData.registerChannels(inputChannel, outputChannel);
			connected = socketData.checkConnection();

			if (!connected) {
				socketData.close();
			}
			
			serverChannel.close(); /* conectados o no, no aceptaremos más conexiones en esta ejecución del hilo */
		} catch (Exception e) {
			connectingError(inputChannel, outputChannel, e);
		} finally {
			isConnecting = Boolean.FALSE;
		}
		
		Log.debug(this, PrintUtils.format("Fin de espera de conexión en servidor '%s'. %s", connection.printConnection(), 
			(connected ? "Conexión establecida con éxito." : "No se ha conseguido establecer la conexión."))
		);
		
		notifyConnectionResult(connected);
		
		if (connected) {
			Log.debug(this, "Iniciada espera de eventos en servidor");
			waitForEvents();
		}
	}

	private void connectingError(final SocketChannel inputChannel, final SocketChannel outputChannel, final Exception e) {
		try {
			closeAll(socketData, serverChannel, inputChannel, outputChannel);
		} catch (IOException io) {
			Log.error(this, PrintUtils.format("Error cerrando canales, sockets, selectores para conexión '%s'", 
				connection.printConnection()), e);
		}
		if (!connectionInterrupted) { /* Si ha sido un error no provocado por cancelación forzada de la conexión */
			notifyErrorDuringConnection(PrintUtils.format("Error durante espera de conexión '%s'", 
				connection.printConnection()), e);
		}
	}

	/* Obtiene el nombre del hilo. */
	@Override
	String getThreadName() {
		return PrintUtils.format(THREAD_NAME_FORMAT, 
			connection.getConnectionName(), count, connection.getConnectionMode(), connection.getPort());
	}

	@Override
	protected boolean isConnecting() {
		return isConnecting;
	}
	
	@Override
	protected void interruptConnection() throws IOException {
		if (isConnecting() && serverChannel != null) {
			connectionInterrupted = Boolean.TRUE;
			serverChannel.close();
		}
	}
	
	/* Notifica el resultado de la conexión al observador de conexiones. */
	@Override
	protected void notifyConnectionResult(final boolean connected) {
		if (connected) {
			getObserver().connected();
		}
	}
}
