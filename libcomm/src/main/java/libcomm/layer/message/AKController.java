package libcomm.layer.message;

import java.util.Timer;

import libcomm.connection.ConnectionMode;

import commons.log.Log;
import commons.util.PrintUtils;

/**
 * Controlador de eventos de confirmación de recepción de mensajes, o AK,
 * mediante un temporizador que controla que los mensajes reciban su AK antes de
 * 5 segundos o sean reenviados un número determinado de intentos. Si algún
 * mensaje no recibe su AK la librería se desconecta con un error.
 * <p>
 * 21/02/2016 18:11:08
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class AKController<K, M> extends Timer {

	/* Nombre del temporizador. */
	private static final String NAME = "AK-Timer [%s]";
	
	/* Milisegundos máximos de espera para recibir un AK antes de reenviarlo. */
	public static final long MAX_WAITING_MILLIS = 5000L;

	/* Número máximo de intentos. */
	public static final int MAX_ATTEMPTS = 3;

	/* Milisegundos cada cuanto se ejecuta la comprobación de mensajes pendientes de AK */
	public static final long INTERVAL_MILLIS = 200L;
	
	/* Observador de eventos de tarea de control de AK */
	private final AKObserver<K, M> observer;
	
	/* Tarea a ejecutar. */
	private AKTimerTask<K, M> task;

	/* Modo de conexión, cliente o servidor. */
	private final ConnectionMode mode;
	
	/*
	 * Constructor de clase. Inicia un temporizador en modo 'daemon' y con un
	 * observador para los eventos del temporizador.
	 */
	AKController(final AKObserver<K, M> observer, final ConnectionMode mode) {
		super(PrintUtils.format(NAME, mode), Boolean.TRUE);
		this.observer = observer;
		this.mode = mode;
	}
	
	/* Arranca el temporizador. */
	void start() {
		this.task = new AKTimerTask<>(observer, MAX_ATTEMPTS, MAX_WAITING_MILLIS);
		schedule(task, 0, INTERVAL_MILLIS);
		Log.debug(this, PrintUtils.format("%s: %s",
			PrintUtils.format("Iniciada tarea para comprobar AK en [%s] cada [%s] milisegundos", mode, INTERVAL_MILLIS),
			PrintUtils.format("tiempo máximo sin AK [%s] milisegundos en [%s] intentos.", MAX_WAITING_MILLIS, MAX_ATTEMPTS)
		));
	}
	
	/* Detiene el temporizador. */
	void stop() {
		if (this.task != null) {
			this.task.cancel();
		}
	}

	/* Notificación de mensaje enviado. Pasa a esperar recibir AK. */
	void messageSent(final K key, final M message) {
		this.task.messageSent(key, message);
	}
	
	/* Notificación de AK recibido. Confirma mensaje en espera y lo retira de la cola de pendientes. */
	void receiveAK (final K key) {
		this.task.receiveAK(key);
	}
}
