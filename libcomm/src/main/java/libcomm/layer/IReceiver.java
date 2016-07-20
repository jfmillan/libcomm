package libcomm.layer;

/**
 * Proporciona métodos para recibir un mensaje.
 * <p>
 * 13/03/2016 12:35:53
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public interface IReceiver<M> {
	/**
	 * Gestiona la recepción de un mensaje.
	 * 
	 * @param message
	 *            Mensaje.
	 */
	void receive (M message);
}
