/**
 * 
 */
package project.util.logger;

import javax.swing.event.ChangeEvent;

/**
 * @author Chris Hartley
 *
 */
public class LogItem extends ChangeEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 770935831547440904L;

	// Private member data.
    private final String message;
    private final long time;
    private final LogItemType type;
    
    
    /**
     * Constructor LogItem
     *
     * @param src
     * @param type
     * @param msg
     */
    public LogItem(Object src, LogItemType type, String msg) {
        super(src);
        this.message = msg;
        this.type = type;
        this.time = System.currentTimeMillis();
    }
    
    
    /**
     * Constructor LogItem
     *
     * @param src
     * @param msg
     * @param type
     * @param time
     */
    public LogItem(Object src, String msg, LogItemType type, long time) {
        super(src);
        this.message = msg;
        this.type = type;
        this.time = time;
    }
    
    
    /**
     * 
     * @return
     */
    public final String getMessage() {
        return message != null ? message : "";
    }
    
    
    /**
     * 
     * @return
     */
    public final LogItemType getType() {
        return type;
    }
    
    
    /**
     * 
     * @return
     */
    public final long getTimeStamp() {
        return time;
    }
    
    
    public String toString() {
        return Log.getFormattedTimeStamp( getTimeStamp() ) + " "
             + getType() + " " + getMessage();
    }

}
