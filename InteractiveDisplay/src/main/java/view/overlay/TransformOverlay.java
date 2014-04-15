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
public abstract class TransformOverlay implements OverlayRenderer {
    protected AffineTransform preTransform;

    public abstract void drawTransformedOverlays(Graphics g);

    public AffineTransform getPreTransform()
    {
        return preTransform;
    }

    public void setupTransform(AffineTransform transform)
    {
        preTransform = transform;
    }

    @Override
    public void drawOverlays(Graphics gr) {

        Graphics2D g = (Graphics2D) gr.create();

        if(preTransform != null) {
            AffineTransform tx = g.getTransform();
            tx.concatenate(preTransform);
            g.setTransform(tx);
        }

        drawTransformedOverlays(g);

        g.dispose();
    }

    @Override
    public void setCanvasSize(int width, int height) {
        // Change canvas size
    }
}
