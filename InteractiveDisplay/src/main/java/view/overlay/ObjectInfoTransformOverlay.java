package view.overlay;

import java.awt.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by moon on 14/04/14.
 */
public class ObjectInfoTransformOverlay extends TransformOverlay {
    protected int x, y;

    protected ArrayList<ObjectInfo> objectList;

    final Color labelColor = Color.white;
    final Color highlightColor = Color.yellow;
    final Font labelFont = new Font( "Monospaced", Font.PLAIN, 3 );

    /**
     * Update data to show in the overlay.
     */
    public synchronized void updateXY(final int x, final int y )
    {
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
                if(oi.X == x && oi.Y == y)
                {
                    g.setColor( highlightColor );
                    g.drawString( oi.Label, oi.X, oi.Y );
                    g.drawString( "" + oi.X + ", " + oi.Y, oi.X, oi.Y + 4);
                    g.setColor( labelColor );
                }
                else
                {
                    g.drawString( oi.Label, oi.X, oi.Y );
                    g.drawString( "" + oi.X + ", " + oi.Y, oi.X, oi.Y + 4);
                }
            }
        }
        g.setColor(c);
    }
}