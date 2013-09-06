package plugin.desginer;

import net.imglib2.RealRandomAccessible;
import plugin.ModelPlugin;
import view.display.InteractiveDisplayView;

import javax.swing.*;

/**
 * SourceDesigner generates a user-defined RealRandomAccessible
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/5/13
 */
public class SourceDesigner extends AbstractDesigner {

    public SourceDesigner()
    {
        super("SourceDesigner", "Inject Source");
    }

    public void process()
    {
        if(plugin != null)
        {
            ModelPlugin pluginModel = (ModelPlugin)plugin;
            RealRandomAccessible random = pluginModel.getSource();
            System.out.println(random.toString());

            InteractiveDisplayView view = model.getDisplayView();
            view.updateRealRandomSource(pluginModel.getSource());
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
