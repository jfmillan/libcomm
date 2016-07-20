package libcomm.message.parser;

import libcomm.exception.MalformedMessageException;
import libcomm.message.Field;
import libcomm.message.FieldMetaData;
import libcomm.message.MessageType;
import libcomm.message.SequenceNumber;

import org.junit.Assert;
import org.junit.Test;

import commons.util.StrUtils;

/**
 * Prueba la clase {@link FieldParser}.
 * 
 * <p>
 * 24/01/2016 12:03:58
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TFieldParser {

	@Test
	public void parse_sequence_number_ok() {
		Exception error = null;
		Field<String> result = null;
		try {
			result = FieldParser.parse(FieldMetaData.SEQUENCE_NUMBER, "AB3F");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNull(error);
		Assert.assertNotNull(result);
		Assert.assertEquals(new SequenceNumber(0xAB3F).printForMessage(), result.getValue());
	}

	@Test
	public void parse_no_sequence_number_ok() {
		Exception error = null;
		Field<String> result = null;
		try {
			result = FieldParser.parse(FieldMetaData.SEQUENCE_NUMBER, "****");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNull(error);
		Assert.assertNotNull(result);
		Assert.assertEquals((String) null, result.getValue());
	}

	@Test
	public void parse_message_type_ok() {
		Exception error = null;
		Field<String> result = null;
		try {
			result = FieldParser.parse(FieldMetaData.MESSAGE_TYPE, "ST");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNull(error);
		Assert.assertNotNull(result);
		Assert.assertEquals(MessageType.ST.toString(), result.getValue());
	}

	@Test
	public void parse_package_id_ok() {
		Exception error = null;
		Field<Long> result = null;
		try {
			result = FieldParser.parse(FieldMetaData.PACKAGE_ID, "123456789012");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNull(error);
		Assert.assertNotNull(result);
		Assert.assertEquals(Long.valueOf(123456789012L), result.getValue());
	}

	@Test
	public void parse_package_id_with_asterisks_ok() {
		Exception error = null;
		Field<Long> result = null;
		try {
			result = FieldParser.parse(FieldMetaData.PACKAGE_ID, "1234********");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNull(error);
		Assert.assertNotNull(result);
		Assert.assertEquals(Long.valueOf(1234L), result.getValue());
	}

	@Test
	public void parse_no_reading_ok() {
		Exception error = null;
		Field<Long> result = null;
		try {
			result = FieldParser.parse(FieldMetaData.PACKAGE_ID, "************");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNull(error);
		Assert.assertNotNull(result);
		Assert.assertEquals((Long) null, result.getValue());
	}

	@Test
	public void parse_weight_ok() {
		Exception error = null;
		Field<Integer> result = null;
		try {
			result = FieldParser.parse(FieldMetaData.WEIGHT, "10084");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNull(error);
		Assert.assertNotNull(result);
		Assert.assertEquals(Integer.valueOf(10084), result.getValue());
	}

	@Test
	public void parse_weight_with_asterisks_ok() {
		Exception error = null;
		Field<Integer> result = null;
		try {
			result = FieldParser.parse(FieldMetaData.WEIGHT, "100**");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNull(error);
		Assert.assertNotNull(result);
		Assert.assertEquals(Integer.valueOf(100), result.getValue());
	}

	@Test
	public void parse_no_weight_ok() {
		Exception error = null;
		Field<Integer> result = null;
		try {
			result = FieldParser.parse(FieldMetaData.WEIGHT, "*****");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNull(error);
		Assert.assertNotNull(result);
		Assert.assertEquals((Integer) null, result.getValue());
	}
	
	@Test
	public void parse_required_reply_ok() {
		Exception error = null;
		Field<Character> result = null;
		try {
			result = FieldParser.parse(FieldMetaData.REQUIRED_REPLY, "N");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNull(error);
		Assert.assertNotNull(result);
		Assert.assertEquals(Character.valueOf('N'), result.getValue());
	}

	@Test
	public void parse_enabled_position_ok() {
		Exception error = null;
		Field<Character> result = null;
		try {
			result = FieldParser.parse(FieldMetaData.ENABLED_POSITION, "Y");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNull(error);
		Assert.assertNotNull(result);
		Assert.assertEquals(Character.valueOf('Y'), result.getValue());
	}
	
	@Test
	public void parse_position_ok() {
		Exception error = null;
		Field<String> result = null;
		try {
			result = FieldParser.parse(FieldMetaData.POSITION, "TR09");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNull(error);
		Assert.assertNotNull(result);
		Assert.assertEquals("TR09", result.getValue());
	}
	
	
	
	
	@Test
	public void empty_sequence_number_exception_fail() {
		Exception error = null;
		try {
			FieldParser.parse(FieldMetaData.SEQUENCE_NUMBER, StrUtils.EMPTY_STRING);
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNotNull(error);
	}	
	
	@Test
	public void empty_message_type_exception_fail() {
		Exception error = null;
		try {
			FieldParser.parse(FieldMetaData.MESSAGE_TYPE, StrUtils.EMPTY_STRING);
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNotNull(error);
	}	
	
	@Test
	public void empty_package_id_exception_fail() {
		Exception error = null;
		try {
			FieldParser.parse(FieldMetaData.PACKAGE_ID, StrUtils.EMPTY_STRING);
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNotNull(error);
	}	

	@Test
	public void empty_position_exception_fail() {
		Exception error = null;
		try {
			FieldParser.parse(FieldMetaData.POSITION, StrUtils.EMPTY_STRING);
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNotNull(error);
	}	

	
	@Test
	public void empty_weight_exception_fail() {
		Exception error = null;
		try {
			FieldParser.parse(FieldMetaData.WEIGHT, StrUtils.EMPTY_STRING);
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNotNull(error);
	}	

	@Test
	public void empty_required_reply_exception_fail() {
		Exception error = null;
		try {
			FieldParser.parse(FieldMetaData.REQUIRED_REPLY, StrUtils.EMPTY_STRING);
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNotNull(error);
	}	
	
	@Test
	public void empty_enabled_position_exception_fail() {
		Exception error = null;
		try {
			FieldParser.parse(FieldMetaData.ENABLED_POSITION, StrUtils.EMPTY_STRING);
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNotNull(error);
	}
	
	@Test
	public void partial_sequence_number_exception_fail() {
		Exception error = null;
		try {
			FieldParser.parse(FieldMetaData.SEQUENCE_NUMBER, "FF**");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNotNull(error);
	}	
	
	@Test
	public void bad_message_type_exception_fail() {
		Exception error = null;
		try {
			FieldParser.parse(FieldMetaData.MESSAGE_TYPE, "gt");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNotNull(error);
	}	
	
	@Test
	public void bad_package_id_exception_fail() {
		Exception error = null;
		try {
			FieldParser.parse(FieldMetaData.PACKAGE_ID, "123456789abc");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNotNull(error);
	}	

	@Test
	public void bad_position_exception_fail() {
		Exception error = null;
		try {
			FieldParser.parse(FieldMetaData.POSITION, "AÑPO");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNotNull(error);
	}	

	
	@Test
	public void bad_weight_exception_fail() {
		Exception error = null;
		try {
			FieldParser.parse(FieldMetaData.WEIGHT, "**323");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNotNull(error);
	}	

	@Test
	public void bad_required_reply_exception_fail() {
		Exception error = null;
		try {
			FieldParser.parse(FieldMetaData.REQUIRED_REPLY, "y");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNotNull(error);
	}	
	
	@Test
	public void bad_enabled_position_exception_fail() {
		Exception error = null;
		try {
			FieldParser.parse(FieldMetaData.ENABLED_POSITION, "NN");
		} catch (MalformedMessageException e) {
			error = e;
		}
		Assert.assertNotNull(error);
	}	
}
