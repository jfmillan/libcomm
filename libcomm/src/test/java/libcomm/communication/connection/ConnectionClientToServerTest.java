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
 * Tests de conexión y desconexión entre cliente y servidor.
 * <p>
 * 06/03/2016 11:14:15
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class ConnectionClientToServerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConfigureLog.configure(LogSystem.DEFAULT);
	}

	/**
	 * Comprueba los siguientes estados:
	 * <ul>
	 * <li>Servidor conectando
	 * <li>Cliente conectando, conectado, sin errores
	 * <li>Cliente desconectado
	 * <li>Servidor se desconecta, error, pero reconecta
	 * <li>Servidor conectando
	 * <li>Cliente conectando, conectado, sin errores
	 * <li>Servidor desconectado
	 * <li>Cliente se desconecta, error, pero se reconecta
	 * <li>Cliente conectando, se desconecta por timeout
	 * </ul>
	 * 
	 * @throws CommunicationException
	 *             en caso de producrse algún error de comunicaciones.
	 * @throws InterruptedException
	 *             en caso de producirse la interrupción del hilo.
	 */
	@Test
	public void connectionClientToServer() throws CommunicationException, InterruptedException {
		final CommTest client = new CommTest("connectionClientToServer", ConnectionMode.CLIENT);
		final CommTest server = new CommTest("connectionClientToServer", ConnectionMode.SERVER);
		assertEquals("Comenzamos desconectados en cliente", ConnectionState.DISCONNECTED, client.getConnectionState());
		assertEquals("Comenzamos desconectados en servidor", ConnectionState.DISCONNECTED, server.getConnectionState());
		
		Log.info(this, "1. Servidor desconectado -> conectando #######################################################");
		server.connect();
		Thread.sleep(Constants.SECOND);
		assertEquals("Estamos conectando", ConnectionState.CONNECTING, server.getConnectionState());
		Assert.assertNull("No hay error", server.error());
		Thread.sleep(server.context().getConnectionTimeout()+Constants.SECOND);
		assertEquals("Seguimos conectando", ConnectionState.CONNECTING, server.getConnectionState());
		Assert.assertFalse("No hay evento de conexión", server.connectionEvent());
		Assert.assertFalse("No hay evento de desconexión", server.disconnectionEvent());		
		Assert.assertNull("No hay error", server.error());
		
		Log.info(this, "2. Cliente conectando, conectado, sin errores ################################################");
		client.connect();
		Thread.sleep(Constants.SECOND);
		assertEquals("Estamos conectados en cliente", ConnectionState.CONNECTED, client.getConnectionState());
		assertEquals("Estamos conectados en servidor", ConnectionState.CONNECTED, server.getConnectionState());
		Assert.assertTrue("Hay evento de conexión en cliente", client.connectionEvent());
		Assert.assertTrue("Hay evento de conexión en servidor", server.connectionEvent());

		Thread.sleep(client.context().getConnectionTimeout() + Constants.SECOND);
		Assert.assertNull("No hay error en cliente", client.error());
		Assert.assertNull("No hay error en servidor", server.error());
		Assert.assertFalse("No hay evento de desconexión en cliente", client.disconnectionEvent());		
		Assert.assertFalse("No hay evento de desconexión en servidor", server.disconnectionEvent());


		Log.info(this, "3. Cliente desconectado, se desconecta servidor, error, pero reconecta, conectando ###########");
		client.disconnect();
		Thread.sleep(3*Constants.SECOND);
		Assert.assertTrue("Hay evento de desconexión en cliente", client.disconnectionEvent());
		Assert.assertTrue("Hay evento de desconexión en servidor", server.disconnectionEvent());
		Assert.assertNull("No hay error en cliente", client.error());
		Assert.assertNotNull("Hay error en servidor", server.findError(CommErrorType.REMOTE_DISCONNECTION));
		Assert.assertFalse("No hay evento de conexión en cliente", client.connectionEvent());		
		Assert.assertFalse("No hay evento de conexión en servidor", server.connectionEvent());
		assertEquals("Estamos desconectados en cliente", ConnectionState.DISCONNECTED, client.getConnectionState());
		assertEquals("Estamos conectando en servidor", ConnectionState.CONNECTING, server.getConnectionState());


		Log.info(this, "4. Cliente conectando, conectado, sin errores ################################################");
		client.connect();
		Thread.sleep(Constants.SECOND);
		assertEquals("Estamos conectados en cliente", ConnectionState.CONNECTED, client.getConnectionState());
		assertEquals("Estamos conectados en servidor", ConnectionState.CONNECTED, server.getConnectionState());
		Assert.assertTrue("Hay evento de conexión en cliente", client.connectionEvent());
		Assert.assertTrue("Hay evento de conexión en servidor", server.connectionEvent());
		Assert.assertFalse("No hay evento de desconexión en cliente", client.disconnectionEvent());		
		Assert.assertFalse("No hay evento de desconexión en servidor", server.disconnectionEvent());
		Assert.assertNull("No hay error en servidor", server.error());
		Assert.assertNull("No hay error en cliente", client.error());

		Log.info(this, "5. Servidor desconectado, se desconecta cliente, error, pero reconecta #######################");
		server.disconnect();
		Thread.sleep(3*Constants.SECOND);
		Assert.assertTrue("Hay evento de desconexión en cliente", client.disconnectionEvent());
		Assert.assertTrue("Hay evento de desconexión en servidor", server.disconnectionEvent());
		Assert.assertNotNull("Hay error en cliente", client.findError(CommErrorType.REMOTE_DISCONNECTION));
		Assert.assertNull("No hay error en servidor", server.error());
		Assert.assertFalse("No hay evento de conexión en cliente", client.connectionEvent());		
		Assert.assertFalse("No hay evento de conexión en servidor", server.connectionEvent());
		assertEquals("Estamos desconectados en servidor", ConnectionState.DISCONNECTED, server.getConnectionState());
		assertEquals("Estamos conectando en cliente", ConnectionState.CONNECTING, client.getConnectionState());
		
		Log.info(this, "6. Cliente desconectado por timeout ##########################################################");
		Thread.sleep(client.context().getConnectionTimeout() + Constants.SECOND);
		Assert.assertNotNull("Hay error en cliente", client.findError(CommErrorType.TIMEOUT));
		Assert.assertFalse("No hay evento de conexión en cliente", client.connectionEvent());		
		Assert.assertTrue("Hay evento de desconexión en cliente", client.disconnectionEvent());
		assertEquals("Estamos desconectados en cliente", ConnectionState.DISCONNECTED, client.getConnectionState());
		
		// No ha habido mensajes en cualquier caso
		Assert.assertTrue("Sin mensajes en cliente", client.receivedMessages().isEmpty());
		Assert.assertTrue("Sin mensajes en servidor", server.receivedMessages().isEmpty());
	}
}
