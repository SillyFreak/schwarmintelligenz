
package cbcserver;


/**
 * <p>
 * Command strings for the CBC communication.
 * </p>
 * 
 * @version V1.0 06.12.2012
 * @author Clemens Koza
 */
public interface Commands {
    public static String STOP      = "ao00";
    public static String FORWARD   = "ao01";
    public static String BACKWARD  = "ao02";
    public static String LEFT      = "ao03";
    public static String RIGHT     = "ao04";
    public static String RECONNECT = "ao05";
    public static String RANDOM    = "fo04";
    
    public static String ACTIVE    = "actv";
    public static String INACTIVE  = "iact";
    public static String STATUS    = "stat";
}
