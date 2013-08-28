package controller.action;

import net.imglib2.RealRandomAccess;
import net.imglib2.img.imageplus.ImagePlusImg;
import net.imglib2.type.numeric.ARGBType;

import org.jhotdraw.app.Application;
import org.jhotdraw.app.View;
import org.jhotdraw.app.action.AbstractApplicationAction;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.geom.BezierPath;
import org.jhotdraw.util.ResourceBundleUtil;
import org.jhotdraw.draw.BezierFigure;

import view.InteractiveDisplayView;
import view.viewer.InteractiveRealViewer;
//import view.InteractiveViewer2D;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

/**
 * Grow the selection boundary example.
 *
 * @version 0.1beta
 * @since 8/22/13 4:43 PM
 * @author HongKee Moon
 */
public class GrowSelectionToolAction extends AbstractApplicationAction
{
    public final static String ID = "tool.growSelection";

    public GrowSelectionToolAction(Application app) {
        this(app, ID);
    }

    public GrowSelectionToolAction(Application app, String id) {
        super(app);
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("model.Labels");
        labels.configureAction(this, id);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Application app = getApplication();
        View view = app.getActiveView();
        if (view != null) {
            process(((InteractiveDisplayView) view).getIview2d());
        }
    }

    private void process(InteractiveRealViewer viewer)
    {
        //ImagePlusImg<?, ?> source = viewer.getSourceInterval();
        RealRandomAccess<?> source = (RealRandomAccess<?>) viewer.getSource();
        if(ARGBType.class.isInstance(source.get()))
        {
            Set<Figure> figures = viewer.getDisplay().getSelectedFigures();
            for(Figure f: figures)
            {
                f.willChange();
                if(BezierFigure.class.isInstance(f))
                {
                    BezierFigure bf = (BezierFigure) f;

//                    AffineTransform tran = new AffineTransform();
//                    tran.translate(5,5);
//                    f.transform(tran);
                    Rectangle2D.Double rec = bf.getBounds();

                    for(int i = 0 ; i < bf.getNodeCount(); i++)
                    {
                        BezierPath.Node n = bf.getNode(i);
                        Point2D.Double pos = n.getControlPoint(0);
                        double x = 10, y = 10;
                        if(pos.x < rec.getCenterX())
                            x *= -1;

                        if(pos.y < rec.getCenterY())
                            y *= -1;

                        n.moveBy(x, y);
                        bf.setNode(i, n);
//                        Point2D.Double pos = n.getControlPoint(0);
//                        bf.setPoint(i, new Point2D.Double(pos.x + 10, pos.y + 10));
                    }
                }
                f.changed();

            }
            viewer.getDisplay().repaint();

        }
        else
        {
        }
    }
}
