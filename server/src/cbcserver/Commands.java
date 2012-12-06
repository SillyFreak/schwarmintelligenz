
package cbcserver;


public interface Commands {
    public String STOP = "ao00";
    public String FORWARD = "ao01";
    public String BACKWARD = "ao02";
    public String LEFT = "ao03";
    public String RIGHT = "ao04";
    public String RECONNECT = "ao05";
    public String RANDOM = "fo04";
    
    public String ACTIVE = "actv";
    public String INACTIVE = "iact";
    public String STATUS = "stat";
}
