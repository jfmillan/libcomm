package libcomm.message.rfc1006;


/**
 * Representa un TSDU de RFC1006 (por tanto, sin cabecera RFC1006).
 * 
 * <p>
 * 13/02/2016 20:38:31
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public interface ITsdu extends IBytes {
	/**
	 * Devuelve el tamaño del TSDU en bytes.
	 * 
	 * @return Tamaño del TSDU en bytes.
	 */
	int size();
}
