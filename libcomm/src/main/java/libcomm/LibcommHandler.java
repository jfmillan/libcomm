package libcomm;

import libcomm.connection.ConnectionState;
import libcomm.context.ConnectionContext;
import libcomm.event.EventNotifier;
import libcomm.event.EventType;
import libcomm.exception.CommErrorType;
import libcomm.exception.CommunicationException;
import libcomm.layer.AbstractLayer;
import libcomm.layer.ILayerCommands;
import libcomm.layer.message.MessageLayer;
import libcomm.message.Message;
import libcomm.util.ErrorUtils;
import libcomm.util.MessageUtils;

import commons.log.Log;
import commons.util.Constants;
import commons.util.PrintUtils;

/**
 * Controla la conexión, envío y recepción de mensajes y desconexión de libcomm.
 * 
 * <p>
 * 10/01/2016 19:10:36
 * </p>
 * 
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
public class LibcommHandler extends AbstractLayer<Message<?>, Message<?>> 
		implements LibcommInterface<Message<?>> { /* Envía y observa mensajes.*/
	
	/* Formato de nombre para notificador de eventos: EventNotifier [%s-%s], donde el primer argumento es el nombre de 
	 * conexión y el segundo, el modo cliente o servidor */
	private static final String EVENT_NOTIFIER_FORMAT = "NotifierThread [%s-%s]";

	/* Notifica los eventos de Libcomm. */
	private final EventNotifier<Message<?>> notifier;

	/* Contexto de la conexión. */
	private final ConnectionContext context;
	
	/* Objeto para bloqueo y sincronización. */
	private final Object lock;

	/* Comandos a las capas inferiores. Enviará mensajes. */
	private final ILayerCommands<Message<?>> command;
	
	/* Estado actual de la conexión. */
	private volatile ConnectionState connectionState;
	
	/**
	 * Constructor de clase.
	 * 
	 * @param listener
	 *            Interfaz que será notificada de los eventos producidos por
	 *            Libcomm.
	 * @param context Contexto o configuración de la conexión.
	 */
	public LibcommHandler (final LibcommListener<Message<?>> listener, final ConnectionContext context) {
		this.notifier = new EventNotifier<>(
			PrintUtils.format(EVENT_NOTIFIER_FORMAT, context.getConnectionName(), context.getConnectionMode()), listener
		);
		this.context = context;
		this.command = new MessageLayer(context, this);
		this.lock = new Object();
		setConnectionState(ConnectionState.DISCONNECTED);
	}

	/** Establece la conexión. */
	@Override
	public void connect() {
		Log.info(this, PrintUtils.format("Iniciando conexión %s", context.printConnection()));
		CommunicationException error = null;
		try {
			synchronized (lock) {
				if (!allowsConnection()) {
					final Boolean updateState = !ConnectionState.CONNECTING.equals(connectionState);
					notifyError(CommErrorType.CONNECTION_ERROR, 
						"No se permite intento de conexión '%s'. Estado actual de la conexión '%s'", 
						updateState, context.printConnection(), getConnectionState());
					return;
				}
				setConnectionState(ConnectionState.CONNECTING);
				
				if (!notifier.isRunning()) {
					notifier.start();
				}
				command.connect();
			}
		} catch (final Exception e) {
			error = new CommunicationException(CommErrorType.CONNECTION_ERROR, e);
		}
		if (error != null) {
			notifyError(CommErrorType.CONNECTION_ERROR, "No se ha conseguido establecer la conexión '%s'", 
				context.printConnection()
			);
		}
	}

	/**
	 * Corta la conexión.
	 */
	@Override
	public void disconnect() {
		Log.info(this, PrintUtils.format("Iniciando desconexión %s", context.printConnection()));
		CommunicationException error = null;
		try {
			synchronized(lock) {
				if (!allowsDisconnection()) {
					notifyError(CommErrorType.DISCONNECTION_ERROR, 
						"No se permite desconectar '%s' en el estado actual de la conexión '%s'. Se ignora el intento de desconexión",
						context.printConnection(), getConnectionState());
					return;
				}
				setConnectionState(ConnectionState.DISCONNECTING);
				command.disconnect();
			}
		} catch (final Exception e) {
			error = new CommunicationException(CommErrorType.DISCONNECTION_ERROR, e);
		}
		if (error != null) {
			notifyError(CommErrorType.DISCONNECTION_ERROR, 
				error, "Error al desconectar '%s'", context.printConnection()
			);
		}
	}

	/** Envía un mensaje {@link Message}. */
	@Override
	public void send(final Message<?> message) {
		CommunicationException error = null;
		try {
			synchronized(lock) {
				if (!isConnected()) {
					notifyError(CommErrorType.SENDING, "No se pueden enviar mensaje '%s'. Desconectado.", message);
				}
				Log.info(this, PrintUtils.format("Enviando mensaje '%s'", message));
				MessageUtils.check(message);
				command.send(message);
			}
		} catch (final Exception e) {
			error = new CommunicationException(CommErrorType.SENDING, e);
		}
		if (error != null) {
			notifyError(CommErrorType.SENDING, error, "Error enviando mensaje '%s'", message);
		}
	}

	/** Notificación de mensaje recibido. Se informa al listener. */
	@Override
	public void receive(final Message<?> message) {
		synchronized(lock) {
			if (!isConnected()) {
				notifyError(
					CommErrorType.COMMUNICATION_ERROR, "No se puede recibir mensaje '%s'. Desconectado.", message
				);
			}
		}
		this.notifier.addEvent(EventType.MESSAGE_RECEIVED, message);
	}

	/**
	 * Comprueba si estamos conectados.
	 */
	private boolean isConnected() {
		return ConnectionState.CONNECTED.equals(getConnectionState());
	}
	
	/** Notificación de conexión. Se informa al listener. */
	@Override
	public void connected() {
		setConnectionState(ConnectionState.CONNECTED);
		Log.info(this, PrintUtils.format("Conectado '%s'", context.printConnection()));
		this.notifier.addEvent(EventType.CONNECTED, context);
	}

	/** Notificación de desconexión. Se informa al listener. */
	@Override
	public void disconnected() {
		setDisconnection();
		
		Log.info(this, PrintUtils.format("Desconectado '%s'", context.printConnection()));
		this.notifier.addEvent(EventType.DISCONNECTED, context);
		this.notifier.stop(); 
	}

	/**
	 * Notificación de desconexión con error. Se informa al listener del error y
	 * de la desconexión. Si se cumplen una serie de condiciones se intenta
	 * reconectar.
	 */
	@Override
	public void disconnected(final CommunicationException cause) {
		final ConnectionState previousState = setDisconnection();

		Log.error(this, PrintUtils.format("Desconectado '%s' con error", context.printConnection()), cause);
		this.notifier.addEvent(EventType.ERROR, cause);
		this.notifier.addEvent(EventType.DISCONNECTED, context);
		
		/* Aparte de log, sirve para reinicia el estado interrupted en caso de haberse interrumpido. */
		Log.debug(this, PrintUtils.format("Hilo interrumpido en %s: %s", context.getConnectionMode(), Thread.interrupted()));
		
		if (canReconnect(previousState, cause)) {
			final int waitSeconds = 1;
			Log.debug(this, PrintUtils.format("Estado anterior a la desconexión con error: %s. Intentando reconectar tras %ss...",
				previousState, waitSeconds)
			);
			
			try {
				Thread.sleep(waitSeconds * Constants.SECOND);
			} catch (InterruptedException e) {
				Log.error(this, "Hilo interrumpido inesperadamente mientras se esperaba reconectar", e);
			}
			connect();
		} else {
			this.notifier.stop(); 
		}
	}
	
	/* Establece si se puede reconectar tras una desconexión o se debe permanecer desconectado. Para poder reconectar el 
	 * estado anterior debe ser CONECTADO, y no haberse tratado de algún error fatal declarado en CommErrorType#FATAL_ERRORS.
	 */
	private boolean canReconnect(final ConnectionState state, final CommunicationException cause) {
		if (!ConnectionState.CONNECTED.equals(state)) {
			return false; /* El unico estado previo que permite reconexión es CONECTADO */
		}
		
		return !ErrorUtils.findErrorType(cause, CommErrorType.FATAL_ERRORS);
	}

	/* Establece el estado desconectado y devuelve el estado previo. */
	private ConnectionState setDisconnection() {
		final ConnectionState previous;
		synchronized (lock) {
			previous = connectionState;
			setConnectionState(ConnectionState.DISCONNECTED);
		}
		return previous;
	}

	@Override
	public void error(final String error, final CommunicationException cause) {
		notifyError(CommErrorType.COMMUNICATION_ERROR, cause, "Error de comunicación: %s", error);
	}
	

	/* Notifica al observador un error durante una desconexión. Comprueba si debe actualizar el estado de conexión. */
	private void notifyError(final CommErrorType errorType, final String message, final Object... params) {
		notifyError(errorType, message, Boolean.TRUE, params);
	}
	
	/* Notifica al observador un error durante una desconexión. */
	private void notifyError(final CommErrorType errorType, final String message, final Boolean updateStateWhenError, final Object... params) {
		notifyError(errorType, null, updateStateWhenError, message, params);
	}

	/*
	 * Notifica al observador un error durante una desconexión. Se puede acompañar de una excepción motivo del 
	 * error (opcional). Comprueba si debe actualizar el estado de conexión.
	 */
	private void notifyError(final CommErrorType errorType, final Throwable cause, final String message, final Object... params) {
		notifyError(errorType, cause, Boolean.TRUE, message, params);
	}
	
	/* Notifica al observador un error durante una desconexión. Se puede acompañar de una excepción motivo del error (opcional). */
	private void notifyError(final CommErrorType errorType, final Throwable cause, final Boolean updateStateWhenError, final String message, final Object... params) {
		final String __message = PrintUtils.format(message, params);
		final CommunicationException error = CommunicationException.createException(errorType, __message, context, cause);
		if (Boolean.TRUE.equals(updateStateWhenError)) { /* controla nulo */
			updateStateWhenError();
		}
		this.notifier.addEvent(EventType.ERROR, error);
	}
	
	/**
	 * Devuelve el parámetro indicado.
	 * @return parámetro connectionState a devolver.
	 */
	@Override
	public ConnectionState getConnectionState() {
		return connectionState;
	}

	/**
	 * Establece el estado de la conexión.
	 * @param connectionState Parámetro a establecer en connectionState.
	 */
	private void setConnectionState(final ConnectionState connectionState) {
		synchronized (lock) {
			this.connectionState = connectionState;	
		}
	}
	
	/**
	 * Indica si se permite conectar en función del estado actual de la
	 * conexión.
	 * 
	 * @return <code>true</code> si se permite conectar, <code>false</code> en
	 *         caso contrario.
	 */
	private boolean allowsConnection() {
		synchronized (lock) {
			return ConnectionState.allowsConnection(connectionState);
		}
	}
	
	/**
	 * Indica si se permite desconectar en función del estado actual de la
	 * conexión.
	 * 
	 * @return <code>true</code> si se permite desconectar, <code>false</code> en
	 *         caso contrario.
	 */
	private boolean allowsDisconnection() {
		synchronized (lock) {
			return ConnectionState.allowsDisconnection(connectionState);
		}
	}
	
	/*
	 * Actualiza el estado de la conexión ante un error, si es necesario. En
	 * caso de que la librería esté conectada o desconectada, el error dejará el
	 * estado inalterado. Sin embargo, en caso de que el estado actual sea
	 * conectándose o desconectándose, el estado pasaría a desconectado.
	 */
	private void updateStateWhenError () {
		synchronized (lock) {
			if (ConnectionState.CONNECTING.equals(connectionState) 
					|| ConnectionState.DISCONNECTING.equals(connectionState)) {
				this.connectionState = ConnectionState.DISCONNECTED;
				Log.error(this, PrintUtils.format("Error provoca cambio a estado '%s'", connectionState));
			}
		}
	}
}
