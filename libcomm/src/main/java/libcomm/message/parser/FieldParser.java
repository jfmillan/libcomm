package libcomm.message.parser;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import libcomm.exception.CommErrorType;
import libcomm.exception.MalformedMessageException;
import libcomm.message.Field;
import libcomm.message.FieldMetaData;
import libcomm.util.MessageUtils;
import libcomm.util.Regex;

import org.apache.commons.lang3.StringUtils;

import commons.util.Constants;
import commons.util.TypeConverter;

/**
 * Parseo genérico para un campo de un mensaje.
 * 
 * <p>
 * 24/01/2016 02:01:11
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class FieldParser {

	/**
	 * Todos los campos de mensaje cumplen el mismo patrón a la hora de ser
	 * analizados: o bien ocupan todo el texto o bien son acompañados del
	 * caracter de relleno <code>*</code>.
	 * <p>
	 * <code>T</code> se corresponde con el tipo de dato devuelto.
	 * 
	 * @param metaData
	 *            Metadatos del tipo.
	 * @param toParse
	 *            Cadena de texto a parsear.
	 * @return Dato analizado o parseado.
	 * @throws MalformedMessageException en caso de error.
	 */
	public static <T> Field<T> parse(final FieldMetaData metaData, final String toParse) throws MalformedMessageException {
		Objects.requireNonNull(metaData);
		Objects.requireNonNull(toParse);
		
		final Regex regex = metaData.getRegex();
		Objects.requireNonNull(regex);
		
		try {
			/* Si el número de grupos capturados es cero o si no hay caracter de relleno, capturamos el grupo 0 */
			final Pattern pattern = regex.getCompiledPattern();
			final Matcher matcher = pattern.matcher(toParse);
			
			if (matcher.matches()) {
				final int groupCount = matcher.groupCount();
				final boolean noPad = StringUtils.containsNone(toParse, MessageUtils.MESSAGE_PAD);

				String parsedResult = (Constants.ZERO.equals(groupCount) || noPad) 
					? matcher.group(Constants.ZERO)
					: matcher.group(1);
					
				if (StringUtils.containsOnly(parsedResult, MessageUtils.MESSAGE_PAD)) {
					parsedResult = null; /* Si solo hay relleno se considera que el dato no existe */
				}
					
				final T converted = TypeConverter.from(parsedResult, metaData.getType());
				return Field.create(metaData, converted);
			}
		} catch (final Exception e) {
			throw MalformedMessageException.createException (CommErrorType.PARSE_MESSAGE_FIELD, metaData, toParse, e);
		}
		throw MalformedMessageException.createException (CommErrorType.PARSE_MESSAGE_FIELD, metaData, toParse, null);
	}
}
