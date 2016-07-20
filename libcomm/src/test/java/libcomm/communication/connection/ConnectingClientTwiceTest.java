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
public class ConnectingClientTwiceTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConfigureLog.configure(LogSystem.DEFAULT);
	}

	/**
	 * Cliente: Comprueba el paso de desconectado a conectando, intenta conectar
	 * de nuevo cuando ya está conectando, provocando un error pero
	 * permanenciendo en estado conectando. A continuación se desconecta por
	 * timeout y velve a intentar conectarse.
	 * 
	 * @throws CommunicationException
	 *             en caso de producrse algún error de comunicaciones.
	 * @throws InterruptedException
	 *             en caso de producirse la interrupción del hilo.
	 */
	@Test
	public void connectingClientTwice() throws CommunicationException, InterruptedException {
		final CommTest client = new CommTest("connectingTwiceClient", ConnectionMode.CLIENT);
		assertEquals("Comenzamos desconectados", ConnectionState.DISCONNECTED, client.getConnectionState());
		
		Log.info(this, "1. desconectado -> conectando ###################################################################");
		client.connect();
		Thread.sleep(2*Constants.SECOND);
		assertEquals("Estamos conectando", ConnectionState.CONNECTING, client.getConnectionState());
		Assert.assertNull("No hay error", client.error());
		Assert.assertFalse("No hay evento de conexión", client.connectionEvent());
		Assert.assertFalse("No hay evento de desconexión", client.disconnectionEvent());
		
		Log.info(this, "2. conectando -> conectando (error) -> permanence conectando ####################################");
		client.connect();
		Thread.sleep(Constants.SECOND);
		Assert.assertNotNull("Error por intentar conectar mientras estamos conectando", client.findError(CommErrorType.CONNECTION_ERROR));
		assertEquals("Seguimos en estado conectando", ConnectionState.CONNECTING, client.getConnectionState());
		Assert.assertFalse("No hay evento de conexión", client.connectionEvent());
		Assert.assertFalse("No hay evento de desconexión", client.disconnectionEvent());
		
		Log.info(this, "3. conectando -> timeout -> desconectado ########################################################");
		Thread.sleep(client.context().getConnectionTimeout()+Constants.SECOND);
		assertEquals("Timeout de conexión", ConnectionState.DISCONNECTED, client.getConnectionState());
		Assert.assertNotNull("Error por timeout", client.findError(CommErrorType.TIMEOUT));
		Assert.assertFalse("No hay evento de conexión", client.connectionEvent());
		Assert.assertTrue("Hay evento de desconexión", client.disconnectionEvent());
		
		Log.info(this, "4. desconectado -> conectando ###################################################################");
		client.connect();
		Thread.sleep(client.context().getConnectionTimeout()-2*Constants.SECOND);
		assertEquals("Estamos conectando de nuevo", ConnectionState.CONNECTING, client.getConnectionState());
		Assert.assertFalse("No hay evento de conexión", client.connectionEvent());
		Assert.assertFalse("No hay evento de desconexión", client.disconnectionEvent());
		Assert.assertNull("No hay error", client.error());
		
		Log.info(this, "5. desconectado #################################################################################");
		client.disconnect();
		Thread.sleep(client.context().getConnectionTimeout());
		assertEquals("Cancelamos la conexión", ConnectionState.DISCONNECTED, client.getConnectionState());
		Assert.assertTrue("Hay evento de desconexión", client.disconnectionEvent());
		Assert.assertFalse("No hay evento de conexión", client.connectionEvent());
		Assert.assertNull("No hay error", client.error());
		
		// No ha habido mensajes en cualquier caso
		Assert.assertTrue("Sin mensajes", client.receivedMessages().isEmpty());
	}	
}
