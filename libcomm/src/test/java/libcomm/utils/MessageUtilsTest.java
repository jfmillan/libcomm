package libcomm.utils;

import java.util.Random;

import commons.util.PrintUtils;

/**
 * Utilidades estáticas para tests y pruebas de mensajería. Ayudarán a crear mensajes con
 * un contenido aleatorio de cualquier tipo.
 * <p>
 * 16/03/2016 20:56:51
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class MessageUtilsTest {
	private final static Random RANDOM = new Random(System.currentTimeMillis());
	
	/* Caracteres con las letras del abecedario. */
	private static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/* Caracteres numéricos. */
	private static final String NUMBERS = "0123456789";
	
	/* Caracteres alfanuméricos */
	private static final String ALPHANUMERICS = PrintUtils.format("%s%s", ALPHA, NUMBERS);
	
	/* Caracteres para flag */
	private static final String FLAG = "YN";
	  
	/**
	 * Crea un campo String con contenido aleatorio en cuanto a longitud y
	 * contenido. La longitud estará dentro de los márgenes establecidos por el
	 * mínimo y el máximo, ambos incluidos. El contenido de cada carácter vendrá
	 * determinado por el String que se utilice.
	 * 
	 * @param characters
	 *            Caracteres permitidos.
	 * @param min
	 *            Tamaño mínimo.
	 * @param max
	 *            Tamaño máximo.
	 * @return Campo aleatorio.
	 */
	public static String createField (final String characters, final int min, final int max) {
		final int __min = Math.min(min, max);
		final int __max = Math.max(min, max)+1; /* +1 para incluir el propio maximo en el random */
		
		final StringBuilder sb = new StringBuilder();
		
		final int length = RANDOM.nextInt(__max - __min) + __min;
		
		for (int i = 0; i < length; i++) {
			sb.append(characters.charAt(RANDOM.nextInt(characters.length())));
		}
		
		return sb.toString();
	}
	
	/**
	 * Crea un campo con caracteres del alfabeto, de tamaño entre un mínimo y un
	 * máximo.
	 * 
	 * @param min
	 *            Tamaño mínimo.
	 * @param max
	 *            Tamaño máximo.
	 * @return Campo aleatorio con caracteres del alfabeto.
	 */
	public static String createAlpha (final int min, final int max) {
		return createField(ALPHA, min, max);
	}
	
	/**
	 * Crea un campo numérico, de tamaño entre un mínimo y un máximo.
	 * 
	 * @param min
	 *            Tamaño mínimo.
	 * @param max
	 *            Tamaño máximo.
	 * @return Campo aleatorio con números.
	 * */
	public static String createNumeric (final int min, final int max) {
		return createField(NUMBERS, min, max);
	}

	/**
	 * Crea un campo alfanumérico, de tamaño entre un mínimo y un máximo.
	 * 
	 * @param min
	 *            Tamaño mínimo.
	 * @param max
	 *            Tamaño máximo.
	 * @return Campo aleatorio alfanumérico.
	 * */
	public static String createAlphanumeric (final int min, final int max) {
		return createField(ALPHANUMERICS, min, max);
	}
	
	/**
	 * Crea un campo flag de tamaño siempre 1.
	 * 
	 * @return Flay Y o N.
	 */
	public static String createFlag () {
		return createField(FLAG, 1, 1);
	}
}
