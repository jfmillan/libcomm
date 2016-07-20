package libcomm.connection;

import commons.util.PrintUtils;

/**
 * Interfaz con los métodos disponibles para identificar y configurar una
 * conexión.
 * <p>
 * 09/01/2016 23:10:17
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public interface IConnection {
	
	/**
	 * Obtiene el host o IP de conexión.
	 * 
	 * @return Host o IP de conexión.
	 */
	String getHost();
	
	/**
	 * Obtiene el puerto de la conexión.
	 * 
	 * @return Puerto de conexión.
	 */
	int getPort();

	/**
	 * Obtiene el nombre de la conexión.
	 * 
	 * @return Nombre de la conexión.
	 */
	String getConnectionName();

	/**
	 * Obtiene el tiempo de timeout de conexión en milisegundos.
	 * 
	 * @return Tiempo de timeout de conexión en milisegundos.
	 */
	long getConnectionTimeout();

	/**
	 * Obtiene el modo de conexión, cliente o servidor.
	 * 
	 * @return Modo de conexión, cliente o servidor.
	 */
	ConnectionMode getConnectionMode();
	
	/** Obtiene un texto con los datos de la conexión.
	 * 
	 * @return Texto con los datos de la conexión.
	 */
	default String printConnection() {
		return PrintUtils.format("Conexion %s: host[%s] puerto[%s] modo[%s]", 
			getConnectionName(), getHost(), getPort(), getConnectionMode()
		);
	}
}
