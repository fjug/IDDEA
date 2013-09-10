package model.source;

import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.type.numeric.integer.LongType;

/**
 * A RealRandomAccess that procedurally generates values (iteration count)
 * for the mandelbrot set.
 *
 * @author Tobias Pietzsch
 */
public class MandelbrotRealRandomAccessible implements RealRandomAccessible< LongType >
{
    long maxIterations;
    static double realCurve = 0d;

    public void setRealCurve(double a)
    {
        if(a > 1d) realCurve = 1d;
        else if(a < 0d) realCurve = 0d;
        else realCurve = a;
        System.out.println("realCurve = " + realCurve);
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

    final static private long mandelbrot( final double re0, final double im0, final long maxIterations )
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
