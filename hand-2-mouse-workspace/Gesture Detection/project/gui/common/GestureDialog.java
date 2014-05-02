/**
 * 
 */
package project.gui.common;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import project.recognition.Gesture;

/**
 * @author Chris Hartley
 *
 */
public class GestureDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7449446884266557725L;


	public static final String SAVE_ACTION_COMMAND = "gesture-dialog-save-action";
	public static final String CANCEL_ACTION_COMMAND = "gesture-dialog-cancel-action";


	private static final String newGestureTitle = "Create a New Gesture";
	private static final String editGestureTitle = "Edit Gesture";
	
	
	private final GestureDetailsPanel gdPanel = new GestureDetailsPanel();
	private Gesture oldGesture = null;
	
	
	public GestureDialog(Dialog owner) {
		super(owner, newGestureTitle, true);
		makeContent();
	}
	public GestureDialog(Frame owner) {
		super(owner, newGestureTitle, true);
		makeContent();
	}
	public GestureDialog(Window owner) {
		super(owner, newGestureTitle, ModalityType.APPLICATION_MODAL);
		makeContent();
	}
	public GestureDialog(Dialog owner, Gesture gesture) {
		this(owner);
		setGesture(gesture);
	}
	public GestureDialog(Frame owner, Gesture gesture) {
		this(owner);
		setGesture(gesture);
	}
	public GestureDialog(Window owner, Gesture gesture) {
		this(owner);
		setGesture(gesture);
	}
	
	
	public final void setGesture(Gesture gesture) {
		oldGesture = gesture;
		gdPanel.setGesture( new Gesture(gesture.getName(), gesture.getId(),
				gesture.getClassifier(), gesture.getColor(),
				gesture.getGestureListener() ) );
		setTitle(editGestureTitle);
	}
	
	public final Gesture getGesture() {
		return gdPanel.getGesture();
	}
	
	
	private final void makeContent() {
		JButton btn;
		JPanel pnl = new JPanel( new BorderLayout() );
		gdPanel.setBorder( BorderFactory.createEmptyBorder(4, 12, 4, 12) );
		pnl.add(gdPanel, BorderLayout.CENTER);
		
		JPanel btnPnl = new JPanel(null);
		BoxLayout box = new BoxLayout(btnPnl, BoxLayout.X_AXIS);
		btnPnl.setLayout(box);
		btnPnl.setBorder( BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0,
						btnPnl.getBackground().darker()), 
				BorderFactory.createEmptyBorder(4, 12, 4, 12) ) );
		btnPnl.add(Box.createHorizontalGlue());
		btnPnl.add( btn = new JButton("Save") );
		btn.setActionCommand(SAVE_ACTION_COMMAND);
		btn.addActionListener(this);
		btnPnl.add( btn = new JButton("Cancel") );
		btn.setActionCommand(CANCEL_ACTION_COMMAND);
		btn.addActionListener(this);
		pnl.add(btnPnl, BorderLayout.SOUTH);
		
		setContentPane(pnl);
		pack();
		
		addComponentListener( new ComponentAdapter() {
            
			@Override
			public void componentShown(ComponentEvent ce) {
                gdPanel.requestFocusInWindow();
            }
			
        } );
		addWindowListener( new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent we) {
				firePropertyChange(CANCEL_ACTION_COMMAND, oldGesture, null);
				setVisible(false);
			}
			
		} );
	}
	
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		String cmd = ae.getActionCommand();
		
		if (cmd != null) {
			firePropertyChange(cmd, oldGesture, getGesture());
			setVisible(false);
		}
	}
	
}
