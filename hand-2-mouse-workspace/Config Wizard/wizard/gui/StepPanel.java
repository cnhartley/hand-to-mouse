/**
 * 
 */
package wizard.gui;

import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * 
 * 
 * @author Chris N. Hartley
 */
public abstract class StepPanel<CONFIG_OBJ> extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5858971483986419413L;

	
	/**
	 * Constructor for a new instance of this {@link StepPanel} with the 
	 * specified layout manager.
	 * 
	 * @param lm
	 */
	public StepPanel(LayoutManager lm) {
		super(lm);
	}
	
	
	/**
	 * Called when this implementation of the {@link StepPanel} becomes active
	 * in the {@link ConfigWizard}. This has the parameter of the collector 
	 * object the wizard is utilizing to store any user information in it.
	 * 
	 * @param obj
	 */
	abstract public void initialize(CONFIG_OBJ obj);
	
	
	/**
	 * Called when the user clicks on the previous button in the 
	 * {@link ConfigWizard} to go back to the previous step panel.
	 * 
	 * @param obj
	 */
	abstract public void previous(CONFIG_OBJ obj);
	
	
	/**
	 * Called when the user clicks on the next button in the 
	 * {@link ConfigWizard} to go to the next step panel. The implementation of
	 * this method should store any required information into the collector
	 * object that is passed as the parameter.
	 *  
	 * @param obj
	 */
	abstract public void next(CONFIG_OBJ obj);
	
}