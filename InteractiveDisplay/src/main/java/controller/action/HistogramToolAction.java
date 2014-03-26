package controller.action;

/**
 * Created with IntelliJ IDEA.
 *
 * @version 0.1beta
 * @since 3/25/14 1:29 PM
 * @author HongKee Moon
 */

import controller.plot.HistogramPlot;
import net.imglib2.*;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.jhotdraw.app.Application;
import org.jhotdraw.app.View;
import org.jhotdraw.app.action.AbstractApplicationAction;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.util.ResourceBundleUtil;
import view.display.InteractiveDisplayView;
import view.viewer.InteractiveRealViewer;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by moon on 25/03/14.
 */
public class HistogramToolAction extends AbstractApplicationAction
{
    public final static String ID = "tool.computeHistogram";

    /** Creates a new instance. */
    public HistogramToolAction(Application app) {
        this(app,ID);
    }
    public HistogramToolAction(Application app, String id) {
        super(app);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("model.Labels");
        labels.configureAction(this, id);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        final Application app = getApplication();
        View view = app.getActiveView();
        if (view != null) {
            InteractiveRealViewer viewer = ((InteractiveDisplayView) view).getCurrentInteractiveViewer2D();

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

                Set<Figure> figures = viewer.getJHotDrawDisplay().getSelectedFigures();
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
                Set<Figure> figures = viewer.getJHotDrawDisplay().getSelectedFigures();

                ArrayList<Long> list = new ArrayList<Long>();

                for(Figure f: figures)
                {
                    Rectangle2D.Double rec = f.getBounds();

                    System.out.format("X=%f, Y=%f, W=%f, H=%f\n", rec.getX(), rec.getY(), rec.getX() + rec.getWidth(), rec.getY() + rec.getHeight());

                    Rectangle2D.Double viewRec = viewer.getJHotDrawDisplay().viewToOrigin(rec);

                    double[] min = new double[] {viewRec.getX(), viewRec.getY()};
                    double[] max = new double[] {viewRec.getX() + viewRec.getWidth(), viewRec.getY() + viewRec.getHeight()};

                    System.out.format("X=%f, Y=%f, W=%f, H=%f\n", min[0], min[1], max[0], max[1]);

                    int numDimensions = 2;

                    // compute the number of pixels of the output and the size of the real interval
                    double[] intervalSize = new double[numDimensions];
                    double[] pixelSize = new double[]{rec.getWidth(), rec.getHeight()};
                    double[] gaps = new double[numDimensions];

                    for ( int d = 0; d < numDimensions; ++d )
                    {
                        intervalSize[ d ] = max[d] - min[d];
                        gaps[d] = intervalSize[d] / pixelSize[d];
                    }

                    // create a RealRandomAccess on the source
                    RealRandomAccess< LongType > realRandomAccess = (RealRandomAccess< LongType >)realSource.realRandomAccess();

                    for(int x = 0; x < rec.getWidth(); x++)
                    {
                        double xpos = min[0] + x * gaps[0];
                        for(int y = 0; y < rec.getHeight(); y++)
                        {
                            double ypos = min[1] + y * gaps[1];

                            realRandomAccess.setPosition(new double[]{xpos, ypos});
//                            System.out.format("DX=%f, DY=%f\n",  xpos, ypos);
                            Point2D.Double pixel = viewer.getJHotDrawDisplay().originToView(xpos, ypos);
//                            System.out.format("CX=%f, CY=%f\n",  pixel.getX(), pixel.getY());

                            boundarySize++;
                            if(f.contains(pixel))
                            {
                                size++;
                                Double d = realRandomAccess.get().getRealDouble();
                                //System.out.println(d);
                                sum += d;
                                list.add(d.longValue());
                            }
                        }
                    }
                }
                System.out.println("LongType");
                System.out.println("Pixels: " + size);
                System.out.println("Boundary Pixels: " + boundarySize);
                System.out.println("Sum: " + sum);
                System.out.println("MeanIntensity of selected regions: " + sum / size);

                HistogramPlot plot = new HistogramPlot(list);
            }
            else
            {
                RandomAccessible<RealType> source = (RandomAccessible<RealType>) Views.raster(realSource);

                double sum = 0d;

                long size = 0;
                long boundarySize = 0;
                Set<Figure> figures = viewer.getJHotDrawDisplay().getSelectedFigures();

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
