package libcomm.message;

import java.util.Map;

import libcomm.exception.CommErrorType;
import libcomm.exception.MalformedMessageException;
import libcomm.message.parser.ParserAK;

/**
 * Constructor de mensaje AK (<i>acknowgledgement</i>).
 * <p>
 * 17/01/2016 00:33:25
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class BuilderAK extends MessageAbstractBuilder<MessageAK> {
	
	/**
	 * Constructor de mensaje AK (<i>acknowgledgement</i>).
	 */
	BuilderAK() {
		super (new ParserAK());
	}

	/**
	 * Construye un mensaje vacío AK (<i>acknowgledgement</i>).
	 * 
	 * @return Mensaje AK (<i>acknowgledgement</i>).
	 */
	@Override
	public MessageAK buildEmptyMessage() {
		return new MessageAK(new ContentAK());
	}
	
	/**
	 * Construye un mensaje AK (<i>acknowgledgement</i>). a partir de un número
	 * de secuencia opcional y un contenido en forma de cadena de texto.
	 * 
	 * @param sequenceNumber
	 *            Número de secuencia.
	 * @param content
	 *            Contenido en forma de cadena de texto.
	 * @return Mensaje AK (<i>acknowgledgement</i>).
	 * @throws MalformedMessageException
	 *             Si el mensaje está mal formado.
	 */
	@Override
	MessageAK buildMessageFromContent(final SequenceNumber sequenceNumber, final String content) throws MalformedMessageException {
		final MessageAK ak = buildEmptyMessage();
		setSequenceNumber(ak, sequenceNumber);
		
		try {
			final Map<FieldMetaData, Field<?>> fields = getParser().parse(content);
			
			/* número de secuencia confirmado, obligatorio */
			final String seqNumber = String.valueOf(fields.get(FieldMetaData.SEQUENCE_NUMBER).getValue());
			ak.setConfirmedSequenceNumber(new SequenceNumber(seqNumber));
		
		} catch (MalformedMessageException e) {
			throw e;
		} catch (Exception e) {
			throw MalformedMessageException.createException(CommErrorType.BUILD_MESSAGE, MessageType.AK, content, e);
		}
		
		return ak;
	}
}
