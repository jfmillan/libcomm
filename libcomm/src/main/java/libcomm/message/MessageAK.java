package libcomm.message;


/**
 * Representa un mensaje AK (<i>acknowgledgement</i>) completo.
 * <p>
 * 17/01/2016 00:52:03
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class MessageAK extends Message<ContentAK> {

	MessageAK(final ContentAK content) {
		super(MessageType.AK, content);
	}
	
	/**
	 * Devuelve el número de secuencia a confirmar.
	 * 
	 * @return Número de secuencia a confirmar.
	 */
	public SequenceNumber getConfirmedSequenceNumber() {
		return getContent().getConfirmedSequenceNumber();
	}


	/**
	 * Establece el número de secuencia confirmado.
	 * 
	 * @param sequenceNumber
	 *            Número de secuencia confirmado.
	 */
	public void setConfirmedSequenceNumber(final SequenceNumber sequenceNumber) {
		getContent().setConfirmedSequenceNumber(sequenceNumber);
	}

}
