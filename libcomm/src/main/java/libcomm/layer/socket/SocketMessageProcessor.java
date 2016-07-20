package libcomm.layer.socket;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import libcomm.message.rfc1006.IBytes;

import commons.log.Log;
import commons.util.PrintUtils;

/**
 * Inicia un hilo para procesar los mensajes a nivel de socket y evitar esperas.
 * 
 * <p>
 * 12/03/2016 23:06:22
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class SocketMessageProcessor implements Runnable {
	private final IMessageProcessor<IBytes> messageProcessor;
	private final BlockingQueue<IBytes> messages;
	private Thread thread;
	private final AtomicBoolean running;
	private final String threadName;

	SocketMessageProcessor(final String threadName, final IMessageProcessor<IBytes> messageProcessor) {
		this.messageProcessor = messageProcessor;
		this.threadName = threadName;
		this.messages = new LinkedBlockingQueue<IBytes>();
		this.running = new AtomicBoolean(Boolean.FALSE);
	}
	
	/** Arranca el hilo de proceso de mensajes a nivel socket. */
	synchronized void start () {
		if (running.getAndSet(Boolean.TRUE)) {
			Log.debug(this, PrintUtils.format(
				"Hilo de envío a nivel socket '%s' ya iniciado anteriormente. Se ignora nuevo intento de iniciarlo.", threadName
			));
			return;
		}
		Log.debug(this, PrintUtils.format("Iniciando hilo de proceso de mensajes-socket '%s'", threadName));
		thread = new Thread(this, threadName);
		thread.setDaemon(Boolean.TRUE);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}

	boolean isRunning() {
		return thread != null && thread.isAlive() && running.get();
	}
	
	/* Añade un mensaje para ser procesado en cuanto sea posible. */
	synchronized void addMessage(final IBytes message) {
		if (isRunning()) {
			messages.offer(message);
		} else {
			Log.error(this, PrintUtils.format(
				"Procesador de mensajes en socket no iniciado, no se puede procesar '%s'", message)
			);
		}
	}
	
	@Override
	public void run() {
		IBytes message = null;
		/* Esperamos por mensajes a enviar */
		Log.debug(this, PrintUtils.format("Esperando mensajes a procesar '%s'", threadName));
		
		while (!thread.isInterrupted()) {
			try {
				message = messages.take();
				if (message == null) {
					continue; /* no debería ocurrir */
				}
				messageProcessor.process(message);
			} catch (InterruptedException e) {
				thread.interrupt();
			} catch (Throwable t) {
				Log.error(this, PrintUtils.format("Error al procesar mensaje '%s'", 
					(message != null && message.getBytes() != null ? PrintUtils.print(message.getBytes()) : message)), t);
			}
		}
		Log.debug(this, "Hilo de procesamiento de mensajes interrumpido. Dejan de procesarse los mensajes.");

		running.set(Boolean.FALSE);
	}

	/* Detiene el hilo de procesamiento de mensajes. */
	synchronized void stop() {
		if (!isRunning()) {
			Log.debug(this, 
				PrintUtils.format("Hilo de procesamiento de mensajes '%s' no iniciado, no se puede detener", threadName)
			);
			return;
		}
		thread.interrupt();
	}
}