package com.indago.iddea.view.component;
import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;

public class DummyRealRandomAccessible implements RealRandomAccessible<DoubleType>
{
    public DummyRealRandomAccessible()
    {
    }

    public class DummyRealRandomAccess extends RealPoint implements RealRandomAccess< DoubleType >
    {
        final protected DoubleType t;
        public DummyRealRandomAccess()
        {
            super( 2 ); // number of dimensions is 2
            t = new DoubleType();
        }

        @Override
        public DoubleType get()
        {
            t.set( 0 );
            return t;
        }

        @Override
        public DummyRealRandomAccess copyRealRandomAccess()
        {
            return copy();
        }

        @Override
        public DummyRealRandomAccess copy()
        {
            final DummyRealRandomAccess a = new DummyRealRandomAccess();
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
    public DummyRealRandomAccess realRandomAccess()
    {
        return new DummyRealRandomAccess();
    }

    @Override
    public DummyRealRandomAccess realRandomAccess( final RealInterval interval )
    {
        return realRandomAccess();
    }
}
