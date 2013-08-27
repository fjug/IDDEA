package controller.action;

import net.imglib2.*;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.img.imageplus.ImagePlusImg;
import net.imglib2.realtransform.AffineTransform;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.InverseRealTransform;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.ui.AffineTransformType;
import net.imglib2.ui.AffineTransformType2D;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
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
import view.viewer.InteractiveRealViewer;
//import view.InteractiveViewer2D;

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
            InteractiveRealViewer viewer = ((InteractiveDisplayView) view).getIview2d();

            RealRandomAccessible<?> realSource = (RealRandomAccessible<?>) viewer.getSource();

            if(ARGBType.class.isInstance(Util.getTypeFromRealRandomAccess(realSource)))
            {
                RandomAccessible< ARGBType > source = (RandomAccessible<ARGBType>) Views.raster(realSource);

                long size = 0;

                double r = 0;
                long rCnt = 0;
                double g = 0;
                long gCnt = 0;
                double b = 0;
                long bCnt = 0;

                Set<Figure> figures = viewer.getDisplay().getSelectedFigures();
                for(Figure f: figures)
                {
                    Rectangle2D.Double rec = f.getBounds();

                    RandomAccessibleInterval< ARGBType > viewSource = (RandomAccessibleInterval< ARGBType >) Views.offsetInterval(
                            source,
                            new long[]{ (long)rec.getX(), (long)rec.getY() },
                            new long[]{ (long)rec.getWidth(), (long)rec.getHeight() }
                    );

                    Cursor<ARGBType> cur = Views.iterable(viewSource).localizingCursor();

                    while(cur.hasNext())
                    {
                        cur.fwd();

                        Point2D.Double point = new Point2D.Double(cur.getDoublePosition(0) + rec.getX(), cur.getDoublePosition(1) + rec.getY());
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
                }
                System.out.println("ARGBType");
                System.out.println("Pixels: " + size);
                System.out.println("MeanIntensity(R) of selected regions: " + r / rCnt);
                System.out.println("MeanIntensity(G) of selected regions: " + g / gCnt);
                System.out.println("MeanIntensity(B) of selected regions: " + b / bCnt);
            }
            else if(LongType.class.isInstance(Util.getTypeFromRealRandomAccess(realSource)))
            {
                double sum = 0l;

                long size = 0;
                long boundarySize = 0;
                Set<Figure> figures = viewer.getDisplay().getSelectedFigures();

                for(Figure f: figures)
                {
                    Rectangle2D.Double rec = f.getBounds();

                    Point2D.Double start = viewer.getDisplay().viewToOrigin(new Point2D.Double(rec.getX(), rec.getY()));
                    Point2D.Double end = viewer.getDisplay().viewToOrigin(new Point2D.Double(rec.getX() + rec.getWidth(), rec.getY() + rec.getHeight()));

                    double[] min = new double[] {start.x, start.y};
                    double[] max = new double[] {end.x, end.y};

                    System.out.format("X=%f, Y=%f, W=%f, H=%f\n", min[0], min[1], max[0], max[1]);
                    FinalRealInterval interval = new FinalRealInterval( min, max );

                    int numDimensions = interval.numDimensions();

                    // compute the number of pixels of the output and the size of the real interval
                    long[] pixelSize = new long[ numDimensions ];
                    double[] intervalSize = new double[ numDimensions ];

                    for ( int d = 0; d < numDimensions; ++d )
                    {
                        intervalSize[ d ] = interval.realMax( d ) - interval.realMin( d );
                        pixelSize[ d ] = Math.round( intervalSize[ d ] * 200 ) + 1;
                    }

                    // create the output image
                    ImgFactory< LongType > factory= new ArrayImgFactory< LongType >();
                    Img< LongType > out = factory.create( pixelSize, Util.getTypeFromRealRandomAccess( (RealRandomAccessible<LongType>)realSource ) );

                    // cursor to iterate over all pixels
                    Cursor< LongType > cursor = out.localizingCursor();

                    // create a RealRandomAccess on the source (interpolator)
                    RealRandomAccess< LongType > realRandomAccess = (RealRandomAccess< LongType >)realSource.realRandomAccess();

                    // the temporary array to compute the position
                    double[] tmp = new double[ numDimensions ];

                    while ( cursor.hasNext() )
                    {
                        cursor.fwd();

                        // compute the appropriate location of the interpolator
                        for ( int d = 0; d < numDimensions; ++d )
                        {
                            tmp[d] += cursor.getDoublePosition( d ) / out.realMax( d ) * intervalSize[ d ]
                                    + interval.realMin( d );
                        }

                        Point2D.Double pixel = viewer.getDisplay().originToView(cursor.getDoublePosition( 0 ), cursor.getDoublePosition( 1 ));
                        System.out.format("CX=%f, CY=%f\n",  pixel.getX(), pixel.getY());
//                        if(f.contains(pixel))
//                        {
                            size++;

                            // set the position
                            realRandomAccess.setPosition( tmp );

                            // set the new value
                            sum += realRandomAccess.get().getRealDouble();
//                        }
                    }
                }
                System.out.println("LongType");
                System.out.println("Pixels: " + size);
                System.out.println("Boundary Pixels: " + boundarySize);
                System.out.println("Sum: " + sum);
                System.out.println("MeanIntensity of selected regions: " + sum / size);
            }
            else
            {
                RandomAccessible< RealType > source = (RandomAccessible<RealType>) Views.raster(realSource);

                double sum = 0d;

                long size = 0;
                long boundarySize = 0;
                Set<Figure> figures = viewer.getDisplay().getSelectedFigures();

                for(Figure f: figures)
                {
                    Rectangle2D.Double rec = f.getBounds();

                    RandomAccessibleInterval< RealType > viewSource = (RandomAccessibleInterval< RealType >) Views.offsetInterval( source,
                            new long[] { (long)rec.getX(), (long)rec.getY() }, new long[]{ (long)rec.getWidth(), (long)rec.getHeight() } );

                    System.out.format("X=%f, Y=%f, W=%f, H=%f\n", rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight());

                    Cursor<RealType> cur = (Cursor<RealType>) Views.iterable(viewSource).localizingCursor();

                    while(cur.hasNext())
                    {
                        cur.fwd();

                        boundarySize++;
                        Point2D.Double point = new Point2D.Double(cur.getDoublePosition(0) + rec.getX(), cur.getDoublePosition(1) + rec.getY());
                        //System.out.format("CX=%f, CY=%f\n", cur.getDoublePosition(0) + rec.getX(), cur.getDoublePosition(1) + rec.getY());

                        if(f.contains(point))
                        {
                            sum += cur.get().getRealDouble();
                            size++;
                        }
                    }
                }
                System.out.println("RealType");
                System.out.println("Pixels: " + size);
                System.out.println("Boundary Pixels: " + boundarySize);
                System.out.println("MeanIntensity of selected regions: " + sum / size);
            }
        }
    }
}
