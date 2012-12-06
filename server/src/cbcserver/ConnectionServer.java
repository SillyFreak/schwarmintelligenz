package cbcserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ConnectionServer implements Runnable {

    protected ServerSocket server;
    protected Thread connectionThread;
    public ArrayList<Robot> robots;
    public ArrayList<ClientThread> clientThreads;

    public ConnectionServer(int port, ArrayList<Robot> robots) {
        this.robots = robots;
        clientThreads = new ArrayList<ClientThread>();

        try {
            System.out.println("Serversocket auf Port " + port);
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            InetAddress[] inetAddresses = InetAddress.getAllByName(hostAddress);
            for (InetAddress inetAddress : inetAddresses) {
                System.out.println("IP: "+inetAddress.getHostAddress());
            }
            server = new ServerSocket(port);
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        connectionThread = new Thread(this);
        connectionThread.start();
    }

    public void run() {
        while (!Thread.interrupted()) {
            try {
                Socket socket = server.accept();
                String ip = socket.getInetAddress().toString();
                Robot robot = null;
                for (Robot r : robots) {
                    if (ip.contains(r.ip)) {
                        robot = r;
                        break;
                    }
                }
                if (robot == null) {
                    System.out.println("Roboter-Adresse " + ip + " nicht gefunden");
                } else {
                    System.out.println(robot.n + " connected");
                }
                ClientThread st = new ClientThread(socket, this, robot);
                clientThreads.add(st);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() throws IOException {
        if (connectionThread.isAlive()) {
            connectionThread.interrupt();
        }
        server.close();
    }

    public synchronized void send(Robot robot, String message) {
        for (ClientThread s : clientThreads) {
            if (s.robot == robot) {
                s.send(message);
                break;
            }
        }
    }

    public synchronized void sendAll(String m) {
        for (ClientThread s : clientThreads) {
            s.send(m);
        }
    }
}
