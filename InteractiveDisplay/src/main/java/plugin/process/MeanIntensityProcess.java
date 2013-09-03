package plugin.process;

import plugin.ProcessPlugin;

/**
 * Created with IntelliJ IDEA.
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/3/13
 */
public class MeanIntensityProcess extends ProcessPlugin {
    @Override
    public String getName() {
        return "MeanIntensityProcess";
    }

    @Override
    public String getAuthor() {
        return "HongKee Moon";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
}
