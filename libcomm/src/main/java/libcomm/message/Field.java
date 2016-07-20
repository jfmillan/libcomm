package libcomm.message;

import libcomm.util.MessageUtils;

import org.apache.commons.lang3.StringUtils;

import commons.util.StrUtils;

/**
 * Representa el campo de un mensaje.
 * <p>
 * 16/01/2016 13:44:00
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class Field<T> implements IPrintForMessage {

	/* Valor del campo. */
	private final T value;
	
	/* Longitud mínima para imprimir el mensaje. */
	private final FieldMetaData metaData;

	/*
	 * Constructor protected con el valor y el mínimo de longitud. Para crear un
	 * campo desde una clase externa debe usarse el método estático
	 * #create(FieldMetaData, Object).
	 */
	protected Field (final FieldMetaData metaData, final T value) {
		this.value = value;
		this.metaData = metaData;
	}
	
	/**
	 * Obtiene el valor del campo.
	 * 
	 * @return Valor del campo.
	 */
	public T getValue() {
		return this.value;
	}


	/**
	 * Crea un campo.
	 * 
	 * @param metaData
	 *            Metadatos del campo.
	 * @param value
	 *            Valor.
	 * @return Campo creado.
	 */
	public static <T> Field<T> create(final FieldMetaData metaData, final T value) {
		return new Field<T>(metaData, value);
	}
	
	/**
	 * Impresión por defecto, un toString del objeto completado con caracter de
	 * relleno <code>*</code> hasta completar el total de longitud mínima.
	 * 
	 * @return Campo impreso.
	 */
	@Override
	public String printForMessage() {
		String print = null;
		if (value instanceof IPrintForMessage) {
			print = ((IPrintForMessage)value).printForMessage();
		} else {
			print = value != null ? value.toString() : StrUtils.EMPTY_STRING;
		}
		return metaData != null ? StringUtils.rightPad(print, metaData.getLength(), MessageUtils.MESSAGE_PAD) : print;
	}
}
