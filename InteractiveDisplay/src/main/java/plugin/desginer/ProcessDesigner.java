package plugin.desginer;

import org.jhotdraw.util.prefs.PreferencesUtil;
import plugin.ProcessPlugin;
import view.display.InteractiveDisplayView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

/**
 * Post processor for the displayed source
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/5/13
 */
public class ProcessDesigner extends AbstractDesigner {

    private final Preferences prefs;

    public ProcessDesigner()
    {
        super("ProcessDesigner");

        buttons.put("Process Image", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                process();
            }
        });

        initializeComponents();
        prefs = PreferencesUtil.userNodeForPackage(getClass());

        PreferencesUtil.installFramePrefsHandler(prefs, "processDesigner", this);
        Point loc = this.getLocation();
        this.setLocation(loc);
    }

    public void process()
    {
        if(plugin != null)
        {
            ProcessPlugin prsPlugin = (ProcessPlugin)plugin;
            InteractiveDisplayView view = model.getDisplayView();

            // Process the whole picture
            prsPlugin.process(view.interval);

            // Process the selected regions
        }
        else
        {
            System.out.println("Compile it first!");
        }
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
