package model;

import edu.umd.cs.findbugs.annotations.Nullable;

import org.jhotdraw.draw.tool.CreationTool;
import org.jhotdraw.draw.tool.BezierTool;
import org.jhotdraw.draw.tool.TextCreationTool;
import org.jhotdraw.draw.tool.TextAreaCreationTool;
import org.jhotdraw.draw.tool.ImageTool;
import org.jhotdraw.draw.liner.ElbowLiner;
import org.jhotdraw.draw.liner.CurvedLiner;
import org.jhotdraw.draw.tool.ConnectionTool;
import org.jhotdraw.draw.decoration.ArrowTip;
import org.jhotdraw.gui.URIChooser;
import org.jhotdraw.util.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;

import org.jhotdraw.app.*;
import org.jhotdraw.draw.*;
import org.jhotdraw.draw.action.*;
import org.jhotdraw.gui.JFileURIChooser;
import org.jhotdraw.gui.filechooser.ExtensionFileFilter;

import controller.tool.SpimTool;
import view.InteractiveDisplayView;

/**
 * Created with IntelliJ IDEA.
 * User: moon
 * Date: 8/6/13
 * Time: 2:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class InteractiveDisplayApplicationModel extends DefaultApplicationModel {
    /**
     * This editor is shared by all views.
     */
    private DefaultDrawingEditor sharedEditor;

    /** Creates a new instance. */
    public InteractiveDisplayApplicationModel() {
    }

    public DefaultDrawingEditor getSharedEditor() {
        if (sharedEditor == null) {
            sharedEditor = new DefaultDrawingEditor();
        }
        return sharedEditor;
    }

    @Override
    public void initView(Application a,View p) {
        if (a.isSharingToolsAmongViews()) {
            ((InteractiveDisplayView) p).setEditor(getSharedEditor());
        }
    }

    /**
     * Creates toolbars for the application.
     * This class always returns an empty list. Subclasses may return other
     * values.
     */
    @Override
    public List<JToolBar> createToolBars(Application a, @Nullable View pr) {
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
        InteractiveDisplayView p = (InteractiveDisplayView) pr;

        DrawingEditor editor;
        if (p == null) {
            editor = getSharedEditor();
        } else {
            editor = p.getEditor();
        }

        LinkedList<JToolBar> list = new LinkedList<JToolBar>();
        JToolBar tb;
        tb = new JToolBar();


        addCreationButtonsTo(tb, editor);
        tb.setName(labels.getString("window.drawToolBar.title"));
        list.add(tb);

        return list;
    }

    private void addCreationButtonsTo(JToolBar tb, DrawingEditor editor) {
        addDefaultCreationButtonsTo(tb, editor,
                ButtonFactory.createDrawingActions(editor),
                ButtonFactory.createSelectionActions(editor));
    }

    public void addDefaultCreationButtonsTo(JToolBar tb, final DrawingEditor editor,
                                            Collection<Action> drawingActions, Collection<Action> selectionActions) {
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");

        ButtonFactory.addSelectionToolTo(tb, editor, drawingActions, selectionActions);

        HashMap< AttributeKey, Object > a = new HashMap< AttributeKey, Object >();
//		org.jhotdraw.draw.AttributeKeys.UNCLOSED_PATH_FILLED.put( a, false );
        org.jhotdraw.draw.AttributeKeys.FILL_COLOR.put( a, new Color( 0.0f, 1.0f, 0.0f, 0.1f ) );
        org.jhotdraw.draw.AttributeKeys.STROKE_COLOR.put( a, new Color( 1.0f, 0.0f, 0.0f, 0.33f ) );
//		org.jhotdraw.draw.AttributeKeys.STROKE_WIDTH.put( a, 2.5 );
        ButtonFactory.addToolTo(tb, editor, new BezierTool(new BezierFigure(true), a), "edit.createPolygon", labels);

        tb.addSeparator();

        ButtonFactory.addToolTo(tb, editor, new SpimTool(),  "edit.createSpim", labels);
    }

    @Override
    public URIChooser createOpenChooser(Application a, @Nullable View v) {
        JFileURIChooser c = new JFileURIChooser();
        c.addChoosableFileFilter(new ExtensionFileFilter("Drawing .xml","xml"));
        return c;
    }

    @Override
    public URIChooser createSaveChooser(Application a, @Nullable View v) {
        JFileURIChooser c = new JFileURIChooser();
        c.addChoosableFileFilter(new ExtensionFileFilter("Drawing .xml","xml"));
        return c;
    }
}
