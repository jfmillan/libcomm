package libcomm.communication.transmission;

import java.util.Arrays;

import libcomm.connection.ConnectionMode;
import libcomm.exception.CommunicationException;
import libcomm.layer.message.AKController;
import libcomm.message.MessageGT;
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
 * Tests de transmisión de mensaje GT en ambos sentidos. 
 * <p>
 * 17/03/2016 20:20:25
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TransmissionGTTest extends AbstractTransmissionTest {

	public TransmissionGTTest() throws CommunicationException {
		super (
			TransmissionGTTest.class,
			new CommTest("GT-server", ConnectionMode.SERVER),
			new CommTest("GT-client", ConnectionMode.CLIENT)
		);
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConfigureLog.configure(LogSystem.DEFAULT);
	}

	
	/**
	 * Realiza los siguientes pasos:
	 * <ul>
	 * <li>Genera un mensaje GT esperado para cliente y para servidor, sin
	 * número de secuencia.
	 * <li>Envía los mensajes.
	 * <li>Recoge los mensajes recibidos en cada lado de la conexión y los
	 * compara con los esperados. Debe tenerse en cuenta que ahora sí tendrán
	 * numero de secuencia.
	 * </ul>
	 * 
	 * @throws Exception
	 *             si se produce algún error.
	 */
	@Override
	void concreteTransmissionTest() throws Exception {
		final MessageGT gtCl2Srv = MessageFactoryTest.getRandomGT();
		final MessageGT gtSrv2Cl = MessageFactoryTest.getRandomGT();

		Log.info(this, PrintUtils.format("Generado mensaje GT cliente a servidor: '%s'", gtCl2Srv));
		Log.info(this, PrintUtils.format("Generado mensaje GT servidor a cliente: '%s'", gtSrv2Cl));
		
		addClientToServerMessage(gtCl2Srv);
		addServerToClientMessage(gtSrv2Cl);

		Log.info(this, "##############################################################################################");
		Log.info(this, PrintUtils.format("Se envia CLIENTE -> SERVIDOR: %s", gtCl2Srv));
		getClient().send(gtCl2Srv);
		Thread.sleep(1*Constants.SECOND);

		Log.info(this, "##############################################################################################");
		Log.info(this, PrintUtils.format("Se envia SERVIDOR -> CLIENTE: %s", gtSrv2Cl));
		getServer().send(gtSrv2Cl);
		Thread.sleep(Constants.SECOND);
		
		/* Es el primer mensaje en ambos casos, por lo que el número de secuencia será 1 en ambos casos */
		gtCl2Srv.setSequenceNumber(new SequenceNumber(0x1));
		gtSrv2Cl.setSequenceNumber(new SequenceNumber(0x1));
		
		/* Esperamos el tiempo maximo de AK + 1 segundo */
		final long akWaitMillis = (AKController.MAX_WAITING_MILLIS*(1+AKController.MAX_ATTEMPTS)) + 3*Constants.SECOND;
		Log.info(this, PrintUtils.format("Esperando '%s' milisegundos antes de comprobar mensajes", akWaitMillis));
		Thread.sleep(akWaitMillis);
		
		checkMessages(Arrays.asList(gtCl2Srv), getServer().receivedMessages());
		checkMessages(Arrays.asList(gtSrv2Cl), getClient().receivedMessages());
	}
}
