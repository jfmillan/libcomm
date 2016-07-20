package libcomm.exception;


/**
 * 
 * <p>
 * 09/01/2016 20:55:54
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class MandatoryContextFailureException extends CommunicationException {

	private static final long serialVersionUID = 5663681902290188482L;
	
	private static final CommErrorType ERROR_TYPE = CommErrorType.CONFIGURATION;

	/** Constructor sin argumentos. */
	public MandatoryContextFailureException() {
		super(ERROR_TYPE);
	}

	/**
	 * Constructor con un mensaje.
	 * 
	 * @param message
	 *            Mensaje que describe la excepción.
	 */
	public MandatoryContextFailureException(String message) {
		super(ERROR_TYPE, message);
	}

	/**
	 * Constructor con otra excepción de la que partir.
	 * 
	 * @param cause
	 *            Excepción que a su vez ha provocado esta otra excepción a
	 *            crear.
	 */
	public MandatoryContextFailureException(Throwable cause) {
		super(ERROR_TYPE, cause);
	}

	/**
	 * Constructor con mensaje y motivo.
	 * 
	 * @param message
	 *            Mensaje que describe la excepción.
	 * @param cause
	 *            Excepción que a su vez ha provocado esta otra excepción a
	 *            crear.
	 */
	public MandatoryContextFailureException(String message, Throwable cause) {
		super(ERROR_TYPE, message, cause);
	}

	/**
	 * Constructor con mensaje y motivo.
	 * 
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
	public MandatoryContextFailureException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(ERROR_TYPE, message, cause, enableSuppression, writableStackTrace);
	}
}
