package libcomm.event;

import java.util.concurrent.atomic.AtomicInteger;

import commons.util.PrintUtils;

/**
 * Representa un evento lanzado por la librería de comunicaciones. 
 * <p>
 * 09/02/2016 00:53:05
 * </p>
 * @author Jorge Fdez. &lt;jfmillan@gmail.com&gt;
 * @version 1.0
 */
class Event<T> {
	private static final String PRINT_FORMAT = "Evento (%s) '%s', '%s'";
	private final static AtomicInteger counter = new AtomicInteger(0);
	
	private final EventType type;
	private final T event;
	private final int count;
	

	Event(final EventType type, final T ob) {
		this.type = type;
		this.event = ob;
		count = counter.incrementAndGet();
	}
	
	EventType getType () {
		return type;
	}

	T getEvent () {
		return event;
	}
	
	@Override
	public String toString() {
		return PrintUtils.format(PRINT_FORMAT, count, type, event);
	}
}
