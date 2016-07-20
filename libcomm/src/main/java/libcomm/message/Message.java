package libcomm.message;

import libcomm.exception.MalformedMessageException;
import libcomm.message.rfc1006.IBytes;
import libcomm.util.MessageUtils;

import commons.log.Log;
import commons.util.PrintUtils;

/**
 * Representa un mensaje de protocolo ficticio con su número de secuencia,
 * tipo y contenido.
 * <p>
 * 15/01/2016 20:52:32
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public abstract class Message<C extends IMessageContent> implements IBytes {
	/* Formato para impresión del mensaje: número de secuencia + tipo + contenido. */
	private final static String PRINT_FORMAT = "%s%s%s";
	
	/* Número de secuencia. */
	private SequenceNumber sequenceNumber;
	
	/* Tipo de mensaje*/
	private final MessageType type;
	
	/* Contenido */
	private final C content;
	
	/* Constructor con visibilidad de paquete, se llamará desde un builder o factoría. */
	Message(final MessageType type, final C content) {
		this.type = type;
		this.content = content;
	}
	
	/**
	 * Obtiene el tipo de mensaje.
	 * 
	 * @return Tipo de mensaje.
	 */
	public MessageType getMessageType() {
		return type;
	}
	
	/**
	 * Obtiene los campos con el contenido. *
	 * 
	 * @return Contenido del mensaje.
	 */
	C getContent () {
		return content;
	}
	
	/**
	 * Establece un número de secuencia.
	 * 
	 * @param sequenceNumber
	 *            Número de secuencia.
	 */
	public void setSequenceNumber (final SequenceNumber sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	
	/**
	 * Obtiene el número de secuencia.
	 * 
	 * @return Número de secuencia.
	 */
	public SequenceNumber getSequenceNumber () {
		return this.sequenceNumber;
	}

	/**
	 * Obtiene una representación del objeto como array de bytes unidimensional.
	 * 
	 * @return Array de bytes unidimiensional que representa el objeto.
	 */
	@Override
	public byte[] getBytes() {
		byte[] result;
		try {
			result = MessageUtils.encode(printMessage());
		} catch (MalformedMessageException e) {
			Log.error(this.getClass(), 
				PrintUtils.format("Error al codificar el mensaje para '%s'", e.printArguments()), e );
			result = null;
		}
		return result;
	}

	/**
	 * Imprime el mensaje: cabecera + tipo de mensaje + contenido. Los campos
	 * vacíos o parciales se completan con caracter <code>*</code> definido en
	 * {@link MessageUtils#MESSAGE_PAD}
	 * 
	 * @return Mensaje impreso.
	 */
	public String printMessage() {
		return PrintUtils.format(PRINT_FORMAT,
			MessageUtils.printForMessage(sequenceNumber),
			MessageUtils.printForMessage(type),
			MessageUtils.printForMessage(content));
	}
	
	/**
	 * Sobrecarga de {@link Object#toString()}.
	 * 
	 * @return Cadena que representa el objeto.
	 */
	@Override
	public String toString() {
		return printMessage();
	}
}
