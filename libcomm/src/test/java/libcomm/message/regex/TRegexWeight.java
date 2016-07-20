package libcomm.message.regex;

import java.util.regex.Pattern;

import libcomm.util.Regex;

import org.junit.Assert;
import org.junit.Test;

import commons.util.StrUtils;

/**
 * Test para probar regex de peso, {@link Regex#WEIGHT}.
 * <p>
 * 19/01/2016 20:00:40
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TRegexWeight extends TRegexTest {

	private static final Pattern pattern = Regex.WEIGHT.getCompiledPattern();

	@Test
	public void empty_fail() {
		Assert.assertFalse(matches(pattern, StrUtils.EMPTY_STRING));
	}
	
	@Test
	public void numbers_1_5_ok() {
		Assert.assertTrue(matches(pattern, "12345"));
	}
	
	@Test
	public void numbers_6_0_ok() {
		Assert.assertTrue(matches(pattern, "67890"));
	}
	
	@Test
	public void numbers_too_long_fail() {
		Assert.assertFalse(matches(pattern, "123456"));
	}
	
	@Test
	public void numbers_too_short_fail() {
		Assert.assertFalse(matches(pattern, "1234"));
	}
	
	@Test
	public void asterisks_ok() {
		Assert.assertTrue(matches(pattern, "*****"));
	}
	
	@Test
	public void asterisks_too_long_fail() {
		Assert.assertFalse(matches(pattern, "******"));
	}
	
	@Test
	public void asterisks_too_short_fail() {
		Assert.assertFalse(matches(pattern, "****"));
	}
	
	@Test
	public void mixed_numbers_asterisks_ok() {
		Assert.assertTrue(matches(pattern, "6789*"));
	}
	
	@Test
	public void mixed_numbers_asterisks_too_long_fail() {
		Assert.assertFalse(matches(pattern, "123***"));
	}
	
	@Test
	public void mixed_numbers_asterisks_too_short_fail() {
		Assert.assertFalse(matches(pattern, "12**"));
	}
	
	@Test
	public void mixed_asterisks_numbers_fail() {
		Assert.assertFalse(matches(pattern, "**345"));
	}
	
	@Test
	public void mixed_numbers_asterisks_numbers_fail() {
		Assert.assertFalse(matches(pattern, "1**87"));
	}
	
	@Test
	public void mixed_asterisks_numbers_asterisks_fail() {
		Assert.assertFalse(matches(pattern, "*456*"));
	}
	
	@Test
	public void with_letter_fail() {
		Assert.assertFalse(matches(pattern, "1234a"));
	}
	
	@Test
	public void all_letters_fail() {
		Assert.assertFalse(matches(pattern, "defgh"));
	}
	
	@Test
	public void symbols_fail() {
		Assert.assertFalse(matches(pattern, "123@7"));
	}
	
	@Test
	public void group_weight_ok() {
		Assert.assertEquals(group(pattern, "98765", 0), "98765");
	}

	@Test
	public void group_weight_with_asterisks_ok() {
		Assert.assertEquals(group(pattern, "4321*", 1), "4321");
	}

	@Test
	public void group_no_weight_ok() {
		Assert.assertEquals(group(pattern, "*****", 0), "*****");
	}
	
}
