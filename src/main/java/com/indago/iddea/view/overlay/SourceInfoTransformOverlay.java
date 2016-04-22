package com.indago.iddea.view.overlay;

import java.awt.*;
import java.util.*;

public class SourceInfoTransformOverlay extends TransformOverlay {
    protected String sourceName = "Mandelbrot Example";

    protected String timepointString = new Date().toString();

    /**
     * Update data to show in the overlay.
     */
    public synchronized void updateInfo(final String source, final String time )
    {
        sourceName = source;
        timepointString = time;
    }

    public void drawTransformedOverlays(Graphics g)
    {
        Color c = g.getColor();
        g.setColor(Color.white);
        g.setFont( new Font( "Monospaced", Font.PLAIN, 5 ) );
        g.drawString( sourceName, 1, 100 );
        g.drawString( timepointString, 180, 100 );
        g.setColor(c);
    }
}