
package cbcserver;


import static cbcserver.Logger.*;
import static cbcserver.Robot.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXRootPane;

import cbcserver.L10n.Localizable;
import cbcserver.actions.HelpAction;
import cbcserver.actions.LangAction;
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
public final class CBCGUI extends JXRootPane implements Commands, ChangedListener, Localizable {
    private static final long       serialVersionUID = -2620534937798975690L;
    
    private static final L10n[]     l10ns            = {new L10n(Locale.GERMAN), new L10n(Locale.ENGLISH)};
    public static final int         PORT             = 28109;
    
    private static final Logger     log              = new Logger("CBCGUI", DEBUG);
    private static boolean          debug            = false;
    
    private final JLabel            label;
    private final JXCollapsiblePane help;
    
    private final ExecutorService   pool;
    private final BotStatus         bots;
    private final Busy              busy;
    
    private Robot                   selectedRobot;
    private L10n                    l10n;
    
    public CBCGUI() throws IOException {
        JXPanel p = new JXPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(p);
        
        //debug menu
        if(debug) makeJToolBar();
        
        { //center panel
            JXPanel buttons = new JXPanel(new GridLayout(1, 0, 3, 3));
//            ButtonGroup g = new ButtonGroup();
            for(int i = 0; i < robots.size(); i++) {
                Robot r = robots.get(i);
                
                JXButton b = new JXButton(r.action = new LeaderAction(this, r));
                r.action.install(b);
                b.setForeground(Color.GRAY);
                b.setFocusPainted(false);
                b.setFont(b.getFont().deriveFont(30f));
                buttons.add(b);
                
//                JToggleButton b = new JToggleButton(r.action = new LeaderAction(this, r));
//                b.setForeground(Color.GRAY);
//                b.setFocusPainted(false);
//                b.setFont(b.getFont().deriveFont(30f));
//                g.add(b);
//                buttons.add(b);
                
                r.action.addChangedListener(this);
            }
            
            help = new JXCollapsiblePane(Direction.DOWN);
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
            
            HelpAction aHelp = new HelpAction(this);
            JToggleButton help = new JToggleButton(aHelp);
            aHelp.installIcons(help);
            help.setContentAreaFilled(false);
            help.setBorder(null);
            help.setFocusPainted(false);
            
            LangAction aLang = new LangAction(this);
            JXButton lang = new JXButton(aLang);
            lang.setFocusPainted(false);
            
            JXPanel buttons = new JXPanel(new GridLayout());
            buttons.add(help);
            buttons.add(lang);
            
            JXPanel status = new JXPanel(new BorderLayout());
            status.add(label);
            status.add(buttons, BorderLayout.EAST);
            
            p.add(status, BorderLayout.SOUTH);
        }
        
        { //start server and status update thread
            pool = Executors.newCachedThreadPool();
            (new SwarmServer(pool, PORT)).start();
            (bots = new BotStatus(pool)).start();
            (busy = new Busy(pool)).start();
        }
        
        setL10n(l10ns[0]);
        orderRobots();
    }
    
    public void toggleLang() {
        setL10n(l10n == l10ns[0]? l10ns[1]:l10ns[0]);
    }
    
    @Override
    public void setL10n(L10n l10n) {
        this.l10n = l10n;
        help.getContentPane().removeAll();
        help.getContentPane().add(makeHelpPane());
        
        for(Robot r:robots)
            r.setL10n(l10n);
        updateLabels();
    }
    
    public void setHelpVisible(boolean visible) {
        help.setCollapsed(!visible);
    }
    
    private void makeJToolBar() {
        JToolBar bar = new JToolBar(SwingConstants.VERTICAL);
        
        for(int i = 0; i < robots.size(); i++)
            bar.add(robots.get(i).receive);
        
        bar.add(new StatusAction(this));
        
        getContentPane().add(bar, BorderLayout.WEST);
    }
    
    private JComponent makeHelpPane() {
        JXLabel l = new JXLabel(l10n.format("help"));
        l.setFont(l.getFont().deriveFont(16f));
        l.setVerticalAlignment(SwingUtilities.TOP);
        JXPanel help = new JXPanel(new BorderLayout());
        help.add(l);
        
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
        log.printf(DEBUG, "%s %s", robot.name(), robot.action.isCharging()? "inactive":"active");
        orderRobots();
    }
    
    public void requestStatus() {
        bots.invoke();
    }
    
    /**
     * <p>
     * Selects the robot to be the leader and deselects the other robots, the orders the robots.
     * </p>
     */
    public void setLeader(Robot r) {
        selectedRobot = r;
        for(Robot other:robots)
            if(r != other) other.action.setSelected(false);
        orderRobots();
    }
    
    /**
     * <p>
     * Orders the robots. The first active robot following the selected one will be the leader, the other ones will
     * follow their respective previous robots.
     * </p>
     */
    private void orderRobots() {
        try {
            log.printf(INFO, "reorder robots...");
            
            int size = robots.size(), first = selectedRobot == null? 0:selectedRobot.ordinal();
            Robot active = null;
            for(int i = first; i < first + size; i++) {
                Robot r = robots.get(i % size);
                if(!r.action.isCharging()) {
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
                label.setText(l10n.format("order.noActive"));
                log.printf(INFO, "...no robot is active");
                
            } else if(selectedRobot == null) {
                //active, but nothing selected
                label.setText(l10n.format("order.choose"));
                log.printf(INFO, "...no robot selected");
                
            } else {
                //active, and selection. reorder robots
                
                busy.invoke();
                
                selectedRobot.action.setSelected(false);
                selectedRobot = active;
                selectedRobot.action.setSelected(true);
                
                log.printf(INFO, "...selected: %s", selectedRobot);
                
                StringBuilder sb = new StringBuilder("<html>");
                Robot lastRobot = null;
                for(int i = first; i < first + size; i++) {
                    Robot robot = robots.get(i % size);
                    if(robot.action.isCharging()) {
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
            }
        } catch(IOException ex) {
            log.trace(ERROR, ex);
        }
    }
    
    private void updateLabels() {
        int size = robots.size(), first = selectedRobot == null? 0:selectedRobot.ordinal();
        Robot active = null;
        for(int i = first; i < first + size; i++) {
            Robot r = robots.get(i % size);
            if(!r.action.isCharging()) {
                active = r;
                break;
            }
        }
        
        if(active == null) {
            //nothing active; clear the selected state
            label.setText(l10n.format("order.noActive"));
            
        } else if(selectedRobot == null) {
            //active, but nothing selected
            label.setText(l10n.format("order.choose"));
            
        } else {
            //active, and selection. reorder robots
            
            StringBuilder sb = new StringBuilder("<html>");
            Robot lastRobot = null;
            for(int i = first; i < first + size; i++) {
                Robot robot = robots.get(i % size);
                if(robot.action.isCharging()) {
                    continue;
                }
                
                if(lastRobot != null) {
                    //a follower
                    sb.append(" &larr; ");
                }
                //append HTML, set leader for the next robot
                sb.append(robot.getHTMLNamePlain());
                lastRobot = robot;
            }
            sb.append("</html>");
            label.setText(sb.toString());
        }
    }
    
    /**
     * Starts the application. The window can only be closed if debug is enabled.
     */
    public static void main(String... args) throws IOException {
        if(args.length == 1) debug = "debug".equals(args[0]);
        
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
    
    private class Busy extends Interruptible {
        public Busy(ExecutorService pool) {
            super(pool);
        }
        
        @Override
        protected synchronized void execute() {
            while(isRunning()) {
                try {
                    wait();
                    log.printf(INFO, "Robots are now busy...");
                    Thread.sleep(3 * 1000);
                    for(Robot r:robots)
                        r.action.setBusy(false);
                    log.printf(INFO, "Robots are now ready!");
                } catch(InterruptedException ex) {
                    log.trace(WARNING, ex);
                }
            }
        }
        
        public synchronized void invoke() {
            for(Robot r:robots)
                r.action.setBusy(true);
            notify();
        }
    }
}
