/**
 * 
 */
package project.gui.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import project.MainEntry;

/**
 * 
 * @author Chris Hartley
 * @author Adin Miller
 */
public class VideoCaptureMirrorPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5011685128270293027L;

	
	// Private member data.
	private Object updateLock = new Object();
	private BufferedImage vcOffline = null;
	private ImageIcon vcVideoImage = null;
	
	
	/**
	 * Constructor for a new instance of this video mirror panel.
	 */
	public VideoCaptureMirrorPanel(ImageIcon imgIcon) {
		super(null);
		this.vcVideoImage = imgIcon;
		vcVideoImage.setImageObserver(this);
		
		setBackground(Color.black);
		setMinimumSize( new Dimension(160, 120) );
		setMaximumSize( new Dimension(640, 480) );
		setPreferredSize( new Dimension(320, 240) );
		setBounds(0, 0, 320, 240);
		setBorder( BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2),
				BorderFactory.createLineBorder(Color.BLACK, 1) ));
		
		try {
			vcOffline = MainEntry.getImageFromPath("gui/images/offline.png");
		}
		catch (IllegalArgumentException | IOException ex) {
			ex.printStackTrace();
		}
	}
	
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		final Insets ins = getInsets();
		final int x = ins.left;
		final int y = ins.top;
		final int w = getWidth() - x - ins.right;
		final int h = getHeight() - y - ins.bottom;
		
		synchronized (updateLock) {
			if (vcVideoImage != null && vcVideoImage.getImage() != null) {
				g2d.drawImage(vcVideoImage.getImage(), x, y, w, h, this);
			}
			else {
				g2d.drawImage(vcOffline, x, y, w, h, this);
			}
		}
	}
	
}
