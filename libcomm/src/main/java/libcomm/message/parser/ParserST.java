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
 * Parser de mensaje ST:
 * <ul>
 * <li>FieldMetaData#POSITION, posición.
 * <li>FieldMetaData#ENABLED_POSITION, marca de posición habilitada.
 * </ul>
 * <p>
 * 24/01/2016 17:19:39
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class ParserST implements IMessageParser {

	/**
	 * Parser de mensaje ST:
	 * <ul>
	 * <li>FieldMetaData#POSITION, posición.
	 * <li>FieldMetaData#ENABLED_POSITION, marca de posición habilitada.
	 * </ul>
	 */
	@Override
	public Map<FieldMetaData, Field<?>> parse(final String toParse) throws MalformedMessageException {
		try {
			final Pattern pattern = Regex.MESSAGE_CONTENT_ST.getCompiledPattern();
			final Matcher matcher = pattern.matcher(toParse);
			if (!matcher.matches()) {
				throw MalformedMessageException.createException(CommErrorType.PARSE_MESSAGE, MessageType.ST, toParse, null);
			}

			final Map<FieldMetaData, Field<?>> result = new HashMap<FieldMetaData, Field<?>>();
			
			/* Posición */
			final Field<String> position = FieldParser.parse(FieldMetaData.POSITION, matcher.group(1));
			result.put(FieldMetaData.POSITION, position);
			
			/* Marca de posición habilitada. */
			final Field<Character> enabledPosition = FieldParser.parse(FieldMetaData.ENABLED_POSITION, matcher.group(2));
			result.put(FieldMetaData.ENABLED_POSITION, enabledPosition);
	
			return result;
		} catch (MalformedMessageException e) {
			throw e;
		} catch (Exception e) {
			throw MalformedMessageException.createException(CommErrorType.PARSE_MESSAGE, MessageType.ST, toParse, e);
		}
	}
}
