package libcomm.message.rfc1006;

import commons.util.ColUtils;

/**
 * Representa el TSDU de un telegrada de confirmación de conexión o <i>Connection
 * Confirmed</i>. Contiene los bytes sin decodificar que forman un telegrama de
 * solicitud de conexión.
 * 
 * <p>
 * 13/02/2016 21:26:01
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class CcTsdu implements ITsdu {
	
	/* Bytes. */
	private final byte[] bytes;
	
	/* Calling tsap */
	private byte[] callingTsap;
	
	/* Called tsap */
	private byte[] calledTsap;
	
	/**
	 * Constructor de clase. Hace una copia del array de bytes recibido.
	 * 
	 * @param bytes
	 *            Bytes que representan un mensaje encapsulado en un
	 *            <code>CcTsdu</code>.
	 */
	public CcTsdu (final byte[] bytes) {
		this.bytes = ColUtils.copy(bytes);
	}

	@Override
	public byte[] getBytes() {
		return bytes;
	}
	
	/**
	 * Devuelve el parámetro indicado.
	 * @return parámetro callingTsap a devolver.
	 */
	public byte[] getCallingTsap() {
		return callingTsap;
	}

	/**
	 * Establece el parámetro indicado.
	 * @param callingTsap Parámetro a establecer en callingTsap.
	 */
	public void setCallingTsap(byte[] callingTsap) {
		this.callingTsap = callingTsap;
	}

	/**
	 * Devuelve el parámetro indicado.
	 * @return parámetro calledTsap a devolver.
	 */
	public byte[] getCalledTsap() {
		return calledTsap;
	}

	/**
	 * Establece el parámetro indicado.
	 * @param calledTsap Parámetro a establecer en calledTsap.
	 */
	public void setCalledTsap(byte[] calledTsap) {
		this.calledTsap = calledTsap;
	}

	@Override
	public int size() {
		return bytes != null ? bytes.length : 0;
	}
}
