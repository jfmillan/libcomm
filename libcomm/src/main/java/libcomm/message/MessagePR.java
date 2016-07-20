package libcomm.message;


/**
 * Representa un mensaje PR (<i>position reached</i>) completo.
 * <p>
 * 16/01/2016 23:04:44
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class MessagePR extends Message<ContentPR> {

	MessagePR(final ContentPR content) {
		super(MessageType.PR, content);
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
	public void setPosition(final String position) {
		getContent().setPosition(position);
	}


	/**
	 * Devuelve el parámetro indicado.
	 * @return parámetro weight a devolver.
	 */
	public Integer getWeight() {
		return getContent().getWeight();
	}


	/**
	 * Establece el parámetro indicado.
	 * @param weight Parámetro a establecer en weight.
	 */
	public void setWeight(final Integer weight) {
		getContent().setWeight(weight);
	}


	/**
	 * Devuelve el parámetro indicado.
	 * @return parámetro requiredReply a devolver.
	 */
	public Boolean getRequiredReply() {
		return getContent().getRequiredReply();
	}


	/**
	 * Establece el parámetro indicado.
	 * 
	 * @param requiredReply
	 *            Parámetro a establecer en requiredReply.
	 */
	public void setRequiredReply(final Boolean requiredReply) {
		getContent().setRequiredReply(requiredReply);
	}
}
