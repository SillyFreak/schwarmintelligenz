/**
 * LeaderAction.java
 * 
 * Created on 13.12.2012
 */

package cbcserver.actions;


import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.event.ActionEvent;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.event.EventListenerList;

import cbcserver.CBCGUI;
import cbcserver.ChangedListener;
import cbcserver.Robot;


/**
 * <p>
 * The class LeaderAction.
 * </p>
 * 
 * @version V0.0 13.12.2012
 * @author SillyFreak
 */
public class LeaderAction extends CBCGUIAction {
    private static final long       serialVersionUID = 4586228180396062210L;
    
    private final EventListenerList listeners        = new EventListenerList();
    
    private static final String     ROBOT_KEY        = "LeaderAction:RobotKey";
    
    public LeaderAction(CBCGUI gui, Robot robot) {
        super(gui, "", new ToggleIcon(robot));
        putValue(ROBOT_KEY, robot);
        setEnabled(false);
    }
    
    private Robot getRobot() {
        return (Robot) getValue(ROBOT_KEY);
    }
    
    @Override
    public void setEnabled(boolean newValue) {
        super.setEnabled(newValue);
        putValue(Action.NAME, newValue? "":"<html>" + getRobot().displayName + "</html>");
        fireChanged();
    }
    
    private void fireChanged() {
        Robot r = getRobot();
        
        // Guaranteed to return a non-null array
        Object[] l = listeners.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i = l.length - 2; i >= 0; i -= 2) {
            if(l[i] == ChangedListener.class) {
                ((ChangedListener) l[i + 1]).change(r);
            }
        }
    }
    
    public void addChangedListener(ChangedListener l) {
        listeners.add(ChangedListener.class, l);
    }
    
    public void removeChangedListener(ChangedListener l) {
        listeners.remove(ChangedListener.class, l);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(!((AbstractButton) e.getSource()).isSelected()) return;
        gui.setLeader(getRobot());
    }
    
    /**
     * Icon that paints a color dependent on the enablead & selected state
     */
    private static class ToggleIcon implements Icon {
        private static final float[] floats = {0, 1};
        private final Color[]        normal, selected, disabled;
        
        public ToggleIcon(Robot r) {
            Color color = r.color;
            Color darker = color.darker();
            Color ddarker = darker.darker();
            
            normal = new Color[] {color, darker};
            selected = new Color[] {ddarker, color};
            disabled = new Color[] {Color.LIGHT_GRAY, Color.GRAY};
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            JToggleButton b = (JToggleButton) c;
            Graphics2D g2d = (Graphics2D) g;
            
            Color[] colors = !b.isEnabled()? disabled:b.isSelected()? selected:normal;
            int w = b.getWidth(), h = b.getHeight();
            g2d.setPaint(new LinearGradientPaint(w * .4f, h * .05f, w * .6f, h * .95f, floats, colors));
            g2d.fillRect(0, 0, w, h);
        }
        
        @Override
        public int getIconWidth() {
            return 0;
        }
        
        @Override
        public int getIconHeight() {
            return 0;
        }
    }
}
