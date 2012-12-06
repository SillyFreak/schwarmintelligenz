
package cbcserver;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;


public class RobotHandle extends Interruptible implements Commands {
    private final Logger     log;
    
    protected Socket         sock;
    protected BufferedWriter out;
    protected BufferedReader in;
    protected Robot          robot;
    
    public RobotHandle(ExecutorService pool, Socket sock, Robot robot) {
        super(pool);
        log = new Logger(robot.toString());
        this.sock = sock;
        this.robot = robot;
        try {
            out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        } catch(Exception ex) {
            log.trace(ex);
        }
    }
    
    @Override
    protected void execute() {
        try {
            for(String msg; (msg = in.readLine()) != null;) {
                log.printf("received '%s'%n", msg);
                robot.setActive(msg.contains(ACTIVE));
            }
        } catch(Exception ex) {
            log.printf("disconnected (%s)%n", ex);
        } finally {
            robot.setActive(false);
            robot.client = null;
            try {
                out.close();
            } catch(Exception e) {}
            try {
                in.close();
            } catch(Exception e) {}
            try {
                sock.close();
            } catch(Exception e) {}
        }
    }
    
    public void send(String message) throws IOException {
        log.printf("sending '%s'%n", message);
        out.write(message);
        out.flush();
    }
}