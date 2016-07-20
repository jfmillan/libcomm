package libcomm.exception;

import libcomm.message.FieldMetaData;
import libcomm.message.MessageType;

import commons.util.PrintUtils;

/**
 * Excepción por mensaje mal formado.
 * 
 * <p>
 * 24/01/2016 02:05:23
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class MalformedMessageException extends CommunicationException {

	private static final long serialVersionUID = 2855637893542391597L;

	/**
	 * Constructor de clase.
	 * 
	 * @param errorType
	 *            Tipo de error.
	 */
	public MalformedMessageException(final CommErrorType errorType) {
		super(errorType);
	}

	/**
	 * Constructor de clase.
	 * 
	 * @param errorType
	 *            Tipo de error
	 * @param message
	 *            Mensaje que describe la excepción.
	 */
	public MalformedMessageException(final CommErrorType errorType, String message) {
		super(errorType, message);
	}

	/**
	 * Constructor de clase.
	 * @param errorType
	 *            Tipo de error
	 * @param cause
	 *            Excepción que a su vez ha provocado esta otra excepción a
	 *            crear.
	 */
	public MalformedMessageException(final CommErrorType errorType, Throwable cause) {
		super(errorType, cause);
	}

	/**
	 * Constructor de clase.
	 * @param errorType
	 *            Tipo de error
	 * @param message
	 *            Mensaje que describe la excepción.
	 * @param cause
	 *            Excepción que a su vez ha provocado esta otra excepción a
	 *            crear.
	 */
	public MalformedMessageException(final CommErrorType errorType, String message, Throwable cause) {
		super(errorType, message, cause);
	}

	/**
	 * Constructor de clase.
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
	public MalformedMessageException(final CommErrorType errorType, String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(errorType, message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Crea una excepción a partir de un tipo, un contenido y otra excepción.
	 * @param errorType
	 *            Tipo de error
	 * @param messageType
	 *            Tipo de mensaje.
	 * @param content
	 *            Contenido del mensaje.
	 * @param cause
	 *            Excepción origen del error.
	 * @return Excepción de mensaje mal formado.
	 */
	public static MalformedMessageException createException (final CommErrorType errorType, final MessageType messageType, final String content, final Throwable cause) {
		final String errorMsg = PrintUtils.format("Mensaje '%s' no puede ser construido a partir de '%s'", messageType, content);
		final MalformedMessageException exception = new MalformedMessageException(errorType, errorMsg, cause);
		exception.addArgument("messageType", messageType);
		exception.addArgument("content", content);
		
		return exception;
	}
	
	/**
	 * Crea una excepción a partir de metadatos, un contenido y otra excepción.
	 * @param errorType
	 *            Tipo de error
	 * @param metaData
	 *            Metadatos con información del campo.
	 * @param toParse
	 *            Contenido que se intentaba utilizar para crear el campo.
	 * @param cause
	 *            Excepción origen del error.
	 * @return Excepción de mensaje mal formado.
	 */
	public static MalformedMessageException createException (final CommErrorType errorType, final FieldMetaData metaData, final String toParse, final Throwable cause) {
		final String errorMsg = 
			PrintUtils.format("Campo '%s' de tipo '%s' malformado a partir de '%s'", metaData, metaData.getType(), toParse);
		final MalformedMessageException exception = new MalformedMessageException(errorType, errorMsg, cause);
		exception.addArgument("metaData", metaData);
		exception.addArgument("toParse", toParse);
		
		return exception;
	}
}
