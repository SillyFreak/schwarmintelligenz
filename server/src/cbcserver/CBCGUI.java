package cbcserver;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.Border;

public final class CBCGUI extends JPanel implements Commands, Observer, Runnable {

    public static int PORT = 28109;
    public ArrayList<Robot> robots;
    private JToggleButton[] button;
    private JLabel label;
    private ConnectionServer cs;
    private Robot selectedRobot;

    public CBCGUI() {
        this.setLayout(new BorderLayout());
        robots = Robot.getAllRobots();
        button = new JToggleButton[Robot.ANZAHL];
        ButtonListener bl = new ButtonListener();
        Border border = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        JPanel center = new JPanel();
        center.setBorder(border);
        center.setLayout(new GridLayout(1, Robot.ANZAHL));
        this.add(center, BorderLayout.CENTER);
        for (int i = 0; i < button.length; i++) {
            Robot r = robots.get(i);
            button[i] = new JToggleButton(r.getHTMLName());
            button[i].addActionListener(bl);
            button[i].setOpaque(true);
            button[i].setBackground(r.c);
            button[i].setEnabled(false);
            button[i].setFont(button[i].getFont().deriveFont(30.0f));
            r.addObserver(this);
            center.add(button[i]);
        }
        label = new JLabel("Choose the first");
        label.setBorder(border);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(label.getFont().deriveFont(30.0f));
        this.add(label, BorderLayout.SOUTH);
        cs = new ConnectionServer(PORT, robots);
        new Thread(this).start();
    }

    @Override
    public void update(Observable o, Object o1) {
        Robot robot = (Robot) o;
        System.out.println(robot.n + ": " + (robot.isActive() ? "active" : "inactive"));
        for (JToggleButton b : button) {
            if (b.getText().equals(robot.getHTMLName())) {

                if (robot.isActive()) {
                    //b.setBackground(Color.green);
                    b.setEnabled(true);
                } else {
                    //b.setBackground(Color.red);
                    b.setEnabled(false);
                }
                break;
            }
        }
        orderRobots();
    }

    public class ButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            JToggleButton theButton = (JToggleButton) (ae.getSource());
            if (!theButton.isSelected()) { // Already Pressed?
                theButton.setSelected(true);
                return;
            }
            String color = ae.getActionCommand();
            for (Robot r : robots) {
                if (r.getHTMLName().equals(color)) {
                    selectedRobot = r;
                    break;
                }
            }
            if (selectedRobot.isActive()) {
                orderRobots();
                for (JToggleButton b : button) {
                    b.setSelected(false);
                }
                theButton.setSelected(true);
            } else {
                theButton.setSelected(false);
            }
        }
    }

    public void orderRobots() {
        if (selectedRobot == null) {
            return;
        }
        String text = "<html>" + selectedRobot.getHTMLNamePlain();
        cs.send(selectedRobot, RANDOM);
        int i = selectedRobot.number + 1;
        if (i >= Robot.ANZAHL) {
            i = 0;
        }
        Robot lastRobot = selectedRobot;
        while (i != selectedRobot.number) {
            Robot nextRobot = robots.get(i);
            if (nextRobot.isActive()) {
                text += " &larr " + nextRobot.getHTMLNamePlain();
                cs.send(nextRobot, lastRobot.follow);
                lastRobot = nextRobot;
            }
            i++;
            if (i >= Robot.ANZAHL) {
                i = 0;
            }
        }
        label.setText(text + "</html>");
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(60 * 1000);
                for (Robot r : robots) {
                    if (r.isActive()) {
                        cs.send(r, STATUS);
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

        }
    }

    public static void main(String... args) {
        JFrame frame = new JFrame("CBC Manager");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.add(new CBCGUI());
        frame.setSize(1000, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}