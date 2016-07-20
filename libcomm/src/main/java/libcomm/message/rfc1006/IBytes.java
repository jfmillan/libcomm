package libcomm.message.rfc1006;

/**
 * Especifica métodos necesarios para trabajar con Bytes en Rfc1006.
 * <p>
 * 15/01/2016 20:16:55
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public interface IBytes {
	/**
	 * Obtiene una representación del objeto como array de bytes unidimensional.
	 * 
	 * @return Array de bytes unidimiensional que representa el objeto.
	 */
	byte[] getBytes();
}
