
package cbcserver;


import static cbcserver.Logger.*;
import static cbcserver.Robot.*;
import static javax.swing.JOptionPane.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXRootPane;

import cbcserver.actions.LeaderAction;
import cbcserver.actions.StatusAction;


/**
 * <p>
 * Main class of the swarm server. Shows a panel with buttons for the four robots, and starts the server and status
 * threads.
 * </p>
 * 
 * @version V1.0 06.12.2012
 * @author SillyFreak
 */
public final class CBCGUI extends JXRootPane implements Commands, ChangedListener {
    private static final long       serialVersionUID = -2620534937798975690L;
    
    public static final int         PORT             = 28109;
    
    private static final Logger     log              = new Logger("CBCGUI", DEBUG);
    private static boolean          debug            = false;
    
    private final JLabel            label;
    private final JXCollapsiblePane help;
    
    private final ExecutorService   pool;
    private final BotStatus         bots;
    
    private Robot                   selectedRobot;
    
    public CBCGUI() throws IOException {
        JXPanel p = new JXPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(p);
        
        //debug menu
        if(debug) makeJToolBar();
        
        { //center panel
            JXPanel buttons = new JXPanel(new GridLayout(1, 0, 3, 3));
            ButtonGroup g = new ButtonGroup();
            for(int i = 0; i < robots.size(); i++) {
                Robot r = robots.get(i);
                
                JToggleButton b = new JToggleButton(r.action = new LeaderAction(this, r));
                b.setForeground(Color.GRAY);
                b.setFocusPainted(false);
                b.setFont(b.getFont().deriveFont(30f));
                g.add(b);
                buttons.add(b);
                
                r.action.addChangedListener(this);
            }
            
            help = new JXCollapsiblePane(Direction.DOWN);
            help.add(makeHelpPane());
            help.setCollapsed(true);
            
            JXPanel center = new JXPanel(new BorderLayout());
            center.add(buttons, BorderLayout.CENTER);
            center.add(help, BorderLayout.SOUTH);
            p.add(center);
        }
        
        { //status label
            label = new JXLabel();
            label.setHorizontalAlignment(JXLabel.CENTER);
            label.setFont(label.getFont().deriveFont(30f));
            
            JXPanel status = new JXPanel(new BorderLayout());
            status.add(label);
            status.add(new JToggleButton());
            
            p.add(status, BorderLayout.SOUTH);
        }
        
        { //start server and status update thread
            pool = Executors.newCachedThreadPool();
            (new SwarmServer(pool, PORT)).start();
            (bots = new BotStatus(pool)).start();
        }
        
        orderRobots();
    }
    
    private void makeJToolBar() {
        JToolBar bar = new JToolBar(SwingConstants.VERTICAL);
        
        for(int i = 0; i < robots.size(); i++)
            bar.add(robots.get(i).receive);
        
        bar.add(new StatusAction(this));
        
        getContentPane().add(bar, BorderLayout.WEST);
    }
    
    private JComponent makeHelpPane() {
        JXPanel help = new JXPanel();
        help.add(new JXLabel("help!"));
        
        JScrollPane sp = new JScrollPane(help);
        sp.setPreferredSize(new Dimension(0, 300));
        sp.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        return sp;
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
        log.printf(DEBUG, "%s %s", robot.name(), robot.action.isEnabled()? "active":"inactive");
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
            log.printf(INFO, "reorder robots...");
            
            int size = robots.size(), first = selectedRobot == null? 0:selectedRobot.ordinal();
            Robot active = null;
            for(int i = first; i < first + size; i++) {
                Robot r = robots.get(i % size);
                if(r.action.isEnabled() && active == null) {
                    active = r;
                    break;
                }
            }
            
            if(active == null) {
                //nothing active; clear the selected state
                if(selectedRobot != null) {
                    selectedRobot.action.setSelected(false);
                    selectedRobot = null;
                }
                label.setText("<html>Kein Roboter Aktiv...</html>");
                log.printf(INFO, "...no robot is active");
                
            } else if(selectedRobot == null) {
                //active, but nothing selected
                label.setText("<html>W&auml;hle einen Anführer!</html>");
                log.printf(INFO, "...no robot selected");
                
            } else {
                //active, and selection. reorder robots
                
                selectedRobot.action.setSelected(false);
                selectedRobot = active;
                selectedRobot.action.setSelected(true);
                
                log.printf(INFO, "...selected: %s", selectedRobot);
                
                StringBuilder sb = new StringBuilder("<html>");
                Robot lastRobot = null;
                for(int i = first; i < first + size; i++) {
                    Robot robot = robots.get(i % size);
                    if(!robot.action.isEnabled()) {
                        log.printf(TRACE, "  %s inactive", robot);
                        continue;
                    }
                    
                    if(lastRobot == null) {
                        //the first robot in the chain
                        robot.send(RANDOM);
                        log.printf(TRACE, "  leader: %s", robot);
                    } else {
                        //a follower
                        sb.append(" &larr; ");
                        robot.send(lastRobot.follow);
                        log.printf(TRACE, "  next:   %s", robot);
                    }
                    //append HTML, set leader for the next robot
                    sb.append(robot.getHTMLNamePlain());
                    lastRobot = robot;
                }
                sb.append("</html>");
                label.setText(sb.toString());
//            } else {
//                //active, and selection, but no change
//                log.printf(INFO, "...same leader");
            }
        } catch(IOException ex) {
            log.trace(ERROR, ex);
        }
    }
    
    /**
     * Starts the application. The window can only be closed if debug is enabled.
     */
    public static void main(String... args) throws IOException {
        if(args.length == 1) debug = "debug".equals(args[0]);
        
        JOptionPane.showMessageDialog(null, "", "", INFORMATION_MESSAGE);
        
        CBCGUI gui = new CBCGUI();
        
        JXFrame frame = new JXFrame("CBC Manager");
        frame.setDefaultCloseOperation(debug? JFrame.EXIT_ON_CLOSE:JFrame.DO_NOTHING_ON_CLOSE);
        frame.setRootPane(gui);
        frame.setSize(1000, 400);
        frame.setLocationRelativeTo(null);
        
        
        frame.setExtendedState(JXFrame.MAXIMIZED_BOTH);
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
        
        //TODO busy pane
//        JXBusyLabel l = new JXBusyLabel(new Dimension(50, 50));
//        l.setAlignmentX(CENTER_ALIGNMENT);
//        l.setBusy(true);
//        
//        JXPanel p = new JXPanel(null);
//        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
//        p.add(Box.createVerticalGlue());
//        p.add(l);
//        p.add(Box.createVerticalGlue());
//        
//        frame.setWaitPane(p);
//        frame.setWaitPaneVisible(true);
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
                    log.printf(INFO, "Status update...");
                    sendAll(STATUS);
                } catch(InterruptedException ex) {
                    log.trace(WARNING, ex);
                } catch(IOException ex) {
                    log.trace(ERROR, ex);
                }
            }
        }
        
        public synchronized void invoke() {
            notify();
        }
    }
}
