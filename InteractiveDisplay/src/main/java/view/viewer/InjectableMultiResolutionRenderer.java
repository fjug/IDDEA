package view.viewer;

import net.imglib2.RandomAccessible;
import net.imglib2.RealRandomAccessible;
import net.imglib2.concatenate.Concatenable;
import net.imglib2.converter.Converter;
import net.imglib2.display.screenimage.awt.ARGBScreenImage;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineSet;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.ui.*;

/**
 * An {@link AbstractMultiResolutionRenderer} for a single {@link RenderSource}.
 *
 * @param <A>
 *            transform type
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 * @author Stephan Saalfeld
 * @author HongKee Moon
 */
public class InjectableMultiResolutionRenderer< A extends AffineSet & AffineGet & Concatenable< AffineGet > > extends AbstractMultiResolutionRenderer< A >
{
    /**
     * Factory for creating {@link MultiResolutionRenderer}.
     */
    public static class Factory< A extends AffineSet & AffineGet & Concatenable< AffineGet > > implements RendererFactory< A >
    {
        final protected AffineTransformType< A > transformType;

        final protected RenderSource< ?, A > source;

        final protected double[] screenScales;

        final protected long targetRenderNanos;

        final protected boolean doubleBuffered;

        final protected int numRenderingThreads;

        /**
         * Create a factory for {@link MultiResolutionRenderer
         * MultiResolutionRenderer} of the given source, with the specified
         * multi-resolution, multi-threading, and double-buffering properties.
         *
         * @param transformType
         *            which transformation type (e.g.
         *            {@link AffineTransformType2D affine 2d} or
         *            {@link AffineTransformType3D affine 3d}) is used for the
         *            source and viewer transforms.
         * @param source
         *            source data to be rendered.
         * @param screenScales
         *            Scale factors from the viewer canvas to screen images of
         *            different resolutions. A scale factor of 1 means 1 pixel
         *            in the screen image is displayed as 1 pixel on the canvas,
         *            a scale factor of 0.5 means 1 pixel in the screen image is
         *            displayed as 2 pixel on the canvas, etc. The screen scales
         *            are assumed to be ordered finer-to-coarse, with index 0
         *            corresponding to the full resolution usually.
         * @param targetRenderNanos
         *            Target rendering time in nanoseconds. The rendering time
         *            for the coarsest rendered scale should be below this
         *            threshold.
         * @param doubleBuffered
         *            Whether to use double buffered rendering.
         * @param numRenderingThreads
         *            How many threads to use for rendering.
         */
        public Factory(
                final AffineTransformType< A > transformType,
                final RenderSource< ?, A > source,
                final double[] screenScales,
                final long targetRenderNanos,
                final boolean doubleBuffered,
                final int numRenderingThreads )
        {
            this.transformType = transformType;
            this.source = source;
            this.screenScales = screenScales;
            this.targetRenderNanos = targetRenderNanos;
            this.doubleBuffered = doubleBuffered;
            this.numRenderingThreads = numRenderingThreads;
        }

        @Override
        public AbstractRenderer< A > create( final RenderTarget display, final PainterThread painterThread )
        {
            return new InjectableMultiResolutionRenderer< A >( transformType, source, display, painterThread, screenScales, targetRenderNanos, doubleBuffered, numRenderingThreads );
        }
    }

    /**
     * source data to be rendered.
     */
    protected RenderSource< ?, A > source;

    /**
     * @param transformType
     *            which transformation type (e.g. {@link AffineTransformType2D
     *            affine 2d} or {@link AffineTransformType3D affine 3d}) is used
     *            for the source and viewer transforms.
     * @param source
     *            source data to be rendered.
     * @param display
     *            The canvas that will display the images we render.
     * @param painterThread
     *            Thread that triggers repainting of the display. Requests for
     *            repainting are send there.
     * @param screenScales
     *            Scale factors from the viewer canvas to screen images of
     *            different resolutions. A scale factor of 1 means 1 pixel in
     *            the screen image is displayed as 1 pixel on the canvas, a
     *            scale factor of 0.5 means 1 pixel in the screen image is
     *            displayed as 2 pixel on the canvas, etc. The screen scales are
     *            assumed to be ordered finer-to-coarse, with index 0
     *            corresponding to the full resolution usually.
     * @param targetRenderNanos
     *            Target rendering time in nanoseconds. The rendering time for
     *            the coarsest rendered scale should be below this threshold.
     * @param doubleBuffered
     *            Whether to use double buffered rendering.
     * @param numRenderingThreads
     *            How many threads to use for rendering.
     */
    public InjectableMultiResolutionRenderer(
            final AffineTransformType< A > transformType,
            final RenderSource< ?, A > source,
            final RenderTarget display,
            final PainterThread painterThread,
            final double[] screenScales,
            final long targetRenderNanos,
            final boolean doubleBuffered,
            final int numRenderingThreads )
    {
        super( transformType, display, painterThread, screenScales, targetRenderNanos, doubleBuffered, numRenderingThreads );
        this.source = source;
    }

    @Override
    protected SimpleInterruptibleProjector< ?, ARGBType > createProjector( final A viewerTransform, final A screenScaleTransform, final ARGBScreenImage target )
    {
        return createProjector( transformType, source, viewerTransform, screenScaleTransform, target, numRenderingThreads );
    }

    protected static < T, A extends AffineGet & Concatenable< AffineGet > > SimpleInterruptibleProjector< T, ARGBType > createProjector(
            final AffineTransformType< A > transformType,
            final RenderSource< T, A > source,
            final A viewerTransform,
            final A screenScaleTransform,
            final ARGBScreenImage screenImage,
            final int numRenderingThreads )
    {
        return new SimpleInterruptibleProjector< T, ARGBType >( getTransformedSource( transformType, source, viewerTransform, screenScaleTransform ), source.getConverter(), screenImage, numRenderingThreads );
    }

    protected static < T, A extends AffineGet & Concatenable< AffineGet > > RandomAccessible< T > getTransformedSource(
            final AffineTransformType< A > transformType,
            final RenderSource< T, A > source,
            final A viewerTransform,
            final A screenScaleTransform )
    {
        final RealRandomAccessible< T > img = source.getInterpolatedSource();

        final A sourceToScreen = transformType.createTransform();
        transformType.set( sourceToScreen, screenScaleTransform );
        sourceToScreen.concatenate( viewerTransform );
        sourceToScreen.concatenate( source.getSourceTransform() );

        return RealViews.constantAffine( img, sourceToScreen );
    }

    public void injectSource(final RealRandomAccessible source)
    {
        ((Injectable)this.source).injectSource(source);
    }

    public void injectConverter(final Converter converter)
    {
        ((Injectable)this.source).injectConverter(converter);
    }
}