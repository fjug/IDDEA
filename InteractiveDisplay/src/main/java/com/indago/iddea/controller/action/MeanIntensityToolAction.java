package com.indago.iddea.controller.action;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.view.Views;

import org.jhotdraw.app.Application;
import org.jhotdraw.app.View;
import org.jhotdraw.app.action.AbstractApplicationAction;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.util.ResourceBundleUtil;

import com.indago.iddea.view.display.InteractiveDisplayView;
import com.indago.iddea.view.viewer.InteractiveRealViewer;

/**
 * Compute mean intensity values for the highlighted selection region.
 *
 * @version 0.1beta
 * @since 8/20/13 3:54 PM
 * @author HongKee Moon
 */
public class MeanIntensityToolAction extends AbstractApplicationAction
{
    public final static String ID = "tool.computeMeanIntensity";

    /** Creates a new instance. */
    public MeanIntensityToolAction(final Application app) {
        this(app,ID);
    }
    public MeanIntensityToolAction(final Application app, final String id) {
        super(app);
        final ResourceBundleUtil labels = ResourceBundleUtil.getBundle("model.Labels");
        labels.configureAction(this, id);
    }


    @Override
    public void actionPerformed(final ActionEvent evt) {
        final Application app = getApplication();
        final View view = app.getActiveView();
        if (view != null) {
            final InteractiveRealViewer viewer = ((InteractiveDisplayView) view).getCurrentInteractiveViewer2D();

            final RealRandomAccessible<?> realSource = (RealRandomAccessible<?>) viewer.getSource();

            if(ARGBType.class.isInstance(realSource.realRandomAccess().get()))
            {
                final RandomAccessible< ARGBType > source = (RandomAccessible<ARGBType>) Views.raster(realSource);

                long size = 0;

                double r = 0;
                long rCnt = 0;
                double g = 0;
                long gCnt = 0;
                double b = 0;
                long bCnt = 0;

                final Set<Figure> figures = viewer.getJHotDrawDisplay().getSelectedFigures();
                for(final Figure f: figures)
                {
                    final Rectangle2D.Double rec = f.getBounds();

                    final RandomAccessibleInterval< ARGBType > viewSource = (RandomAccessibleInterval< ARGBType >) Views.offsetInterval(
                            source,
                            new long[]{ (long)rec.getX(), (long)rec.getY() },
                            new long[]{ (long)rec.getWidth(), (long)rec.getHeight() }
                    );

                    final Cursor<ARGBType> cur = Views.iterable(viewSource).localizingCursor();

                    while(cur.hasNext())
                    {
                        cur.fwd();

                        final Point2D.Double point = new Point2D.Double(cur.getDoublePosition(0) + rec.getX(), cur.getDoublePosition(1) + rec.getY());
                        if(f.contains(point))
                        {
                            final int pixel = cur.get().get();
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
            else if(LongType.class.isInstance(realSource.realRandomAccess().get()))
            {
                double sum = 0l;

                long size = 0;
                long boundarySize = 0;
                final Set<Figure> figures = viewer.getJHotDrawDisplay().getSelectedFigures();

                for(final Figure f: figures)
                {
                    final Rectangle2D.Double rec = f.getBounds();

                    System.out.format("X=%f, Y=%f, W=%f, H=%f\n", rec.getX(), rec.getY(), rec.getX() + rec.getWidth(), rec.getY() + rec.getHeight());

                    final Rectangle2D.Double viewRec = viewer.getJHotDrawDisplay().viewToOrigin(rec);

                    final double[] min = new double[] {viewRec.getX(), viewRec.getY()};
                    final double[] max = new double[] {viewRec.getX() + viewRec.getWidth(), viewRec.getY() + viewRec.getHeight()};

                    System.out.format("X=%f, Y=%f, W=%f, H=%f\n", min[0], min[1], max[0], max[1]);

                    final int numDimensions = 2;

                    // compute the number of pixels of the output and the size of the real interval
                    final double[] intervalSize = new double[numDimensions];
                    final double[] pixelSize = new double[]{rec.getWidth(), rec.getHeight()};
                    final double[] gaps = new double[numDimensions];

                    for ( int d = 0; d < numDimensions; ++d )
                    {
                        intervalSize[ d ] = max[d] - min[d];
                        gaps[d] = intervalSize[d] / pixelSize[d];
                    }

                    // create a RealRandomAccess on the source
                    final RealRandomAccess< LongType > realRandomAccess = (RealRandomAccess< LongType >)realSource.realRandomAccess();

                    for(int x = 0; x < rec.getWidth(); x++)
                    {
                        final double xpos = min[0] + x * gaps[0];
                        for(int y = 0; y < rec.getHeight(); y++)
                        {
                            final double ypos = min[1] + y * gaps[1];

                            realRandomAccess.setPosition(new double[]{xpos, ypos});
//                            System.out.format("DX=%f, DY=%f\n",  xpos, ypos);
                            final Point2D.Double pixel = viewer.getJHotDrawDisplay().originToView(xpos, ypos);
//                            System.out.format("CX=%f, CY=%f\n",  pixel.getX(), pixel.getY());

                            boundarySize++;
                            if(f.contains(pixel))
                            {
                                size++;
                                sum += realRandomAccess.get().getRealDouble();
                            }
                        }
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
                final RandomAccessible< RealType > source = (RandomAccessible<RealType>) Views.raster(realSource);

                double sum = 0d;

                long size = 0;
                long boundarySize = 0;
                final Set<Figure> figures = viewer.getJHotDrawDisplay().getSelectedFigures();

                for(final Figure f: figures)
                {
                    final Rectangle2D.Double rec = f.getBounds();

                    final RandomAccessibleInterval< RealType > viewSource = (RandomAccessibleInterval< RealType >) Views.offsetInterval( source,
                            new long[] { (long)rec.getX(), (long)rec.getY() }, new long[]{ (long)rec.getWidth(), (long)rec.getHeight() } );

                    System.out.format("X=%f, Y=%f, W=%f, H=%f\n", rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight());

                    final Cursor<RealType> cur = (Cursor<RealType>) Views.iterable(viewSource).localizingCursor();

                    while(cur.hasNext())
                    {
                        cur.fwd();

                        boundarySize++;
                        final Point2D.Double point = new Point2D.Double(cur.getDoublePosition(0) + rec.getX(), cur.getDoublePosition(1) + rec.getY());
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
