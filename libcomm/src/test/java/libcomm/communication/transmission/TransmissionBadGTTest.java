package libcomm.communication.transmission;

import libcomm.connection.ConnectionMode;
import libcomm.exception.CommErrorType;
import libcomm.exception.CommunicationException;
import libcomm.layer.message.AKController;
import libcomm.message.MessageGT;
import libcomm.utils.CommTest;
import libcomm.utils.MessageFactoryTest;

import org.junit.Assert;
import org.junit.BeforeClass;

import commons.log.ConfigureLog;
import commons.log.Log;
import commons.log.LogSystem;
import commons.util.Constants;
import commons.util.PrintUtils;

/**
 * Tests de transmisión de mensaje GT incorrecto en ambos sentidos. El mensaje no llegará a ser enviado. 
 * <p>
 * 19/03/2016 20:20:25
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TransmissionBadGTTest extends AbstractTransmissionTest {

	public TransmissionBadGTTest() throws CommunicationException {
		super (
			TransmissionBadGTTest.class,
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
	 * <li>Genera un mensaje GT incorrecto para ser enviado por el servidor y por el cliente.
	 * <li>Envía los mensajes.
	 * <li>Los mesajes no llegan al otro lado, se produce un error de envío.
	 * <li>Se comprueba que no ha llegado ningún mensaje al otro lado.
	 * <li>Se comprueban errores.
	 * </ul>
	 * @throws Exception si se produce algún error. 
	 */
	@Override
	void concreteTransmissionTest() throws Exception {
		final MessageGT gtCl2Srv = MessageFactoryTest.getBadRandomGT();
		final MessageGT gtSrv2Cl = MessageFactoryTest.getBadRandomGT();

		Log.info(this, PrintUtils.format("Generado mensaje GT incorrecto cliente a servidor: '%s'", gtCl2Srv));
		Log.info(this, PrintUtils.format("Generado mensaje GT incorrecto servidor a cliente: '%s'", gtSrv2Cl));
		
		addClientToServerMessage(gtCl2Srv);
		addServerToClientMessage(gtSrv2Cl);

		Log.info(this, "##############################################################################################");
		Log.info(this, PrintUtils.format("Se envia CLIENTE -> SERVIDOR: %s", gtCl2Srv));
		getClient().send(gtCl2Srv);
		Thread.sleep(1*Constants.SECOND);
		Assert.assertNotNull("Error en cliente por mensaje mal formado", getClient().findError(CommErrorType.PARSE_MESSAGE));
		
		
		Log.info(this, "##############################################################################################");
		Log.info(this, PrintUtils.format("Se envia SERVIDOR -> CLIENTE: %s", gtSrv2Cl));
		getServer().send(gtSrv2Cl);
		Thread.sleep(Constants.SECOND);
		Assert.assertNotNull("Error en servidor por mensaje mal formado", getServer().findError(CommErrorType.PARSE_MESSAGE));
		
		/* Esperamos el tiempo maximo de AK + 1 segundo */
		final long akWaitMillis = (AKController.MAX_WAITING_MILLIS*(1+AKController.MAX_ATTEMPTS)) + 3*Constants.SECOND;
		Log.info(this, PrintUtils.format("Esperando '%s' milisegundos antes de comprobar mensajes", akWaitMillis));
		Thread.sleep(akWaitMillis);

		/* No hay mensajes cliente ni en servidor */
		Assert.assertTrue("Lista de mensajes en cliente vacía", getClient().receivedMessages().isEmpty());
		Assert.assertTrue("Lista de mensajes en servidor vacía", getServer().receivedMessages().isEmpty());
		
		assertNoErrors();
	}
}
