/**
 * 
 */
package project.gui.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import project.util.logger.Log;
import project.util.logger.LogItem;
import project.util.logger.LogItemType;
import project.util.logger.LogListener;


/**
 * 
 * @author Chris Hartley
 *
 */
public class LogPanel extends JPanel implements LogListener {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -683208083097054461L;
	
	private final HashMap<LogItemType,SimpleAttributeSet> attrs =
			new HashMap<LogItemType,SimpleAttributeSet>(8);
	
	private StyledDocument logger = null;
	private JTextPane logTextPane = null;
	
	
	/**
	 * Constructor for a new instance of this log panel.
	 */
	public LogPanel() {
		super( new BorderLayout() );
		
		buildPanel();
		
		// register this as a listener to the Log.
		Log.addLogListener(this);
	}
	
	
	/**
	 * 
	 */
	private final void buildPanel() {
		setMinimumSize( new Dimension(250, 400) );
		setMaximumSize( new Dimension(0, 0) );
		setPreferredSize( new Dimension(250, 400) );
		
		logTextPane = new JTextPane();
		logTextPane.setBackground(Color.BLACK);
		logTextPane.setForeground(Color.WHITE);
		logTextPane.setMargin( new Insets(5, 5, 5, 5) );
		logTextPane.setEditable(false);
		add(new JScrollPane(logTextPane), BorderLayout.CENTER);
		
		configureStyles(logTextPane.getStyledDocument());
	}


	/**
	 * 
	 * @param doc
	 */
	private void configureStyles(StyledDocument doc) {
		logger = doc;
		
		SimpleAttributeSet baseAttr = new SimpleAttributeSet();
        StyleConstants.setFontFamily(baseAttr, "Monospaced");
        StyleConstants.setFontSize(baseAttr, 12);
        attrs.put(LogItemType.NORMAL, baseAttr);
        
        SimpleAttributeSet attr = new SimpleAttributeSet(baseAttr);
        StyleConstants.setItalic(attr, true);
        StyleConstants.setForeground(attr, Color.orange.darker());
        attrs.put(LogItemType.WARNING, attr);
        
        attr = new SimpleAttributeSet(baseAttr);
        StyleConstants.setForeground(attr, Color.cyan.darker());
        attrs.put(LogItemType.DEBUG, attr);
        
        attr = new SimpleAttributeSet(baseAttr);
        StyleConstants.setBold(attr, true);
        StyleConstants.setForeground(attr, Color.red);
        attrs.put(LogItemType.ERROR, attr);
	}


	@Override
	public void logChanged(LogItem li) {
		try {
			logger.insertString(logger.getLength(), li + "\n", attrs.get(li.getType()));
			logTextPane.setCaretPosition(logger.getLength());
		} catch (BadLocationException ignore) { }
		
		repaint();
	}
	
}
