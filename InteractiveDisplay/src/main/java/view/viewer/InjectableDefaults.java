package view.viewer;


import java.awt.Graphics;
import java.awt.image.BufferedImage;

import net.imglib2.concatenate.Concatenable;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineSet;
import net.imglib2.ui.AffineTransformType;
import net.imglib2.ui.MultiResolutionRenderer;
import net.imglib2.ui.RenderSource;
import net.imglib2.ui.RendererFactory;
import net.imglib2.ui.viewer.InteractiveRealViewer2D;
import net.imglib2.ui.viewer.InteractiveRealViewer3D;
import net.imglib2.ui.viewer.InteractiveViewer2D;
import net.imglib2.ui.viewer.InteractiveViewer3D;

/**
 * Default rendering settings used by the convenience viewer classes
 * {@link InteractiveViewer2D}, {@link InteractiveRealViewer2D},
 * {@link InteractiveViewer3D}, and {@link InteractiveRealViewer3D}.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 * @author HongKee Moon
 */
public class InjectableDefaults
{
    /**
     * Whether to discard the alpha components when drawing
     * {@link BufferedImage} to {@link Graphics}.
     */
    public static final boolean discardAlpha = true;

    /**
     * Whether to use double buffered rendering.
     */
    public static final boolean doubleBuffered = true;

    /**
     * How many threads to use for rendering.
     */
    public static final int numRenderingThreads = 3;

    /**
     * For the {@link MultiResolutionRenderer}: Scale factors from the viewer
     * canvas to screen images of different resolutions. A scale factor of 1
     * means 1 pixel in the screen image is displayed as 1 pixel on the canvas,
     * a scale factor of 0.5 means 1 pixel in the screen image is displayed as 2
     * pixel on the canvas, etc.
     */
    public static final double[] screenScales = new double[] { 1, 0.5, 0.25, 0.125 };

    /**
     * Target rendering time in nanoseconds. The rendering time for the coarsest
     * rendered scale in a {@link MultiResolutionRenderer} should be below this
     * threshold.
     */
    public static final long targetRenderNanos = 15 * 1000000;

    /**
     * Create a factory to construct the default {@link Renderer} type with
     * default settings for a single {@link RenderSource}.
     *
     * @param transformType
     * @param source
     *            the source data that will be rendered by the {@link Renderer}
     *            that is created by the returned factory.
     * @return a factory to construct the default {@link Renderer} for the given
     *         {@link RenderSource source data}.
     */
    public static < A extends AffineSet & AffineGet & Concatenable< AffineGet > > RendererFactory< A > rendererFactory( final AffineTransformType< A > transformType, final RenderSource< ?, A > source )
    {
        return new InjectableMultiResolutionRenderer.Factory< A >( transformType, source, screenScales, targetRenderNanos, doubleBuffered, numRenderingThreads );
    }
}