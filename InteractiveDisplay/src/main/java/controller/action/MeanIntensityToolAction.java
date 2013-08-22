package controller.action;

import net.imglib2.*;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.img.imageplus.ImagePlusImg;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.util.*;
import net.imglib2.Cursor;
import org.jhotdraw.gui.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.net.URI;
import java.util.Set;
import java.util.prefs.Preferences;
import org.jhotdraw.app.Application;
import org.jhotdraw.app.View;
import org.jhotdraw.app.action.AbstractApplicationAction;
import org.jhotdraw.gui.URIChooser;
import org.jhotdraw.net.URIUtil;
import org.jhotdraw.util.prefs.PreferencesUtil;
import org.jhotdraw.app.action.file.*;
import view.InteractiveDisplayView;
import view.InteractiveViewer2D;

/**
 * Created with IntelliJ IDEA.
 *
 * @version 0.1beta
 * @since 8/20/13 3:54 PM
 * @author HongKee Moon
 */
public class MeanIntensityToolAction extends AbstractApplicationAction
{
    public final static String ID = "tool.computeMeanIntensity";

    /** Creates a new instance. */
    public MeanIntensityToolAction(Application app) {
        this(app,ID);
    }
    public MeanIntensityToolAction(Application app, String id) {
        super(app);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("model.Labels");
        labels.configureAction(this, id);
    }


    @Override
    public void actionPerformed(ActionEvent evt) {
        final Application app = getApplication();
        View view = app.getActiveView();
        if (view != null) {
            InteractiveViewer2D viewer = ((InteractiveDisplayView) view).getIview2d();

            ImagePlusImg<?, ?> source = viewer.getSourceInterval();
            if(ARGBType.class.isInstance(source.firstElement()))
            {
                //net.imglib2.Cursor<ARGBType> cur = (net.imglib2.Cursor<ARGBType>)(net.imglib2.Cursor<?>) source.localizingCursor();

                System.out.println("Dim(0) aka. Width=" + source.dimension(0) + "\t Dim(1) aka. Height=" + source.dimension(1));
                System.out.println("Min(0)=" + source.min(0) + "\t Min(1)=" + source.min(1));
                System.out.println("Max(0)=" + source.max(0) + "\t Max(1)=" + source.max(1));

                long size = 0;

                double r = 0;
                long rCnt = 0;
                double g = 0;
                long gCnt = 0;
                double b = 0;
                long bCnt = 0;

                // Set position with the specific bounding box
                //cur.localize();

                // Basic naive version
//                Set<Figure> figures = viewer.getDisplay().getSelectedFigures();
//                for(Figure f: figures)
//                {
//                    net.imglib2.Cursor<ARGBType> cur = (net.imglib2.Cursor<ARGBType>)(net.imglib2.Cursor<?>) source.localizingCursor();
//                    while(cur.hasNext())
//                    {
//                        cur.next();
//                        Point2D.Double point = new Point2D.Double(cur.getDoublePosition(0), cur.getDoublePosition(1));
//                        if(f.contains(point))
//                        {
//                            size++;
//                            int pixel = cur.get().get();
//                            rCnt += (ARGBType.red(pixel) > 0)? 1 : 0;
//                            r += ARGBType.red(pixel);
//                            gCnt += (ARGBType.green(pixel) > 0)? 1 : 0;
//                            g += ARGBType.green(pixel);
//                            bCnt += (ARGBType.blue(pixel) > 0)? 1 : 0;
//                            b += ARGBType.blue(pixel);
//                        }
//                    }
//                }

                // Bounding box version(faster)
                Set<Figure> figures = viewer.getDisplay().getSelectedFigures();
                for(Figure f: figures)
                {
                    Rectangle2D.Double rec = f.getBounds();

                    RandomAccessibleInterval< ARGBType > viewSource = (RandomAccessibleInterval< ARGBType >) Views.offsetInterval( source,
                            new long[] { (long)rec.getX(), (long)rec.getY() }, new long[]{ (long)rec.getWidth(), (long)rec.getHeight() } );

                    Cursor<ARGBType> cur = Views.iterable(viewSource).localizingCursor();

                    while(cur.hasNext())
                    {
                        cur.fwd();

                        Point2D.Double point = new Point2D.Double(cur.getDoublePosition(0) + rec.getX(), cur.getDoublePosition(1) + rec.getY());
                        try{
                            if(f.contains(point))
                            {
                                int pixel = cur.get().get();
                                size++;
                                rCnt += (ARGBType.red(pixel) > 0)? 1 : 0;
                                r += ARGBType.red(pixel);
                                gCnt += (ARGBType.green(pixel) > 0)? 1 : 0;
                                g += ARGBType.green(pixel);
                                bCnt += (ARGBType.blue(pixel) > 0)? 1 : 0;
                                b += ARGBType.blue(pixel);
                            }
                        }
                        catch(ArrayIndexOutOfBoundsException exc)
                        {

                        }
                    }
                }

                System.out.println("Pixels: " + size);
                System.out.println("MeanIntensity(R) of selected regions: " + r / rCnt);
                System.out.println("MeanIntensity(G) of selected regions: " + g / gCnt);
                System.out.println("MeanIntensity(B) of selected regions: " + b / bCnt);
            }
            else
            {
                double sum = 0d;

                System.out.println("Dim(0) aka. Width=" + source.dimension(0) + "\t Dim(1) aka. Height=" + source.dimension(1));
                System.out.println("Min(0)=" + source.min(0) + "\t Min(1)=" + source.min(1));
                System.out.println("Max(0)=" + source.max(0) + "\t Max(1)=" + source.max(1));

                long size = 0;
                Set<Figure> figures = viewer.getDisplay().getSelectedFigures();

                for(Figure f: figures)
                {
                    Rectangle2D.Double rec = f.getBounds();

                    RandomAccessibleInterval< RealType > viewSource = (RandomAccessibleInterval< RealType >) Views.offsetInterval( source,
                            new long[] { (long)rec.getX(), (long)rec.getY() }, new long[]{ (long)rec.getWidth(), (long)rec.getHeight() } );

                    Cursor<RealType> cur = (Cursor<RealType>) Views.iterable(viewSource).localizingCursor();

                    while(cur.hasNext())
                    {
                        cur.fwd();

                        Point2D.Double point = new Point2D.Double(cur.getDoublePosition(0) + rec.getX(), cur.getDoublePosition(1) + rec.getY());
                        try{
                            if(f.contains(point))
                            {
                                sum += ((RealType<?>)cur.get()).getRealDouble();
                                size++;
                            }
                        }
                        catch(ArrayIndexOutOfBoundsException exc)
                        {

                        }
                    }
                }

                System.out.println("Pixels: " + size);
                System.out.println("MeanIntensity of selected regions: " + sum / size);
            }
        }
    }
}
