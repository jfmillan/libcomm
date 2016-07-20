package libcomm.communication.transmission;

import java.util.Arrays;

import libcomm.connection.ConnectionMode;
import libcomm.exception.CommunicationException;
import libcomm.layer.message.AKController;
import libcomm.message.MessagePR;
import libcomm.message.SequenceNumber;
import libcomm.utils.CommTest;
import libcomm.utils.MessageFactoryTest;

import org.junit.BeforeClass;

import commons.log.ConfigureLog;
import commons.log.Log;
import commons.log.LogSystem;
import commons.util.Constants;
import commons.util.PrintUtils;

/**
 * Tests de transmisión de mensaje PR en ambos sentidos. 
 * <p>
 * 17/03/2016 20:20:25
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TransmissionPRTest extends AbstractTransmissionTest {

	public TransmissionPRTest() throws CommunicationException {
		super (
			TransmissionPRTest.class,
			new CommTest("PR-server", ConnectionMode.SERVER),
			new CommTest("PR-client", ConnectionMode.CLIENT)
		);
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConfigureLog.configure(LogSystem.DEFAULT);
	}

	
	/**
	 * Realiza los siguientes pasos:
	 * <ul>
	 * <li>Genera un mensaje PR esperado para cliente y para servidor, sin
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
		final MessagePR prCl2Srv = MessageFactoryTest.getRandomPR();
		final MessagePR prSrv2Cl = MessageFactoryTest.getRandomPR();

		Log.info(this, PrintUtils.format("Generado mensaje PR cliente a servidor: '%s'", prCl2Srv));
		Log.info(this, PrintUtils.format("Generado mensaje PR servidor a cliente: '%s'", prSrv2Cl));
		
		addClientToServerMessage(prCl2Srv);
		addServerToClientMessage(prSrv2Cl);

		Log.info(this, "##############################################################################################");
		Log.info(this, PrintUtils.format("Se envia CLIENTE -> SERVIDOR: %s", prCl2Srv));
		getClient().send(prCl2Srv);
		Thread.sleep(1*Constants.SECOND);

		Log.info(this, "##############################################################################################");
		Log.info(this, PrintUtils.format("Se envia SERVIDOR -> CLIENTE: %s", prSrv2Cl));
		getServer().send(prSrv2Cl);
		Thread.sleep(Constants.SECOND);
		
		/* Es el primer mensaje en ambos casos, por lo que el número de secuencia será 1 en ambos casos */
		prCl2Srv.setSequenceNumber(new SequenceNumber(0x1));
		prSrv2Cl.setSequenceNumber(new SequenceNumber(0x1));
		
		/* Esperamos el tiempo maximo de AK + 1 segundo */
		final long akWaitMillis = (AKController.MAX_WAITING_MILLIS*(1+AKController.MAX_ATTEMPTS)) + 3*Constants.SECOND;
		Log.info(this, PrintUtils.format("Esperando '%s' milisegundos antes de comprobar mensajes", akWaitMillis));
		Thread.sleep(akWaitMillis);
		
		checkMessages(Arrays.asList(prCl2Srv), getServer().receivedMessages());
		checkMessages(Arrays.asList(prSrv2Cl), getClient().receivedMessages());
	}
}
