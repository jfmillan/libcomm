package libcomm.message;

import libcomm.util.Regex;


/**
 * Clase con información de metadatos común a cada campo: tamaño, tipo.
 * <p>
 * 16/01/2016 01:32:44
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public enum FieldMetaData {
	SEQUENCE_NUMBER (String.class, SequenceNumber.LENGTH),
	MESSAGE_TYPE (String.class, 2),
	PACKAGE_ID (Long.class, 12),
	POSITION (String.class, 4),
	WEIGHT (Integer.class, 5),
	REQUIRED_REPLY (Character.class, 1),
	ENABLED_POSITION (Character.class, 1); /* Valor genérico para flags si/no */
	
	/* Tipo del campo. */
	private final Class<?> type;
	
	/* Longitud del campo. */
	private final int length;
	
	/* Constructor privado. */
	private <T> FieldMetaData (final Class<T> clazz, final int length) {
		this.type = clazz;
		this.length = length;
	};
	
	/**
	 * Obtiene la expresión regular que evalua el campo.
	 * 
	 * @return Expresión regular.
	 */
	public Regex getRegex() {
		return Regex.valueOf(name());
	}
	
	/**
	 * Obtiene el tipo.
	 * @return Clase con el tipo.
	 */
	@SuppressWarnings("unchecked")
	public <T> Class<T> getType() {
		return (Class<T>) type;
	}
	
	/**
	 * Obtiene la longitud del campo.
	 * 
	 * @return Longitud del campo.
	 */
	public int getLength() {
		return length;
	}
	
}
