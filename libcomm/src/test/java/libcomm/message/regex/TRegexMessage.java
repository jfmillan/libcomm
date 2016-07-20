package libcomm.message.regex;

import java.util.regex.Pattern;

import libcomm.util.Regex;

import org.junit.Assert;
import org.junit.Test;

import commons.util.StrUtils;

/**
 * Test para probar regex de contenido de mensaje de libcomm genérico,
 * {@link Regex#MESSAGE}. <b>No comprueba que el contenido del mensaje sea
 * correcto, sólo que haya algún contenido</b>.
 * <p>
 * 23/01/2016 00:15:50
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TRegexMessage extends TRegexTest {

	
	private static final Pattern pattern = Regex.MESSAGE.getCompiledPattern();

	@Test
	public void correct_message_minimum_ok() {
		Assert.assertTrue(matches(pattern, "0000AK1"));
	}

	@Test
	public void correct_message_normal_ok() {
		Assert.assertTrue(matches(pattern, "FFFFAK1234"));
	}
	
	@Test
	public void empty_sequence_ok() {
		Assert.assertTrue(matches(pattern, "****AK34F2"));
	}

	@Test
	public void correct_message_long_ok() {
		Assert.assertTrue(matches(pattern, "ABCDPR143142333413****43124124*32341344***AEBADFAE"));
	}

	@Test
	public void correct_message_ak_ok() {
		Assert.assertTrue(matches(pattern, "F001AK3299"));
	}

	@Test
	public void correct_message_pr_ok() {
		Assert.assertTrue(matches(pattern, "D211PR1234567890**PO129876*Y"));
	}
	
	@Test
	public void correct_message_gt_ok() {
		Assert.assertTrue(matches(pattern, "AB32GT1234567890**PO12"));
	}
	
	@Test
	public void correct_message_st_ok() {
		Assert.assertTrue(matches(pattern, "15BFSTPO45N"));
	}

	@Test
	public void incorrect_sequence_number_fail() {
		Assert.assertFalse(matches(pattern, "00AKAK1"));
	}

	@Test
	public void incorrect_sequence_number_too_short_fail() {
		Assert.assertFalse(matches(pattern, "002AK1"));
	}

	
	@Test
	public void incorrect_message_normal_fail() {
		Assert.assertFalse(matches(pattern, "FFFFZZ1234"));
	}
	
	@Test
	public void incorrect_empty_sequence_fail() {
		Assert.assertFalse(matches(pattern, "3**3AK34F2"));
	}

	@Test
	public void incorrect_message_long_fail() {
		Assert.assertFalse(matches(pattern, "ABCDPR143142333413****43124124*32341344***AEñBADFAE"));
	}

	@Test
	public void incorrect_message_ak_fail() {
		Assert.assertFalse(matches(pattern, "F001AK32_9"));
	}

	@Test
	public void incorrect_message_pr_fail() {
		Assert.assertFalse(matches(pattern, "D211PR__34567890**PO129876*Y"));
	}
	
	@Test
	public void incorrect_message_gt_fail() {
		Assert.assertFalse(matches(pattern, "AB32GT12345#7890**PO12"));
	}
	
	@Test
	public void incorrect_message_st_fail() {
		Assert.assertFalse(matches(pattern, "15BFstPO45N"));
	}
	
	@Test
	public void incorrect_type_fail() {
		Assert.assertFalse(matches(pattern, "15BFOOPO45N"));
	}
	
	@Test
	public void incorrect_empty_content_fail() {
		Assert.assertFalse(matches(pattern, "D211PR"));
	}
	
	@Test
	public void empty_fail() {
		Assert.assertFalse(matches(pattern, StrUtils.EMPTY_STRING));
	}
	
	@Test
	public void lower_case_fail() {
		Assert.assertFalse(matches(pattern, "D211PR1234567890**po129876*y"));
	}

	@Test
	public void all_asterisks_fail() {
		Assert.assertFalse(matches(pattern, "**********"));
	}

	@Test
	public void all_asterisk_except_type_ok() {
		Assert.assertTrue(matches(pattern, "****GT*********"));
	}

	@Test
	public void group_sequence_number_ok() {
		Assert.assertEquals(group(pattern, "1234ST00P1N", 1), "1234");
	}

	@Test
	public void group_message_type_ok() {
		Assert.assertEquals(group(pattern, "1234ST00P1N", 2), "ST");
	}
	
	@Test
	public void group_content_ok() {
		Assert.assertEquals(group(pattern, "1234ST00P1N", 3), "00P1N");
	}
}
