package libcomm.layer;


/**
 * Comandos disponibles a ejecutar en una capa de comunicación.
 * <p>
 * 11/02/2016 23:55:36
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public interface ILayerCommands<M> extends ISender<M> {
	/** Establece la conexión.*/
	void connect();
	
	/** Corta la conexión. */
	void disconnect();
}
