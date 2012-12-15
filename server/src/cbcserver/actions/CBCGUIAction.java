/**
 * CBCGUIAction.java
 * 
 * Created on 13.12.2012
 */

package cbcserver.actions;


import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import cbcserver.CBCGUI;


/**
 * <p>
 * The class CBCGUIAction.
 * </p>
 * 
 * @version V0.0 13.12.2012
 * @author SillyFreak
 */
public abstract class CBCGUIAction extends AbstractAction {
    private static final long serialVersionUID = -5028070500077758037L;
    
    protected final CBCGUI    gui;
    
    public CBCGUIAction(CBCGUI gui, String name) {
        super(name);
        this.gui = gui;
    }
    
    public CBCGUIAction(CBCGUI gui, String name, Icon icon) {
        super(name, icon);
        this.gui = gui;
    }
    
    @Override
    public abstract void actionPerformed(ActionEvent e);
}
