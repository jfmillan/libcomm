package libcomm.communication.transmission;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import libcomm.connection.ConnectionMode;
import libcomm.connection.ConnectionState;
import libcomm.exception.CommErrorType;
import libcomm.exception.CommunicationException;
import libcomm.message.Message;
import libcomm.utils.AbstractCommTest;

import org.junit.Assert;
import org.junit.Test;

import commons.log.Log;
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
public abstract class AbstractTransmissionTest {
	
	/* Cliente. */
	private final AbstractCommTest client;
	
	/* Servidor. */
	private final AbstractCommTest server;
	
	/* Clase para mostrar en logs. */
	private final Class<?> clazz;
	
	/* Mensajes que envía el cliente y que son esperados en el servidor. */
	private final List<Message<?>> clientSendsToServer;
	
	/* Mensajes que envía el servidor y que son esperados en el cliente. */
	private final List<Message<?>> serverSendsToClient;
	
	
	/* Constructor. Indica la clase que se usará en el log. */
	public AbstractTransmissionTest(final Class<?> clazz, final AbstractCommTest server, final AbstractCommTest client) {
		this.clazz = clazz != null ? clazz : this.getClass();
		this.clientSendsToServer = new ArrayList<>();
		this.serverSendsToClient = new ArrayList<>();
		this.client = client;
		this.server = server;
	}

	/* Constructor. Para el log se utilizará la propia clase abstracta. */
	public AbstractTransmissionTest(final AbstractCommTest server, final AbstractCommTest client) {
		this(null, server, client);
	}


	/** Realiza la lógica de cada test concreto: genera mensajes, los envía y los comprueba.
	 * @throws Exception si se produce algún error. */
	abstract void concreteTransmissionTest() throws Exception;

	
	/**
	 * Obtiene el cliente.
	 * 
	 * @return Cliente.
	 */
	public AbstractCommTest getClient() {
		return client;
	}
	
	
	/**
	 * Obtiene el servidor.
	 * 
	 * @return Servidor.
	 */
	public AbstractCommTest getServer() {
		return server;
	}
	
	
	/**
	 * Test que transmite mensajes. El número, tipo y sentido de los mensajes
	 * dependerá de lo que implemente cada test concreto en el método
	 * concreteTransmissiónTest.
	 * <ul>
	 * <li>Conecta servidor y cliente.
	 * <li>Realiza la lógica de cada test concreto: genera mensajes, los envía y
	 * los comprueba.
	 * <li>Desconecta cliente y servidor.
	 * <li>Verifica que no hay más errores.
	 * </ul>
	 * 
	 * @throws Exception
	 *             en caso de error.
	 */
	@Test
	public void transmissionTest() throws Exception {
		/* Establece la conexión. */
		stablishConnection();

		/* Lógica concreta de cada test. */
		concreteTransmissionTest();
		
		/* desconectta */
		disconnection();
		
		/* Comprueba que no hay errores antes de finalizar. */
		assertNoErrors();
	}

	
	/**
	 * Añade un mensaje a enviar por el cliente o por el servidor, sin número de
	 * secuencia.
	 * 
	 * @param mode
	 *            Modo de conexión.
	 * @param message
	 *            Mensaje a enviar.
	 */
	void addMessageToSend (final ConnectionMode mode, final Message<?> message) {
		final List<Message<?>> list;
		if (ConnectionMode.CLIENT.equals(mode)) {
			list = clientSendsToServer;
		} else if (ConnectionMode.SERVER.equals(mode)) {
			list = serverSendsToClient;
		} else {
			throw new IllegalArgumentException("Modo null, debe indicarse cliente o servidor");
		}
		list.add(message);
	}

	
	/* Añade un mensaje a enviar por el cliente al servidor, sin número de secuencia. */
	boolean addClientToServerMessage (final Message<?> message) {
		return this.clientSendsToServer.add(message);
	}

	
	/* Añade un mensaje a enviar por el servidor al cliente, sin número de secuencia. */
	boolean addServerToClientMessage (final Message<?> message) {
		return this.serverSendsToClient.add(message);
	}
	
	/* Obtiene los mensajes de cliente a servidor. */
	List<Message<?>> getClientSendsToServerMessages () {
		return this.clientSendsToServer;
	}
	
	/* Obtiene los mensajes de servidor a cliente. */
	List<Message<?>> getServerSendsToServerMessages() {
		return this.serverSendsToClient;
	}
	
	/* Comprueba que no hay errores en cliente o servidor. */
	void assertNoErrors () {
		Assert.assertNull("No hay error en cliente", client.error());
		Assert.assertNull("No hay error en servidor", server.error());
	}
	
	
	/* Establece la conexión de cliente y servidor. */
	void stablishConnection() 
			throws CommunicationException, InterruptedException {
		assertEquals("Comenzamos desconectados en cliente", ConnectionState.DISCONNECTED, client.getConnectionState());
		assertEquals("Comenzamos desconectados en servidor", ConnectionState.DISCONNECTED, server.getConnectionState());
		
		Log.info(clazz, "Servidor desconectado -> conectando #########################################################");
		server.connect();
		Thread.sleep(Constants.SECOND);
		assertEquals("Estamos conectando", ConnectionState.CONNECTING, server.getConnectionState());
		Assert.assertNull("No hay error", server.error());
		Thread.sleep(server.context().getConnectionTimeout()+Constants.SECOND);
		assertEquals("Seguimos conectando", ConnectionState.CONNECTING, server.getConnectionState());
		Assert.assertFalse("No hay evento de conexión", server.connectionEvent());
		Assert.assertFalse("No hay evento de desconexión", server.disconnectionEvent());		
		Assert.assertNull("No hay error", server.error());
		
		Log.info(clazz, "Cliente conectando, conectado, sin errores ##################################################");
		client.connect();
		Thread.sleep(Constants.SECOND);
		assertEquals("Estamos conectados en cliente", ConnectionState.CONNECTED, client.getConnectionState());
		assertEquals("Estamos conectados en servidor", ConnectionState.CONNECTED, server.getConnectionState());
		Assert.assertTrue("Hay evento de conexión en cliente", client.connectionEvent());
		Assert.assertTrue("Hay evento de conexión en servidor", server.connectionEvent());
		Thread.sleep(client.context().getConnectionTimeout() + Constants.SECOND);
		assertNoErrors();
		Assert.assertFalse("No hay evento de desconexión en cliente", client.disconnectionEvent());		
		Assert.assertFalse("No hay evento de desconexión en servidor", server.disconnectionEvent());
	}

	
	/* Desconecta los sistemas */
	void disconnection() throws InterruptedException {
		Log.info(this, "Servidor desconectado, se desconecta cliente, error, pero reconecta ##########################");
		server.disconnect();
		Thread.sleep(2*Constants.SECOND);
		Assert.assertTrue("Hay evento de desconexión en cliente", client.disconnectionEvent());
		Assert.assertTrue("Hay evento de desconexión en servidor", server.disconnectionEvent());
		Assert.assertNotNull("Hay error en cliente", client.findError(CommErrorType.REMOTE_DISCONNECTION));
		Assert.assertNull("No hay error en servidor", server.error());
		Assert.assertFalse("No hay evento de conexión en cliente", client.connectionEvent());		
		Assert.assertFalse("No hay evento de conexión en servidor", server.connectionEvent());
		assertEquals("Estamos desconectados en servidor", ConnectionState.DISCONNECTED, server.getConnectionState());
		assertEquals("Estamos conectando en cliente", ConnectionState.CONNECTING, client.getConnectionState());
		
		Log.info(this, "Cliente desconectado por timeout #############################################################");
		Thread.sleep(client.context().getConnectionTimeout() + Constants.SECOND);
		Assert.assertNotNull("Hay error en cliente", client.findError(CommErrorType.TIMEOUT));
		Assert.assertFalse("No hay evento de conexión en cliente", client.connectionEvent());		
		Assert.assertTrue("Hay evento de desconexión en cliente", client.disconnectionEvent());
		assertEquals("Estamos desconectados en cliente", ConnectionState.DISCONNECTED, client.getConnectionState());
		
		Thread.sleep(3*Constants.SECOND);
		
		assertNoErrors();
	}
	
	
	/**
	 * Se comprueba y asegura que dos listas de mensajes son iguales, tanto en
	 * el contenido de cada mensaje como en su orden. Se recorre elemento a
	 * elemento para poder comprobar cada mensaje por separado. En este punto
	 * los mensajes deben ser totalmente iguales, incluido número de secuenia.
	 * 
	 * @param expected
	 *            Mensajes esperados.
	 * @param obtained
	 *            Mensajes obtenidos realmente.
	 */ 
	<M extends Message<?>> void checkMessages (final List<M> expected, final List<M> obtained) {
		Assert.assertNotNull("Listado de mensajes esperados no nulo", expected);
		Assert.assertNotNull("Listado de mensajes obtenidos no nulo", obtained);
		
		Assert.assertFalse("Listado de mensajes esperados no vacío", expected.isEmpty());
		Assert.assertFalse("Listado de mensajes obtenidos no vacío", obtained.isEmpty());
		
		Assert.assertEquals("Listado de mensajes esperados y obtenidos tienen el mismo tamaño", expected.size(), obtained.size());
		
		Message<?> expectedMsg;
		Message<?> obtainedMsg;
		for (int i = 0; i < expected.size(); i++) {
			expectedMsg = expected.get(i);
			obtainedMsg = expected.get(i);
			Assert.assertNotNull("Mensaje esperado no nulo", expectedMsg);
			Assert.assertNotNull("Mensaje obtenido no nulo", obtainedMsg);
			Assert.assertEquals(
				PrintUtils.format("Mensaje esperado '%s' igual a mensaje obtenido '%s'", expectedMsg, obtainedMsg), 
				expectedMsg.toString(), obtainedMsg.toString()
			);
		}
	}
}
