package view.console;

import org.jhotdraw.util.prefs.PreferencesUtil;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.prefs.Preferences;
import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/3/13
 */
@SuppressWarnings("serial")
public class ConsolePanel extends JPanel {

    private JTextArea textArea = new JTextArea(15, 30);
    private TextAreaOutputStream taOutputStream = new TextAreaOutputStream(
            textArea, "Console");
    private Preferences prefs;

    public ConsolePanel() {
        prefs = PreferencesUtil.userNodeForPackage(getClass());
        setLayout(new BorderLayout());
        add(new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        PrintStream textPrintStream = new PrintStream(taOutputStream);
        System.setOut(textPrintStream);
        System.setErr(textPrintStream);

//        int timerDelay = 1000;
//        new Timer(timerDelay , new ActionListener() {
//            int count = 0;
//            @Override
//            public void actionPerformed(ActionEvent arg0) {
//
//                // though this outputs via System.out.println, it actually displays
//                // in the JTextArea:
//                System.out.println("Count is now: " + count + " seconds");
//                count++;
//            }
//        }).start();
    }

    public void createAndShowGui() {
        JFrame frame = new JFrame("Console");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ConsolePanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        PreferencesUtil.installFramePrefsHandler(prefs, "console", frame);
        Point loc = frame.getLocation();
        frame.setLocation(loc);
    }

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                createAndShowGui();
//            }
//        });
//    }
}