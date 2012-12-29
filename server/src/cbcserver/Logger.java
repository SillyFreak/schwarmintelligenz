/**
 * Logger.java
 * 
 * Created on 06.12.2012
 */

package cbcserver;


import java.util.HashMap;
import java.util.Map;


/**
 * <p>
 * A simple Logger that prints an object name before the output.
 * </p>
 * 
 * @version V1.0 06.12.2012
 * @author Clemens Koza
 */
public class Logger {
    private static final int                  prefix  = 8;
    
    public static final int                   ALL     = -1;
    public static final int                   TRACE   = 0;
    public static final int                   DEBUG   = 1;
    public static final int                   INFO    = 2;
    public static final int                   WARNING = 3;
    public static final int                   ERROR   = 4;
    
    private static final Map<String, Integer> levels  = new HashMap<String, Integer>();
    
    public static void putLevel(String name, int level) {
        levels.put(name, level);
    }
    
    public static int getLevel(String name) {
        Integer i = levels.get(name);
        return i == null? -1:i;
    }
    
    private final int    level;
    private final String header;
    
    public Logger(String name) {
        this(name, -1);
    }
    
    public Logger(String name, int level) {
        this.level = level;
        StringBuilder header = new StringBuilder(name);
        if(header.length() > prefix - 2) header.setLength(prefix - 2);
        header.append(": ");
        while(header.length() < prefix)
            header.append(' ');
        this.header = header.toString().replaceAll("%", "%%");
    }
    
    public void printf(int level, String format, Object... args) {
        if(this.level <= level) System.out.printf(header + format + "%n", args);
    }
    
    public void trace(int level, Throwable ex) {
        if(this.level <= level) {
            System.err.print(header);
            ex.printStackTrace();
        }
    }
}
