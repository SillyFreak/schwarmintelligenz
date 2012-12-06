
package cbcserver;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;


public class ClientThread extends Interruptible implements Commands {
    
    protected Socket           client;
    protected ConnectionServer server;
    protected PrintWriter      out;
    protected BufferedReader   in;
    protected Robot            robot;
    
    public ClientThread(ExecutorService pool, Socket client, ConnectionServer server, Robot robot) {
        super(pool);
        this.client = client;
        this.server = server;
        this.robot = robot;
        try {
            out = new PrintWriter(client.getOutputStream());
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void execute() {
        try {
            for(String m; (m = in.readLine()) != null;) {
                System.out.printf("client: %s received: '%s'%n", robot, m);
                robot.setActive(m.contains(ACTIVE));
            }
        } catch(Exception ex) {
            System.out.printf("client: %s disconnected (%s)%n", robot, ex);
            robot.setActive(false);
            server.clientThreads.remove(this);
            try {
                out.close();
                in.close();
                client.close();
            } catch(Exception e) {}
        }
    }
    
    public void send(String message) {
        System.out.printf("client: %s sent: '%s'%n", robot, message);
        out.write(message);
        out.flush();
    }
}
