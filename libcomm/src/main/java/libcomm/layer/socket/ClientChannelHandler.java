package libcomm.layer.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import libcomm.connection.IConnection;
import libcomm.context.ConnectionContext;
import libcomm.exception.CommErrorType;
import libcomm.exception.CommunicationException;
import libcomm.layer.ILayerObserver;
import libcomm.message.rfc1006.IBytes;

import commons.log.Log;
import commons.util.PrintUtils;

/**
 * Establece la conexión de los canales en modo cliente. La gestión de mensajes
 * enviados y recibidos se hace en la superclase.
 * <p>
 * 01/02/2016 22:20:23
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class ClientChannelHandler extends AbstractChannelHandler {
	/* Formato para el nombre de la conexión: nombre [contador] (CLIENT, ip:puerto) */
	private final static String THREAD_NAME_FORMAT = "%s [%s] (%s, %s:%s)";

	/* Integer con operaciones atómicas para establecer el contador. */
	private final static AtomicInteger counter = new AtomicInteger(0);
	
	/* Contador para distiguir un intento de conexión de otro en el nombre del hilo. */
	private static int count = 0;
	
	/* Flag para controlar si está conectando. */
	private volatile boolean isConnecting;
	
	/* Flag para controlar si se ha forzado una desconexión durante tiempo de conexión */
	private volatile boolean externalInterruption;
	
	/* Objeto que encapsula los canales */
	private SocketData socketData;
	
	/* Canal de entrada. */
	private SocketChannel inputChannel;
	
	/* Canal de salida.*/
	private SocketChannel outputChannel;
	
	/* Objeto que encapsula los datos de conexión */
	private IConnection connection;
	
	/* Constructor, recibe contexto y observador de conexión. */
	ClientChannelHandler(final ConnectionContext context, final ILayerObserver<IBytes> observer) {
		super(context, ClientChannelHandler.class, observer);
		this.connection = getContext();
		this.isConnecting = Boolean.FALSE;
		this.externalInterruption = Boolean.FALSE;
		count = counter.incrementAndGet();
	}

	private void prepareForConnection() {
		this.isConnecting = Boolean.TRUE;
		this.externalInterruption = false;
		this.inputChannel = null;
		this.outputChannel = null;
		this.socketData = getSocketData();
	}
	
	@Override
	public void run() {
		prepareForConnection();
		
		Log.debug(this, PrintUtils.format("Iniciando hilo de conexión de cliente para '%s'", connection.printConnection()));
		
		boolean connected = false;
		boolean errorNotified = false;
		
		try {
			connected = tryConnect(connection.getConnectionTimeout());
			if (connected) {
				socketData.registerChannels(inputChannel, outputChannel);
				connected = socketData.checkConnection(); 
			} 
		} catch (Exception e) {
			connectingError (inputChannel, outputChannel, e);
			errorNotified = true;
		} finally {
			this.isConnecting = Boolean.FALSE;
		}

		Log.debug(this, PrintUtils.format("Fin de espera de conexión en cliente '%s'. %s", connection.printConnection(), 
			(connected ? "Conexión establecida con éxito." : "No se ha conseguido establecer la conexión."))
		);
		
		if (!connected && !errorNotified) { 
			connectingError(inputChannel, outputChannel, new CommunicationException(CommErrorType.TIMEOUT));
		}
		
		notifyConnectionResult(connected);

		if (connected) {
			Log.debug(this, "Iniciada espera de eventos en cliente");
			waitForEvents();
		}
	}

	private void connectingError(final SocketChannel inputChannel, final SocketChannel outputChannel, final Exception e) {
		try {
			closeAll(socketData, inputChannel, outputChannel);
		} catch (IOException io) {
			Log.error(this, PrintUtils.format("Error cerrando canales, sockets, selectores para conexión '%s'", 
				connection.printConnection()), e);
		}
		if (!externalInterruption) { /* Si ha sido un error no provocado por cancelación forzada de la conexión */
			notifyErrorDuringConnection(PrintUtils.format("Error durante espera de conexión '%s'", 
				connection.printConnection()), e);
		}
	}

	/*
	 * Intenta conectar dos canales. Sólo devolverá un estado de conectado
	 * (<code>true</code>) en caso de haber logrado conectar con éxito <b>ambos
	 * canales</b>, antes de cumplirse el timeout de conexión.
	 */
	private boolean tryConnect(final long timeout) throws IOException {
		boolean outputConnected, inputConnected = false;
		long remainingTimeout = timeout;
		final long initialTime = System.currentTimeMillis();
		
		Log.debug(this, PrintUtils.format(
			"Iniciando espera de conexión en cliente, canal de salida. Maximo '%s' milisegundos.", remainingTimeout)
		);
		outputChannel = connectionWait(remainingTimeout);
		
		outputConnected = channelConnected(outputChannel);
		if (outputConnected) {
			remainingTimeout -= (System.currentTimeMillis() - initialTime);
			if (remainingTimeout > 0) {
				Log.debug(this, PrintUtils.format(
					"Iniciando espera de conexión en cliente, canal de entrada. Maximo '%s' milisegundos.", remainingTimeout)
				);
				inputChannel = connectionWait(remainingTimeout);
				inputConnected = channelConnected(inputChannel);
			}
		}
		
		return outputConnected && inputConnected;
	}

	/* Indica si un canal está conectado, para ello debe ser distinto de null, estar abierto y estar conectado. */
	private static boolean channelConnected (final SocketChannel channel) {
		return channel != null && channel.isOpen() && channel.isConnected();
	}
	
	/*
	 * Espera de conexión de los canales hasta terminar el tiempo máximo de
	 * espera. Devuelve el canal sólo si ha conseguido conectar. Devuelve nulo
	 * en caso contrario.
	 */
	private SocketChannel connectionWait(final long timeout) throws IOException {
		final InetSocketAddress socketAddress = getAddress();
		
		final Selector selector = Selector.open();
		SocketChannel channel = null;

		final long initialTime = System.currentTimeMillis();
		
		long remainingTimeout = timeout;
		boolean connected = false;
		
		do {
			Log.debug(this, PrintUtils.format("Quedan '%s' milisegundos para establecer la conexión", remainingTimeout));
			channel = SocketChannel.open();
			channel.configureBlocking(Boolean.FALSE);
			channel.register(selector, SelectionKey.OP_CONNECT);
			
			channel.connect(socketAddress);
			int selectedChannels = selector.select(remainingTimeout);

			if (selectedChannels == 1) {
				final Iterator<SelectionKey> selectionIterator = selector.selectedKeys().iterator();
				final SelectionKey selectedKey = selectionIterator.hasNext() ? selectionIterator.next() : null;
				
				if (selectedKey != null && selectedKey.isValid() && selectedKey.isConnectable()) {
					try {
						channel.finishConnect();
					} catch (IOException e) {
						Log.error(this, e.getMessage());
					}
					selectionIterator.remove();
				}

				connected = channel.isConnected();
				if (connected) {
					Log.debug(this, "Canal conectado");
				} else {
					selectedKey.cancel();
				}
			}

			remainingTimeout = timeout - (System.currentTimeMillis() - initialTime);
		} while (!connected && remainingTimeout > 0);
		
		return connected ? channel : null;
	}

	/* Obtiene el nombre del hilo. */
	@Override
	String getThreadName() {
		return PrintUtils.format(THREAD_NAME_FORMAT, 
			connection.getConnectionName(), count, connection.getConnectionMode(), connection.getHost(), connection.getPort());
	}

	@Override
	protected boolean isConnecting() {
		return isConnecting;
	}

	@Override
	protected void interruptConnection() throws IOException {
		if (isConnecting()) {
			externalInterruption = Boolean.TRUE;
			
			final Thread connectionThread = getConnectionThread();
			if (connectionThread != null) {
				connectionThread.interrupt();
			}
		}
	}
	
	/* Notifica el resultado de la conexión al observador de conexiones. */
	protected void notifyConnectionResult(final boolean connected) {
		if (connected) {
			getObserver().connected();
		} else if (!externalInterruption) { /* si es desconexión externa ya se se notifica desde la superclase la desconexión */
			getObserver().disconnected();
		}
	}
}
