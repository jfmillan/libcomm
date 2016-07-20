package libcomm.util;

import libcomm.exception.CommErrorType;
import libcomm.exception.LibcommException;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Utilidades para errores.
 * <p>
 * 20/07/2016 23:27:38
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class ErrorUtils {
	/**
	 * Busca uno o varios tipos de error en una pila de excepciones. Si
	 * encuentra alguno, devuelve <code>true</code>, o bien devuelve
	 * <code>false</code> en caso contrario.
	 * 
	 * @param exception
	 *            Error en el que hay que buscar el tipo concreto.
	 * @param types
	 *            Tipos de error a buscar. Basta encontrar alguno en toda la
	 *            pila para devolver <code>true</code>.
	 * @return Indica si encuentra el error de alguno de los tipos indicados en
	 *         la pila de excepciones.
	 */
	public static boolean findErrorType(final LibcommException exception, final CommErrorType... types) {
		boolean foundException = false;
		
		if (exception == null || types == null || types.length == 0) {
			return false;
		}
		
		Throwable currentEx = exception;
		while (!foundException && currentEx != null) {
			if (currentEx instanceof LibcommException) {
				foundException = ArrayUtils.contains(types, ((LibcommException)currentEx).getErrorType());
			}
			currentEx = currentEx.getCause();
		}
		return foundException;
	}
}
