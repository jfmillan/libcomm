package libcomm.message.rfc1006;

import commons.util.ColUtils;
import commons.util.PrintUtils;
import commons.util.StrUtils;

/**
 * Representa el TSDU de un telegrada de datos, DT o <i>Data TSDU</i>. Contiene
 * los bytes sin decodificar que forman un mensaje encapsulado en un
 * <code>DataTsdu</code>. No contiene la cabecera RFC1006, sólo el DT (con su
 * propia cabecera DT).
 * 
 * <p>
 * 17/01/2016 13:13:01
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class DataTsdu implements ITsdu {
	
	/* Formato para imprimir */
	private static final String PRINT_FORMAT = "Cabecera: %s  Cuerpo: %s";
	
	/* Bytes con la cabecera DT. */
	private final byte[] header;
	
	/* Datos útiles del telegrama. */
	private final byte[] body;
	
	/**
	 * Constructor de clase. Hace una copia del array de bytes recibido.
	 * 
	 * @param header
	 *            Bytes que representan la cabecera de un mensaje encapstulado
	 *            en un <code>DataTsdu</code>.
	 * @param body
	 *            Bytes que representan el cuerpo de un mensaje encapsulado en
	 *            un <code>DataTsdu</code>.
	 */
	public DataTsdu (final byte[] header, final byte[] body) {
		this.header = ColUtils.copy(header);
		this.body = ColUtils.copy(body);
	}

	/**
	 * Devuelve los bytes de la cabecera.
	 * 
	 * @return bytes de la cabecera.
	 */
	public byte[] getHeader() {
		return this.header;
	}
	
	/**
	 * Devuelve los bytes del cuerpo del mensaje.
	 * 
	 * @return bytes del cuerpo del mensaje.
	 */
	public byte[] getBody() {
		return this.body;
	}
	
	@Override
	public byte[] getBytes() {
		return ColUtils.concat(header, body);
	}

	@Override
	public int size() {
		return 	(header != null ? header.length : 0)
				+ (body != null ? body.length : 0);
	}
	
	@Override
	public String toString() {
		return PrintUtils.format(PRINT_FORMAT, 
			PrintUtils.print(header), (body != null ? PrintUtils.print(body) : StrUtils.EMPTY_STRING)
		);
	}
}
