package libcomm.layer.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import commons.log.Log;
import commons.util.ColUtils;
import commons.util.PrintUtils;

/**
 * Tarea a realizar cada x segundos, comprobar aquellos mensajes que no hayan
 * recibido su AK y reenviar o dar error en caso de superar el máximo de
 * reenvíos.
 * 
 * Contiene un mapa con los items pendientes de AK.
 *  
 * <p>
 * 21/02/2016 18:30:13
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class AKTimerTask<K, M> extends TimerTask {

	/* Número máximo de intentos */
	private final int maxAttempts;
	
	/* Mapa con los ítems pendientes de AK. */
	private final Map<K, AKWaitingItem<K, M>> pendingAKMessages;
	
	/* Bloqueo de lectura al mapa de pendientes. */
	private Lock readLock;

	/* Bloqueo de escritura en el mapa de pendientes. */
	private Lock writeLock;
	
	/* Observador de enventos del temporizador. */
	private final AKObserver<K, M> observer;

	/* Tiempo máximo de espera sin AK, en milisegundos. */
	private final long maxWaitingMillis;



	/* Constructor de clase. Recibe un observador para los eventos del temporizador. */
	AKTimerTask(final AKObserver<K, M> observer, final int maxAttempts, final long maxWaitingMillis) {
		this.pendingAKMessages = Collections.synchronizedMap(new HashMap<>());
		final ReadWriteLock accessLock = new ReentrantReadWriteLock();
		this.writeLock = accessLock.writeLock();
		this.readLock = accessLock.readLock();
		this.observer = observer;
		this.maxAttempts = maxAttempts;
		this.maxWaitingMillis = maxWaitingMillis;
	}

	/* Añade un mensaje al mapa de pendientes de AK. Esto ocurre cada vez que se envía un mensaje que no sea un propio AK. */
	void messageSent (final K key, final M message) {
		ColUtils.requireNonNull(key, message);
		int attempt = 0;
		try {
			writeLock.lock();;
			AKWaitingItem<K, M> item = pendingAKMessages.get(key);
			
			if (item == null) { /* Si ya hay item el message lo descartamos, ya tenemos uno igual almacenado. */
				item = new AKWaitingItem<K, M>(key, message);
				pendingAKMessages.put(key, item);
			}
			
			attempt = item.increment();
		} finally {
			writeLock.unlock();
		}
		if (attempt > 1) { /* Si no es el primer intento logeamos. */
			Log.debug(this, PrintUtils.format("Mensaje '%s' con clave '%s', intento de envío número '%s'", message, key, attempt));
		}
	}
	
	/*
	 * Retira un mensaje del mapa de pendientes. Esto ocurre cada vez que se
	 * recibe un AK. Si se recibe un AK de un mensaje que no estaba en
	 * pendientes simplemente se logea y se ignora.
	 */
	void receiveAK (final K key) {
		Objects.requireNonNull(key);

		AKWaitingItem<K, M> item = null; 
		item = retireItem (key);
		
		final M message = item != null ? item.getMessage() : null;
		if (message != null) {
			Log.debug(this, PrintUtils.format("Recibido AK de confirmación para '%s', mensaje '%s'", key, message));
		} else {
			Log.error (this, PrintUtils.format( 
				"Recibido AK de confirmación para mensaje con clave '%s' no encontrado en la lista de pendientes, se ignora.", 
				key)
			);
		}
	}

	/* Elimina un item de la cola de espera por AK. Se hace tanto al recibir el AK como al darlo por perdido y notificar fallo por AK no recibido */
	private AKWaitingItem<K, M> retireItem (final K key) {
		AKWaitingItem<K, M> item;
		try {
			writeLock.lock();
			item = pendingAKMessages.remove(key);
		} finally {
			writeLock.unlock();
		}
		return item;
	}
	
	/**
	 * Tarea a ejecutar, comprueba el mapa de pendientes de AK, reenviando los mensajes pertinentes e incrementando el 
	 * número de reintentos. Si antes de reenviar un mensaje se detecta que se ha superado el número de intentos máximos 
	 * permitidos, se notifica de un error de AK para ese mensaje, dejando de comprobar el resto.
	 */
	@Override
	public void run() {
		List<AKWaitingItem<K, M>> itemsToResend = null;
		AKWaitingItem<K, M> failureItem = null;
		
		try {
			AKWaitingItem<K, M> item = null;
			Entry<K, AKWaitingItem<K, M>> entry = null;
			
			readLock.lock();
			final Iterator<Entry<K, AKWaitingItem<K, M>>> pendingIterator = pendingAKMessages.entrySet().iterator();
			final long nowTime = System.currentTimeMillis();
			
			while (pendingIterator.hasNext() && failureItem == null) {
				entry = pendingIterator.next();
				item = entry.getValue();

				if (item != null && !item.isInTime(nowTime, maxWaitingMillis)) {
					if (itemsToResend == null) {
						itemsToResend = new ArrayList<AKWaitingItem<K, M>> ();
					}

					if (item.getAttempts() > maxAttempts) {
						failureItem = item;
					} else if (item.getAttempts() != maxAttempts) {
						itemsToResend.add(item); 
					} else {
						item.increment(); /* si ya fue el ultimo intento no se reenvía más veces pero se incrementa */
					}
				}
			}
		} catch (Exception e) {
			Log.error(this, "Error inesperado comprobando mensajes pendientes de AK", e);
		} finally {
			readLock.unlock();
		}
		
		logResult (ColUtils.size(itemsToResend), failureItem);
		if (failureItem != null) {
			retireItem(failureItem.getKey());
			failureAk(failureItem);
		} else {
			resendMessages(itemsToResend);
		}
		
	}

	/* Notifica de error por AK */
	private void failureAk(final AKWaitingItem<K, M> failureItem) {
		observer.akFailure(failureItem.getKey(), failureItem.getMessage());
	}

	/** Reenvía los mensajes y reinicia la marca de tiempo para el siguiente reenvío. */
	private void resendMessages(List<AKWaitingItem<K, M>> items) {
		if (items != null) {
			for (final AKWaitingItem<K, M> item : items) {
				observer.resendMessage(item.getMessage());
				item.resetMarkTime();
			}
		}
	}

	/* Imprime el resultado del log si hay algo que reenviar o se produce algun fallo por AK */
	private void logResult (int withoutAk, AKWaitingItem<K, M> failureItem) {
		if (withoutAk == 0 && failureItem == null) {
			return;
		}
		
		final String detailLog = failureItem != null ?
			PrintUtils.format("Mensaje '%s' con clave '%s' ha alcanzado los '%s' intentos de reenvío. Se notifica fallo por AK no recibido",
				failureItem.getMessage(), failureItem.getKey(), maxAttempts)
			: PrintUtils.format("'%s' mensajes pendientes de AK a reenviar", withoutAk);
		
		final String resultLog = PrintUtils.format("Comprobar mensajes pendientes de AK finalizado. %s", detailLog); 
		
		/* Según el resultado, lo imprimimos como error o como fatal. */
		if (failureItem == null) { 
			Log.error(this, resultLog);
		} else {
			Log.fatal(this, resultLog);
		}
	}
}
