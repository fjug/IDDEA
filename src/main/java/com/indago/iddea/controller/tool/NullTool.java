package com.indago.iddea.controller.tool;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.tool.AbstractTool;

import com.indago.iddea.view.display.JHotDrawInteractiveDisplay2D;

/**
 * This tool does literally nothing.
 * We use this silly appearing tool e.g. when we want to interacti with the
 * transformation in the IddeaComponent without touching any existing
 * annotation.
 * 
 * @since 8/12/13 5:12 PM
 * @author HongKee Moon, Florian Jug
 */

public class NullTool extends AbstractTool {

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
    public void mouseClicked( final MouseEvent evt ) {
        if (evt.getClickCount() == 2) {
            // reset the transformation
            DrawingView view = this.editor.getActiveView();
            if ( JHotDrawInteractiveDisplay2D.class.isInstance( view ) )
            {
                ((JHotDrawInteractiveDisplay2D) view).resetTransform();
            }
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
