import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.jhotdraw.draw.Figure;
import plugin.ProcessPlugin;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;
import net.imglib2.ui.OverlayRenderer;

import java.awt.*;
import java.util.*;

/**
 * The sample code for calculating meanintensity values for whole picture and selected regions
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/3/13
 */
public class MeanIntensityProcess extends ProcessPlugin {

    public MeanIntensityProcess()
    {
        // Overlay painters are added here
        painters.add(new SourceInfoOverlay());
    }
    
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

    @Override
    public void process(RandomAccessibleInterval source)
    {
        if(ARGBType.class.isInstance(Util.getTypeFromRandomAccess(source)))
        {
            Cursor cur = Views.iterable(source).localizingCursor();

            long size = 0;

            double r = 0;
            long rCnt = 0;
            double g = 0;
            long gCnt = 0;
            double b = 0;
            long bCnt = 0;

            while(cur.hasNext())
            {
                cur.fwd();

                {
                    int pixel = ((ARGBType) cur.get()).get();
                    size++;
                    rCnt += (ARGBType.red(pixel) > 0)? 1 : 0;
                    r += ARGBType.red(pixel);
                    gCnt += (ARGBType.green(pixel) > 0)? 1 : 0;
                    g += ARGBType.green(pixel);
                    bCnt += (ARGBType.blue(pixel) > 0)? 1 : 0;
                    b += ARGBType.blue(pixel);
                }
            }

            System.out.println("ARGBType");
            System.out.println("Pixels: " + size);
            System.out.println("MeanIntensity(R) of selected regions: " + r / rCnt);
            System.out.println("MeanIntensity(G) of selected regions: " + g / gCnt);
            System.out.println("MeanIntensity(B) of selected regions: " + b / bCnt);
        }
    }

    @Override
    public void process(RandomAccessibleInterval source, Set<Figure> figures)
    {
        if(ARGBType.class.isInstance(Util.getTypeFromRandomAccess(source)))
        {
            long size = 0;

            double r = 0;
            long rCnt = 0;
            double g = 0;
            long gCnt = 0;
            double b = 0;
            long bCnt = 0;


            for(Figure f: figures)
            {
                Rectangle2D.Double rec = f.getBounds();
                
                RandomAccessibleInterval< ARGBType > viewSource = (RandomAccessibleInterval< ARGBType >) Views.offsetInterval(
                        source,
                        new long[]{ (long)rec.getX(), (long)rec.getY() },
                        new long[]{ (long)rec.getWidth(), (long)rec.getHeight() }
                );

                Cursor<ARGBType> cur = Views.iterable(viewSource).localizingCursor();

                while(cur.hasNext())
                {
                    cur.fwd();

                    Point2D.Double point = new Point2D.Double(cur.getDoublePosition(0) + rec.getX(), cur.getDoublePosition(1) + rec.getY());
                    if(f.contains(point))
                    {
                        int pixel = ((ARGBType) cur.get()).get();
                        size++;
                        rCnt += (ARGBType.red(pixel) > 0)? 1 : 0;
                        r += ARGBType.red(pixel);
                        gCnt += (ARGBType.green(pixel) > 0)? 1 : 0;
                        g += ARGBType.green(pixel);
                        bCnt += (ARGBType.blue(pixel) > 0)? 1 : 0;
                        b += ARGBType.blue(pixel);
                    }
                }
            }

            System.out.println("ARGBType");
            System.out.println("Pixels: " + size);
            System.out.println("MeanIntensity(R) of selected regions: " + r / rCnt);
            System.out.println("MeanIntensity(G) of selected regions: " + g / gCnt);
            System.out.println("MeanIntensity(B) of selected regions: " + b / bCnt);
        }
    }

    public class SourceInfoOverlay implements OverlayRenderer {
	    protected String sourceName = "Mandelbrot Process Example";
	
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
	        g.setFont( new Font( "Monospaced", Font.PLAIN, 12 ) );
	        g.drawString( sourceName, ( int ) g.getClipBounds().getWidth() / 2 - 100, 12 );
	        g.drawString( timepointString, ( int ) g.getClipBounds().getWidth() - 240, 12 );
	        g.setColor(c);
	    }
	
	    @Override
	    public void setCanvasSize(int width, int height) {
	        // Change canvas size
	    }
    }
}
