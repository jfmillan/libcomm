package libcomm.layer.socket;

import libcomm.layer.IReceiver;
import libcomm.message.rfc1006.IBytes;

/**
 * Procesador de mensajes a nivel socket que se encarga de recibirlos.
 * <p>
 * 13/03/2016 01:05:14
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class SocketMessageReceiver implements IMessageProcessor<IBytes> {

	/* Permite recibir el mensaje. */
	private final IReceiver<IBytes> receiver;

	/**
	 * Constructor de clase.
	 * 
	 * @param receiver
	 *            Interfaz que va a recibir los mensajes procesados.
	 */
	SocketMessageReceiver(final IReceiver<IBytes> receiver) {
		this.receiver = receiver;
	}

	/**
	 * Procesa el mensaje, recibiéndolo.
	 * 
	 * @param message
	 *            Mensaje a procesar.
	 */
	@Override
	public void process(final IBytes message) {
		this.receiver.receive(message);
	}
}
