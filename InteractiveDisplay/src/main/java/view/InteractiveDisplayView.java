package view;

import edu.umd.cs.findbugs.annotations.Nullable;
import ij.ImagePlus;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.ui.util.FinalSource;
import net.imglib2.img.imageplus.*;

import net.imglib2.view.Views;
import org.jhotdraw.draw.io.OutputFormat;
import org.jhotdraw.draw.io.InputFormat;
import org.jhotdraw.draw.print.DrawingPageable;
import org.jhotdraw.draw.io.DOMStorableInputOutputFormat;

import java.awt.print.Pageable;

import org.jhotdraw.gui.*;
import org.jhotdraw.undo.*;
import org.jhotdraw.util.*;

import java.awt.*;
import java.beans.*;
import java.io.*;
import java.net.URI;

import javax.swing.*;
import javax.swing.border.*;

import org.jhotdraw.app.AbstractView;
import org.jhotdraw.app.action.edit.RedoAction;
import org.jhotdraw.app.action.edit.UndoAction;
import org.jhotdraw.draw.*;
import org.jhotdraw.draw.action.*;
import org.jhotdraw.gui.URIChooser;
import org.jhotdraw.net.URIUtil;
import view.ui.ARGBRealConverter;
import view.ui.RealARGBConverter;
import view.ui.RealARGBConverterDecode;


/**
 * InteractvieDisplayView shows a DrawingView where users create and modify figures.
 *
 * @version 0.1beta
 * @since 8/12/13 5:05 PM
 * @author HongKee Moon
 */

public class InteractiveDisplayView extends AbstractView {

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

    private InteractiveViewer2D iview2d;

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

        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");

        JPanel placardPanel = new JPanel(new BorderLayout());
        javax.swing.AbstractButton pButton;
        pButton = ButtonFactory.createZoomButton(view);
        pButton.putClientProperty("Quaqua.Button.style","placard");
        pButton.putClientProperty("Quaqua.Component.visualMargin",new Insets(0,0,0,0));
        pButton.setFont(UIManager.getFont("SmallSystemFont"));
        placardPanel.add(pButton, BorderLayout.WEST);
        pButton = ButtonFactory.createToggleGridButton(view);
        pButton.putClientProperty("Quaqua.Button.style","placard");
        pButton.putClientProperty("Quaqua.Component.visualMargin",new Insets(0,0,0,0));
        pButton.setFont(UIManager.getFont("SmallSystemFont"));
        labels.configureToolBarButton(pButton, "view.toggleGrid.placard");
        placardPanel.add(pButton, BorderLayout.EAST);
        scrollPane.add(placardPanel, JScrollPane.LOWER_LEFT_CORNER);
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
            final ImagePlus imp = new ImagePlus( filename );
            ImagePlusImg<FloatType, ? > map = ImagePlusImgs.from( imp );
            if(iview2d != null)
            {
                iview2d.stop();

            }
            iview2d = show(map);

            editor.remove(view);
            InteractiveDrawingView newView = iview2d.getDisplay();
            newView.copyFrom(view);

            editor.add(newView);
            view = newView;

            scrollPane.setViewportView(view);
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

    public < T extends RealType< T > & NativeType< T >> InteractiveViewer2D show( final ImagePlusImg<T, ? > interval )
    {
        final AffineTransform2D transform = new AffineTransform2D();
        InteractiveViewer2D iview = null;

        if(ARGBType.class.isInstance(interval.firstElement()))
        {
            Converter<ARGBType, FloatType> conv = new ARGBRealConverter();
            RandomAccessible< FloatType > view1 = Converters.convert((RandomAccessibleInterval<ARGBType>)(ImagePlusImg<?, ?>)interval, conv, new FloatType());

//            RealRandomAccessible< FloatType > interpolated = Views.interpolate( view1, new NLinearInterpolatorFactory<FloatType>() );
            RealRandomAccessible< FloatType > interpolated = Views.interpolate( view1, new NearestNeighborInterpolatorFactory<FloatType>() );

            final RealARGBConverterDecode< FloatType > converter = new RealARGBConverterDecode< FloatType >();

            iview = new InteractiveViewer2D(interval, new FinalSource< FloatType, AffineTransform2D >(interpolated, transform, converter));
        }
        else
        {
            final T min = Views.iterable( interval ).firstElement().copy();
            final T max = min.copy();
            getMinMax( Views.iterable( interval ), min, max );

//            RealRandomAccessible< T > interpolated = Views.interpolate( interval, new NLinearInterpolatorFactory<T>() );
            RealRandomAccessible< T > interpolated = Views.interpolate( interval, new NearestNeighborInterpolatorFactory<T>() );
            final RealARGBConverter< T > converter = new RealARGBConverter< T >( min.getMinValue(), max.getMaxValue());

            iview = new InteractiveViewer2D(interval, new FinalSource< T, AffineTransform2D >( interpolated, transform, converter));
        }

        return iview;
    }

    public InteractiveDrawingView getInteractiveDrawingView()
    {
        final int width = 800;
        final int height = 600;

        final int maxIterations = 100;
        final MandelbrotRealRandomAccessible mandelbrot = new MandelbrotRealRandomAccessible( maxIterations );

        final AffineTransform2D transform = new AffineTransform2D();
        transform.scale( 200 );
        transform.translate( width / 2.0, height / 2.0 );

        final RealARGBConverter< LongType > converter = new RealARGBConverter< LongType >( 0, maxIterations );

        InteractiveViewer2D iview = new InteractiveViewer2D( width, height, new FinalSource<LongType, AffineTransform2D >( mandelbrot, transform, converter ) );


//        String filename = new String("/Users/moon/Pictures/aeonflux.jpeg");
//
//        // Image import
//        final ImagePlus imp = new ImagePlus( filename );
//        ImagePlusImg<FloatType, ? > map = ImagePlusImgs.from( imp );
//
//        int width = map.getWidth();
//        int height = map.getHeight();
//
//        ImageRandomAccessible img = new ImageRandomAccessible((ImagePlusImg<ARGBType, ?>)(ImagePlusImg<?, ?>)map);
//        final AffineTransform2D transform = new AffineTransform2D();
//
//        InteractiveViewer2D iview = new InteractiveViewer2D( width, height, new ImageSource< ARGBType, AffineTransform2D >( img, transform, new TypeIdentity<ARGBType>()));
//


//        String filename = new String("/Users/moon/Pictures/aeonflux.jpeg");
////        String filename = new String("/Users/moon/Projects/ScientificPlatform/ImgLib2/imglib/examples/graffiti.tif");
//        final ImagePlus imp = new ImagePlus( filename );
//        ImagePlusImg<FloatType, ? > map = ImagePlusImgs.from( imp );
//
//        InteractiveViewer2D iview = show(map);
//
        return iview.getDisplay();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        scrollPane = new javax.swing.JScrollPane();

        JSlider sliderTime = new JSlider( JSlider.HORIZONTAL, 0, 10 - 1, 0 );
        view = getInteractiveDrawingView();

        setLayout(new java.awt.BorderLayout());

        scrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setViewportView(view);

        add(scrollPane, java.awt.BorderLayout.CENTER);
        add(sliderTime, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPane;
    private InteractiveDrawingView view;
    // End of variables declaration//GEN-END:variables

}

