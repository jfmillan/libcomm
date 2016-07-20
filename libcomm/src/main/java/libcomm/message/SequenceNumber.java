package libcomm.message;

import libcomm.util.MessageUtils;

import org.apache.commons.lang3.StringUtils;

import commons.util.Constants;
import commons.util.PrintUtils;
import commons.util.StrUtils;

/**
 * Encapsula el número de secuencia de un mensaje de protocolo ficticio.
 * <p>
 * 13/01/2016 00:01:13
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class SequenceNumber implements IPrintForMessage{
	
	/** Indica que el número de secuencia no está definido todavía. */
	public static final Integer NOT_DEFINED = Integer.valueOf(-1);
	
	/** Constante para mostrar cuando el número de secuencia no está definido. */
	public static final String NOT_DEFINED_STRING = "SEQ NUMBER NOT DEFINED";
	
	/* Máximo número de secuencia. */
	private static final Integer MAX_SEQUENCE_NUMBER = 0xFFFF;
	
	/* Prefijo para cadena hexadecimal */
	private static final String HEX_PREFIX = "0x";
	
	/** Longitud del número de secuencia. */
	public static final Integer LENGTH = 4;
	
	/* Número de secuencia encapsulado. */
	private volatile int number;

	/** Constructor vacío, genera un número de secuencia sin definir. */
	public SequenceNumber () {
		this.number = NOT_DEFINED;
	}
	
	/**
	 * Constructor. Recibe el número de secuencia.
	 * 
	 * @param number
	 *            Número de secuencia.
	 */
	public SequenceNumber (final Integer number) {
		this.number = number == null || number < 0 ? NOT_DEFINED : number;
	}

	/**
	 * Constructor. Recibe el número de secuencia como cadena de texto. Admite caracteres 0-9 y A-F. Puede indicarse
	 * el prefijo 0x o no. Por ejemplo, puede indicarse: 93AF o 0x93AF.
	 * 
	 * @param number
	 *            Número de secuencia como cadena de texto.
	 */
	public SequenceNumber (final String number) {
		if (StrUtils.hasChars(number, Boolean.TRUE)) {
			final String hex = number.toLowerCase().startsWith(HEX_PREFIX) ?
					number : PrintUtils.format("%s%s", HEX_PREFIX, number);
			
			this.number = Integer.decode(hex);
		} else {
			this.number = NOT_DEFINED;
		}
	}

	
	/**
	 * Constructor. Recibe el número de secuencia.
	 * 
	 * @param number
	 *            Número de secuencia.
	 */
	public SequenceNumber (final SequenceNumber number) {
		this(number.getNumber());
	}
	
	/**
	 * Indica si el número de secuencia no está definido con un valor válido.
	 * 
	 * @return <code>true</code> si no está definido, <code>false</code> en caso
	 *         contrario.
	 */
	public boolean isNotDefined () {
		return NOT_DEFINED.equals(this.number);
	}
	
	/**
	 * Indica si el número de secuencia está definido con un valor válido.
	 * 
	 * @return <code>true</code> si está definido, <code>false</code> en caso
	 *         contrario.
	 */
	public boolean isDefined() {
		return !isNotDefined();
	}
	
	/*
	 * Obtiene el número de secuencia como entero.
	 * 
	 * @return Número de secuencia.
	 */
	public int getNumber() {
		return this.number;
	}
	
	/**
	 * Incrementa el número de secuencia. Lo reinicia a 1 si supera el máximo
	 * <code>FFFF</code>. Si no está definido no hace nada. Devuelve una copia
	 * del número de secuencia generado. Se incrementa y se devuelve
	 * atómicamente.
	 * 
	 * @return Número de secuencia siguiente, tras haberlo incrementado en una
	 *         unidad. Si ha llegado al máximo se devuelve el primer valor
	 *         permitido, 1.
	 */
	public synchronized SequenceNumber incrementAndGet () {
		if (isDefined()) {
			this.number = MAX_SEQUENCE_NUMBER.equals(this.number) ? 0x1 : number + 0x1;
		}
		return new SequenceNumber(number);
	}	
	
	/*
	 * Imprime el número de secuencia como un String hexadecimal.
	 * 
	 * @return cadena de texto con el número de secuencia en hexadecimal.
	 */
	@Override
	public String toString() {
		return isDefined() ? 
			PrintUtils.format("%s%s", HEX_PREFIX, Integer.toHexString(number).toUpperCase()) 
			: NOT_DEFINED_STRING;
	}
	
	/**
	 * Imprime el número de secuencia para un mensaje. Rellena con ceros por la
	 * izquierda si tiene valor y menos de cuatro cifras. Si no tiene valor,
	 * rellena con caracter de relleno <code>*</code>.
	 */
	@Override
	public String printForMessage() {
		return isDefined() ? /* Si tiene valor rellenamos con ceros por la izquierda si es necesario */
			StringUtils.leftPad(Integer.toHexString(number).toUpperCase(), LENGTH, Constants.ZERO_CHAR)
			/* Si no tiene valor rellenamos con caracter por defecto */
			: StringUtils.leftPad(StrUtils.EMPTY_STRING, LENGTH, MessageUtils.MESSAGE_PAD);	
	}

	@Override
	public int hashCode() {
		return Integer.valueOf(number).hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
	
		if (this == obj) {
			return true;
		}
		
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		final SequenceNumber other = (SequenceNumber) obj;
		return other.getNumber() == number;
	}
}