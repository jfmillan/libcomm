package libcomm.message;

import libcomm.exception.MalformedMessageException;
import libcomm.message.rfc1006.DataTsdu;
import libcomm.util.MessageUtils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test para probar la factoría de mensajes.
 * <p>
 * 24/01/2016 22:14:32
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TMessageFactory {

	@Test
	public void build_empty_ok() {
		MessageST st = null;
		Exception error = null;
		try {
			st = MessageFactory.getMessage(MessageType.ST);
		} catch (Exception e) {
			error = e;
		}
		Assert.assertNull(error);
		Assert.assertNotNull(st);
		Assert.assertNotNull(st.getSequenceNumber());
		Assert.assertEquals(SequenceNumber.NOT_DEFINED, Integer.valueOf(st.getSequenceNumber().getNumber()));
		Assert.assertNotNull(st.getMessageType());
		Assert.assertEquals(MessageType.ST, st.getMessageType());
		Assert.assertNotNull(st.getContent());
	}


	@Test
	public void build_empty_fail() {
		MessageST msg = null;
		Exception error = null;
		try {
			msg = MessageFactory.getMessage((MessageType)null);
		} catch (Exception e) {
			error = e;
		}
		Assert.assertNotNull(error);
		Assert.assertNull(msg);
	}

	@Test
	public void build_with_sequence_number_ok() {
		MessageST st = null;
		Exception error = null;
		try {
			st = MessageFactory.getMessage(new SequenceNumber(0x34F2), MessageType.ST);
		} catch (Exception e) {
			error = e;
		}
		Assert.assertNull(error);
		Assert.assertNotNull(st);
		Assert.assertNotNull(st.getSequenceNumber());
		Assert.assertEquals(Integer.valueOf(0x34F2), Integer.valueOf(st.getSequenceNumber().getNumber()));
		Assert.assertNotNull(st.getMessageType());
		Assert.assertEquals(MessageType.ST, st.getMessageType());
		Assert.assertNotNull(st.getContent());
	}
	
	private static DataTsdu getDataTsdu (final String msg) throws MalformedMessageException {
		return new DataTsdu(null, MessageUtils.encode(msg));
	}
	
	@Test
	public void build_ak_ok() {
		final String msgStr = "0F3BAK440C"; 
		MessageAK ak = null;
		Exception error = null;
		try {
			ak = MessageFactory.getMessage(getDataTsdu(msgStr));
		} catch (Exception e) {
			error = e;
		}
		Assert.assertNull(error);
		Assert.assertNotNull(ak);
		Assert.assertNotNull(ak.getSequenceNumber());
		Assert.assertEquals(Integer.valueOf(0x0F3B), Integer.valueOf(ak.getSequenceNumber().getNumber()));
		Assert.assertNotNull(ak.getMessageType());
		Assert.assertEquals(MessageType.AK, ak.getMessageType());
		Assert.assertNotNull(ak.getContent());
		
		Assert.assertNotNull(ak.getConfirmedSequenceNumber());
		Assert.assertEquals(Integer.valueOf(0x440C), Integer.valueOf(ak.getConfirmedSequenceNumber().getNumber()));
		Assert.assertEquals(msgStr, ak.printMessage());
	}
	
	@Test
	public void build_ak_from_fields_ok() {
		final String expected = "0F3BAK440C";
		final MessageAK ak = MessageFactory.getMessage(new SequenceNumber(0x0F3B), MessageType.AK);
		ak.setConfirmedSequenceNumber(new SequenceNumber(0x440C));
		
		Assert.assertEquals(expected, ak.printMessage());
	}
	
	@Test
	public void build_pr_ok() {
		final String msgStr = "56B3PR9877704871**P3A18897*N";
		Exception error = null;
		MessagePR pr = null;
		try {
			pr = MessageFactory.getMessage(getDataTsdu(msgStr));
		} catch (MalformedMessageException e) {
			error = e;
		}

		Assert.assertNull(error);
		Assert.assertNotNull(pr);
		Assert.assertNotNull(pr.getSequenceNumber());
		Assert.assertEquals(Integer.valueOf(0x56B3), Integer.valueOf(pr.getSequenceNumber().getNumber()));
		Assert.assertNotNull(pr.getMessageType());
		Assert.assertEquals(MessageType.PR, pr.getMessageType());
		Assert.assertNotNull(pr.getContent());
		
		Assert.assertNotNull(pr.getPackageId());
		Assert.assertEquals(Long.valueOf(9877704871L), pr.getPackageId());
		
		Assert.assertNotNull(pr.getPosition());
		Assert.assertEquals("P3A1", pr.getPosition());
		
		Assert.assertNotNull(pr.getWeight());
		Assert.assertEquals(Integer.valueOf(8897), pr.getWeight());
		
		Assert.assertNotNull(pr.getRequiredReply());
		Assert.assertFalse(pr.getRequiredReply());
		Assert.assertEquals(msgStr, pr.printMessage());
	}

	@Test
	public void build_pr_from_fields_ok() {
		final String expected = "56B3PR9877704871**P3A18897*N";
		final MessagePR pr = MessageFactory.getMessage(new SequenceNumber(0x56B3), MessageType.PR);
		pr.setPackageId(9877704871L);
		pr.setPosition("P3A1");
		pr.setWeight(8897);
		pr.setRequiredReply(Boolean.FALSE);
		
		Assert.assertEquals(expected, pr.printMessage());
	}
	
	@Test
	public void builder_gt_ok() {
		final String msgStr = "0001GT98765001****Q331";
		Exception error = null;
		MessageGT gt = null;
		try {
			gt = MessageFactory.getMessage(getDataTsdu(msgStr));
		} catch (MalformedMessageException e) {
			error = e;
		}

		Assert.assertNull(error);
		Assert.assertNotNull(gt);
		Assert.assertNotNull(gt.getSequenceNumber());
		Assert.assertEquals(Integer.valueOf(0x0001), Integer.valueOf(gt.getSequenceNumber().getNumber()));
		Assert.assertNotNull(gt.getMessageType());
		Assert.assertEquals(MessageType.GT, gt.getMessageType());
		Assert.assertNotNull(gt.getContent());
		
		Assert.assertNotNull(gt.getPackageId());
		Assert.assertEquals(Long.valueOf(98765001L), gt.getPackageId());
		
		Assert.assertNotNull(gt.getPosition());
		Assert.assertEquals("Q331", gt.getPosition());
		
		Assert.assertEquals(msgStr, gt.printMessage());		
	}
	
	@Test
	public void build_gt_from_fields_ok() {
		final String expected = "0001GT98765001****Q331";
		final MessageGT gt = MessageFactory.getMessage(new SequenceNumber(0x1), MessageType.GT);
		gt.setPackageId(98765001L);
		gt.setPosition("Q331");
		
		Assert.assertEquals(expected, gt.printMessage());
	}

	@Test
	public void builder_st_ok() {
		final String msgStr = "FFFFST0002Y";
		Exception error = null;
		MessageST st = null;
		try {
			st = MessageFactory.getMessage(getDataTsdu(msgStr));
		} catch (MalformedMessageException e) {
			error = e;
		}

		Assert.assertNull(error);
		Assert.assertNotNull(st);
		Assert.assertNotNull(st.getSequenceNumber());
		Assert.assertEquals(Integer.valueOf(0xFFFF), Integer.valueOf(st.getSequenceNumber().getNumber()));
		Assert.assertNotNull(st.getMessageType());
		Assert.assertEquals(MessageType.ST, st.getMessageType());
		Assert.assertNotNull(st.getContent());
		
		Assert.assertNotNull(st.getPosition());
		Assert.assertEquals("0002", st.getPosition());
		
		Assert.assertNotNull(st.getEnabledPosition());
		Assert.assertTrue(st.getEnabledPosition());
		Assert.assertEquals(msgStr, st.printMessage());	
	}
	
	@Test
	public void build_st_from_fields_ok() {
		final String expected = "FFFFST0002Y";
		final MessageST st = MessageFactory.getMessage(new SequenceNumber(0xFFFF), MessageType.ST);
		st.setPosition("0002");
		st.setEnabledPosition(Boolean.TRUE);
		
		Assert.assertEquals(expected, st.printMessage());
	}

	@Test
	public void build_message_fail() {
		MessageAK ak = null;
		Exception error = null;
		try {
			ak = MessageFactory.getMessage(getDataTsdu("0f3BAK*4GC"));
		} catch (Exception e) {
			error = e;
		}
		Assert.assertNotNull(error);
		Assert.assertNull(ak);
	}
}
