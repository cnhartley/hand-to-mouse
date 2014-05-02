/**
 * 
 */
package project.gui.fullscreen;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * 
 * 
 * @author Chris Hartley
 * @author Adin Miller
 */
public class FullScreenFrame extends JFrame implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5143956511613249418L;

	
	// Private member data.
	private final InterfacePanel iPanel;
	
	
	/**
	 * Constructor for a new instance of this full-screen window class.
	 */
	public FullScreenFrame(final String title) {
		super(title);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent ev) {
				close();
			}
		} );
		
		setBackground(Color.BLACK);
		setForeground(Color.WHITE);
		
		setContentPane(iPanel = new InterfacePanel(this));
	}
	
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		GraphicsEnvironment env =
				GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice myDevice = env.getDefaultScreenDevice();
		
		boolean isFullScreenSupported = myDevice.isFullScreenSupported();
		
		setUndecorated(isFullScreenSupported);
		setResizable(!isFullScreenSupported);
		if (isFullScreenSupported) {
			myDevice.setFullScreenWindow(this);
			validate();
			System.out.println("Screen Resolution: " + getSize());
		}
		else {
			Dimension minDim = new Dimension(640, 480);
			setSize(minDim);
			setMinimumSize(minDim);
			setExtendedState(MAXIMIZED_BOTH);
			
			setVisible(true);
		}
		
		iPanel.initialize();
	}
	

	/**
	 * Provides necessary closures and finalizations of this application prior
	 * to exiting.
	 */
	public final void close() {
		iPanel.close();
		
		setVisible(false);
		dispose();
		
		System.exit(NORMAL);
	}

}
