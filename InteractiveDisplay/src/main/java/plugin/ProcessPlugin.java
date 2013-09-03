package plugin;

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
}
