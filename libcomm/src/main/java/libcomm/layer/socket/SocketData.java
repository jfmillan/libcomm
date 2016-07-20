package libcomm.layer.socket;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import libcomm.exception.CommErrorType;
import libcomm.exception.CommunicationException;
import libcomm.util.BufferUtils;

import commons.log.Log;

/**
 * Datos de un socket conectado. Mantiene las referencias necesarias para poder
 * gestionar el envío y recepción de mensajes.
 * <p>
 * 31/01/2016 12:39:16
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class SocketData implements Closeable {
	/* Marca de desconexión remota, sabremos que hemos alcanzado el final del canal (desconexión) cuando leamos -1 bytes */
	private static final int DISCONNECTION_MARK = -1;

	/* Canal de entrada. */
	private SocketChannel inputChannel;

	/* Canal de salida. */
	private SocketChannel outputChannel;

	/* Buffer de lectura, utilizado por el canal de entrada. */
	private ByteBuffer readBuffer;
	
	/* Buffer de escritura, utilizado por el canal de salida. */
	private ByteBuffer writeBuffer;
	
	/* Selector que registra los canales. */
	private Selector selector;

	/* Booleano atómico para comprobar si el socket está abierto o cerrado. */
	private final AtomicBoolean isOpen;

	/* Clave de entrada para poder modificar sus intereses. */
	private SelectionKey inputKey;

	/* Clave de salida para poder modificar sus intereses. */
	private SelectionKey outputKey;
	
	/* Constructor. */
	SocketData() {
		this.isOpen = new AtomicBoolean(Boolean.TRUE);
	}
	
	/*
	 * Registralos canales de entrada y salida, así como un selector para
	 * gestionarlos.
	 */
	void registerChannels(final SocketChannel inputChannel,
			final SocketChannel outputChannel) throws CommunicationException {
		if (!isOpen.get()) {
			throw createError("Socket cerrado, no se pueden registrar canales");
		}

		this.inputChannel = inputChannel;
		this.outputChannel = outputChannel;

		try {
			if (this.inputChannel.isBlocking()) {
				this.inputChannel.configureBlocking(Boolean.FALSE);
			}
			
			if (this.outputChannel.isBlocking()) {
				this.outputChannel.configureBlocking(Boolean.FALSE);
			}
			
			this.selector = Selector.open();
			
			readBuffer = ByteBuffer.allocate(inputChannel.socket().getReceiveBufferSize());
			readBuffer.flip();
			inputKey = inputChannel.register(selector, SelectionKey.OP_READ, readBuffer);
			
			writeBuffer = ByteBuffer.allocate(outputChannel.socket().getSendBufferSize());
			writeBuffer.flip();
			outputKey = outputChannel.register(selector, SelectionKey.OP_READ, writeBuffer);
		} catch (IOException e) {
			throw createError("Error registrando canales en el selector", e);
		}
	}

	/* Obtiene el selector */
	Selector selector() {
		return isOpen.get() ? selector : null;
	}
	
	/* Despierta inmediatamente el selector, si estaba esperando. */
	Selector wakeup() {
		if (selector != null && selector.isOpen() && isOpen.get()) {
			return selector.wakeup();
		}
		return null;
	}
	
	/* Devuelve el buffer de lectura utilizado por el canal de entrada. */
	ByteBuffer readBuffer() {
		return this.readBuffer;
	}
	
	
	/* Devuelve el buffer de escritura utilizado por el canal de salida. */
	ByteBuffer writeBuffer() {
		return this.writeBuffer;
	}

	/* Establece los intereses de escucha de un SelectionKey. */
	private void setInterest (final SelectionKey key, final int interest) {
		if (isOpen.get() && selector.isOpen() && key != null && key.isValid()) {
			key.interestOps(interest);
		}
	}
	
	/* Establece los intereses para el canal de entrada */
	void inputInterest (final int interest) {
		setInterest (inputKey, interest);
	}
	
	/* Establece los intereses para el canal de salida */
	void outputInterest (final int interest) {
		setInterest (outputKey, interest);
	}

	/* Lee bytes del canal de entrada, almacenándolos en el buffer facilitado, el buffer de lectura. */
	byte[] read(final ByteBuffer buffer) throws IOException {
		buffer.clear(); 
		
		/* Una única llamada a read puede no leer toda la información del canal, seguimos leyendo mientras sigamos recuperando datos */
		int bytesReaded = this.inputChannel.read(buffer);
		int totalReaded = bytesReaded;
		while (bytesReaded > 0) {
			bytesReaded = this.inputChannel.read(buffer);
			totalReaded += bytesReaded;
		}
		
		if (totalReaded == DISCONNECTION_MARK) {
			return null;
		}
		
		return BufferUtils.readFromBuffer(buffer, Boolean.TRUE);
	}

	/* Escribe bytes en el canal de salida, tomándolos del buffer facilitado, el buffer de escritura. 
	 * El buffer está listo para leer (flip) */ 
	void write (final ByteBuffer buffer) throws IOException {
		int bytesWritten = 1; /* valor falso se sobrescribirá en la primera vuelta de bucle */
		while (buffer.hasRemaining() && bytesWritten > 0) {
			bytesWritten = this.outputChannel.write(buffer);
		}
	}
	
	/**
	 * Comprueba el estado de conexión de los canales. Devuelve
	 * <code>true</code> si ambos canales están conectados, <code>false</code>
	 * en caso contrario.
	 * 
	 * @return <code>true</code> si ambos canales están conectados,
	 *         <code>false</code> en caso contrario.
	 */
	boolean checkConnection() {
		return !isClosed()
			&& inputChannel != null && inputChannel.isConnected()
			&& outputChannel != null && outputChannel.isConnected()
			&& selector != null && selector.isOpen();
	}
	
	/* Indica si el objeto ha sido cerrado. */
	boolean isClosed() {
		return !isOpen.get();
	}

	/*
	 * Cierra todos los elementos que contiene el objeto (operación
	 * irreversible). En caso de error se captura, no relanza.
	 */
	public void close() {
		if (!isOpen.getAndSet(Boolean.FALSE)) {
			return;
		}

		if (selector != null) {
			close(selector.wakeup());
		}
		close(inputChannel);
		close(outputChannel);
	}
	
	private void close(final Closeable c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (IOException e) {
			Log.error(this, "Error cerrando socket (canales, selector). Se da por cerrado.", e);
		}
	}

	private static CommunicationException createError(final String message) {
		return createError(message, null);
	}

	private static CommunicationException createError(final String message, final Throwable t) {
		final CommunicationException error = new CommunicationException(CommErrorType.SOCKET_ERROR, message, t);
		return error;
	}

	/* Indica si un canal es el canal de entrada. */
	boolean isInputChannel(final SocketChannel channel) {
		return channel != null && channel.equals(inputChannel);
	}

	/* Indica si un canal es el canal de salida. */
	boolean isOutputChannel(final SocketChannel channel) {
		return channel != null && channel.equals(outputChannel);
	}
}
