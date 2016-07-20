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
public class TRegexValidContent extends TRegexTest {

	private static final Pattern pattern = Regex.VALID_CONTENT.getCompiledPattern();

	@Test
	public void empty_ok() {
		Assert.assertTrue(matches(pattern, StrUtils.EMPTY_STRING));
	}

	@Test
	public void numbers_ok() {
		Assert.assertTrue(matches(pattern, "0123456789"));
	}

	@Test
	public void letters_ok() {
		Assert.assertTrue(matches(pattern, "ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
	}

	@Test
	public void asterisks_ok() {
		Assert.assertTrue(matches(pattern, "********"));
	}

	@Test
	public void mixed_ok() {
		Assert.assertTrue(matches(pattern, "ABC32340**32422ASABAS3****DAF****"));
	}
	
	@Test
	public void bad_letters_lower_case_fail() {
		Assert.assertFalse(matches(pattern, "ABCdE"));
	}
	
	@Test
	public void bad_letters_not_allowed_fail() {
		Assert.assertFalse(matches(pattern, "ABCÑE"));
	}
	
	@Test
	public void symbols_fail() {
		Assert.assertFalse(matches(pattern, "A$@#E:F"));
	}
}
