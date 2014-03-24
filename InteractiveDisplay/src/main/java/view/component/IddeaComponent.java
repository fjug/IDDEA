package view.component;

import controller.tool.SpimTool;
import edu.umd.cs.findbugs.annotations.Nullable;
import ij.ImagePlus;
import model.figure.DrawFigureFactory;
import model.source.MandelbrotRealRandomAccessible;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.TypeIdentity;
import net.imglib2.converter.RealARGBConverter;
import net.imglib2.exception.ImgLibException;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.imageplus.ImagePlusImg;
import net.imglib2.img.imageplus.ImagePlusImgs;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.ui.util.InterpolatingSource;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.jhotdraw.draw.*;
import org.jhotdraw.draw.action.ButtonFactory;
import org.jhotdraw.draw.io.DOMStorableInputOutputFormat;
import org.jhotdraw.draw.io.InputFormat;
import org.jhotdraw.draw.io.OutputFormat;
import org.jhotdraw.draw.tool.BezierTool;
import org.jhotdraw.gui.URIChooser;
import org.jhotdraw.net.URIUtil;
import org.jhotdraw.util.ResourceBundleUtil;
import view.converter.ColorTables;
import view.converter.LUTConverter;
import view.display.InteractiveDrawingView;
import view.viewer.InteractiveRealViewer;
import view.viewer.InteractiveRealViewer2D;
import view.viewer.InteractiveViewer2D;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import static org.jhotdraw.draw.AttributeKeys.STROKE_COLOR;

/**
 * Created with IntelliJ IDEA.
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/4/13
 */
public class IddeaComponent extends JPanel {

    private InteractiveRealViewer iview2d;
    private DrawingEditor editor;
    private String toolbarLocation;
    private boolean toolbarVisible = false;
    private Img img = null;

    public InteractiveRealViewer getIview2d() {
        return iview2d;
    }

    MandelbrotRealRandomAccessible mandelbrot;

    public InteractiveDrawingView getInteractiveDrawingView()
    {
        final int width = 800;
        final int height = 600;

        final int maxIterations = 100;
        mandelbrot = new MandelbrotRealRandomAccessible( maxIterations );

        final AffineTransform2D transform = new AffineTransform2D();
        transform.scale( 200 );
        transform.translate( width / 2.0, height / 2.0 );

        final LUTConverter< LongType > converter = new LUTConverter< LongType >( 0d, 50, ColorTables.FIRE);

        InteractiveRealViewer2D iview = new InteractiveRealViewer2D<LongType>(width, height, mandelbrot, transform, converter);
        iview2d = iview;

        return iview.getJHotDrawDisplay();
    }

    public < T extends RealType< T > & NativeType< T >> InteractiveDrawingView getInteractiveDrawingView(Img<T> img)
    {
        final int width = (int) img.max(0);
        final int height = (int) img.min(0);

        final AffineTransform2D transform = new AffineTransform2D();
        InteractiveViewer2D iview = null;

        System.out.println(img.firstElement().getClass());
        {
            final T min = Views.iterable( img ).firstElement().copy();
            final T max = min.copy();
            getMinMax( Views.iterable( img ), min, max );

            RealRandomAccessible< T > interpolated = Views.interpolate( Views.extendZero(img), new NearestNeighborInterpolatorFactory<T>() );
            //final RealARGBConverter< T > converter = new RealARGBConverter< T >( min.getMinValue(), max.getMaxValue());

            final LUTConverter< T > converter = new LUTConverter< T >( min.getMinValue(), max.getMaxValue(), ColorTables.FIRE);
            iview = new InteractiveViewer2D<T>(width, height, Views.extendZero(img), transform, converter);
        }

        iview2d = iview;
        return iview.getJHotDrawDisplay();
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

    public < T extends RealType< T > & NativeType< T >> IddeaComponent(Img<T> img) {

        this.img = img;
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
        ButtonFactory.addToolTo(tb, editor, new BezierTool(new BezierFigure(), foreground), "edit.scribbleForeground", labels);

        HashMap<AttributeKey, Object> background = new HashMap< AttributeKey, Object>();
        org.jhotdraw.draw.AttributeKeys.STROKE_COLOR.put( background, new Color( 0.0f, 0.0f, 1.0f, 0.33f) );
        ButtonFactory.addToolTo(tb, editor, new BezierTool(new BezierFigure(), background), "edit.scribbleBackground", labels);

        tb.add(ButtonFactory.createStrokeWidthButton(
                editor,
                new double[]{5d, 10d, 15d},
                ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels")));
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

        if(img == null)
            view = getInteractiveDrawingView();
        else
            view = getInteractiveDrawingView(img);

        setLayout(new java.awt.BorderLayout());

        scrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setViewportView(view);

        add(scrollPane, java.awt.BorderLayout.CENTER);

        if(toolbarVisible)
        {
            add(tb, toolbarLocation);
        }
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JToolBar tb;
    private JScrollPane scrollPane;
    private InteractiveDrawingView view;

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

    public void setImg(Img img) {
        iview2d = show(img);

        editor.remove(view);
        InteractiveDrawingView newView = getIview2d().getJHotDrawDisplay();
        newView.copyFrom(view);

        editor.add(newView);
        view = newView;

        scrollPane.setViewportView(view);
    }

    private static < T extends Type< T > & Comparable< T > > void getMinMax( final IterableInterval< T > source, final T minValue, final T maxValue )
    {
        for ( final T t : source )
            if ( minValue.compareTo( t ) > 0 )
                minValue.set( t );
            else if ( maxValue.compareTo( t ) < 0 )
                maxValue.set( t );
    }

    public < T extends RealType< T > & NativeType< T >> InteractiveViewer2D show( final Img<T> interval ) {
        final AffineTransform2D transform = new AffineTransform2D();
        InteractiveViewer2D iview = null;

        System.out.println(interval.firstElement().getClass());

        {
            final T min = Views.iterable( interval ).firstElement().copy();
            final T max = min.copy();
            getMinMax( Views.iterable( interval ), min, max );

//            RealRandomAccessible< T > interpolated = Views.interpolate( interval, new NLinearInterpolatorFactory<T>() );
            RealRandomAccessible< T > interpolated = Views.interpolate( Views.extendZero(interval), new NearestNeighborInterpolatorFactory<T>() );
            //final RealARGBConverter< T > converter = new RealARGBConverter< T >( min.getMinValue(), max.getMaxValue());

            final LUTConverter< T > converter = new LUTConverter< T >( min.getMinValue(), max.getMaxValue(), ColorTables.FIRE);
            iview = new InteractiveViewer2D<T>((int)interval.max(0), (int)interval.max(1), Views.extendZero(interval), transform, converter);
        }

        return iview;
    }

    public void setPreferredSize(Dimension dim)
    {
        iview2d.getJHotDrawDisplay().setPreferredSize(dim);
    }
    // End of variables declaration//GEN-END:variables

}
