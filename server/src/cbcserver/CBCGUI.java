
package cbcserver;


import static java.util.Arrays.*;
import static java.util.Collections.*;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
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


public final class CBCGUI extends JRootPane implements Commands, ChangedListener, ActionListener {
    private static final long       serialVersionUID = -2620534937798975690L;
    
    public static final int         PORT             = 28109;
    public static final List<Robot> robots           = unmodifiableList(asList(Robot.values()));
    
    private static final Logger     log              = new Logger("CBCGUI");
    private static final boolean    debug            = true;
    
    private final JMenuItem         status;
    private final JLabel            label;
    
    private final ExecutorService   pool;
    private final SwarmServer       cs;
    private final BotStatus         bots;
    
    private Robot                   selectedRobot;
    
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
            (cs = new SwarmServer(pool, PORT)).start();
            (bots = new BotStatus(pool)).start();
        }
    }
    
    @Override
    public void change(Robot robot) {
        log.printf("%s %s%n", robot.name(), robot.isActive()? "active":"inactive");
        robot.button.setEnabled(robot.isActive());
        orderRobots();
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if(ae.getSource() == status) {
            bots.invoke();
            return;
        }
        
        if(!((AbstractButton) ae.getSource()).isSelected()) return;
        selectedRobot = Robot.valueOf(ae.getActionCommand());
        if(selectedRobot.isActive()) {
            orderRobots();
        }
    }
    
    public void orderRobots() {
        try {
            if(selectedRobot == null) return;
            cs.send(selectedRobot, RANDOM);
            
            int size = robots.size(), first = selectedRobot.ordinal();
            
            StringBuilder sb = new StringBuilder("<html>");
            sb.append(selectedRobot.getHTMLNamePlain());
            Robot lastRobot = selectedRobot;
            for(int i = (first + 1) % size; i != first; i = (i + 1) % size) {
                Robot nextRobot = robots.get(i);
                if(!nextRobot.isActive()) continue;
                
                sb.append(" &larr; ").append(nextRobot.getHTMLNamePlain());
                cs.send(nextRobot, lastRobot.follow);
                lastRobot = nextRobot;
            }
            sb.append("</html>");
            label.setText(sb.toString());
        } catch(IOException ex) {
            log.trace(ex);
        }
    }
    
    public static void main(String... args) throws IOException {
        CBCGUI gui = new CBCGUI();
        
        JFrame frame = new JFrame("CBC Manager");
        frame.setDefaultCloseOperation(debug? JFrame.EXIT_ON_CLOSE:JFrame.DO_NOTHING_ON_CLOSE);
        frame.add(gui);
        frame.setSize(1000, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
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
                    cs.sendAll(STATUS);
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
