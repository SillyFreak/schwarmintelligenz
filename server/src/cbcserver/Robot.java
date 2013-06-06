
package cbcserver;


import static cbcserver.Logger.*;
import static java.lang.String.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;

import cbcserver.L10n.Localizable;
import cbcserver.actions.LeaderAction;


/**
 * <p>
 * Enum for the swarm's Robots.
 * </p>
 * 
 * @version V1.1 06.12.2012
 * @author SillyFreak
 */
public enum Robot implements Localizable {
    RED("192.168.1.11"), YELLOW("192.168.1.12"), GREEN("192.168.1.13"), MAGENTA("192.168.1.14");
    
    //enum stuff
    
    public static final List<Robot>        robots;
    public static final Map<String, Robot> byAddress;
    
    static {
        robots = unmodifiableList(asList(Robot.values()));
        
        HashMap<String, Robot> m = new HashMap<String, Robot>();
        for(Robot r:values())
            m.put(r.ip, r);
        byAddress = unmodifiableMap(m);
    }
    
    /**
     * <p>
     * Returns the robot by its index
     * </p>
     */
    public static Robot getByIndex(int index) {
        return robots.get(index);
    }
    
    /**
     * <p>
     * Returns the robot by its IP address
     * </p>
     */
    public static Robot getByAddress(String addr) {
        return byAddress.get(addr);
    }
    
    private Robot(String ip) {
        //logic
        this.ip = ip;
        this.follow = (char) ('0' + ordinal());
        this.listeners = new EventListenerList();
        
        //GUI
        this.color = getColor(name());
        this.ccode = color.getRGB() & 0x00FFFFFF;
//        this.receive = new JCheckBox("", true);
    }
    
    //helper
    
    /**
     * <p>
     * Returns a color by the field name in {@link Color}
     * </p>
     */
    private static Color getColor(String name) {
        try {
            return (Color) Color.class.getField(name).get(null);
        } catch(IllegalAccessException ex) {
            throw new AssertionError(ex);
        } catch(NoSuchFieldException ex) {
            return null;
        }
    }
    
    public final Logger             log = new Logger(name(), DEBUG);
    
    //logic
    
    public static final int         SELECTED = 0x1, CHARGING = 2, BUSY = 0x4;
    
    public final String             ip;
    public final char               follow;
    private int                     state;
    private long                    lastPing;
    public RobotHandle              client;
    private final EventListenerList listeners;
    
    public int getState() {
        return state;
    }
    
    public void setState(int state) {
        int change = this.state ^ state;
        this.state = state;
        
        //enabled if neither charging nor busy
        action.setEnabled((state & (CHARGING | BUSY)) == 0);
        if((change & CHARGING) != 0) {
            fireChanged();
        }
        
        action.setBusy((state & BUSY) != 0);
    }
    
    public void setSelected(boolean selected) {
        setState(selected? (state | SELECTED):(state & ~SELECTED));
    }
    
    public boolean isSelected() {
        return (state & SELECTED) != 0;
    }
    
    public void checkTimeout() {
        if(System.currentTimeMillis() - lastPing > 1500 && !isCharging()) {
            log.printf(DEBUG, "...ping timeout");
            setState(state ^ CHARGING);
        }
    }
    
    public void setCharging(boolean charging) {
        if(!charging) {
            log.printf(DEBUG, "...ping update");
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
    
    private void fireChanged() {
        // Guaranteed to return a non-null array
        Object[] l = listeners.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i = l.length - 2; i >= 0; i -= 2) {
            if(l[i] == ChangedListener.class) {
                ((ChangedListener) l[i + 1]).change(this);
            }
        }
    }
    
    public void addChangedListener(ChangedListener l) {
        listeners.add(ChangedListener.class, l);
    }
    
    public void removeChangedListener(ChangedListener l) {
        listeners.remove(ChangedListener.class, l);
    }
    
    /**
     * <p>
     * Sends a message if the robot is connected and has an associated {@link RobotHandle}.
     * </p>
     */
    public void send(char m) throws IOException {
        if(client != null) client.send(m);
    }
    
    /**
     * <p>
     * Sends a message to every robot that is connected and has an associated {@link RobotHandle}.
     * </p>
     */
    public static void sendAll(char m) throws IOException {
        for(Robot r:robots)
            r.send(m);
    }
    
    //GUI
    
//    public final JCheckBox receive;
    public final Color  color;
    private final int   ccode;
    public String       displayName;
    public LeaderAction action;
    
    @Override
    public void setL10n(L10n l10n) {
        displayName = l10n.format("robot." + ordinal());
//        receive.setText("<html>" + displayName + "</html>");
        action.setL10n(l10n);
    }
    
    public String getHTMLNamePlain() {
        return format("<font color=#%06X>%s</font>", ccode, displayName);
    }
}
