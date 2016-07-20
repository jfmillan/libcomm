package libcomm.message.regex;

import java.util.regex.Pattern;

import libcomm.message.MessageType;
import libcomm.util.Regex;

import org.junit.Assert;
import org.junit.Test;

import commons.util.StrUtils;

/**
 * Test para probar regex de mensaje válido, {@link Regex#MESSAGE_TYPE}.
 * <p>
 * 19/01/2016 20:00:40
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TRegexValidType extends TRegexTest {

	private static final Pattern pattern = Regex.MESSAGE_TYPE.getCompiledPattern();
	
	@Test
	public void empty_fail() {
		Assert.assertFalse(matches(pattern, StrUtils.EMPTY_STRING));
	}
	
	@Test
	public void check_PR_ok() {
		Assert.assertTrue(matches(pattern, MessageType.PR.printForMessage()));
	}
	
	@Test
	public void check_GT_ok() {
		Assert.assertTrue(matches(pattern, MessageType.GT.printForMessage()));
	}

	@Test
	public void check_ST_ok() {
		Assert.assertTrue(matches(pattern, MessageType.ST.printForMessage()));
	}

	@Test
	public void check_AK_ok() {
		Assert.assertTrue(matches(pattern, MessageType.AK.printForMessage()));
	}

	@Test
	public void check_other_fail() {
		Assert.assertFalse(matches(pattern, "ZZ"));
	}

	@Test
	public void asterisks_fail() {
		Assert.assertFalse(matches(pattern, "**"));
	}

	@Test
	public void lower_case_PR_fail() {
		Assert.assertFalse(matches(pattern, MessageType.PR.printForMessage().toLowerCase()));
	}
	
	@Test
	public void lower_case_GT_fail() {
		Assert.assertFalse(matches(pattern, MessageType.GT.printForMessage().toLowerCase()));
	}
	
	@Test
	public void lower_case_ST_fail() {
		Assert.assertFalse(matches(pattern, MessageType.ST.printForMessage().toLowerCase()));
	}
	
	@Test
	public void lower_case_AK_fail() {
		Assert.assertFalse(matches(pattern, MessageType.AK.printForMessage().toLowerCase()));
	}

	@Test
	public void too_short_fail() {
		Assert.assertFalse(matches(pattern, "A"));
	}


	@Test
	public void too_long_fail() {
		Assert.assertFalse(matches(pattern, "AKAK"));
	}
	
	@Test
	public void double_first_fail() {
		Assert.assertFalse(matches(pattern, "AAK"));
	}

}
