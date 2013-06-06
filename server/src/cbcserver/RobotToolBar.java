/**
 * RobotToolBar.java
 * 
 * Created on 06.06.2013
 */

package cbcserver;


import static cbcserver.Robot.*;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import cbcserver.L10n.Localizable;
import cbcserver.actions.CBCGUIAction;


/**
 * <p>
 * {@code RobotToolBar}
 * </p>
 * 
 * @version V0.0 06.06.2013
 * @author SillyFreak
 */
public class RobotToolBar extends JToolBar implements Localizable {
    private static final long serialVersionUID = -6222822219385074782L;
    
    private final JCheckBox[] receivers;
    private final CBCGUI      gui;
    
    public RobotToolBar(CBCGUI gui) {
        super(SwingConstants.VERTICAL);
        this.gui = gui;
        
        receivers = new JCheckBox[robots.size()];
        for(int i = 0; i < receivers.length; i++) {
            receivers[i] = new JCheckBox();
            add(receivers[i]);
        }
    }
    
    @Override
    public void setL10n(L10n l10n) {
        for(int i = 0; i < receivers.length; i++) {
            receivers[i].setText("<html>" + robots.get(i).getHTMLNamePlain() + "</html>");
        }
    }
    
    public void addCommand(String name, char command) {
        add(new SendAction(this, name, command));
    }
    
    public CBCGUI getGui() {
        return gui;
    }
    
    private static final class SendAction extends CBCGUIAction {
        private static final long  serialVersionUID = -2703804741398073368L;
        
        private final RobotToolBar bar;
        private final char         command;
        
        public SendAction(RobotToolBar bar, String name, char command) {
            super(bar.getGui(), name);
            this.bar = bar;
            this.command = command;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                for(Robot r:robots)
                    if(bar.receivers[r.ordinal()].isSelected()) r.send(command);
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
