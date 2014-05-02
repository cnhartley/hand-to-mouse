/**
 * 
 */
package project.util.logger;

import java.util.EventListener;

/**
 * @author Chris Hartley
 *
 */
public interface LogListener extends EventListener {

	
	/**
	 * 
	 * @param li
	 */
	public abstract void logChanged(LogItem li);
	
}
