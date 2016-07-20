package libcomm.message;



/**
 * Representa un mensaje AK (<i>acknowgledgement</i>).
 * <p>
 * 16/01/2016 17:53:08
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class ContentAK extends AbstractContent {

	/* Indicador de si la posición está habilitada. */
	private Field<SequenceNumber> confirmedSequenceNumber;

	/* Constructor por defecto. */
	ContentAK() {
		super();
	}
	
	/**
	 * Devuelve el número de secuencia a confirmar.
	 * 
	 * @return 
	 * 		Número de secuencia a confirmar.
	 */
	SequenceNumber getConfirmedSequenceNumber() {
		return getFieldValue(confirmedSequenceNumber);
	}


	/**
	 * Establece el parámetro indicado.
	 * 
	 * @param sequenceNumber
	 *            Establece el número de secuencia confirmado.
	 */
	void setConfirmedSequenceNumber(final SequenceNumber sequenceNumber) {
		this.confirmedSequenceNumber = Field.create(FieldMetaData.SEQUENCE_NUMBER, sequenceNumber);
	}

	/**
	 * Imprime los campos del mensaje concatenados, formando el contenido
	 * completo.
	 * 
	 * @return {@link String} con el <b>contenido</b> del mensaje impreso
	 *         completo.
	 */
	@Override
	public String printForMessage() {
		return printFields(confirmedSequenceNumber);
	}

	/**
	 * Sobrecarga de {@link Object#toString()}.
	 * 
	 * @return Cadena que representa el objeto.
	 */
	@Override
	public String toString() {
		return printForMessage();
	}
}
