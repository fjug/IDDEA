package view.overlay;

import java.awt.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by moon on 14/04/14.
 */
public class ObjectInfoTransformOverlay extends TransformOverlay {
    protected String label = "Mandelbrot Example";

    protected int x, y;

    protected ArrayList<ObjectInfo> objectList;

    final Color labelColor = Color.white;
    final Font labelFont = new Font( "Monospaced", Font.PLAIN, 3 );

    /**
     * Update data to show in the overlay.
     */
    public synchronized void updateInfo(final String label, final int x, final int y )
    {
        this.label = label;
        this.x = x;
        this.y = y;
    }

    public void setObjectList(ArrayList<ObjectInfo> list)
    {
        objectList = list;
    }

    public void drawTransformedOverlays(Graphics g)
    {
        Color c = g.getColor();
        g.setColor( labelColor );
        g.setFont( labelFont );

        if(objectList != null)
        {
            for(ObjectInfo oi : objectList)
            {
                g.drawString( oi.Label, oi.X, oi.Y );
                g.drawString( "" + oi.X + ", " + oi.Y, oi.X, oi.Y + 4);
            }
        }
        g.setColor(c);
    }
}