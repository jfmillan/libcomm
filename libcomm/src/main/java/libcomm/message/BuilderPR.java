package libcomm.message;

import java.util.Map;

import libcomm.exception.CommErrorType;
import libcomm.exception.MalformedMessageException;
import libcomm.message.parser.ParserPR;

/**
 * Constructor de mensaje PR (<i>position reached</i>).
 * <p>
 * 16/01/2016 23:02:31
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class BuilderPR extends MessageAbstractBuilder<MessagePR> {

	/**
	 * Constructor de mensaje PR (<i>position reached</i>).
	 */
	BuilderPR() {
		super (new ParserPR());
	}

	/**
	 * Construye un mensaje vacío PR (<i>position reached</i>).
	 * 
	 * @return Mensaje PR (<i>position reached</i>).
	 */
	@Override
	public MessagePR buildEmptyMessage() {
		return new MessagePR(new ContentPR());
	}

	
	/**
	 * Construye un mensaje PR (<i>position reached</i>) a partir de un número
	 * de secuencia opcional y un contenido en forma de cadena de texto.
	 * 
	 * @param sequenceNumber
	 *            Número de secuencia.
	 * @param content
	 *            Contenido en forma de cadena de texto.
	 * @return Mensaje PR (<i>position reached</i>).
	 * @throws MalformedMessageException
	 *             Si el mensaje está mal formado.
	 */
	@SuppressWarnings("unchecked")
	@Override
	MessagePR buildMessageFromContent(final SequenceNumber sequenceNumber, final String content) 
			throws MalformedMessageException {
		final MessagePR pr = buildEmptyMessage();
		setSequenceNumber(pr, sequenceNumber);

		try {
			final Map<FieldMetaData, Field<?>> fields = getParser().parse(content);

			/* id de bulto, opcional */
			final Field<Long> packageId = (Field<Long>) fields.get(FieldMetaData.PACKAGE_ID);
			pr.setPackageId(packageId.getValue());
			
			/* posición, obligatorio */
			final Field<String> position = (Field<String>) fields.get(FieldMetaData.POSITION);
			pr.setPosition(position.getValue());
			
			/* peso, opcional */
			final Field<Integer> weight = (Field<Integer>) fields.get(FieldMetaData.WEIGHT);
			pr.setWeight(weight.getValue());
			
			/* requiere contestación, obligatorio */
			final Field<Character> reply = (Field<Character>) fields.get(FieldMetaData.REQUIRED_REPLY);
			final FieldFlag flag = FieldFlag.create(FieldMetaData.REQUIRED_REPLY, reply.getValue());
			if (flag.isFlagPresent()) {
				pr.setRequiredReply(flag.getBooleanValue());
			} else {
				/* Si las expresiones regulares son correctas no debería ocurrir */
				throw MalformedMessageException.createException(CommErrorType.BUILD_MESSAGE, MessageType.PR, content, null);
			}
		} catch (MalformedMessageException e) {
			throw e;
		} catch (Exception e) {
			throw MalformedMessageException.createException(CommErrorType.BUILD_MESSAGE, MessageType.PR, content, e);
		}
		return pr;
	}
}
