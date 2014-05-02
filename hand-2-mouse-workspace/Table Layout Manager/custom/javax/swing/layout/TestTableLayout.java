/**
 * 
 */
package custom.javax.swing.layout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * @author MRR54
 *
 */
public class TestTableLayout extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3297763186547091381L;
	
	
	/**
	 * Constructor
	 */
	public TestTableLayout() {
		super("Test Table Layout Manager");
		
		setContentPane(build());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(800, 600);
		
		setVisible(true);
	}
	
	
	private Container build() {
		final int rows = 3;
		final int cols = 5;
		JPanel main = new JPanel( new BorderLayout() );
		main.add( new JLabel("CENTER"), BorderLayout.CENTER);
		
		JPanel pnl = new JPanel( new TableLayout(rows, cols) );
		pnl.setBorder( BorderFactory.createLineBorder(Color.RED, 3) );
		Dimension[] colDim = new Dimension[] {
				new Dimension(36, 24),
				new Dimension(128, 14),
				new Dimension(64, 12),
				new Dimension(256, 16),
				new Dimension(64, 18)
		};
		
		for (int i = 0; i < rows * cols; i++) {
			JLabel lbl = new JLabel("" + i, SwingConstants.CENTER);
			lbl.setMinimumSize(colDim[i % cols]);
			lbl.setMaximumSize(colDim[i % cols]);
			lbl.setPreferredSize(colDim[i % cols]);
			lbl.setBorder( BorderFactory.createLineBorder(Color.BLUE) );
			if (i==0)
				pnl.add(lbl, TableLayout.ColumnSpan(2));
			pnl.add(lbl);
		}
		main.add(pnl, BorderLayout.SOUTH);
		
		return main;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new TestTableLayout();
	}

}
