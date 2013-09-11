package plugin;

import net.imglib2.RealRandomAccessible;
import net.imglib2.ui.OverlayRenderer;

import java.util.LinkedList;

/**
 * Abstract ModelPlugin for creating RandomAccessibleSource
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/3/13
 */
public abstract class ModelPlugin<T> implements IPlugin {

    public abstract RealRandomAccessible<T> getSource();
    protected LinkedList<OverlayRenderer> painters = new LinkedList<OverlayRenderer>();

    @Override
    public PluginType getPluginType() {
        return PluginType.ModelPlugin;
    }

    public abstract void animate();

    public LinkedList<OverlayRenderer> getPainters()
    {
        return painters;
    }
}
