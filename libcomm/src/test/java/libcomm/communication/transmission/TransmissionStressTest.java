package libcomm.communication.transmission;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import libcomm.connection.ConnectionMode;
import libcomm.connection.ConnectionState;
import libcomm.exception.CommunicationException;
import libcomm.layer.message.AKController;
import libcomm.message.Message;
import libcomm.message.SequenceNumber;
import libcomm.utils.CommThreadTest;
import libcomm.utils.MessageFactoryTest;

import org.junit.Assert;
import org.junit.BeforeClass;

import commons.log.ConfigureLog;
import commons.log.Log;
import commons.log.LogSystem;
import commons.util.Constants;
import commons.util.PrintUtils;

/**
 * Tests de transmisión de mensajes de todos los tipos y en grandes cantidades,
 * con esperas aleatorias y simultáneas en un hilo distinto para clliente y
 * servidor.
 * <p>
 * 17/03/2016 20:20:25
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TransmissionStressTest extends AbstractTransmissionTest {

	/* Número total de mensajes a enviar por cada comunicador por separado, cliente y servidor. */
	private static final int MESSAGES = 10000;
	
	/* Tiempo máximo de espera entre envío de mensajes, calculado aleatoriamente entre 0 (sin espera) y el número indicado (máxima espera). */
	private static final int MAX_WAIT_MILLIS = 10;
	
	/* Semilla aleatoria para crear los mensajes. */
	private static final Random RANDOM = new Random(System.currentTimeMillis());
	
	/* Señal de comienzo */
	private CountDownLatch startSignal;
	
	/* Señal de final */
	private CountDownLatch doneSignal;
	
	/* Hilo cliente. */
	private CommThreadTest clientThread;
	
	/* Hilo servidor. */
	private CommThreadTest serverThread;
	
	/* Constructor, crea cliente y servidor como hilos independientes.*/
	public TransmissionStressTest() throws CommunicationException {
		super (
			TransmissionStressTest.class,
			new CommThreadTest("server", ConnectionMode.SERVER),
			new CommThreadTest("client", ConnectionMode.CLIENT)
		);
	}

	/* Inicia los hilos cliente y servidor. */
	private void initThreads() {
		startSignal = new CountDownLatch(1);
		doneSignal = new CountDownLatch(2);
		
		this.clientThread = (CommThreadTest) getClient();
		this.clientThread.setStartSignal(startSignal);
		this.clientThread.setDoneSignal(doneSignal);
		this.clientThread.setMaxWaitMillis(MAX_WAIT_MILLIS);
		
		this.serverThread = (CommThreadTest) getServer();
		this.serverThread.setStartSignal(startSignal);
		this.serverThread.setDoneSignal(doneSignal);
		this.serverThread.setMaxWaitMillis(MAX_WAIT_MILLIS);
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConfigureLog.configure(LogSystem.DEFAULT);
	}

	
	/* Comprueba el estado de cliente y servidor. */
	private void checkState() {
		assertEquals("Estamos conectados en cliente", ConnectionState.CONNECTED, clientThread.getConnectionState());
		assertEquals("Estamos conectados en servidor", ConnectionState.CONNECTED, serverThread.getConnectionState());
		Assert.assertFalse("No hay evento de desconexión en cliente", clientThread.disconnectionEvent());		
		Assert.assertFalse("No hay evento de desconexión en servidor", serverThread.disconnectionEvent());
		Assert.assertNull("No hay error en servidor", serverThread.error());
		Assert.assertNull("No hay error en cliente", clientThread.error());
	}


	/* Crea un listado aleatorio de mensajes. */
	private List<Message<?>> createRandomMessages() {
		final List<Message<?>> messages = new ArrayList<>(MESSAGES);
		
		for (int i = 0; i < MESSAGES; i++) {
			messages.add(createRandomMessage());
		}
		
		return messages;
	}

	/* Crea un único mensaje aleatorio, de un tipo también aleatorio. */
	private Message<?> createRandomMessage() {
		final Message<?> message;
		final int index = RANDOM.nextInt(3);
		
		switch(index) {
		case 0:
			message = MessageFactoryTest.getRandomPR();
			break; /* asignamos el 0 al PR */
		case 1:
			message = MessageFactoryTest.getRandomGT();
			break; /* asignamos el 1 al GT */
		case 2: 
			message = MessageFactoryTest.getRandomST();
			break;/* asignamos el 2 al ST */
		default: /* No debería ocurrir. */ 
			message = null;
		}
		
		Objects.requireNonNull(message);
		return message;
	}

	/* Establece números de secuencia a los mensajes a partir del número de secuencia indicado, incluido. */
	private void setSequenceNumbersFrom(final List<Message<?>> messages, final SequenceNumber sequenceNumber) {
		Objects.requireNonNull(messages);
		
		SequenceNumber currentSN = sequenceNumber != null ? new SequenceNumber(sequenceNumber) : new SequenceNumber(0x01);
		for (final Message<?> message : messages) {
			message.setSequenceNumber(currentSN);
			currentSN = currentSN.incrementAndGet();
		}
	}
	
	
	/**
	 * Realiza los siguientes pasos:
	 * <ul>
	 * <li>Inicia los hilos para cliente y servidor.
	 * <li>Genera los mensajes para cliente y servidor. Sin enviarlos. 
	 * <li>Inicia el envío de mensajes en ambos hilos simultáneamente.
	 * <li>Espera a que ambos hilos terminen de enviar sus mensajes.
	 * <li>Establece los números de secuencia esperados de los mensajes enviados.
	 * <li>Comprueba los mensajes.
	 * <li>Comprueba que no hay errores ni desconexiones, y que cliente y servidor siguen conectados.
	 * </ul>
	 * @throws Exception si se produce algún error.
	 */
	@Override
	void concreteTransmissionTest() throws Exception {
		/* Inicia los hilos para cliente y servidor. */
		initThreads();
		
		
		/* Genera los mensajes para cliente y servidor. Sin enviarlos. */
		Log.info(this, PrintUtils.format("Creando '%s' mensajes de cliente a servidor #####################", MESSAGES));
		final List<Message<?>> clientToServerMessages = createRandomMessages();
		clientThread.setMessagesToSend(clientToServerMessages);
		
		Log.info(this, PrintUtils.format("Creando '%s' mensajes de servidor a cliente #####################", MESSAGES));
		final List<Message<?>> serverToClientMessages = createRandomMessages();
		serverThread.setMessagesToSend(serverToClientMessages);
		
		/* Inicia el envío de mensajes en ambos hilos simultáneamente. */
		Log.info(this, "Iniciando hilos cliente y servidor ###########################################################");
		this.clientThread.start();
		this.serverThread.start();
		this.startSignal.countDown();
		
		
		/* Espera a que ambos hilos terminen de enviar sus mensajes. */
		Log.info(this, "Esperando a que ambos hilos terminen de enviar sus mensajes ##################################");
		doneSignal.await();
		
		
		/* Establece los números de secuencia esperados de los mensajes enviados. */
		Log.info(this, "Estableciendo números de secuencia de cliente a servidor #####################################");
		setSequenceNumbersFrom(clientToServerMessages, new SequenceNumber(0x01));
		
		Log.info(this, "Estableciendo números de secuencia de servidor a cliente #####################################");
		setSequenceNumbersFrom(serverToClientMessages, new SequenceNumber(0x01));
		
		
		/* Comprueba los mensajes. Esperamos el tiempo maximo de AK + 1 segundo. */
		final long akWaitMillis = (AKController.MAX_WAITING_MILLIS*(1+AKController.MAX_ATTEMPTS)) + 3*Constants.SECOND;
		Log.info(this, PrintUtils.format("Esperando '%s' milisegundos antes de comprobar mensajes", akWaitMillis));
		Thread.sleep(akWaitMillis);

		/* Comprueba los mensajes descartando duplicados por posibles reenvíos */
		checkMessages(clientToServerMessages, removeDuplicate(serverThread.receivedMessages()));
		checkMessages(serverToClientMessages, removeDuplicate(clientThread.receivedMessages()));

		
		/* Comprueba que no hay errores ni desconexiones, y que cliente y servidor siguen conectados. */
		checkState();
		
		Thread.sleep(3*Constants.SECOND);
	}

	/** Elimina mensajes duplicados. No comprueba sólo el número de secuencia, ya que el contador podría haber dado la vuelta. */
	private List<Message<?>> removeDuplicate(final List<Message<?>> messages) {
		if (messages == null) {
			return null;
		}
		
		final Set<String> processedMsgStr = new HashSet<>();
		final List<Message<?>> result = new ArrayList<>(messages.size());
		for (final Message<?> message : messages) {
			if (processedMsgStr.add(message.printMessage())) {
				result.add(message);
			}
		}
		
		Log.info (this, PrintUtils.format("Eliminados duplicados recibidos, originalmente: '%s', finalmente '%s'", 
			messages.size(), result.size())
		);
		return result;
	}
}

