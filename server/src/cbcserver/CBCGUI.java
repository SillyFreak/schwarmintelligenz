
package cbcserver;


import static cbcserver.Robot.*;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JToggleButton;


/**
 * <p>
 * Main class of the swarm server. Shows a panel with buttons for the four robots, and starts the server and status
 * threads.
 * </p>
 * 
 * @version V1.0 06.12.2012
 * @author SillyFreak
 */
public final class CBCGUI extends JRootPane implements Commands, ChangedListener, ActionListener {
    private static final long     serialVersionUID = -2620534937798975690L;
    
    public static final int       PORT             = 28109;
    
    private static final Logger   log              = new Logger("CBCGUI");
    private static final boolean  debug            = true;
    
    private final JMenuItem       status;
    private final JLabel          label;
    
    private final ExecutorService pool;
    private final BotStatus       bots;
    
    private Robot                 selectedRobot;
    
    public CBCGUI() throws IOException {
        JComponent p = (JComponent) getContentPane();
        p.setLayout(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        //debug menu
        if(debug) {
            status = new JMenuItem("Request Status");
            status.addActionListener(this);
            
            JMenu debug = new JMenu("Debug");
            debug.add(status);
            
            JMenuBar bar = new JMenuBar();
            bar.add(debug);
            
            setJMenuBar(bar);
        } else status = null;
        
        { //center panel
            JPanel center = new JPanel(new GridLayout(1, 0, 3, 3));
            ButtonGroup g = new ButtonGroup();
            for(int i = 0; i < robots.size(); i++) {
                Robot r = robots.get(i);
                r.addChangedListener(this);
                
                JToggleButton b = r.button;
                b.addActionListener(this);
                g.add(b);
                center.add(b);
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
    
    /**
     * {@inheritDoc}
     * 
     * <p>
     * Re-orders the robots when a robot becomes active or inactive
     * </p>
     */
    @Override
    public void change(Robot robot) {
        log.printf("%s %s", robot.name(), robot.isActive()? "active":"inactive");
        orderRobots();
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>
     * If a toggle button was pressed, selects a robot to be the leader, and re-orders them. For the debug-button,
     * the status update is invoked.
     * </p>
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
        if(ae.getSource() == status) {
            bots.invoke();
            return;
        }
        
        if(!((AbstractButton) ae.getSource()).isSelected()) return;
        selectedRobot = Robot.valueOf(ae.getActionCommand());
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
            
            log.println("reorder robots");
            log.printf("  selected: %s", selectedRobot);
            
            int size = robots.size(), first = selectedRobot.ordinal();
            StringBuilder sb = new StringBuilder("<html>");
            Robot lastRobot = null;
            for(int i = first; i < first + size; i++) {
                Robot robot = robots.get(i % size);
                if(!robot.isActive()) continue;
                
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
            else log.println("...no robot is active");
        } catch(IOException ex) {
            log.trace(ex);
        }
    }
    
    /**
     * Starts the application. The window can only be closed if debug is enabled.
     */
    public static void main(String... args) throws IOException {
        CBCGUI gui = new CBCGUI();
        
        JFrame frame = new JFrame("CBC Manager");
        frame.setDefaultCloseOperation(debug? JFrame.EXIT_ON_CLOSE:JFrame.DO_NOTHING_ON_CLOSE);
        frame.add(gui);
        frame.setSize(1000, 400);
        frame.setLocationRelativeTo(null);
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
                    log.println("Status update...");
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
