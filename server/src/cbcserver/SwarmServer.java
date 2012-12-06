
package cbcserver;


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
 * @author Clemens Koza
 */
public class SwarmServer extends Interruptible {
    private static final Logger log = new Logger("Server");
    
    private final int           port;
    
    public SwarmServer(ExecutorService pool, int port) throws IOException {
        super(pool);
        this.port = port;
        
        for(Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces(); ni.hasMoreElements();) {
            NetworkInterface iface = ni.nextElement();
            log.printf("network interface: %s", iface.getDisplayName());
            for(Enumeration<InetAddress> ia = iface.getInetAddresses(); ia.hasMoreElements();) {
                InetAddress address = ia.nextElement();
                log.printf("  IP: %s", address.getHostAddress());
            }
        }
        log.printf("socket on port %d", port);
    }
    
    @Override
    protected void execute() {
        ServerSocket ssock = null;
        try {
            ssock = new ServerSocket(port);
            log.println("now waiting for connections");
            
            while(isRunning()) {
                try {
                    log.println("waiting for connection...");
                    Socket sock = ssock.accept();
                    String addr = sock.getInetAddress().getHostAddress();
                    log.printf("accepting %s", addr);
                    
                    Robot robot = Robot.getByAddress(addr);
                    if(robot == null) {
                        log.printf("unknown address %s", addr);
                    } else {
                        log.printf("%s connected", robot);
                        (robot.client = new RobotHandle(pool, sock, robot)).start();
                    }
                } catch(InterruptedIOException ex) {
                    log.printf("interrupted: %s", ex);
                } catch(IOException ex) {
                    log.trace(ex);
                }
            }
            log.println("exiting!");
        } catch(InterruptedIOException ex) {
            log.printf("interrupted: %s", ex);
        } catch(IOException ex) {
            log.trace(ex);
        } finally {
            try {
                ssock.close();
            } catch(IOException ex) {
                log.trace(ex);
            }
        }
    }
}
