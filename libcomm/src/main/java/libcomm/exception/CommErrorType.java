package libcomm.exception;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Enumerado que representa los tipos de error que se pueden producir.
 * <p>
 * 05/03/2016 22:23:28
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public enum CommErrorType {
	UNDEFINED_ERROR ("Error no definido"), 
	TYPE_CONVERT ("Conversión de tipos incorrecta"), 
	REFLECTION ("Reflexión"), 
	CONFIGURATION ("Configuración"), 
	TSAPS ("Configuración de TSAPs"),
	ENCODE ("Codificación"), 
	DECODE ("Decodificación"), 
	PARSE_MESSAGE ("Interpretación de mensaje"), 
	PARSE_MESSAGE_FIELD ("Interpretar campo de mensaje"), 
	BUILD_MESSAGE ("Construir mensaje"), 
	AK_FAILURE ("AK no recibido"), 
	CONNECTION_ERROR ("Error conectando"),
	DISCONNECTION_ERROR ("Error desconectando"),
	COMMUNICATION_ERROR ("Error de comunicaciones"),
	SOCKET_ERROR ("Error en sockets"),
	SENDING ("Enviando mensaje"),
	TPKT_TOO_LONG ("TPKT de RFC1006 demasiado largo"), 
	REMOTE_DISCONNECTION ("Desconexión del sisema remoto"),
	TIMEOUT ("Desconexión por timeout"),
	FLOW_CONTROL ("Error en control de flujo");

	/* Tipos de errores que se consideran fatales y tras los que NO se intenta reconectar (debe hacerse manualmente) */
	public final static CommErrorType [] FATAL_ERRORS = {TSAPS, AK_FAILURE, TPKT_TOO_LONG};
	
	/* Breve descripción del error. */
	private final String description;

	private CommErrorType(final String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * Indica si el error es fatal y no permite reconexión automática.
	 * 
	 * @return Indica si el error es fatal (no permite reconexión).
	 */
	public boolean isFatal() {
		return isFatal(this);
	}
	
	/**
	 * Indica si el error es fatal y no permite reconexión automática.
	 * 
	 * @param type
	 *            Tipo de error.
	 * @return Indica si el error es fatal (no permite reconexión).
	 */
	public static boolean isFatal(final CommErrorType type) {
		return type != null && ArrayUtils.contains(FATAL_ERRORS, type);
	}
}
