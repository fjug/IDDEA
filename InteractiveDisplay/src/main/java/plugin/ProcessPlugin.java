package plugin;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import org.jhotdraw.draw.Figure;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/3/13
 */
public abstract class ProcessPlugin implements IPlugin {

    @Override
    public PluginType getPluginType() {
        return PluginType.ProcessPlugin;
    }

    public abstract void process(RandomAccessibleInterval source);

    public abstract void process(RandomAccessibleInterval source, Set<Figure> figures);
}
