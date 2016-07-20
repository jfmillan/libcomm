package libcomm.message.regex;

import java.util.regex.Pattern;

import libcomm.message.FieldFlag;
import libcomm.util.MessageUtils;
import libcomm.util.Regex;

import org.junit.Assert;
import org.junit.Test;

import commons.util.StrUtils;

/**
 * Test para probar regex de requerimiento de respuesta, {@link Regex#REQUIRED_REPLY}.
 * <p>
 * 19/01/2016 20:00:40
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TRegexRequiredReply extends TRegexTest {

	private static final Pattern pattern = Regex.REQUIRED_REPLY.getCompiledPattern();
	
	@Test
	public void empty_fail() {
		Assert.assertFalse(matches(pattern, StrUtils.EMPTY_STRING));
	}
	
	@Test
	public void check_Y_ok() {
		Assert.assertTrue(matches(pattern, FieldFlag.YES_FLAG));
	}
	
	@Test
	public void check_N_ok() {
		Assert.assertTrue(matches(pattern, FieldFlag.NOT_FLAG));
	}

	@Test
	public void check_asterisk_fail() {
		Assert.assertFalse(matches(pattern, MessageUtils.MESSAGE_PAD));
	}

	@Test
	public void check_too_long_Y_fail() {
		Assert.assertFalse(matches(pattern, "YY"));
	}

	@Test
	public void check_too_long_N_fail() {
		Assert.assertFalse(matches(pattern, "NN"));
	}

	@Test
	public void lower_case_y_fail() {
		Assert.assertFalse(matches(pattern, "y"));
	}

	@Test
	public void lower_case_n_fail() {
		Assert.assertFalse(matches(pattern, "n"));
	}

	@Test
	public void sign_fail() {
		Assert.assertFalse(matches(pattern, "@"));
	}

	@Test
	public void number_fail() {
		Assert.assertFalse(matches(pattern, "1"));
	}
	
	@Test
	public void bad_letter_fail() {
		Assert.assertFalse(matches(pattern, "I"));
	}
}
