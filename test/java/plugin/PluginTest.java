package plugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.text.BadLocationException;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * very basic unit tests for the {@link Plugin}.
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/3/13
 */
public class PluginTest extends TestCase {
    /**
     * Asserts that two strings are equal, ignoring case.
     *
     * @param expected
     * @param actual
     */
    private void assertEqualsIgnoreCase(String expected, String actual) {
        expected = expected != null ? expected.toLowerCase() : null;
        actual = actual != null ? actual.toLowerCase() : null;
        assertEquals(expected, actual);
    }

    protected void setUp()
    {

    }

    @Test
    public void testRuntimeCompilation()
    {
        assertEquals(1, 1);
    }
}
