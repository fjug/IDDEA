package plugin;

import net.imglib2.ui.OverlayRenderer;

import java.util.LinkedList;

/**
 * Interface of plugin
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/3/13
 */
public interface IPlugin {
    String getName();
    String getAuthor();
    String getVersion();
    PluginType getPluginType();
    LinkedList<OverlayRenderer> getPainters();
}

