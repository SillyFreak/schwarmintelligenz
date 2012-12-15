
package cbcserver;


import static java.lang.String.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;

import cbcserver.actions.LeaderAction;


/**
 * <p>
 * Enum for the swarm's Robots.
 * </p>
 * 
 * @version V1.0 06.12.2012
 * @author Clemens Koza
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
    
    private final String   ip;
    
    public final JCheckBox receive;
    public final Color     color;
    private final int      ccode;
    public final String    displayName;
    public final String    follow;
    public LeaderAction    action;
    
    public RobotHandle     client;
    
    private Robot(String displayName, String follow, String ip) {
        this.color = getColor(name());
        this.ccode = color.getRGB() & 0x00FFFFFF;
        this.displayName = displayName;
        this.follow = follow;
        this.ip = ip;
        
        receive = new JCheckBox("<html>" + displayName + "</html>", true);
    }
    
    public String getHTMLNamePlain() {
        return format("<font color=#%06X>%s</font>", ccode, displayName);
    }
    
    /**
     * <p>
     * Sends a message if the robot is connected and has an associated {@link RobotHandle}.
     * </p>
     */
    public void send(String m) throws IOException {
        if(receive.isSelected() && client != null) client.send(m);
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
}
