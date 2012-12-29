
package cbcserver;


import static cbcserver.Robot.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import cbcserver.actions.LeaderAction;
import cbcserver.actions.StatusAction;


/**
 * <p>
 * Main class of the swarm server. Shows a panel with buttons for the four robots, and starts the server and status
 * threads.
 * </p>
 * 
 * @version V1.0 06.12.2012
 * @author Clemens Koza
 */
public final class CBCGUI extends JRootPane implements Commands, ChangedListener {
    private static final long     serialVersionUID = -2620534937798975690L;
    
    public static final int       PORT             = 28109;
    
    private static final Logger   log              = new Logger("CBCGUI");
    private static boolean        debug            = false;
    
    private final JLabel          label;
    
    private final ExecutorService pool;
    private final BotStatus       bots;
    
    private Robot                 selectedRobot;
    
    public CBCGUI() throws IOException {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(p);
        
        //debug menu
        if(debug) makeJToolBar();
        
        { //center panel
            JPanel center = new JPanel(new GridLayout(1, 0, 3, 3));
            ButtonGroup g = new ButtonGroup();
            for(int i = 0; i < robots.size(); i++) {
                Robot r = robots.get(i);
                
                JToggleButton b = new JToggleButton(r.action = new LeaderAction(this, r));
                b.setForeground(Color.GRAY);
                b.setFocusPainted(false);
                b.setFont(b.getFont().deriveFont(30f));
                g.add(b);
                center.add(b);
                
                r.action.addChangedListener(this);
            }
            p.add(center, BorderLayout.CENTER);
        }
        
        { //status label
            label = new JLabel("<html>W&auml;hle den Anf&uuml;hrer</html>");
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setFont(label.getFont().deriveFont(30f));
            p.add(label, BorderLayout.SOUTH);
        }
        
        { //start server and status update thread
            pool = Executors.newCachedThreadPool();
            (new SwarmServer(pool, PORT)).start();
            (bots = new BotStatus(pool)).start();
        }
    }
    
    private void makeJToolBar() {
        JToolBar bar = new JToolBar(SwingConstants.VERTICAL);
        
        for(int i = 0; i < robots.size(); i++)
            bar.add(robots.get(i).receive);
        
        bar.add(new StatusAction(this));
        
        getContentPane().add(bar, BorderLayout.WEST);
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>
     * Re-orders the robots when a robot becomes active or inactive
     * </p>
     */
    @Override
    public void change(Robot robot) {
        log.printf("%s %s", robot.name(), robot.action.isEnabled()? "active":"inactive");
        orderRobots();
    }
    
    public void requestStatus() {
        bots.invoke();
    }
    
    /**
     * <p>
     * If a toggle button was pressed, selects a robot to be the leader, and re-orders them. For the debug-button,
     * the status update is invoked.
     * </p>
     */
    public void setLeader(Robot r) {
        selectedRobot = r;
        orderRobots();
    }
    
    /**
     * <p>
     * Orders the robots. The first active robot following the selected one will be the leader, the other ones will
     * follow their respective previous robots.
     * </p>
     */
    public void orderRobots() {
        try {
            //if no leader is selected, do nothing
            if(selectedRobot == null) return;
            
            log.printf("reorder robots");
            log.printf("  selected: %s", selectedRobot);
            
            int size = robots.size(), first = selectedRobot.ordinal();
            StringBuilder sb = new StringBuilder("<html>");
            Robot lastRobot = null;
            for(int i = first; i < first + size; i++) {
                Robot robot = robots.get(i % size);
                if(!robot.action.isEnabled()) {
                    log.printf("  %s inactive", robot);
                    continue;
                }
                
                if(lastRobot == null) {
                    //the first robot in the chain
                    robot.send(RANDOM);
                    log.printf("  leader: %s", robot);
                } else {
                    //a follower
                    sb.append(" &larr; ");
                    robot.send(lastRobot.follow);
                    log.printf("  next:   %s", robot);
                }
                //append HTML, set leader for the next robot
                sb.append(robot.getHTMLNamePlain());
                lastRobot = robot;
            }
            sb.append("</html>");
            if(lastRobot != null) label.setText(sb.toString());
            else log.printf("...no robot is active");
        } catch(IOException ex) {
            log.trace(ex);
        }
    }
    
    /**
     * Starts the application. The window can only be closed if debug is enabled.
     */
    public static void main(String... args) throws IOException {
        if(args.length == 1) debug = "debug".equals(args[0]);
        
        CBCGUI gui = new CBCGUI();
        
        JFrame frame = new JFrame("CBC Manager");
        frame.setDefaultCloseOperation(debug? JFrame.EXIT_ON_CLOSE:JFrame.DO_NOTHING_ON_CLOSE);
        frame.add(gui);
        frame.setSize(1000, 400);
        frame.setLocationRelativeTo(null);
        
        
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        if(!debug) {
            frame.setUndecorated(true);
            frame.setAlwaysOnTop(true);
            frame.setResizable(false);
            
            GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            System.out.println("Fullscreen: " + device.isFullScreenSupported());
            if(device.isFullScreenSupported()) {
                device.setFullScreenWindow(frame);
            }
        }
        frame.setVisible(true);
    }
    
    /**
     * <p>
     * Robot status update thread. By {@linkplain #invoke() invoking} the BotStatus object, a status update will be
     * printed immediately. Otherwise, a status update will occur every minute.
     * </p>
     */
    private class BotStatus extends Interruptible {
        public BotStatus(ExecutorService pool) {
            super(pool);
        }
        
        @Override
        protected synchronized void execute() {
            while(isRunning()) {
                try {
                    wait(60 * 1000);
                    log.printf("Status update...");
                    sendAll(STATUS);
                } catch(InterruptedException ex) {
                    log.trace(ex);
                } catch(IOException ex) {
                    log.trace(ex);
                }
            }
        }
        
        public synchronized void invoke() {
            notify();
        }
    }
}
