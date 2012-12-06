
package cbcserver;


import static java.lang.String.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JToggleButton;
import javax.swing.event.EventListenerList;


/**
 * <p>
 * Enum for the swarm's Robots.
 * </p>
 * 
 * @version V1.0 06.12.2012
 * @author SillyFreak
 */
public enum Robot {
    RED("ROT", "fo00", "192.168.1.11"),
    YELLOW("GELB", "fo01", "192.168.1.12"),
    GREEN("GR&Uuml;N", "fo02", "192.168.1.13"),
    BLUE("BLAU", "fo03", "192.168.1.14");
    
    public static final List<Robot>   robots = unmodifiableList(asList(Robot.values()));
    private static Map<String, Robot> byAddress;
    
    static {
        byAddress = new HashMap<String, Robot>();
        for(Robot r:values())
            byAddress.put(r.ip, r);
    }
    
    /**
     * <p>
     * Returns the robot by its IP address
     * </p>
     */
    public static Robot getByAddress(String addr) {
        return byAddress.get(addr);
    }
    
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
    
    private final EventListenerList listeners = new EventListenerList();
    private final Color             color;
    private final int               ccode;
    private final String            displayName;
    private final String            ip;
    
    public final String             follow;
    public final JToggleButton      button;
    
    private boolean                 active    = true;
    public RobotHandle              client;
    
    private Robot(String displayName, String follow, String ip) {
        this.color = getColor(name());
        this.ccode = color.getRGB() & 0x00FFFFFF;
        this.displayName = displayName;
        this.follow = follow;
        this.ip = ip;
        
        button = new JToggleButton("");
        button.setActionCommand(name());
        button.setOpaque(true);
        button.setBackground(color);
        button.setEnabled(active);
        button.setFont(button.getFont().deriveFont(30.0f));
    }
    
    public String getHTMLNamePlain() {
        return format("<font color=#%06X>%s</font>", ccode, displayName);
    }
    
    public boolean isActive() {
        return active;
    }
    
    /**
     * <p>
     * Enables/disables the toggle button and fires an event
     * </p>
     */
    public void setActive(boolean active) {
        this.active = active;
        button.setEnabled(active);
        fireChanged();
    }
    
    /**
     * <p>
     * Sends a message if the robot is connected and has an associated {@link RobotHandle}.
     * </p>
     */
    public void send(String m) throws IOException {
        if(active && client != null) client.send(m);
    }
    
    /**
     * <p>
     * Sends a message to every robot that is connected and has an associated {@link RobotHandle}.
     * </p>
     */
    public static void sendAll(String m) throws IOException {
        for(Robot r:robots)
            r.send(m);
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
}
