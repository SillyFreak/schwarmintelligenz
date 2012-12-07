
package cbcserver;


/**
 * <p>
 * Command strings for the CBC communication.
 * </p>
 * 
 * @version V1.0 06.12.2012
 * @author SillyFreak
 */
public interface Commands {
    public static final String STOP      = "ao00";
    public static final String FORWARD   = "ao01";
    public static final String BACKWARD  = "ao02";
    public static final String LEFT      = "ao03";
    public static final String RIGHT     = "ao04";
    public static final String RECONNECT = "ao05";
    public static final String RANDOM    = "fo04";
    
    public static final String ACTIVE    = "actv";
    public static final String INACTIVE  = "iact";
    public static final String STATUS    = "stat";
}
