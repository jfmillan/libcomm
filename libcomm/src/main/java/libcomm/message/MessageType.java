package libcomm.message;

import org.apache.commons.lang3.StringUtils;

import commons.util.Constants;


/**
 * Tipos de mensaje disponibles en el protocolo con su constructor asociado.
 * <p>
 * 12/01/2016 23:53:36
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public enum MessageType implements IPrintForMessage {
	PR (new BuilderPR()), /* (P)osition (R)eached */
	GT (new BuilderGT()), /* (G)o (T)o */
	ST (new BuilderST()), /* (ST)ate */
	AK (new BuilderAK()); /* (A)c(K)nowledgement */

	private final MessageAbstractBuilder<?> builder;
	
	private MessageType(final MessageAbstractBuilder<?> builder) {
		this.builder = builder;
	}
	
	/**
	 * Imprime el tipo de mensaje.
	 * 
	 * @return Tipo de mensaje como {@link String}.
	 */
	@Override
	public String printForMessage() {
		return this.toString();
	}
	
	/**
	 * Imprime los distintos tipos de mensaje, de modo que puedan ser utilizados
	 * para identificarlos en una expresión regular: tipos separados por
	 * caracter <code>|</code>.
	 * 
	 * @return Todos los tipos separados por una barra vertical, de forma que
	 *         pueda ser utilizado como expresión regular.
	 */
	public static String printAllForRegex() {
		return StringUtils.join(MessageType.values(), Constants.VERTICAL_SLASH);
	}

	/**
	 * Devuelve el constructor de mensajes asociado al tipo.
	 * 
	 * @return constructor de mensaje.
	 */
	MessageAbstractBuilder<?> getBuilder() {
		return builder;
	}
}
