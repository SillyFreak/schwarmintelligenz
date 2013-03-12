
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

import javax.swing.JCheckBox;

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
    
    public final Logger    log = new Logger(name(), DEBUG);
    private final String   ip;
    
    public final JCheckBox receive;
    public final Color     color;
    private final int      ccode;
    public String          displayName;
    public final char      follow;
    public LeaderAction    action;
    
    public RobotHandle     client;
    
    private Robot(String ip) {
        this.color = getColor(name());
        this.ccode = color.getRGB() & 0x00FFFFFF;
        this.follow = (char) ('0' + ordinal());
        this.ip = ip;
        
        receive = new JCheckBox("", true);
    }
    
    @Override
    public void setL10n(L10n l10n) {
        displayName = l10n.format("robot." + ordinal());
        receive.setText("<html>" + displayName + "</html>");
        action.setL10n(l10n);
    }
    
    public String getHTMLNamePlain() {
        return format("<font color=#%06X>%s</font>", ccode, displayName);
    }
    
    /**
     * <p>
     * Sends a message if the robot is connected and has an associated {@link RobotHandle}.
     * </p>
     */
    public void send(char m) throws IOException {
        if(receive.isSelected() && client != null) client.send(m);
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
}
