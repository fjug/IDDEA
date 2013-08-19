package model;

import edu.umd.cs.findbugs.annotations.Nullable;

import org.jhotdraw.app.action.file.*;
import org.jhotdraw.app.action.edit.*;
import org.jhotdraw.draw.tool.BezierTool;
import org.jhotdraw.gui.URIChooser;
import org.jhotdraw.util.*;

import java.awt.Color;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jhotdraw.app.*;
import org.jhotdraw.draw.*;
import org.jhotdraw.draw.action.*;
import org.jhotdraw.gui.JFileURIChooser;
import org.jhotdraw.gui.filechooser.ExtensionFileFilter;

import controller.tool.SpimTool;
import controller.action.OpenFileAction;
import controller.action.*;
import view.InteractiveDisplayView;

/**
 * InteractiveDisplay application model class provides an application model.
 * Java initiates it with InteractiveDisplay view in Main class.
 *
 * @version 0.1beta
 * @since 2:45 PM 8/6/13
 * @author HongKee Moon
 */

public class InteractiveDisplayApplicationModel extends AbstractApplicationModel {
    @Nullable private MenuBuilder menuBuilder;

    /**
     * This editor is shared by all views.
     */
    private DefaultDrawingEditor sharedEditor;

    /** Creates a new instance. */
    public InteractiveDisplayApplicationModel() {
    }

    /**
     * Returns an {@code ActionMap} with a default set of actions (See
     * class comments).
     */
    @Override
    public ActionMap createActionMap(Application a, @Nullable View v) {
        ActionMap m=new ActionMap();

        m.put(NewFileAction.ID, new NewFileAction(a));
        m.put(OpenFileAction.ID, new controller.action.OpenFileAction(a));
        m.put(SaveFileAction.ID, new SaveFileAction(a,v));
        m.put(SaveFileAsAction.ID, new SaveFileAsAction(a,v));
        m.put(CloseFileAction.ID, new CloseFileAction(a,v));

        m.put(UndoAction.ID, new UndoAction(a,v));
        m.put(RedoAction.ID, new RedoAction(a,v));
        m.put(CutAction.ID, new CutAction());
        m.put(CopyAction.ID, new CopyAction());
        m.put(PasteAction.ID, new PasteAction());
        m.put(DeleteAction.ID, new DeleteAction());
        m.put(DuplicateAction.ID, new DuplicateAction());
        m.put(SelectAllAction.ID, new SelectAllAction());
        m.put(ClearSelectionAction.ID, new ClearSelectionAction());

        //m.put(OpenImageFileAction.ID, new OpenImageFileAction(a));

        return m;
    }

    /** Creates the DefaultMenuBuilder. */
    protected MenuBuilder createMenuBuilder() {
        return new InteractiveDisplayMenuBuilder();
    }

    @Override
    public MenuBuilder getMenuBuilder() {
        if (menuBuilder==null) {
            menuBuilder=createMenuBuilder();
        }
        return menuBuilder;
    }

    public void setMenuBuilder(@Nullable MenuBuilder newValue) {
        menuBuilder = newValue;
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

        labels = ResourceBundleUtil.getBundle("model.Labels");
        ButtonFactory.addToolTo(tb, editor, new SpimTool(),  "edit.createSpim", labels);
    }

    @Override
    public URIChooser createOpenChooser(Application a, @Nullable View v) {
        JFileURIChooser c = new JFileURIChooser();
        ExtensionFileFilter defaultFilter = new ExtensionFileFilter("Drawing .xml","xml");
        c.addChoosableFileFilter(defaultFilter);
        c.addChoosableFileFilter(new ExtensionFileFilter("TIFF", new String[] {"tif","tiff"}));
        c.addChoosableFileFilter(new ExtensionFileFilter("JPEG", new String[] {"jpg","jpeg"}));
        c.addChoosableFileFilter(new ExtensionFileFilter("PNG", "png"));

        c.setFileFilter(defaultFilter);
        return c;
    }

    @Override
    public URIChooser createSaveChooser(Application a, @Nullable View v) {
        JFileURIChooser c = new JFileURIChooser();
        ExtensionFileFilter defaultFilter = new ExtensionFileFilter("Drawing .xml","xml");
        c.addChoosableFileFilter(defaultFilter);
        c.addChoosableFileFilter(new ExtensionFileFilter("TIFF", new String[] {"tif","tiff"}));
        c.addChoosableFileFilter(new ExtensionFileFilter("JPEG", new String[] {"jpg","jpeg"}));
        c.addChoosableFileFilter(new ExtensionFileFilter("PNG", "png"));

        c.setFileFilter(defaultFilter);
        return c;
    }
}
