package libcomm.utils;

import libcomm.connection.ConnectionMode;
import libcomm.context.ConnectionContext;
import libcomm.exception.CommunicationException;

/**
 * Inicia un servidor o cliente con Libcomm, según el modo que se le indique,
 * con TSPAs incorrectos respecto a {@link CommTest}.
 * 
 * <p>
 * 29/02/2016 21:01:12
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class BadTsapsCommTest extends AbstractCommTest {
	/* Constructor, recibe el modo: cliente o servidor */
	public BadTsapsCommTest(final String name, final ConnectionMode mode) throws CommunicationException {
		super (ConnectionContext.createContext(new BadTsapsConfigurationTest(name, mode).getProperties()));
	}
}
