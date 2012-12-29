/**
 * Logger.java
 * 
 * Created on 06.12.2012
 */

package cbcserver;


/**
 * <p>
 * A simple Logger that prints an object name before the output.
 * </p>
 * 
 * @version V1.0 06.12.2012
 * @author Clemens Koza
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
    
    public void printf(String format, Object... args) {
        System.out.printf(header + format + "%n", args);
    }
    
    public void trace(Throwable ex) {
        System.err.print(header);
        ex.printStackTrace();
    }
}
