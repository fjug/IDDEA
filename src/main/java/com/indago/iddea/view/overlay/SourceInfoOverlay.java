package com.indago.iddea.view.overlay;

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
public class SourceInfoOverlay implements OverlayRenderer {
    protected String sourceName = "Mandelbrot Example";

    protected String timepointString = new Date().toString();

    protected AffineTransform preTransform;

    /**
     * Update data to show in the overlay.
     */
    public synchronized void updateInfo( AffineTransform transform, final String source, final String time )
    {
        preTransform = transform;
        sourceName = source;
        timepointString = time;
    }

    @Override
    public void drawOverlays(Graphics gr) {

        Graphics2D g = (Graphics2D) gr.create();

        AffineTransform tx = g.getTransform();
        tx.concatenate(preTransform);
        g.setTransform(tx);

        Color c = g.getColor();
        g.setColor(Color.white);
        g.setFont( new Font( "Monospaced", Font.PLAIN, 12 ) );
        g.drawString( sourceName, ( int ) g.getClipBounds().getWidth() / 2 - 100, 12 );
        g.drawString( timepointString, ( int ) g.getClipBounds().getWidth() - 240, 12 );
        g.setColor(c);

        g.dispose();
    }

    @Override
    public void setCanvasSize(int width, int height) {
        // Change canvas size
    }
}
