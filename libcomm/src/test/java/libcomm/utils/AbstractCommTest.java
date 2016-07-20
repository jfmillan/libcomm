package libcomm.utils;

import java.util.ArrayList;
import java.util.List;

import libcomm.LibcommHandler;
import libcomm.LibcommInterface;
import libcomm.LibcommListener;
import libcomm.connection.ConnectionMode;
import libcomm.connection.ConnectionState;
import libcomm.connection.IConnection;
import libcomm.context.ConnectionContext;
import libcomm.exception.CommErrorType;
import libcomm.exception.CommunicationException;
import libcomm.message.Message;
import libcomm.util.ErrorUtils;

import commons.log.Log;

/**
 * Inicia un servidor o cliente con Libcomm, según el modo que se le indique.
 * 
 * <p>
 * 29/02/2016 21:01:12
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public abstract class AbstractCommTest implements LibcommListener<Message<?>>, LibcommInterface<Message<?>> {

	/* Modo cliente o servidor. */
	@SuppressWarnings("unused")
	private final ConnectionMode mode;
	
	/* Interfaz de comunicaciones. */
	private final LibcommInterface<Message<?>> Libcomm;
	
	/* Configuración de conexión. */
	private final ConnectionContext context;
	
	/* Indica si se ha recibido evento de conexión.*/
	private volatile boolean connectionEvent;
	
	/* Indica si se ha recibido evento de desconexión. */
	private volatile boolean disconnectionEvent;

	/* Último error recibido. */
	private volatile CommunicationException lastError;
	
	/* Mensajes recibidos. */
	private final List<Message<?>> receivedMessages;
	
	/* Constructor, recibe el modo: cliente o servidor */
	AbstractCommTest(ConnectionContext context) {
		this.mode = context.getConnectionMode();
		this.context = context;
		this.receivedMessages = new ArrayList<>();
		this.Libcomm = new LibcommHandler(this, context);
		this.connectionEvent = this.disconnectionEvent = false;
	}

	public ConnectionContext context() {
		return this.context;
	}
	
	@Override
	public void connect() {
		Libcomm.connect();
	}

	@Override
	public void disconnect() {
		Libcomm.disconnect();
	}

	@Override
	public void send(Message<?> message) {
		Libcomm.send(message);
	}

	@Override
	public ConnectionState getConnectionState() {
		return Libcomm.getConnectionState();
	}

	@Override
	public void connected(final IConnection connection) {
		this.connectionEvent = connection != null;
	}

	@Override
	public void disconnected(final IConnection connection) {
		this.disconnectionEvent = connection != null;
	}

	@Override
	public void error(final CommunicationException error) {
		Log.error(AbstractCommTest.class, "Error notificado", error);
		this.lastError = error;
	}
	
	/* Devuelve el error si y solo si existe, y si aparece el mismo tipo al pasado como parámetro. En cualquier caso lo setea a null. */
	public CommunicationException findError(final CommErrorType type) {
		final CommunicationException exception = this.lastError;
		this.lastError = null;
		return ErrorUtils.findErrorType (exception, type) ? exception : null;
	}
	
	@Override
	public void received(final Message<?> message) {
		this.receivedMessages.add(message);
	}

	/* Devuelve el error si lo hay, y en cualquier caso lo setea a null. */
	public CommunicationException error() {
		final CommunicationException result = this.lastError;
		this.lastError = null;
		return result;
	}

	/* Devuelve la lista de mensajes si los hay, y en cualquier caso la limpia. */
	public List<Message<?>> receivedMessages() {
		final List<Message<?>> result = new ArrayList<>(this.receivedMessages);
		this.receivedMessages.clear();
		return result;
	}

	/* Devuelve el evento de conexión si lo hay, y en cualquier caso lo setea a falso. */
	public boolean connectionEvent() {
		final boolean result = this.connectionEvent;
		this.connectionEvent = false;
		return result;
	}

	/* Devuelve el evento de desconexión si lo hay, y en cualquier caso lo setea a falso. */
	public boolean disconnectionEvent() {
		final boolean result = this.disconnectionEvent;
		this.disconnectionEvent = false;
		return result;
	}
}
