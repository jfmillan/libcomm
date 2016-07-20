package libcomm.utils;

import libcomm.message.FieldMetaData;
import libcomm.message.MessageFactory;
import libcomm.message.MessageGT;
import libcomm.message.MessagePR;
import libcomm.message.MessageST;
import libcomm.message.MessageType;

/**
 * Factoría de mensajes aleatorios para tests y pruebas de envío y recepción.
 * <p>
 * 16/03/2016 22:44:07
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class MessageFactoryTest {

	/** 
	 * Obtiene un PR aleatorio. 
	 * 
	 * @return Mensaje PR generado.
	 */
	public static MessagePR getRandomPR () {
		final MessagePR pr = (MessagePR) MessageFactory.getMessage(MessageType.PR);
		pr.setPackageId(createRandomPackageId());
		pr.setPosition(createRandomPosition());
		pr.setRequiredReply(createRandomFlag());
		pr.setWeight(createRandomWeight());

		return pr;
	}
	
	/** 
	 * Obtiene un ST aleatorio. 
	 * 
	 * @return Mensaje ST generado.
	 */
	public static MessageST getRandomST () {
		final MessageST st = (MessageST) MessageFactory.getMessage(MessageType.ST);
		st.setPosition(createRandomPosition());
		st.setEnabledPosition(createRandomFlag());

		return st;
	}

	/** 
	 * Obtiene un GT aleatorio. 
	 * 
	 * @return Mensaje GT generado.
	 */
	public static MessageGT getRandomGT() {
		final MessageGT gt = (MessageGT) MessageFactory.getMessage(MessageType.GT);
		gt.setPackageId(createRandomPackageId());
		gt.setPosition(createRandomPosition());
		
		return gt;
	}
	
	/** 
	 * Obtiene un GT aleatorio mal generado, con la posición formada únicamente por caracteres en minúsculas. 
	 * 
	 * @return Mensaje GT generado.
	 */
	public static MessageGT getBadRandomGT() {
		final MessageGT badGt = (MessageGT) MessageFactory.getMessage(MessageType.GT);
		badGt.setPackageId(createRandomPackageId());
		
		/* solo alfabeto (createRandomPosition podría generar sólo numericos y al hacer el lowercase quedarse igual */
		final String position = MessageUtilsTest.createAlpha(FieldMetaData.POSITION.getLength()/2,  FieldMetaData.POSITION.getLength());
		badGt.setPosition(position.toLowerCase());
		
		return badGt;
	}
	
	/* Crea un id de paquete aleatorio. */
	private static long createRandomPackageId() {
		return Long.parseLong(MessageUtilsTest.createNumeric(FieldMetaData.PACKAGE_ID.getLength()/2, FieldMetaData.PACKAGE_ID.getLength()));
	}

	/* Crea una posición aleatoria. */
	private static String createRandomPosition() {
		return MessageUtilsTest.createAlphanumeric(FieldMetaData.POSITION.getLength(),  FieldMetaData.POSITION.getLength());
	}
	
	/* Crea un booleano aleatorio para representar un campo flag (Y equivale a true, N a false). */
	private static Boolean createRandomFlag () {
		final String flag = MessageUtilsTest.createFlag();
		return flag.equals("Y");
	}

	/* Crea un peso aleatorio. */
	private static int createRandomWeight() {
		return Integer.parseInt(MessageUtilsTest.createNumeric(FieldMetaData.WEIGHT.getLength()/2, FieldMetaData.WEIGHT.getLength()));
	}
}
