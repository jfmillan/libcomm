package libcomm.layer.message;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Representa un ítem esperando a ser confirmado. También indica el número de
 * veces que el ítem ha tenido que ser reenviado.
 * <p>
 * 21/02/2016 17:07:33
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class AKWaitingItem<K, M> {
	private final K key;
	private final M message;
	private final AtomicInteger attempts;
	private volatile long markTime;
	
	/* Constructor, recibe una clave y un mensaje, establece el contador a cero. */
	AKWaitingItem (final K key, final M message) {
		this.key = key;
		this.message = message;
		this.attempts = new AtomicInteger(0);
		resetMarkTime();
	}
	
	/* Resetea la marca de tiempo al momento actual */
	void resetMarkTime() {
		this.markTime = System.currentTimeMillis();
	}

	/* Comprueba si el momento indicado por parámetro está dentro de los milisegundos indicados respecto a la marca de tiempo.*/
	boolean isInTime (final long time, final long maxWaitingMillis) {
		return (markTime + maxWaitingMillis) > time;
	}

	/* Comprueba si el momento actual está dentro de los milisegundos indicados respecto a la marca de tiempo.*/
	boolean isInTime (final long maxWaitingMillis) {
		return isInTime(System.currentTimeMillis(), maxWaitingMillis);
	}
	
	/* Obtiene la clave */
	K getKey() {
		return key;
	}
	
	/* Obtiene el mensaje. */
	M getMessage() {
		return message;
	}
	
	/* Obtiene el valor del contador. */
	int getAttempts() {
		return attempts.get();
	}
	
	/* Incrementa el contador, devuelve el valor incrementado. */
	int increment() {
		return attempts.incrementAndGet();
	}
}
