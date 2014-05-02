/**
 * Hand Gesture Detection with OpenCV
 * Group Project - CSC 484, Winter Quarter 2014
 * California Polytechnic State University, SLO
 * 
 * Team members:
 *   Chris Hartley (cnhartle@calpoly.edu)
 *   Adin Miller   (amille@calpoly.edu)
 *   Shubham Kahal (observer)
 * 
 * Collaboration with:
 *   Carmen Badea, Intel Corp. (carmen.t.badea@intel.com)
 * 
 * Abstract:
 * ...
 * 
 */
package project;

import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.opencv.core.Core;

import project.gui.fullscreen.FullScreenFrame;
import project.gui.tray.GestureTrayApp;
import project.util.logger.Log;


/**
 * Provide the main entry point for the graphical user interface and supporting
 * gesture detection implementations of this package. This class supplies the
 * {@code main(String[] args)} method to allow command-line parameters and load
 * the full-screen application to demonstrate the hand gestures masked to mouse
 * input.
 * 
 * @author Chris Hartley
 * @author Adin Miller
 */
public class MainEntry {

	
	/**
	 * Provides a static field containing the name of this application to be
	 * used in any implementation environment.
	 */
	public static final String APP_NAME =
			"Gesture Recognization - Grab, Drag, Drop, and Throw";
	
	
	/**
	 * The default configuration file name. This file will always be an XML
	 * document.
	 */
	private static String configFileName = "./config.xml";
	
	
	/**
	 * The configuration object for any computer / user specific settings.
	 * 
	 * @see project.Config
	 */
	private static Config config;
	
	
	private static boolean reconfigure = true;
	private static boolean runAsSystemTrayApp = true;
	
	
	/**
	 * Returns an {@link ImageIcon} for the specified file {@code path}
	 * parameter. The file path should be relative to this current class of
	 * {@link MainEntry}.
	 * 
	 * @param path  the relative path from this class or an absolute path to the
	 *              desired resource to read in as an image.
	 * 
	 * @return  an {@link ImageIcon} containing the specified resource image by
	 *          the {@code path} parameter.
	 * 
	 * @throws  IllegalArgumentException if the image path cannot be resolved.
	 * @throws  IOException if an error occurs during reading of the image.
	 * 
	 * @see #getImageFromPath(String)
	 */
	public final static ImageIcon getImageIconFromPath(String path)
			throws IllegalArgumentException, IOException
	{
		return new ImageIcon( getImageFromPath(path) );
	}
	
	
	/**
	 * Returns a {@link BufferedImage} for the specified file {@code path}
	 * parameter. The file path should be relative to this current class of
	 * {@link MainEntry}.
	 * 
	 * @param path  the relative path from this class or an absolute path to the
	 *              desired resource to read in as an image.
	 * 
	 * @return  a {@link BufferedImage} of the resource at the specified
	 *          {@code path}.
	 * 
	 * @throws  IllegalArgumentException if the image path cannot be resolved.
	 * @throws  IOException if an error occurs during reading of the image.
	 * 
	 * @see java.lang.Class#getResource(String)
	 * @see javax.imageio.ImageIO.read(URL)
	 */
	public final static BufferedImage getImageFromPath(String path)
			throws IllegalArgumentException, IOException
	{
		return ImageIO.read( MainEntry.class.getResource(path) );
	}
	
	
	/**
	 * This method provides the main entry point of the overall application.
	 * Several command-line parameters may be used in order to specify certain
	 * application settings on start-up.
	 * 
	 * @param args	an array of {@link String}s for each whitespace separated
	 * 				command-line word.
	 */
	public static void main(String[] args) {
		// Parse the command-line parameters...
		parseCommandLineParameters(args);
		
		// Set to the system look & feel...
	    setSystemLookAndFeel();
	    
		// If command-line parameters contained reconfigure or no configuration
	    // file was found, create a new configuration for this program.
		if (reconfigure || !config.checkIfConfigFileExists())
			createNewConfiguration();
		
		// Initialize the OpenCV preliminary configurations...
		loadOpenCvNativeLibrary();
		
	    // Initialize and run the application...
	    SwingUtilities.invokeLater( initializeGraphicalUserInterface() );
	}
	
	
	/**
	 * Load the native library for the OpenCV installation. If there are any
	 * errors, this method will attempt to reconfigure the configuration file
	 * and request for updated locations of the require native library files
	 * for OpenCV.
	 * 
	 * @see org.opencv.core.Core#NATIVE_LIBRARY_NAME
	 * @see java.lang.System#load(String)
	 */
	private static final void loadOpenCvNativeLibrary() {
		boolean libraryLoaded = false;
		String msg;
		
		while (!libraryLoaded) {
			msg = "[unknown error]";
		    try {
		    	System.load(config.getOpenCVlibPath());
		    	libraryLoaded = true;
		    }
		    catch (SecurityException se) {
		    	msg = "Security manager doe not allow loading of the native "
		    			+ "library for OpenCV: '"
		    			+ Core.NATIVE_LIBRARY_NAME + "'. Error message:\n"
		    			+ se.getMessage();
		    }
		    catch (UnsatisfiedLinkError usle) {
		    	msg = "Required dynamic library is unsatisfiably linked: '"
		    			+ Core.NATIVE_LIBRARY_NAME + "'. Error message:\n"
		    	        + usle.getMessage();
		    }
		    catch (NullPointerException npe) {
		    	msg = "Required dynamic library for OpenCV is null. "
		    			+ "Error message:\n" + npe.getMessage();
		    }
		    finally {
		    	if (!libraryLoaded) {
			    	msg += "\n\nPlease reconfigure your installation of this "
			    			+ "software.";
			    	System.err.println(msg);
			    	
			    	Object[] opts = { "Reconfigure", "Exit" };
			    	int opt = JOptionPane.showOptionDialog(null, msg, APP_NAME,
			    			JOptionPane.YES_NO_OPTION,
			    			JOptionPane.WARNING_MESSAGE, null, opts, opts[0]);
			    	if (opt == JOptionPane.YES_OPTION)
			    		createNewConfiguration();
			    	else
			    		System.exit(0);
			    }
		    }
		}
	}
	
	
	/**
	 * Initializes the graphical user interface (GUI) based on the command-line
	 * parameters and the configuration file and returns the reference to the 
	 * GUI as a {@link Runnable} interface. The {@code run()} method of the GUI
	 * to load initializes and displays their application implementation.
	 * 
	 * @return	a reference to the {@link Runnable} GUI interface to be
	 * 			executed by the caller.
	 */
	private static final Runnable initializeGraphicalUserInterface() {
		Runnable app = null;
	    if (runAsSystemTrayApp) {
	    	try {
				app = new GestureTrayApp(APP_NAME);
			}
	    	catch (UnsupportedOperationException
	    			| SecurityException
	    			| AWTException e)
	    	{
				e.printStackTrace();
			}
	    }
	    if (app == null)
			app = new FullScreenFrame(APP_NAME);
	    
	    return app;
	}
	
	
	/**
	 * Attempts to set the look and feel of the user interface to the users'
	 * default system look and feel. If any exceptions are caught while 
	 * attempting to set the look and feel nothing happens.
	 */
	private static final void setSystemLookAndFeel() {
		try {
	    	UIManager.setLookAndFeel(
	    			UIManager.getSystemLookAndFeelClassName() );
	    }
	    catch (Exception e) { }
	}
	
	
	/**
	 * Creates a new configuration file through the configuration wizard.
	 * 
	 * @see project.Config(String)
	 */
	private static void createNewConfiguration() {
		config = new Config(configFileName);
		config.run();
	}
	
	
	/**
	 * Parses the command-line arguments specified in the {@link String} array
	 * parameter.
	 * 
	 * @param args
	 */
	private static final void parseCommandLineParameters(String[] args) {
		final ArrayList<String> params = new ArrayList<String>();
		for (String arg : args)
			params.add(arg);
		
		runAsSystemTrayApp |= params.contains("--tray") || params.contains("-t");
		reconfigure |= params.contains("--reconfig") || params.contains("-r");
		
		int ndx = Math.max(	params.indexOf("-p"),
							params.indexOf("--classpath"));
		
		if (ndx > -1 && ndx < params.size()) {
			addPathToClasspath(params.get(ndx + 1));
		}
		
		config = new Config(configFileName);
	}
	
	
	/**
	 * Add the specified path to the system class loader. This method fast 
	 * fails and return if the path is {@code null}.
	 * 
	 * @param path	the {@link String} path to add to the system class loader.
	 */
	private static final void addPathToClasspath(String path) {
		if (path == null)
			return;
		
		Log.debug("adding '" + path + "'...");

		Method method;
		File f = new File(path);
	    Class<URLClassLoader> urlClass = URLClassLoader.class;
		
		try {
			URL url = f.toURI().toURL();
			URLClassLoader urlClassLoader =
					(URLClassLoader)ClassLoader.getSystemClassLoader();
			
			method = urlClass.getDeclaredMethod("addURL", new Class[]{ URL.class });
			method.setAccessible(true);
			method.invoke(urlClassLoader, new Object[]{ url });
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		catch (SecurityException e) {
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		finally {
			Log.debug("Classloader after adding: ["
					+ Arrays.toString(((URLClassLoader)ClassLoader.getSystemClassLoader()).getURLs())
					+ "]");
		}
		
	}

}
