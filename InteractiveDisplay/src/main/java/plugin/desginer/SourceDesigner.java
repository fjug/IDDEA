package plugin.desginer;

import net.imglib2.RealRandomAccessible;
import org.jhotdraw.util.prefs.PreferencesUtil;
import plugin.ModelPlugin;
import view.display.InteractiveDisplayView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

/**
 * SourceDesigner generates a user-defined RealRandomAccessible
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/5/13
 */
public class SourceDesigner extends AbstractDesigner {

    Timer timer;

    private final Preferences prefs;

    public SourceDesigner()
    {
        super("SourceDesigner");

//        buttons.put("Inject Source", new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                process();
//            }
//        });

        buttons.put("Start", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                start();
            }
        });

        buttons.put("Stop", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stop();
            }
        });

        initializeComponents();
        prefs = PreferencesUtil.userNodeForPackage(getClass());

        PreferencesUtil.installFramePrefsHandler(prefs, "sourceDesigner", this);
        Point loc = this.getLocation();
        this.setLocation(loc);
    }

    private void start()
    {
        if(plugin != null)
        {
            final ModelPlugin pluginModel = (ModelPlugin)plugin;

            timer = new Timer(100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pluginModel.animate();
                    model.getDisplayView().updateRequest();
                }
            });

            timer.start();
        }
    }

    private void stop()
    {
        if(timer != null)
        {
            timer.stop();
        }
    }

    public void inject()
    {
        if(plugin != null)
        {
            ModelPlugin pluginModel = (ModelPlugin)plugin;
            RealRandomAccessible random = pluginModel.getSource();
            System.out.println(random.toString());

            InteractiveDisplayView view = model.getDisplayView();

            view.updateRealConverter(pluginModel.getConverter());
            view.updateRealRandomSource(pluginModel.getSource());
        }
//        else
//        {
//            System.out.println("Compile it first!");
//        }
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
