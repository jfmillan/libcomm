package libcomm.message.regex;

import java.util.regex.Pattern;

import libcomm.util.Regex;

import org.junit.Assert;
import org.junit.Test;

import commons.util.StrUtils;

/**
 * Test para probar regex de identificador de bulto, {@link Regex#PACKAGE_ID}.
 * <p>
 * 19/01/2016 20:00:40
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TRegexPackageId extends TRegexTest {

	private static final Pattern pattern = Regex.PACKAGE_ID.getCompiledPattern();

	@Test
	public void empty_fail() {
		Assert.assertFalse(matches(pattern, StrUtils.EMPTY_STRING));
	}
	
	@Test
	public void numbers_ok() {
		Assert.assertTrue(matches(pattern, "123456789012"));
	}
	
	@Test
	public void numbers_too_long_fail() {
		Assert.assertFalse(matches(pattern, "1234567890123"));
	}
	
	@Test
	public void numbers_too_short_fail() {
		Assert.assertFalse(matches(pattern, "12345678901"));
	}
	
	@Test
	public void asterisks_ok() {
		Assert.assertTrue(matches(pattern, "************"));
	}
	
	@Test
	public void asterisks_too_long_fail() {
		Assert.assertFalse(matches(pattern, "*************"));
	}
	
	@Test
	public void asterisks_too_short_fail() {
		Assert.assertFalse(matches(pattern, "***********"));
	}
	
	@Test
	public void mixed_numbers_asterisks_ok() {
		Assert.assertTrue(matches(pattern, "1234567890**"));
	}
	
	@Test
	public void mixed_numbers_asterisks_too_long_fail() {
		Assert.assertFalse(matches(pattern, "1234567890***"));
	}
	
	@Test
	public void mixed_numbers_asterisks_too_short_fail() {
		Assert.assertFalse(matches(pattern, "12345678***"));
	}
	
	@Test
	public void mixed_asterisks_numbers_fail() {
		Assert.assertFalse(matches(pattern, "**3456789012"));
	}
	
	@Test
	public void mixed_numbers_asterisks_numbers_fail() {
		Assert.assertFalse(matches(pattern, "1234***89012"));
	}
	
	@Test
	public void mixed_asterisks_numbers_asterisks_fail() {
		Assert.assertFalse(matches(pattern, "***456789***"));
	}
	
	@Test
	public void with_letter_fail() {
		Assert.assertFalse(matches(pattern, "123456C89012"));
	}
	
	@Test
	public void all_letters_fail() {
		Assert.assertFalse(matches(pattern, "ABCDEFGHIJKL"));
	}
	
	@Test
	public void symbols_fail() {
		Assert.assertFalse(matches(pattern, "12345@789012"));
	}
	
	@Test
	public void group_package_id_ok() {
		Assert.assertEquals(group(pattern, "123456789012", 0), "123456789012");
	}

	@Test
	public void group_package_id_with_asterisks_ok() {
		Assert.assertEquals(group(pattern, "12345678****", 1), "12345678");
	}

	@Test
	public void group_no_reading_ok() {
		Assert.assertEquals(group(pattern, "************", 0), "************");
	}
}
