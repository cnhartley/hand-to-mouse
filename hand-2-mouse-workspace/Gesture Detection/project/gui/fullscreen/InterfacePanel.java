/**
 * 
 */
package project.gui.fullscreen;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.opencv.highgui.VideoCapture;

import project.MainEntry;
import project.gui.common.VideoCaptureMirrorPanel;
import project.recognition.GestureRecognizer;

/**
 * 
 * 
 * @author Chris Hartley
 * @author Adin Miller
 */
public class InterfacePanel extends JLayeredPane implements ActionListener {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6977876168957431740L;

	private static final String ACTION_CMD_EXIT = "acmd-exit";
	private static final String ACTION_CMD_CLASSIFIERS = "acmd-classifiers";
	private static final String ACTION_CMD_VIDEO_DEVICE = "acmd-video-device";
	
	private final Stack<Component> visibleComponents = new Stack<Component>();
	
	
	private VideoCapture vcDevice = null;
	private int vcWidth = 0;
	private int vcHeight = 0;
	
	private final JPanel objPanel;
	private final InterfaceMenu vcMenu;
	private VideoCaptureMirrorPanel vcMirror;
	private VideoCaptureDevicePanel vcDevicePanel;
	private ClassifiersPanel vcClassifiersPanel;
	private GestureRecognizer gr = null;
	
	private boolean displayDetailAndHelp = false;
	
	private final JFrame frame;
	
	/**
	 * 
	 */
	public InterfacePanel(JFrame frame) {
		super();
		
		this.frame = frame;
		
		setFocusable(true);
		setBackground(Color.DARK_GRAY);
		setForeground(Color.YELLOW);
		addComponentListener( new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent ce) {
				Component comp = ce.getComponent();
				if (comp != null)
					resized(comp.getWidth(), comp.getHeight());
			}
		} );
		
		objPanel = new JPanel(null) {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 4872914305990980565L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				
				Graphics2D g2d = (Graphics2D)g;
				
				Insets ins = getInsets();
				int x = ins.left;
				int y = ins.top;
				int w = getWidth() - ins.left - ins.right;
				int h = getHeight() - ins.top - ins.bottom;
				
				GradientPaint gp = new GradientPaint(
						x, y, getBackground().brighter(),
						x, h, getBackground().darker() );
				g2d.setPaint(gp);
				g2d.fillRect(x, y, w, h);
				
				g2d.setFont(getFont());
				g2d.setColor(getForeground());
				
				FontMetrics fm = g2d.getFontMetrics();
				x += fm.getHeight();
				y = getHeight() - ins.bottom - fm.getHeight() - fm.getDescent();
				
				if (displayDetailAndHelp) {
					String[] details = { 
							"Hot Keys:",
							"=========",
							"  'm', ECSAPE - Hide/show menu",
							"  'd', F1 - Hide/Show these details",
							" ",
							"Resources:",
							"==========",
							"  VideoCapture Device: " + vcDevice,
							"  VideoCapture Size: " + vcWidth + "x" + vcHeight,
							"  Gesture Classifier: " + gr
					};
					
					for (int i = details.length - 1; i >= 0; i--) {
						g2d.drawString(details[i], x, y);
						y -= fm.getHeight();
					}
				}
			}
			
		};
		objPanel.setBackground(getBackground());
		objPanel.setForeground(getForeground());
		objPanel.setBounds(0, 0, 640, 480);
		add(objPanel, DEFAULT_LAYER);
		
		
		vcMenu = new InterfaceMenu();
		add(vcMenu, POPUP_LAYER);
		
		vcClassifiersPanel = new ClassifiersPanel();
		vcClassifiersPanel.setLocation( 24, 24);
		add(vcClassifiersPanel, POPUP_LAYER);
		
		vcDevicePanel = new VideoCaptureDevicePanel();
		vcDevicePanel.setLocation(24, 24);
		add(vcDevicePanel, POPUP_LAYER);
		
		vcMenu.setVisible(false);
		vcDevicePanel.setVisible(false);
		vcClassifiersPanel.setVisible(false);
	}
	
	
	/**
	 * 
	 */
	public final void initialize() {
		addKeyListener( new KeyCapture() );
		requestFocusInWindow(false);
		
		//HandGestureRobot robot = new HandGestureRobot();
		
		gr = new GestureRecognizer();
		/*
		"../classifiers/fist_classifier.xml",
		"../classifiers/closed_palm_classifier.xml",
		"../classifiers/hand_classifier_1.xml",			// not good!
		"../classifiers/hand_classifier_2.xml",
		"../classifiers/fist.xml",
		"../classifiers/palm.xml"
		*
		gr.registerGesture("Fist 00", GestureEvent.CLOSED_HAND_DETECTED,
				gr.loadClassifier("../classifiers/fist_classifier.xml"),
				Color.RED, robot).setEnabled(true); // Pretty good fist detection!
		
		gr.registerGesture("Closed Palm 00", GestureEvent.CLOSED_HAND_DETECTED,
				gr.loadClassifier("../classifiers/closed_palm_classifier.xml"),
				Color.CYAN, robot).setEnabled(false); // Picks up opened hand (figures)!
		
		gr.registerGesture("Fist 01", GestureEvent.CLOSED_HAND_DETECTED,
				gr.loadClassifier("../classifiers/fist.xml"),
				Color.YELLOW, robot).setEnabled(false);
		
		gr.registerGesture("Hand 01", GestureEvent.OPENED_HAND_DETECTED,
				gr.loadClassifier("../classifiers/hand_classifier_1.xml"),
				Color.GREEN, robot).setEnabled(false); // Finds lots of random objects!

		gr.registerGesture("Hand 02", GestureEvent.OPENED_HAND_DETECTED,
				gr.loadClassifier("../classifiers/hand_classifier_2.xml"),
				Color.GREEN.brighter(), robot).setEnabled(false); // Finds random stuff, not hands!
		
		gr.registerGesture("Palm 01", GestureEvent.OPENED_HAND_DETECTED,
				gr.loadClassifier("../classifiers/palm.xml"),
				Color.ORANGE, robot).setEnabled(true);
		//*/
		vcMirror = new VideoCaptureMirrorPanel(gr.getVideoCaptureImageIcon());
		vcMirror.setLocation(12, 12);
		add(vcMirror, MODAL_LAYER);
		
		validate();
	}
	
	
	/**
	 * 
	 * @param show
	 */
	public final void setDetailAndHelpVisible(boolean show) {
		displayDetailAndHelp = show;
		repaint();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public final boolean isDetailAndHelpVisible() {
		return displayDetailAndHelp;
	}
	
	
	/**
	 * 
	 * @param width
	 * @param height
	 */
	private final void resized(int width, int height) {
		vcMenu.setSize(width, vcMenu.height);
		objPanel.setSize(width, height);
		
		vcMirror.setLocation(12, height - 12 - vcMirror.getHeight());
	}
	
	
	/**
	 * 
	 */
	public final void close() {
		if (gr != null)
			gr.stop();
	}
	

	@Override
	public void actionPerformed(ActionEvent ae) {
		final String cmd = ae.getActionCommand(); 
		if (cmd.equals(ACTION_CMD_EXIT))
			((FullScreenFrame)frame).close();
		else if (cmd.equals(ACTION_CMD_CLASSIFIERS))
			toggleComponentVisiblity(vcClassifiersPanel);
		else if (cmd.equals(ACTION_CMD_VIDEO_DEVICE))
			toggleComponentVisiblity(vcDevicePanel);
		else
			System.err.println("Unknown action command! [" + cmd + "]");
	}
	
	
	/**
	 * 
	 * @param comp
	 */
	private final void toggleComponentVisiblity(Component comp) {
		if (comp == null)
			return;
		
		if (comp.isVisible()) {
			visibleComponents.remove(comp);
			comp.setVisible(false);
		}
		else {
			visibleComponents.push(comp);
			comp.setVisible(true);
		}
	}

	
	/**
	 * 
	 * @author Chris Hartley
	 *
	 */
	private class KeyCapture implements KeyListener {


		@Override
		public void keyReleased(KeyEvent ke) {
			switch (ke.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				if (!visibleComponents.empty()) {
					visibleComponents.pop().setVisible(false);
					break;
				}
			case KeyEvent.VK_M: // Display/hide menu options
				System.out.println("Display/hide menu options");
				toggleComponentVisiblity(vcMenu);
				break;
			
			case KeyEvent.VK_F1:
			case KeyEvent.VK_H: // Display/hide help information
				System.out.println("Display/hide help information");
				setDetailAndHelpVisible(!isDetailAndHelpVisible());
				break;
				
			// Relative location of the video mirror panel.
			case KeyEvent.VK_NUMPAD1:
			case KeyEvent.VK_NUMPAD2:
			case KeyEvent.VK_NUMPAD3:
			case KeyEvent.VK_NUMPAD4:
			case KeyEvent.VK_NUMPAD5:
			case KeyEvent.VK_NUMPAD6:
			case KeyEvent.VK_NUMPAD7:
			case KeyEvent.VK_NUMPAD8:
			case KeyEvent.VK_NUMPAD9:
				
			// Size of the video mirror panel.
			case KeyEvent.VK_PLUS:
			case KeyEvent.VK_MINUS:
				
			default:
				break;
			}
		}

		
		@Override
		public void keyPressed(KeyEvent ke) { }

		
		@Override
		public void keyTyped(KeyEvent ke) { }
		
	}
	
	
	
	/**
	 * 
	 * @author Chris Hartley
	 *
	 */
	protected class InternalInterfacePanel extends JPanel {
		
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -7828099573862474712L;

		
		// Private member data.
		private final Border iiBorder =
				BorderFactory.createMatteBorder(2, 2, 2, 2, Color.LIGHT_GRAY);
		
		
		/**
		 * 
		 * @param layout
		 */
		public InternalInterfacePanel(LayoutManager layout) {
			super(layout);
			
			setOpaque(false);
			setBackground( new Color(0, 0, 0, 128) );
			setForeground(Color.WHITE);
			setBorder(iiBorder);
			
			setBounds(0, 0, 128, 48);
		}
		
		
		@Override
		public void setVisible(boolean aFlag) {
			int x = (InterfacePanel.this.getWidth() - getWidth()) >> 1;
			int y = (InterfacePanel.this.getHeight() - getHeight()) >> 1;
			setLocation(x, y);
			
			super.setVisible(aFlag);
		}
		
		
		@Override
		protected void paintComponent(Graphics g) {
			Insets ins = getInsets();
			final int x = ins.left;
			final int y = ins.top;
			final int width = getWidth() - ins.left - ins.right;
			final int height = getHeight() - ins.top - ins.bottom;
			
			Graphics2D g2d = (Graphics2D)g;
			g2d.setPaint(getBackground());
			g2d.fillRect(x, y, width, height);
			
			super.paintComponents(g);
		}
		
	}
	
	
	
	/**
	 * 
	 * @author Chris Hartley
	 *
	 */
	private class ClassifiersPanel extends InternalInterfacePanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7753570973753225555L;

		// Private member data.
		private Timer refreshClsInfoTimer = null;
		private JLabel clsList;
		
		/**
		 * Constructor
		 */
		public ClassifiersPanel() {
			super( new GridLayout(0, 1) );
			
			JLabel lbl;
			add( lbl = new JLabel("Cascade Classifier(s):") );
			lbl.setFont(getFont().deriveFont(Font.BOLD, 24));
			lbl.setForeground(getForeground());
			
			add( new JLabel("Loaded classifier(s):") );
			add( clsList = new JLabel("[unknown]") );
			
			Dimension dim = getLayout().preferredLayoutSize(this);
			setBounds(0, 0, dim.width + 24, dim.height + 24);
		}
		
		
		@Override
		public void setVisible(boolean aFlag) {
			if (aFlag == isVisible())
				return;
			
			super.setVisible(aFlag);
			
			
			if (aFlag) {
				if (refreshClsInfoTimer != null)
					refreshClsInfoTimer.cancel();
				
				refreshClsInfoTimer = new Timer("refreshVCinfoTimer");
				refreshClsInfoTimer.scheduleAtFixedRate(new TimerTask() {

					@Override
					public void run() {
						updateValues();
					}
					
				}, 1000, 1000);
			}
			else if (refreshClsInfoTimer != null) {
				refreshClsInfoTimer.cancel();
				refreshClsInfoTimer = null;
			}
		}
		
		
		/**
		 * 
		 */
		private final void updateValues() {
			if (!isVisible())
				return;
			
			if (gr != null) {
				try {
					clsList.setText("" + Arrays.toString(gr.getGestureNames()));
				}
				catch (Exception ignore) { }
			}
			else {
				clsList.setText("[unknown]");
			}
		}
		
	}
	
	
	
	/**
	 * 
	 * @author Chris Hartley
	 *
	 */
	private class VideoCaptureDevicePanel extends InternalInterfacePanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -8431960467034816722L;

		// Private member data.
		private Timer refreshVCinfoTimer = null;
		private final JLabel vcDeviceLbl;
		private final JLabel vcSizeLbl;
		private final JLabel vcFPSLbl;
		private final JLabel vcSupportSizesLbl;
		
		
		/**
		 * Constructor
		 */
		public VideoCaptureDevicePanel() {
			super( new GridLayout(0, 2) );
			
			JLabel lbl;
			add( lbl = new JLabel("Video Capture Device Info:") );
			lbl.setFont(getFont().deriveFont(Font.BOLD, 24));
			lbl.setForeground(getForeground());
			add( new JLabel("") );
			
			add( new JLabel("Video Capture Device:") );
			add(vcDeviceLbl = new JLabel("[unknown]") );
			add( new JLabel("Video Capture Size:") );
			add(vcSizeLbl = new JLabel(vcWidth + "x" + vcHeight) );
			add( new JLabel("Video Capture FPS:") );
			add(vcFPSLbl = new JLabel("[unknown]") );
			
			add( new JLabel("Video Capture FPS:") );
			add(vcSupportSizesLbl = new JLabel("[unknown]") );
			
			Dimension dim = getLayout().preferredLayoutSize(this);
			setBounds(0, 0, dim.width + 24, dim.height + 24);
		}
		
		
		@Override
		public void setVisible(boolean aFlag) {
			if (aFlag == isVisible())
				return;
			
			super.setVisible(aFlag);
			
			
			if (aFlag) {
				if (refreshVCinfoTimer != null)
					refreshVCinfoTimer.cancel();
				
				refreshVCinfoTimer = new Timer("refreshVCinfoTimer");
				refreshVCinfoTimer.scheduleAtFixedRate(new TimerTask() {

					@Override
					public void run() {
						updateValues();
					}
					
				}, 1000, 1000);
			}
			else if (refreshVCinfoTimer != null) {
				refreshVCinfoTimer.cancel();
				refreshVCinfoTimer = null;
			}
		}
		
		
		/**
		 * 
		 */
		private final void updateValues() {
			if (!isVisible())
				return;
			
			if (gr != null) {
				try {
					vcDeviceLbl.setText("" + gr.getVideoCaptureDevice());
					vcSizeLbl.setText(gr.getVideoCaptureWidth() + "x" + gr.getVideoCaptureHeight());
					vcFPSLbl.setText("" + gr.getFPS());
					vcSupportSizesLbl.setText("" + gr.getVideoCaptureDevice().getSupportedPreviewSizes());
				}
				catch (Exception ignore) { }
			}
			else {
				vcDeviceLbl.setText("[unknown]");
				vcSizeLbl.setText("0x0");
				vcFPSLbl.setText("[unknown]");
				vcSupportSizesLbl.setText("[unknown]");
			}
		}
		
	}
	
	
	
	/**
	 * 
	 * @author Chris Hartley
	 *
	 */
	private class InterfaceMenu extends JPanel {
		
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -7526669077611391107L;

		
		public final int height = 64;
		
		
		/**
		 * Constructor for a new instance of the main interface menu panel.
		 */
		public InterfaceMenu() {
			super( new FlowLayout(FlowLayout.CENTER) );
			
			setForeground(Color.WHITE);
			setBackground( new Color(0, 0, 0, 128) );
			
			setBounds(0, 0, 640, height);
			setBorder( BorderFactory.createMatteBorder(2, 0, 2, 0, Color.LIGHT_GRAY) );
			
			build();
		}
		
		
		/**
		 * 
		 */
		private final void build() {
			JButton btn;
			try {
				add(btn = new InterfaceMenuButton("images/classifiers.png"));
				btn.setActionCommand(ACTION_CMD_CLASSIFIERS);
				btn.addActionListener(InterfacePanel.this);
				
				add(btn = new InterfaceMenuButton("images/videoDevice.png"));
				btn.setActionCommand(ACTION_CMD_VIDEO_DEVICE);
				btn.addActionListener(InterfacePanel.this);
				
				add(btn = new InterfaceMenuButton("images/exit.png"));
				btn.setActionCommand(ACTION_CMD_EXIT);
				btn.addActionListener(InterfacePanel.this);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		
		@Override
		public void setSize(int width, int height) {
			Dimension dim = getLayout().preferredLayoutSize(this);
			super.setSize(width, dim.height);
		}
		
		
		@Override
		public void setVisible(boolean visible) {
			super.setVisible(visible);
			InterfacePanel.this.repaint();
		}
		
		
		
		/**
		 * 
		 * @author Chris Hartley
		 *
		 */
		private class InterfaceMenuButton extends JButton {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = -1240465364223495311L;

			
			/**
			 * Constructor for a new instance of this interface menu button with the specified text.
			 * 
			 * @param text
			 * @throws Exception 
			 */
			public InterfaceMenuButton(String imgPath) throws Exception {
				super( MainEntry.getImageIconFromPath(imgPath) );
				
				setPressedIcon( MainEntry.getImageIconFromPath(
						imgPath.replace(".png", "-pressed.png")) );
				
				setRolloverIcon( MainEntry.getImageIconFromPath(
						imgPath.replace(".png", "-hover.png")) );
				
				setRolloverEnabled(true);
				setSelected(false);
				
				setBorder(null);
				setBorderPainted(false);
				setForeground(Color.WHITE);
				setBackground( new Color(0, 0, 0, 255) );
				setDoubleBuffered(true);
				setContentAreaFilled(false);
				setOpaque(false);
				setFocusPainted(false);
				setFocusable(false);
				
				setFont( getFont().deriveFont(Font.BOLD, 24) );
			}
			
			
			@Override
			protected void fireStateChanged() {
				super.fireStateChanged();
				InterfacePanel.this.repaint();
			}
		}
	}

}
