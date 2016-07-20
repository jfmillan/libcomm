package libcomm.message;


/**
 * Representa un mensaje ST (<i>state (of position)</i>) completo.
 * <p>
 * 17/01/2016 00:52:03
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class MessageST extends Message<ContentST> {

	MessageST(final ContentST content) {
		super(MessageType.ST, content);
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
	 * @return parámetro requiredReply a devolver.
	 */
	public Boolean getEnabledPosition() {
		return getContent().getEnabledPosition();
	}


	/**
	 * Establece el parámetro indicado.
	 * 
	 * @param enabledPosition
	 *            Parámetro a establecer en requiredReply.
	 */
	public void setEnabledPosition(final Boolean enabledPosition) {
		getContent().setEnabledPosition(enabledPosition);
	}
}
