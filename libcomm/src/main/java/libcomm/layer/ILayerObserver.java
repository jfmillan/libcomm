package libcomm.layer;

import libcomm.exception.CommunicationException;

/**
 * Observador de eventos en una capa de comunicación.
 * <p>
 * 11/02/2016 23:50:57
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public interface ILayerObserver<M> extends IReceiver<M> {

	/** Notificación de conexión. */
	void connected ();

	/** Notificación de desconexión. */
	void disconnected ();
	
	/**
	 * Notificación de desconexión debido a un error.
	 * 
	 * @param cause
	 *            Causa del error.
	 */
	void disconnected (CommunicationException cause);
	
	/**
	 * Notificación de algún error.
	 * 
	 * @param error
	 *            Descripción del error.
	 * @param cause
	 *            Causa del error.
	 */
	void error(String error, CommunicationException cause);
}
