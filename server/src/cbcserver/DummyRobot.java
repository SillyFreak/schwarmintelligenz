/**
 * DummyRobot.java
 * 
 * Created on 21.02.2013
 */

package cbcserver;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


/**
 * <p>
 * The class DummyRobot.
 * </p>
 * 
 * @version V0.0 21.02.2013
 * @author SillyFreak
 */
public class DummyRobot implements Commands {
    public static void main(String[] args) {
        try {
            Socket s = null;
            for(int i = 0; i < Robot.robots.size(); i++) {
                try {
                    SocketAddress local = new InetSocketAddress(InetAddress.getLocalHost(), CBCGUI.PORT + 1 + i);
                    SocketAddress server = new InetSocketAddress(InetAddress.getLocalHost(), CBCGUI.PORT);
                    
                    s = new Socket();
                    s.bind(local);
                    s.connect(server);
                    break;
                } catch(BindException ex) {
                    s = null;
                }
            }
            if(s == null) return;
            
            
            InputStream is = s.getInputStream();
            OutputStream os = s.getOutputStream();
            for(int i; (i = is.read()) != -1;) {
                char c = (char) i;
                
                System.out.println(c);
                
                if(c == STATUS) os.write(ACTIVE);
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}
