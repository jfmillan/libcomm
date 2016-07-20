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
 * Parser de mensaje GT:
 * <ul>
 * <li>FieldMetaData#PACKAGE_ID, identificador de bulto.
 * <li>FieldMetaData#POSITION, posición.
 * </ul>
 * <p>
 * 24/01/2016 17:18:38
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class ParserGT implements IMessageParser {

	/**
	 * Parser de mensaje GT:
	 * <ul>
	 * <li>FieldMetaData#PACKAGE_ID, identificador de bulto.
	 * <li>FieldMetaData#POSITION, posición.
	 * </ul>
	 */
	@Override
	public Map<FieldMetaData, Field<?>> parse(final String toParse) throws MalformedMessageException {
		try {
			final Pattern pattern = Regex.MESSAGE_CONTENT_GT.getCompiledPattern();
			final Matcher matcher = pattern.matcher(toParse);
			if (!matcher.matches()) {
				throw MalformedMessageException.createException(CommErrorType.PARSE_MESSAGE, MessageType.GT, toParse, null);
			}

			final Map<FieldMetaData, Field<?>> result = new HashMap<FieldMetaData, Field<?>>();
			
			/* Identificador de bulto */
			final Field<Long> packageId = FieldParser.parse(FieldMetaData.PACKAGE_ID, matcher.group(1));
			result.put(FieldMetaData.PACKAGE_ID, packageId);
			
			/* Posición */
			final Field<String> position = FieldParser.parse(FieldMetaData.POSITION, matcher.group(2));
			result.put(FieldMetaData.POSITION, position);
			
			return result;
		} catch (MalformedMessageException e) {
			throw e;
		} catch (Exception e) {
			throw MalformedMessageException.createException(CommErrorType.PARSE_MESSAGE, MessageType.GT, toParse, e);
		}
	}
}
