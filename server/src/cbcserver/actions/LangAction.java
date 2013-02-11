/**
 * LangAction.java
 * 
 * Created on 10.01.2013
 */

package cbcserver.actions;


import java.awt.event.ActionEvent;

import cbcserver.CBCGUI;


/**
 * <p>
 * The class LangAction.
 * </p>
 * 
 * @version V0.0 10.01.2013
 * @author SillyFreak
 */
public class LangAction extends CBCGUIAction {
    private static final long serialVersionUID = -5226127523632832790L;
    
    public LangAction(CBCGUI gui) {
        super(gui, "DE/EN");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        gui.toggleLang();
    }
}
