package libcomm.message;



/**
 * Representa el contenido de un mensaje PR (<i>position reached</i>).
 * <p>
 * 15/01/2016 20:48:22
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class ContentPR extends AbstractContent {

	/* Identificador de paquete. */
	private Field<Long> packageId;
	
	/* Identificador de posición. */
	private Field<String> position;
	
	/* Peso en gramos. */
	private Field<Integer> weight;
	
	/* Indicador de si requiere respuesta. */
	private FieldFlag requiredReply;

	/* Constructor por defecto. */
	ContentPR() {
		super();
	}

	/**
	 * Devuelve el parámetro indicado.
	 * 
	 * @return parámetro packageId a devolver.
	 */
	Long getPackageId() {
		return getFieldValue(packageId);
	}


	/**
	 * Establece el parámetro indicado.
	 * 
	 * @param packageId
	 *            Parámetro a establecer en packageId.
	 */
	void setPackageId(final Long packageId) {
		this.packageId = Field.create(FieldMetaData.PACKAGE_ID, packageId);
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
	 * @return parámetro weight a devolver.
	 */
	Integer getWeight() {
		return getFieldValue(weight);
	}


	/**
	 * Establece el parámetro indicado.
	 * @param weight Parámetro a establecer en weight.
	 */
	void setWeight(final Integer weight) {
		this.weight = Field.create(FieldMetaData.WEIGHT, weight);
	}


	/**
	 * Devuelve el parámetro indicado.
	 * @return parámetro requiredReply a devolver.
	 */
	Boolean getRequiredReply() {
		return requiredReply != null ? requiredReply.getBooleanValue() : null;
	}


	/**
	 * Establece el parámetro indicado.
	 * 
	 * @param requiredReply
	 *            Parámetro a establecer en requiredReply.
	 */
	void setRequiredReply(final Boolean requiredReply) {
		this.requiredReply = FieldFlag.create(FieldMetaData.REQUIRED_REPLY, requiredReply);
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
		return printFields(packageId, position, weight, requiredReply);
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
