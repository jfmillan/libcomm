package libcomm.message;

import java.util.Map;

import libcomm.exception.CommErrorType;
import libcomm.exception.MalformedMessageException;
import libcomm.message.parser.ParserST;

/**
 * Constructor de mensaje ST (<i>state (of position)</i>).
 * <p>
 * 17/01/2016 00:33:25
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class BuilderST extends MessageAbstractBuilder<MessageST> {

	/**
	 * Constructor de mensaje ST (<i>state (of position)</i>).
	 */
	BuilderST() {
		super(new ParserST());
	}

	/**
	 * Construye un mensaje vacío ST (<i>state (of position)</i>).
	 * 
	 * @return Mensaje ST (<i>state (of position)</i>).
	 */
	@Override
	public MessageST buildEmptyMessage() {
		return new MessageST(new ContentST());
	}

	/**
	 * Construye un mensaje ST (<i>state (of position)</i>) a partir de un
	 * número de secuencia opcional y un contenido en forma de cadena de texto.
	 * 
	 * @param sequenceNumber
	 *            Número de secuencia.
	 * @param content
	 *            Contenido en forma de cadena de texto.
	 * @return Mensaje ST (<i>state (of position)</i>).
	 * @throws MalformedMessageException
	 *             Si el mensaje está mal formado.
	 */
	@SuppressWarnings("unchecked")
	@Override
	MessageST buildMessageFromContent(final SequenceNumber sequenceNumber, final String content) 
			throws MalformedMessageException {
		final MessageST st = buildEmptyMessage();
		setSequenceNumber(st, sequenceNumber);

		try {
			final Map<FieldMetaData, Field<?>> fields = getParser().parse(content);

			/* posición, obligatorio */
			final Field<String> position = (Field<String>) fields.get(FieldMetaData.POSITION);
			st.setPosition(position.getValue());

			/* estado enabled/disabled, obligatorio */
			final Field<Character> state = (Field<Character>) fields.get(FieldMetaData.ENABLED_POSITION);
			final FieldFlag flag = FieldFlag.create(FieldMetaData.ENABLED_POSITION, state.getValue());
			if (flag.isFlagPresent()) {
				st.setEnabledPosition(flag.getBooleanValue());
			} else {
				/* Si las expresiones regulares son correctas no debería ocurrir */
				throw MalformedMessageException.createException(CommErrorType.BUILD_MESSAGE, MessageType.ST, content, null);
			}
		} catch (MalformedMessageException e) {
			throw e;
		} catch (Exception e) {
			throw MalformedMessageException.createException(CommErrorType.BUILD_MESSAGE, MessageType.ST, content, e);
		}
		return st;
	}
}
