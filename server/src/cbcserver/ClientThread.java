
package cbcserver;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;


public class ClientThread extends Interruptible implements Commands {
    private final Logger       log;
    
    protected Socket           client;
    protected ConnectionServer server;
    protected PrintWriter      out;
    protected BufferedReader   in;
    protected Robot            robot;
    
    public ClientThread(ExecutorService pool, Socket client, ConnectionServer server, Robot robot) {
        super(pool);
        log = new Logger(robot.toString());
        this.client = client;
        this.server = server;
        this.robot = robot;
        try {
            out = new PrintWriter(client.getOutputStream());
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch(Exception ex) {
            log.trace(ex);
        }
    }
    
    @Override
    public void execute() {
        try {
            for(String m; (m = in.readLine()) != null;) {
                log.printf("received '%s'%n", m);
                robot.setActive(m.contains(ACTIVE));
            }
        } catch(Exception ex) {
            log.printf("disconnected (%s)%n", robot, ex);
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
        log.printf("sent '%s'%n", message);
        out.write(message);
        out.flush();
    }
}
