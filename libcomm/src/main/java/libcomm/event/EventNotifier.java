package libcomm.event;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import libcomm.LibcommListener;
import libcomm.connection.IConnection;
import libcomm.exception.CommunicationException;

import commons.log.Log;
import commons.util.PrintUtils;

/**
 * Notificador de eventos. Recoge los mensajes recibidos en una cola
 * sincronizada e informa a un listener de cada uno, según orden de llegada.
 * 
 * <p>
 * 09/02/2016 00:59:42
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class EventNotifier<M> implements Runnable {

	private final LibcommListener<M> listener;
	private final BlockingQueue<Event<?>> events;
	private Thread thread;
	private final AtomicBoolean running;
	private final String threadName;

	public EventNotifier(final String threadName, final LibcommListener<M> listener) {
		this.listener = listener;
		this.threadName = threadName;
		this.events = new LinkedBlockingQueue<Event<?>>();
		this.running = new AtomicBoolean(Boolean.FALSE);
	}
	
	/** Arranca el hilo de notificación de eventos. */
	public synchronized void start () {
		if (running.getAndSet(Boolean.TRUE)) {
			Log.info(this, 
				PrintUtils.format(
					"Hilo de notificación de eventos '%s' ya iniciado anteriormente. Se ignora nuevo intento de inciarlo.", 
					threadName
				)
			);
			return;
		}
		Log.info(this, PrintUtils.format("Iniciando hilo de notificacion de eventos '%s'", threadName));
		thread = new Thread(this, threadName);
		thread.setDaemon(Boolean.TRUE);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}

	public boolean isRunning() {
		return thread != null && thread.isAlive() && running.get();
	}
	
	/* Añade un evento de un tipo y con un argumento adjunto. */
	public synchronized void addEvent(final EventType type, final Object argument) {
		final Event<Object> event = new Event<>(type, argument);
		if (isRunning()) {
			events.offer(event);
		} else {
			Log.error(this, PrintUtils.format("Notificador de eventos no iniciado, no se puede notificar '%s'", event));
		}
	}
	
	@Override
	public void run() {
		Event<?> event = null;
		while (!thread.isInterrupted()) {
			try {
				/* Esperamos por un evento */
				Log.debug(this, PrintUtils.format("Esperando evento en %s", threadName));
				event = events.take();
				if (event == null) {
					continue; /* no debería ocurrir */
				}
				processEvent(event);
			} catch (InterruptedException e) {
				thread.interrupt();
			} catch (Throwable t) {
				Log.error(this, PrintUtils.format("Error al notificar '%s'", event), t);
			}
		}
		finalizeEvents();
		Log.debug(this, "Hilo de notificación de eventos interrumpido. Dejan de procesarse los eventos");

		running.set(Boolean.FALSE);
	}

	/* Finaliza los eventos que quedan en la cola, si es posible. */
	private synchronized void finalizeEvents() {
		final int eventsToFinalizeCount = events.size();
		if (eventsToFinalizeCount == 0) {
			return;
		}

		Log.debug(this, PrintUtils.format(
			"Se finaliza el hilo de eventos. Intentan notificarse los últimos '%s' eventos", eventsToFinalizeCount)
		);
		
		Event<?> lastEvent = null;
		int lastNotifiedCount = 0;
		do {
			lastEvent = events.poll();
			if (lastEvent != null) {
				try {
					processEvent(lastEvent);
				} catch (Throwable t) {
					Log.error(this, PrintUtils.format("Error al notificar '%s'", lastEvent), t);
				}
				lastNotifiedCount++;
			}
		} while (lastEvent != null);

		Log.debug(this, PrintUtils.format(
			"Notificados ultimos '%s' eventos de '%s' que quedaban por notificar antes de finalizar el hilo",
			lastNotifiedCount, eventsToFinalizeCount)
		);
	}

	/** Detiene el hilo de notificación de eventos. */
	public synchronized void stop() {
		if (!isRunning()) {
			Log.info(this, 
				PrintUtils.format("Hilo de notificación de eventos '%s' no iniciado, no se puede detener", threadName)
			);
			return;
		}
		thread.interrupt();
	}

	@SuppressWarnings("unchecked")
	private void processEvent(final Event<?> event) {
		final EventType type = event.getType();
		Log.debug(this, PrintUtils.format("Se procesa %s", event));
		
		if (EventType.MESSAGE_RECEIVED.equals(type)) {
			final Event<M> __event = (Event<M>) event;
			listener.received(__event.getEvent());
		} else if (EventType.CONNECTED.equals(type)) {
			final Event<IConnection> __event = (Event<IConnection>) event;
			listener.connected(__event.getEvent());
		} else if (EventType.DISCONNECTED.equals(type)) {
			final Event<IConnection> __event= (Event<IConnection>) event;
			listener.disconnected(__event.getEvent());
		} else if (EventType.ERROR.equals(type)) {
			final Event<CommunicationException> __event = (Event<CommunicationException>) event;
			listener.error(__event.getEvent());
		} else {
			Log.error(this, PrintUtils.format("No se puede procesar evento de tipo desconocido: %s", event));
		}
	}
}
