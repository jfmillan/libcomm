package libcomm.message.regex;

import java.util.regex.Pattern;

import libcomm.util.Regex;

import org.junit.Assert;
import org.junit.Test;

import commons.util.StrUtils;

/**
 * Test para probar regex de contenido válido, {@link Regex#VALID_CONTENT}.
 * <p>
 * 19/01/2016 20:00:40
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TRegexPosition extends TRegexTest {

	private static final Pattern pattern = Regex.POSITION.getCompiledPattern();

	@Test
	public void empty_fail() {
		Assert.assertFalse(matches(pattern, StrUtils.EMPTY_STRING));
	}
	
	@Test
	public void numbers_ok() {
		Assert.assertTrue(matches(pattern, "1234"));
	}
	
	@Test
	public void numbers_too_long_fail() {
		Assert.assertFalse(matches(pattern, "12345"));
	}
	
	@Test
	public void numbers_too_short_fail() {
		Assert.assertFalse(matches(pattern, "123"));
	}
	
	@Test
	public void asterisks_fail() {
		Assert.assertFalse(matches(pattern, "****"));
	}
	
	@Test
	public void asterisks_numbers_fail() {
		Assert.assertFalse(matches(pattern, "*456"));
	}
	
	@Test
	public void numbers_asterisks_fail() {
		Assert.assertFalse(matches(pattern, "789*"));
	}
	
	@Test
	public void mixed_numbers_asterisks_fail() {
		Assert.assertFalse(matches(pattern, "1*23"));
	}
	
	@Test
	public void letters_ok() {
		Assert.assertTrue(matches(pattern, "AGDE"));
	}
	
	@Test
	public void letters_too_long_fail() {
		Assert.assertFalse(matches(pattern, "AGDED"));
	}
	
	@Test
	public void letters_too_short_fail() {
		Assert.assertFalse(matches(pattern, "RTY"));
	}
	
	@Test
	public void letters_asterisks_fail() {
		Assert.assertFalse(matches(pattern, "ABC*"));
	}

	@Test
	public void asterisks_letters_fail() {
		Assert.assertFalse(matches(pattern, "*ABC"));
	}

	@Test
	public void not_allowed_letter_fail() {
		Assert.assertFalse(matches(pattern, "ABCÑ"));
	}
	
	@Test
	public void lower_case_letter_fail() {
		Assert.assertFalse(matches(pattern, "ABcD"));
	}
	
	@Test
	public void mixed_numbers_letters_ok() {
		Assert.assertTrue(matches(pattern, "12AB"));
	}
	
	@Test
	public void mixed_letters_numbers_ok() {
		Assert.assertTrue(matches(pattern, "NT36"));
	}
	
	@Test
	public void mixed_first_letter_ok() {
		Assert.assertTrue(matches(pattern, "A47H"));
	}
	
	@Test
	public void mixed_first_number_ok() {
		Assert.assertTrue(matches(pattern, "4R7H"));
	}

	@Test
	public void symbols_fail() {
		Assert.assertFalse(matches(pattern, "12@3"));
	}
}
