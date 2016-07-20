package libcomm.message.parser;

import java.util.Map;

import libcomm.exception.MalformedMessageException;
import libcomm.message.Field;
import libcomm.message.FieldMetaData;

/**
 * Establece el método para parsear los campos de un mensaje concreto en base a
 * un contenido facilitado como {@link String}.
 * <p>
 * 24/01/2016 16:14:45
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public interface IMessageParser {

	/**
	 * Parseo de mensaje a partir de {@link String}, obtiene un mapa de campos
	 * que sólo establece los necesarios para el mensaje que se está tratando.
	 * Cada constructor debe saber como interpretar el mapa devuelto por su
	 * parser.
	 * 
	 * @param toParse
	 *            Mensaje a parsear.
	 * @return Mapa de campos indexados por sus metadatos.
	 * @throws MalformedMessageException
	 *             Si se detecta que el mensaje está mal formado.
	 */
	Map<FieldMetaData, Field<?>> parse(String toParse) throws MalformedMessageException;
}
