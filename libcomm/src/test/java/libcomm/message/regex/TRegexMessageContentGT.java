package libcomm.message.regex;

import java.util.regex.Pattern;

import libcomm.util.Regex;

import org.junit.Assert;
import org.junit.Test;

import commons.util.StrUtils;

/**
 * Test para probar regex de contenido de mensaje GT
 * {@link Regex#MESSAGE_CONTENT_GT}.
 * <p>
 * 23/01/2016 18:10:27
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TRegexMessageContentGT extends TRegexTest {
	
	private static final Pattern pattern = Regex.MESSAGE_CONTENT_GT.getCompiledPattern();

	@Test
	public void empty_fail() {
		Assert.assertFalse(matches(pattern, StrUtils.EMPTY_STRING));
	}
	
	@Test
	public void double_content_fail() {
		Assert.assertFalse(matches(pattern, "1234567890**PS011234567890**PS01"));
	}
	
	@Test
	public void message_all_id_ok() {
		Assert.assertTrue(matches(pattern, "123456789012PS01"));
	}
	
	@Test
	public void message_partial_id_ok() {
		Assert.assertTrue(matches(pattern, "12345678****PS01"));
	}
	
	@Test
	public void message_no_reading_id_ok () {
		Assert.assertTrue(matches(pattern, "************PS01"));
	}
	
	@Test
	public void left_asterisks_id_fail() {
		Assert.assertFalse(matches(pattern, "***456789012PS01"));
	}
	
	@Test
	public void middle_asterisks_id_fail() {
		Assert.assertFalse(matches(pattern, "123****89012PS01"));
	}
	
	@Test
	public void too_short_id_fail() {
		Assert.assertFalse(matches(pattern, "12345678901PS01"));		
	}
	
	@Test
	public void too_long_id_fail() {
		Assert.assertFalse(matches(pattern, "123456789012*PS01"));
	}
	
	@Test
	public void too_short_position_fail() {
		Assert.assertFalse(matches(pattern, "123456789012PS1"));
	}
	
	@Test
	public void too_long_position_fail() {
		Assert.assertFalse(matches(pattern, "123456789012PS001"));
	}
	
	@Test
	public void left_asterisk_position_fail() {
		Assert.assertFalse(matches(pattern, "123456789012*P01"));
	}
	
	@Test
	public void right_asterisk_position_fail() {
		Assert.assertFalse(matches(pattern, "123456789012PS0*"));
	}
	
	@Test
	public void middle_asterisk_position_fail() {
		Assert.assertFalse(matches(pattern, "123456789012PS*1"));
	}
	
	@Test
	public void all_asterisks_position_fail() {
		Assert.assertFalse(matches(pattern, "123456789012****"));
	}
	
	@Test
	public void too_short_no_reading_fail() {
		Assert.assertFalse(matches(pattern, "***********PS01"));
	}
	
	@Test
	public void too_long_no_reading_fail() {
		Assert.assertFalse(matches(pattern, "*************PS01"));
	}
	
	@Test
	public void letters_in_id_fail() {
		Assert.assertFalse(matches(pattern, "123AB6789012PS01"));
	}
	
	@Test
	public void symbols_in_id_fail() {
		Assert.assertFalse(matches(pattern, "1234$6789012PS01"));
	}
	
	@Test
	public void symbols_in_position_fail() {
		Assert.assertFalse(matches(pattern, "123456789012PS_1"));
	}
	
	@Test
	public void lower_case_letters_position_fail() {
		Assert.assertFalse(matches(pattern, "123456789012Ps01"));
	}
	
	@Test
	public void bad_letter_position_fail() {
		Assert.assertFalse(matches(pattern, "123456789012PÑ01"));
	}
	
	@Test
	public void group_get_id_ok() {
		Assert.assertEquals(group(pattern, "123456789012PS01", 1), "123456789012");		
	}
	
	@Test
	public void group_get_id_with_asterisks_ok() {
		Assert.assertEquals(group(pattern, "12345*******PS01", 1), "12345*******");		
	}
	
	@Test
	public void group_get_no_reading_id_ok() {
		Assert.assertEquals(group(pattern, "************PS01", 1), "************");
	}
	
	@Test
	public void group_position_ok() {
		Assert.assertEquals(group(pattern, "123456789012WG56", 2), "WG56");
	}
	
	@Test
	public void group_position_with_asterisks_in_id_ok() {
		Assert.assertEquals(group(pattern, "12345678****WG56", 2), "WG56");
	}
	
	@Test
	public void group_position_with_no_reading_ok() {
		Assert.assertEquals(group(pattern, "************WG56", 2), "WG56");
	}
}
