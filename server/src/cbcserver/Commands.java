
package cbcserver;


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
