package cbcserver;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Observable;

public class Robot extends Observable{

    public static final int ANZAHL = 4;
    String n;
    int number;
    String follow;
    String found;
    String ip;
    Color c;
    private boolean active;

    private Robot(String n, int number, String follow, String found, String ip, Color c) {
        this.n = n;
        this.number = number;
        this.follow = follow;
        this.found = found;
        this.ip = ip;
        this.c = c;
        this.active = false;
    }

    public static ArrayList<Robot> getAllRobots() {
        ArrayList<Robot> r = new ArrayList<Robot>(Robot.ANZAHL);
        r.add(new Robot("PINK", 0, "fo00", "fs00","192.168.1.11", Color.PINK));
        r.add(new Robot("ORANGE", 1, "fo01", "fs01", "192.168.1.12", Color.ORANGE));
        r.add(new Robot("GREEN", 2, "fo02", "fs02", "192.168.1.13", Color.GREEN));
        r.add(new Robot("BLUE", 3, "fo03", "fs03", "192.168.1.14", Color.BLUE));
        return r;
    }
    
    public String getHTMLName(){
        return "<html>"+getHTMLNamePlain()+"</html>";
    }
    
    public String getHTMLNamePlain(){
        return "<font color="+n+">"+n+"</font>";
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
        super.setChanged();
        super.notifyObservers(null);
    }

}
