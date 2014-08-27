package model.figure;

import model.xml.DefaultDOMFactory;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.GroupFigure;
import org.jhotdraw.draw.QuadTreeDrawing;
import org.jhotdraw.draw.LineFigure;
import org.jhotdraw.draw.EllipseFigure;
import org.jhotdraw.draw.TriangleFigure;
import org.jhotdraw.draw.RoundRectangleFigure;
import org.jhotdraw.draw.TextFigure;
import org.jhotdraw.draw.RectangleFigure;
import org.jhotdraw.draw.TextAreaFigure;
import org.jhotdraw.draw.ImageFigure;

/**
 * DrawFingureFactory provides how to store DOM model for the drawing objects.
 * Color store information is added.
 *
 * @version 0.1beta
 * @since 8/12/13 5:03 PM
 * @author HongKee Moon
 */

public class DrawFigureFactory extends DefaultDOMFactory {
    private final static Object[][] classTagArray = {
        { DefaultDrawing.class, "drawing" },
        { QuadTreeDrawing.class, "drawing" },
        { BezierFigure.class, "b" },
        { BezierFigure.class, "bezier" },
        { GroupFigure.class, "g" },
        { java.awt.Color.class, "color" },	// for storing color.
        { LineFigure.class, "l" },
        { EllipseFigure.class, "e" },
        { TriangleFigure.class, "triangle" },
        { RectangleFigure.class, "r" },
        { RoundRectangleFigure.class, "rr" },
        { TextFigure.class, "t" },
        { TextAreaFigure.class, "ta" },
        { ImageFigure.class, "image" },
    };
    
    private final static Object[][] enumTagArray = {
        { AttributeKeys.StrokePlacement.class, "strokePlacement" },
        { AttributeKeys.StrokeType.class, "strokeType" },
        { AttributeKeys.Underfill.class, "underfill" },
        { AttributeKeys.Orientation.class, "orientation" },
    };
    
    /** Creates a new instance. */
    public DrawFigureFactory() {
        for (Object[] o : classTagArray) {
            addStorableClass((String) o[1], (Class) o[0]);
        }
        for (Object[] o : enumTagArray) {
            addEnumClass((String) o[1], (Class) o[0]);
        }
    }
}
