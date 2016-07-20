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
public class ConnectingServerTwiceTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConfigureLog.configure(LogSystem.DEFAULT);
	}

	/**
	 * Servidor: Comprueba el paso de desconectado a conectando, intenta
	 * conectar de nuevo cuando ya está conectando, provocando un error pero
	 * permanenciendo en estado conectando. A continuación se desconecta y
	 * vuelve a intentar conectarse.
	 * 
	 * @throws CommunicationException
	 *             en caso de producrse algún error de comunicaciones.
	 * @throws InterruptedException
	 *             en caso de producirse la interrupción del hilo.
	 */
	@Test
	public void connectingServerTwice() throws CommunicationException, InterruptedException {
		final CommTest server = new CommTest("connectingTwiceServer", ConnectionMode.SERVER);
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
		
		// conectando -> conectando (error) -> permanence conectando
		server.connect();
		Thread.sleep(Constants.SECOND);
		Assert.assertNotNull("Error por intentar conectar mientras estamos conectando", server.findError(CommErrorType.CONNECTION_ERROR));
		assertEquals("Seguimos en estado conectando", ConnectionState.CONNECTING, server.getConnectionState());
		Assert.assertFalse("No hay evento de conexión", server.connectionEvent());
		Assert.assertFalse("No hay evento de desconexión", server.disconnectionEvent());

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
		Assert.assertFalse("No hay evento de conexión", server.connectionEvent());
		Assert.assertFalse("No hay evento de desconexión", server.disconnectionEvent());
		
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
