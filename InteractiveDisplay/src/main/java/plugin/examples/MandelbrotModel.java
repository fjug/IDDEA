package plugin.examples;

import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.type.numeric.integer.LongType;
import plugin.ModelPlugin;

import net.imglib2.ui.OverlayRenderer;

import java.awt.*;
import java.util.*;

/**
 * Sample Mandelbrot model plugin for IDDEA
 * You can copy and paste it in SourceDesigner
 * Process -> Inject Source
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/3/13
 */
public class MandelbrotModel extends ModelPlugin<LongType> {

    MandelbrotRealRandomAccessible source = new MandelbrotRealRandomAccessible();

    public MandelbrotModel()
    {
        // Overlay painters are added here
        painters.add(new SourceInfoOverlay());
    }

    boolean go = true;

    @Override
    public String getName() {
        return "MandelbrotModel";
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
    public RealRandomAccessible<LongType> getSource() {
        return source;
    }

    @Override
    public void animate()
    {
        double value = source.getRealCurve();

        if(go)
        {
            if(value >= 1d) go = false;
        }
        else
        {
            if(value <= 0d) go = true;
        }

        if(go) source.setRealCurve(value + 0.01d);
        else source.setRealCurve(value - 0.01d);
    }

    public class SourceInfoOverlay implements OverlayRenderer {
        protected String sourceName = "Mandelbrot Source Example";

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

    // Inner class MandelbrotRealRandomAccessible
    class MandelbrotRealRandomAccessible implements RealRandomAccessible< LongType >
    {
        long maxIterations;
        double realCurve = 0d;

        public void setRealCurve(double a)
        {
            if(a > 1d) realCurve = 1d;
            else if(a < 0d) realCurve = 0d;
            else realCurve = a;
        }

        public double getRealCurve()
        {
            return realCurve;
        }

        public MandelbrotRealRandomAccessible()
        {
            maxIterations = 50;
        }

        public MandelbrotRealRandomAccessible( final long maxIterations )
        {
            this.maxIterations = maxIterations;
        }

        public void setMaxIterations( final long maxIterations )
        {
            this.maxIterations = maxIterations;
        }

        final private long mandelbrot( final double re0, final double im0, final long maxIterations )
        {
            double re = re0;
            double im = im0;
            long i = 0;
            for ( ; i < maxIterations; ++i )
            {
                final double squre = re * (re - re * realCurve) ;
                final double squim = im * im;
                if ( squre + squim > 4 )
                    break;
                im = 2 * re * im + im0;
                re = squre - squim  + re0;
            }
            return i;
        }

        public class MandelbrotRealRandomAccess extends RealPoint implements RealRandomAccess< LongType >
        {
            final protected LongType t;
            public MandelbrotRealRandomAccess()
            {
                super( 2 ); // number of dimensions is 2
                t = new LongType();
            }

            @Override
            public LongType get()
            {
                t.set( mandelbrot( position[ 0 ], position[ 1 ], maxIterations ) );
                return t;
            }

            @Override
            public MandelbrotRealRandomAccess copyRealRandomAccess()
            {
                return copy();
            }

            @Override
            public MandelbrotRealRandomAccess copy()
            {
                final MandelbrotRealRandomAccess a = new MandelbrotRealRandomAccess();
                a.setPosition( this );
                return a;
            }
        }

        @Override
        public int numDimensions()
        {
            return 2;
        }

        @Override
        public MandelbrotRealRandomAccess realRandomAccess()
        {
            return new MandelbrotRealRandomAccess();
        }

        @Override
        public MandelbrotRealRandomAccess realRandomAccess( final RealInterval interval )
        {
            return realRandomAccess();
        }
    }
}

