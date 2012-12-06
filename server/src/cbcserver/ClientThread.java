package cbcserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread implements Commands{

    protected Socket client;
    protected ConnectionServer server;
    protected PrintWriter out;
    protected BufferedReader in;
    protected Robot robot;
    
    public ClientThread(Socket client, ConnectionServer server, Robot robot) {
        this.client = client;
        this.server = server;
        this.robot = robot;
        try {
            out = new PrintWriter(client.getOutputStream());
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.start();
    }

    @Override public void run() {
        try {
            while (true) {
                String m = in.readLine();
                //System.out.println(robot.n+": "+m);
                if (m.contains(ACTIVE)){
                    //System.out.println("ACTIVSETZEN");
                    robot.setActive(true);
                } else if (m.contains(INACTIVE)){
                    //System.out.println("INACTIVSETZEN");
                    robot.setActive(false);
                }
            }
        } catch (Exception ex) {
            System.out.println(robot.n+" disconnected ("+ex+")");
            robot.setActive(false);
            server.clientThreads.remove(this);
            try {
                out.close();
                in.close();
                client.close();
            } catch (Exception e) {
            }
        }
    }
    
    public void send(String message){
        out.write(message);
        out.flush();
    }
}
