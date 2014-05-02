/**
 * 
 */
package project.gui.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import project.recognition.AbstractGesture;
import project.recognition.GestureRecognizer;
import project.recognition.types.HaarClassifierGesture;

/**
 * @author Chris Hartley
 *
 */
public class ClassifierDetailsPanel extends JPanel implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4598685182809719959L;

	
	private final JPanel gesturesPanel;
	private final GestureRecognizer gr;
	
	
	/**
	 * Constructor for a new instance of this classifier details panel.
	 * 
	 * @param gr  the {@link GestureRecognizer} used in conjunction with this
	 *            implementation.
	 */
	public ClassifierDetailsPanel(GestureRecognizer gr) {
		super( new BorderLayout() );
		
		this.gr = gr;
		
		setBorder( BorderFactory.createEmptyBorder(4, 4, 4, 4) );
		
		JPanel pnl = new JPanel( new BorderLayout() );
		JLabel lbl = new JLabel("Registered Classifier(s):");
		lbl.setForeground(getForeground());
		lbl.setFont(getFont().deriveFont(Font.BOLD, 14f));
		pnl.add(lbl, BorderLayout.NORTH);
		
		final String msg = "These are all of the successfully registered "
				+ "gestures for this gesture detection application; where the "
				+ "check boxes indicate which gestures are actively being "
				+ "detected:";
		JTextArea txt = new JTextArea(msg);
		txt.setOpaque(false);
		txt.setEditable(false);
		txt.setLineWrap(true);
		txt.setWrapStyleWord(true);
		txt.setFont(getFont().deriveFont(Font.PLAIN, 12f));
		txt.setFocusable(false);
		txt.setForeground(Color.DARK_GRAY);
		pnl.add(txt, BorderLayout.CENTER);
		add(pnl, BorderLayout.NORTH);
		
		gesturesPanel = new JPanel( new GridLayout(0, 1, 3, 3) );
		gesturesPanel.setOpaque(false);
		gesturesPanel.setBorder( BorderFactory.createEmptyBorder(4, 12, 4, 12) );
		gesturesPanel.setMinimumSize( new Dimension(240, 240) );
		gesturesPanel.setMaximumSize( new Dimension(480, 480) );
		gesturesPanel.setPreferredSize( new Dimension(480, 240) );
		add(gesturesPanel, BorderLayout.CENTER);
		
		pnl = new JPanel(null);
		BoxLayout box = new BoxLayout(pnl, BoxLayout.X_AXIS);
		pnl.setBorder( BorderFactory.createEmptyBorder(4, 4, 4, 4) );
		pnl.setLayout(box);
		pnl.setOpaque(false);
		
		JButton btn;
		pnl.add( btn = new JButton("Register New Gesture...") );
		btn.setEnabled(false);
		pnl.add(Box.createHorizontalGlue());
		pnl.add( btn = new JButton("Close") );
		btn.setEnabled(true);
		btn.addActionListener( new AbstractAction() {

			private static final long serialVersionUID = 6744337165042396414L;

			@Override
			public void actionPerformed(ActionEvent ae) {
				JComponent comp = (JComponent)ae.getSource();
				comp.getTopLevelAncestor().setVisible(false);
			}
			
		});
		add(pnl, BorderLayout.SOUTH);
		
		SwingUtilities.invokeLater(this);
	}
	
	
	@Override
	public void setVisible(boolean aFlag) {
		if (aFlag == isVisible())
			return;
		
		super.setVisible(aFlag);
		
		if (aFlag)
			SwingUtilities.invokeLater(this);
	}
	
	
	@Override
	public void run() {
		final JLabel loading = new JLabel("Loading...");
		final Font plainFont = getFont().deriveFont(Font.PLAIN, 12f);
		final Font monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 11);
		final Font emFont = getFont().deriveFont(Font.ITALIC, 12f);
		
		// Update the list of gesture that are registered to the gesture recognizer...
		gesturesPanel.removeAll();
		gesturesPanel.add(loading);
		gesturesPanel.revalidate();
		
		// For each gesture build the appropriate row...
		String txt = "unknown type";
		JLabel lbl;
		JPanel row;
		JCheckBox ckBox;
		for (final AbstractGesture g : gr.getGestures()) {
			if (g == null)
				continue;
			
			row = new JPanel( new BorderLayout() );
			row.setOpaque(false);
			
			ckBox = new JCheckBox(g.getName(), g.isEnabled());
			ckBox.setFont(plainFont);
			ckBox.addItemListener( new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent ie) {
					g.setEnabled(ie.getStateChange() == ItemEvent.SELECTED);
				}
				
			} );
			row.add(ckBox, BorderLayout.WEST);
			
			if (g instanceof HaarClassifierGesture) {
				HaarClassifierGesture hcg = (HaarClassifierGesture)g;
				
				/*switch (hcg.getId()) {
				case GestureEvent.OPENED_HAND_DETECTED:
					txt = "Opened Hand";
					break;
				case GestureEvent.CLOSED_HAND_DETECTED:
					txt = "Closed Hand";
					break;
				default:
					txt = "unknown";
				}*/
				lbl = new JLabel(" - [" + txt + "]");
				lbl.setFont(emFont);
				lbl.setForeground(getForeground());
				row.add(lbl, BorderLayout.CENTER);
				
				lbl = new JLabel(hcg.getHighLightColor().toString());
				lbl.setFont(monoFont);
				lbl.setForeground(hcg.getHighLightColor());
				row.add(lbl, BorderLayout.EAST);
				
				lbl = new JLabel("(" + hcg.getClassifier() + ")");
				lbl.setHorizontalAlignment(SwingConstants.RIGHT);
				lbl.setFont(monoFont);
				lbl.setForeground(Color.DARK_GRAY);
				row.add(lbl, BorderLayout.SOUTH);
			}
			gesturesPanel.add(row, 0);
		}
		
		gesturesPanel.remove(loading);
		gesturesPanel.revalidate();
		repaint();
	}
	
	
}
