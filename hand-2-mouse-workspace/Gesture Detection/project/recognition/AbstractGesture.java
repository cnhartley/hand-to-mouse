/**
 * 
 */
package project.recognition;

import java.awt.Color;
import java.io.Serializable;

import javax.swing.event.EventListenerList;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import project.recognition.event.GestureEvent;
import project.recognition.event.GestureListener;

/**
 * <p>This provides an abstract class for any detectable gesture based on the
 * video capture image matrix returned from the OpenCV interface.
 * <p>This abstract class is required in order for the {@link GestureRecognizer}
 * to call its {@code detect(Mat)} method where the specific detection is
 * implemented.
 * <p>Example:
 * <p><code>
 *<pre>public class GestureX extends AbstractGesture {
 *
 *	private static final SOME_GESTURE_ID = GestureEvent.OPENED_HAND_DETECTED;
 *	                                    // GestureEvent.CLOSED_HAND_DETECTED;
 *
 *	public GestureX() {
 *		super("Gesture X", "...", true);
 *		addGestureListener( ... );  // some GestureListener
 *	}
 *
 *	public void detect(Mat mat) {
 *		boolean hasDetected = false;
 *		Point centerPoint = null;
 *
 *		// 1. Process the image matrix...
 *
 *		// 2. Update hasDetected to true if found and set the centerPoint...
 *
 *		if (hasDetected)
 *			fireGestureDetected(SOME_GESTURE_ID, centerPoint.x, centerPoint.y);
 *	}
 *}</pre>
 * </code>
 * 
 * @author Chris Hartley
 * @author Adin Miller
 */
public abstract class AbstractGesture implements Serializable {

	/*
	 * The unique serial version user interface identification number.
	 */
	private static final long serialVersionUID = -8866579913710676820L;
	
	/*
	 * The event listener list containing all registered event listeners for
	 * this instance of the gesture.
	 */
	private final EventListenerList listenerList = new EventListenerList();

	// Member data.
	private boolean enabled = true;
	private String name = null;
	private String desc = "";
	
	
	/**
	 * Constructor for a new instance of this gesture with the specified name
	 * and set to have no description and the default of enabled.
	 * 
	 * @param name	The {@link String} name for this gesture.
	 */
	public AbstractGesture(String name) {
		this(name, null, true);
	}
	
	
	/**
	 * Constructor for a new instance of this gesture with the specified name
	 * and whether the gesture is enabled or not. This does not have a 
	 * description associated by default.
	 * 
	 * @param name		The {@link String} name for this gesture.
	 * @param enabled	The {@code boolean} value indicating if the gesture is
	 * 					enabled, {@code true} or not {@code false}.
	 */
	public AbstractGesture(String name, boolean enabled) {
		this(name, null, enabled);
	}
	
	
	/**
	 * Constructor for a new instance of this gesture with the specified name
	 * and description. By default, this new instance will be enabled.
	 * 
	 * @param name		The {@link String} name for this gesture.
	 * @param desc		The {@link String} description for this gesture.
	 */
	public AbstractGesture(String name, String desc) {
		this(name, desc, true);
	}
	
	
	/**
	 * Constructor for a new instance of this gesture with the specified name,
	 * description, and whether the gesture is enabled or not.
	 * 
	 * @param name		The {@link String} name for this gesture.
	 * @param desc		The {@link String} description for this gesture.
	 * @param enabled	The {@code boolean} value indicating if the gesture is
	 * 					enabled, {@code true} or not {@code false}.
	 */
	public AbstractGesture(String name, String desc, boolean enabled) {
		setName(name);
		setDescription(desc);
		setEnabled(enabled);
	}
	
	
	/**
	 * Returns {@code true} if this instance of the gesture is currently
	 * enabled; otherwise, returns {@code false}.
	 * 
	 * @return	{@code true} if the gesture is enabled; otherwise, {@code false}
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	
	/**
	 * Set this instance of the gesture to be enabled or disabled based on the
	 * parameter specified.
	 * 
	 * @param enabled	whether this gesture is enabled or not.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	
	/**
	 * <p>Primary means for this implementation of the gesture detection. The 
	 * matrix parameter is a {@link org.opencv.core.Mat} which should not be
	 * modified! If there are required modification to the image data, make a
	 * copy and use the copy to manipulate the data. Otherwise, any changes to
	 * the original image data will be reflected in the publishing of the image
	 * from within the {@link GestureRecognizer}
	 * <p>If the implementation detects the appropriate object(s), use the
	 * {@link #fireGestureDetected(int, int, int)} method to notify the
	 * registered {@link GestureListener}s.
	 * 
	 * @param matrix	The matrix of image data captured from the connected web
	 * 					camera through the OpenCV interface.
	 * 
	 * @see org.opencv.core.Mat
	 * @see project.recognition.event.GestureListener
	 * @see #fireGestureDetected(int, int, int)
	 */
	abstract public void detect(final Mat matrix);
	
	
	/**
	 * Returns the name for this particular instance of the gesture as a
	 * {@link String}.
	 * 
	 * @return	the name for this instance of the gesture.
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Sets the name for this instance of the gesture with the specified name
	 * parameter.
	 *  
	 * @param name	The new name for this instance of the gesture.
	 */
	protected void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * Returns the description for this particular instance of the gesture as a
	 * {@link String}. If no description has been set for the instance, a
	 * zero-length {@link String} is returned.
	 * 
	 * @return
	 */
	public String getDescription() {
		return desc;
	}
	
	
	/**
	 * Sets the description for this instance of the gesture with the specified
	 * description parameter. If the parameter is {@code null}, the description
	 * will be set to a zero-length {@link String}.
	 * 
	 * @param description	The detailed description for this instance of the
	 * 						gesture.
	 */
	protected void setDescription(String description) {
		this.desc = description == null ? "" : description;
	}
	
	
	/**
	 * Registers the specified {@link GestureListener} to this instance of the
	 * gesture for notification when the gesture has been detected from the
	 * {@link #detect(Mat)} method.
	 * 
	 * @param gl	The new {@link GestureListener} to register.
	 * 
	 * @see project.recognition.event.GestureListener
	 * @see javax.swing.event.EventListenerList#add(Class, java.util.EventListener)
	 */
	public final void addGestureListener(GestureListener gl) {
		listenerList.add(GestureListener.class, gl);
	}
	
	
	/**
	 * Removes the specified {@link GestureListener} from the registered 
	 * listener for this instance of the gesture. 
	 * 
	 * @param gl
	 * 
	 * @see project.recognition.event.GestureListener
	 * @see javax.swing.event.EventListenerList#remove(Class, java.util.EventListener)
	 */
	public final void removeGestureListener(GestureListener gl) {
		listenerList.remove(GestureListener.class, gl);
	}
	
	
	/**
	 * This creates a new {@link GestureEvent} based on the specified parameters
	 * and notifies all of the registered {@link GestureListener}s by calling
	 * {@link #notifyGestureListeners(GestureEvent)}.
	 * 
	 * @param id	The unique identifier for the detection type.
	 * @param x 	The center X-coordinate for the focus gesture detected
	 * @param y 	The center Y-coordinate for the focus gesture detected
	 * 
	 * @see project.recognition.event.GestureEvent
	 */
	protected synchronized final void fireGestureDetected(int id, int x, int y)
	{
		GestureEvent ge = new GestureEvent(this, id,
				System.currentTimeMillis(), x, y);
		
		notifyGestureListeners(ge);
	}
	
	
	/**
	 * Notifies all registered {@link GestureListener}s of this instance of the
	 * gesture with the specified {@link GestureEvent}. This notification is 
	 * handled in a separate thread.
	 * 
	 * @param ge	The new gesture event to notify all appropriate registered
	 * 				listeners with.
	 * 
	 * @see project.recognition.event.GestureEvent
	 */
	private final void notifyGestureListeners(final GestureEvent ge) {
		if (ge == null)
			return;
		
		final GestureListener[] listeners =
				listenerList.getListeners(GestureListener.class);
		
		new Thread( new Runnable() {

			@Override
			public void run() {
				for (GestureListener gl : listeners)
					gl.gestureDetected(ge);
			}
			
		} ).start();
	}
	
	
	/**
	 * Converts the {@link java.awt.Color} to the OpenCV
	 * {@link org.opencv.core.Scalar} value for the color.
	 * 
	 * @param color	the {@link Color} to convert to the {@link Scalar}.
	 * 
	 * @return	the converted {@link Scalar} value for the specified
	 * 			{@link Color}.
	 */
	protected static final Scalar convertColorToScalar(Color color) {
		return color != null ? new Scalar(color.getBlue(), color.getGreen(),
				color.getRed(), color.getAlpha()) : new Scalar(0, 0, 0, 0);
	}
	
	
	/**
	 * Converts the OpenCV {@link org.opencv.core.Scalar} value to the
	 * {@link java.awt.Color} equivalent.
	 * 
	 * @param scalar	the {@link Scalar} value to convert to the {@link Color}
	 * 
	 * @return	the converted {@link Color} for the specified {@link Scalar}
	 * 			value.
	 */
	protected static final Color convertScalarToColor(Scalar scalar) {
		return scalar != null ? new Color((float)scalar.val[0],
				(float)scalar.val[1], (float)scalar.val[2], 
				(float)scalar.val[3]) : new Color(0, 0, 0, 0);
	}
	
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name='" + getName() + "',"
				+ "enabled=" + isEnabled() + "]";
	}

}
