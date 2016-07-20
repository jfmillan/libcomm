package libcomm.communication.connection;

import static org.junit.Assert.assertEquals;
import libcomm.connection.ConnectionMode;
import libcomm.connection.ConnectionState;
import libcomm.exception.CommErrorType;
import libcomm.exception.CommunicationException;
import libcomm.utils.CommTest;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import commons.log.ConfigureLog;
import commons.log.Log;
import commons.log.LogSystem;
import commons.util.Constants;

/**
 * Tests de inicio de establecimiento de conexión, sólo de un lado, conectando y
 * desconectando antes de finalizar la conexión.
 * <p>
 * 28/02/2016 23:53:16
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class ConnectingClientTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConfigureLog.configure(LogSystem.DEFAULT);
	}

	/**
	 * Cliente: Comprueba el paso de desconectado a conectando, de nuevo a
	 * desconectado antes del timeout, de nuevo a conectando y, ahora sí, se
	 * espera al timeout. Se produce un error por timeout y se pasa a estado
	 * desconectado. Vuelve a intentar conectarse.
	 * 
	 * @throws CommunicationException
	 *             en caso de producrse algún error de comunicaciones.
	 * @throws InterruptedException
	 *             en caso de producirse la interrupción del hilo.
	 */
	@Test
	public void connectingClient() throws CommunicationException, InterruptedException {
		final CommTest client = new CommTest("connectingClient", ConnectionMode.CLIENT);
		assertEquals("Comenzamos desconectados", ConnectionState.DISCONNECTED, client.getConnectionState());
		
		Log.info(this, "1. desconectado -> conectando ################################################################");
		client.connect();
		Thread.sleep(client.context().getConnectionTimeout()-2*Constants.SECOND);
		assertEquals("Estamos conectando", ConnectionState.CONNECTING, client.getConnectionState());
		Assert.assertNull("No hay error", client.error());
		Assert.assertFalse("No hay evento de conexión", client.connectionEvent());
		Assert.assertFalse("No hay evento de desconexión", client.disconnectionEvent());
		
		Log.info(this, "2. conectando -> desconectado (no timeout) ###################################################");
		client.disconnect();
		Thread.sleep(Constants.SECOND);
		assertEquals("Cancelamos la conexión", ConnectionState.DISCONNECTED, client.getConnectionState());
		Assert.assertTrue("Hay evento de desconexión", client.disconnectionEvent());
		Assert.assertFalse("No hay evento de conexión", client.connectionEvent());
		Assert.assertNull("No hay error", client.error());
		
		Log.info(this, "3. desconectado -> conectando ################################################################");
		client.connect();
		Thread.sleep(client.context().getConnectionTimeout()-2*Constants.SECOND);
		assertEquals("Estamos conectando de nuevo", ConnectionState.CONNECTING, client.getConnectionState());
		Assert.assertNull("No hay error", client.error());
		Assert.assertFalse("No hay evento de conexión", client.connectionEvent());
		Assert.assertFalse("No hay evento de desconexión", client.disconnectionEvent());

		Log.info(this, "4. conectando -> timeout -> desconectado #####################################################");
		Thread.sleep(client.context().getConnectionTimeout()+Constants.SECOND);
		assertEquals("Timeout de conexión", ConnectionState.DISCONNECTED, client.getConnectionState());
		Assert.assertNotNull("Error por timeout", client.findError(CommErrorType.TIMEOUT));
		Assert.assertFalse("No hay evento de conexión", client.connectionEvent());
		Assert.assertTrue("Hay evento de desconexión", client.disconnectionEvent());
		
		Log.info(this, "5. desconectado -> conectando ################################################################");
		client.connect();
		Thread.sleep(client.context().getConnectionTimeout()-Constants.SECOND*2);
		assertEquals("Estamos conectando de nuevo", ConnectionState.CONNECTING, client.getConnectionState());
		Assert.assertFalse("No hay evento de conexión", client.connectionEvent());
		Assert.assertFalse("No hay evento de desconexión", client.disconnectionEvent());
		Assert.assertNull("No hay error", client.error());
		
		Log.info(this, "6. desconectado ##############################################################################");
		client.disconnect();
		Thread.sleep(client.context().getConnectionTimeout()+Constants.SECOND);
		assertEquals("Cancelamos la conexión", ConnectionState.DISCONNECTED, client.getConnectionState());
		Assert.assertTrue("Hay evento de desconexión", client.disconnectionEvent());
		Assert.assertFalse("No hay evento de conexión", client.connectionEvent());
		Assert.assertNull("No hay error", client.error());
		
		// No ha habido mensajes en cualquier caso
		Assert.assertTrue("Sin mensajes", client.receivedMessages().isEmpty());
	}
}