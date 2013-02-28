/**
 * LeaderAction.java
 * 
 * Created on 13.12.2012
 */

package cbcserver.actions;


import static cbcserver.Logger.*;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.painter.BusyPainter;
import org.jdesktop.swingx.painter.Painter;

import sun.swing.SwingUtilities2;
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
    private static final long        serialVersionUID = 4586228180396062210L;
    
    private final EventListenerList  listeners        = new EventListenerList();
    
    public static final int          SELECTED         = 1, CHARGING = 2, BUSY = 4;
    
    private static final BusyPainter bp;
    
    static {
        bp = new BusyPainter(70);
        int r = 5;
        bp.setPointShape(new Ellipse2D.Double(-r, -r, 2 * r, 2 * r));
        bp.setPoints(9);
        bp.setPaintCentered(true);
    }
    
    private ForegroundPainter        fp;
    private final Robot              robot;
    private int                      state;
    private long                     lastPing;
    
    public LeaderAction(CBCGUI gui, Robot robot) {
        super(gui, "", null);
        this.robot = robot;
        setState(CHARGING);
    }
    
    @Override
    public void setL10n(L10n l10n) {
        putValue(Action.NAME, l10n.format("button.charging", getRobot().displayName));
    }
    
    public void install(JXButton b) {
        b.setBackgroundPainter(new TogglePainter());
        b.setForegroundPainter(fp = new ForegroundPainter(b));
//        JXBusyLabel l = new JXBusyLabel();
//        l.setBorder(BorderFactory.createLineBorder(Color.BLACK));
//        l.setIcon(new EmptyIcon(30, 30));
//        l.setBusyPainter(bp);
//        l.setBusy(true);
//        
//        b.setLayout(new BorderLayout());
//        b.add(l);
    }
    
    public int getState() {
        return state;
    }
    
    public void setState(int state) {
        int change = this.state ^ state;
        
        this.state = state;
        setEnabled(!isCharging() && !isBusy());
        if((change & CHARGING) != 0) {
            fireChanged();
        }
        
        if(fp != null) fp.setBusy((state & BUSY) != 0);
    }
    
    public void setSelected(boolean selected) {
        setState(selected? (state | SELECTED):(state & ~SELECTED));
    }
    
    public boolean isSelected() {
        return (state & SELECTED) != 0;
    }
    
    public void checkTimeout() {
        if(System.currentTimeMillis() - lastPing > 1500 && !isCharging()) {
            robot.log.printf(DEBUG, "...ping timeout");
            setState(state ^ CHARGING);
        }
    }
    
    public void setCharging(boolean charging) {
        if(!charging) {
            robot.log.printf(DEBUG, "...ping update");
            lastPing = System.currentTimeMillis();
        }
        setState(charging? (state | CHARGING):(state & ~CHARGING));
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
        private final Color[] normal, pressed, selected, disabled;
        
        public TogglePainter() {
            Color color = robot.color;
            Color darker = color.darker();
            Color ddarker = darker.darker();
            
            normal = new Color[] {color, darker};
            pressed = new Color[] {color, ddarker};
            selected = new Color[] {darker, color};
            disabled = new Color[] {Color.LIGHT_GRAY, Color.GRAY};
        }
        
        @Override
        public void paint(Graphics2D g, JXButton b, int width, int height) {
            boolean isPressed = b.getModel().isArmed() && b.getModel().isPressed();
            Color[] colors = isCharging()? disabled:isSelected()? selected:isPressed? pressed:normal;
            int w = b.getWidth(), h = b.getHeight();
            
            g.setPaint(new LinearGradientPaint(w * .4f, h * .05f, w * .6f, h * .95f, floats, colors));
            g.fillRect(0, 0, w, h);
        }
    }
    
    private class ForegroundPainter implements Painter<JXButton> {
        private final JXButton b;
        private Timer          busy;
        
        private Rectangle      viewRect = new Rectangle();
        private Rectangle      textRect = new Rectangle();
        private Rectangle      iconRect = new Rectangle();
        
        public ForegroundPainter(JXButton b) {
            this.b = b;
        }
        
        @Override
        public void paint(Graphics2D g, JXButton b, int width, int height) {
            if(isCharging()) {
                layout(b, SwingUtilities2.getFontMetrics(b, g), b.getWidth(), b.getHeight());
                View v = (View) b.getClientProperty(BasicHTML.propertyKey);
                v.paint(g, textRect);
            } else if(isBusy()) {
                bp.paint(g, b, width, height);
            }
        }
        
        private String layout(AbstractButton b, FontMetrics fm, int width, int height) {
            Insets i = b.getInsets();
            viewRect.x = i.left;
            viewRect.y = i.top;
            viewRect.width = width - (i.right + viewRect.x);
            viewRect.height = height - (i.bottom + viewRect.y);
            
            textRect.x = textRect.y = textRect.width = textRect.height = 0;
            iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;
            
            // layout the text and icon
            return SwingUtilities.layoutCompoundLabel(b, fm, b.getText(), null, b.getVerticalAlignment(),
                    b.getHorizontalAlignment(), b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
                    viewRect, iconRect, textRect, b.getText() == null? 0:b.getIconTextGap());
        }
        
        public void setBusy(boolean busy) {
            if(busy) startAnimation();
            else stopAnimation();
        }
        
        private void startAnimation() {
            if(busy != null) {
                stopAnimation();
            }
            
            busy = new Timer(110, new ActionListener() {
                int frame = bp.getPoints();
                
                public void actionPerformed(ActionEvent e) {
                    frame = (frame + 1) % bp.getPoints();
                    bp.setFrame(frame);
                    b.repaint();
                }
            });
            busy.start();
        }
        
        private void stopAnimation() {
            if(busy != null) {
                busy.stop();
                b.repaint();
                busy = null;
            }
        }
    }
}
