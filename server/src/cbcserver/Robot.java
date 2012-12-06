
package cbcserver;


import static java.lang.String.*;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JToggleButton;
import javax.swing.event.EventListenerList;


public enum Robot {
    RED("fo00", "fs00", "192.168.1.11"),
    YELLOW("fo01", "fs01", "192.168.1.12"),
    GREEN("fo02", "fs02", "192.168.1.13"),
    BLUE("fo03", "fs03", "192.168.1.14");
    
    private static Map<String, Robot> byAddress;
    
    static {
        byAddress = new HashMap<String, Robot>();
        for(Robot r:values())
            byAddress.put(r.ip, r);
    }
    
    public static Robot getByAddress(String addr) {
        return byAddress.get(addr);
    }
    
    private static Color getColor(String name) {
        try {
            return (Color) Color.class.getField(name).get(null);
        } catch(IllegalAccessException ex) {
            throw new AssertionError(ex);
        } catch(NoSuchFieldException ex) {
            return null;
        }
    }
    
    public final String             follow;
    public final String             found;
    public final String             ip;
    public final Color              c;
    public final JToggleButton      button;
    
    private final EventListenerList listeners = new EventListenerList();
    
    public RobotHandle             client;
    private boolean                 active    = false;
    
    private Robot(String follow, String found, String ip) {
        this.follow = follow;
        this.found = found;
        this.ip = ip;
        this.c = getColor(name());
        
        button = new JToggleButton(getHTMLName());
        button.setActionCommand(name());
        button.setOpaque(true);
        button.setBackground(c);
        button.setEnabled(false);
        button.setFont(button.getFont().deriveFont(30.0f));
    }
    
    public String getHTMLName() {
        return format("<html><font color=%s>%1$s</font></html>", name());
    }
    
    public String getHTMLNamePlain() {
        return format("<font color=%s>%1$s</font>", name());
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
        fireChanged();
    }
    
    public void send(String m) throws IOException {
        if(active && client != null) client.send(m);
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
