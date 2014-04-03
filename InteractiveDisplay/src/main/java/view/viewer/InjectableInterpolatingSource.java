package view.viewer;

/**
 * Created with IntelliJ IDEA.
 *
 * @version 0.1beta
 * @since 4/3/14 12:03 PM
 * @author HongKee Moon
 */

import net.imglib2.RandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.ui.RenderSource;
import net.imglib2.view.Views;

/**
 * A {@link RenderSource}, that provides an interpolated source
 * {@link RandomAccessible} and is able to support nearest-neighbor
 *
 * The (discrete) source {@link RandomAccessible}, transform, and
 * {@link Converter} provided in the constructor.
 *
 * @param <T>
 *            pixel type
 * @param <A>
 *            transform type
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 *
 * @author HongKee
 */
public class InjectableInterpolatingSource< T extends NumericType< T >, A > extends InjectableSource< T, A >
{
    protected RandomAccessible< T > intervalSource;

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public InjectableInterpolatingSource( final RandomAccessible< T > source, final A sourceTransform, final Converter< ? super T, ARGBType > converter )
    {
        super(Views.interpolate( source, new NearestNeighborInterpolatorFactory< T >() ), sourceTransform, converter);

        this.intervalSource = source;
    }

    public void injectIntervalSource(RandomAccessible<T> source)
    {
        this.intervalSource = source;

        injectSource(Views.interpolate( source, new NLinearInterpolatorFactory< T >() ));
    }

    public RandomAccessible< T > getIntervalSource()
    {
        if(intervalSource == null)
            throw new NullPointerException("IntervalSource is null");

        return intervalSource;
    }
}
