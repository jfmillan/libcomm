package libcomm.encoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import libcomm.exception.MalformedMessageException;
import libcomm.util.MessageUtils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import commons.log.ConfigureLog;
import commons.log.Log;
import commons.log.LogSystem;
import commons.util.PrintUtils;

/**
 * Test para comprobar el codificador de mensajes a bytes.
 * <p>
 * 15/03/2016 21:29:07
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class EncoderDecoderTest {
	/* Número de intentos que se codificarán y recodificarán. */
	private static final int N = 500;
	
	/* Número máximo de frases a concatenar */
	private static final int MAX_CONCAT = 8;
	
	/* Número de hilos simultaneos para prueba multihilo. */
	private static final int THREAD_N = 5;
	
	private static final List<String> expected = Arrays.asList(
		"abcdefghijklmnopqrstuvwxyz",
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ",
		"0123456789_-.,;[]{}!·$%&/()=?¿¡\\",
		"ÁÉÍÓÚáéíóú",
		"ÀÈÌÒÙàèìòù",
		"ÄËÏÖÜäëïöü",
		"<-ñÑ->",
		"Ãã€Õõ~*",
		"|<->|",
		"The quick brown fox jumps over the lazy dog",
		"El veloz murciélago hindú comía feliz cardillo y kiwi. La cigüeña tocaba el saxofón detrás del palenque de paja"
	);

	/* Lista de Strings, mezcla de lista anterior 'expected' de forma aleatoria */
	private static List<String> expectedMix = createSourceMix();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConfigureLog.configure(LogSystem.DEFAULT);
	}
	
	/** Codifica y decodifica una vez cada elemento de la lista expected. 
	 * @throws MalformedMessageException en caso de error.
	 */
	@Test
	public void encodeDecodeOnce() throws MalformedMessageException {
		Assert.assertEquals(expected.size(), encoderDecoder(expected));
	}

	/** Codifica y decodifica una vez cada elemento de la lista expected. 
	 * @throws MalformedMessageException en caso de error.
	 */
	@Test
	public void encodeDecodeN() throws MalformedMessageException {
		Assert.assertEquals(N, encoderDecoder(expectedMix));
	}

	/**
	 * Codifica y decodifica una vez cada elemento de la lista en varios hilos
	 * simultaneamente.
	 * 
	 * @throws MalformedMessageException
	 *             en caso de error.
	 * @throws InterruptedException
	 *             en caso de error.
	 */
	@Test
	public void encodeDecodeMultiThread() throws MalformedMessageException, InterruptedException {
		List<Thread> threads = new ArrayList<>();
		final List<Throwable> errors = new ArrayList<>();
		
		/* CountDown's latch para controlar el comienzo simultaneo y la espera a que todos finalicen. */
		final CountDownLatch startSignal = new CountDownLatch(1);
	    final CountDownLatch doneSignal = new CountDownLatch(THREAD_N);
		
		for (int i = 0; i < THREAD_N; i++) {
			final String name = PrintUtils.format("Hilo '%s'", i+1);
			
			final Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						startSignal.await();
						Log.debug(this, PrintUtils.format("Iniciado '%s'", name));
						Assert.assertEquals(N, encoderDecoder(expectedMix));
					} catch (Exception e) {
						errors.add(e);
						throw new IllegalArgumentException(e);
					} finally {
						doneSignal.countDown();
						Log.debug(this, PrintUtils.format("Finalizado '%s'", name));
					}
				}
			};
			
			final Thread thread = new Thread(runnable, name);
			thread.setDaemon(Boolean.TRUE);
			threads.add(thread);
		}
		
		try {
			threads.iterator().forEachRemaining((Thread t) -> t.start());
		} catch (Throwable ex) {
			Log.error(this, "Error por excepción", ex);
			Assert.fail(PrintUtils.format("Error por excepción '%s'", ex.getMessage()));
		}
		
		/* Comenzamos todos los hilos a la vez. */
		startSignal.countDown();
		
		/* Esperamos a que todos terminen */
		doneSignal.await();

		for (final Throwable t : errors) {
			Log.error(this, "Error por excepción", t);
		}
		
		/* No hay errores */
		Assert.assertEquals(0, errors.size());
	}
	
	/* Crea un mezcladillo de frases fusionando frases de expected aleatoriamente */
	private static List<String> createSourceMix () {
		final List<String> result = new ArrayList<>();
		final Random random = new Random(System.currentTimeMillis());
		
		int concatN = 0;
		int count = 0;
		for (int i = 0; i < N; i++) {
			/* Añadiremos a source entre 1 y MAX_CONCAT frases de expected, en cualquier orden, pudiendo repetirse. */
			concatN = random.nextInt(MAX_CONCAT) + 1;
			StringBuilder sb = new StringBuilder();
			
			int currentPhrase = 0;
			for (int c = 0; c < concatN; c++) {
				currentPhrase = random.nextInt(expected.size());
				sb.append (expected.get(currentPhrase));
			}
			
			Log.debug(EncoderDecoderTest.class, PrintUtils.format("createSourceMix, creada frase %s de %s: '%s'", 
				++count, N, sb.toString())
			);
			result.add(sb.toString());
		}
		
		return result;
	}
	
	/* Codifica todos los elementos de la lista y los vuelve a decodificar. */
	private int encoderDecoder(final List<String> source) throws MalformedMessageException {
		Log.debug(this, PrintUtils.format("Frases a comprobar: '%s' ##############################", source.size()));
		int counter = 0;
		for (final String expectedStr : source) {
			Log.debug(this, PrintUtils.format("Codificamos: %s", expectedStr));
			
			final byte[] encoded = MessageUtils.encode(expectedStr);
			Log.debug(this, PrintUtils.format("Resultado decodificado: %s", encoded));
			Assert.assertNotNull("Codificado no es null", encoded);
			Assert.assertNotEquals("Codificado no es vacío", encoded.length, 0);

			final String decoded = MessageUtils.decode(encoded);
			Log.debug(this, PrintUtils.format("Resultado recodificado: %s", decoded));
			Assert.assertNotNull("Decodificado no es null", decoded);
			Assert.assertNotEquals("Decodificado no es vacío", decoded.length(), 0);
			
			Log.debug(this, PrintUtils.format("Esperado original: %s", expectedStr));
			Log.debug(this, PrintUtils.format("Recodificado.....: %s", decoded));
			Assert.assertEquals("Redecodificado es igual a esperado original", expectedStr, decoded);
			
			counter++;
		}
		
		return counter;
	}
}
