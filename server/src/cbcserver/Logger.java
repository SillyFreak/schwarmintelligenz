/**
 * Logger.java
 * 
 * Created on 06.12.2012
 */

package cbcserver;


/**
 * <p>
 * The class Logger.
 * </p>
 * 
 * @version V0.0 06.12.2012
 * @author SillyFreak
 */
public class Logger {
    private static final int prefix = 8;
    private final String     header;
    
    public Logger(String name) {
        StringBuilder header = new StringBuilder(name);
        if(header.length() > prefix - 2) header.setLength(prefix - 2);
        header.append(": ");
        while(header.length() < prefix)
            header.append(' ');
        this.header = header.toString().replaceAll("%", "%%");
    }
    
    public void println(Object x) {
        System.out.println(header + x);
    }
    
    public void printf(String format, Object... args) {
        System.out.printf(header + format, args);
    }
    
    public void trace(Throwable ex) {
        System.err.print(header);
        ex.printStackTrace();
    }
}
