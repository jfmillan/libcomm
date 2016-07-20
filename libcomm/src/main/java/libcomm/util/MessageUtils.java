package libcomm.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import libcomm.exception.CommErrorType;
import libcomm.exception.MalformedMessageException;
import libcomm.message.Field;
import libcomm.message.FieldMetaData;
import libcomm.message.IPrintForMessage;
import libcomm.message.Message;
import libcomm.message.MessageType;
import libcomm.message.parser.FieldParser;
import libcomm.message.rfc1006.IBytes;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import commons.util.PrintUtils;
import commons.util.StrUtils;

/**
 * Utilidades para trabajar con mensajes.
 * <p>
 * 15/01/2016 22:59:18
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class MessageUtils {
	
	/** Constant para caracter de relleno en mensajes. */
	public static final Character MESSAGE_PAD = '*';
	
	/** Char-set utilizado para codificar y decodificar los bytes y mensajes */
	public static final String CHARSET = "UTF-8";
	
	/* Transforma un array de caracteres en bytes, según codificación establecida en <code>CHARSET</code>. */
	private static CharsetEncoder encoder;
	
	/* Transforma un array de bytes en caracteres, según codificación establecida en <code>CHARSET</code>. */
	private static CharsetDecoder decoder;
	
	/* Bloqueo para codificador */
	private static final ReentrantLock encoderLock = new ReentrantLock();

	/* Bloqueo para decodificador */
	private static final ReentrantLock decoderLock = new ReentrantLock();
	
	static {
		encoder = Charset.forName(CHARSET).newEncoder();
		decoder = Charset.forName(CHARSET).newDecoder();
	}


	/**
	 * Devuelve una cadena de texto para imprimir un mensaje o una parte. Si se
	 * indica una longitud mínima y no se cumple, se rellenará con el caracter
	 * por defecto <code>*</code> definido en {@link MessageUtils#MESSAGE_PAD}.
	 * 
	 * @param message
	 *            Mensaje total o parcial a imprimir.
	 * @param minLength
	 *            Longitud mínima.
	 * @return Resultado listo para mostrar en pantalla.
	 */
	public static String printForMessage (final IPrintForMessage message, final Integer minLength) {
		final String print = message != null ? message.printForMessage() : StrUtils.EMPTY_STRING;
		return minLength != null ? 
			StringUtils.rightPad(print, minLength, MessageUtils.MESSAGE_PAD)
			: print;
	}

	
	/**
	 * Devuelve una cadena de texto para imprimir un mensaje o una parte. Es
	 * equivalente a llamar a
	 * {@link #printForMessage(IPrintForMessage, Integer)} con una longitud
	 * mínima <code>null</code>.
	 * 
	 * @param message
	 *            Mensaje total o parcial a imprimir.
	 * @return Resultado listo para mostrar en pantalla.
	 */
	public static String printForMessage (final IPrintForMessage message) {
		return printForMessage(message, null);
	}
	
	
	/**
	 * Codifica un mensaje o una parte, obteniendo un array de bytes
	 * codificados.
	 * 
	 * @param message
	 *            Mensaje a codificar.
	 * @return Array de bytes que representan el mensaje codificado.
	 * @throws MalformedMessageException en caso de error.
	 */
	public static byte[] encode (final String message) throws MalformedMessageException {
		final byte[] encodedBytes;
		try {
			if (StrUtils.hasChars(message, Boolean.TRUE)) {
				final char [] chars = message.toCharArray();
				
				ByteBuffer byteBuffer = null;
				final CharBuffer charBuffer = CharBuffer.wrap(chars);
				try {
					encoderLock.lock();
					byteBuffer = encoder.encode(charBuffer);
				} finally {
					encoderLock.unlock();
				}
				
				encodedBytes = BufferUtils.readFromBuffer(byteBuffer, Boolean.FALSE);
			} else {
				encodedBytes = ArrayUtils.EMPTY_BYTE_ARRAY;
			}
		} catch (Throwable t) {
			final MalformedMessageException e = new MalformedMessageException(
				CommErrorType.ENCODE, PrintUtils.format("Error al codificar mensaje '%s'", message), t
			);
			e.addArgument("Message", message);
			throw e;
		}
		return encodedBytes;
	}
	
	/**
	 * Decodifica un mensaje o una parte, obteniendo un {@link String} con el
	 * mensaje decodificado según {@value #CHARSET}.
	 * 
	 * @param bytes
	 *            Bytes a decodificar.
	 * @return String con el texto decodificado.
	 * @throws MalformedMessageException en caso de error.
	 */
	public static String decode (final IBytes bytes) throws MalformedMessageException {
		return bytes != null ? decode(bytes.getBytes()) : StrUtils.EMPTY_STRING;
	}
	
	/**
	 * Decodifica un mensaje o una parte, obteniendo un {@link String} con el
	 * mensaje decodificado según {@value #CHARSET}.
	 * 
	 * @param bytes
	 *            Bytesa decodificar.
	 * @return String con el texto decodificado.
	 * @throws MalformedMessageException en caso de error.
	 */
	public static String decode (final byte[] bytes) throws MalformedMessageException {
		final String decodedResult;
		try {
			if (bytes != null && bytes.length > 0) {
				final ByteBuffer byteBuffer = BufferUtils.writeInToBuffer(bytes, Boolean.TRUE);
				CharBuffer charBuffer = null;
				
				try {
					decoderLock.lock();
					charBuffer = decoder.decode(byteBuffer);
				} finally {
					decoderLock.unlock();
				}
				
				decodedResult = charBuffer.toString();
			} else {
				decodedResult = StrUtils.EMPTY_STRING;
			}
		} catch (Throwable t) {
			final MalformedMessageException e = new MalformedMessageException(
				CommErrorType.DECODE, PrintUtils.format("Error al decodificar bytes '%s'", PrintUtils.print(bytes)), t
			);
			e.addArgument("Bytes", bytes);
			throw e;
		}
		return decodedResult;
	}
	
	/**
	 * Comprueba un mensaje asegurandose que su formato es correcto. En caso de
	 * mensaje incorrecto lanza una excepción por mensaje mal formado.
	 * 
	 * @param message
	 *            Mensaje a comprobar.
	 * @throws MalformedMessageException
	 *             En caso de mensaje incorrecto.
	 */
	public static void check(final Message<?> message) throws MalformedMessageException {
		final String messageStr = message != null ? message.printMessage() : StrUtils.EMPTY_STRING;
		if (!StrUtils.hasChars(messageStr, Boolean.TRUE)) {
			throw new MalformedMessageException (CommErrorType.PARSE_MESSAGE, 
				PrintUtils.format("Mensaje '%s' con formato incorrecto", messageStr)
			);
		}
		check(messageStr);
	}
	/**
	 * Comprueba un mensaje recibido como String, asegurandose que su formato es correcto. En caso de
	 * mensaje incorrecto lanza una excepción por mensaje mal formado.
	 * 
	 * @param message
	 *            Mensaje a comprobar, como String.
	 * @throws MalformedMessageException
	 *             En caso de mensaje incorrecto.
	 */
	public static void check(final String message) throws MalformedMessageException {
		final Pattern pattern = Regex.MESSAGE.getCompiledPattern();
		final Matcher matcher = pattern.matcher(message);
		
		final String messageError = PrintUtils.format("Mensaje '%s' con formato incorrecto", message);
		
		if (!matcher.matches() || matcher.groupCount() != 3) { /* Un mensaje válido debe tener número de secuencia, tipo y contenido */
			throw new MalformedMessageException(CommErrorType.PARSE_MESSAGE, messageError);
		}
		
		/* Número de secuencia, puede ser nulo o vacío. */
		FieldParser.parse(FieldMetaData.SEQUENCE_NUMBER, matcher.group(1));
		
		/* Tipo de mensaje. */
		final Field<String> messageTypeField = FieldParser.parse(FieldMetaData.MESSAGE_TYPE, matcher.group(2));
		final String msgType = messageTypeField.getValue();
		if (!StrUtils.hasChars(msgType, Boolean.TRUE)) {
			throw new MalformedMessageException(CommErrorType.PARSE_MESSAGE_FIELD,
				PrintUtils.format("%s. Tipo de mensaje incorrecto: %s", messageError, msgType)
			);
		}
		
		/* Contenido del mensaje */
		final String contentStr = matcher.group(3);
		final MessageType type = MessageType.valueOf(msgType);
		
		String regex = Regex.VALID_CONTENT.getRegex();
		if (MessageType.PR.equals(type)) {
			regex = Regex.MESSAGE_CONTENT_PR.getRegex();
		} else if (MessageType.GT.equals(type)) {
			regex = Regex.MESSAGE_CONTENT_GT.getRegex();
		} else if (MessageType.ST.equals(type)) {
			regex = Regex.MESSAGE_CONTENT_ST.getRegex();
		} else if (MessageType.AK.equals(type)) { 
			/* en este punto no deberíamos recibir AK pero lo tratamos igualmente */
			regex = Regex.MESSAGE_CONTENT_AK.getRegex();
		}
		
		if (!Pattern.matches(regex, contentStr)) {
			throw new MalformedMessageException(CommErrorType.PARSE_MESSAGE_FIELD,
				PrintUtils.format("%s. Contenido incorrecto: %s", messageError, contentStr)
			);	
		}
	}
}
