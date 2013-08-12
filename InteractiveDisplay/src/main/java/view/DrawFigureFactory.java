package view;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.GroupFigure;
import org.jhotdraw.draw.QuadTreeDrawing;
import org.jhotdraw.draw.connector.ChopBezierConnector;
import xml.DefaultDOMFactory;

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
