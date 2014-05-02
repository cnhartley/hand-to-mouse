/**
 * 
 */
package project.recognition.event;

import javax.swing.event.ChangeEvent;

/**
 * The GestureEvent is used to notify interested parties that a particular 
 * gesture has been successfully identified within a video capture frame.
 * 
 * @author Chris Hartley
 * @author Adin Miller
 */
public class GestureEvent extends ChangeEvent {

	
	/**
	 * serial version user id
	 */
	private static final long serialVersionUID = -5190362653877604071L;

	
	/**
	 * Gesture event id indicating that an opened hand was detected.
	 */
	public static final int OPENED_HAND_DETECTED = 0x00;

	
	/**
	 * Gesture event id indicating that a closed hand was detected.
	 */
	public static final int CLOSED_HAND_DETECTED = 0x01;

	
	// Member data.
	private final int id;
	private final long when;
	private final int x;
	private final int y;
	
	
	/**
	 * Constructor for a new instance of a gesture event with the specified
	 * parameters of the source object, the event id type, when the event 
	 * occurred, and the center of gravity for the detected gesture region. 
	 * 
	 * @param source	The source {@link Object} from which this gesture event
	 * 					was created.
	 * @param id		The identification of which type of gesture event this
	 * 					instance represents. Either {@link #OPENED_HAND_DETECTED}
	 * 					or {@link #CLOSED_HAND_DETECTED}.
	 * @param when		The time, in milliseconds, of when this event occurred.
	 * @param x			The center of gravity's X-coordinate for this events
	 * 					location on screen.
	 * @param y			The center of gravity's Y-coordinate for this events
	 * 					location on screen.
	 */
	public GestureEvent(Object source, int id, long when, int x, int y) {
		super(source);

		this.id = id;
		this.when = when;
		this.x = x;
		this.y = y;
	}
	
	
	/**
	 * Constructor for a new instance of a gesture event with the specified
	 * parameters of the source object, the event id type, and the center of 
	 * gravity for the detected gesture region.
	 * 
	 * @param source	The source {@link Object} from which this gesture event
	 * 					was created.
	 * @param id		The identification of which type of gesture event this
	 * 					instance represents. Either {@link #OPENED_HAND_DETECTED}
	 * 					or {@link #CLOSED_HAND_DETECTED}.
	 * @param pt		The {@link java.awt.Point} of the center of gravity for
	 * 					this events location on screen.
	 * 
	 * @see GestureEvent#GestureEvent(Object, int, long, int, int)
	 * @see java.awt.Point
	 */
	public GestureEvent(Object source, int id, java.awt.Point pt) {
		this(source, id, System.currentTimeMillis(), pt.x, pt.y);
	}
	
	
	/**
	 * Constructor for a new instance of a gesture event with the specified
	 * parameters of the source object, the event id type, and the center of 
	 * gravity for the detected gesture region.
	 * 
	 * @param source	The source {@link Object} from which this gesture event
	 * 					was created.
	 * @param id		The identification of which type of gesture event this
	 * 					instance represents. Either {@link #OPENED_HAND_DETECTED}
	 * 					or {@link #CLOSED_HAND_DETECTED}.
	 * @param pt		The {@link org.opencv.core.Point} of the center of 
	 * 					gravity for this events location on screen.
	 * 
	 * @see GestureEvent#GestureEvent(Object, int, long, int, int)
	 * @see org.opencv.core.Point
	 */
	
	public GestureEvent(Object source, int id, org.opencv.core.Point pt) {
		this(source, id, System.currentTimeMillis(), (int)pt.x, (int)pt.y);
	}
	
	
	/**
	 * Constructor for a new instance of a gesture event with the specified
	 * parameters of the source object, the event id type, and the center of
	 * gravity for the detected gesture region. 
	 * 
	 * @param source	The source {@link Object} from which this gesture event
	 * 					was created.
	 * @param id		The identification of which type of gesture event this
	 * 					instance represents. Either {@link #OPENED_HAND_DETECTED}
	 * 					or {@link #CLOSED_HAND_DETECTED}.
	 * @param x			The center of gravity's X-coordinate for this events
	 * 					location on screen.
	 * @param y			The center of gravity's Y-coordinate for this events
	 * 					location on screen.
	 */
	public GestureEvent(Object source, int id, int x, int y) {
		this(source, id, System.currentTimeMillis(), x, y);
	}
	
	
	/**
	 * Returns this instance gesture event id type. Either
	 * {@link #OPENED_HAND_DETECTED} or {@link #CLOSED_HAND_DETECTED}.
	 * 
	 * @return	this instance gesture event id type.
	 */
	public final int getID() {
		return id;
	}
	
	
	/**
	 * Returns this instance time, in milliseconds, of when the event was fired.
	 * 
	 * @return	this instance time of when the event was fired.
	 */
	public final long getWhen() {
		return when;
	}
	
	
	/**
	 * Returns the center of gravity location as a {@link java.awt.Point} for
	 * this instance of the gesture event.
	 * 
	 * @return	the location of the center of gravity for the detected region.
	 */
	public final java.awt.Point getLocation() {
		return new java.awt.Point(x, y);
	}
	
	
	/**
	 * Returns the X-coordinate as an {@code int} of the center of gravity
	 * location for this instance of the gesture event.
	 * 
	 * @return	the X-coordinate for the center of gravity of the detected
	 * 			region.
	 */
	public final int getX() {
		return x;
	}
	
	
	/**
	 * Returns the Y-coordinate as an {@code int} of the center of gravity
	 * location for this instance of the gesture event.
	 * 
	 * @return	the Y-coordinate for the center of gravity of the detected
	 * 			region.
	 */
	public final int getY() {
		return y;
	}
	
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ "source=" + getSource() + ","
				+ "id" + getID() + ","
				+ "when" + getWhen() + ","
				+ "location=" + getLocation() + "]";
	}

}
