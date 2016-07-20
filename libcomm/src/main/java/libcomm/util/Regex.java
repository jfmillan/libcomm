package libcomm.util;

import java.util.regex.Pattern;

import libcomm.message.FieldFlag;
import libcomm.message.FieldMetaData;
import libcomm.message.MessageType;

import commons.util.PrintUtils;

/**
 * Expresiones regulares asociadas a cada campo. Provee métodos para formar
 * expresiones para cualquier mensaje, uniendo como grupo la expresión regular
 * de cada campo.
 * <p>
 * Para los grupos de mensaje válidos se recurre al enumerado
 * {@link MessageType}.
 * </p>
 * <p>
 * 17/01/2016 14:24:10
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public enum Regex {

	/**
	 * Número de secuencia: números hexadecimales del <code>0000</code> al
	 * <code>FFFF</code>, sin <code>0x</code>. Admite que toda la cadena venga
	 * vacía como <code>****</code> pero no admite un número parcial, por
	 * ejemplo <code>A1C*</code> o <code>*A1C</code> sería inválido. Se añaden a
	 * la expresión regular algunas condiciones previas para ayudar al
	 * compilador a encontrar errores.
	 */
	SEQUENCE_NUMBER(PrintUtils.format("(?=.{%s}$)(?![G-Za-z\\s])([\\dA-F]+|\\%s+)$", 
		FieldMetaData.SEQUENCE_NUMBER.getLength(), MessageUtils.MESSAGE_PAD)),

	/**
	 * Tipo de mensaje válido: Cualquiera de los tipos definidos en
	 * {@link MessageType}.
	 */
	MESSAGE_TYPE(PrintUtils.format("(?=.{%s}$)(%s)", FieldMetaData.MESSAGE_TYPE.getLength(), 
		MessageType.printAllForRegex())),
	
	/**
	 * Contenido de mensaje válido, genérico: cualquier combinación de dígito
	 * <code>0-9</code>, letras mayúsculas <code>A-Z</code> (no incluye
	 * especiales ni <code>Ñ</code>, y caracter de relleno <code>*</code>. Se
	 * añaden a la expresión regular algunas condiciones previas para ayudar al
	 * compilador a encontrar errores.
	 */
	VALID_CONTENT (PrintUtils.format("(?![a-z\\s])([\\dA-Z\\%s]*)", MessageUtils.MESSAGE_PAD)),
	
	/**
	 * Identificador de bulto: cualquier combinación de números decimales
	 * seguidos de caracteres de asterisco, hasta 12 caracteres de longitud
	 * total. Los asteriscos sólo se permiten, si aparecen, a la derecha de los
	 * números. Cadenas válidas son <code>123456789012</code>,
	 * <code>1234567890**</code> o <code>************</code>. Se añaden a la
	 * expresión regular algunas condiciones previas para ayudar al compilador a
	 * encontrar errores.
	 */
	PACKAGE_ID (PrintUtils.format("(?=.{%s}$)(?![a-zA-Z\\s])(\\d*)(\\%s*)$", 
		FieldMetaData.PACKAGE_ID.getLength(), MessageUtils.MESSAGE_PAD)),
	
	/**
	 * Identificador de posición: cualquier combinación de números decimales y
	 * caracteres. No se permite que venga vacía o parcial (con asteriscos de
	 * relleno). Se añaden a la expresión regular algunas condiciones previas
	 * para ayudar al compilador a encontrar errores.
	 */
	POSITION (PrintUtils.format("(?=.{%s}$)(?![a-z\\s])([A-Z0-9]+)$",
		FieldMetaData.POSITION.getLength())),
	
	/**
	 * Peso: cualquier combinación de números decimales
	 * seguidos de caracteres de asterisco, hasta 5 caracteres de longitud
	 * total. Los asteriscos sólo se permiten, si aparecen, a la derecha de los
	 * números. Cadenas válidas son <code>12345</code>,
	 * <code>123**</code> o <code>*****</code>. Se añaden a la
	 * expresión regular algunas condiciones previas para ayudar al compilador a
	 * encontrar errores.
	 */
	WEIGHT (PrintUtils.format("(?=.{%s}$)(?![a-zA-Z\\s])(\\d*)(\\%s*)$", 
		FieldMetaData.WEIGHT.getLength(), MessageUtils.MESSAGE_PAD)),
		
	/** Requerimiento de respuesta: Y o N, obligatorio, no se admite vacío o relleno, <code>*</code>. */
	REQUIRED_REPLY(PrintUtils.format("(?=.{%s}$)(%s|%s)", FieldMetaData.REQUIRED_REPLY.getLength(), 
		FieldFlag.YES_FLAG, FieldFlag.NOT_FLAG)),

	/** Posición habilitada: Y o N, obligatorio, no se admite vacío o relleno, <code>*</code>. */
	ENABLED_POSITION(PrintUtils.format("(?=.{%s}$)(%s|%s)", FieldMetaData.ENABLED_POSITION.getLength(), 
		FieldFlag.YES_FLAG, FieldFlag.NOT_FLAG)),
	
	/**
	 * Expresión regular para identificar un mensaje recibido. El contenido
	 * concreto tendrá que ser evaluado por otras expresiones.
	 * <ul>
	 * <li>Número de secuencia del mensaje, {@link #SEQUENCE_NUMBER}.
	 * <li>Tipo de mensaje, {@link #MESSAGE_TYPE}.
	 * <li>Contenido de mensaje, cualquier secuencia de letras entre la A y la Z
	 * (sin caractere especiales), y números del 0 al 9. También se admite el
	 * caracter de relleno <code>*</code>.
	 * </ul>
	 */
	MESSAGE (PrintUtils.format("(?=^[\\dA-Z\\%s]{%s,}$)^([\\dA-F]{%s}|\\%s{%s})(%s)([\\dA-Z\\%s]+)$", 
		MessageUtils.MESSAGE_PAD, (FieldMetaData.SEQUENCE_NUMBER.getLength() + FieldMetaData.MESSAGE_TYPE.getLength()),
		FieldMetaData.SEQUENCE_NUMBER.getLength(), MessageUtils.MESSAGE_PAD, FieldMetaData.SEQUENCE_NUMBER.getLength(), 
		MessageType.printAllForRegex(), MessageUtils.MESSAGE_PAD)), 
		// (?=^[\\dA-Z\\*]{6,}$)^([\\dA-F]{4}|\\*{4})(AK|PR|GT|ST)([\\dA-Z\\*]+)$"
	
	/**
	 * Expresión regular para contenido de mensaje ST, <i>state (of
	 * position)</i>.
	 * <ul>
	 * <li>Número de secuencia del mensaje, {@link #SEQUENCE_NUMBER}.
	 * <li>Tipo de mensaje, {@link #MESSAGE_TYPE}.
	 * <li>Identificador de la posición, {@link #POSITION}.
	 * <li>Indicador de posición habilitada o deshabilitada,
	 * {@link #ENABLED_POSITION}.
	 * </ul>
	 */
	MESSAGE_CONTENT_ST (PrintUtils.format("(?=^[\\dA-Z]{%s}$)^(?![a-z\\s])([\\dA-Z]{%s})(%s|%s)$",
		(FieldMetaData.POSITION.getLength() + FieldMetaData.REQUIRED_REPLY.getLength()),
		FieldMetaData.POSITION.getLength(), FieldFlag.YES_FLAG, FieldFlag.NOT_FLAG)),
	//(?=^[\\dA-Z]{5}$)^(?![a-z\\s])([\\dA-Z]{4})(Y|N)$

		
	/**
	 * Expresión regular para contenido de mensaje PR, <i>position reached</i>.
	 * <ul>
	 * <li>Número de secuencia del mensaje, {@link #SEQUENCE_NUMBER}.
	 * <li>Tipo de mensaje, {@link #MESSAGE_TYPE}.
	 * <li>Identificador de bulto, {@link #PACKAGE_ID}.
	 * <li>Identificador de la posición, {@link #POSITION}.
	 * <li>Peso en gramos, {@link #WEIGHT}.
	 * <li>Indicador de si necesita respuesta, {@link #REQUIRED_REPLY}.
	 * </ul>
	 */
	MESSAGE_CONTENT_PR (PrintUtils.format(
		"(?=[\\dA-Z\\%s]{%s}$)(\\d{0,%s}\\%s{0,%s})(?<=[\\d\\%s]{%s})([\\dA-Z]{%s})(?=[\\d\\%s%s%s]{%s})(\\d{0,%s}\\%s{0,%s})(%s|%s)",
		MessageUtils.MESSAGE_PAD, (FieldMetaData.PACKAGE_ID.getLength() + FieldMetaData.POSITION.getLength() 
		+ FieldMetaData.WEIGHT.getLength() + FieldMetaData.REQUIRED_REPLY.getLength()), FieldMetaData.PACKAGE_ID.getLength(),
		MessageUtils.MESSAGE_PAD, FieldMetaData.PACKAGE_ID.getLength(), MessageUtils.MESSAGE_PAD, FieldMetaData.PACKAGE_ID.getLength(),
		FieldMetaData.POSITION.getLength(), MessageUtils.MESSAGE_PAD, FieldFlag.YES_FLAG, FieldFlag.NOT_FLAG,
		(FieldMetaData.WEIGHT.getLength() + FieldMetaData.REQUIRED_REPLY.getLength()), FieldMetaData.WEIGHT.getLength(),
		MessageUtils.MESSAGE_PAD, FieldMetaData.WEIGHT.getLength(), FieldFlag.YES_FLAG, FieldFlag.NOT_FLAG)),
//	(?=[\\dA-Z\\*]{22}$)(\\d{0,12}\\*{0,12})(?<=[\\d\\*]{12})([\\dA-Z]{4})(?=[\\d\\*YN]{6})(\\d{0,5}\\*{0,5})(Y|N)


	/**
	 * Expresión regular para contenido de mensaje GT, <i>go to</i>.
	 * <ul>
	 * <li>Número de secuencia del mensaje, {@link #SEQUENCE_NUMBER}.
	 * <li>Tipo de mensaje, {@link #MESSAGE_TYPE}.
	 * <li>Identificador de bulto, {@link #PACKAGE_ID}.
	 * <li>Identificador de la posición, {@link #POSITION}.
	 * </ul>
	 */
	MESSAGE_CONTENT_GT (PrintUtils.format("(?=[\\dA-Z\\%s]{%s}$)(\\d{0,%s}\\%s{0,%s})(?<=[\\d\\%s]{%s})([\\dA-Z]{%s})$",
		MessageUtils.MESSAGE_PAD, (FieldMetaData.PACKAGE_ID.getLength() + FieldMetaData.POSITION.getLength()),
		FieldMetaData.PACKAGE_ID.getLength(), MessageUtils.MESSAGE_PAD, FieldMetaData.PACKAGE_ID.getLength(), 
		MessageUtils.MESSAGE_PAD, FieldMetaData.PACKAGE_ID.getLength(), FieldMetaData.POSITION.getLength())),
//		(?=[\\dA-Z\\*]{16}$)(\\d{0,12}\\*{0,12})(?<=[\\d\\*]{12})([\\dA-Z]{4})$

	/**
	 * Expresión regular para mensaje AK, <i>acknowgledgement</i>.
	 * <ul>
	 * <li>Número de secuencia del mensaje confirmado, {@link #SEQUENCE_NUMBER}.
	 * </ul>
	 */
	MESSAGE_CONTENT_AK (PrintUtils.format("(?=.{%s}$)(?![G-Za-z\\s])([\\dA-F]+|\\%s+)$", 
		FieldMetaData.SEQUENCE_NUMBER.getLength(), MessageUtils.MESSAGE_PAD));
	
	/* Expresión regular. */
	private String regex;

	/* Constructor privado. */
	private Regex (final String regex) {
		this.regex = regex;
	}
	
	/**
	 * Obtiene la expresión regular almacenada.
	 * 
	 * @return Expresión regular almacenada.
	 */
	public String getRegex () {
		return this.regex;
	}
	
	/**
	 * Obtiene el patrón compilado para la expresión regular almacenada.
	 * 
	 * @return Patrón compilado para la expresión regular almacenada.
	 */
	public Pattern getCompiledPattern () {
		return Pattern.compile(this.regex);
	}
}
