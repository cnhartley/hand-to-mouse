/**
 * 
 */
package project.recognition;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import org.opencv.highgui.VideoCapture;
import project.recognition.GestureRecognizerWorker;
import project.util.logger.Log;

/**
 * 
 * @author Chris Hartley
 *
 */
public class GestureRecognizer {

	// Private member data.
	private ImageIcon vcImage = null;
	private VideoCapture vcDevice = null;
	private GestureRecognizerWorker grProcessor = null;
	private final Map<String,AbstractGesture> gestures =
			Collections.synchronizedMap(new HashMap<String,AbstractGesture>());
	

	/**
	 * Constructor for a new instance of a gesture recognizer with the specified
	 * video capture device.
	 * 
	 * @param camera
	 */
	public GestureRecognizer() {
		super();

		vcImage = new ImageIcon() {
			
			private static final long serialVersionUID = 1529931784440692166L;

			@Override
			public void setImage(Image image) {
				super.setImage(image);
				
				ImageObserver iob = getImageObserver();
				if (iob instanceof Component)
					((Component)iob).repaint();
			}
			
		};
		start();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public final ImageIcon getVideoCaptureImageIcon() {
		return vcImage;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public final VideoCapture getVideoCaptureDevice() {
		return vcDevice;
	}
	
	
	/**
	 * 
	 * @param gesture
	 * 
	 * @return 
	 */
	public final AbstractGesture registerGesture(AbstractGesture gesture) {
		if (gesture == null || gestures.containsKey(gesture.getName()))
			return null;
		
		gestures.put(gesture.getName(), gesture);
		return gesture;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public double getFPS() {
		return grProcessor != null ? grProcessor.getFPS() : 0d;
	}
	
	
	/**
	 * 
	 */
	public void start() {
		stop();
		Log.debug("Starting the GestureRecognizer...");
		if (vcDevice == null) {
			vcDevice = new VideoCapture(0);
			if (!vcDevice.isOpened())
				vcDevice.open(0);
		}
		
		if(!vcDevice.isOpened()) {
			Log.error("Default device not opened for video capture!");
			return;
		}
		
		grProcessor = new GestureRecognizerWorker(vcDevice, vcImage, gestures);
		grProcessor.execute();
	}
	
	
	/**
	 * 
	 */
	public void stop() {
		if (grProcessor != null) {
			grProcessor.cancel(true);
		}
		if (vcDevice != null) {
			vcDevice.release();
		}
	}
	
	
	@Override
	public String toString() {
		return getClass().getSimpleName()
				+ "[device=" + vcDevice + ", gesture(s)='"
				+ Arrays.toString(getGestureNames())
				+ "']";
	}
	
	/**
	 * 
	 * @return
	 */
	public String[] getGestureNames() {
		String[] tmp = new String[1];
		return gestures.keySet().toArray(tmp);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public final AbstractGesture[] getGestures() {
		AbstractGesture[] tmp = new AbstractGesture[1];
		return gestures.values().toArray(tmp);
	}


	/**
	 * 
	 * @return
	 */
	public final int getVideoCaptureWidth() {
		return vcImage != null ? vcImage.getIconWidth() : 0;
	}


	/**
	 * 
	 * @return
	 */
	public final int getVideoCaptureHeight() {
		return vcImage != null ? vcImage.getIconHeight() : 0;
	}

}
