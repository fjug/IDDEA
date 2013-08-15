package view;

import ij.process.ColorProcessor;
import net.imglib2.*;
import net.imglib2.img.imageplus.ImagePlusImg;
import net.imglib2.sampler.special.OrthoSliceCursor;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;

/**
 * Created with IntelliJ IDEA.
 *
 * @version 0.1beta
 * @since 8/14/13 5:18 PM
 * @author HongKee Moon
 */
public class ImageRandomAccessible implements RandomAccessible< ARGBType >
{
    final protected ARGBType t;
    final RandomAccessible< ARGBType > source;

    public ImageRandomAccessible(ImagePlusImg<ARGBType, ?> source)
    {
        this.source = source;
        t = new ARGBType();
    }

    final private ARGBType getPixel( long[] position )
    {
        RandomAccess<ARGBType> acc = source.randomAccess();
        acc.setPosition(position);


        return acc.get();
    }

    public class ImageRandomAccess extends Point implements RandomAccess< ARGBType >
    {
        public ImageRandomAccess()
        {
            super( 2 ); // number of dimensions is 2
        }

        @Override
        public ARGBType get()
        {
            try{
                t.set( getPixel(position));
            }
            catch (ArrayIndexOutOfBoundsException ex)
            {
                t.set(0);
            }
            return t;
        }

        @Override
        public ImageRandomAccess copyRandomAccess()
        {
            return copy();
        }

        @Override
        public ImageRandomAccess copy()
        {
            final ImageRandomAccess a = new ImageRandomAccess();
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
    public ImageRandomAccess randomAccess()
    {
        return new ImageRandomAccess();
    }

    @Override
    public ImageRandomAccess randomAccess( final Interval interval )
    {
        return randomAccess();
    }
}
