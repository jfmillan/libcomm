package libcomm.message;

import libcomm.util.MessageUtils;

/**
 * Establece métodos para imprimir el mensaje como {@link String}.
 * <p>
 * 13/01/2016 01:46:09
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public interface IPrintForMessage {

	/**
	 * Imprime un mensaje o parte de un mensaje como {@link String}. En caso de
	 * no tener valor que imprimir, debe imprimir cadena vacía o caracteres de
	 * relleno por defecto <code>*</code> establecido en
	 * {@link MessageUtils#MESSAGE_PAD} con el tamaño oportuno, nunca <code>null</code>.
	 * 
	 * @return {@link String} con el mensaje o parte.
	 */
	String printForMessage();
}
