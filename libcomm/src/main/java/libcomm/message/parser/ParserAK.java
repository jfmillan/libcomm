package libcomm.message.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import libcomm.exception.CommErrorType;
import libcomm.exception.MalformedMessageException;
import libcomm.message.Field;
import libcomm.message.FieldMetaData;
import libcomm.message.MessageType;
import libcomm.util.Regex;

/**
 * Parseo de los campos de un mensaje AK.
 * <ul>
 * <li>FieldMetaData#SEQUENCE_NUMBER, número de secuencia que se confirma.
 * </ul>
 * 
 * <p>
 * 24/01/2016 16:07:34
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class ParserAK implements IMessageParser {

	/**
	 * Parser de mensaje AK:
	 * <ul>
	 * <li>FieldMetaData#SEQUENCE_NUMBER, número de secuencia que se confirma.
	 * </ul>
	 */
	@Override
	public Map<FieldMetaData, Field<?>> parse(final String toParse) throws MalformedMessageException {
		try {
			final Pattern pattern = Regex.MESSAGE_CONTENT_AK.getCompiledPattern();
			final Matcher matcher = pattern.matcher(toParse);
			if (!matcher.matches()) {
				throw MalformedMessageException.createException(CommErrorType.PARSE_MESSAGE, MessageType.AK, toParse, null);
			}

			final Map<FieldMetaData, Field<?>> result = new HashMap<FieldMetaData, Field<?>>();

			/* número de secuencia confirmado */
			final Field<String> confirmedSequenceNumber = FieldParser.parse(FieldMetaData.SEQUENCE_NUMBER, matcher.group());
			result.put(FieldMetaData.SEQUENCE_NUMBER, confirmedSequenceNumber);
	
			return result;
		} catch (MalformedMessageException e) {
			throw e;
		} catch (Exception e) {
			throw MalformedMessageException.createException(CommErrorType.PARSE_MESSAGE, MessageType.AK, toParse, e);
		}
	}
}
