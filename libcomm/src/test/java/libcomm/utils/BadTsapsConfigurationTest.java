package libcomm.utils;

import java.util.Properties;

import libcomm.connection.ConnectionMode;
import libcomm.context.ConnectionProperties;

import commons.util.PrintUtils;

/**
 * Clase de configuración para utilizar en tests con TSAPs incorrectos.
 * <p>
 * 14/03/2016 21:51:40
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class BadTsapsConfigurationTest {

	private static final String CALLING_TSAP_CLIENT = "SG-bad-SCF";
	private static final String CALLED_TSAP_CLIENT = "SCF-bad-SG";
	
	private final Properties properties;
	private final String name;
	
	BadTsapsConfigurationTest(final String name, final ConnectionMode mode) {
		this.name = name;
		this.properties = getCommonProperties(mode);
	}
	
	private Properties getCommonProperties(final ConnectionMode mode) {
		final Properties prop = new Properties();
		
		prop.setProperty(ConnectionProperties.KEY_CONNECTION_NAME, PrintUtils.format("Test-%s[%s]", this.name, mode));
		prop.setProperty(ConnectionProperties.KEY_CONNECTION_HOST, "localhost");
		prop.setProperty(ConnectionProperties.KEY_CONNECTION_PORT, "102");
		prop.setProperty(ConnectionProperties.KEY_CONNECTION_TIMEOUT_MILLIS, "5000");
		prop.setProperty(ConnectionProperties.KEY_CONNECTION_MODE, mode.toString());
		prop.setProperty(ConnectionProperties.KEY_CONNECTION_CALLING_TSAP, CALLING_TSAP_CLIENT); 
		prop.setProperty(ConnectionProperties.KEY_CONNECTION_CALLED_TSAP, CALLED_TSAP_CLIENT);
		prop.setProperty(ConnectionProperties.KEY_CONNECTION_AK_ENABLED, "true");
		
		return prop;
	}
	
	Properties getProperties () {
		return properties;
	}
}
