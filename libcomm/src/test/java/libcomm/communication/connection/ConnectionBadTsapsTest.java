package libcomm.communication.connection;

import static org.junit.Assert.assertEquals;
import libcomm.connection.ConnectionMode;
import libcomm.connection.ConnectionState;
import libcomm.exception.CommErrorType;
import libcomm.exception.CommunicationException;
import libcomm.utils.BadTsapsCommTest;
import libcomm.utils.CommTest;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import commons.log.ConfigureLog;
import commons.log.Log;
import commons.log.LogSystem;
import commons.util.Constants;

/**
 * Tests de conexión y desconexión entre cliente y servidor cuyos TSAP no coinciden.
 * <p>
 * 14/03/2016 21:53:10
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class ConnectionBadTsapsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConfigureLog.configure(LogSystem.DEFAULT);
	}

	/**
	 * Comprueba los siguientes estados:
	 * <ul>
	 * <li>Servidor conectando
	 * <li>Cliente conectando, error por TSAP incorrecto
	 * <li>Cliente y servidor desconectados con error
	 * <li>Servidor conectando, sigue en escucha
	 * <li>Cliente con TSAPs correctos conectado, sin errores
	 * </ul>
	 * 
	 * @throws CommunicationException
	 *             en caso de producrse algún error de comunicaciones.
	 * @throws InterruptedException
	 *             en caso de producirse la interrupción del hilo.
	 */
	@Test
	public void connectionBadTsaps() throws CommunicationException, InterruptedException {
		final BadTsapsCommTest badTsapsClient = new BadTsapsCommTest("connectionBadTsaps", ConnectionMode.CLIENT);
		final CommTest server = new CommTest("connectionBadTsaps", ConnectionMode.SERVER);
		final CommTest client = new CommTest("connectionBadTsaps", ConnectionMode.CLIENT);
		
		assertEquals("Comenzamos desconectados en cliente con TSAPs incorrecto", ConnectionState.DISCONNECTED, badTsapsClient.getConnectionState());
		assertEquals("Comenzamos desconectados en cliente correcto", ConnectionState.DISCONNECTED, client.getConnectionState());
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
		
		
		Log.info(this, "2. Cliente conectando, error por TSAP incorrecto #############################################");
		badTsapsClient.connect();
		Thread.sleep(2*Constants.SECOND);
		Assert.assertFalse("No hay evento de conexión en cliente", badTsapsClient.connectionEvent());
		Assert.assertFalse("No hay evento de conexión en servidor", server.connectionEvent());
		Assert.assertTrue("Hay evento de desconexión en servidor", server.disconnectionEvent());
		Assert.assertNotNull("Hay error por TSPAs en servidor", server.findError(CommErrorType.TSAPS));
		assertEquals("Estamos desconectados en servidor", ConnectionState.DISCONNECTED, server.getConnectionState());

		Thread.sleep(badTsapsClient.context().getConnectionTimeout()+Constants.SECOND);
		Assert.assertNotNull("Hay error en cliente (timeout)", badTsapsClient.findError(CommErrorType.TIMEOUT));
		Assert.assertTrue("Hay evento de desconexión en cliente (timeout)", badTsapsClient.disconnectionEvent());		
		assertEquals("Estamos desconectados en cliente", ConnectionState.DISCONNECTED, badTsapsClient.getConnectionState());


		Log.info(this, "3. Volvemos a conectar servidor ##############################################################");
		server.connect();
		Thread.sleep(Constants.SECOND);
		assertEquals("Estamos conectando", ConnectionState.CONNECTING, server.getConnectionState());
		Assert.assertNull("No hay error", server.error());
		Thread.sleep(server.context().getConnectionTimeout()+Constants.SECOND);
		assertEquals("Seguimos conectando", ConnectionState.CONNECTING, server.getConnectionState());
		Assert.assertFalse("No hay evento de conexión", server.connectionEvent());
		Assert.assertFalse("No hay evento de desconexión", server.disconnectionEvent());		
		Assert.assertNull("No hay error", server.error());

		Log.info(this, "4. Cliente con TSPAS correctos conectado, sin errores ########################################");
		client.connect();
		Thread.sleep(2*Constants.SECOND);
		assertEquals("Estamos conectados en cliente", ConnectionState.CONNECTED, client.getConnectionState());
		assertEquals("Estamos conectados en servidor", ConnectionState.CONNECTED, server.getConnectionState());
		Assert.assertTrue("Hay evento de conexión en cliente", client.connectionEvent());
		Assert.assertTrue("Hay evento de conexión en servidor", server.connectionEvent());

		Thread.sleep(client.context().getConnectionTimeout() + Constants.SECOND);
		Assert.assertNull("No hay error en cliente", client.error());
		Assert.assertNull("No hay error en servidor", server.error());
		Assert.assertFalse("No hay evento de desconexión en cliente", client.disconnectionEvent());		
		Assert.assertFalse("No hay evento de desconexión en servidor", server.disconnectionEvent());
		

		// No ha habido mensajes en cualquier caso
		Assert.assertTrue("Sin mensajes en cliente incorrecto", badTsapsClient.receivedMessages().isEmpty());
		Assert.assertTrue("Sin mensajes en cliente correcto", client.receivedMessages().isEmpty());
		Assert.assertTrue("Sin mensajes en servidor", server.receivedMessages().isEmpty());
		
		/* desconectamos todo para que no interfiera en otros test por estar el puerto ocupado escuchando. */
		client.disconnect();
		Thread.sleep(2*Constants.SECOND);
		server.disconnect();
		Thread.sleep(2*Constants.SECOND);
	}
}
