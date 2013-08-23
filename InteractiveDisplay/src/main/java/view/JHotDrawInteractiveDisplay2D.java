package view;

import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.ui.InteractiveDisplayCanvas;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformEventHandlerFactory;
import net.imglib2.ui.TransformListener;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * JHotDrawInteractiveDisplay2D gives 2D display view with the buffered image.
 * This class uses MultiResolutionRenderer for realtime scaling view.
 *
 * @version 0.1beta
 * @since 8/12/13 5:09 PM
 * @author TobiasPietzsch <tobias.pietzsch@gmail.com>
 * @author HongKee Moon
 */

public class JHotDrawInteractiveDisplay2D<T> extends InteractiveDrawingView implements InteractiveDisplayCanvas<T>
{
    /**
     * Mouse/Keyboard handler that manipulates the view transformation.
     */
    protected TransformEventHandler< T > handler;

    /**
     * Listeners that we have to notify about view transformation changes.
     */
    final protected CopyOnWriteArrayList< TransformListener< T > > transformListeners;

    /**
     * The {@link OverlayRenderer} that draws on top of the current {@link #bufferedImage}.
     */
    final protected CopyOnWriteArrayList<OverlayRenderer> overlayRenderers;

    /**
     * The {@link AffineTransform} stores the previous transform to restore in the next transformation.
     */
    //private AffineTransform preTransform = new AffineTransform(0.7775, 0.0, 0.0, 0.7775, 0.0, 66.75);

    /**
     * The {@link BufferedImage} that is actually drawn on the canvas. Depending
     * on {@link #discardAlpha} this is either the {@link BufferedImage}
     * obtained from {@link #screenImage}, or {@link #screenImage}s buffer
     * re-wrapped using a RGB color model.
     */
    protected BufferedImage bufferedImage;

    public JHotDrawInteractiveDisplay2D( final int width, final int height, final TransformEventHandlerFactory< T > factory)
    {
        super();
        
        setPreferredSize( new Dimension( width, height ) );
        setFocusable( true );

        this.bufferedImage = null;
        this.overlayRenderers = new CopyOnWriteArrayList< OverlayRenderer >();
        this.transformListeners = new CopyOnWriteArrayList< TransformListener< T > >();
        
        addTransformListener(new TransformListener<T>(){
            @Override
            public void transformChanged(T transform) {

                if(AffineTransform2D.class.isInstance(transform))
                {
                    // Convert AffineTransform2D to java.awt.geo.AffineTransform object
                    AffineTransform2D trsf = (AffineTransform2D)transform;
                    // array design is different
                    double[] tr = trsf.getRowPackedCopy();
                    preTransform = new AffineTransform(tr[0], tr[3], tr[1], tr[4], tr[2], tr[5]);
                    invalidateHandles();                  
                }
            }
        });

        addComponentListener( new ComponentAdapter()
        {
            @Override
            public void componentResized( final ComponentEvent e )
            {
                final int w = getWidth();
                final int h = getHeight();
                handler.setCanvasSize( w, h, true );
                for ( final OverlayRenderer or : overlayRenderers )
                    or.setCanvasSize( w, h );
                
                //setBounds(0,0,w,h);
//				enableEvents( AWTEvent.MOUSE_MOTION_EVENT_MASK );
            }
        } );

        addMouseListener( new MouseAdapter()
        {
            @Override
            public void mousePressed( final MouseEvent e )
            {
                requestFocusInWindow();
            }
        } );

        handler = factory.create( this );
        handler.setCanvasSize( width, height, false );

        //activateHandler will call addHandler(handler) in case of changing to SpimTool
        //addHandler( handler );
    }

    public void activateHandler()
    {
        addHandler( handler );
    }

    public void deactivateHandler()
    {
        removeHandler( handler );
    }

    @Override
    public void drawImage(Graphics2D g) {
        final BufferedImage bi;
        synchronized ( this )
        {
            bi = bufferedImage;
        }

        if ( bi != null )
        {
            g.drawImage( bi, 0, 0, getWidth(), getHeight(), null );
        }

        for ( final OverlayRenderer or : overlayRenderers )
            or.drawOverlays( g );
    }

    /**
     * Set the {@link BufferedImage} that is to be drawn on the canvas.
     *
     * @param bufferedImage image to draw (may be null).
     */
    public synchronized void setBufferedImage( final BufferedImage bufferedImage )
    {
        this.bufferedImage = bufferedImage;
    }

    /**
     * Add an {@link OverlayRenderer} that draws on top of the current {@link #bufferedImage}.
     *
     * @param renderer overlay renderer to add.
     */
    public void addOverlayRenderer( final OverlayRenderer renderer )
    {
        overlayRenderers.add( renderer );
        renderer.setCanvasSize( getWidth(), getHeight() );
    }

    /**
     * Remove an {@link OverlayRenderer}.
     *
     * @param renderer overlay renderer to remove.
     */
    public void removeOverlayRenderer( final OverlayRenderer renderer )
    {
        overlayRenderers.remove( renderer );
    }

    /**
     * Add a {@link TransformListener} to notify about view transformation changes.
     *
     * @param listener the transform listener to add.
     */
    public void addTransformListener( final TransformListener< T > listener )
    {
        transformListeners.add( listener );
    }

    /**
     * Remove a {@link TransformListener}.
     *
     * @param listener the transform listener to remove.
     */
    public void removeTransformListener( final TransformListener< T > listener )
    {
        transformListeners.remove( listener );
    }

    /**
     * Add new event handler. Depending on the interfaces implemented by
     * <code>handler</code> calls {@link Component#addKeyListener(KeyListener)},
     * {@link Component#addMouseListener(MouseListener)},
     * {@link Component#addMouseMotionListener(MouseMotionListener)},
     * {@link Component#addMouseWheelListener(MouseWheelListener)}.
     */
    public void addHandler( final Object handler )
    {
        if ( KeyListener.class.isInstance( handler ) )
            addKeyListener( ( KeyListener ) handler );

        if ( MouseMotionListener.class.isInstance( handler ) )
            addMouseMotionListener( ( MouseMotionListener ) handler );

        if ( MouseListener.class.isInstance( handler ) )
            addMouseListener( ( MouseListener ) handler );

        if ( MouseWheelListener.class.isInstance( handler ) )
            addMouseWheelListener( ( MouseWheelListener ) handler );
    }

    /**
     * Remove an event handler.
     * Add new event handler. Depending on the interfaces implemented by
     * <code>handler</code> calls {@link Component#removeKeyListener(KeyListener)},
     * {@link Component#removeMouseListener(MouseListener)},
     * {@link Component#removeMouseMotionListener(MouseMotionListener)},
     * {@link Component#removeMouseWheelListener(MouseWheelListener)}.
     */
    public void removeHandler( final Object handler )
    {
        if ( KeyListener.class.isInstance( handler ) )
            removeKeyListener( ( KeyListener ) handler );

        if ( MouseMotionListener.class.isInstance( handler ) )
            removeMouseMotionListener( ( MouseMotionListener ) handler );

        if ( MouseListener.class.isInstance( handler ) )
            removeMouseListener( ( MouseListener ) handler );

        if ( MouseWheelListener.class.isInstance( handler ) )
            removeMouseWheelListener( ( MouseWheelListener ) handler );
    }

    /**
     * Get the {@link TransformEventHandler} that handles mouse and key events
     * to update our view transform.
     *
     * @return handles mouse and key events to update the view transform.
     */
    public TransformEventHandler< T > getTransformEventHandler()
    {
        return handler;
    }

    /**
     * Set the {@link TransformEventHandler} that handles mouse and key events
     * to update our view transform.
     *
     * @param transformEventHandler mouse and key events to update the view transform
     */
    public synchronized void setTransformEventHandler( final TransformEventHandler< T > transformEventHandler )
    {
        removeHandler( handler );
        handler = transformEventHandler;
        handler.setCanvasSize( getWidth(), getHeight(), false );
        addHandler( handler );
    }

    /**
     * This is called by our {@link #getTransformEventHandler() transform event
     * handler} when the transform is changed. In turn, we notify all our
     * {@link TransformListener TransformListeners} that the view transform has
     * changed.
     */
    @Override
    public void transformChanged( final T transform )
    {
        for ( final TransformListener< T > l : transformListeners )
            l.transformChanged( transform );
    }
}
