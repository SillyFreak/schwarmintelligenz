
package cbcserver;


import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;


public class ConnectionServer extends Interruptible {
    private static final Logger     log = new Logger("Server");
    
    private final int               port;
    private final List<Robot>       robots;
    
    public final List<ClientThread> clientThreads;
    
    public ConnectionServer(ExecutorService pool, int port, List<Robot> robots) throws IOException {
        super(pool);
        this.port = port;
        this.robots = robots;
        clientThreads = new ArrayList<ClientThread>();
        
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
    public void execute() {
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            log.println("now waiting for connections");
            
            while(isRunning()) {
                try {
                    log.println("waiting for connection...");
                    Socket socket = server.accept();
                    String ip = socket.getInetAddress().getHostAddress();
                    log.printf("accepting %s%n", ip);
                    Robot robot = null;
                    for(Robot r:robots) {
                        if(ip.contains(r.ip)) {
                            robot = r;
                            break;
                        }
                    }
                    if(robot == null) {
                        log.printf("unknown address %s%n", ip);
                    } else {
                        log.printf("%s connected%n", robot);
                    }
                    ClientThread st = new ClientThread(pool, socket, this, robot);
                    clientThreads.add(st);
                    st.start();
                } catch(InterruptedIOException ex) {
                    log.printf("interrupted: %s%n", ex);
                } catch(IOException ex) {
                    log.trace(ex);
                }
            }
            log.println("server: exiting!");
        } catch(InterruptedIOException ex) {
            log.printf("interrupted: %s%n", ex);
        } catch(IOException ex) {
            log.trace(ex);
        } finally {
            try {
                server.close();
            } catch(IOException ex) {
                log.trace(ex);
            }
        }
    }
    
    public synchronized void send(Robot robot, String message) {
        for(ClientThread s:clientThreads) {
            if(s.robot == robot) {
                s.send(message);
                break;
            }
        }
    }
    
    public synchronized void sendAll(String m) {
        for(ClientThread s:clientThreads) {
            s.send(m);
        }
    }
}
