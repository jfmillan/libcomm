package libcomm.layer;

/**
 * Proporciona métodos para enviar un mensaje.
 * <p>
 * 13/03/2016 12:36:26
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public interface ISender<M> {
	/**
	 * Envía un mensaje.
	 * 
	 * @param message
	 *            Mensaje.
	 */
	void send (M message);
}
