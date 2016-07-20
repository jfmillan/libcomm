package libcomm.message.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Super clase para tests de regex.
 * <p>
 * 19/01/2016 20:22:11
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class TRegexTest {
	
	/* Comprueba si una cadena de texto cumple un patrón ya compilado. */
	protected boolean matches (final Pattern pattern, final String regexToValidate) {
		final Matcher matcher = pattern.matcher(regexToValidate);
		return matcher.matches();
	}
	
	/* Comprueba si un objeto cumple un patrón ya compilado. */
	protected boolean matches (final Pattern pattern, final Object regexToValidate) {
		return regexToValidate != null ? matches(pattern, regexToValidate.toString()) : false;
	}
	
	/* Devuelve un determinado grupo tras comprobar una expresión regular en un patrón ya compilado. */
	protected String group(final Pattern pattern, final String regexToValidate, final int groupNumber) {
		final Matcher matcher = pattern.matcher(regexToValidate);
		matcher.matches();
		return matcher.group(groupNumber);
	}
}
