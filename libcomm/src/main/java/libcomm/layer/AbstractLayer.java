package libcomm.layer;

/**
 * Clase abstracta que representa una capa de comunicaciones. Implementa métodos
 * de observador de capa, incluyendo el tipo de mensaje recibido que notifica a
 * otras capas. También implementa métodos de comandos a otras capas, incluyendo
 * el tipo de mensaje enviado.
 * <p>
 * 21/02/2016 15:25:47
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */							/* RM, received message; SM, sent message */
public abstract class AbstractLayer<RM, SM> implements ILayerObserver<RM>, ILayerCommands<SM> {
}
