
package cbcserver;


import static cbcserver.CBCGUI.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;


public class SwarmServer extends Interruptible {
    private static final Logger log = new Logger("Server");
    
    private final int           port;
    
    public SwarmServer(ExecutorService pool, int port) throws IOException {
        super(pool);
        this.port = port;
        
        for(Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces(); ni.hasMoreElements();) {
            NetworkInterface iface = ni.nextElement();
            log.printf("network interface: %s%n", iface.getDisplayName());
            for(Enumeration<InetAddress> ia = iface.getInetAddresses(); ia.hasMoreElements();) {
                InetAddress address = ia.nextElement();
                log.printf("  IP: %s%n", address.getHostAddress());
            }
        }
        log.printf("socket on port %d%n", port);
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
                    log.printf("accepting %s%n", addr);
                    
                    Robot robot = Robot.getByAddress(addr);
                    if(robot == null) {
                        log.printf("unknown address %s%n", addr);
                    } else {
                        log.printf("%s connected%n", robot);
                        (robot.client = new RobotHandle(pool, sock, robot)).start();
                    }
                } catch(InterruptedIOException ex) {
                    log.printf("interrupted: %s%n", ex);
                } catch(IOException ex) {
                    log.trace(ex);
                }
            }
            log.println("exiting!");
        } catch(InterruptedIOException ex) {
            log.printf("interrupted: %s%n", ex);
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
    
    public synchronized void send(Robot robot, String message) throws IOException {
        robot.send(message);
    }
    
    public synchronized void sendAll(String m) throws IOException {
        for(Robot r:robots)
            r.send(m);
    }
}
