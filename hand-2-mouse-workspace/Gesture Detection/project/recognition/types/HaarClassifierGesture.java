/**
 * 
 */
package project.recognition.types;

import java.awt.Color;
import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import project.recognition.AbstractGesture;
import project.util.logger.Log;

/**
 * Provides an implementation of the {@link AbstractGesture} using Haar-like 
 * cascading classifiers for the detection method.
 * 
 * @author Chris Hartley
 * @author Adin Miller
 * 
 * @see project.recognition.AbstractGesture
 */
public class HaarClassifierGesture extends AbstractGesture {


	/**
	 * Serial version user id
	 */
	private static final long serialVersionUID = 6269084053345285341L;

	
	// Private member data.
	private CascadeClassifier cc = null;
	private Scalar sColor;
	private Color color;
	
	
	/**
	 * Constructor for a new instance with the specified name. This constructs
	 * a gesture that is by default enabled.
	 * 
	 * @param name	The name for this new instance of the Haar-like classifier.
	 */
	public HaarClassifierGesture(String name) {
		super(name);
	}
	
	
	/**
	 * Constructor for a new instance with the specified name and default
	 * enabled property.
	 * 
	 * @param name		The name for this new instance of the Haar-like 
	 * 					cascading classifier.
	 * @param enabled	Whether or not this particular gesture is enabled upon
	 * 					creation.
	 */
	public HaarClassifierGesture(String name, boolean enabled) {
		super(name, enabled);
	}

	
	/**
	 * @param name
	 * @param desc
	 * @param enabled
	 */
	public HaarClassifierGesture(String name, String desc, boolean enabled) {
		super(name, desc, enabled);
	}

	
	/**
	 * @param name
	 * @param desc
	 */
	public HaarClassifierGesture(String name, String desc) {
		super(name, desc);
	}

	
	/* (non-Javadoc)
	 * @see project.recognition.AbstractGesture#detect(org.opencv.core.Mat)
	 */
	@Override
	public void detect(Mat matrix) {
		if (cc == null)
			return;
		
		Mat grayImg = new Mat();
		MatOfRect objects = new MatOfRect();
		Point pt1 = new Point(0, 0);
		Point pt2 = new Point(0, 0);
		Point center = new Point(0, 0);
		Scalar color = null;//TOO_SMALL_OF_AREA_BOX_COLOR;
		
		Imgproc.cvtColor(matrix, grayImg, Imgproc.COLOR_RGB2GRAY);
		cc.detectMultiScale(grayImg, objects);
		
		Rect[] shapes = objects.toArray();
		for (int i = 0; i < shapes.length; i++) {
			pt1 = shapes[i].tl();
			pt2 = shapes[i].br();
			
			color = getHighLightScalar();
			Core.rectangle(matrix, pt1, pt2, color, 2);
			
			center.x = pt1.x + (pt2.x - pt1.x) / 2.0;
			center.y = pt1.y + (pt2.y - pt1.y) / 2.0; 
		
			Core.circle(matrix, center, 5, convertColorToScalar(Color.YELLOW));
			fireGestureDetected(0, (int)center.x, (int)center.y);
		}
		// TODO: this.notifyGestureListeners(shapes);
	}
	
	
	public void setHighLightColor(Color color) {
		this.color = color;
		this.sColor = convertColorToScalar(color);
	}
	
	public Color getHighLightColor() {
		return color;
	}
	
	public final Scalar getHighLightScalar() {
		return sColor;
	}
	
	
	/**
	 * 
	 * @param cc	The Haar-like cascading classifier to use.
	 */
	public void setClassifier(CascadeClassifier cc) {
		this.cc = cc;
	}
	
	
	/**
	 * 
	 * @param ccFileName	The Haar-like cascading classifier XML file to use.
	 */
	public void setClassifier(String ccFileName) {
		setClassifier( loadClassifier(ccFileName) );
	}
	
	
	/**
	 * 
	 * @return
	 */
	public CascadeClassifier getClassifier() {
		return cc;
	}
	
	
	/**
	 * 
	 * @param cPath the relative or absolute path to the cascade classifier; the
	 *              file should be a XML file.
	 * @return
	 */
	public static final CascadeClassifier loadClassifier(String cPath) {
		try {
			File f = new File( HaarClassifierGesture.class.getResource(cPath).toURI() );
			if (f != null && f.canRead())
				return new CascadeClassifier(f.getAbsolutePath());
			else
				Log.error("Unable to read the resource: " + cPath);
		}
		catch (NullPointerException npe) {
			Log.error("Could not locate resource: [classifier] " + cPath);
			Log.error(" -- Current resource path: " + HaarClassifierGesture.class.getName());
		}
		catch (Exception ex) {
			Log.error("Unable to load resource: [classifier] " + cPath);
		}
		return null;
	}

}
