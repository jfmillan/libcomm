package libcomm.utils;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import libcomm.connection.ConnectionMode;
import libcomm.context.ConnectionContext;
import libcomm.exception.CommErrorType;
import libcomm.exception.CommunicationException;
import libcomm.message.Message;

import commons.log.Log;
import commons.util.ColUtils;
import commons.util.PrintUtils;

/**
 * Inicia un servidor o cliente con Libcomm en un nuevo hilo desde el que enviar los mensajes.
 * 
 * <p>
 * 21/03/2016 20:51:12
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class CommThreadTest extends AbstractCommTest implements Runnable {
	
	/* Formato para nombre del hilo */
	private static final String THREAD_NAME = "Thread-Sender[%s]";
	
	/* Semilla aleatoria. */
	private static final Random RANDOM = new Random(System.currentTimeMillis());

	
	/* Señal de comienzo */
	private CountDownLatch startSignal;
	
	/* Señal de final */
	private CountDownLatch doneSignal;

	/* Nombre del hilo. */
	private final String name;

	/* Mensajes a enviar */
	private Collection<Message<?>> messagesToSend;

	/*
	 * Tiempo máximo de espera entre envío de mensajes, calculado aleatoriamente
	 * entre 0 (sin espera) y el número indicado (máxima espera).
	 */
	private Long maxWaitMillis;

	
	
	/**
	 * Constructor, recibe el modo: cliente o servidor, así como objetos de
	 * sincronización para comenzar y esperar a finalizar la tarea del hilo.
	 * 
	 * @param name
	 *            Nombre del hilo.
	 * @param mode
	 *            Modo de la conexión.
	 * @throws CommunicationException
	 *             si se produce algún error.
	 */
	public CommThreadTest(final String name, final ConnectionMode mode) throws CommunicationException {
		super (ConnectionContext.createContext(new ConfigurationTest(name, mode).getProperties()));
		this.name = PrintUtils.format(THREAD_NAME, name);
		this.maxWaitMillis = 0L;
	}
	
	/**
	 * Establece el startSignal.
	 * 
	 * @param startSignal
	 *            Señal de comienzo, el hilo esperará antes de comenzar hasta
	 *            que se habilite.
	 * 
	 */
	public void setStartSignal (final CountDownLatch startSignal) {
		this.startSignal = startSignal;
	}
	
	/**
	 * Establece el stopSignal.
	 * 
	 * @param doneSignal
	 *            Señal de espera al final, el hilo esperará antes de finalizar
	 *            hasta que se habilite.
	 */
	public void setDoneSignal (final CountDownLatch doneSignal) {
		this.doneSignal = doneSignal;
	}

	/* Inicia el hilo. */
	public void start() {
		final Thread thread = new Thread(this, name);
		thread.setDaemon(Boolean.TRUE);
		thread.start();
	}

	/* Establece los mensajes a enviar. */
	public void setMessagesToSend(final Collection<Message<?>> messages) {
		this.messagesToSend = messages;
	}
	
	/* Establece la espera máxima entre envío de mensajes. */
	public void setMaxWaitMillis (final long millis) {
		this.maxWaitMillis = millis;
	}
	
	/* Calcula un máximo de milisegundos de espera. */
	private long getRandomWait() {
		return maxWaitMillis > 0 ? RANDOM.nextInt(maxWaitMillis.intValue()+1) : 0;
	}
	
	/* Inicia el envío de mensajes. Se quedará esperando hasta que se arranque el startSignal. */
	@Override
	public void run() {
		ColUtils.requireNonNull(messagesToSend, startSignal, doneSignal);
		
		try {
			Log.info(this, PrintUtils.format("Esperando en '%s' para iniciar envio", name));
			startSignal.await();
			sendAllMessages();
		} catch (final Exception e) {
			final String error = PrintUtils.format("Error enviando mensajes en '%s'. Se interrumpe el envío.", name);
			Log.error(this, error, e);
			this.error(new CommunicationException(CommErrorType.COMMUNICATION_ERROR, error));
		} finally {
			doneSignal.countDown();
		}
	}

	private void sendAllMessages() throws InterruptedException {
		long wait;
		final int periodLog = messagesToSend.size() / 10;
		int i = 0, m = 1;
		for (final Message<?> message : messagesToSend) {
			if (i > periodLog) {
				Log.info(this, PrintUtils.format(
					"Enviando mensaje número '%s': '%s' ###################################", m, message)
				);
				i = 0;
			}
			wait = getRandomWait();
			if (wait > 0) {
				Thread.sleep(wait);
			}
			send(message);
			i++; m++;
		}
	}
}
