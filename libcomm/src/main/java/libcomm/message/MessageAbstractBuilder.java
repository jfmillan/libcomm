package libcomm.message;

import java.util.Objects;

import libcomm.exception.MalformedMessageException;
import libcomm.message.parser.IMessageParser;



/**
 * Clase abstracta para construir un mensaje vacío.
 * <p>
 * 16/01/2016 18:49:35
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
abstract class MessageAbstractBuilder<M extends Message<?>> {
	/* Parser del constructor del mensaje. */
	private final IMessageParser parser;
	
	/**
	 * Constructor de clase.
	 * 
	 * @param parser
	 *            Parseador del constructor de mensajes.
	 */
	MessageAbstractBuilder(final IMessageParser parser) {
		Objects.requireNonNull(parser);
		this.parser = parser;
	}
	
	/* Develve el parser que el constructor de mensajes necesita. */
	protected IMessageParser getParser() {
		return parser;
	}
	
	/* Establece el número de secuencia que se pasa, o un nuevo número de secuencia sin definir
	 * en caso de pasar null.
	 */
	protected void setSequenceNumber (final Message<?> message, final SequenceNumber sequenceNumber) {
		Objects.requireNonNull(message);
		message.setSequenceNumber(sequenceNumber != null ? sequenceNumber : new SequenceNumber());
	}
	
	/**
	 * Construye un mensaje vacío.
	 * 
	 * @return Mensaje vacío.
	 */
	abstract M buildEmptyMessage();
	
	/**
	 * Construye un mensaje a partir de un número de secuencia (opcional) y un
	 * contenido en formato de cadena de texto.
	 * 
	 * @param sequenceNumber
	 *            Número de secuencia, opcional
	 * @param content
	 *            Contenido en forma de cadena de texto.
	 * @return Mensaje construido.
	 * @throws MalformedMessageException
	 *             si el mensaje está mal formado.
	 */
	abstract M buildMessageFromContent(SequenceNumber sequenceNumber, String content) throws MalformedMessageException;
	
	/**
	 * Construye un mensaje a partir de un contenido en forma de cadena de
	 * texto. Por defecto, es equivalente a llamar a
	 * {@link #buildMessageFromContent(SequenceNumber, String)} con número de
	 * secuencia <code>null</code>.
	 * 
	 * @param content
	 *            Contenido en forma de cadena de texto.
	 * @return Mensaje construido.
	 * @throws MalformedMessageException
	 *             si el mensaje está mal formado.
	 */
	M buildMessageFromContent(final String content) throws MalformedMessageException {
		return buildMessageFromContent(null, content);
	}
}
