package libcomm.connection;

/**
 * Modos de conexión disponibles: cliente, servidor.
 * <p>
 * 04/01/2016 19:19:57
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public enum ConnectionMode {
	CLIENT, SERVER;
	
	
	/**
	 * Permite obtener el enumerado correspondiente a un String, sin distinguir
	 * mayúsculas/minúsculas.
	 * 
	 * @param modeStr
	 *            Modo en formato {@link String}.
	 * @return Modo enumerado, <code>null</code> si no lo encuentra.
	 */
	public static ConnectionMode getMode (final String modeStr) {
		final String modeUp = modeStr != null ? modeStr.toUpperCase() : null;
		try {
			return ConnectionMode.valueOf(modeUp);
		} catch (IllegalArgumentException e) {
			/* Si se pasa un valor que no corresponde al enumerado simplemente null */
		}
		return null;
	}
}
