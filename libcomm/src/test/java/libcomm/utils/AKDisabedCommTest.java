package libcomm.utils;

import libcomm.connection.ConnectionMode;
import libcomm.context.ConnectionContext;
import libcomm.exception.CommunicationException;

/**
 * Inicia un servidor o cliente con Libcomm, según el modo que se le indique, con AK desactivado.
 * 
 * <p>
 * 19/03/2016 17:35:32
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class AKDisabedCommTest extends AbstractCommTest {
	
	/* Constructor, recibe el modo: cliente o servidor */
	public AKDisabedCommTest(final String name, final ConnectionMode mode) throws CommunicationException {
		super (ConnectionContext.createContext(new AKDisabledConfigurationTest(name, mode).getProperties()));
	}
}