
package cbcserver;


import static java.util.Arrays.*;
import static java.util.Collections.*;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;


public final class CBCGUI extends JPanel implements Commands, ChangedListener, Runnable {
    private static final long      serialVersionUID = -2620534937798975690L;
    
    public static int              PORT             = 28109;
    
    private final ConnectionServer cs;
    
    private final List<Robot>      robots;
    private final JLabel           label;
    private Robot                  selectedRobot;
    
    public CBCGUI() throws IOException {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        robots = unmodifiableList(asList(Robot.values()));
        
        JPanel center = new JPanel(new GridLayout(1, 0, 3, 3));
        ButtonListener bl = new ButtonListener();
        ButtonGroup g = new ButtonGroup();
        for(int i = 0; i < robots.size(); i++) {
            Robot r = robots.get(i);
            JToggleButton b = r.button;
            g.add(b);
            b.addActionListener(bl);
            r.addChangedListener(this);
            center.add(b);
        }
        add(center, BorderLayout.CENTER);
        
        label = new JLabel("Choose the first");
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(label.getFont().deriveFont(30f));
        this.add(label, BorderLayout.SOUTH);
        cs = new ConnectionServer(Executors.newCachedThreadPool(), PORT, robots);
        cs.start();
        new Thread(this).start();
    }
    
    @Override
    public void change(Robot robot) {
        System.out.printf("gui:    %s: %s%n", robot.name(), robot.isActive()? "active":"inactive");
        robot.button.setEnabled(robot.isActive());
        orderRobots();
    }
    
    public class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent ae) {
            if(!((AbstractButton) ae.getSource()).isSelected()) return;
            
            selectedRobot = Robot.valueOf(ae.getActionCommand());
            
            if(selectedRobot.isActive()) orderRobots();
        }
    }
    
    public void orderRobots() {
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
    }
    
    @Override
    public void run() {
        while(!Thread.interrupted()) {
            try {
                Thread.sleep(60 * 1000);
                for(Robot r:robots) {
                    if(r.isActive()) {
                        cs.send(r, STATUS);
                    }
                }
            } catch(InterruptedException ex) {
                ex.printStackTrace();
            }
            
        }
    }
    
    public static void main(String... args) throws IOException {
        CBCGUI gui = new CBCGUI();
        
        JFrame frame = new JFrame("CBC Manager");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.add(gui);
        frame.setSize(1000, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
