package libcomm.exception;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import commons.exception.CommonException;
import commons.util.PrintUtils;

/**
 * Excepción genérica de más alto nivel para el proyecto.
 * <p>
 * 03/01/2016 20:08:09
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class LibcommException extends CommonException {
	private static final String PRINT_ARGUMENT_FORMAT = "[Arg %s: %s, %s]";
	
	private static final long serialVersionUID = -4786088067172952160L;

	private final LinkedHashMap<String, Object> arguments;
	
	private final CommErrorType errorType;
	
	/**
	 * Constructor, recibe el tipo de error.
	 * 
	 * @param errorType
	 *            Tipo de error
	 */
	public LibcommException(final CommErrorType errorType) {
		arguments = new LinkedHashMap<String, Object>();
		this.errorType = errorType;
	}

	/**
	 * Constructor con un mensaje.
	 * 
	 * @param errorType
	 *            Tipo de error
	 * 
	 * @param message
	 *            Mensaje que describe la excepción.
	 */
	public LibcommException(final CommErrorType errorType, final String message) {
		super(message);
		this.errorType = errorType;
		arguments = new LinkedHashMap<String, Object>();
	}

	/**
	 * Constructor con otra excepción de la que partir.
	 * 
	 * @param errorType
	 *            Tipo de error
	 * @param cause
	 *            Excepción que a su vez ha provocado esta otra excepción a
	 *            crear.
	 */
	public LibcommException(final CommErrorType errorType, final Throwable cause) {
		super(cause);
		this.errorType = errorType;
		arguments = new LinkedHashMap<String, Object>();
	}

	/**
	 * Constructor con mensaje y motivo.
	 * 
	 * @param errorType
	 *            Tipo de error
	 * @param message
	 *            Mensaje que describe la excepción.
	 * @param cause
	 *            Excepción que a su vez ha provocado esta otra excepción a
	 *            crear.
	 */
	public LibcommException(final CommErrorType errorType, final String message, final Throwable cause) {
		super(message, cause);
		this.errorType = errorType;
		arguments = new LinkedHashMap<String, Object>();
	}

	/**
	 * Constructor con mensaje y motivo.
	 * 
	 * @param errorType
	 *            Tipo de error
	 * @param message
	 *            Mensaje que describe la excepción.
	 * @param cause
	 *            Excepción que a su vez ha provocado esta otra excepción a
	 *            crear.
	 * @param enableSuppression
	 *            Indica si se permite suprimir excepciones de la pila de la
	 *            excepción.
	 * @param writableStackTrace
	 *            Indica si se permite imprimir la pila de la excepción.
	 */
	public LibcommException(final CommErrorType errorType, final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		arguments = new LinkedHashMap<String, Object>();
		this.errorType = errorType;
	}
	
	/**
	 * Añade un argumento a la excepción.
	 * 
	 * @param argumentKey
	 *            Clave de texto que identifica el argumento.
	 * @param argumentValue
	 *            Valor del argumento.
	 */
	public void addArgument (final String argumentKey, final Object argumentValue) {
		arguments.put(argumentKey, argumentValue);
	}
	
	/**
	 * Obtiene un argumento a partir de su clave.
	 * 
	 * @param argumentKey
	 *            Clave del argumento.
	 * @return Valor del argumento, <code>null</code> si no existe.
	 */
	public Object getArgument (final String argumentKey) {
		return arguments.get(argumentKey);
	}
	
	/**
	 * Obtiene una copia de todos los argumentos.
	 * 
	 * @return Copia de los argumentos, vacío si no hay ninguno.
	 */
	public LinkedHashMap<String, Object> getArguments() {
		return new LinkedHashMap<String, Object>(arguments);
	}
	
	public CommErrorType getErrorType () {
		return this.errorType;
	}
	
	/**
	 * Imprime los argumentos según el formato {@value #PRINT_ARGUMENT_FORMAT}.
	 * 
	 * @return Cadena de texto con los argumentos de la excepción.
	 */
	public String printArguments () {
		final StringBuilder sb = new StringBuilder();
		
		int i = 0;
		for (final Entry<String, Object> entry : arguments.entrySet()) {
			sb.append(PrintUtils.format(PRINT_ARGUMENT_FORMAT, i, entry.getKey(), entry.getValue()));
		}
		return sb.toString();
	}
}
