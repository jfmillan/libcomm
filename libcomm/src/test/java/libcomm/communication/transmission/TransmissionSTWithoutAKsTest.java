package libcomm.communication.transmission;

import java.util.Arrays;

import libcomm.connection.ConnectionMode;
import libcomm.exception.CommunicationException;
import libcomm.layer.message.AKController;
import libcomm.message.MessageST;
import libcomm.message.SequenceNumber;
import libcomm.utils.AKDisabedCommTest;
import libcomm.utils.MessageFactoryTest;

import org.junit.BeforeClass;

import commons.log.ConfigureLog;
import commons.log.Log;
import commons.log.LogSystem;
import commons.util.Constants;
import commons.util.PrintUtils;

/**
 * Tests de transmisión de mensaje ST en ambos sentidos, con AK desactivado en ambos lados, de modo que no se producen errores por falta de confirmación. 
 * <p>
 * 17/03/2016 20:20:25
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TransmissionSTWithoutAKsTest extends AbstractTransmissionTest {

	public TransmissionSTWithoutAKsTest() throws CommunicationException {
		super (
			TransmissionSTWithoutAKsTest.class,
			new AKDisabedCommTest("ST-server", ConnectionMode.SERVER),
			new AKDisabedCommTest("ST-client", ConnectionMode.CLIENT)
		);
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConfigureLog.configure(LogSystem.DEFAULT);
	}

	
	/**
	 * Realiza los siguientes pasos:
	 * <ul>
	 * <li>Genera un mensaje ST esperado para cliente y para servidor, sin
	 * número de secuencia.
	 * <li>Envía los mensajes.
	 * <li>Recoge los mensajes recibidos en cada lado de la conexión y los
	 * compara con los esperados. Debe tenerse en cuenta que ahora sí tendrán
	 * numero de secuencia.
	 * </ul>
	 * @throws Exception si se produce algún error.
	 */
	@Override
	void concreteTransmissionTest() throws Exception {
		final MessageST stCl2Srv = MessageFactoryTest.getRandomST();
		final MessageST stSrv2Cl = MessageFactoryTest.getRandomST();

		Log.info(this, PrintUtils.format("Generado mensaje ST cliente a servidor: '%s'", stCl2Srv));
		Log.info(this, PrintUtils.format("Generado mensaje ST servidor a cliente: '%s'", stSrv2Cl));
		
		addClientToServerMessage(stCl2Srv);
		addServerToClientMessage(stSrv2Cl);

		Log.info(this, "##############################################################################################");
		Log.info(this, PrintUtils.format("Se envia CLIENTE -> SERVIDOR: %s", stCl2Srv));
		getClient().send(stCl2Srv);
		Thread.sleep(1*Constants.SECOND);

		Log.info(this, "##############################################################################################");
		Log.info(this, PrintUtils.format("Se envia SERVIDOR -> CLIENTE: %s", stSrv2Cl));
		getServer().send(stSrv2Cl);
		Thread.sleep(Constants.SECOND);
		
		/* Es el primer mensaje en ambos casos, por lo que el número de secuencia será 1 en ambos casos */
		stCl2Srv.setSequenceNumber(new SequenceNumber(0x1));
		stSrv2Cl.setSequenceNumber(new SequenceNumber(0x1));
		
		/* Esperamos el tiempo maximo de AK + 1 segundo */
		final long akWaitMillis = (AKController.MAX_WAITING_MILLIS*(1+AKController.MAX_ATTEMPTS)) + 3*Constants.SECOND;
		Log.info(this, PrintUtils.format("Esperando '%s' milisegundos antes de comprobar mensajes", akWaitMillis));
		Thread.sleep(akWaitMillis);
		
		checkMessages(Arrays.asList(stCl2Srv), getServer().receivedMessages());
		checkMessages(Arrays.asList(stSrv2Cl), getClient().receivedMessages());
	}
}
