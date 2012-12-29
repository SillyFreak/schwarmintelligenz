
package cbcserver;


import static cbcserver.Logger.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;


/**
 * <p>
 * RobotHandle handles the network connection to a single robot.
 * </p>
 * 
 * @version V1.0 06.12.2012
 * @author SillyFreak
 */
public class RobotHandle extends Interruptible implements Commands {
    private final Logger     log;
    
    protected Socket         sock;
    protected BufferedWriter out;
    protected BufferedReader in;
    protected Robot          robot;
    
    public RobotHandle(ExecutorService pool, Socket sock, Robot robot) {
        super(pool);
        log = new Logger(robot.toString(), DEBUG);
        this.sock = sock;
        this.robot = robot;
        try {
            out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        } catch(Exception ex) {
            log.trace(ERROR, ex);
        }
    }
    
    @Override
    protected void execute() {
        try {
            for(int i; (i = in.read()) != -1;) {
                char c = (char) i;
                log.printf(DEBUG, "received '%s'", c);
                
                if(c == ACTIVE) robot.action.setEnabled(true);
                else if(c == INACTIVE) robot.action.setEnabled(false);
            }
        } catch(Exception ex) {
            log.printf(WARNING, "disconnected (%s)", ex);
        } finally {
            robot.action.setEnabled(false);
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
    
    /**
     * <p>
     * Sends a string message to the robot
     * </p>
     */
    public void send(char message) throws IOException {
        log.printf(DEBUG, "sending '%s'", message);
        out.write(message);
        out.flush();
    }
}
