package render;

import net.imglib2.RandomAccessible;
import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.interpolation.randomaccess.LanczosInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.Views;

/**
 * Created with IntelliJ IDEA.
 *
 * @version 0.1beta
 * @since 8/15/13 2:27 PM
 * @author HongKee Moon
 */
public class ImageSource< T extends NumericType<T>, A >
{
    protected final RandomAccessible< T > source;

    protected final A sourceTransform;

    protected final Converter< ? super T, ARGBType > converter;

    protected NLinearInterpolatorFactory< T > factory;

    public ImageSource( final RandomAccessible< T > source, final A sourceTransform, final Converter< ? super T, ARGBType > converter )
    {
        this.source = source;
        this.sourceTransform = sourceTransform;
        this.converter = converter;
        this.factory = new NLinearInterpolatorFactory<T>();
    }
    /**
     * Get the image, extended to infinity and interpolated.
     *
     * @return the extended and interpolated {@link net.imglib2.RandomAccessible image}.
     */
    public RealRandomAccessible< T > getInterpolatedSource()
    {
        return Views.interpolate(source, factory);
    }

    /**
     * Get the transform from the {@link #getSource(long) source}
     * into the global coordinate system.
     *
     * @return transforms source into the global coordinate system.
     */
    public A getSourceTransform()
    {
        return sourceTransform;
    }

    /**
     * Get the {@link net.imglib2.converter.Converter} (converts {@link #source} type T to ARGBType
     * for display).
     */
    public Converter< ? super T, ARGBType > getConverter()
    {
        return converter;
    }
}
