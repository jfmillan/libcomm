package libcomm.context;

import java.util.Properties;

import libcomm.connection.ConnectionMode;
import libcomm.exception.CommErrorType;
import libcomm.exception.CommunicationException;
import libcomm.exception.MandatoryContextFailureException;

import commons.exception.TypeConvertException;
import commons.log.Log;
import commons.util.PrintUtils;
import commons.util.StrUtils;
import commons.util.TypeConverter;

/**
 * Clase capaz de leer y almacenar las propiedades de configuración de una
 * conexión.
 * <p>
 * 09/01/2016 20:31:49
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class ConnectionProperties  {
	
	/* Clave para host */
	public static final String KEY_CONNECTION_HOST = "host";
	
	/* Clave para puerto */
	public static final String KEY_CONNECTION_PORT = "port";
	
	/* Clave para nombre */
	public static final String KEY_CONNECTION_NAME = "name";
	
	/* Clave para timeout en milisegundos */
	public static final String KEY_CONNECTION_TIMEOUT_MILLIS = "timeout.millis";
	
	/* Clave para modo de conexión, cliente o servidor */
	public static final String KEY_CONNECTION_MODE = "mode";
	
	/* Clave para <i>calling TSAP</i> de RFC1006 */
	public static final String KEY_CONNECTION_CALLING_TSAP = "calling.tsap";
	
	/* Clave para <i>called TSAP</i> de RFC1006 */
	public static final String KEY_CONNECTION_CALLED_TSAP = "called.tsap";

	/* Clave para AK activado/desactivado */
	public static final String KEY_CONNECTION_AK_ENABLED = "ak.enabled";
	
	/* Host por defecto, no es obligatorio porque en modo servidor no es necesario.*/
	private static final String DEFAULT_CONNECTION_HOST = "localhost";
	
	/* Puerto de conexión por defecto, 102 según RFC1006 */
	private static final int DEFAULT_CONNECTION_PORT = 102;

	/* Nombre por defecto para la conexión. */
	private static final String DEFAULT_CONNECTION_NAME = "Libcomm";

	/* Milisegundos de espera para establecer una conexión. */
	private static final long DEFAULT_CONNECTION_TIMEOUT = 20000;

	/* Modo por defecto para AK, activado. */
	private static final boolean DEFAULT_AK_ENABLED = Boolean.TRUE;
	
	/* Host/ip de conexión, sólo es necesaria en modo cliente. */
	private String host;
	
	/* Puerto de conexión. */
	private int port;

	/* Nombre de la conexión. */
	private String connectionName;

	/* Timeout de conexion. */
	private long connectionTimeout;

	/* Modo de conexión. */
	private ConnectionMode connectionMode;

	/* Configuraciones de TSAP */
	private String callingTsap;
	private String calledTsap;
	
	/* AK activado o desactivado. */
	private Boolean akEnabled;
	
	/**
	 * Constructor de clase.
	 * 
	 * @param properties
	 *            Propiedades de la conexión.
	 * 
	 * @throws CommunicationException
	 *             en caso de error.
	 */
	ConnectionProperties(final Properties properties) throws CommunicationException {
		configureHost(properties);
		configurePort(properties);
		configureName(properties);
		configureTimeout(properties);
		configureMode(properties);
		configureTSAPs(properties);
		configureAKEnabled(properties);
	}

	private void configureHost(final Properties properties) throws CommunicationException {
		this.host = getOptionalValue(properties, KEY_CONNECTION_HOST, String.class, DEFAULT_CONNECTION_HOST);
	}
	
	private void configurePort(final Properties properties) throws CommunicationException {
		this.port = getOptionalValue(properties, KEY_CONNECTION_PORT, Integer.class, DEFAULT_CONNECTION_PORT);
	}
	
	private void configureName(final Properties properties) throws CommunicationException {
		this.connectionName = getOptionalValue(properties, KEY_CONNECTION_NAME, String.class, DEFAULT_CONNECTION_NAME);
	}
	
	private void configureTimeout(final Properties properties) throws CommunicationException {
		this.connectionTimeout = 
			getOptionalValue(properties, KEY_CONNECTION_TIMEOUT_MILLIS, Long.class, DEFAULT_CONNECTION_TIMEOUT);
	}
	
	private void configureMode(final Properties properties) throws CommunicationException {
		final String modeStr = getMandatoryValue(properties, KEY_CONNECTION_MODE, String.class);
		this.connectionMode = ConnectionMode.getMode(modeStr);
		if (connectionMode == null) {
			throw getMandatoryContextFailureException(KEY_CONNECTION_MODE, modeStr);
		}
	}
	
	private void configureTSAPs(final Properties properties) throws CommunicationException {
		this.callingTsap = getMandatoryValue(properties, KEY_CONNECTION_CALLING_TSAP, String.class);
		if (callingTsap == null) {
			throw getMandatoryContextFailureException(KEY_CONNECTION_CALLING_TSAP, callingTsap);
		}

		this.calledTsap = getMandatoryValue(properties, KEY_CONNECTION_CALLED_TSAP, String.class);
		if (calledTsap == null) {
			throw getMandatoryContextFailureException(KEY_CONNECTION_CALLED_TSAP, calledTsap);
		}
	}

	private void configureAKEnabled(final Properties properties) throws CommunicationException {
		this.akEnabled = getOptionalValue(properties, KEY_CONNECTION_AK_ENABLED, Boolean.class, DEFAULT_AK_ENABLED);
	}
	
	private <T> T getValue(final Properties properties, final String key, final boolean mandatory, Class<T> clazz) 
			throws MandatoryContextFailureException, TypeConvertException {
		final String stringValue = properties.getProperty(key);
		if (!StrUtils.hasChars(stringValue, Boolean.TRUE) && mandatory) {
			throw getMandatoryContextFailureException(key, stringValue);
		}
		return TypeConverter.from(stringValue, clazz);
	}

	private MandatoryContextFailureException getMandatoryContextFailureException(final String key, final String stringValue) {
		final String error = 
			PrintUtils.format("No se encuentra configuración para parámetro obligatorio '%s', valor '%s'", key, stringValue);
		final MandatoryContextFailureException ex = new MandatoryContextFailureException(error);
		ex.addArgument("Parámetro", key);
		ex.addArgument("Valor", stringValue);
		return ex;
	}
	
	private <T> T getMandatoryValue(final Properties properties, final String key, Class<T> clazz) 
			throws CommunicationException {
		T result = null;
		try {
			result = getValue(properties, key, Boolean.TRUE, clazz);
		} catch (MandatoryContextFailureException e) {
			throw e;
		} catch (TypeConvertException e) {
			throw new CommunicationException(CommErrorType.TYPE_CONVERT, e);
		} catch (Exception e) {
			throw new CommunicationException(CommErrorType.CONFIGURATION, e);
		}
		return result;
	}
	
	private <T> T getOptionalValue(final Properties properties, final String key, Class<T> clazz, final T defaultValue) 
			throws CommunicationException {
		T result = null;
		try {
			result = getValue(properties, key, Boolean.TRUE, clazz);
		} catch (MandatoryContextFailureException e) {
			/* No es obligatorio, no pasa nada, sigue a null */
			result = null;
		} catch (TypeConvertException e) {
			 /* La excepcion TypeConvertException sí se relanza, una cosa es no configurar y otra configurar mal */
			throw new CommunicationException (CommErrorType.TYPE_CONVERT, e);
		}
		
		if (result == null) {
			Log.debug(this, 
				PrintUtils.format("No se ha establecido configuracion para '%s', valor por defecto '%s'", key, defaultValue));
			result = defaultValue; /* Puede seguir siendo null */
		}
		return result;
	}
	
	/* Obtiene el puerto de la conexión. */
	int getPort() {
		return port;
	}

	/* Obtiene el host o IP de conexión. */ 
	String getHost() {
		return host;
	}

	/* Obtiene el nombre de la conexión. */
	String getConnectionName() {
		return connectionName;
	}

	/* Obtiene el tiempo de timeout de conexión en milisegundos.*/
	long getConnectionTimeout() {
		return connectionTimeout;
	}

	/* Obtiene el modo de conexión, cliente o servidor. */
	ConnectionMode getConnectionMode() {
		return connectionMode;
	}
	
	/* Obtiene el parámetro 'calling.tsap' */
	String getCallingTsap() {
		return this.callingTsap;
	}

	/* Obtiene el parámetro 'called.tsap' */
	String getCalledTsap() {
		return this.calledTsap;
	}
	
	/* Indica si AK está habilitado o no. */
	boolean isAKEnabled () {
		return this.akEnabled;
	}
}
