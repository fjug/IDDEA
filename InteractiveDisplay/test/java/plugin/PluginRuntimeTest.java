package plugin;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/3/13
 */
public class PluginRuntimeTest extends TestCase {

    @Test
    public void testCall() throws Exception {
        PluginRuntime time = new PluginRuntime();
        assertTrue(time.call());
    }
}
