package libcomm.message.regex;

import java.util.regex.Pattern;

import libcomm.util.Regex;

import org.junit.Assert;
import org.junit.Test;

import commons.util.StrUtils;

/**
 * Test para probar regex de contenido de mensaje PR
 * {@link Regex#MESSAGE_CONTENT_PR}.
 * <p>
 * 23/01/2016 19:51:01
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TRegexMessageContentPR extends TRegexTest {
	
	private static final Pattern pattern = Regex.MESSAGE_CONTENT_PR.getCompiledPattern();

	@Test
	public void empty_fail() {
		Assert.assertFalse(matches(pattern, StrUtils.EMPTY_STRING));
	}
	
	@Test
	public void double_content_fail() {
		Assert.assertFalse(matches(pattern, ""));
	}

	@Test
	public void correct_message_ok() {
		Assert.assertTrue(matches(pattern, "123456789012BY01*****Y"));
	}

	@Test
	public void correct_message_asterisks_id_ok() {
		Assert.assertTrue(matches(pattern, "00234232****BY01*****Y"));
	}

	@Test
	public void left_asterisks_id_fail() {
		Assert.assertFalse(matches(pattern, "****42320123BY01*****Y"));
	}
	
	@Test
	public void middle_asterisks_id_fail() {
		Assert.assertFalse(matches(pattern, "00234**20104BY01*****Y"));			
	}
	
	@Test
	public void no_reading_no_weight_ok() {
		Assert.assertTrue(matches(pattern, "************BY01*****Y"));
	}
	
	@Test
	public void too_short_id_fail() {
		Assert.assertFalse(matches(pattern, "00234232***BY01*****Y"));
	}
	
	@Test
	public void too_long_id_fail() {
		Assert.assertFalse(matches(pattern, "00234232*****BY01*****Y"));
	}
	
	@Test
	public void too_short_position_fail() {
		Assert.assertFalse(matches(pattern, "00234232****BY0*****Y"));
	}
	
	@Test
	public void too_long_position_fail() {
		Assert.assertFalse(matches(pattern, "00234232****BY011*****Y"));
	}
	
	@Test
	public void left_asterisk_position_fail() {
		Assert.assertFalse(matches(pattern, "00234232*****Y01*****Y"));
	}
	
	@Test
	public void right_asterisk_position_fail() {
		Assert.assertFalse(matches(pattern, "00234232****BY0******Y"));
	}
	
	@Test
	public void middle_asterisk_position_fail() {
		Assert.assertFalse(matches(pattern, "123456789012BY*1*****Y"));
	}
	
	@Test
	public void asterisks_position_fail() {
		Assert.assertFalse(matches(pattern, "123456789012*********Y"));
	}
	
	@Test
	public void too_short_no_reading_fail() {
		Assert.assertFalse(matches(pattern, "***********BY01*****Y"));
	}
	
	@Test
	public void too_long_no_reading_fail() {
		Assert.assertFalse(matches(pattern, "*************BY01*****Y"));
	}
	
	@Test
	public void no_reading_left_asterisk_position_fail() {
		Assert.assertFalse(matches(pattern, "*************Y01*****Y"));
	}
	
	@Test
	public void letters_in_id_fail() {
		Assert.assertFalse(matches(pattern, "12AB56789012BY01*****Y"));
	}
	
	@Test
	public void symbols_in_id_fail() {
		Assert.assertFalse(matches(pattern, "1234_6789012BY01*****Y"));
	}
	
	@Test
	public void symbols_in_position_fail() {
		Assert.assertFalse(matches(pattern, "123456789012BY_1*****Y"));
	}
	
	@Test
	public void lower_case_in_position_fail() {
		Assert.assertFalse(matches(pattern, "123456789012By01*****Y"));
	}
	
	@Test
	public void bad_letter_in_position_fail() {
		Assert.assertFalse(matches(pattern, "123456789012BYÑ1*****Y"));
	}
	
	@Test
	public void message_with_weight_ok() {
		Assert.assertTrue(matches(pattern, "123456789012WB9399048Y"));
	}
	
	@Test 
	public void asterisks_in_id_with_weight_ok() {
		Assert.assertTrue(matches(pattern, "123456789***WB9399048Y"));
	}
	
	@Test
	public void no_reading_weight_ok() {
		Assert.assertTrue(matches(pattern, "************WB9399048Y"));
	}
	
	@Test
	public void right_asterisks_weight_ok() {
		Assert.assertTrue(matches(pattern, "123456789012WB93990**Y"));
	}
	
	@Test
	public void asterisks_in_id_and_weight_ok() {
		Assert.assertTrue(matches(pattern, "123456******WB93990**Y"));
	}
	
	@Test
	public void no_reading_asterisks_in_weight_ok() {
		Assert.assertTrue(matches(pattern, "************WB93990**Y"));
	}
	
	@Test
	public void no_position_fail() {
		Assert.assertFalse(matches(pattern, "123456789012****99048Y"));
	}
	
	@Test
	public void only_position_short_id_fail() {
		Assert.assertFalse(matches(pattern, "***********WB93******Y"));
	}
	
	@Test
	public void only_position_long_id_fail() {
		Assert.assertFalse(matches(pattern, "*************WB93****Y"));
	}
	
	@Test
	public void only_too_short_position_fail() {
		Assert.assertFalse(matches(pattern, "*************B9******Y"));
	}
	
	@Test
	public void no_reading_too_long_no_weight_fail() {
		Assert.assertFalse(matches(pattern, "************WB93******Y"));
	}
	
	@Test
	public void no_reading_too_short_no_weight_fail() {
		Assert.assertFalse(matches(pattern, "************WB93****Y"));
	}
	
	@Test
	public void too_long_weight_fail() {
		Assert.assertFalse(matches(pattern, "123456789012WB93990488Y"));
	}
	
	@Test
	public void too_short_weight_fail() {
		Assert.assertFalse(matches(pattern, "123456789012WB939908Y"));
	}
	
	@Test
	public void too_long_weight_asterisks_fail() {
		Assert.assertFalse(matches(pattern, "123456789012WB939904**Y"));
	}
	
	@Test
	public void too_short_weight_asterisks_fail() {
		Assert.assertFalse(matches(pattern, "123456789012WB9399**Y"));
	}
	
	@Test
	public void too_short_no_weight_fail() {
		Assert.assertFalse(matches(pattern, "123456789012WB93****Y"));
	}
	
	@Test
	public void too_long_no_weight_fail() {
		Assert.assertFalse(matches(pattern, "123456789012WB93******Y"));
	}
	
	@Test
	public void letters_in_weight_fail() {
		Assert.assertFalse(matches(pattern, "123456789012WB93990A8Y"));
	}
	
	@Test
	public void symbols_in_weight_fail() {
		Assert.assertFalse(matches(pattern, "123456789012WB93_9048Y"));
	}

	@Test
	public void bad_letter_flag_fail() {
		Assert.assertFalse(matches(pattern, "123456789012WB9399048U"));
	}
	
	@Test
	public void lower_case_flag_fail() {
		Assert.assertFalse(matches(pattern, "123456789012WB9399048n"));
	}
	
	@Test
	public void no_flag_fail() {
		Assert.assertFalse(matches(pattern, "123456789012WB9399048"));
	}
	
	@Test
	public void asterisk_flag_fail() {
		Assert.assertFalse(matches(pattern, "123456789012WB9399048*"));
	}
	
	@Test
	public void double_flag_fail() {
		Assert.assertFalse(matches(pattern, "123456789012WB9399048YY"));
	}
	
	@Test
	public void flag_and_asterisk_fail() {
		Assert.assertFalse(matches(pattern, "123456789012WB9399048Y*"));
	}

	@Test
	public void group_get_id_ok() {
		Assert.assertEquals(group(pattern, "123456789012WB9399048Y", 1), "123456789012");		
	}
	
	@Test
	public void group_get_id_asterisks_ok() {
		Assert.assertEquals(group(pattern, "123456789***WB9399048Y", 1), "123456789***");
	}
	
	@Test
	public void group_get_no_reading_id_ok() {
		Assert.assertEquals(group(pattern, "************WB9399048Y", 1), "************");
	}
	
	@Test
	public void group_complete_id_get_position_ok() {
		Assert.assertEquals(group(pattern, "123456789012WB9399048Y", 2), "WB93");
	}
	
	@Test
	public void group_id_with_asterisks_get_position_ok() {
		Assert.assertEquals(group(pattern, "123456789***WB9399048Y", 2), "WB93");
	}
	
	@Test
	public void group_no_reading_get_position_ok() {
		Assert.assertEquals(group(pattern, "************WB9399048Y", 2), "WB93");
	}
	
	@Test
	public void group_complete_id_get_weight_ok() {
		Assert.assertEquals(group(pattern, "123456789012WB9399048Y", 3), "99048");
	}
	
	@Test
	public void group_complete_id_get_weight_with_asterisks_ok() {
		Assert.assertEquals(group(pattern, "123456789012WB93990**Y", 3), "990**");
	}
	
	@Test
	public void group_complete_id_get_no_weight_ok() {
		Assert.assertEquals(group(pattern, "123456789012WB93*****Y", 3), "*****");
	}
	
	@Test
	public void group_id_with_asterisks_get_weight_ok() {
		Assert.assertEquals(group(pattern, "123456789***WB9399048Y", 3), "99048");
	}
	
	@Test
	public void group_id_with_asterisks_get_weight_with_asterisks_ok() {
		Assert.assertEquals(group(pattern, "123456789***WB93990**Y", 3), "990**");
	}
	
	@Test
	public void group_id_with_asterisks_get_no_weight_ok() {
		Assert.assertEquals(group(pattern, "123456789***WB93*****Y", 3), "*****");
	}
	
	@Test
	public void group_no_reading_get_weigth_ok() {
		Assert.assertEquals(group(pattern, "************WB9399048Y", 3), "99048");
	}
	
	@Test
	public void group_no_reading_get_weight_with_asterisks_ok() {
		Assert.assertEquals(group(pattern, "************WB93990**Y", 3), "990**");
	}
	
	@Test
	public void group_no_reading_get_no_weight_ok() {
		Assert.assertEquals(group(pattern, "************WB93*****Y", 3), "*****");
	}
	
	@Test
	public void group_weight_get_flag_ok() {
		Assert.assertEquals(group(pattern, "123456789***WB9399048Y", 4), "Y");
	}
	
	@Test
	public void group_weight_with_asterisks_get_flag_ok() {
		Assert.assertEquals(group(pattern, "123456789***WB93990**Y", 4), "Y");
	}
	
	@Test
	public void group_no_weight_get_flag_ok() {
		Assert.assertEquals(group(pattern, "123456789***WB93*****Y", 4), "Y");
	}
}
