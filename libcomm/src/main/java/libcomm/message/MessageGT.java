package libcomm.message;


/**
 * Representa un mensaje GT (<i>go to</i>) completo.
 * <p>
 * 17/01/2016 00:51:07
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class MessageGT extends Message<ContentGT> {

	MessageGT(final ContentGT content) {
		super(MessageType.GT, content);
	}

	/**
	 * Devuelve el parámetro indicado.
	 * 
	 * @return parámetro packageId a devolver.
	 */
	public Long getPackageId() {
		return getContent().getPackageId();
	}


	/**
	 * Establece el parámetro indicado.
	 * 
	 * @param packageId
	 *            Parámetro a establecer en packageId.
	 */
	public void setPackageId(final Long packageId) {
		getContent().setPackageId(packageId);
	}


	/**
	 * Devuelve el parámetro indicado.
	 * 
	 * @return parámetro position a devolver.
	 */
	public String getPosition() {
		return getContent().getPosition();
	}


	/**
	 * Establece el parámetro indicado.
	 * 
	 * @param position
	 *            Parámetro a establecer en position.
	 */
	public void setPosition(String position) {
		getContent().setPosition(position);
	}
}
