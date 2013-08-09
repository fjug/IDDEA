package view;

import static org.jhotdraw.draw.AttributeKeys.CANVAS_HEIGHT;
import static org.jhotdraw.draw.AttributeKeys.CANVAS_WIDTH;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformEventHandlerFactory;
import net.imglib2.ui.TransformListener;

import org.jhotdraw.draw.DefaultDrawingView;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.event.TransformEdit;
import org.jhotdraw.draw.handle.Handle;

import javax.swing.*;
import javax.swing.undo.UndoableEdit;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: moon
 * Date: 8/7/13
 * Time: 11:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class JHotDrawInteractiveDisplay2D<T> extends DefaultDrawingView implements TransformListener<T>
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
    private AffineTransform preTransform = new AffineTransform(0.7775, 0.0, 0.0, 0.7775, 0.0, 66.75);

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
                    
                    translation.x = (int) -tr[2];
                    translation.y = (int) -tr[5];
                    scaleFactor = tr[0];
                    invalidateHandles();                  
//                    AffineTransform trans = new AffineTransform(tr[0], tr[3], tr[1], tr[4], tr[2], tr[5]);
//
//                    getDrawing().willChange();
//                    if(preTransform != null)
//                    {
//                        try{
//                            preTransform.invert();
//                        } catch (NoninvertibleTransformException ex)
//                        {
//                        }
//                        getDrawing().transform(preTransform);
//                    }
//                    getDrawing().transform(trans);
//                    preTransform = trans;
//                    getDrawing().changed();

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
    public void paintComponent(Graphics gr) {
        Graphics2D g = (Graphics2D) gr;

        setViewRenderingHints(g);
       
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
        
        //drawBackground(g);
        drawCanvas(g);
        drawConstrainer(g);
//		if (isDrawingDoubleBuffered()) {
//			if (DefaultDrawingView.isWindows) {
//				drawDrawingNonvolatileBuffered(g);
//			} else {
//				drawDrawingVolatileBuffered(g);
//			}
//		} else {
//			drawDrawing(g);
//		}

        drawDrawing(g);
        drawHandles(g);
        drawTool(g);
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

    /** Draws the canvas. If the {@code AttributeKeys.CANVAS_FILL_OPACITY} is
     * not fully opaque, the canvas area is filled with the background paint
     * before the {@code AttributeKeys.CANVAS_FILL_COLOR} is drawn.
     */
    @Override
    protected void drawCanvas(Graphics2D gr) {
        if (drawing != null) {
            Graphics2D g = (Graphics2D) gr.create();                
            g.setTransform(preTransform);

            drawing.setFontRenderContext(g.getFontRenderContext());
            drawing.drawCanvas(g);
            g.dispose();
        }
    }
    
    @Override
    protected void drawDrawing(Graphics2D gr) {

        if (drawing != null) {
            if (drawing.getChildCount() == 0 && emptyDrawingLabel != null) {
                emptyDrawingLabel.setBounds(0, 0, getWidth(), getHeight());
                emptyDrawingLabel.paint(gr);
            } else {
                Graphics2D g = (Graphics2D) gr.create();                
                g.setTransform(preTransform);
                
                drawing.setFontRenderContext(g.getFontRenderContext());
                drawing.draw(g);

                g.dispose();
            }

        }
    }
    
    /**
     * Converts drawing coordinates to view coordinates.
     */
    @Override
    public Point drawingToView(
            Point2D.Double p) {
    	 Point2D po = preTransform.transform(p, null);
    	return new Point((int) po.getX(), (int) po.getY());
    }

    @Override
    public Rectangle drawingToView(
            Rectangle2D.Double r) {
    	double[] drawing = {r.x, r.y, r.x + r.width, r.y, r.x + r.width, r.y + r.height, r.x, r.y + r.height};
    	double[] view = new double[8];
    	preTransform.transform(drawing, 0, view, 0, 4);

    	int x1 = Math.min((int)view[0], (int)view[2]);
    	x1 = Math.min(x1, (int)view[4]);
    	x1 = Math.min(x1, (int)view[6]);
    
    	int x2 = Math.max((int)view[0], (int)view[2]);
    	x2 = Math.max(x2, (int)view[4]);
    	x2 = Math.max(x2, (int)view[6]);

    	int y1 = Math.min((int)view[1], (int)view[3]);
    	y1 = Math.min(y1, (int)view[5]);
    	y1 = Math.min(y1, (int)view[7]);
    	
    	int y2 = Math.max((int)view[1], (int)view[3]);
    	y2 = Math.max(y2, (int)view[5]);
    	y2 = Math.max(y2, (int)view[7]);

    	return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * Converts view coordinates to drawing coordinates.
     */
    @Override
    public Point2D.Double viewToDrawing(Point p) {
    	Point2D point = null;

    	try {
			point = preTransform.inverseTransform(new Point2D.Double((double)p.x, (double)p.y), null);
		} catch (NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return (Point2D.Double) point;
    }

    @Override
    public Rectangle2D.Double viewToDrawing(Rectangle r) {
    	double[] drawing = {r.x, r.y, r.x + r.width, r.y, r.x + r.width, r.y + r.height, r.x, r.y + r.height};
    	double[] view = new double[8];
    	preTransform.transform(drawing, 0, view, 0, 4);

    	try {
        	preTransform.inverseTransform(drawing, 0, view, 0, 4);
		} catch (NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	double x1 = Math.min(view[0], view[2]);
    	x1 = Math.min(x1, view[4]);
    	x1 = Math.min(x1, view[6]);
    
    	double x2 = Math.max(view[0], view[2]);
    	x2 = Math.max(x2, view[4]);
    	x2 = Math.max(x2, view[6]);

    	double y1 = Math.min(view[1], view[3]);
    	y1 = Math.min(y1, view[5]);
    	y1 = Math.min(y1, view[7]);
    	
    	double y2 = Math.max(view[1], view[3]);
    	y2 = Math.max(y2, view[5]);
    	y2 = Math.max(y2, view[7]);
    	
    	return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
    }

    
    @Override
    public AffineTransform getDrawingToViewTransform() {
        AffineTransform t = new AffineTransform();
        t.setTransform(preTransform);
        return t;
    }
       
    /**
     * All the scale factor and translating factor are decided by the given transformation.
     * We don't need this for in this context.
     */
    @Override
    protected void validateViewTranslation() {
    }
    
    @Override
    protected void repaintDrawingArea(Rectangle2D.Double r) {
        Rectangle vr = drawingToView(r);
        repaint(vr);
    }

    @Override
    public void invalidate() {
    }
}
