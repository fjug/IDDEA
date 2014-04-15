package view.overlay;

import net.imglib2.ui.OverlayRenderer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.*;



/**
 * Created with IntelliJ IDEA.
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/10/13
 */
public class ObjectInfoOverlay implements OverlayRenderer {
    protected String sourceName = "Object";

    protected String timepointString = new Date().toString();

    /**
     * Update data to show in the overlay.
     */
    public synchronized void updateInfo( final String source, final String time )
    {
        sourceName = source;
        timepointString = time;
    }

    @Override
    public void drawOverlays(Graphics g) {

        Color c = g.getColor();
        g.setColor(Color.white);
        g.setFont( new Font( "Monospaced", Font.BOLD, 12 ) );
        g.drawString( sourceName, ( int ) g.getClipBounds().getWidth() / 2 - 100, 12 );
        g.drawString( timepointString, ( int ) g.getClipBounds().getWidth() - 240, 12 );
        g.setColor(c);
    }

    @Override
    public void setCanvasSize(int width, int height) {
        // Change canvas size
    }
}
