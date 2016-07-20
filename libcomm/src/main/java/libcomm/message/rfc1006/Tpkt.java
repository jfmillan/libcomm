package libcomm.message.rfc1006;

import commons.util.ColUtils;
import commons.util.PrintUtils;
import commons.util.StrUtils;

/**
 * Representa un Tpkt o paquete de datos RFC1006. Contiene la cabecera RFC1006 y
 * un TSDU que puede tener sus propias cabeceras.
 * <p>
 * 14/02/2016 17:32:53
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class Tpkt<T extends ITsdu> implements IBytes {

	/* Formato para imprimir */
	private static final String PRINT_FORMAT = "Cabecera: %s  TSDU: %s";
	
	/* Bytes con la cabecera RFC1006. */
	private final byte[] header;
	
	/* TSDU. */
	private final T tsdu;

	/* Tamaño del TPKT en número de bytes. */
	private final int size;
	
	/**
	 * Constructor de clase. Recibe la cabecera y el TSDU.
	 * 
	 * @param rfc1006Header
	 *            Bytes con la cabecera RFC1006.
	 * @param tsdu
	 *            Datos o TSDU.
	 */
	public Tpkt (final byte[] rfc1006Header, final T tsdu) {
		this.header = ColUtils.copy(rfc1006Header);
		this.tsdu = tsdu;
		this.size = (header != null ? header.length : 0) + tsdu.size();
	}

	@Override
	public byte[] getBytes() {
		return ColUtils.concat(header, tsdu.getBytes());
	}
	
	/**
	 * Obtiene el tamaño en bytes del TPKT.
	 * 
	 * @return Tamaño del TPKT.
	 */
	public int size() {
		return this.size;
	}
	
	@Override
	public String toString() {
		return PrintUtils.format(PRINT_FORMAT, 
			PrintUtils.print(header), 
			(tsdu != null ? PrintUtils.print(tsdu.getBytes()) : StrUtils.EMPTY_STRING)
		);
	}
}