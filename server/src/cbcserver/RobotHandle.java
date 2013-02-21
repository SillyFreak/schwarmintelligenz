
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
    protected Socket         sock;
    protected BufferedWriter out;
    protected BufferedReader in;
    protected Robot          robot;
    
    public RobotHandle(ExecutorService pool, Socket sock, Robot robot) {
        super(pool);
        this.sock = sock;
        this.robot = robot;
        try {
            out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        } catch(Exception ex) {
            robot.log.trace(ERROR, ex);
        }
    }
    
    @Override
    protected void execute() {
        try {
            for(int i; (i = in.read()) != -1;) {
                char c = (char) i;
                robot.log.printf(TRACE, "received '%s'", c);
                
                if(c == ACTIVE) robot.action.setCharging(false);
                else if(c == INACTIVE) robot.action.setCharging(true);
            }
        } catch(Exception ex) {
            robot.log.printf(WARNING, "disconnected (%s)", ex);
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
        robot.log.printf(TRACE, "sending '%s'", message);
        out.write(message);
        out.flush();
    }
}
