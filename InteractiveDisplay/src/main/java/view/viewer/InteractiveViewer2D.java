package view.viewer;

import net.imglib2.RandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;

/**
 * Simplified Interactive viewer for a 2D {@link RandomAccessible}.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 * @author HongKee Moon
 */
public class InteractiveViewer2D< T extends NumericType< T > > extends InteractiveRealViewer2D< T > {

    /**
     * Create an interactive viewer for a 2D {@link RandomAccessible}.
     *
     * @param width
     *            window width.
     * @param height
     *            window height.
     * @param source
     *            The source image to display. It is assumed that the source is
     *            extended to infinity.
     * @param sourceTransform
     *            Transformation from source to global coordinates. This is
     *            useful for pre-scaling when showing anisotropic data, for
     *            example.
     * @param converter
     *            Converter from the source type to argb for rendering the
     *            source.
     */
    public InteractiveViewer2D( final int width, final int height, final RandomAccessible< T > source, final AffineTransform2D sourceTransform, final Converter< ? super T, ARGBType > converter ) {
        this( width, height, new InjectableInterpolatingSource< T, AffineTransform2D >( source, sourceTransform, converter ) );

        this.intervalSource = source;
    }

    public InteractiveViewer2D( final int width, final int height, final RandomAccessible< T > source, final Converter< ? super T, ARGBType > converter ) {
        this( width, height, source, new AffineTransform2D(), converter );
    }

    public InteractiveViewer2D( final int width, final int height, final InjectableInterpolatingSource< T, AffineTransform2D > interpolatingSource ) {
        super( width, height, interpolatingSource.getInterpolatedSource(), interpolatingSource.getSourceTransform(), interpolatingSource.getConverter() );

        this.source = interpolatingSource.getInterpolatedSource();
    }
}
