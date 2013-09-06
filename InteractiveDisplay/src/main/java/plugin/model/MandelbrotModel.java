package plugin.model;

import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.type.numeric.integer.LongType;
import plugin.ModelPlugin;

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

    public MandelbrotModel()
    {

    }

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
        return new MandelbrotRealRandomAccessible();
    }

    // Inner class MandelbrotRealRandomAccessible
    class MandelbrotRealRandomAccessible implements RealRandomAccessible< LongType >
    {
        final protected LongType t;
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
            t = new LongType();
            maxIterations = 50;
        }

        public MandelbrotRealRandomAccessible( final long maxIterations )
        {
            t = new LongType();
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
            public MandelbrotRealRandomAccess()
            {
                super( 2 ); // number of dimensions is 2
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
