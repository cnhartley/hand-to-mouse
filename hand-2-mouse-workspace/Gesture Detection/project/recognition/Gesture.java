/**
 * 
 */
package project.recognition;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;

import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.objdetect.CascadeClassifier;

import project.recognition.event.GestureEvent;
import project.recognition.event.GestureListener;

/**
 * 
 * 
 * @author Chris Hartley
 * @author Adin Miller
 */
public class Gesture implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7146004174528609744L;

	
	// Private member data.
	private String name;
	private int id;
	private CascadeClassifier cc;
	private Color color;
	private Scalar sColor;
	private GestureListener gl;
	private boolean enabled = true;
	
	
	/**
	 * Constructor for a new instance of this Gesture object with the specified
	 * name, cascade classifier, color, and gesture listener.
	 * 
	 * @param name
	 * @param id    GestureEvent.OPENED_HAND_DETECTED or GestureEvent.CLOSED_HAND_DETECTED
	 * @param cc
	 * @param color
	 * @param gl
	 */
	public Gesture(String name, int id, CascadeClassifier cc, Color color,
			GestureListener gl) throws NullPointerException
	{
		if (name == null || cc == null || color == null || gl == null)
			throw new NullPointerException("Gesture(" + name + "," + id + ","
					+ cc + "," + color + "," + gl + ")");
		
		this.name = name;
		this.id = id;
		this.cc = cc;
		this.color = color;
		this.gl = gl;
		
		// Scalar as a color vector => (blue, green, red, alpha)
		this.sColor = new Scalar(color.getBlue(), color.getGreen(),
				color.getRed(), color.getAlpha());
	}
	
	
	public CascadeClassifier getClassifier() {
		return cc;
	}
	
	public int getId() {
		return id;
	}
	
	public Color getColor() {
		return color;
	}
	
	public Scalar getScalarColor() {
		return sColor;
	}
	
	public GestureListener getGestureListener() {
		return gl;
	}
	
	public void notifyGestureListeners(final Rect[] shapes) {
		if (shapes.length == 0)
			return;
		
		new Thread( new Runnable() {

			@Override
			public void run() {
				Rectangle[] rects = new Rectangle[shapes.length];
				Point centerPt = new Point(-1, -1);
				int largest = -1;
				int area;
				for (int i = 0; i < shapes.length; i++) {
					rects[i] =
						new Rectangle((int)shapes[i].x, (int)shapes[i].y,
								shapes[i].width, shapes[i].height);
					area = rects[i].width * rects[i].height;
					if (area > largest) {
						largest = area;
						centerPt.x = (int)rects[i].getCenterX();
						centerPt.y = (int)rects[i].getCenterY();
					}
				}
				
				GestureEvent ge = new GestureEvent(Gesture.this, getId(),
						System.currentTimeMillis(), centerPt.x, centerPt.y);
				gl.gestureDetected(ge);
			}
			
		}).start();
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ "name=" + getName() + ",id=" + getId() + ","
				+ "color=" + getColor() + ","
				+ "enabled=" + isEnabled() + ","
				+ "classifier='" + getClassifier() + "',"
				+ "listener='" + getGestureListener() + "']";
	}
	
}
