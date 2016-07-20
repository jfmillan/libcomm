package libcomm.message;



/**
 * Representa un mensaje ST (<i>state (of position)</i>).
 * <p>
 * 15/01/2016 20:48:22
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class ContentST extends AbstractContent {

	/* Identificador de posición. */
	private Field<String> position;
	
	/* Indicador de si la posición está habilitada. */
	private FieldFlag enabledPosition;

	/* Constructor por defecto. */
	ContentST() {
		super();
	}

	/**
	 * Devuelve el parámetro indicado.
	 * 
	 * @return parámetro position a devolver.
	 */
	String getPosition() {
		return getFieldValue(position);
	}


	/**
	 * Establece el parámetro indicado.
	 * 
	 * @param position
	 *            Parámetro a establecer en position.
	 */
	void setPosition(final String position) {
		this.position = Field.create(FieldMetaData.POSITION, position);
	}


	/**
	 * Devuelve el parámetro indicado.
	 * @return parámetro requiredReply a devolver.
	 */
	Boolean getEnabledPosition() {
		return enabledPosition != null ? enabledPosition.getBooleanValue() : null;
	}


	/**
	 * Establece el parámetro indicado.
	 * 
	 * @param enabledPosition
	 *            Parámetro a establecer en requiredReply.
	 */
	void setEnabledPosition(final Boolean enabledPosition) {
		this.enabledPosition = FieldFlag.create(FieldMetaData.ENABLED_POSITION, enabledPosition);
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
		return printFields(position, enabledPosition);
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
