package libcomm.exception;

import libcomm.connection.IConnection;

/**
 * Excepciones producidas por eventos de comunicación.
 * <p>
 * 06/01/2016 17:15:08
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class CommunicationException extends LibcommException {
	private static final long serialVersionUID = -3533842508928271320L;

	/**
	 * Constructor.
	 * 
	 * @param errorType
	 *            Tipo de error
	 */
	public CommunicationException(final CommErrorType errorType) {
		super(errorType);
	}

	/**
	 * Constructor con un mensaje.
	 * 
	 * @param errorType
	 *            Tipo de error
	 * @param message
	 *            Mensaje que describe la excepción.
	 */
	public CommunicationException(final CommErrorType errorType, final String message) {
		super(errorType, message);
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
	public CommunicationException(final CommErrorType errorType, final Throwable cause) {
		super(errorType, cause);
	}

	/**
	 * Constructor con mensaje y motivo.
	 * 
	 * @param errorType
	 *            Tipo de error
	 * 
	 * @param message
	 *            Mensaje que describe la excepción.
	 * @param cause
	 *            Excepción que a su vez ha provocado esta otra excepción a
	 *            crear.
	 */
	public CommunicationException(final CommErrorType errorType, final String message, final Throwable cause) {
		super(errorType, message, cause);
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
	public CommunicationException(final CommErrorType errorType, final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(errorType, message, cause, enableSuppression, writableStackTrace);
	}
	
	/**
	 * Crea una excepción a partir de un mensaje y una conexión que se añade
	 * como argumento a la cexcepción. También permite indicar un motivo.
	 * 
	 * @param errorType
	 *            Tipo de error
	 * @param message
	 *            Mensaje que describe la excepción.
	 * @param connection
	 *            Datos de la conexión que produjo la excepción.
	 * @param cause
	 *            Excepción que a su vez ha provocado esta otra excepción a
	 *            crear.
	 * @return Excepción de comunicaciones creada.
	 */
	public static CommunicationException createException (final CommErrorType errorType, final String message, final IConnection connection, final Throwable cause) {
		final CommunicationException error = new CommunicationException(errorType, message, cause);
		if (connection != null) {
			error.addArgument("Conexión", connection);
		}
		return error;
	}
	
	/**
	 * Crea una excepción a partir de un mensaje y una conexión que se añade
	 * como argumento a la cexcepción. También permite indicar un motivo.
	 * 
	 * @param errorType
	 *            Tipo de error
	 * @param message
	 *            Mensaje que describe la excepción.
	 * @param connection
	 *            Datos de la conexión que produjo la excepción.
	 * @return Excepción de comunicaciones creada.
	 */
	public static CommunicationException createException (final CommErrorType errorType, final String message, final IConnection connection) {
		return CommunicationException.createException(errorType, message, connection, null);
	}
}
