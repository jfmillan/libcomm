package libcomm;

import libcomm.connection.IConnection;
import libcomm.exception.CommunicationException;

/**
 * Notificaciones que puede realizar la librería de comunicaciones al exterior.
 * <p>
 * 09/01/2016 23:04:24
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public interface LibcommListener<M> {
	/**
	 * Notifica que se ha completado la conexión.
	 * 
	 * @param connection
	 *            Conexión que se ha completado.
	 */
	void connected (IConnection connection);
	
	/**
	 * Notifica de algún tipo de error de comunicación.
	 * 
	 * @param error
	 *            Error a notificar.
	 */
	void error(CommunicationException error);
	
	/**
	 * Notifica que se ha producido una desconexión.
	 * 
	 * @param connection
	 *            Conexión desconectada.
	 */
	void disconnected (IConnection connection);
	
	/**
	 * Notifica que se ha recibido un mensaje.
	 * 
	 * @param message
	 *            Mensaje recibido.
	 */
	void received (M message);
}
