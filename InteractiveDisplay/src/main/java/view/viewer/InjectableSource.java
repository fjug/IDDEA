package view.viewer;

import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.ui.RenderSource;

/**
 * A injectable source implementation of {@link RenderSource}, with source
 * {@link RealRandomAccessible}, transform, and {@link Converter} provided in
 * the constructor.
 *
 * @param <T>
 *            pixel type
 * @param <A>
 *            transform type
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 * @author HongKee Moon
 */
public class InjectableSource< T, A > implements RenderSource< T, A >, Injectable<T>
{
    protected RealRandomAccessible< T > source;

    protected Converter< ? super T, ARGBType > converter;

    protected final A sourceTransform;

    /**
     * Create a {@link RenderSource}.
     *
     * @param source
     *            a source image, extending to infinity and interpolated if
     *            necessary.
     * @param sourceTransform
     *            The transformation from the source image coordinates into the
     *            global coordinate system.
     * @param converter
     *            A converter from the {@link #source} type T to
     *            {@link ARGBType}.
     */
    public InjectableSource( final RealRandomAccessible< T > source, final A sourceTransform, final Converter< ? super T, ARGBType > converter )
    {
        this.source = source;
        this.sourceTransform = sourceTransform;
        this.converter = converter;
    }

    @Override
    public RealRandomAccessible< T > getInterpolatedSource()
    {
        return source;
    }

    @Override
    public A getSourceTransform()
    {
        return sourceTransform;
    }

    @Override
    public Converter< ? super T, ARGBType > getConverter()
    {
        return converter;
    }

    @Override
    public void injectSource(RealRandomAccessible<T> source)
    {
        this.source = source;
    }

    @Override
    public void injectConverter(Converter< ? super T, ARGBType >  converter)
    {
        this.converter = converter;
    }
}