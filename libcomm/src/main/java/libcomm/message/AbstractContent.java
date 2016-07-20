package libcomm.message;

import commons.util.ColUtils;


/**
 * Representa el contenido útil de un mensaje, el conjunto de sus campos.
 * <p>
 * 15/01/2016 20:21:53
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
abstract class AbstractContent implements IMessageContent {

	/**
	 * Obtiene el valor de un campo controlando nulos.
	 * 
	 * @param field
	 *            Campo.
	 * @return Valor del campo, <code>null</code> si el campo o su valor son
	 *         nulos.
	 */
	protected <T> T getFieldValue (final Field<T> field) {
		return field != null ? field.getValue() : null;
	}
	
	/**
	 * Imprime un conjunto de campos uno detrás de otro.
	 * 
	 * @param fields
	 *            Campos a imprimir.
	 * @return cadena concatenada de todos los campos.
	 */
	protected String printFields(final Field<?>... fields) {
		final StringBuilder result = new StringBuilder();
		if (ColUtils.hasElements(fields)) {
			for (final Field<?> field : fields) {
				if (field != null) {
					result.append(field.printForMessage());
				}
			}
		}
		return result.toString();
	}
}
