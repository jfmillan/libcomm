package libcomm.layer.socket;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import libcomm.connection.ConnectionMode;
import libcomm.context.ConnectionContext;
import libcomm.exception.CommErrorType;
import libcomm.exception.CommunicationException;
import libcomm.layer.ILayerObserver;
import libcomm.layer.IReceiver;
import libcomm.layer.ISender;
import libcomm.message.rfc1006.IBytes;
import libcomm.util.BufferUtils;

import commons.log.Log;
import commons.util.Constants;
import commons.util.PrintUtils;
import commons.util.StrUtils;

/**
 * Clase abstracta que arranca el hilo de comunicación para todos los canales y
 * escucha y recibe los eventos de lectura y escritura para los canales de
 * entrada y salida respectivamente.
 * <p>
 * La implementación concreta de la conexión dependerá del modo de conexión,
 * cliente o servidor.
 * <p>
 * 10/01/2016 23:51:05
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
abstract class AbstractChannelHandler implements Runnable, IReceiver<IBytes>, ISender<IBytes> { 
	/* Contexto de conexión.*/
	private final ConnectionContext context;
	
	/* Clase para logs */
	private final Class<? extends AbstractChannelHandler> loggerClass;
	
	/* Observador de la conexión. */
	private final ILayerObserver<IBytes> observer;

	/* Hilo de conexión: establece la conexión y maneja los eventos recibidos y enviados. */
	private volatile Thread connectionThread;
	
	/* Datos del Socket construido. */
	private volatile SocketData socket;
	
	/* Dirección de conexión. */
	private InetSocketAddress address;
	
	/* Semáforo para controlar el envío de mensajes junto con readyToSend. */
	private final Semaphore outputSemaphore;
	
	/* AtomicBoolean para controlar el envío de mensajes junto con outputSemaphore. */
	private final AtomicBoolean readyToSend;
	
	/* Constructor, recibe el contexto y el observador de la conexión. */
	AbstractChannelHandler (final ConnectionContext context, final Class<? extends AbstractChannelHandler> logClass, 
			final ILayerObserver<IBytes> observer) {
		this.context = context;
		this.loggerClass = logClass;
		this.observer = observer;
		
		outputSemaphore = new Semaphore(Constants.ZERO);
		readyToSend = new AtomicBoolean(Boolean.FALSE);
	}
	
	/** Inicia el hilo de conexión de los canales.*/
	synchronized void connect() {
		Log.debug(loggerClass, PrintUtils.format("Iniciando hilo de conexión '%s'", context.printConnection()));
		if (isConnecting()) {
			notifyErrorDuringConnection(
				PrintUtils.format("No se puede conectar '%s', ya existe un hilo de conexión activo '%s'", 
					context.printConnection(), getThreadName())
			);
			return;
		}
		
		try {
			socket = new SocketData();
			connectionThread = new Thread(this, getThreadName());
			connectionThread.setDaemon(Boolean.TRUE);
			connectionThread.setPriority(Thread.MAX_PRIORITY);
			connectionThread.start();
		} catch (final Exception e) {
			notifyErrorDuringConnection(PrintUtils.format("No se puede conectar '%s'", context.printConnection()), e);
		}
	}

	/**
	 * Desconexión.
	 * 
	 * @param cause
	 *            Causa de la desconexión.
	 */
	synchronized void disconnect (final CommunicationException cause) {
		Log.debug(loggerClass, PrintUtils.format("Desconectando '%s' %s", context.printConnection(), 
			(cause != null ? PrintUtils.format("con error: %s", cause.getMessage()) : StrUtils.EMPTY_STRING)));
		
		if (connectionThread == null) {
			return;
		}
		
		if (isConnecting()) {
			try {
				interruptConnection();
			} catch (final Exception e) {
				Log.error(loggerClass, "Error desconectando, se ignora", e);
			}
		} else {
			connectionThread.interrupt();
		}
		
		if (socket != null) {
			socket.close();
		}
		
		if (cause != null) {
			observer.disconnected(cause);
		} else {
			observer.disconnected();
		}
		connectionThread = null;
	}

	/* Espera eventos de lectura (recepción) o de escritura (envío) */
	void waitForEvents() {
		final Selector selector = socket.selector();
		if (selector == null) {
			Log.fatal (loggerClass, "No se han registrado correctamente los canales en el selector o está cerrado");
			return;
		}
		
		Iterator<SelectionKey> selectedKeys;
		SelectionKey key;
		while (stillAlive(connectionThread)) {
			try {
				final int readyChannels = selector.select();
				if (readyChannels == 0) {
					continue;
				}
				
				selectedKeys = selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					key = selectedKeys.next();
					
					if (!key.isValid()) {
						continue;
					}
					
					if (key.isReadable()) {
						readFromChannel(key);
					} else if (key.isWritable()) {
						writeIntoChannel(key);
					}
					
					selectedKeys.remove();
				}
			} catch (Throwable e) {
				if (!stillAlive(connectionThread)) {
					final String error = "Interrumpido hilo de manejo de eventos en canales de socket";
					Log.error(loggerClass, error, e);
					disconnect(new CommunicationException(CommErrorType.COMMUNICATION_ERROR, e));
				} else {
					Log.error(loggerClass, e.getMessage(), e);
				}
			}
		}
	}

	protected boolean stillAlive(final Thread thread) {
		return thread != null && thread.isAlive() && !thread.isInterrupted();
	}

	/* Lee los datos de un canal. */
	private void readFromChannel(final SelectionKey key) throws IOException {
		final SocketChannel channel = (SocketChannel) key.channel();
		if (!socket.isInputChannel(channel)) {
			Log.debug(loggerClass, "Recibido evento de lectura en canal distinto a canal de entrada, se descarta");
			return;
		}

		final ByteBuffer buffer = (ByteBuffer) key.attachment();
		final byte[] readedBytes = socket.read(buffer);
		
		if (readedBytes != null) {
			receive(readedBytes);			
		} else { /* desconexión */
			disconnect(new CommunicationException(CommErrorType.REMOTE_DISCONNECTION));
		}
	}

	/* Escribe los datos en un canal. */
	private void writeIntoChannel(final SelectionKey key) throws IOException {
		final SocketChannel channel = (SocketChannel) key.channel();
		if (!socket.isOutputChannel(channel)) {
			Log.debug(loggerClass, "Recibido evento de escritura en canal distinto a canal de salida, se descarta");
			return;
		}
		
		if (readyToSend.getAndSet(Boolean.FALSE)) {
			final ByteBuffer buffer = (ByteBuffer) key.attachment();
			socket.write(buffer);
			socket.outputInterest(SelectionKey.OP_READ);
			outputSemaphore.release();
		}
	}
	
	/* Recibe un mensaje, leyendo del canal de entrada. */
	void receive (final byte[] messageBytes) {
		if (messageBytes != null && messageBytes.length > 0) {
			receive(BufferUtils.getIBytes(messageBytes));
		}
	}

	@Override
	public void receive (final IBytes message) {
		this.observer.receive(message);
	}

	/* Envía un mensaje, escribiendo en el canal de salida. */
	@Override
	public void send(final IBytes message) {
		if (message == null) {
			return;
		}
		
		final byte[] messageBytes = message.getBytes();
		if (messageBytes == null || messageBytes.length == 0) {
			return;
		}

		final ByteBuffer buffer = socket.writeBuffer();
		BufferUtils.writeInToBuffer(buffer, messageBytes, Boolean.TRUE);
		
		if (buffer.hasRemaining()) {
			forceWritingChannel();
		}
		
	}

	/* Fuerza la escritura en el canal. */
	private void forceWritingChannel () {
		if (!readyToSend.getAndSet(Boolean.TRUE)) {
			socket.outputInterest(SelectionKey.OP_WRITE);
			socket.wakeup();
			try {
				outputSemaphore.acquire(); /* Esperamos a que se envíe */
			} catch (InterruptedException e) {
				Log.error(this, "Error esperando a que se escriba en el canal, hilo interrumpido", e);
				disconnect(new CommunicationException (CommErrorType.SOCKET_ERROR, e));
			}
		}
	}
	
	/**
	 * Indica si se está en medio del proceso de conexión.
	 * 
	 * @return Indica si está conectando o no.
	 */
	protected abstract boolean isConnecting();

	/**
	 * Interrumpe la conexión. Si se consigue interrumpir correctamente, no
	 * debería lanzar ningún error.
	 * 
	 * @throws IOException
	 *             en caso de error.
	 */
	protected abstract void interruptConnection() throws IOException;

	/**
	 * Obtiene el contexto. Sólo accesible a subclases.
	 * 
	 * @return Contexto de conexión.
	 */
	protected ConnectionContext getContext() {
		return this.context;
	}
	
	/**
	 * Obtiene el observador de conexión. Sólo accesible a subclases.
	 * 
	 * @return Observador.
	 */
	protected ILayerObserver<IBytes> getObserver() {
		return this.observer;
	}
	
	/**
	 * Obtiene el contenedor de datos del socket. Sólo accesible a subclases.
	 * 
	 * @return Datos de sockets.
	 */
	protected SocketData getSocketData() {
		return this.socket;
	}
	
	/**
	 * Obtiene la dirección de conexión, según estemos en modo cliente o
	 * servidor.
	 * 
	 * @return Objeto
	 *            con los datos de dirección de la conexión.
	 */
	protected InetSocketAddress getAddress() {
		if (this.address == null) {
			setAddress();
		}
		return this.address;
	}
	
	/* Establece y obtiene internamente la dirección de conexión, según estemos en modo cliente o servidor. */
	private void setAddress() {
		final InetSocketAddress inetAddress;
		
		if (ConnectionMode.CLIENT.equals(context.getConnectionMode())) {
			inetAddress = new InetSocketAddress(context.getHost(), context.getPort());
		} else if (ConnectionMode.SERVER.equals(context.getConnectionMode())) {
			inetAddress = new InetSocketAddress(context.getPort());
		} else {
			inetAddress = null;
		}
		
		this.address = inetAddress;
	}
	
	
	
	/**
	 * Obtiene el nombre del hilo.
	 * 
	 * @return Nombre del hilo.
	 */
	abstract String getThreadName();
	
	/** Obtiene el hilo de la conexión.
	 * @return Hilo de la conexión. */
	protected Thread getConnectionThread() {
		return connectionThread;
	}
	
	/* Notifica al observador un error durante una desconexión. */
	protected void notifyErrorDuringConnection(final String message) {
		notifyErrorDuringConnection(message, null);
	}
	
	/* Notifica al observador un error durante una desconexión. Se puede acompañar de una excepción motivo del error (opcional). */
	protected void notifyErrorDuringConnection(final String message, final Throwable cause) {
		this.observer.error(message, new CommunicationException(CommErrorType.CONNECTION_ERROR, cause)); 
	}

	/* Notifica el resultado de la conexión al observador de conexiones. */
	abstract void notifyConnectionResult(final boolean connected);
	
	/**
	 * Permite cerrar varios elementos {@link Closeable} controlando nulos.
	 * 
	 * @param closeables
	 *            Elementos cerrables a cerrar.
	 * @throws IOException si se produce algún error.
	 */
	protected static void closeAll (final Closeable...closeables) throws IOException {
		if (closeables != null) {
			for (final Closeable closeable : closeables) {
				if (closeable != null) {
					closeable.close();
				}
			}
		}
	}
}
