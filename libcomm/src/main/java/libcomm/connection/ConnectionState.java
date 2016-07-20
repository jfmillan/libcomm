package libcomm.connection;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Estados de una conexión. Indica también si una conexión en un determinado estado
 * permite conectar o desconectar.
 * <p>
 * 31/01/2016 13:12:00
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public enum ConnectionState {
	CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED;
	
	/* Estados que permiten conexión */
	private final static ConnectionState[] allowingConnectionStates = {DISCONNECTED};
	
	/* Estados que permiten desconexión. */
	private final static ConnectionState[] allowingDisconnectionStates = {CONNECTING, CONNECTED};
	
	/**
	 * Indica si el estado actual permite un nuevo intento de conexión.
	 * 
	 * @return <code>true</code> si el estado permite una nueva conexión,
	 *         <code>false</code> en caso contrario.
	 */
	public boolean allowsConnection () {
		return ConnectionState.allowsConnection(this);
	}

	/**
	 * Indica si el estado actual permite un nuevo intento de desconexión.
	 * 
	 * @return <code>true</code> si el estado permite una nueva desconexión,
	 *         <code>false</code> en caso contrario.
	 */
	public boolean allowsDisconnection () {
		return ConnectionState.allowsDisconnection(this);
	}

	
	/**
	 * Indica si el estado facilitado permite un nuevo intento de conexión.
	 * 
	 * @param state
	 *            Estado de la conexión.
	 * @return <code>true</code> si el estado permite una nueva conexión,
	 *         <code>false</code> en caso contrario.
	 */
	public static boolean allowsConnection (final ConnectionState state) {
		return state != null && ArrayUtils.contains(allowingConnectionStates, state);
	}

	/**
	 * Indica si el estado facilitado permite un nuevo intento de desconexión.
	 * 
	 * @param state
	 *            Estado de la conexión.
	 * @return <code>true</code> si el estado permite una nueva desconexión,
	 *         <code>false</code> en caso contrario.
	 */
	public static boolean allowsDisconnection (final ConnectionState state) {
		return state != null && ArrayUtils.contains(allowingDisconnectionStates, state);
	}
	
	/**
	 * Permite obtener el enumerado correspondiente a un String, sin distinguir
	 * mayúsculas/minúsculas.
	 * 
	 * @param stateStr
	 *            Estado en formato {@link String}.
	 * @return Estado enumerado, <code>null</code> si no lo encuentra.
	 */
	public static ConnectionState getConnectionState (final String stateStr) {
		final String modeUp = stateStr != null ? stateStr.toUpperCase() : null;
		try {
			return ConnectionState.valueOf(modeUp);
		} catch (IllegalArgumentException e) {
			/* Si se pasa un valor que no corresponde al enumerado simplemente null */
		}
		return null;
	}
}
