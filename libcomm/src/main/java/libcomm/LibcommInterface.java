package libcomm;

import libcomm.connection.ConnectionState;
import libcomm.message.Message;


/**
 * Interfaz que facilita las acciones disponibles desde un sisema externo a
 * Libcomm.
 * <p>
 * 10/01/2016 19:14:37
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public interface LibcommInterface<M> {
	/**
	 * Establece la conexión.
	 */
	void connect();
	
	/**
	 * Corta la conexión.
	 */
	void disconnect();
	
	/**
	 * Envía un mensaje {@link Message}.
	 * 
	 * @param message
	 *            Mensaje.
	 */
	void send (M message);
	
	/**
	 * Devuelve el estado de la conexión: conectado, desconectado, conectando...
	 * @return Estado de conexión
	 */
	ConnectionState getConnectionState();
}
