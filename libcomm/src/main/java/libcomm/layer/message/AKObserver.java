package libcomm.layer.message;

/**
 * Observador parametrizado con clave y tipo de mensaje, para eventos
 * relacionados con la gestión de AK.
 * <p>
 * 21/02/2016 18:14:32
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
interface AKObserver<K, M> {
	
	/* Notifica que hay que reenviar un mensaje. */
	void resendMessage (M message);
	
	/* Notifica que se ha producido un error por AK no recibido. */
	void akFailure (K key, M message);
}
