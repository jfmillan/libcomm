package libcomm.communication.connection;

import static org.junit.Assert.assertEquals;
import libcomm.connection.ConnectionMode;
import libcomm.connection.ConnectionState;
import libcomm.exception.CommunicationException;
import libcomm.utils.CommTest;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import commons.log.ConfigureLog;
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
public class ConnectingServerTest {
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConfigureLog.configure(LogSystem.DEFAULT);
	}

	/**
	 * Servidor: Comprueba el paso de desconectado a conectando, de nuevo a
	 * desconectado, y de nuevo a conectando. La conexión no llega a
	 * establecerse.
	 * 
	 * @throws CommunicationException
	 *             en caso de producrse algún error de comunicaciones.
	 * @throws InterruptedException
	 *             en caso de producirse la interrupción del hilo.
	 */
	@Test
	public void connectingServer() throws CommunicationException, InterruptedException {
		final CommTest server = new CommTest("connectingServer", ConnectionMode.SERVER);
		assertEquals("Comenzamos desconectados", ConnectionState.DISCONNECTED, server.getConnectionState());
		
		// desconectado -> conectando
		server.connect();
		Thread.sleep(Constants.SECOND);
		assertEquals("Estamos conectando", ConnectionState.CONNECTING, server.getConnectionState());
		Assert.assertNull("No hay error", server.error());
		Thread.sleep(server.context().getConnectionTimeout()+Constants.SECOND);
		assertEquals("Seguimos conectando", ConnectionState.CONNECTING, server.getConnectionState());
		Assert.assertFalse("No hay evento de conexión", server.connectionEvent());
		Assert.assertFalse("No hay evento de desconexión", server.disconnectionEvent());		
		Assert.assertNull("No hay error", server.error());
		
		// conectando -> desconectado
		server.disconnect();
		Thread.sleep(Constants.SECOND);
		assertEquals("Cancelamos la conexión", ConnectionState.DISCONNECTED, server.getConnectionState());
		Assert.assertFalse("No hay evento de conexión", server.connectionEvent());
		Assert.assertTrue("Hay evento de desconexión", server.disconnectionEvent());
		Assert.assertNull("No hay error", server.error());
		
		// desconectado -> conectando
		server.connect();
		Thread.sleep(Constants.SECOND);
		assertEquals("Estamos conectando de nuevo", ConnectionState.CONNECTING, server.getConnectionState());
		Assert.assertNull("No hay error", server.error());
		Thread.sleep(server.context().getConnectionTimeout()+Constants.SECOND);
		assertEquals("Seguimos conectando de nuevo", ConnectionState.CONNECTING, server.getConnectionState());
		Assert.assertFalse("No hay evento de conexión", server.connectionEvent());
		Assert.assertFalse("No hay evento de desconexión", server.disconnectionEvent());
		Assert.assertNull("No hay error", server.error());

		// desconectado
		server.disconnect();
		Thread.sleep(Constants.SECOND);
		assertEquals("Cancelamos la conexión", ConnectionState.DISCONNECTED, server.getConnectionState());
		Assert.assertFalse("No hay evento de conexión", server.connectionEvent());
		Assert.assertTrue("Hay evento de desconexión", server.disconnectionEvent());
		Assert.assertNull("No hay error", server.error());
		
		// No ha habido mensajes en cualquier caso
		Assert.assertTrue("Sin mensajes", server.receivedMessages().isEmpty());
	}
}
