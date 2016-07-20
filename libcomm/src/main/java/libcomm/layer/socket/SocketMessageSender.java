package libcomm.layer.socket;

import libcomm.layer.ISender;
import libcomm.message.rfc1006.IBytes;

/**
 * Procesador de mensajes a nivel socket que se encarga de enviarlos.
 * <p>
 * 13/03/2016 00:59:24
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class SocketMessageSender implements IMessageProcessor<IBytes> {

	/* Permite enviar el mensaje. */
	private final ISender<IBytes> sender;

	/**
	 * Constructor de clase.
	 * 
	 * @param sender
	 *            Interfaz que enviará los mensajes procesados.
	 */
	SocketMessageSender(final ISender<IBytes> sender) {
		this.sender = sender;
	}

	/**
	 * Procesa el mensaje, enviándolo.
	 * 
	 * @param message
	 *            Mensaje a procesar.
	 */
	@Override
	public void process(final IBytes message) {
		this.sender.send(message);
	}
}
