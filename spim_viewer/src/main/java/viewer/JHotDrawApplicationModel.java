/*
 * @(#)JHotDrawApplicationModel.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */

package viewer;

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

import static org.jhotdraw.draw.AttributeKeys.*;

/**
 * Provides factory methods for creating views, menu bars and toolbars.
 * <p>
 * See {@link ApplicationModel} on how this class interacts with an application.
 * 
 * @author Werner Randelshofer.
 * @version $Id: DrawApplicationModel.java 731 2011-01-22 09:21:06Z rawcoder $
 */
public class JHotDrawApplicationModel extends DefaultApplicationModel {

    /**
     * This editor is shared by all views.
     */
    private DefaultDrawingEditor sharedEditor;  

    /** Creates a new instance. */
    public JHotDrawApplicationModel() {
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
            ((JHotDrawView) p).setEditor(getSharedEditor());
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
        JHotDrawView p = (JHotDrawView) pr;

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

        ButtonFactory.addToolTo(tb, editor, new tool.SpimTool(),  "edit.createSpim", labels);
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
