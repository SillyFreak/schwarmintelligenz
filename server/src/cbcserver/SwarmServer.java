
package cbcserver;


import static cbcserver.Logger.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;


/**
 * <p>
 * The SwarmServer accepts connections from the robots. A robot is identified by its IP, so no other connections
 * will be accepted
 * </p>
 * 
 * @version V1.0 06.12.2012
 * @author SillyFreak
 */
public class SwarmServer extends Interruptible {
    private static final Logger log = new Logger("Server", INFO);
    
    private final int           port;
    
    public SwarmServer(ExecutorService pool, int port) throws IOException {
        super(pool);
        this.port = port;
        
        for(Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces(); ni.hasMoreElements();) {
            NetworkInterface iface = ni.nextElement();
            log.printf(TRACE, "network interface: %s", iface.getDisplayName());
            for(Enumeration<InetAddress> ia = iface.getInetAddresses(); ia.hasMoreElements();) {
                InetAddress address = ia.nextElement();
                log.printf(TRACE, "  IP: %s", address.getHostAddress());
            }
        }
        log.printf(INFO, "socket on port %d", port);
    }
    
    @Override
    protected void execute() {
        ServerSocket ssock = null;
        try {
            ssock = new ServerSocket(port);
            log.printf(TRACE, "now waiting for connections");
            
            while(isRunning()) {
                try {
                    log.printf(TRACE, "waiting for connection...");
                    Socket sock = ssock.accept();
                    
                    if(sock.getInetAddress().isLoopbackAddress()) {
                        int index = sock.getPort() - (CBCGUI.PORT + 1);
                        if(index < 0 || index >= Robot.robots.size()) {
                            log.printf(TRACE, "unknown dummy %s", index);
                        } else {
                            Robot robot = Robot.robots.get(index);
                            log.printf(INFO, "dummy connected: %s", robot);
                            (robot.client = new RobotHandle(pool, sock, robot)).start();
                        }
                    } else {
                        String addr = sock.getInetAddress().getHostAddress();
                        log.printf(TRACE, "accepting %s", addr);
                        
                        Robot robot = Robot.getByAddress(addr);
                        if(robot == null) {
                            log.printf(TRACE, "unknown address %s", addr);
                        } else {
                            log.printf(INFO, "robot connected: %s", robot);
                            (robot.client = new RobotHandle(pool, sock, robot)).start();
                        }
                    }
                } catch(InterruptedIOException ex) {
                    log.printf(DEBUG, "interrupted: %s", ex);
                } catch(IOException ex) {
                    log.trace(WARNING, ex);
                }
            }
            log.printf(INFO, "exiting!");
        } catch(InterruptedIOException ex) {
            log.printf(DEBUG, "interrupted: %s", ex);
        } catch(IOException ex) {
            log.trace(WARNING, ex);
        } finally {
            try {
                ssock.close();
            } catch(IOException ex) {
                log.trace(WARNING, ex);
            }
        }
    }
}
