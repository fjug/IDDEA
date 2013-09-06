package plugin.desginer;

import javax.swing.*;

/**
 * Post processor for the displayed source
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/5/13
 */
public class ProcessDesigner extends AbstractDesigner {

    public ProcessDesigner()
    {
        super("ProcessDesigner", "Process Image");
    }

    public void process()
    {

    }

    public static void main(String[] args) {
        // Start all Swing applications on the EDT.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new SourceDesigner().setVisible(true);
            }
        });
    }

}
