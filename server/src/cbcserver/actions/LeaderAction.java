/**
 * LeaderAction.java
 * 
 * Created on 13.12.2012
 */

package cbcserver.actions;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.EventListenerList;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.painter.Painter;

import cbcserver.CBCGUI;
import cbcserver.ChangedListener;
import cbcserver.L10n;
import cbcserver.L10n.Localizable;
import cbcserver.Robot;


/**
 * <p>
 * The class LeaderAction.
 * </p>
 * 
 * @version V0.0 13.12.2012
 * @author SillyFreak
 */
public class LeaderAction extends CBCGUIAction implements Localizable {
    private static final long       serialVersionUID = 4586228180396062210L;
    
    private final EventListenerList listeners        = new EventListenerList();
    
    public static final int         SELECTED         = 1, CHARGING = 2, BUSY = 4;
    
    private final Robot             robot;
    private String                  chargingText;
    private int                     state;
    
    public LeaderAction(CBCGUI gui, Robot robot) {
        super(gui, "", null);
        this.robot = robot;
//        setState(CHARGING);
    }
    
    @Override
    public void setL10n(L10n l10n) {
        chargingText = l10n.format("button.charging", getRobot().displayName);
        putValue(Action.NAME, isCharging()? chargingText:"");
    }
    
    public void install(JXButton b) {
        b.setBackgroundPainter(new TogglePainter(getRobot()));
    }
    
    public int getState() {
        return state;
    }
    
    public void setState(int state) {
        int change = this.state ^ state;
        
        this.state = state;
        setEnabled(!isCharging() && !isBusy());
        putValue(Action.NAME, isCharging()? chargingText:"");
        if((change & CHARGING) != 0) fireChanged();
    }
    
    public void setSelected(boolean selected) {
        setState(selected? (state | SELECTED):(state & ~SELECTED));
    }
    
    public boolean isSelected() {
        return (state & SELECTED) != 0;
    }
    
    public boolean isCharging() {
        return (state & CHARGING) != 0;
    }
    
    public void setBusy(boolean busy) {
        setState(busy? (state | BUSY):(state & ~BUSY));
    }
    
    public boolean isBusy() {
        return (state & BUSY) != 0;
    }
    
    private Robot getRobot() {
        return robot;
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
        if(!isSelected()) {
            setState(state ^ SELECTED);
            gui.setLeader(getRobot());
        }
    }
    
    private class TogglePainter implements Painter<JXButton> {
        private final float[] floats = {0, 1};
        private final Color[] normal, selected, disabled;
        
        public TogglePainter(Robot r) {
            Color color = r.color;
            Color darker = color.darker();
            Color ddarker = darker.darker();
            
            normal = new Color[] {color, darker};
            selected = new Color[] {ddarker, color};
            disabled = new Color[] {Color.LIGHT_GRAY, Color.GRAY};
        }
        
        @Override
        public void paint(Graphics2D g, JXButton b, int width, int height) {
            Color[] colors = isCharging()? disabled:isSelected()? selected:normal;
            int w = b.getWidth(), h = b.getHeight();
            g.setPaint(new LinearGradientPaint(w * .4f, h * .05f, w * .6f, h * .95f, floats, colors));
            g.fillRect(0, 0, w, h);
        }
    }
}
