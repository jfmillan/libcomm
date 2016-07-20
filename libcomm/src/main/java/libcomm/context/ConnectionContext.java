package libcomm.context;

import java.io.InputStream;
import java.util.Properties;

import libcomm.connection.ConnectionMode;
import libcomm.connection.IConnection;
import libcomm.exception.CommErrorType;
import libcomm.exception.CommunicationException;

import commons.log.Log;
import commons.util.PrintUtils;

/**
 * Contexto con datos y configuraciones de la conexión.
 * <p>
 * Los datos a configurar son:
 * <ul>
 * <li><code>host</code>, dirección ip o nombre de host de la conexión, sólo
 * necesario en modo cliente. Por defecto <code>localhost</code>.
 * <li><code>port</code>, puerto de conexión, por defecto 102.
 * <li><code>name</code>, nombre de la conexión, sólo a efectos identificativos
 * y de log.
 * <li><code>mode</code>Modo de conexión: servidor (espera conexiones de un
 * cliente) o cliente (intenta conectar a un servidor).
 * <li><code>timeout.millis</code>, milisegundos de espera antes de que se
 * provoque un timeout de conexión, por defecto 20 segundos (20000
 * milisegundos).
 * <li><code>calling.tsap</code>, configuración de RFC1006, identifica el punto
 * de conexión local.
 * <li><code>called.tsap</code>, configuración de RFC1006, identifica el punto
 * de conexión remoto.
 * </ul>
 * <p>
 * 09/01/2016 20:28:42
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class ConnectionContext implements IConnection {

	/* Propiedades de conexión. */
	private ConnectionProperties connectionProperties;
	

	/* Constructor privado, el objeto debe construirse llamando a createContext(Properties) */
	private ConnectionContext() {
	}
	
	/**
	 * Obtiene el contexto de la conexión creado a partir de un
	 * {@link Properties}.
	 * 
	 * @param file
	 *            Ruta del fichero {@link Properties}.
	 * @return Contexto de conexión.
	 * @throws CommunicationException en caso de error.
	 */
	public static ConnectionContext createContext (final String file) throws CommunicationException {
		final Properties properties = new Properties();
		InputStream fileStream = null;
		try {
			fileStream = ConnectionContext.class.getClassLoader().getResourceAsStream(file);
			properties.load(fileStream);
		} catch (Throwable t) {
			final String error = PrintUtils.format("Error configurando Libcomm con fichero '%s'", file);
			Log.error(ConnectionContext.class, error, t);
			final CommunicationException ce = new CommunicationException(CommErrorType.CONFIGURATION, error, t);
			ce.addArgument("File", file);
			throw ce;
		}

		return createContext(properties);
	}
	
	/**
	 * Obtiene el contexto de la conexión creado a partir de un
	 * {@link Properties}.
	 * 
	 * @param properties
	 *            Parametros de conexión.
	 * @return Contexto de conexión.
	 * @throws CommunicationException en caso de error.
	 */
	public static ConnectionContext createContext (final Properties properties) throws CommunicationException {
		final ConnectionContext context = new ConnectionContext();
		context.setConnectionProperties(new ConnectionProperties(properties));
		return context;
	}
	
	private void setConnectionProperties (final ConnectionProperties connectionProperties) {
		this.connectionProperties = connectionProperties;
	}

	/**
	 * Obtiene el puerto de la conexión.
	 * 
	 * @return Puerto de conexión.
	 */
	public int getPort() {
		return connectionProperties.getPort();
	}

	/**
	 * Obtiene el host o IP de conexión.
	 * 
	 * @return Host o IP de conexión.
	 */
	public String getHost() {
		return connectionProperties.getHost();
	}

	/**
	 * Obtiene el nombre de la conexión.
	 * 
	 * @return Nombre de la conexión.
	 */
	public String getConnectionName() {
		return connectionProperties.getConnectionName();
	}

	/**
	 * Obtiene el tiempo de timeout de conexión en milisegundos.
	 * 
	 * @return Tiempo de timeout de conexión en milisegundos.
	 */
	public long getConnectionTimeout() {
		return connectionProperties.getConnectionTimeout();
	}

	/**
	 * Obtiene el modo de conexión, cliente o servidor.
	 * 
	 * @return Modo de conexión, cliente o servidor.
	 */
	public ConnectionMode getConnectionMode() {
		return connectionProperties.getConnectionMode();
	}

	/**
	 * Obtiene el parámetro <i>calling.tsap</i> necesario en la capa de comunicaciones de RFC1006.
	 * @return <i>calling.tsap</i>
	 */
	public String getCallingTsap() {
		return connectionProperties.getCallingTsap();
	}

	/**
	 * Obtiene el parámetro <i>called.tsap</i> necesario en la capa de comunicaciones de RFC1006.
	 * @return <i>called.tsap</i>
	 */
	public String getCalledTsap() {
		return connectionProperties.getCalledTsap();
	}

	/* Indica si el envío y espera de AK está habilitado o no. */
	public boolean isAKEnabled() {
		return connectionProperties.isAKEnabled();
	}
	
	/** Sobrescribe toString mostrando los datos de la conexión. */
	@Override
	public String toString() {
		return printConnection();
	}
}
