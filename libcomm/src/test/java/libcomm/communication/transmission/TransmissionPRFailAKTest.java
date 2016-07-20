package libcomm.communication.transmission;

import static org.junit.Assert.assertEquals;
import libcomm.connection.ConnectionMode;
import libcomm.connection.ConnectionState;
import libcomm.exception.CommErrorType;
import libcomm.exception.CommunicationException;
import libcomm.layer.message.AKController;
import libcomm.message.Message;
import libcomm.message.MessagePR;
import libcomm.message.SequenceNumber;
import libcomm.utils.AKDisabedCommTest;
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
 * Tests de transmisión de mensaje PR con fallo por AK no recibido.
 * <p>
 * 20/03/2016 01:15:23
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TransmissionPRFailAKTest extends AbstractTransmissionTest {

	public TransmissionPRFailAKTest() throws CommunicationException {
		super (
			TransmissionPRFailAKTest.class,
			new AKDisabedCommTest("PR-server", ConnectionMode.SERVER),
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
	 * <li>Genera un mensaje PR en el cliente para enviar a servidor.
	 * <li>Envía el mensaje.
	 * <li>Se producen 3 envíos del mismo mensaje debido a que el servidor tiene el AK desconectado.
	 * <li>Se produce desconexión por fallo de AK.
	 * <li>Se comprueba que hemos recibido tres veces el mismo mensaje en el servidor.
	 * </ul>
	 * @throws Exception si se produce algún error.
	 */
	@Override
	void concreteTransmissionTest() throws Exception {
		final MessagePR prCl2Srv = MessageFactoryTest.getRandomPR();

		Log.info(this, PrintUtils.format("Generado mensaje PR cliente a servidor: '%s'", prCl2Srv));
		
		for (int i = 0; i < AKController.MAX_ATTEMPTS; i++) {
			addClientToServerMessage(prCl2Srv);
		}
		
		Log.info(this, "##############################################################################################");
		Log.info(this, PrintUtils.format("Se envia CLIENTE -> SERVIDOR: %s", prCl2Srv));
		getClient().send(prCl2Srv);
		Thread.sleep(1*Constants.SECOND);
		Assert.assertNull("No hay errores todavía", getClient().error());
		
		/* Se reenviará el mismo mensaje 3 veces, con el mismo número de secuencia. */
		for (final Message<?> msg : getClientSendsToServerMessages()) {
			msg.setSequenceNumber(new SequenceNumber(0x1));
		}

		/* Esperamos el tiempo maximo de AK + 1 segundo */
		final long akWaitMillis = (AKController.MAX_WAITING_MILLIS*(1+AKController.MAX_ATTEMPTS)) + 3*Constants.SECOND;
		Log.info(this, PrintUtils.format("Esperando '%s' milisegundos antes de comprobar mensajes", akWaitMillis));
		Thread.sleep(akWaitMillis);
		
		Log.info(this, "Comprobando resultados #######################################################################");
		checkMessages(getClientSendsToServerMessages(), getServer().receivedMessages());
		Assert.assertTrue("Mensajes vacíos en cliente", getClient().receivedMessages().isEmpty());
		Assert.assertNotNull("Error en cliente por mensaje no confirmado desde servidor", getClient().findError(CommErrorType.AK_FAILURE));
		Assert.assertNotNull("Error por desconexión remota en servidor", getServer().findError(CommErrorType.REMOTE_DISCONNECTION));
		assertEquals("Desconectados en cliente", ConnectionState.DISCONNECTED, getClient().getConnectionState());
		assertEquals("Conectando en servidor", ConnectionState.CONNECTING, getServer().getConnectionState());

		assertNoErrors();
	}
	

	/* Desconecta los sistemas. */
	@Override
	void disconnection() throws InterruptedException {
		Log.info(this, "Servidor desconectado, el cliente ya está desconectado #######################################");
		assertEquals("Estamos desconectados en cliente", ConnectionState.DISCONNECTED, getClient().getConnectionState());
		assertEquals("Estamos conectando en servidor", ConnectionState.CONNECTING, getServer().getConnectionState());
		
		getServer().disconnect();
		Thread.sleep(2*Constants.SECOND);
		Assert.assertTrue("Hay evento de desconexión en servidor", getServer().disconnectionEvent());
		Assert.assertNull("No hay error en servidor", getServer().error());
		assertEquals("Estamos desconectados en servidor", ConnectionState.DISCONNECTED, getServer().getConnectionState());
		assertEquals("Estamos conectando en cliente", ConnectionState.DISCONNECTED, getServer().getConnectionState());

		Thread.sleep(3*Constants.SECOND);
		
		assertNoErrors();
	}
}
