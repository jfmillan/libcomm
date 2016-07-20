package libcomm.layer.socket;

/**
 * Proporciona métodos para procesar un mensaje.
 * <p>
 * 12/03/2016 23:53:11
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
interface IMessageProcessor<M> {
	/* Procesa un mensaje. */
	void process(M message);
}
