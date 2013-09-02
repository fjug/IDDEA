package controller.tool;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.tool.AbstractTool;
import view.display.JHotDrawInteractiveDisplay2D;


/**
 * SpimTool provides a default bezier drawing.
 *
 * @version 0.1beta
 * @since 8/12/13 5:12 PM
 * @author HongKee Moon
 */

public class SpimTool extends AbstractTool {

    public void mouseDragged(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void activate(DrawingEditor editor) {
        super.activate(editor);
//    	System.out.println("Activated.");
//    	System.out.println(isActive());

        DrawingView view = this.editor.getActiveView();
        //System.out.println(view);
        if ( JHotDrawInteractiveDisplay2D.class.isInstance( view ) )
        {
            ((JHotDrawInteractiveDisplay2D) view).activateHandler();
        }

    }

    @Override
    public void deactivate(DrawingEditor editor) {
        super.deactivate(editor);
//    	System.out.println("Deactivated.");
//    	System.out.println(isActive());

        DrawingView view = this.editor.getActiveView();
        //System.out.println(view);
        if ( JHotDrawInteractiveDisplay2D.class.isInstance( view ) )
        {
            ((JHotDrawInteractiveDisplay2D) view).deactivateHandler();
        }
    }

    @Override
    public void keyPressed(KeyEvent evt) {
    }

    @Override
    public void keyReleased(KeyEvent evt) {
    }

    @Override
    public void keyTyped(KeyEvent evt) {
    }
}
