/*
 * @(#)JHotDrawView.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */

package viewer;

import mpicbg.spim.data.SequenceDescription;
import net.imglib.display.RealARGBColorConverter;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.histogram.DiscreteFrequencyDistribution;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.view.Views;

import org.jhotdraw.draw.io.TextInputFormat;
import org.jhotdraw.draw.io.OutputFormat;
import org.jhotdraw.draw.io.InputFormat;
import org.jhotdraw.draw.io.ImageOutputFormat;
import org.jhotdraw.draw.io.ImageInputFormat;
import org.jhotdraw.draw.print.DrawingPageable;
import org.jhotdraw.draw.io.DOMStorableInputOutputFormat;

import java.awt.geom.*;
import java.awt.print.Pageable;

import org.jhotdraw.gui.*;
import org.jhotdraw.samples.draw.DrawFigureFactory;
import org.jhotdraw.undo.*;
import org.jhotdraw.util.*;

import java.awt.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.URI;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.*;
import javax.xml.parsers.ParserConfigurationException;

import org.jhotdraw.app.AbstractView;
import org.jhotdraw.app.action.edit.RedoAction;
import org.jhotdraw.app.action.edit.UndoAction;
import org.jhotdraw.draw.*;
import org.jhotdraw.draw.action.*;
import org.jhotdraw.gui.URIChooser;
import org.jhotdraw.net.URIUtil;
import org.xml.sax.SAXException;

import viewer.crop.CropDialog;
import viewer.gui.brightness.ConverterSetup;
import viewer.gui.brightness.MinMaxGroup;
import viewer.gui.brightness.NewBrightnessDialog;
import viewer.gui.brightness.SetupAssignments;
import viewer.render.Source;
import viewer.render.SourceAndConverter;
import viewer.render.SourceState;
import viewer.render.ViewerState;


/**
 * A default implementation of {@link DrawingView} suited for viewing drawings
 * with a small number of figures.
 *
 * FIXME - Implement clone Method.
 * FIXME - Use double buffering for the drawing to improve performance.
 *
 * @author Werner Randelshofer
 * @version $Id: DefaultDrawingView.java 779 2012-04-16 09:33:50Z rawcoder $
 */

public class JHotDrawView extends AbstractView {
    
    /**
     * Each DrawView uses its own undo redo manager.
     * This allows for undoing and redoing actions per view.
     */
    private UndoRedoManager undo;
    
    /**
     * Depending on the type of an application, there may be one editor per
     * view, or a single shared editor for all views.
     */
    private DrawingEditor editor;
    
  
	SetupAssignments setupAssignments;

	NewBrightnessDialog brightnessDialog;

	CropDialog cropDialog;
	
    /**
     * Creates a new view.
     */
    public JHotDrawView() {
    	
    	try{
            initComponents();    		
    	} catch (Exception ex)
    	{
    		ex.printStackTrace();
    	}

        
        scrollPane.setLayout(new PlacardScrollPaneLayout());
        scrollPane.setBorder(new EmptyBorder(0,0,0,0));
        
        setEditor(new DefaultDrawingEditor());
        undo = new UndoRedoManager();
        view.setDrawing(createDrawing());
        view.getDrawing().addUndoableEditListener(undo);
        initActions();
        undo.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                setHasUnsavedChanges(undo.hasSignificantEdits());
            }
        });
               
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
        
        JPanel placardPanel = new JPanel(new BorderLayout());
        javax.swing.AbstractButton pButton;
        pButton = ButtonFactory.createZoomButton(view);
        pButton.putClientProperty("Quaqua.Button.style","placard");
        pButton.putClientProperty("Quaqua.Component.visualMargin",new Insets(0,0,0,0));
        pButton.setFont(UIManager.getFont("SmallSystemFont"));
        placardPanel.add(pButton, BorderLayout.WEST);
        pButton = ButtonFactory.createToggleGridButton(view);
        pButton.putClientProperty("Quaqua.Button.style","placard");
        pButton.putClientProperty("Quaqua.Component.visualMargin",new Insets(0,0,0,0));
        pButton.setFont(UIManager.getFont("SmallSystemFont"));
        labels.configureToolBarButton(pButton, "view.toggleGrid.placard");
        placardPanel.add(pButton, BorderLayout.EAST);
        scrollPane.add(placardPanel, JScrollPane.LOWER_LEFT_CORNER);
        
        view.startThread();
    }
    
    /**
     * Creates a new Drawing for this view.
     */
    protected Drawing createDrawing() {
        Drawing drawing = new QuadTreeDrawing();
        DOMStorableInputOutputFormat ioFormat =
                new DOMStorableInputOutputFormat(new DrawFigureFactory());
        
        drawing.addInputFormat(ioFormat);
        ImageFigure prototype = new ImageFigure();
        drawing.addInputFormat(new ImageInputFormat(prototype));
        drawing.addInputFormat(new TextInputFormat(new TextFigure()));
        TextAreaFigure taf = new TextAreaFigure();
        taf.setBounds(new Point2D.Double(10,10), new Point2D.Double(60,40));
        drawing.addInputFormat(new TextInputFormat(taf));
        
        drawing.addOutputFormat(ioFormat);
        drawing.addOutputFormat(new ImageOutputFormat());
        return drawing;
    }
    
    
    /**
     * Creates a Pageable object for printing the view.
     */
    public Pageable createPageable() {
        return new DrawingPageable(view.getDrawing());
        
    }
    
    
    /**
     * Initializes view specific actions.
     */
    private void initActions() {
        getActionMap().put(UndoAction.ID, undo.getUndoAction());
        getActionMap().put(RedoAction.ID, undo.getRedoAction());
    }
    @Override
    protected void setHasUnsavedChanges(boolean newValue) {
        super.setHasUnsavedChanges(newValue);
        undo.setHasSignificantEdits(newValue);
    }
    
    /**
     * Writes the view to the specified uri.
     */
    @Override
    public void write(URI f, URIChooser fc) throws IOException {
        Drawing drawing = view.getDrawing();
        OutputFormat outputFormat = drawing.getOutputFormats().get(0);
        for(OutputFormat o : drawing.getOutputFormats())
        {
        	System.out.println(o);
        }
        outputFormat.write(f, drawing);
    }
    
    /**
     * Reads the view from the specified uri.
     */
    @Override
    public void read(URI f, URIChooser fc) throws IOException {
        try {

            final Drawing drawing = createDrawing();

            boolean success = false;
                for (InputFormat sfi : drawing.getInputFormats()) {
                        try {
                            sfi.read(f, drawing, true);
                            success = true;
                            break;
                        } catch (Exception e) {
                        // try with the next input format
                        }
                    }
            if (!success) {
                ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.app.Labels");
                throw new IOException(labels.getFormatted("file.open.unsupportedFileFormat.message", URIUtil.getName(f)));
            }

//            ((QuadTreeDrawing) drawing).setAttributeOnChildren(org.jhotdraw.draw.AttributeKeys.UNCLOSED_PATH_FILLED, false);
            ((QuadTreeDrawing) drawing).setAttributeOnChildren(org.jhotdraw.draw.AttributeKeys.FILL_COLOR, new Color( 0.0f, 1.0f, 0.0f, 0.1f ));
            ((QuadTreeDrawing) drawing).setAttributeOnChildren(org.jhotdraw.draw.AttributeKeys.STROKE_COLOR, new Color( 1.0f, 0.0f, 0.0f, 0.33f ));
//            ((QuadTreeDrawing) drawing).setAttributeOnChildren(org.jhotdraw.draw.AttributeKeys.STROKE_WIDTH, 2.5);
            
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    view.getDrawing().removeUndoableEditListener(undo);
                    view.setDrawing(drawing);
                    view.getDrawing().addUndoableEditListener(undo);
                    undo.discardAllEdits();
                }
            });
        } catch (InterruptedException e) {
            InternalError error = new InternalError();
            e.initCause(e);
            throw error;
        } catch (InvocationTargetException e) {
            InternalError error = new InternalError();
            error.initCause(e);
            throw error;
        }
    }
    
    
    /**
     * Sets a drawing editor for the view.
     */
    public void setEditor(DrawingEditor newValue) {
        if (editor != null) {
            editor.remove(view);
        }
        editor = newValue;
        if (editor != null) {
            editor.add(view);
        }
    }
    
    /**
     * Gets the drawing editor of the view.
     */
    public DrawingEditor getEditor() {
        return editor;
    }
    
    /**
     * Clears the view.
     */
    @Override
    public void clear() {
        final Drawing newDrawing = createDrawing();
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    view.getDrawing().removeUndoableEditListener(undo);
                    view.setDrawing(newDrawing);
                    view.getDrawing().addUndoableEditListener(undo);
                    undo.discardAllEdits();
                }
            });
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    public boolean canSaveTo(URI file) {
        return new File(file).getName().endsWith(".xml");
    }
    
//    public viewer.SpimDrawingView getSpimDrawingView()
//    {
//    	return view;
//    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor. 
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    { 
        scrollPane = new javax.swing.JScrollPane();
        
		JSlider sliderTime = new JSlider( JSlider.HORIZONTAL, 0, 10 - 1, 0 );
        view = new SpimDrawingView(sliderTime);

        setLayout(new java.awt.BorderLayout());

        scrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setViewportView(view);
		
        add(scrollPane, java.awt.BorderLayout.CENTER);
        add(sliderTime, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPane;
    private SpimDrawingView view;
    // End of variables declaration//GEN-END:variables
    
}

