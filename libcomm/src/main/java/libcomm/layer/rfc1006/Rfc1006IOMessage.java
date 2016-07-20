package libcomm.layer.rfc1006;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import libcomm.exception.CommErrorType;
import libcomm.exception.CommunicationException;
import libcomm.message.rfc1006.CcTsdu;
import libcomm.message.rfc1006.CrTsdu;
import libcomm.message.rfc1006.DataTsdu;
import libcomm.message.rfc1006.IBytes;
import libcomm.message.rfc1006.ITsdu;
import libcomm.message.rfc1006.Tpkt;
import libcomm.util.BufferUtils;

import commons.log.Log;
import commons.util.ColUtils;
import commons.util.PrintUtils;

/**
 * Operaciones de creación y lectura y acumulación de mensajes RFC1006. Cuando
 * se reciben los mensajes del canal podrían haber llegado incompletos, más de
 * uno, etc. Por ejemplo, podemos recibir medio mensaje, dos mensajes y medio,
 * cuatro mensajes completos... Esta clase actúa como lector de mensajes, además
 * de permitir lecturas incompletas que serán completadas más adelante. También
 * permite crear mensajes RFC1006 a partir de bytes y leer los campos que
 * interesen de cada uno.
 * <p>
 * 15/02/2016 00:16:03
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class Rfc1006IOMessage {

	/* NOTA: A la hora de actuar sobre los buffer, los métodos get... dejan el buffer inalterado, 
	 * los métodos read..., sin embargo, avanzan contador de posición */
	
	/* Tamaño máximo especificado para un TPKT según protocolo. */
	private static final int MAX_TPKT_SIZE = 128;
	
	/* Tamaño de cabecera RFC1006 */
	private static final int RFC1006_HEADER_SIZE = 4;

	/* Tamaño de la parte fija de la cabecera para mensajes de establecimiento de conexión CR y CC en clase 0. 
	 * La longitud final variará según parámetros */
	private static final int RFC1006_CLASS_0_CONNECTION_MIN_HEADER_SIZE = 7;

	/* Tamaño de cabecera de un telegrama de tipo DT (<code>data user</code>). */  
	private static final int RFC1006_CLASS_0_DT_HEADER_SIZE = 3;
	
	/* Versión RFC1006 utilizada: v3 */
	private static final byte RFC1006_VERSION = 0x03;
	
	/* Byte que identifica un mensaje CR (connection request). */
	private static final byte RFC1006_CONNECTION_TPDU_CR_CODE = (byte) 0xE0;

	/* Byte que identifica un mensaje CC (connection confirmed). */
	private static final byte RFC1006_CONNECTION_TPDU_CC_CODE = (byte) 0xD0;

	/* Constante para campo DST-REF en el establecimiento de conexion (0x00 0x00). */
	private static final byte [] RFC1006_CONNECTION_TPDU_DST_REF = {BufferUtils.BYTE_ZERO, BufferUtils.BYTE_ZERO};
	
	/* Constante para campo SRC-REF en el establecimiento de conexion (0x4F 0x45). */
	private static final byte [] RFC1006_CONNECTION_TPDU_SRC_REF = {(byte) 0x4f, (byte) 0x45 };

	/* Constante para bits de opciones en el establecimiento de conexión */
	private static final byte RFC1006_CONNECTION_TPDU_OPTIONS = (byte) 0x00;

	
	/* Codigo del parametro de conexión para identificar el <i>tamaño de TPDU</i>.*/
	private static final byte RFC1006_CONNECTION_TPDU_SIZE_CODE = (byte) 0xC0;

	/*
	 * Constante para identificar el valor del parámetro <i>TPDU Size</i> a 128: 
	 * 7 en decimal, <0000><0111> en binario, 0x07 en hexadecimal.
	 */
	private static final byte RFC1006_CONNECTION_TPDU_SIZE_VALUE = (byte) 0x07;

	/* Codigo identificador del parametro de conexión <i>Calling TSAP</i>. */
	private static final byte RFC1006_CONNECTION_TPDU_CALLING_TSAP_CODE = (byte) 0xC1;
	
	/* Codigo identificador del parametro ce conexión <i>Called TSAP</i>. */
	private static final byte RFC1006_CONNECTION_TPDU_CALLED_TSAP_CODE = (byte) 0xC2;

	/* Código de mensaje DT */
	private static final byte RFC1006_DT_CODE = (byte) 0xF0;
	
	/* Código de fin de TPDU, indica cuando un TSDU es el último y finaliza el TPDU. */
	private static final byte RFC1006_DT_EOT = (byte) 0x80;
	
	/*
	 * ByteBuffer utilizado para acumular mensajes recibidos, tiene el tamaño
	 * máximo especificado para un mensaje RFC1006 multiplicado por 10, lo que
	 * debería ser más que suficiente, ya que como mucho tendremos un mensaje
	 * incompleto (el resto serán notificados). Todo byte recibido que llega a
	 * leerse será escrito en este buffer.
	 */
	private final ByteBuffer currentMessageBuffer;
	
	/* Constructor. */
	Rfc1006IOMessage() {
		currentMessageBuffer = ByteBuffer.allocate(MAX_TPKT_SIZE*10);
		currentMessageBuffer.flip(); /* para que la primera lectura que compruebe bytes anteriores no intente leer un buffer vacío */
	}
	
	/**
	 * Crea una cabecera TPKT de RFC1006, cuyos campos principales son el numero
	 * de version y el tamaño del mensaje completo.
	 * 
	 * <pre>
	 *  0                   1                   2                   3
	 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
	 * |     vrsn    |    reserved   |          packet length          | 
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * </pre>
	 * 
	 * http://www.faqs.org/rfcs/rfc1006.html#ixzz0fu7pMSG4
	 * 
	 * @param tsdu
	 *            TSDU para el que se creará la cabecera.
	 * @return Cabecera del paquete.
	 */
	private static byte [] createRfc1006Header (final ITsdu tsdu) {
		/* Cabecera RFC1006 = 4 bytes. */
		final byte [] header = new byte [RFC1006_HEADER_SIZE];
		
		/* Primer byte, version. */
		header[0] = RFC1006_VERSION;
		
		/* Segundo byte, reservado, a cero.*/
		header[1] = BufferUtils.BYTE_ZERO;
		
		/* Tercer y cuarto byte, tamaño del paquete: CABECERA RFC1006 + CABECERA TSDU + CUERPO TSDU */
		int size = RFC1006_HEADER_SIZE + tsdu.getBytes().length;
		header [2] = (byte) ((size >> 8) & 0xFF);
		header [3] = (byte) (size & 0xFF);
		
		return header;
	}
	
	/**
	 * Crea una cabecera de mensaje DT de clase 0.
	 * 
	 * <pre>
	 *  0                   1                   2                   3 
	 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
	 * | header length |  code |credit |TPDU-NR and EOT|   user data   | 
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * |      ...      |1 1 1 1 0 0 0 0|1 0 0 0 0 0 0 0|      ...      | 
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * </pre>
	 */		
	private static byte [] createDTHeader () {
		return new byte[] {
			/* LI = longitud cabecera - 1, ya que se descarta el propio byte LI */
			RFC1006_CLASS_0_DT_HEADER_SIZE - 1,
			
			/* Codigo DT */	
			RFC1006_DT_CODE,
			
			/* Enviamos el mensaje en un unico TSDU, por tanto finalizamos el TPDU con un EOT */
			RFC1006_DT_EOT
		};
	}
	
	/*
	 * Lee desde un arreglo de bytes cero o más mensajes. Si algún mensaje se queda a medias no lo notifica, se 
	 * almacena en el buffer de mensajes incompletos a la espera de completarlo. Los mensajes son leídos y devueltos 
	 * <b>sin las cabeceras RFC1006</b >. Los mensajes leídos soportados son de tres tipos: DataTsdu, CcTsdu y CrTsdu.
	 */
	List<ITsdu> readTSDUs (final IBytes bytes) {
		final ByteBuffer toReadBytes = readActualMessageBufferBytes(bytes.getBytes());
		final List<ITsdu> result = new ArrayList<ITsdu>();
		
		while (readCompleteTpkt(toReadBytes)) { /* Aunque no quede nada para leer se llamará al metodo y dejara 
												   el buffer listo para lectura */
			result.add(readTsduFromBuffer(currentMessageBuffer)); 
		} /* Lo que no se haya leído queda guardado en el buffer igualmente, esperando */
		
		return result;
	}

	/*
	 * Lee los datos de un buffer y los escribe en el buffer del mensaje actual.
	 * Como máximo guarda un mensaje completo. Si consigue leer un mensaje
	 * completo devuelve true, en caso contrario devuelve false. La posición del
	 * buffer a leer se mantiene para que la siguiente llamada lea desde el
	 * mismo punto.
	 * 
	 * El buffer recibido ya está listo para lectura.
	 */
	private boolean readCompleteTpkt(final ByteBuffer bufferToRead) {
		/* Leemos la cabecera y obtenemos el tamaño total, incluidas cabeceras. */
		final int length = getTpktLength (bufferToRead); /* Deja el buffer inalterado */
		boolean readResult = false;
		
		if (length > 0) {
			/* readResult será true si se consiguen leer 'length' bytes del buffer */
			readResult = readFromBuffer(bufferToRead, length, currentMessageBuffer); /* lee del buffer y escribe en currentMessageBuffer */
		} else { /* Lo llamamos aunque no haya "hasRemaining", currentMessageBuffer quedará así en el mismo modo lectura, siempre. */
			/* Escribimos en el buffer lo poco que haya en bufferToRead */
			readFromBuffer(bufferToRead, bufferToRead.remaining(), currentMessageBuffer);
		}
		return readResult;
	}		
	
	/* Leemos la cabecera <0x03><0x00><0xNN> siendo NN el tamaño en bytes del TPKT completo. 
	 * Si no consigue leerse devolverá cero. 
	 *  0                   1                   2                   3
	 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
	 * |     vrsn    |    reserved   |          packet length          | 
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 */
	private int getTpktLength (final ByteBuffer buffer) {
		final byte[] tpktLength = getNBytesFromPosition(buffer, buffer.position()+2, 2);
		return tpktLength == null ? 0 
			: (0xFF & tpktLength[0]) << 8 | (0xFF & tpktLength[1]);
	}

	/* Obtiene el código de TSDU del buffer sin alterarlo. */
	private byte getTsduCode(final ByteBuffer buffer) {
		/* El código siempre en el segundo byte tras la cabecera RFC1006 */
		final byte[] code = getNBytesFromPosition(buffer, RFC1006_HEADER_SIZE + 1, 1); 
		return code != null ? code[0] : BufferUtils.BYTE_ZERO;
	}

	/**
	 * Obtiene, en caso de ser posible, un número de bytes a partir de una
	 * determinada posición. Deja los indicadores del buffer como estaban, de
	 * modo que pueda leerse de nuevo como si no se hubiese llamado a este
	 * método. En caso de no poder leer el número de bytes completos devuelve
	 * null.
	 */
	private byte [] getNBytesFromPosition (final ByteBuffer buffer, final int position, final int length) {
		byte[] bytes = null;
		
		if (buffer.hasRemaining() && buffer.remaining() > length) {
			/* fijamos posición actual */
			buffer.mark();
			try {
				buffer.position (position);
				bytes = new byte[length];
				buffer.get(bytes, 0, length);
			} catch (Exception e) {
				Log.error(this, PrintUtils.format("Error buscando '%s' bytes en buffer a partir de posición '%s'", length, position), e);
				bytes = null;
			} finally {
				/* restauramos posición marcada. */
				buffer.reset();
			}
		}
		return bytes;
	}
	
	/**
	 * Intenta leer del buffer de lectura el número de bytes indicados y los
	 * escribe en el de escritura.
	 * 
	 * @param readBuffer
	 *            Buffer de lectura listo para ser leído, probablemente desde
	 *            una posición distinta de cero.
	 * @param length
	 *            Número de bytes a intentar leer. Tras leer el buffer no se
	 *            prepara para la escritura ni se borra, con el objetivo de
	 *            seguir leyendo en sucesivas llamadas.
	 * @param writeBuffer
	 *            Buffer listo para ser escrito. Tras escribir en el
	 *            buffer (o incluso si no se ha escrito nada), deja el buffer
	 *            listo para lectura.
	 * @return
	 */
	private static boolean readFromBuffer(final ByteBuffer readBuffer, final int length, final ByteBuffer writeBuffer) {
		int readBytes = 0;
		
		byte b; 
		while (readBytes < length && readBuffer.hasRemaining()) {
			b = readBuffer.get();
			readBytes++;
			writeBuffer.put(b);
		}
		writeBuffer.flip();
		
		return readBytes == length;
	}

	/* Lee un TSDU completo del buffer. Tras leerlo lo compacta y lo deja listo para escritura. */
	private ITsdu readTsduFromBuffer(final ByteBuffer buffer) {
		final ITsdu result;
		final byte tsduCode = getTsduCode(buffer);

		/* descartamos la cabecera RFC1006 */
		buffer.get(new byte[RFC1006_HEADER_SIZE]);
		
		/* Si es un código válido cogemos todos los bytes excepto la cabecera RFC1006 (queremos TSDUs). */
		switch (tsduCode) { 
		case RFC1006_DT_CODE:
			result = extractDataTsduFromBuffer(buffer);
			break;
			
		case RFC1006_CONNECTION_TPDU_CR_CODE:
			result = extractCrTsduFromBuffer(buffer);
			break;
			
		case RFC1006_CONNECTION_TPDU_CC_CODE:
			result = extractCcTsduFromBuffer(buffer);
			break;
		default:
			result = null;
		}

		buffer.compact();
		return result;
	}

	
	/* Extrae un DataTsdu (telegrama DT de un buffer sin cabecera RFC1006 */
	private DataTsdu extractDataTsduFromBuffer(final ByteBuffer buffer) {
		final byte[] dtHeader = new byte[RFC1006_CLASS_0_DT_HEADER_SIZE];
		buffer.get(dtHeader);
		final byte[] dtBody = BufferUtils.readFromBuffer(buffer, Boolean.FALSE);
		return new DataTsdu(dtHeader, dtBody);
	}

	/* Extrae un CrTsdu (telegrama CR de un buffer sin cabecera RFC1006 */
	private CrTsdu extractCrTsduFromBuffer(final ByteBuffer buffer) {
		byte[] callingTsap = extractVariableParam (buffer, RFC1006_CONNECTION_TPDU_CALLING_TSAP_CODE);
		byte[] calledTsap = extractVariableParam (buffer, RFC1006_CONNECTION_TPDU_CALLED_TSAP_CODE);

		final byte[] crBytes = BufferUtils.readFromBuffer(buffer, Boolean.FALSE);
		final CrTsdu cr = new CrTsdu(crBytes);
		cr.setCallingTsap(callingTsap);
		cr.setCalledTsap(calledTsap);
		return cr;
	}

	/* Extrae un CcTsdu (telegrama CC de un buffer sin cabecera RFC1006 */
	private CcTsdu extractCcTsduFromBuffer(final ByteBuffer buffer) {
		byte[] callingTsap = extractVariableParam (buffer, RFC1006_CONNECTION_TPDU_CALLING_TSAP_CODE);
		byte[] calledTsap = extractVariableParam (buffer, RFC1006_CONNECTION_TPDU_CALLED_TSAP_CODE);

		final byte[] ccBytes = BufferUtils.readFromBuffer(buffer, Boolean.FALSE);
		final CcTsdu cc = new CcTsdu(ccBytes);
		cc.setCallingTsap(callingTsap);
		cc.setCalledTsap(calledTsap);
		return cc;
	}
	
	/*
	 * Extrae un parámetro variable de un buffer de bytes. Busca el parámetro
	 * por su código, obtiene su tamaño, y recupera los bytes necesarios.
	 */
	private byte[] extractVariableParam(final ByteBuffer buffer, final byte code) {
		byte[] bytes = null;

		final boolean doMark = buffer.hasRemaining();
		if (doMark) {
			buffer.mark();
		}
	
		try {
			boolean foundCode = false;
			byte __byte;
			while (buffer.hasRemaining() && !foundCode) {
				__byte = buffer.get();
				foundCode = __byte == code;
			}
			
			if (foundCode) {
				final Integer length = buffer.hasRemaining() ? (0xFF & buffer.get()) : null;
				if (length != null && length > 0) {
					if (buffer.remaining() >= length) {
						bytes = new byte[length];
						buffer.get(bytes);
					}
				}
			}
		} finally {
			if (doMark) {
				buffer.reset();
			}
		}
	
		return bytes;
	}

	/*
	 * Obtiene los bytes reales que hay que leer y los encapsula en un
	 * ByteBuffer. Se leerán los los bytes recibidos ahora, precedidos de los
	 * que hayan quedado en el buffer anteriormente. El buffer de mensajes lo
	 * deja listo para escribir desde el principio (clear).
	 */
	private ByteBuffer readActualMessageBufferBytes (final byte[] newBytes) {
		final byte[] fromIncompleteBuffer = BufferUtils.readFromBuffer(currentMessageBuffer, Boolean.FALSE);
		currentMessageBuffer.clear();
		final byte[] bytesToRead = fromIncompleteBuffer.length > 0 
			? ColUtils.concat(fromIncompleteBuffer, newBytes) 
			: newBytes;
			
		return BufferUtils.writeInToBuffer(bytesToRead, Boolean.TRUE);
	}

	
	/**
	 * Añade una cabecera RFC1006 a un TSDU, formando un paquete RFC1006
	 * copmleto.
	 * 
	 * @param tsdu
	 *            Tsdu al que añadir cabecera RFC1006
	 * @return TPKT correspondiente al Tsdu con cabecera.
	 */
	private <T extends ITsdu> Tpkt<T> createTpkt(final T tsdu) {
		final byte[] rfc1006Header = createRfc1006Header(tsdu);
		return new Tpkt<T>(rfc1006Header, tsdu);
	}
	
	/**
	 * Crea un mensaje DT con cabecera RFC1006.
	 * 
	 * @param message
	 *            Mensaje a partir del cual se crea el TPKT.
	 * @return TPKT creado.
	 * @throws CommunicationException si se produce algún error.
	 */
	public Tpkt<DataTsdu> createDt(final IBytes message) throws CommunicationException {
		final byte[] header = createDTHeader();
		final DataTsdu dtTsdu = new DataTsdu(header, message.getBytes());
		final Tpkt<DataTsdu> dt = createTpkt(dtTsdu);
		checkSize(dt);
		return dt;
	}

	/**
	 * Obtiene un mensaje de solicitud de conexión RFC1006 de clase 0 con los TSAP
	 * indicados.
	 * <pre>
	 *  0                   1                   2                   3                   4                   5
	 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 ...
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
	 * |header length  |   CR  |  CDT  |            DST - REF          |            SRC - REF          |Class(0) Option|Variable Part..
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+... 
	 * |     L I       |1 1 1 0 0 0 0 0|0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0|0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0|        ...
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+...
	 * </pre>
	 * @param callingTsap
	 *            Parámetro calling.tsap
	 * @param calledTsap
	 *            Parámetro called.tsap
	 * @return TPKT de solicitud de conexión.
	 * @throws CommunicationException si se produce algún error.
	 */
	public Tpkt<CrTsdu> createCr(final byte[] callingTsap, final byte[] calledTsap) throws CommunicationException {
		final byte[] crBytes = createConnectionMessage (RFC1006_CONNECTION_TPDU_CR_CODE, callingTsap, calledTsap);
		final CrTsdu crTsdu = new CrTsdu(crBytes);
		crTsdu.setCallingTsap(ColUtils.copy(callingTsap));
		crTsdu.setCalledTsap(ColUtils.copy(calledTsap));
		final Tpkt<CrTsdu> cr = createTpkt(crTsdu);
		checkSize(cr);
		return cr;
	}

	/**
	 * Crea un mensaje CC de confirmación de conexión RFC1006 con los TSAP
	 * indicados.
	 * <pre>
	 *  0                   1                   2                   3                   4                   5
	 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 ...
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
	 * |header length  |   CC  |  CDT  |            DST - REF          |            SRC - REF          |Class(0) Option|Variable Part..
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+... 
	 * |     L I       |1 1 0 1 0 0 0 0|0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0|0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0|        ...
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+...
	 * </pre>
	 * @param callingTsap
	 *            Parámetro calling.tsap
	 * @param calledTsap
	 *            Parámetro called.tsap
	 * @return Mensaje de confirmación de conexión.
	 * @throws CommunicationException si se produce algún error.
	 */
	public Tpkt<CcTsdu> createCc(final byte[] callingTsap, final byte[] calledTsap) throws CommunicationException {
		final byte[] ccBytes = createConnectionMessage (RFC1006_CONNECTION_TPDU_CC_CODE, callingTsap, calledTsap);
		final CcTsdu ccTsdu = new CcTsdu(ccBytes);
		ccTsdu.setCallingTsap(ColUtils.copy(callingTsap));
		ccTsdu.setCalledTsap(ColUtils.copy(calledTsap));
		final Tpkt<CcTsdu> cc = createTpkt(ccTsdu);
		checkSize(cc);
		return cc;
	}

	/*
	 * Crea la secuencia de bytes para un mensaje CR o CC, necesarios para
	 * establecer la conexión RFC1006. El formato de ambos mensajes es similar,
	 * al menos con los parámetros por defecto que se añaden.
	 */
	private byte[] createConnectionMessage (final byte messageCode, final byte[] callingTsap, final byte[] calledTsap) {
		/* Tamaño: 
		 * 		Parte fija: LI(1) + Código (1) + DST-REF (2) + SRC-REF (2) + options (1) 
		 * 		Parte variable: tamaño tpdu (1) + callingTsap (n) + calledTsap (m)
		 * 						(cada parámetro variable añade 1 byte de código + 1 byte de tamaño + n bytes de contenido) 
		 */
		int length = 
			RFC1006_CLASS_0_CONNECTION_MIN_HEADER_SIZE	/* parte fija 					*/
			+ (2+1) 									/* parte variable: tamaño tpdu 	*/
			+ (2+callingTsap.length)	 				/* parte variable: calling.tsap */
			+ (2+calledTsap.length);		 			/* parte variable: called.tsap 	*/
		final byte[] message = new byte[length];
		int index = 0;
		
		/* length indicator (se descuenta el propio byte L/I */
		message[index++] = (byte) (length - 1);
		
		/* Código de mensaje */
		message[index++] = messageCode;
		
		/* DST-REF, ambos bytes a cero */
		message[index++] = RFC1006_CONNECTION_TPDU_DST_REF[0];
		message[index++] = RFC1006_CONNECTION_TPDU_DST_REF[1];

		/* SRC-REF */
		message[index++] = RFC1006_CONNECTION_TPDU_SRC_REF[0];
		message[index++] = RFC1006_CONNECTION_TPDU_SRC_REF[1];

		/* Options */
		message[index++] = RFC1006_CONNECTION_TPDU_OPTIONS;
		
		/* Tamaño máximo de TPDU */
		message[index++] = RFC1006_CONNECTION_TPDU_SIZE_CODE;
		message[index++] = (byte) 1;
		message[index++] = RFC1006_CONNECTION_TPDU_SIZE_VALUE;
		
		/* Parámetro calling.tsap */
		message[index++] = RFC1006_CONNECTION_TPDU_CALLING_TSAP_CODE;
		message[index++] = (byte) callingTsap.length;
		for (int i = 0; i < callingTsap.length; i++) {
			message[index++] = callingTsap[i];
		}
		
		/* Parámetro called.tsap */
		message[index++] = RFC1006_CONNECTION_TPDU_CALLED_TSAP_CODE;
		message[index++] = (byte) calledTsap.length;
		for (int i = 0; i < calledTsap.length; i++) {
			message[index++] = calledTsap[i];
		}
		return message;
	}
	
	/* Comprueba el tamaño del TPKT. Si supera el máximo permitido lanza una excepción por tamaño excesivo. */
	private void checkSize(final Tpkt<? extends ITsdu> tpkt) throws CommunicationException {
		if (tpkt == null || tpkt.size() > MAX_TPKT_SIZE) {
			final int size = tpkt != null ? tpkt.size() : 0;
			final String error = PrintUtils.format("Tpkt '%s' con un tamaño de '%s' bytes supera el máximo permitido '%s'",
				tpkt, size, MAX_TPKT_SIZE);
			Log.fatal(this, error);
			final CommunicationException ex = new CommunicationException(CommErrorType.TPKT_TOO_LONG, error);
			ex.addArgument("TPKT", tpkt);
			ex.addArgument("tpktSize", size);
			ex.addArgument("maxSize", MAX_TPKT_SIZE);
			throw ex;
		}
	}
}
