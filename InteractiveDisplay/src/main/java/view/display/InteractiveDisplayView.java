package view.display;

import edu.umd.cs.findbugs.annotations.Nullable;
import ij.ImagePlus;
import model.figure.DrawFigureFactory;
import model.source.MandelbrotRealRandomAccessible;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.*;
import net.imglib2.type.numeric.IntegerType;
import view.component.DummyRealRandomAccessible;
import view.converter.ChannelARGBConverter;
import net.imglib2.display.projector.composite.CompositeXYRandomAccessibleProjector;
import net.imglib2.exception.ImgLibException;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.img.imageplus.*;
import net.imglib2.ui.InteractiveDisplayCanvas;
import net.imglib2.ui.util.InterpolatingSource;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import net.imglib2.view.composite.CompositeIntervalView;
import org.jhotdraw.draw.io.OutputFormat;
import org.jhotdraw.draw.io.InputFormat;
import org.jhotdraw.draw.print.DrawingPageable;
import org.jhotdraw.draw.io.DOMStorableInputOutputFormat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.print.Pageable;

import org.jhotdraw.gui.*;
import org.jhotdraw.undo.*;
import org.jhotdraw.util.*;

import java.awt.*;
import java.beans.*;
import java.io.*;
import java.net.URI;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jhotdraw.app.AbstractView;
import org.jhotdraw.app.action.edit.RedoAction;
import org.jhotdraw.app.action.edit.UndoAction;
import org.jhotdraw.draw.*;
import org.jhotdraw.draw.action.*;
import org.jhotdraw.gui.URIChooser;
import org.jhotdraw.net.URIUtil;

import view.converter.ColorTables;
import view.converter.LUTConverter;
import view.overlay.ObjectInfo;
import view.overlay.ObjectInfoOverlay;
import view.overlay.ObjectInfoTransformOverlay;
import view.overlay.SourceInfoTransformOverlay;
import view.viewer.InteractiveRealViewer;
import view.viewer.InteractiveRealViewer2D;
import view.viewer.InteractiveViewer2D;

import static view.converter.ChannelARGBConverter.Channel.B;
import static view.converter.ChannelARGBConverter.Channel.G;
import static view.converter.ChannelARGBConverter.Channel.R;


/**
 * InteractiveDisplayView shows a DrawingView where users create and modify figures.
 *
 * @version 0.1beta
 * @since 8/12/13 5:05 PM
 * @author HongKee Moon
 */

public class InteractiveDisplayView extends AbstractView implements ChangeListener, ActionListener, MouseMotionListener {

    private javax.swing.JScrollPane scrollPane;
    private JSlider sliderTime;
    private JPanel leftPanel;
    private InteractiveDrawingView view;

    /**
     * Each DrawView uses its own undo redo manager.
     * This allows for undoing and redoing actions per view.
     */
    private UndoRedoManager undo;

    /**
     * Depending on the type of an application, there may be one editor per
     * view, or a single shared editor for all views.
     */
    private DrawingEditor editor;

    /**
     * It holds the current interactive viewer 2d
     */
    private InteractiveRealViewer currentInteractiveViewer2D;

    public InteractiveRealViewer getCurrentInteractiveViewer2D() {
        return currentInteractiveViewer2D;
    }

    private Img interval = null;

    Img< ARGBType > argbImg = null;

    CompositeXYRandomAccessibleProjector projector = null;

    ArrayList< Converter< UnsignedShortType, ARGBType > > converterListARGB = null;

    JCheckBox cbCh0;
    JCheckBox cbCh1;
    JCheckBox cbCh2;
    JCheckBox cbCh3;

    /**
     * Creates a new view.
     */
    public InteractiveDisplayView() {

        try{
            initComponents();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }


        scrollPane.setLayout(new PlacardScrollPaneLayout());
        scrollPane.setBorder(new EmptyBorder(0,0,0,0));

        setEditor(new DefaultDrawingEditor());
        undo = new UndoRedoManager();
        view.setDrawing(createDrawing());
        view.getDrawing().addUndoableEditListener(undo);
        initActions();
        undo.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                setHasUnsavedChanges(undo.hasSignificantEdits());
            }
        });

//        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
//
//        JPanel placardPanel = new JPanel(new BorderLayout());
//        javax.swing.AbstractButton pButton;
//        pButton = ButtonFactory.createZoomButton(view);
//        pButton.putClientProperty("Quaqua.Button.style","placard");
//        pButton.putClientProperty("Quaqua.Component.visualMargin",new Insets(0,0,0,0));
//        pButton.setFont(UIManager.getFont("SmallSystemFont"));
//        placardPanel.add(pButton, BorderLayout.WEST);
//        pButton = ButtonFactory.createToggleGridButton(view);
//        pButton.putClientProperty("Quaqua.Button.style","placard");
//        pButton.putClientProperty("Quaqua.Component.visualMargin",new Insets(0,0,0,0));
//        pButton.setFont(UIManager.getFont("SmallSystemFont"));
//        labels.configureToolBarButton(pButton, "view.toggleGrid.placard");
//        placardPanel.add(pButton, BorderLayout.EAST);
//        scrollPane.add(placardPanel, JScrollPane.LOWER_LEFT_CORNER);
    }

    private void initComponents()
    {
        scrollPane = new javax.swing.JScrollPane();

        leftPanel = new javax.swing.JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        sliderTime = new JSlider( JSlider.HORIZONTAL, 0, 10 - 1, 0 );
        sliderTime.addChangeListener(this);
        view = getInteractiveDrawingView();

        setLayout(new java.awt.BorderLayout());

        scrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setViewportView(view);

        add(scrollPane, java.awt.BorderLayout.CENTER);
        add(sliderTime, java.awt.BorderLayout.SOUTH);
        add(leftPanel, java.awt.BorderLayout.WEST);
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
     * Creates a Pageable object for printing the view.
     */
    public Pageable createPageable() {
        return new DrawingPageable(view.getDrawing());

    }


    /**
     * Initializes view specific actions.
     */
    private void initActions() {
        getActionMap().put(UndoAction.ID, undo.getUndoAction());
        getActionMap().put(RedoAction.ID, undo.getRedoAction());
    }
    @Override
    protected void setHasUnsavedChanges(boolean newValue) {
        super.setHasUnsavedChanges(newValue);
        undo.setHasSignificantEdits(newValue);
    }

    /**
     * Writes the view to the specified uri.
     */
    @Override
    public void write(URI f, URIChooser fc) throws IOException {
        Drawing drawing = view.getDrawing();
        OutputFormat outputFormat = drawing.getOutputFormats().get(0);
        outputFormat.write(f, drawing);
    }

    /**
     * Reads the view from the specified uri.
     */
    @Override
    public void read(URI f, URIChooser fc) throws IOException {
        try {
            if(f.toString().lastIndexOf("xml") > 0)
            {

                final Drawing drawing = createDrawing();

                boolean success = false;
                for (InputFormat sfi : drawing.getInputFormats()) {
                    try {
                        sfi.read(f, drawing, true);
                        success = true;
                        break;
                    } catch (Exception e) {
                        // try with the next input format
                    }
                }
                if (!success) {
                    ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.app.Labels");
                    throw new IOException(labels.getFormatted("file.open.unsupportedFileFormat.message", URIUtil.getName(f)));
                }

                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        view.getDrawing().removeUndoableEditListener(undo);
                        view.setDrawing(drawing);
                        view.getDrawing().addUndoableEditListener(undo);
                        undo.discardAllEdits();
                    }
                });
            }
        } catch (InterruptedException e) {
            InternalError error = new InternalError();
            e.initCause(e);
            throw error;
        } catch (java.lang.reflect.InvocationTargetException e) {
            InternalError error = new InternalError();
            error.initCause(e);
            throw error;
        }
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

    /**
     * Gets the drawing editor of the view.
     */
    public DrawingEditor getEditor() {
        return editor;
    }

    /**
     * Clears the view.
     */
    @Override
    public void clear() {
        final Drawing newDrawing = createDrawing();
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    view.getDrawing().removeUndoableEditListener(undo);
                    view.setDrawing(newDrawing);
                    view.getDrawing().addUndoableEditListener(undo);
                    undo.discardAllEdits();
                }
            });
        } catch (java.lang.reflect.InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean canSaveTo(URI file) {
        return new File(file).getName().endsWith(".xml");
    }

    @Override
    public void setURI(@Nullable URI newValue) {
        if(newValue.toString().lastIndexOf("xml") > 0)
        {
            super.setURI(newValue);
        }
        else
        {
            String filename = newValue.toString().substring(5);

            // Image import
            ImagePlus imp = new ImagePlus( filename );

            // wrap it into an ImgLib image (no copying)
            final Img image = ImagePlusAdapter.wrapNumeric(imp);
            this.interval = image;

            //IntervalView intervalView = Views.interval(image, image);

            System.out.println("Type: " + image.firstElement().getClass());
            System.out.println("Dims: " + image.numDimensions());

            for(int i = 0; i < image.numDimensions(); i++)
            {
                System.out.println("Min: " + image.min(i) + ", Max: " + image.max(i));
                if(i == 3)
                {
                    sliderTime.setMaximum((int) image.max(i) - 1);
                }
            }

            if(!ARGBType.class.isInstance(image.firstElement()))
            {
                if(image.numDimensions() == 4) {

                    intervalView = Views.hyperSlice(interval, 3, 0);

                    final long[] dim = new long[ intervalView.numDimensions() - 1];
                    for ( int d = 0; d < dim.length; ++d )
                        dim[ d ] = intervalView.dimension( d );
                    argbImg = new ArrayImgFactory< ARGBType >().create( dim, new ARGBType() );
                    createCompositeProjector( intervalView, argbImg );

                    cbCh0 = new JCheckBox("Ch-0", true);
                    cbCh0.addChangeListener(this);
                    leftPanel.add(cbCh0);

                    cbCh1 = new JCheckBox("Ch-1", true);
                    cbCh1.addChangeListener(this);
                    leftPanel.add(cbCh1);

                    cbCh2 = new JCheckBox("Ch-2", true);
                    cbCh2.addChangeListener(this);
                    leftPanel.add(cbCh2);

                    cbCh3 = new JCheckBox("Ch-3", true);
                    cbCh3.addChangeListener(this);
                    leftPanel.add(cbCh3);

                    JButton btn = new JButton("Find endpoint");
                    btn.setName("btnEndpoint");
                    btn.addActionListener(this);
                    leftPanel.add(btn);

                    leftPanel.updateUI();

//                    intervalView = Views.hyperSlice(Views.hyperSlice(interval, 3, 0), 2, 0);
//
//                    final UnsignedShortType min = new UnsignedShortType();
//                    final UnsignedShortType max = new UnsignedShortType();
//                    computeMinMax(intervalView, min, max);
//
//                    final RealARGBConverter<UnsignedShortType> converter = new RealARGBConverter< UnsignedShortType >( min.get(), max.get() );
//
//                    currentInteractiveViewer2D.updateConverter(converter);
//                    currentInteractiveViewer2D.updateIntervalSource(Views.extendZero(intervalView));

                }
                else
                {
                    RandomAccessibleInterval imgOrig = Converters.convert(intervalView,
                            new RealDoubleConverter(), new DoubleType());

                    setDoubleTypeScreenImage(Views.interval(imgOrig, imgOrig));
                }

            }
            else
            {
                currentInteractiveViewer2D.updateConverter(new TypeIdentity<ARGBType>());
                currentInteractiveViewer2D.updateIntervalSource(Views.extendZero(interval));
            }
        }
    }

    private void createCompositeProjector( final IntervalView in, final Img< ARGBType > out )
    {
//        final UnsignedShortType min = new UnsignedShortType();
//        final UnsignedShortType max = new UnsignedShortType();
//        computeMinMax(Views.hyperSlice(in, 2, 0), min, max);

        converterListARGB = new ArrayList< Converter< UnsignedShortType, ARGBType > >();
        converterListARGB.add( new RealARGBConverter< UnsignedShortType >( 0, 10000 ) );
//        converterListARGB.add( new RealARGBConverter< UnsignedShortType >( min.get(), max.get() * 0.1 ) );
        converterListARGB.add( new ChannelARGBConverter( G ) );
        converterListARGB.add( new ChannelARGBConverter( R ) );
        converterListARGB.add( new ChannelARGBConverter( B ) );

        projector = new CompositeXYRandomAccessibleProjector( in, out, converterListARGB, 2 );

        projector.setComposite( true );
//		projector.setComposite( 0, false );
//		projector.setComposite( 1, true );
//		projector.setComposite( 2, false );
        projector.map();

        currentInteractiveViewer2D.updateConverter(new TypeIdentity<ARGBType>());
        ARGBType t = new ARGBType();
        t.set(150 << 16 | 150 << 8 | 150);
        currentInteractiveViewer2D.updateIntervalSource(Views.extendValue(out, t));
    }

    private void updateCompositeProjector(final IntervalView in, final Img< ARGBType > out)
    {
//        final UnsignedShortType min = new UnsignedShortType();
//        final UnsignedShortType max = new UnsignedShortType();
//        computeMinMax(in, min, max);
//
//        converterListARGB.set(0,  new RealARGBConverter< UnsignedShortType >( min.get(), max.get() * 0.1 ));

        projector = new CompositeXYRandomAccessibleProjector( in, out, converterListARGB, 2 );

        projector.setComposite( true );
        projector.setComposite( 0, cbCh0.isSelected() );
        projector.setComposite( 1, cbCh1.isSelected() );
        projector.setComposite( 2, cbCh2.isSelected() );
        projector.setComposite( 3, cbCh3.isSelected() );

        projector.map();

        ARGBType t = new ARGBType();
        t.set(150 << 16 | 150 << 8 | 150);
        currentInteractiveViewer2D.updateIntervalSource(Views.extendValue(out, t));
    }

    private void setDoubleTypeScreenImage(final IntervalView< DoubleType > viewImg ) {
        final DoubleType min = new DoubleType();
        final DoubleType max = new DoubleType();
        computeMinMax( viewImg, min, max );

        RealRandomAccessible< DoubleType > interpolated =
                Views.interpolate( Views.extendZero( viewImg ), new NearestNeighborInterpolatorFactory< DoubleType >() );

        //final RealARGBConverter< DoubleType > converter = new RealARGBConverter< DoubleType >( min.get(), max.get() );
        final LUTConverter< DoubleType > converter = new LUTConverter< DoubleType >( min.getMinValue(), max.getMaxValue(), ColorTables.FIRE);

        updateDoubleTypeSourceAndConverter( interpolated, converter );
    }

    public void updateDoubleTypeSourceAndConverter( RealRandomAccessible source, Converter converter ) {
        currentInteractiveViewer2D.updateConverter( converter );
        currentInteractiveViewer2D.updateSource( source );
    }

    public static < T extends Comparable< T > & NativeType< T > > void computeMinMax( final IntervalView< T > viewImg, final T minValue, final T maxValue ) {
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

    @Override
    public boolean hasUnsavedChanges() {
        return false;
    }


    private static < T extends Type< T > & Comparable< T > > void getMinMax( final IterableInterval< T > source, final T minValue, final T maxValue )
    {
        for ( final T t : source )
            if ( minValue.compareTo( t ) > 0 )
                minValue.set( t );
            else if ( maxValue.compareTo( t ) < 0 )
                maxValue.set( t );
    }


    public RandomAccessibleInterval getInterval() {
        return interval;
    }

    /**
     * When the new image is coming, it makes new InteractiveViewer2D.
     * @param interval
     * @return InteractiveView2D
     */
    private < T extends RealType< T > & NativeType< T >> InteractiveViewer2D show( final ImagePlusImg<T, ? > interval )
    {
        final AffineTransform2D transform = new AffineTransform2D();
        InteractiveViewer2D iview = null;

        this.interval = interval;

        if(ARGBType.class.isInstance(interval.firstElement()))
        {
            System.out.println("ARGBType");

            iview = new InteractiveViewer2D(interval.getWidth(), interval.getHeight(),
                    Views.extendZero(interval), transform, new TypeIdentity<ARGBType>());
        }
        else if(interval.getChannels() > 1)
        {
            Img< T > srcImg = null;
            try {
                srcImg = ImagePlusAdapter.wrap(interval.getImagePlus());
            } catch (ImgLibException e) {
                e.printStackTrace();
            }

            final ImgFactory< DoubleType > imgFactory = new ArrayImgFactory< DoubleType >();
            final Img< DoubleType > ret = imgFactory.create( new int[] { interval.getWidth(), interval.getHeight(), 1, 1 }, new DoubleType() );
            final IntervalView< DoubleType > src = Views.hyperSlice( Views.hyperSlice( (Img<DoubleType>)(Img<?>)srcImg, 3, 0 ), 2, 0 );
            final IntervalView< DoubleType > target = Views.offset(ret, 0,0,0,0);

            //DataMover.copy( src, target );

            //Normalize.normalize( ret, new DoubleType( 0. ), new DoubleType( 1. ) );

            final DoubleType min = Views.iterable( target ).firstElement().copy();
            final DoubleType max = min.copy();
            getMinMax( Views.iterable( target ), min, max );

            System.out.println("Min:" + min + "\tMax:" + max);

            //final LUTConverter< DoubleType > converter = new LUTConverter< DoubleType >( min.getMinValue(), max.getMaxValue(), ColorTables.FIRE);
            final RealARGBConverter< DoubleType > converter = new RealARGBConverter< DoubleType >( min.getMinValue(), max.getMaxValue());

            iview = new InteractiveViewer2D<DoubleType>(interval.getWidth(), interval.getHeight(), Views.extendZero(ret), transform, converter);
        }
        else
        {
            final T min = Views.iterable( interval ).firstElement().copy();
            final T max = min.copy();
            getMinMax( Views.iterable( interval ), min, max );

//            RealRandomAccessible< T > interpolated = Views.interpolate( interval, new NLinearInterpolatorFactory<T>() );
            RealRandomAccessible< T > interpolated = Views.interpolate( Views.extendZero(interval), new NearestNeighborInterpolatorFactory<T>() );
            //final RealARGBConverter< T > converter = new RealARGBConverter< T >( min.getMinValue(), max.getMaxValue());

            System.out.println(min.getClass() + " Type");

            final LUTConverter< T > converter = new LUTConverter< T >( min.getMinValue(), max.getMaxValue(), ColorTables.FIRE);

            iview = new InteractiveViewer2D<T>(interval.getWidth(), interval.getHeight(), Views.extendZero(interval), transform, converter);
        }

        return iview;
    }

    /**
     * Update the realConverter with new converter.
     * @param converter
     */
    public void updateRealConverter(Converter converter)
    {
        if(InteractiveRealViewer2D.class.isInstance(currentInteractiveViewer2D))
        {
            ((InteractiveRealViewer2D) currentInteractiveViewer2D).updateConverter(converter);
        }
    }

    /**
     * Update the realRandomSource with new source.
     * @param source
     */
    public void updateRealRandomSource(RealRandomAccessible source)
    {
        if(InteractiveRealViewer2D.class.isInstance(currentInteractiveViewer2D))
        {
            // User doesn't load any picture
            ((InteractiveRealViewer2D) currentInteractiveViewer2D).updateSource(source);
        }
//        else
//        {
//            // In case that user loaded InteractiveViewer2D already,
//            // We're changing to InteractiveRealViewer2D for the updating source
//            currentInteractiveViewer2D = interactiveRealViewer2D;
//
//            editor.remove(view);
//            InteractiveDrawingView newView = getCurrentInteractiveViewer2D().getJHotDrawDisplay();
//            newView.copyFrom(view);
//
//            editor.add(newView);
//            view = newView;
//
//            scrollPane.setViewportView(view);
//
//            ((InteractiveRealViewer2D) currentInteractiveViewer2D).updateSource(source);
//        }
    }

    /**
     * Request repaint() of the viewer.
     */
    public void updateRequest()
    {
        currentInteractiveViewer2D.requestRepaint();
    }

    /**
     * Get the current InteractiveDisplayCanvas.
     * @return InteractiveDisplayCanvas
     */
    public InteractiveDisplayCanvas getInteractiveDisplayCanvas()
    {
        return currentInteractiveViewer2D.getJHotDrawDisplay();
    }

    /**
     * Get the selected shapes
     * @return shapes
     */
    public Set<Figure> getSelectedFigures()
    {
        return currentInteractiveViewer2D.getJHotDrawDisplay().getSelectedFigures();
    }

    private InteractiveDrawingView getInteractiveDrawingView()
    {
//        final int width = 800;
//        final int height = 600;
//
//        final int maxIterations = 100;
//        MandelbrotRealRandomAccessible mandelbrot = new MandelbrotRealRandomAccessible( maxIterations );
//
//        final AffineTransform2D transform = new AffineTransform2D();
//        transform.scale( 200 );
//        transform.translate( width / 2.0, height / 2.0 );
//
//        //final RealARGBConverter< LongType > converter = new RealARGBConverter< LongType >( 0, maxIterations );
//
//        final LUTConverter< LongType > converter = new LUTConverter< LongType >( 0d, 50, ColorTables.FIRE);
//
//        currentInteractiveViewer2D = new InteractiveRealViewer2D<LongType>(width, height, mandelbrot, transform, converter);
//        currentInteractiveViewer2D.getJHotDrawDisplay().addOverlayRenderer(new SourceInfoOverlay());
//
////        interactiveRealViewer2D = iview;
//
//        return currentInteractiveViewer2D.getJHotDrawDisplay();

        final AffineTransform2D transform = new AffineTransform2D();
        RealRandomAccessible< DoubleType > dummy = new DummyRealRandomAccessible();
        final RealARGBConverter< DoubleType > converter = new RealARGBConverter< DoubleType >( 0, 0);

        currentInteractiveViewer2D = new InteractiveRealViewer2D<DoubleType>(300, 200, dummy, transform, converter);

        return currentInteractiveViewer2D.getJHotDrawDisplay();
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent)
    {
        if(sliderTime.equals(changeEvent.getSource()))
        {
            int index = sliderTime.getValue();

            if(argbImg != null) {
                intervalView = Views.hyperSlice(interval, 3, index);
                updateCompositeProjector(Views.hyperSlice(interval, 3, index), argbImg);
            }

        }
        else if(cbCh0.equals(changeEvent.getSource()))
        {
    		projector.setComposite( 0, cbCh0.isSelected() );
            projector.map();
            ARGBType t = new ARGBType();
            t.set(150 << 16 | 150 << 8 | 150);
            currentInteractiveViewer2D.updateIntervalSource(Views.extendValue(argbImg, t));
        }
        else if(cbCh1.equals(changeEvent.getSource()))
        {
            projector.setComposite( 1, cbCh1.isSelected() );
            projector.map();
            ARGBType t = new ARGBType();
            t.set(150 << 16 | 150 << 8 | 150);
            currentInteractiveViewer2D.updateIntervalSource(Views.extendValue(argbImg, t));
        }
        else if(cbCh2.equals(changeEvent.getSource()))
        {
            projector.setComposite( 2, cbCh2.isSelected() );
            projector.map();
            ARGBType t = new ARGBType();
            t.set(150 << 16 | 150 << 8 | 150);
            currentInteractiveViewer2D.updateIntervalSource(Views.extendValue(argbImg, t));
        }
        else if(cbCh3.equals(changeEvent.getSource()))
        {
            projector.setComposite( 3, cbCh3.isSelected() );
            projector.map();
            ARGBType t = new ARGBType();
            t.set(150 << 16 | 150 << 8 | 150);
            currentInteractiveViewer2D.updateIntervalSource(Views.extendValue(argbImg, t));
        }
    }

    IntervalView intervalView = null;
    ObjectInfoTransformOverlay objectLabel = null;
    ObjectInfoOverlay objectInfo = null;

    @Override
    public void actionPerformed(ActionEvent actionEvent) {

        if(actionEvent.getSource() instanceof JButton)
        {
            JButton comp = (JButton) actionEvent.getSource();

            if(comp.getName().equals("btnEndpoint"))
            {
                if(objectLabel == null) {
                    objectLabel = new ObjectInfoTransformOverlay();
                    objectInfo = new ObjectInfoOverlay();
                    currentInteractiveViewer2D.getJHotDrawDisplay().addOverlayRenderer(objectLabel);
                    objectInfo.updateInfo("Detecting started.", new Date().toString());
                    currentInteractiveViewer2D.getJHotDrawDisplay().addOverlayRenderer(objectInfo);

                    // Green channel picked-up
                    objectLabel.setObjectList(detectEndpoints(Views.hyperSlice(intervalView, 2, 1)));

                    currentInteractiveViewer2D.getJHotDrawDisplay().addMouseMotionListener(this);


                    currentInteractiveViewer2D.getJHotDrawDisplay().repaint();
                }
            }
        }
    }

    LinkedHashMap<Point, ObjectInfo> objectMap = new LinkedHashMap<Point, ObjectInfo>();

    public < T extends RealType< T > & NativeType< T >> ArrayList<ObjectInfo> detectEndpoints(IntervalView<T> v)
    {
        objectMap.clear();
        ArrayList<ObjectInfo> objLists = new ArrayList<ObjectInfo>();

        IterableInterval< T > input = Views.iterable(v);
        net.imglib2.Cursor< T > cursorInput = input.localizingCursor();

        T val;

        int i = 0;

        while(cursorInput.hasNext())
        {
            val = cursorInput.next();

            if(val.getRealDouble() > 0)
            {
                net.imglib2.Point position = new net.imglib2.Point( v.numDimensions() );
                position.setPosition(cursorInput);

                ObjectInfo info = new ObjectInfo(
                        position.getIntPosition(0),
                        position.getIntPosition(1),
                        "Endpoint-" + i);
//                System.out.print("" + position.getDoublePosition(0) + "," + position.getDoublePosition(1));
//                System.out.println(val.getRealDouble());
                objLists.add(info);
                objectMap.put(new Point(position.getIntPosition(0), position.getIntPosition(1)), info);
                i++;
            }
        }

        return objLists;
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        Point2D.Double p = currentInteractiveViewer2D.getJHotDrawDisplay().viewToDrawing(mouseEvent.getPoint());

        int x = (int)Math.round(p.getX());
        int y = (int)Math.round(p.getY());

        objectLabel.updateXY(x, y);
        if(objectMap.containsKey(new Point(x, y))) {
            ObjectInfo info = objectMap.get(new Point(x, y));
            objectInfo.updateInfo(info.Label, "" + x + ", " + y);
        }
        else {
            objectInfo.updateInfo("Mouse ", "" + x + ", " + y);
        }

        currentInteractiveViewer2D.getJHotDrawDisplay().repaint();
    }
}

