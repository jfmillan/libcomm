package libcomm.message;

import java.util.Optional;

import libcomm.util.MessageUtils;

/**
 * Clase que representa un campo de tipo Flag, tiene la peculiaridad de que se
 * representa como Y/N pero se comporta como un booleano <code>true</code>/
 * <code>false</code>.
 * <p>
 * 25/01/2016 22:04:47
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class FieldFlag extends Field<Character> {

	/** Caracter utilizado para indicar <code>SÍ</code> en los mensajes.*/
	public static final Character YES_FLAG = 'Y';
	
	/** Caracter utilizado para indicar <code>NO</code> en los mensajes. */
	public static final Character NOT_FLAG = 'N';
	
	/** Booleano utilizado para indicar <code>SÍ</code> en los mensajes.*/
	public static final Boolean YES_BOOLEAN_FLAG = Boolean.TRUE;
	
	/** Booleano utilizado para indicar <code>NO</code> en los mensajes. */
	public static final Boolean NOT_BOOLEAN_FLAG = Boolean.FALSE;
	
	/* Valor booleano representado por el caracter. */
	private final Optional<Boolean> booleanValue;
	
	
	 /*
	 * Constructor protected con el valor y el mínimo de longitud. Para crear un
	 * campo desde una clase externa debe usarse el método estático
	 * #create(FieldMetaData, Object).
	 */
	protected FieldFlag(final FieldMetaData metaData, final Character value) {
		super(metaData, value);
		booleanValue = getBoolean(value);
	}
	
	/**
	 * Obtiene el valor booleano que representa el caracter Y/N.
	 * 
	 * @return <code>true</code> si el campo vale 'Y', <code>false</code> si el
	 *         campo vale 'N', <code>null</code> si no hay valor.
	 */
	public Boolean getBooleanValue () {
		return booleanValue.isPresent() ? booleanValue.get() : null;
	}
	
	/**
	 * Obtiene el valor que representa el flag como caracter.
	 * 
	 * @return <code>Y</code>, <code>N</code> o caracter de relleno
	 *         <code>*</code>.
	 */
	public Character getCharacterValue () {
		return booleanValue.isPresent() ? getValue() : MessageUtils.MESSAGE_PAD;
	}
	
	/**
	 * Indica si hay el campo tiene un flag correcto, <code>Y</code> o
	 * <code>N</code>.
	 * 
	 * @return <code>true</code> si el flag tiene un valor correcto
	 *         <code>Y</code>/<code>N</code>, <code>false</code> en caso
	 *         contrario.
	 */
	public boolean isFlagPresent() {
		return booleanValue.isPresent();
	}
	
	
	/**
	 * Crea un campo de tipo Flag a partir de un caracter.
	 *
	 * @param metaData
	 *            metadatos.
	 * @param value
	 *            Valor como caracter.
	 * @return Campo creado.
	 */
	public static FieldFlag create(final FieldMetaData metaData, final Character value) {
		return new FieldFlag(metaData, value);
	}
	
	
	/**
	 * Crea un campo de tipo Flag a partir de un caracter.
	 *
	 * @param metaData
	 *            metadatos.
	 * @param value
	 *            Valor como booleano.
	 * @return Campo creado.
	 */
	public static FieldFlag create(final FieldMetaData metaData, final Boolean value) {
		final Character ch = value == null ? MessageUtils.MESSAGE_PAD : (value ? YES_FLAG : NOT_FLAG);
		return create(metaData, ch);
	}
	
	
	/**
	 * Impresión del campo: Y si es <code>true</code>, N si es
	 * <code>false</code>, <code>*</code> si no hay valor.
	 * 
	 * @return Campo impreso.
	 */
	@Override
	public String printForMessage() {
		return String.valueOf(getCharacterValue());
	}
	
	/*
	 * Obtiene un opcional booleano correspondiente a 'Y', {@link #YES_FLAG} y
	 * 'N', {@link #NOT_FLAG}. Si no se corresponde con ninguno, devuelve un
	 * opcional vacío.
	 * 
	 * @param ch
	 *            Caracter correspondiente a 'Y', {@link #YES_FLAG} o 'N',
	 *            {@link #NOT_FLAG}. Si es otro caracter.
	 * @return Opcional booleano.
	 */
	private static Optional<Boolean> getBoolean (final Character ch) {
		final Optional<Boolean> op;
		if (ch != null) {
			op = ch.equals(YES_FLAG) 
				? Optional.of(Boolean.TRUE)
				: ch.equals(NOT_FLAG) ? Optional.of(Boolean.FALSE) : Optional.empty();
		} else {
			op = Optional.empty();
		}
		return op;
	}
}
