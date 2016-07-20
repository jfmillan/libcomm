package libcomm.message;



/**
 * Representa un mensaje GT (<i>go to</i>).
 * <p>
 * 16/01/2016 17:48:22
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class ContentGT extends AbstractContent {

	/* Identificador de paquete. */
	private Field<Long> packageId;
	
	/* Identificador de posición. */
	private Field<String> position;
	
	/* Constructor por defecto. */
	ContentGT() {
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
	void setPosition(String position) {
		this.position = Field.create(FieldMetaData.POSITION, position);
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
		return printFields(packageId, position);
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
