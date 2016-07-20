package libcomm.message.regex;

import java.util.regex.Pattern;

import libcomm.util.Regex;

import org.junit.Assert;
import org.junit.Test;

import commons.util.StrUtils;

/**
 * Test para probar regex de contenido de mensaje AK,
 * {@link Regex#MESSAGE_CONTENT_AK}.
 * <p>
 * 21/01/2016 22:14:43
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TRegexMessageContentAK extends TRegexTest {

	
	private static final Pattern pattern = Regex.MESSAGE_CONTENT_AK.getCompiledPattern();

	@Test
	public void empty_fail() {
		Assert.assertFalse(matches(pattern, StrUtils.EMPTY_STRING));
	}
	
	@Test
	public void only_numbers_ok() {
		Assert.assertTrue(matches(pattern, "0123"));
	}

	
	@Test
	public void too_long_fail() {
		Assert.assertFalse(matches(pattern, "3423F"));
	}
	
	@Test
	public void too_short_fail() {
		Assert.assertFalse(matches(pattern, "012"));
	}

	@Test
	public void only_letters_ok() {
		Assert.assertTrue(matches(pattern, "AABC"));
	}

	@Test
	public void mix_numbers_letters_ok() {
		Assert.assertTrue(matches(pattern, "45ED"));
	}

	@Test
	public void mix_letters_numbers_ok() {
		Assert.assertTrue(matches(pattern, "AF67"));
	}
	
	@Test
	public void mix_ok() {
		Assert.assertTrue(matches(pattern, "8A9F"));
	}

	@Test
	public void bad_letters_fail() {
		Assert.assertFalse(matches(pattern, "ABCG"));
	}

	@Test
	public void lower_case_letters_fail() {
		Assert.assertFalse(matches(pattern, "ABCf"));
	}
	
	@Test
	public void asterisks_right_fail() {
		Assert.assertFalse(matches(pattern, "AB**"));
	}

	@Test
	public void asterisks_left_fail() {
		Assert.assertFalse(matches(pattern, "**12"));
	}

	@Test
	public void asterisks_middle_fail() {
		Assert.assertFalse(matches(pattern, "AB*3"));
	}

	@Test
	public void all_asterisks_ok() {
		Assert.assertTrue(matches(pattern, "****"));
	}
	
	@Test
	public void all_asterisks_too_long_fail() {
		Assert.assertFalse(matches(pattern, "*****"));
	}


	@Test
	public void all_asterisks_too_short_fail() {
		Assert.assertFalse(matches(pattern, "***"));
	}


	@Test
	public void min_value_ok() {
		Assert.assertTrue(matches(pattern, "0000"));
	}
	

	@Test
	public void max_value_ok() {
		Assert.assertTrue(matches(pattern, "FFFF"));
	}
	
	@Test
	public void group_sequence_number_ok() {
		Assert.assertEquals(group(pattern, "00F1", 0), "00F1");
	}

	@Test
	public void group_no_sequence_number_ok() {
		Assert.assertEquals(group(pattern, "****", 0), "****");
	}
}
