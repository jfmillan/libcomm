package libcomm.message;

import java.util.Map;

import libcomm.exception.CommErrorType;
import libcomm.exception.MalformedMessageException;
import libcomm.message.parser.ParserGT;

/**
 * Constructor de mensaje GT (<i>go to</i>).
 * <p>
 * 17/01/2016 00:32:35
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class BuilderGT extends MessageAbstractBuilder<MessageGT> {

	/**
	 * Constructor de mensaje GT (<i>go to</i>).
	 */
	BuilderGT() {
		super (new ParserGT());
	}

	
	/**
	 * Construye un mensaje vacío GT (<i>go to</i>).
	 * 
	 * @return Mensaje ST (<i>go to</i>).
	 */
	@Override
	public MessageGT buildEmptyMessage() {
		return new MessageGT(new ContentGT());
	}
	
	/**
	 * Construye un mensaje GT (<i>go to</i>) a partir de un número de secuencia
	 * opcional y un contenido en forma de cadena de texto.
	 * 
	 * @param sequenceNumber
	 *            Número de secuencia.
	 * @param content
	 *            Contenido en forma de cadena de texto.
	 * @return Mensaje GT (<i>go to</i>).
	 * @throws MalformedMessageException
	 *             Si el mensaje está mal formado.
	 */
	@SuppressWarnings("unchecked")
	@Override
	MessageGT buildMessageFromContent(final SequenceNumber sequenceNumber, final String content) 
			throws MalformedMessageException {
		final MessageGT gt = buildEmptyMessage();
		setSequenceNumber(gt, sequenceNumber);

		try {
			final Map<FieldMetaData, Field<?>> fields = getParser().parse(content);

			/* id de bulto, opcional */
			final Field<Long> packageId = (Field<Long>) fields.get(FieldMetaData.PACKAGE_ID);
			gt.setPackageId(packageId.getValue());
			
			/* posición, obligatorio */
			final Field<String> position = (Field<String>) fields.get(FieldMetaData.POSITION);
			gt.setPosition(position.getValue());
		} catch (MalformedMessageException e) {
			throw e;
		} catch (Exception e) {
			throw MalformedMessageException.createException(CommErrorType.BUILD_MESSAGE, MessageType.GT, content, e);
		}
		return gt;
	}
}
