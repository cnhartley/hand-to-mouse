/**
 * 
 */
package project.recognition.event;

import java.util.EventListener;

/**
 * Provides the required method for object that implements some form of a
 * gesture detection listener.
 * 
 * @author Chris Hartley
 * @author Adin Miller
 */
public interface GestureListener extends EventListener {
	
	
	/**
	 * Invoked when a gesture has been detected and specified in the
	 * {@link GestureEvent} parameter, {@code gesture}.
	 * 
	 * @param gesture	The {@link GestureEvent} of the detected gesture.
	 * 
	 * @see GestureEvent
	 */
	public void gestureDetected(GestureEvent gesture);

	
}
