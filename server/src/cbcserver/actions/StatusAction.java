/**
 * StatusAction.java
 * 
 * Created on 13.12.2012
 */

package cbcserver.actions;


import java.awt.event.ActionEvent;

import cbcserver.CBCGUI;


/**
 * <p>
 * The class StatusAction.
 * </p>
 * 
 * @version V0.0 13.12.2012
 * @author Clemens Koza
 */
public class StatusAction extends CBCGUIAction {
    private static final long serialVersionUID = -4073967451357096295L;
    
    public StatusAction(CBCGUI gui) {
        super(gui, "Request Status");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        gui.requestStatus();
    }
}
