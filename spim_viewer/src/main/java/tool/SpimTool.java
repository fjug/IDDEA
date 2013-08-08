package tool;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import net.imglib2.Positionable;
import net.imglib2.util.ValuePair;
import net.imglib2.ui.TransformEventHandler3D;

import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.tool.AbstractTool;

public class SpimTool extends AbstractTool {

	@Override
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
		if ( viewer.SpimDrawingView.class.isInstance( view ) )
		{
			((viewer.SpimDrawingView) view).ActivateMouseHandlers();
		}
			
    }

    @Override
    public void deactivate(DrawingEditor editor) {
    	super.deactivate(editor);
//    	System.out.println("Deactivated.");
//    	System.out.println(isActive());   	
    	
    	DrawingView view = this.editor.getActiveView();
		//System.out.println(view);
		if ( viewer.SpimDrawingView.class.isInstance( view ) )
		{
			((viewer.SpimDrawingView) view).DeactivateMouseHandlers();
		}
    }
}
