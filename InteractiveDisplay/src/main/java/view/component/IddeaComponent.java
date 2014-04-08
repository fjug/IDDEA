package view.component;

import controller.tool.SpimTool;
import model.figure.DrawFigureFactory;
import net.imglib2.IterableInterval;

import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.RealARGBConverter;

import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.jhotdraw.draw.*;
import org.jhotdraw.draw.action.ButtonFactory;
import org.jhotdraw.draw.io.DOMStorableInputOutputFormat;
import org.jhotdraw.draw.io.InputFormat;
import org.jhotdraw.draw.io.OutputFormat;
import org.jhotdraw.draw.tool.BezierTool;
import org.jhotdraw.util.ResourceBundleUtil;

import view.display.InteractiveDrawingView;
import view.viewer.InteractiveRealViewer2D;
import view.viewer.InteractiveViewer2D;

import javax.swing.*;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * IddeaComponent provides better user interactions to handle image and annotate them with jhotdraw figures.
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/4/13
 */
public class IddeaComponent extends JPanel {

    /**
     * It holds the current interactive viewer 2d
     */
    private InteractiveRealViewer2D currentInteractiveViewer2D;
    public InteractiveRealViewer2D getCurrentInteractiveViewer2D() {
        return currentInteractiveViewer2D;
    }

    private DrawingEditor editor;
    private String toolbarLocation;
    private boolean toolbarVisible = false;
    private IntervalView<DoubleType> intervalViewDoubleType = null;

    private JToolBar tb;
    private JScrollPane scrollPane;
    private InteractiveDrawingView view;



    public InteractiveDrawingView getInteractiveDrawingView(IntervalView< DoubleType > viewImg)
    {
        if(viewImg != null)
        {
            final AffineTransform2D transform = new AffineTransform2D();

            final DoubleType min = new DoubleType();
            final DoubleType max = new DoubleType();
            computeMinMax( viewImg, min, max );

            final RealARGBConverter< DoubleType > converter = new RealARGBConverter< DoubleType >( min.get(), max.get());

            currentInteractiveViewer2D = new InteractiveViewer2D<DoubleType>((int)viewImg.max(0), (int)viewImg.max(1), Views.extendZero(viewImg), transform, converter);
        }
        else
        {
            final AffineTransform2D transform = new AffineTransform2D();
            RealRandomAccessible< DoubleType > dummy = new DummyRealRandomAccessible();
            final RealARGBConverter< DoubleType > converter = new RealARGBConverter< DoubleType >( 0, 0);

            currentInteractiveViewer2D = new InteractiveRealViewer2D<DoubleType>(300, 200, dummy, transform, converter);
        }


        return currentInteractiveViewer2D.getJHotDrawDisplay();
    }


    public IddeaComponent() {

        editor = new DefaultDrawingEditor();
        createToolbar();

        try{
            initComponents();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

        setEditor(editor);
        view.setDrawing(createDrawing());
    }

    /**
     * Creates a new Drawing for this view.
     */
    protected Drawing createDrawing() {
        Drawing drawing = new QuadTreeDrawing();
        DOMStorableInputOutputFormat ioFormat =
                new DOMStorableInputOutputFormat(new DrawFigureFactory());

        drawing.addInputFormat(ioFormat);

        drawing.addOutputFormat(ioFormat);
        return drawing;
    }


    /**
     * Sets a drawing editor for the view.
     */
    public void setEditor(DrawingEditor newValue) {
        if (editor != null) {
            editor.remove(view);
        }
        editor = newValue;
        if (editor != null) {
            editor.add(view);
        }
    }

    private void createToolbar()
    {
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");

        tb = new JToolBar();
        tb.setOrientation(JToolBar.HORIZONTAL);

        addCreationButtonsTo(tb, editor);
        tb.setName(labels.getString("window.drawToolBar.title"));
    }


    private void addCreationButtonsTo(JToolBar tb, DrawingEditor editor) {
        addDefaultCreationButtonsTo(tb, editor,
                ButtonFactory.createDrawingActions(editor),
                ButtonFactory.createSelectionActions(editor));
    }

    public void addDefaultCreationButtonsTo(JToolBar tb, final DrawingEditor editor,
                                            Collection<Action> drawingActions, Collection<Action> selectionActions) {

        ButtonFactory.addSelectionToolTo(tb, editor, drawingActions, selectionActions);

        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("model.Labels");
        ButtonFactory.addToolTo(tb, editor, new SpimTool(), "edit.createSpim", labels);

        tb.addSeparator();

        HashMap<AttributeKey, Object > a = new HashMap< AttributeKey, Object >();
        org.jhotdraw.draw.AttributeKeys.FILL_COLOR.put( a, new Color( 0.0f, 1.0f, 0.0f, 0.1f ) );
        org.jhotdraw.draw.AttributeKeys.STROKE_COLOR.put( a, new Color( 1.0f, 0.0f, 0.0f, 0.33f ) );
        ButtonFactory.addToolTo(tb, editor, new BezierTool(new BezierFigure(true), a), "edit.createPolygon",
                ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels"));

        HashMap<AttributeKey, Object> foreground = new HashMap< AttributeKey, Object>();
        org.jhotdraw.draw.AttributeKeys.STROKE_COLOR.put( foreground, new Color(1.0f, 0.0f, 0.0f, 0.33f) );
        org.jhotdraw.draw.AttributeKeys.STROKE_WIDTH.put( foreground, 15d);
        ButtonFactory.addToolTo(tb, editor, new BezierTool(new BezierFigure(), foreground), "edit.scribbleForeground", labels);

        HashMap<AttributeKey, Object> background = new HashMap< AttributeKey, Object>();
        org.jhotdraw.draw.AttributeKeys.STROKE_COLOR.put( background, new Color( 0.0f, 0.0f, 1.0f, 0.33f) );
        org.jhotdraw.draw.AttributeKeys.STROKE_WIDTH.put( background, 15d);
        ButtonFactory.addToolTo(tb, editor, new BezierTool(new BezierFigure(), background), "edit.scribbleBackground", labels);

        tb.add(ButtonFactory.createStrokeWidthButton(
                editor,
                new double[]{5d, 10d, 15d},
                ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels")));
    }


    @SuppressWarnings("unchecked")
    private void initComponents()
    {
        scrollPane = new javax.swing.JScrollPane();

        view = getInteractiveDrawingView(intervalViewDoubleType);

        setLayout(new java.awt.BorderLayout());

        scrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setViewportView(view);

        add(scrollPane, java.awt.BorderLayout.CENTER);

        if(toolbarVisible)
        {
            add(tb, toolbarLocation);
        }
    }


    public void setToolBarLocation(String location) {
        if(location.equals(BorderLayout.WEST) || location.equals(BorderLayout.EAST) )
        {
            tb.setOrientation(JToolBar.VERTICAL);
        }
        else
        {
            tb.setOrientation(JToolBar.HORIZONTAL);
        }

        toolbarLocation = location;
    }

    public void setToolBarVisible(boolean visible) {
        toolbarVisible = visible;
        if(toolbarVisible)
        {
            add(tb, toolbarLocation);
        }
        else
        {
            remove(tb);
        }
    }

    public void loadAnnotations(String filename) {
        try {
            final Drawing drawing = createDrawing();

            boolean success = false;
            for (InputFormat sfi : drawing.getInputFormats()) {
                try {
                    sfi.read(new FileInputStream(filename), drawing, true);
                    success = true;
                    break;
                } catch (Exception e) {
                    // try with the next input format
                }
            }
            if (!success) {
                ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.app.Labels");
                throw new IOException(labels.getFormatted("file.open.unsupportedFileFormat.message", filename));
            }

            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    view.setDrawing(drawing);
                }
            });
        } catch (InterruptedException e) {
            InternalError error = new InternalError();
            error.initCause(e);
            throw error;
        } catch (java.lang.reflect.InvocationTargetException e) {
            InternalError error = new InternalError();
            error.initCause(e);
            throw error;
        } catch (IOException e) {
            InternalError error = new InternalError();
            error.initCause(e);
            throw error;
        }
    }

    public void saveAnnotations(String filename) {
        Drawing drawing = view.getDrawing();
        OutputFormat outputFormat = drawing.getOutputFormats().get(0);
        try {
            outputFormat.write(new FileOutputStream(filename), drawing);
        } catch (IOException e) {
            InternalError error = new InternalError();
            error.initCause(e);
            throw error;

        }
    }

    public < T extends RealType< T > & NativeType< T >>
    void updateScreenImage( final IntervalView< T > viewImg )
    {
        final T min = Views.iterable( viewImg ).firstElement().copy();
        final T max = min.copy();
        computeMinMax( viewImg, min, max );

        RealRandomAccessible< T > interpolated = null;
        if(viewImg.numDimensions() > 2)
            interpolated = Views.interpolate( Views.extendZero(Views.hyperSlice( viewImg, 2, 0 )), new NearestNeighborInterpolatorFactory<T>() );
        else
            interpolated = Views.interpolate( Views.extendZero(viewImg), new NearestNeighborInterpolatorFactory<T>() );

        final RealARGBConverter< T > converter = new RealARGBConverter< T >( min.getMinValue(), max.getMaxValue());

        updateDoubleTypeSourceAndConverter(interpolated, converter);
    }

    /**
     * Sets the image data to be displayed when paintComponent is called.
     *
     * @param viewImg
     *            an IntervalView<DoubleType> containing the desired view
     *            onto the raw image data
     */
    public void setDoubleTypeScreenImage( final IntervalView< DoubleType > viewImg ) {
        final DoubleType min = new DoubleType();
        final DoubleType max = new DoubleType();
        computeMinMax( viewImg, min, max );

        RealRandomAccessible< DoubleType > interpolated = null;
        if(viewImg.numDimensions() > 2)
            interpolated = Views.interpolate( Views.extendZero(Views.hyperSlice( viewImg, 2, 0 )), new NearestNeighborInterpolatorFactory<DoubleType>() );
        else
            interpolated = Views.interpolate( Views.extendZero(viewImg), new NearestNeighborInterpolatorFactory<DoubleType>() );

        final RealARGBConverter< DoubleType > converter = new RealARGBConverter< DoubleType >( min.get(), max.get());

        updateDoubleTypeSourceAndConverter(interpolated, converter);
    }

    /**
     * Sets the image data to be displayed when paintComponent is called.
     *
     * @param viewImg
     *            an IntervalView<LongType> containing the desired view
     *            onto the raw image data
     */
    public void setLongTypeScreenImage( final IntervalView< LongType > viewImg ) {

        final LongType min = new LongType();
        final LongType max = new LongType();
        computeMinMax( viewImg, min, max );

        RealRandomAccessible< LongType > interpolated = Views.interpolate( Views.extendZero(viewImg),
                new NearestNeighborInterpolatorFactory<LongType>() );

        final RealARGBConverter< LongType > converter = new RealARGBConverter< LongType >( min.get(), max.get() );

        updateDoubleTypeSourceAndConverter(interpolated, converter);
    }

    /**
     * Update the realRandomSource with new source.
     * @param source
     */
    public void updateDoubleTypeSourceAndConverter(RealRandomAccessible source,
                                                   RealARGBConverter converter)
    {
        currentInteractiveViewer2D.updateConverter(converter);
        currentInteractiveViewer2D.updateSource(source);
    }

    private static < T extends Type< T > & Comparable< T > > void computeMinMax( final IterableInterval< T > source, final T minValue, final T maxValue )
    {
        for ( final T t : source )
            if ( minValue.compareTo( t ) > 0 )
                minValue.set( t );
            else if ( maxValue.compareTo( t ) < 0 )
                maxValue.set( t );
    }

    public static < T extends RealType< T > & NativeType< T > > void computeMinMax( final IntervalView< T > viewImg, final T minValue, final T maxValue )
    {
        // create a cursor for the image (the order does not matter)
        final Iterator< T > iterator = Views.iterable( viewImg ).iterator();

        // initialize min and max with the first image value
        T type = iterator.next();

        minValue.set( type );
        maxValue.set( type );

        // loop over the rest of the data and determine min and max value
        while ( iterator.hasNext() ) {
            // we need this type more than once
            type = iterator.next();

            if ( type.compareTo( minValue ) < 0 ) minValue.set( type );

            if ( type.compareTo( maxValue ) > 0 ) maxValue.set( type );
        }
    }

//    public < T extends RealType< T > & NativeType< T >> InteractiveViewer2D show( final Img<T> interval ) {
//        final AffineTransform2D transform = new AffineTransform2D();
//        InteractiveViewer2D iview = null;
//
//        System.out.println(interval.firstElement().getClass());
//
//
//        {
//            final T min = Views.iterable( interval ).firstElement().copy();
//            final T max = min.copy();
//            getMinMax( Views.iterable( interval ), min, max );
//
////            RealRandomAccessible< T > interpolated = Views.interpolate( interval, new NLinearInterpolatorFactory<T>() );
//            RealRandomAccessible< T > interpolated = Views.interpolate( Views.extendZero(interval), new NearestNeighborInterpolatorFactory<T>() );
//            //final RealARGBConverter< T > converter = new RealARGBConverter< T >( min.getMinValue(), max.getMaxValue());
//
//            final LUTConverter< T > converter = new LUTConverter< T >( min.getMinValue(), max.getMaxValue(), ColorTables.FIRE);
//            iview = new InteractiveViewer2D<T>((int)interval.max(0), (int)interval.max(1), Views.extendZero(interval), transform, converter);
//        }
//
//        return iview;
//    }

    public void setPreferredSize(Dimension dim)
    {
        currentInteractiveViewer2D.getJHotDrawDisplay().setPreferredSize(dim);
    }
}
