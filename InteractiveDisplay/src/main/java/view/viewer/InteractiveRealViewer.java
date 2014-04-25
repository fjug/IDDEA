package view.viewer;

import javax.swing.JComponent;

import net.imglib2.RandomAccessible;
import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.ui.*;
import net.imglib2.view.Views;
import view.display.JHotDrawInteractiveDisplay2D;
import net.imglib2.concatenate.Concatenable;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineSet;
import net.imglib2.ui.overlay.BufferedImageOverlayRenderer;

import java.awt.*;

/**
 * TODO
 *
 * Simple interactive viewer window. It creates a JFrame with the given
 * {@link InteractiveDisplayCanvas canvas}, and sets up transformation handling
 * and painting of a given {@link RenderSource source}.
 * <p>
 * It implements {@link PainterThread.Paintable} to handle {@link #paint()
 * repainting} through a {@link PainterThread}. It implements
 * {@link TransformListener} to be notified about viewer transformation changes
 * made by the user.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 *
 * @param <T>
 *            pixel type
 * @param <A>
 *            transform type
 * @param <C>
 *            canvas component type
 */
public abstract class InteractiveRealViewer< T, A extends AffineSet & AffineGet & Concatenable< AffineGet >, C extends JComponent & InteractiveDisplayCanvas< A > > implements TransformListener< A >, PainterThread.Paintable {

    final protected AffineTransformType< A > transformType;

    /**
     * Transformation set by the interactive viewer.
     */
    protected A viewerTransform;

    /**
     * Canvas used for displaying the rendered.
     */
    final protected C display;

    /**
     * Thread that triggers repainting of the display.
     */
    final protected PainterThread painterThread;

    /**
     * Underlying source for painting/accessing the data
     */
    protected RealRandomAccessible< T > source;

    /**
     * Underlying intervalSource for accessing the data
     */
    protected RandomAccessible< T > intervalSource;

    protected BufferedImageOverlayRenderer target;

    protected Renderer< A > imageRenderer;

    /**
     * TODO
     *
     * Create an interactive viewer window displaying a given
     * {@link RenderSource <code>source</code>} in the given
     * <code>interactiveDisplayCanvas</code>.
     * <p>
     * A {@link Renderer} is created that paints to a
     * {@link BufferedImageOverlayRenderer} render target which is displayed on
     * the canvas as an {@link OverlayRenderer}. A {@link PainterThread} is
     * created which queues repainting requests from the renderer and
     * interactive canvas, and triggers {@link #paint() repainting} of the
     * viewer.
     *
     * @param transformType
     * @param interactiveDisplayCanvas
     *            the canvas {@link JComponent} which will show the rendered
     *            images.
     * @param rendererFactory
     *            is used to create a {@link Renderer} for the source.
     */
    public InteractiveRealViewer( final AffineTransformType< A > transformType, final C interactiveDisplayCanvas, final RendererFactory< A > rendererFactory ) {
        this.transformType = transformType;
        painterThread = new PainterThread( this );
        viewerTransform = transformType.createTransform();
        display = interactiveDisplayCanvas;
        display.addTransformListener( this );

        target = new BufferedImageOverlayRenderer();
        imageRenderer = rendererFactory.create( target, painterThread );
        display.addOverlayRenderer( target );

//        display.addOverlayRenderer(new SourceInfoOverlay());

//		final GraphicsConfiguration gc = GuiUtil.getSuitableGraphicsConfiguration( GuiUtil.ARGB_COLOR_MODEL );
//		final GraphicsConfiguration gc = GuiUtil.getSuitableGraphicsConfiguration( GuiUtil.RGB_COLOR_MODEL );

        target.setCanvasSize( display.getWidth(), display.getHeight() );

        painterThread.start();
    }

    protected void updateRenderConverter( Converter< ? super T, ARGBType > converter ) {
        ( ( InjectableMultiResolutionRenderer ) imageRenderer ).injectConverter( converter );
    }

    synchronized protected void updateRenderSource( RealRandomAccessible< T > source ) {
        this.source = source;
        ( ( InjectableMultiResolutionRenderer ) imageRenderer ).injectSource( source );

        imageRenderer.paint( viewerTransform );

        display.repaint();
    }

    synchronized protected void updateSourceTransform( A transform ) {
        ( ( InjectableMultiResolutionRenderer ) imageRenderer ).injectSourceTransform( transform );
    }

    public void updateSource( RealRandomAccessible< T > source ) {
        this.updateRenderSource( source );
    }

    synchronized public void updateTransform( A transfrm )
    {
        this.updateSourceTransform(transfrm);
    }

    public void updateIntervalSource( RandomAccessible< T > interval ) {
        this.intervalSource = interval;
        RealRandomAccessible source = Views.interpolate( interval, new NearestNeighborInterpolatorFactory() );
        this.updateRenderSource( source );
    }

    public void updateConverter( Converter< ? super T, ARGBType > converter ) {
        this.updateRenderConverter( converter );
    }

    /**
     * Render the source using the current viewer transformation and
     */
    @Override
    public void paint() {
        imageRenderer.paint( viewerTransform );
        display.repaint();
    }

    @Override
    public void transformChanged( final A transform ) {
        transformType.set( viewerTransform, transform );
        requestRepaint();
    }

    /**
     * Get the canvas component used for painting
     *
     * @return the canvas component used for painting.
     */
    public C getDisplayCanvas() {
        return display;
    }

    /**
     * Request a repaint of the display.
     * Calls {@link Renderer#requestRepaint()}.
     */
    public void requestRepaint() {
        imageRenderer.requestRepaint();
    }

    /**
     * It returns JHotDrawInteractiveDisplay2D instance.
     *
     * @return JHotDrawInteractiveDisplay2D
     */
    public JHotDrawInteractiveDisplay2D getJHotDrawDisplay() {
        return ( JHotDrawInteractiveDisplay2D ) display;
    }

    /**
     * Get the underlying RealRandomAccessible source.
     *
     * @return RealRandomAccessible<T>
     */
    public RealRandomAccessible< T > getSource() {
        return source;
    }

    /**
     * Get the underlying RandomAccessible source.
     *
     * @return RandomAccessible<T>
     */
    public RandomAccessible< T > getIntervalSource() {
        if ( intervalSource == null )
            throw new NullPointerException( "IntervalSource does not exist." );

        return intervalSource;
    }

    /**
     * Check the underlying RandomAccessible source available.
     *
     * @return boolean
     */
    public boolean isIntervalSourceAvailable() {
        if ( intervalSource == null )
            return false;
        else
            return true;
    }

    /**
     * @return the viewerTransform
     */
    public A getViewerTransform() {
        return viewerTransform;
    }
}
