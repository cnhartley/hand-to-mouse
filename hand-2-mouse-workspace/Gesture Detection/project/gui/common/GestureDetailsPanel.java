/**
 * 
 */
package project.gui.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.opencv.objdetect.CascadeClassifier;

import custom.javax.swing.layout.TableLayout;
import project.recognition.Gesture;

/**
 * @author Chris Hartley
 *
 */
public class GestureDetailsPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8368029033228355532L;

	
	//private Gesture gesture = null;
	
	
	/**
	 * Constructor
	 * 
	 * @see GestureDetailsPanel(Gesture)
	 */
	public GestureDetailsPanel() {
		this(null);
	}
	
	
	/**
	 * Constructor
	 * 
	 * @param gesture
	 */
	public GestureDetailsPanel(Gesture gesture) {
		super(null);
		
		//this.gesture = gesture;
		
		makePanel();
		setGesture(gesture);
	}
	
	
	/**
	 * 
	 * @param gesture
	 */
	public final void setGesture(Gesture gesture) {
		//this.gesture = gesture;
		if (gesture != null) {
			enabledBox.setSelected(gesture.isEnabled());
			nameField.setText(gesture.getName());
			classField.setText(gesture.getClassifier().toString());
			updateColorButton(gesture.getColor());
			typeCombo.setSelectedIndex(gesture.getId());
		}
		validate();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public final Gesture getGesture() {
		Gesture g = new Gesture(nameField.getText(),
				typeCombo.getSelectedIndex(),
				new CascadeClassifier(classField.getText()),
				colorBtn.getBackground(), null);
		g.setEnabled(enabledBox.isSelected());
		return g;
	}
	
	private static final String GestureColorChooserActionCommand = "gesture-color-chooser";
	private static final String[] types =
		{ "Open hand gesture", "Closed hand gesture" };
	
	private final JComboBox<String> typeCombo = new JComboBox<String>(types);
	private final JCheckBox enabledBox = new JCheckBox("Enabled", false);
	private final JTextField nameField = new JTextField("");
	private final JTextField classField = new JTextField("");
	private final JButton colorBtn = new JButton();
	
	
	private final void makePanel() {
		//BoxLayout box = new BoxLayout(this, BoxLayout.Y_AXIS);
		//setLayout(box);
		setLayout( new TableLayout(5, 2) );
		JLabel lbl;
		
		add( lbl = new JLabel(" Gesture Details: ") );
		add(enabledBox);
		lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
		
		add( lbl = new JLabel("Name: ") );
		add(nameField);
		lbl.setLabelFor(nameField);
		lbl.setHorizontalAlignment(JLabel.RIGHT);
		
		add( lbl = new JLabel("Type: ") );
		add(typeCombo);
		lbl.setLabelFor(typeCombo);
		lbl.setHorizontalAlignment(JLabel.RIGHT);
		
		add( lbl = new JLabel("Color: ") );
		add(colorBtn);
		lbl.setLabelFor(colorBtn);
		lbl.setHorizontalAlignment(JLabel.RIGHT);
		colorBtn.setContentAreaFilled(false);
		colorBtn.setBorder(null);
		colorBtn.setLayout( new BorderLayout() );
		colorBtn.add( lbl = new JLabel("#HEXNUM"), BorderLayout.CENTER );
		lbl.setOpaque(true);
		lbl.setHorizontalAlignment(JLabel.CENTER);
		lbl.setBorder( BorderFactory.createLoweredBevelBorder() );
		colorBtn.setActionCommand(GestureColorChooserActionCommand);
		colorBtn.addActionListener(this);
		
		add( lbl = new JLabel("Classifier: ") );
		add(classField);
		lbl.setLabelFor(classField);
		lbl.setHorizontalAlignment(JLabel.RIGHT);
	}


	@Override
	public void actionPerformed(ActionEvent ae) {
		String cmd = ae.getActionCommand();
		
		if (cmd == null)
			return;
		
		if (cmd.equals(GestureColorChooserActionCommand))
			chooseColorOption();
	}
	
	
	private final void chooseColorOption() {
		Color newColor = JColorChooser.showDialog(this,
                "Choose the Gestures' Border Color", Color.RED);
		
		if (newColor != null)
			updateColorButton(newColor);
	}
	
	private final void updateColorButton(Color color) {
		final int rgbMask = (1 << 24) - 1;
		JLabel lbl = (JLabel)colorBtn.getComponent(0);
		lbl.setBackground(color);
		float[] hsb = Color.RGBtoHSB(color.getRed(), 
				color.getGreen(), color.getBlue(), null);
		lbl.setForeground(hsb[2] > 0.75f ? Color.BLACK : Color.WHITE);
		lbl.setText(String.format("#%06X", color.getRGB() & rgbMask));
	}
	
	
	@Override
	public boolean requestFocusInWindow() {
		return nameField.requestFocusInWindow();
	}
	
}
