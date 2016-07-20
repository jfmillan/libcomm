package libcomm.util;

import java.nio.ByteBuffer;
import java.util.Objects;

import libcomm.message.rfc1006.IBytes;

import org.apache.commons.lang3.ArrayUtils;

import commons.util.ColUtils;
import commons.util.Constants;

/**
 * Utilidades para manejo de Buffers y arreglos de bytes.
 * <p>
 * 15/01/2016 23:46:34
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class BufferUtils {
	/** Representa un byte con valor cero. */
	public static final byte BYTE_ZERO = 0x00;

	/**
	 * Lee un buffer y obtiene el array de bytes que contiene. Es necesario
	 * indicar si hay que preparlo para lectura primero, ya que de hacerlo dos
	 * veces se perderían los índices que permiten leer el contenido del buffer.
	 * 
	 * @param byteBuffer
	 *            Buffer de bytes a leer.
	 * @param needsFlip
	 *            Indica si necesita operación de flip, lo que prepara el buffer
	 *            para lectura despues de operaciones de escritura.
	 * @return Array con los bytes leidos.
	 */
	public static byte[] readFromBuffer(final ByteBuffer byteBuffer, final boolean needsFlip) {
		if (byteBuffer == null) {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}

		if (needsFlip) {
			byteBuffer.flip();
		}
		
		final byte[] result = new byte[byteBuffer.remaining()];
		byteBuffer.get(result);
		return result;
	}

	/**
	 * Escribe en un buffer los bytes que se facilitan como array. Es necesario
	 * indicar si hay que preparlo para lectura tras finalizar la escritura, ya que de hacerlo dos
	 * veces se perderían los índices que permiten leer el contenido del buffer.
	 * 
	 * @param bytes 
	 *            Array de bytes a escribir.
	 * @param flipAfterWrite
	 *            Indica si necesita operación de flip, lo que prepara el buffer
	 *            para lectura despues de operaciones de escritura.
	 * @return ByteBuffer con los bytes escritos.
	 */
	public static ByteBuffer writeInToBuffer (final byte[] bytes, final boolean flipAfterWrite) {
		final int size = bytes == null ? Constants.ZERO : bytes.length;
		final ByteBuffer buffer = ByteBuffer.allocate(size);
		
		return writeInToBuffer(buffer, bytes, flipAfterWrite);
	}
	
	/**
	 * Escribe en un buffer los bytes que se facilitan como array. Es necesario
	 * indicar si hay que preparlo para lectura tras finalizar la escritura, ya
	 * que de hacerlo dos veces se perderían los índices que permiten leer el
	 * contenido del buffer. Antes de escribir en el buffer se prepara para
	 * escritura limpiando su contenido anterior.
	 * 
	 * @param buffer
	 *            Buffer en el que se escribe.
	 * @param bytes
	 *            Array de bytes a escribir.
	 * @param flipAfterWrite
	 *            Indica si necesita operación de flip, lo que prepara el buffer
	 *            para lectura despues de operaciones de escritura.
	 * @return ByteBuffer con los bytes escritos. Es el mismo buffer que
	 *         <code>buffer</code>.
	 */
	public static ByteBuffer writeInToBuffer (final ByteBuffer buffer, final byte[] bytes, final boolean flipAfterWrite) {
		Objects.requireNonNull(buffer);
		
		final int size = bytes == null ? Constants.ZERO : bytes.length;
		buffer.clear();
		
		if (size > 0) {
			buffer.put(bytes);
		}

		if (flipAfterWrite) {
			buffer.flip();
		}

		return buffer;
	}
	
	/**
	 * Transforma un array de bytes en un objeto que implementa IBytes. Soporta
	 * array nulo, devolviendo un IBytes cuyo resultado sería una array vacío.
	 * 
	 * @param bytes
	 *            Array de bytes.
	 * @return Objeto IBytes representando el parámetro facilitado. En caso de
	 *         pasarse array nulo se comportará igual que si se pasase array
	 *         vacío.
	 */
	public static IBytes getIBytes (final byte[] bytes) {
		final byte[] __bytes = bytes != null ? ColUtils.copy(bytes) : new byte[]{};
		return new IBytes() {
			@Override
			public byte[] getBytes() {
				return __bytes;
			}
		};
	}
}
