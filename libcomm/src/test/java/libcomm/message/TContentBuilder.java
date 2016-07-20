package libcomm.message;

import libcomm.exception.MalformedMessageException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test para probar los builder concretos de contenido de los mensajes.
 * <p>
 * 24/01/2016 22:14:32
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class TContentBuilder {
	
	@Test
	public void builder_pr_ok() {
		final String content = "77889910****P0019981*Y";
		Exception error = null;
		MessagePR pr = null;
		try {
			pr = (MessagePR) MessageType.PR.getBuilder().buildMessageFromContent(content);
		} catch (MalformedMessageException e) {
			error = e;
		}
		
		Assert.assertNull(error);
		Assert.assertNotNull(pr);
		
		Assert.assertNotNull(pr.getPackageId());
		Assert.assertEquals(Long.valueOf(77889910L), pr.getPackageId());
		
		Assert.assertNotNull(pr.getPosition());
		Assert.assertEquals("P001", pr.getPosition());
		
		Assert.assertNotNull(pr.getWeight());
		Assert.assertEquals(Integer.valueOf(9981), pr.getWeight());
		
		Assert.assertNotNull(pr.getRequiredReply());
		Assert.assertTrue(pr.getRequiredReply());
		Assert.assertEquals("****PR77889910****P0019981*Y", pr.printMessage());
	}
	
	@Test
	public void builder_pr_fail() {
		final String content = "77889910****P0019981YN";
		Exception error = null;
		MessagePR pr = null;
		
		try {
			pr = (MessagePR) MessageType.PR.getBuilder().buildMessageFromContent(content);
		} catch (MalformedMessageException e) {
			error = e;
		}
		
		Assert.assertNotNull(error);
		Assert.assertNull(pr);
	}

	
	@Test
	public void builder_gt_ok() {
		final String content = "109876543210BY34";
		Exception error = null;
		MessageGT gt = null;
		try {
			gt = (MessageGT) MessageType.GT.getBuilder().buildMessageFromContent(content);
		} catch (MalformedMessageException e) {
			error = e;
		}
		
		Assert.assertNull(error);
		Assert.assertNotNull(gt);
		
		Assert.assertNotNull(gt.getPackageId());
		Assert.assertEquals(Long.valueOf(109876543210L), gt.getPackageId());
		
		Assert.assertNotNull(gt.getPosition());
		Assert.assertEquals("BY34", gt.getPosition());
		
		Assert.assertEquals("****GT109876543210BY34", gt.printMessage());
	}
	
	@Test
	public void builder_gt_fail() {
		final String content = "109876543210BY3*";
		Exception error = null;
		MessageGT gt = null;
		
		try {
			gt = (MessageGT) MessageType.GT.getBuilder().buildMessageFromContent(content);
		} catch (MalformedMessageException e) {
			error = e;
		}
		
		Assert.assertNotNull(error);
		Assert.assertNull(gt);
	}
	
	@Test
	public void builder_st_ok() {
		final String content = "AABBN";
		Exception error = null;
		MessageST st = null;
		try {
			st = (MessageST) MessageType.ST.getBuilder().buildMessageFromContent(content);
		} catch (MalformedMessageException e) {
			error = e;
		}
		
		Assert.assertNull(error);
		Assert.assertNotNull(st);
		Assert.assertNotNull(st.getPosition());
		Assert.assertEquals("AABB", st.getPosition());
		Assert.assertNotNull(st.getEnabledPosition());
		Assert.assertFalse(st.getEnabledPosition());
		Assert.assertEquals("****STAABBN", st.printMessage());
	}
	
	@Test
	public void builder_st_fail() {
		final String content = "AAbBN";
		Exception error = null;
		MessageST st = null;
		
		try {
			st = (MessageST) MessageType.ST.getBuilder().buildMessageFromContent(content);
		} catch (MalformedMessageException e) {
			error = e;
		}
		
		Assert.assertNotNull(error);
		Assert.assertNull(st);
	}
	
	@Test
	public void builder_ak_ok() {
		final String content = "00FB";
		Exception error = null;
		MessageAK ak = null;
		try {
			ak = (MessageAK) MessageType.AK.getBuilder().buildMessageFromContent(content);
		} catch (MalformedMessageException e) {
			error = e;
		}
		
		final SequenceNumber compareSN = new SequenceNumber(0x00FB);
		Assert.assertNull(error);
		Assert.assertNotNull(ak);
		Assert.assertEquals(compareSN, ak.getConfirmedSequenceNumber());
		Assert.assertEquals(compareSN.printForMessage(), ak.getConfirmedSequenceNumber().printForMessage());
	}
	
	@Test
	public void builder_ak_fail() {
		final String content = "**FB";
		Exception error = null;
		MessageAK ak = null;
		
		try {
			ak = (MessageAK) MessageType.AK.getBuilder().buildMessageFromContent(content);
		} catch (MalformedMessageException e) {
			error = e;
		}
		
		Assert.assertNotNull(error);
		Assert.assertNull(ak);
	}
}
