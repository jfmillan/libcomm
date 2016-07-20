package libcomm.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import libcomm.exception.CommErrorType;
import libcomm.exception.MalformedMessageException;
import libcomm.message.parser.FieldParser;
import libcomm.message.rfc1006.DataTsdu;
import libcomm.util.MessageUtils;
import libcomm.util.Regex;

import commons.util.ColUtils;
import commons.util.PrintUtils;

/**
 * Lee un {@link DataTsdu}, valida que el mensaje está bien formado según el
 * formato <code>SSSSTTCCC...</code>, donde SSSS son los 4 dígitos del número de
 * secuencia, TT son el tipo, y CCC... es el contenido variable del mensaje. El
 * formato correcto concreto de cada mensaje se valida en el constructor de cada
 * mensaje.
 * <p>
 * 17/01/2016 13:37:20
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class DataTsduReader {
	
	/* TSDU con datos a leer. */
	private final DataTsdu dataTsdu;
	
	/* Número de secuencia obtenio. */
	private SequenceNumber sequenceNumber;

	/* Tipo de mensaje. */
	private MessageType messageType;

	/* Contenido de mensaje sin tratar */
	private String contentMessage;
	
	/**
	 * Constructor de clase.
	 * 
	 * @param dataTsdu
	 *            TSDU con datos a leer.
	 */
	DataTsduReader(final DataTsdu dataTsdu) {
		this.dataTsdu = dataTsdu;
	}
	
	/**
	 * Lee la secuencia de bytes del TSDU de datos, comprobando que se trate de
	 * un mensaje válido con su número de secuencia, tipo, y contenido. Genera
	 * un número de secuencia, un tipo de mensaje y un contenido.
	 * 
	 * @throws MalformedMessageException
	 *             en caso de error.
	 */
	void read() throws MalformedMessageException {
		final String message = MessageUtils.decode(dataTsdu.getBody());
		parseMessage(message);
	}

	/**
	 * Analiza el mensaje y extrae de él su número de secuencia, su tipo, y su
	 * contenido sin tratar, que será procesado por un constructor específico
	 * según el tipo de mensaje obtenido.
	 * 
	 * @param message
	 *            Mensaje en forma de {@link String}.
	 * @throws MalformedMessageException
	 *             Si el mensaje está mal formado.
	 */
	private void parseMessage(final String message) throws MalformedMessageException {
		final Pattern pattern = Regex.MESSAGE.getCompiledPattern();
		final Matcher matcher = pattern.matcher(message);
		
		if (!matcher.matches() || matcher.groupCount() != 3) { /* Un mensaje válido debe tener número de secuencia, tipo y contenido */
			throw createException(CommErrorType.PARSE_MESSAGE, message, null);
		}
		
		try {
			final Field<String> sequenceNumberField = FieldParser.parse(FieldMetaData.SEQUENCE_NUMBER, matcher.group(1));
			final Field<String> messageTypeField = FieldParser.parse(FieldMetaData.MESSAGE_TYPE, matcher.group(2));
		
			this.sequenceNumber = new SequenceNumber(sequenceNumberField.getValue());
			this.messageType = MessageType.valueOf(messageTypeField.getValue());
			
			final String contentStr = matcher.group(3);
			if (Pattern.matches(Regex.VALID_CONTENT.getRegex(), contentStr)) {
				this.contentMessage = contentStr;
			}

			ColUtils.requireNonNull(this.sequenceNumber, this.messageType, this.contentMessage);
			
		} catch (Exception e) {
			throw createException(CommErrorType.PARSE_MESSAGE, message, e);
		}
	}
	
	/**
	 * Crea una excepción por mensaje mal formado.
	 * 
	 * @param message
	 *            Mensaje que genera la excepción.
	 * @param cause
	 *            Motivo de la excepción, opcional.
	 * @return Excepción por mensaje mal formado.
	 */
	private MalformedMessageException createException(final CommErrorType errorType, final String message, final Throwable cause) {
		final MalformedMessageException exception = new MalformedMessageException(
			errorType, PrintUtils.format("Error al intentar formar un mensaje a partir de '%s'", message), cause
		);
		
		exception.addArgument("message", message);
		return exception;
	}

	/**
	 * Devuelve el parámetro indicado.
	 * @return parámetro sequenceNumber a devolver.
	 */
	SequenceNumber getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * Devuelve el parámetro indicado.
	 * @return parámetro messageType a devolver.
	 */
	MessageType getMessageType() {
		return messageType;
	}

	/**
	 * Devuelve el parámetro indicado.
	 * @return parámetro contentMessage a devolver.
	 */
	String getContentMessage() {
		return contentMessage;
	}
}
