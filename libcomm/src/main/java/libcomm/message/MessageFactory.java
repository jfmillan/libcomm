package libcomm.message;

import libcomm.exception.MalformedMessageException;
import libcomm.message.rfc1006.DataTsdu;

/**
 * Factoría de mensajes. Provee métodos estáticos para crear mensajes a
 * partir de distintas entradas.
 * <p>
 * 16/01/2016 18:19:54
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class MessageFactory {

	/**
	 * Obtiene un mensaje vacío con número de secuencia y un tipo determinado.
	 * 
	 * @param sequenceNumber
	 *            Número de secuencia.
	 * @param type
	 *            Tipo del mensaje, {@link MessageType}.
	 * @return Mensaje vacío.
	 */
	@SuppressWarnings("unchecked")
	public static <M extends Message<?>> M getMessage (final SequenceNumber sequenceNumber, final MessageType type) {
		final M message = (M) type.getBuilder().buildEmptyMessage();
		message.setSequenceNumber(sequenceNumber);
		return message;
	}
	
	/**
	 * Obtiene un mensaje vacío sin número de secuencia, sólo tipo.
	 * 
	 * @param type
	 *            Tipo del mensaje, {@link MessageType}.
	 * @return Mensaje vacío.
	 */
	public static <M extends Message<?>> M getMessage (final MessageType type) {
		return MessageFactory.getMessage(new SequenceNumber(), type);
	}
	
	/**
	 * Obtiene un mensaje decodificandolo a partir de un {@link DataTsdu}. El
	 * mensaje debería proceder de un canal de comunicaciones, en cuyo caso
	 * llegará completo: con número de secuencia, tipo de mensaje y contenido.
	 * <p>
	 * <b>Importante:</b> La secuencia de bytes se corresponde únicamente al
	 * mensaje, no incluyendo las cabeceras TPKT ni del TPDU de RFC1006.
	 * 
	 * @param dataTsdu
	 *            TSDU con datos (bytes) que conforman el mensaje.
	 * @return Objeto con el mensaje construido.
	 * @throws MalformedMessageException Si el mensaje está mal formado. 
	 */
	@SuppressWarnings("unchecked")
	public static <M extends Message<?>> M getMessage (final DataTsdu dataTsdu) throws MalformedMessageException {
		final DataTsduReader reader = new DataTsduReader(dataTsdu);
		reader.read();
		
		/* Recopilamos las tres partes de un mensaje */
		final SequenceNumber sequenceNumber = reader.getSequenceNumber();
		final MessageType type = reader.getMessageType();
		final String content = reader.getContentMessage();
		
		/* Formamos y devolvemos el mensaje. */
		return (M) type.getBuilder().buildMessageFromContent(sequenceNumber, content);
	}
}
