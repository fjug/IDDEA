package view;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.DiamondFigure;
import org.jhotdraw.draw.EllipseFigure;
import org.jhotdraw.draw.GroupFigure;
import org.jhotdraw.draw.ImageFigure;
import org.jhotdraw.draw.LineConnectionFigure;
import org.jhotdraw.draw.LineFigure;
import org.jhotdraw.draw.QuadTreeDrawing;
import org.jhotdraw.draw.RectangleFigure;
import org.jhotdraw.draw.RoundRectangleFigure;
import org.jhotdraw.draw.TextAreaFigure;
import org.jhotdraw.draw.TextFigure;
import org.jhotdraw.draw.TriangleFigure;
import org.jhotdraw.draw.connector.ChopBezierConnector;
import org.jhotdraw.draw.connector.ChopDiamondConnector;
import org.jhotdraw.draw.connector.ChopEllipseConnector;
import org.jhotdraw.draw.connector.ChopRectangleConnector;
import org.jhotdraw.draw.connector.ChopRoundRectangleConnector;
import org.jhotdraw.draw.connector.ChopTriangleConnector;
import org.jhotdraw.draw.decoration.ArrowTip;
import org.jhotdraw.draw.liner.CurvedLiner;
import org.jhotdraw.draw.liner.ElbowLiner;
import org.jhotdraw.xml.DefaultDOMFactory;

public class DrawFigureFactory extends DefaultDOMFactory {
    private final static Object[][] classTagArray = {
        { DefaultDrawing.class, "drawing" },
        { QuadTreeDrawing.class, "drawing" },
        { BezierFigure.class, "b" },
        { BezierFigure.class, "bezier" },
        { GroupFigure.class, "g" },
        { java.awt.Color.class, "color" },
        
        { ChopBezierConnector.class, "bezierConnector" },        
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
