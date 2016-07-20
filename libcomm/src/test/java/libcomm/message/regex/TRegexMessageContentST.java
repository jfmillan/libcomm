package libcomm.message.regex;

import java.util.regex.Pattern;

import libcomm.util.Regex;

import org.junit.Assert;
import org.junit.Test;

import commons.util.StrUtils;

/**
 * Test para probar regex de contenido de mensaje ST
 * {@link Regex#MESSAGE_CONTENT_ST}.
 * <p>
 * 23/01/2016 00:15:50
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TRegexMessageContentST extends TRegexTest {
	
	private static final Pattern pattern = Regex.MESSAGE_CONTENT_ST.getCompiledPattern();

	@Test
	public void empty_fail() {
		Assert.assertFalse(matches(pattern, StrUtils.EMPTY_STRING));
	}
	
	@Test
	public void double_content_fail() {
		Assert.assertFalse(matches(pattern, "1234Y1234Y"));
	}
	
	@Test
	public void correct_enabled_numbers_ok() {
		Assert.assertTrue(matches(pattern, "1234Y"));
	}

	@Test
	public void correct_disabled_numbers_ok() {
		Assert.assertTrue(matches(pattern, "5678N"));
	}

	@Test
	public void correct_enabled_letters_ok() {
		Assert.assertTrue(matches(pattern, "ABCDY"));
	}

	@Test
	public void correct_disabled_letters_ok() {
		Assert.assertTrue(matches(pattern, "EFGHN"));
	}
	
	@Test
	public void bad_enabled_lower_case_fail() {
		Assert.assertFalse(matches(pattern, "1234y"));
	}

	@Test
	public void bad_disabled_lower_case_fail() {
		Assert.assertFalse(matches(pattern, "5678n"));
	}
	
	@Test
	public void bad_state_fail() {
		Assert.assertFalse(matches(pattern, "1234P"));
	}

	@Test
	public void not_state_fail() {
		Assert.assertFalse(matches(pattern, "1234"));
	}
	
	@Test
	public void yy_ok() {
		Assert.assertTrue(matches(pattern, "ABCYY"));
	}

	@Test
	public void yy_fail() {
		Assert.assertFalse(matches(pattern, "ABYYP"));
	}
	
	@Test
	public void nn_ok() {
		Assert.assertTrue(matches(pattern, "34ABN"));
	}

	@Test
	public void nn_fail() {
		Assert.assertFalse(matches(pattern, "3BNNP"));
	}
	
	@Test
	public void all_asterisk_position_fail() {
		Assert.assertFalse(matches(pattern, "****Y"));
	}

	@Test
	public void some_asterisk_position_fail() {
		Assert.assertFalse(matches(pattern, "BG**Y"));
	}

	@Test
	public void position_too_long_fail() {
		Assert.assertFalse(matches(pattern, "AB001Y"));
	}

	@Test
	public void position_too_long_ends_yy_fail() {
		Assert.assertFalse(matches(pattern, "AB00YY"));
	}

	@Test
	public void position_too_short_fail() {
		Assert.assertFalse(matches(pattern, "DE0Y"));
	}

	@Test
	public void position_too_short_ends_yy_fail() {
		Assert.assertFalse(matches(pattern, "DEYY"));
	}
	
	@Test
	public void not_allowed_letter_fail() {
		Assert.assertFalse(matches(pattern, "567ÑN"));
	}

	@Test
	public void not_allowed_letter_sign_fail() {
		Assert.assertFalse(matches(pattern, "567_N"));
	}

	@Test
	public void not_allowed_lower_case_letter_fail() {
		Assert.assertFalse(matches(pattern, "ABcDY"));
	}
	
	@Test
	public void symbols_fail() {
		Assert.assertFalse(matches(pattern, "AB$DY"));
	}

	@Test
	public void all_asterisks_fail() {
		Assert.assertFalse(matches(pattern, "*****"));
	}

	@Test
	public void state_number_fail() {
		Assert.assertFalse(matches(pattern, "AB0E1"));
	}
	
	@Test
	public void state_number_symbol_fail() {
		Assert.assertFalse(matches(pattern, "AB0E@"));
	}
	
	@Test
	public void state_number_asterisk_fail() {
		Assert.assertFalse(matches(pattern, "AB0E*"));
	}
	
	@Test
	public void group_position_ok() {
		Assert.assertEquals(group(pattern, "AB01Y", 1), "AB01");
	}

	@Test
	public void group_flag_ok() {
		Assert.assertEquals(group(pattern, "AB01Y", 2), "Y");
	}
}
