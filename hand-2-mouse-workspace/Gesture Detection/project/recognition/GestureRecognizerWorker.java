/**
 * 
 */
package project.recognition;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import project.util.logger.Log;

/**
 * 
 * @author Chris Hartley
 *
 * @see javax.swing.SwingWorker<A,B>
 */
public class GestureRecognizerWorker extends SwingWorker<Void, Long> {

	
	/**
	 * The publish format for the image buffer.
	 */
	private static final String publishImageFormat = ".jpg";
	
	
	/**
	 * The maximum number of re-connect attempts before error'ing out.
	 */
	private static final int maxReconnectAttempts = 5;
	
	
	/**
	 * The delay in milliseconds between re-connect attempts on the video
	 * device.
	 */
	private static final int delayInMillis = 2000;
	
	
	// Private member fields.
	private final int frameAvg = 8;
	private final ImageIcon imgIcon;
	private final Map<String,AbstractGesture> gestures;
	private final MatOfByte matrixBuffer = new MatOfByte();
	private final AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
	
	// Private member data.
	private double fps = 0d;
	private VideoCapture camera;
	private AffineTransformOp op = null;
	
	
	/**
	 * Constructor for a new instance with the specified video capture device,
	 * image holder to publish the received frames, and the map containing the
	 * registered gestures that are currently loaded.
	 * 
	 * @param camera
	 * @param imgIcon
	 * @param gestures2
	 */
	public GestureRecognizerWorker(VideoCapture camera, ImageIcon imgIcon,
			Map<String, AbstractGesture> gestures)
	{
		super();
		
		this.camera = camera;
		this.imgIcon = imgIcon;
		this.gestures = gestures;
		
		Log.debug("Initializing " + getClass().getSimpleName());
	}
	
	
	/**
	 * Verifies that the camera device is available and retrieve the next frame
	 * from the video camera to store into the specified {@link Mat} buffer.
	 * 
	 * @param buffer	The buffer to store the next frame to.
	 * @return			{@code true} if the device is available and successfully
	 * 					retrieved an image for the next frame; otherwise,
	 * 					returns {@code false}.
	 */
	private final synchronized boolean readNextVideoFrame(Mat buffer) {
		int reconnectAttempt = 1;
		
		if (camera == null) {
			Log.error("No camera device connected.");
			return false;
		}
		
		// To retrieve various properties of the camera device.
		//camera.get(propId);
		if (!camera.isOpened())
			Log.warning("Camera device is NOT opened at this time.");
		
		while (!camera.read(buffer) || buffer.empty()) {
			if (reconnectAttempt > maxReconnectAttempts) {
				Log.error("After " + reconnectAttempt + " attempts to "
						+ "re-connect to the device, it was unable to retireve "
						+ "an image. Please verify the web camera is connected "
						+ "and functioning properly.");
				return false;
			}
			
			Log.warning("Couldn't retrive image from video. "
					+ "Re-attempting in " + (delayInMillis / 1000.0)
					+ " seconds...");
			
			try {
				Thread.sleep(delayInMillis);
			} catch (Exception ignore) { }
			
			reconnectAttempt++;
			if (!camera.isOpened())
				camera.open(0);
		}
		return true;
	}
	
	
	/**
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		Log.debug("Worker " + getClass().getSimpleName() + " running...");
		Mat img = new Mat();
		
		int frameCount = 0;
		long time, startTime = 0l;
		
		try {
			while (!isCancelled() && readNextVideoFrame(img)) {
				if (++frameCount == frameAvg) {
					time = System.currentTimeMillis();
					publish(time - startTime);
					frameCount = 0;
					startTime = time;
				}
				synchronized(gestures) {
					for (AbstractGesture gesture : gestures.values()) {
						if (gesture != null && gesture.isEnabled())
							gesture.detect(img);
					}
				}
				
				publishImage(img);
			}
			
			Log.debug("Worker " + getClass().getSimpleName() + " canceled!");
		}
		catch (Exception ex) {
			Log.error("Exception caught in " + getClass().getSimpleName() + ": " + ex);
		}
		return null;
	}
	
	
	/**
	 * 
	 * @param in
	 */
	private final synchronized void publishImage(Mat img) {
		InputStream in = null;
		
		Highgui.imencode(publishImageFormat, img, matrixBuffer);
		
		try {
			in = new ByteArrayInputStream(matrixBuffer.toArray());
		
			BufferedImage bufferImg = ImageIO.read(in);
			
			if (op == null) {
				tx.translate(-bufferImg.getWidth(null), 0);
				op = new AffineTransformOp(tx, 
						AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			}
			
			imgIcon.setImage(op.filter(bufferImg, null));
		}
		catch(IOException ex) {
			if (!isCancelled())
				cancel(true);
		}
	}
	
	
	@Override
	protected void process(List<Long> times) {
		for (long milli : times) {
			fps = frameAvg * 1000.0 / milli;
		}
	}

	
	@Override
	protected void done() {
		Log.warning(getClass().getSimpleName() + " has completed!");

		try {
			camera.release();
			Log.debug("Video device released!");
		} catch(Exception ignore) { }
	}


	/**
	 * returns the current frames-per-second that this worker is processing at.
	 * 
	 * @return	the current frames-per-second (FPS).
	 */
	public double getFPS() {
		return fps;
	}

}
