/**
 * 
 */
package wizard.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/**
 * Provides a standard template for a customizable configuration wizard.
 * 
 * @author Christopher N. Hartley
 *
 * @param <CONFIG_OBJ>
 * 
 * @see java.lang.Runnable
 * @see javax.swing.JDialog
 */
public class ConfigWizard<CONFIG_OBJ> extends JDialog implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5759824078932484611L;
	
	/*
	 * Version information for this module.
	 */
	private static final int majorVersion = 1;
	private static final int minorVersion = 2;
	private static final int buildVersion = 10;
	
	/**
	 * The full version {@link Sting} including the major, minor, and build
	 * version codes. This can be used to verify the appropriate version for
	 * compatibility.
	 */
	private static final String version = String.format("%d.%02d.%03d",
			majorVersion, minorVersion, buildVersion);
	
	
	/**
	 * Internal class to manage individual configuration wizard step panels and
	 * allows them to have a title name and specify if they are major steps. 
	 * Major steps are identified as the ones that are listed in the bread
	 * crumbs across the top bar of the wizard; and these are click'able links
	 * to jump back to previously completed steps.
	 * 
	 * @author Christopher N. Hartley
	 *
	 * @see wizard.gui.StepPanel<CONFIG_OBJ>
	 */
	protected final class ConfigWizardStep {
		
		// Protected member data.
		protected String name;
		protected StepPanel<CONFIG_OBJ> comp;
		protected boolean isMajor = true;
		
		
		/**
		 * Constructor for a new instance of this configuration wizard step with
		 * the specified name, step panel, and if it is a major step or not.
		 * 
		 * @param name
		 * @param comp
		 * @param isMajor
		 */
		private ConfigWizardStep(String name, StepPanel<CONFIG_OBJ> comp,
				boolean isMajor)
		{
			this.name = name;
			this.comp = comp;
			this.isMajor = isMajor;
		}
	}
	
	
	private int currentStep = -1;
	private JPanel breadcrumbs;
	private JPanel contentPanel;
	private JButton prevBtn;
	private JButton nextBtn;
	private JButton finishBtn;
	private JButton cancelBtn;
	
	private final ArrayList<ConfigWizardStep> steps =
			new ArrayList<ConfigWizardStep>();
	
	private final CONFIG_OBJ obj;
	
	
	/**
	 * Constructor for a new instance of this configuration wizard.
	 * 
	 * @param title
	 */
	public ConfigWizard(String title, CONFIG_OBJ obj) {
		this.obj = obj;
		setTitle(title);
		setAlwaysOnTop(true);
		setResizable(false);
		setContentPane(makeDialog());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				cancel();
			}
		});
		pack();//setSize(640, 480);
	}
	
	
	/**
	 * 
	 * @param name
	 * @param comp
	 * @param step
	 * @param isMajor
	 */
	private final void addStepImpl(String name, StepPanel<CONFIG_OBJ> comp,
			int step, boolean isMajor)
	{
		if (name == null || name.isEmpty())
			throw new NullPointerException("Step name is undefined");
		
		if (comp == null)
			throw new NullPointerException("Step component undefined");
		
		ConfigWizardStep newStep = new ConfigWizardStep(name, comp, isMajor);
		
		if (step < 0 || step >= steps.size())
			steps.add(newStep);
		else
			steps.add(step, newStep);
	}
	
	
	/**
	 * 
	 * @param name
	 * @param comp
	 */
	public final void addStep(String name, StepPanel<CONFIG_OBJ> comp) {
		addStepImpl(name, comp, -1, true);
	}
	
	
	/**
	 * 
	 * @param name
	 * @param comp
	 * @param step
	 */
	public final void addStep(String name, StepPanel<CONFIG_OBJ> comp,
			int step)
	{
		addStepImpl(name, comp, step, true);
	}
	
	
	/**
	 * 
	 * @param name
	 * @param comp
	 * @param isMajor
	 */
	public final void addStep(String name, StepPanel<CONFIG_OBJ> comp,
			boolean isMajor)
	{
		addStepImpl(name, comp, -1, isMajor);
	}
	
	
	/**
	 * 
	 * @param name
	 * @param comp
	 * @param step
	 * @param isMajor
	 */
	public final void addStep(String name, StepPanel<CONFIG_OBJ> comp,
			int step, boolean isMajor)
	{
		addStepImpl(name, comp, step, isMajor);
	}
	
	
	/**
	 * 
	 * @param step
	 */
	protected final void gotoStep(int step) {
		try {
			ConfigWizardStep cws = steps.get(step);
			setStepPanel(cws);
			currentStep = step;
			updateBreadCrumbs(cws);
			updateButtons();
		}
		catch (IndexOutOfBoundsException ioobe) {
			ioobe.printStackTrace();
		}
		catch (NullPointerException npe) {
			npe.printStackTrace();
		}
	}
	
	
	/**
	 * 
	 * @param step
	 */
	private final void setStepPanel(ConfigWizardStep step) {
		contentPanel.removeAll();
		contentPanel.add(step.comp);
		step.comp.initialize(obj);
		contentPanel.revalidate();
		contentPanel.repaint();
	}
	
	
	/**
	 * 
	 * @param step
	 */
	private final void updateBreadCrumbs(ConfigWizardStep step) {
		breadcrumbs.removeAll();
		JLabel lbl;
		
		final float fontSize = 11f;
		final Font visitedFont = breadcrumbs.getFont().deriveFont(Font.PLAIN, fontSize);
		final Color visitedColor = breadcrumbs.getForeground();
		final Font currentFont = breadcrumbs.getFont().deriveFont(Font.BOLD, fontSize);
		final Color currentColor = breadcrumbs.getForeground();
		final Font futureFont = breadcrumbs.getFont().deriveFont(Font.ITALIC, fontSize);
		final Color futureColor = breadcrumbs.getBackground().brighter();

		int i;
		for (i = 0; i < currentStep; i++) {
			lbl = new JLabel(">  " + steps.get(i).name + "  ");
			lbl.setFont(visitedFont);
			lbl.setForeground(visitedColor);
			breadcrumbs.add(lbl);
		}
		
		lbl = new JLabel(">  " + steps.get(i++).name + "  ");
		lbl.setFont(currentFont);
		lbl.setForeground(currentColor);
		breadcrumbs.add(lbl);
		
		for ( ; i < steps.size(); i++) {
			lbl = new JLabel(">  " + steps.get(i).name + "  ");
			lbl.setFont(futureFont);
			lbl.setForeground(futureColor);
			breadcrumbs.add(lbl);
		}
		breadcrumbs.revalidate();
		breadcrumbs.repaint();
	}
	
	
	/**
	 * 
	 */
	private final void updateButtons() {
		prevBtn.setVisible(currentStep > 0);
		nextBtn.setVisible(currentStep < steps.size() - 1);
		finishBtn.setVisible(currentStep == steps.size() - 1);
		cancelBtn.setVisible(true);
	}
	
	
	@Override
	public void run() {
		start();
	}
	
	
	/**
	 * 
	 */
	public void start() {
		if (!isVisible()) {
			setLocationRelativeTo(null);
		}
		
		gotoStep(0);
		setVisible(true);
	}
	
	
	/**
	 * 
	 */
	public final void next() {
		if (hasNext()) {
			steps.get(currentStep).comp.next(obj);
			gotoStep(currentStep + 1);
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public final boolean hasNext() {
		return currentStep < steps.size();
	}
	
	
	/**
	 * 
	 */
	public final void previous() {
		if (hasPrevious()) {
			steps.get(currentStep).comp.previous(obj);
			gotoStep(currentStep - 1);
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public final boolean hasPrevious() {
		return currentStep > 0;
	}
	
	
	/**
	 * 
	 */
	protected void cancel() {
		int confirm = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to cancel?",
				getTitle(), JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		
		if (confirm == JOptionPane.YES_OPTION) {
			canceled();
			close();
		}
	}
	
	
	/**
	 * 
	 */
	public void canceled() {
		//TODO: testing purposes only... should be overridden
		System.out.println("user canceled configuration");
	}
	
	
	/**
	 * 
	 */
	private final void close() {
		setVisible(false);
		dispose();
	}
	
	
	/**
	 * 
	 */
	protected final void finish() {
		finished(obj);
		close();
	}
	
	
	/**
	 * 
	 * @param obj
	 */
	public void finished(CONFIG_OBJ obj) {
		//TODO: testing purposes only... should be overridden
		System.out.println("finished configuration [" + obj + "]");
	}
	
	
	/**
	 * 
	 * @return
	 */
	private final JPanel makeDialog() {
		JPanel main = new JPanel( new BorderLayout() );
		
		main.add(makeTopPanel(), BorderLayout.NORTH);
		main.add(makeContentPanel(), BorderLayout.CENTER);
		main.add(makeBottomPanel(), BorderLayout.SOUTH);
		
		return main;
	}
	
	
	/**
	 * 
	 * @return
	 */
	private final JPanel makeTopPanel() {
		final Color baseColor = Color.white;
		final Color backColor = new Color(192, 192, 225);
		
		JPanel pnl = new JPanel( new BorderLayout() );
		pnl.setBackground(baseColor);
		
		JLabel titlePanel = new JLabel(getTitle()) {
			private static final long serialVersionUID = -3544817529363727844L;

			@Override
			public void paintComponent(Graphics g) {
				final int w = getWidth();
		        final int h = getHeight();
		        
		        Color c1 = baseColor;
		        Color c2 = backColor;
		        GradientPaint background =
		        		new GradientPaint(0, 0, c1, w, h, c2);
		        
				Graphics2D g2d = (Graphics2D)g;
				g2d.setPaint(background);
		        g2d.fillRect(0, 0, w, h);
		        
				super.paintComponent(g2d);
			}
		};
		titlePanel.setBorder( BorderFactory.createEmptyBorder(12, 12, 12, 12) );
		titlePanel.setFont( new Font(Font.DIALOG, Font.BOLD, 24) );
		pnl.add(titlePanel, BorderLayout.CENTER);
		
		breadcrumbs = new JPanel( new FlowLayout() );
		BoxLayout box = new BoxLayout(breadcrumbs, BoxLayout.X_AXIS);
		breadcrumbs.setBackground(backColor.darker());
		breadcrumbs.setBorder( BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 1, 0,
						breadcrumbs.getBackground().darker()), 
				BorderFactory.createEmptyBorder(4, 12, 4, 12) ) );
		breadcrumbs.setLayout(box);
		breadcrumbs.add( new JLabel("Home") );
		breadcrumbs.add(Box.createHorizontalGlue());
		pnl.add(breadcrumbs, BorderLayout.SOUTH);
		
		return pnl;
	}
	
	
	/**
	 * 
	 * @return
	 */
	private final JPanel makeContentPanel() {
		contentPanel = new JPanel( new BorderLayout() );
		contentPanel.setBorder( BorderFactory.createEmptyBorder(4, 12, 4, 12) );
		return contentPanel;
	}
	
	
	/**
	 * 
	 * @return
	 */
	private final JPanel makeBottomPanel() {
		JPanel pnl = new JPanel(null);
		BoxLayout box = new BoxLayout(pnl, BoxLayout.X_AXIS);
		pnl.setLayout(box);
		pnl.setBorder( BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0,
						pnl.getBackground().darker()), 
				BorderFactory.createEmptyBorder(4, 12, 4, 12) ) );
		
		JLabel versionLbl = new JLabel("ConfigWizard v" + version);
		versionLbl.setFont( new Font(Font.DIALOG_INPUT, Font.PLAIN, 10) );
		versionLbl.setForeground(pnl.getBackground().darker());
		pnl.add(versionLbl);
		pnl.add(Box.createHorizontalGlue());
		pnl.add( prevBtn = new JButton(PREVIOUS_ACTION) );
		pnl.add( nextBtn = new JButton(NEXT_ACTION) );
		pnl.add( finishBtn = new JButton(FINISH_ACTION) );
		pnl.add( cancelBtn = new JButton(CANCEL_ACTION) );
		return pnl;
	}
	
	
	/**
	 * 
	 */
	public final ConfigWizardAction NEXT_ACTION =
			new ConfigWizardAction("Next",
					"Proceed to the next step")
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 7363939000848215303L;

		@Override
		public void actionPerformed(ActionEvent ae) {
			next();
		}
		
	};
	
	
	/**
	 * 
	 */
	public final ConfigWizardAction PREVIOUS_ACTION =
			new ConfigWizardAction("Previous",
					"Go back to the previous step")
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = -5145247122592959510L;

		@Override
		public void actionPerformed(ActionEvent ae) {
			previous();
		}
		
	};
	
	
	/**
	 * 
	 */
	public final ConfigWizardAction FINISH_ACTION =
			new ConfigWizardAction("Finish",
					"Finish this configuration wizard")
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 7363939000848215303L;

		@Override
		public void actionPerformed(ActionEvent ae) {
			finish();
		}
		
	};
	
	
	/**
	 * 
	 */
	public final ConfigWizardAction CANCEL_ACTION =
			new ConfigWizardAction("Cancel",
					"Cancel this configuration wizard")
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 4818575052071560042L;

		@Override
		public void actionPerformed(ActionEvent ae) {
			cancel();
		}
		
	};
	
	
	/**
	 * 
	 * @author Chris
	 *
	 */
	private abstract class ConfigWizardAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8573673334528376495L;

		
		/**
		 * Constructor for new instance of the configuration wizard action with
		 * the specified text and tool-tip.
		 * 
		 * @param text
		 * @param tooltip
		 */
		private ConfigWizardAction(String text, String tooltip) {
			super(text);

			putValue(NAME, text);
			putValue(ACTION_COMMAND_KEY, text);
			putValue(SHORT_DESCRIPTION, tooltip);
			putValue(LONG_DESCRIPTION, tooltip);
		}
		
		
		/**
		 * Constructor for new instance of the configuration wizard action with
		 * the specified text, tool-tip and icon.
		 * 
		 * @param text
		 * @param tooltip
		 * @param icon
		 */
		private ConfigWizardAction(String text, String tooltip, Icon icon) {
			this(text, tooltip);
			
			putValue(LARGE_ICON_KEY, icon);
			putValue(SMALL_ICON, icon);
		}
		
	}
	
}
