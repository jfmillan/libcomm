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
 * Parser de mensaje PR:
 * <ul>
 * <li>FieldMetaData#PACKAGE_ID, identificador de bulto.
 * <li>FieldMetaData#POSITION, posición.
 * <li>FieldMetaData#WEIGHT, peso en gramos.
 * <li>FieldMetaData#REQUIRED_REPLY, indica si necesita respuesta.
 * </ul>
 * <p>
 * 24/01/2016 17:19:23
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class ParserPR implements IMessageParser {

	/**
	 * Parser de mensaje PR:
	 * <ul>
	 * <li>FieldMetaData#PACKAGE_ID, identificador de bulto.
	 * <li>FieldMetaData#POSITION, posición.
	 * <li>FieldMetaData#WEIGHT, peso en gramos.
	 * <li>FieldMetaData#REQUIRED_REPLY, indica si necesita respuesta.
	 * </ul>
	 */
	@Override
	public Map<FieldMetaData, Field<?>> parse(final String toParse) throws MalformedMessageException {
		try {
			final Pattern pattern = Regex.MESSAGE_CONTENT_PR.getCompiledPattern();
			final Matcher matcher = pattern.matcher(toParse);
			if (!matcher.matches()) {
				throw MalformedMessageException.createException(CommErrorType.PARSE_MESSAGE, MessageType.PR, toParse, null);
			}

			final Map<FieldMetaData, Field<?>> result = new HashMap<FieldMetaData, Field<?>>();
			
			/* Identificador de bulto */
			final Field<Long> packageId = FieldParser.parse(FieldMetaData.PACKAGE_ID, matcher.group(1));
			result.put(FieldMetaData.PACKAGE_ID, packageId);
			
			/* Posición */
			final Field<String> position = FieldParser.parse(FieldMetaData.POSITION, matcher.group(2));
			result.put(FieldMetaData.POSITION, position);
			
			/* Peso */
			final Field<Integer> weight = FieldParser.parse(FieldMetaData.WEIGHT, matcher.group(3));
			result.put(FieldMetaData.WEIGHT, weight);
			
			/* Indicador de si requiere respuesta */
			final Field<Character> requiredReply = FieldParser.parse(FieldMetaData.REQUIRED_REPLY, matcher.group(4));
			result.put(FieldMetaData.REQUIRED_REPLY, requiredReply);
			
			return result;
		} catch (MalformedMessageException e) {
			throw e;
		} catch (Exception e) {
			throw MalformedMessageException.createException(CommErrorType.PARSE_MESSAGE, MessageType.PR, toParse, e);
		}
	}

}
