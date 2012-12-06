
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
            System.out.printf("server: network interface: %s%n", iface.getDisplayName());
            for(Enumeration<InetAddress> ia = iface.getInetAddresses(); ia.hasMoreElements();) {
                InetAddress address = ia.nextElement();
                System.out.printf("server:   IP: %s%n", address.getHostAddress());
            }
        }
        System.out.printf("server: socket on port %d%n", port);
    }
    
    @Override
    public void execute() {
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            System.out.println("server: now waiting for connections");
            
            while(isRunning()) {
                try {
                    System.out.println("server: waiting for connection...");
                    Socket socket = server.accept();
                    String ip = socket.getInetAddress().toString();
                    System.out.printf("server: accepting %s%n", ip);
                    Robot robot = null;
                    for(Robot r:robots) {
                        if(ip.contains(r.ip)) {
                            robot = r;
                            break;
                        }
                    }
                    if(robot == null) {
                        System.out.printf("server: unknown address %s%n", ip);
                    } else {
                        System.out.printf("server: %s connected%n", robot);
                    }
                    ClientThread st = new ClientThread(pool, socket, this, robot);
                    clientThreads.add(st);
                    st.start();
                } catch(InterruptedIOException ex) {
                    System.out.printf("server: interrupted: %s%n", ex);
                } catch(IOException ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("server: exiting!");
        } catch(InterruptedIOException ex) {
            System.out.printf("server: interrupted: %s%n", ex);
        } catch(IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                server.close();
            } catch(IOException ex) {
                ex.printStackTrace();
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
