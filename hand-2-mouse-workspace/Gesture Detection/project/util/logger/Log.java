/**
 * 
 */
package project.util.logger;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.event.EventListenerList;

import org.opencv.core.Mat;


/**
 * 
 * @author Chris Hartley
 *
 */
public class Log {

	
	/**
	 * Flag to indicate if this should also echo to the log items to the systems
	 * standard out and standard error.
	 */
	public static boolean ECHO_TO_SYSTEM = true;
	
	
	/**
	 * Flag to write the log items to the log file.
	 */
	public static boolean WRITE_TO_LOG_FILE = true;
    
	
	/**
     * Default log file name and location.
     */
    private static final String _DEFAULT_LOG_FILENAME = "log.txt";
    
    
    /**
     * @see LogItemType#NORMAL
     */
    public static final LogItemType NORMAL = LogItemType.NORMAL;

    
    /**
     * @see LogItemType#WARNING
     */
    public static final LogItemType WARNING = LogItemType.WARNING;

    
    /**
     * @see LogItemType#ERROR
     */
    public static final LogItemType ERROR = LogItemType.ERROR;

    
    /**
     * @see LogItemType#DEBUG
     */
    public static final LogItemType DEBUG = LogItemType.DEBUG;
    
    
    /**
     * The print stream of the log file to write to when the flag
     * {@link #WRITE_TO_LOG_FILE} is set to {@code true}.
     * 
     * @see java.io.PrintStream
     */
    public static PrintStream out = null;
	
    
    // Array list of all log items collected.
    private static final ArrayList<LogItem> logItems =
    		new ArrayList<LogItem>(50);
    
    // The security manager for determining the calling methods.
    private static final MySecurityManager mySecurityManager =
            new MySecurityManager();
    
    // Event listener list for this log items.
    private static final EventListenerList listenerList =
    		new EventListenerList();

    
	/**
	 * 
	 * @param type
	 * @param message
	 */
	protected static final void write(LogItemType type, String message) {
		Object caller = mySecurityManager.getCaller();
		LogItem li = new LogItem(caller, type, message);
		
		if (logItems.add(li))
			fireLogChange(li);
		
		if (ECHO_TO_SYSTEM) {
			if (type == ERROR) System.err.println(li);
			else               System.out.println(li);
		}
		
		if (WRITE_TO_LOG_FILE) {
			if (out == null) connectLogFile(null);
			out.println(li);
		}
	}
	
	
	/**
	 * 
	 * @param logFileName
	 */
	private static final void connectLogFile(String logFileName) {
		if (logFileName == null)
			logFileName = _DEFAULT_LOG_FILENAME;
		
		try {
			if (out == null)
				out = new PrintStream( new BufferedOutputStream(
						new FileOutputStream(logFileName, true) ) );
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			out = System.out;
		}
	}
	
	
	/**
	 * 
	 * @param message
	 */
	public static final void print(String message) {
		write(NORMAL, message);
	}
	
	
	/**
	 * 
	 * @param debugMessage
	 */
	public static final void debug(String debugMessage) {
		write(DEBUG, debugMessage);
	}
	
	
	/**
	 * 
	 * @param warningMessage
	 */
	public static final void warning(String warningMessage) {
		write(WARNING, warningMessage);
	}
	
	
	/**
	 * 
	 * @param errorMessage
	 */
	public static final void error(String errorMessage) {
		write(ERROR, errorMessage);
	}
	
	
	/**
	 * Returns the number of items contained in this log.
	 * 
	 * @return the size of the log item collection.
	 */
	public static final int size() {
		return logItems.size();
	}
	
	
	public static final LogItem getItem(int ndx) {
		if (ndx < 0 || ndx >= size())
			return null;
		
		return logItems.get(ndx);
	}
	

    /**
     * 
     * @param timeInMillis
     * @return
     */
    public final static String getFormattedTimeStamp(long timeInMillis) {
        return getFormattedTimeStamp(timeInMillis, "yyyy.MM.dd HH:mm:ss z");
    }
    
    
    /**
     * 
     * @param timeInMillis
     * @param tsFormat
     * @return
     */
    public final static String getFormattedTimeStamp(long timeInMillis, String tsFormat) {
        return getFormattedTimeStamp(timeInMillis, new SimpleDateFormat(tsFormat));
    }
    
    
    /**
     * 
     * @param timeInMillis
     * @param tsFormat
     * @return
     */
    public final static String getFormattedTimeStamp(long timeInMillis, DateFormat tsFormat) {
        return tsFormat.format( new Date(timeInMillis) );
    }
	
	
	/**
	 * Registers the specified {@link LogListener} to this instance of the
	 * gesture for notification when the gesture has been detected from the
	 * {@link #detect(Mat)} method.
	 * 
	 * @param ll	The new {@link LogListener} to register.
	 * 
	 * @see project.recognition.event.LogListener
	 * @see javax.swing.event.EventListenerList#add(Class, java.util.EventListener)
	 */
	public final static void addLogListener(LogListener ll) {
		listenerList.add(LogListener.class, ll);
	}
	
	
	/**
	 * Removes the specified {@link LogListener} from the registered 
	 * listener for this instance of the gesture. 
	 * 
	 * @param ll
	 * 
	 * @see project.util.logger.LogListener
	 * @see javax.swing.event.EventListenerList#remove(Class, java.util.EventListener)
	 */
	public final static void removeLogListener(LogListener ll) {
		listenerList.remove(LogListener.class, ll);
	}
	
	
	/**
	 * This creates a new {@link LogItem} based on the specified parameters
	 * and notifies all of the registered {@link LogListener}s by calling
	 * {@link #notifyLogListeners(LogItem)}.
	 * 
	 * @param item	The log item that was added.
	 * 
	 * @see project.util.logger.LogItem
	 */
	protected synchronized final static void fireLogChange(LogItem li)
	{
		notifyLogListeners(li);
	}
	
	
	/**
	 * Notifies all registered {@link LogListener}s of this instance of the log
	 * with the specified {@link LogItem}. This notification is handled in a
	 * separate thread.
	 * 
	 * @param li	The new log item event to notify all appropriate registered
	 * 				listeners with.
	 * 
	 * @see project.util.logger.LogItem
	 */
	private final static void notifyLogListeners(final LogItem li) {
		if (li == null)
			return;
		
		final LogListener[] listeners =
				listenerList.getListeners(LogListener.class);
		
		new Thread( new Runnable() {

			@Override
			public void run() {
				for (LogListener ll : listeners)
					ll.logChanged(li);
			}
			
		} ).start();
	}
	
	
    /**
     * Custom security manager for accessing the class context array to
     * determine the calling method / class for log items.
     * 
     * @author Chris N. Hartley
     * @see java.lang.SecurityManager
     */
	private static class MySecurityManager extends SecurityManager {
        
		/**
		 * Returns the class context object at the second position in the set.
		 * 
		 * @return
		 */
		public Object getCaller() {
            return getClassContext()[2];
        }
		
    }

}
