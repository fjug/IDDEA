package com.indago.iddea.controller.action;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.algorithm.labeling.AllConnectedComponents;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.sparse.NtreeImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.indago.iddea.view.display.JHotDrawInteractiveDisplay2D;
import com.indago.iddea.view.overlay.ObjectInfo;
import com.indago.iddea.view.overlay.ObjectInfoOverlay;
import com.indago.iddea.view.overlay.ObjectInfoTransformOverlay;
import com.indago.iddea.view.overlay.ObjectSetInfo;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/**
 * Created by moon on 16/04/14.
 */
public class LabelObjectAction extends AbstractAction implements MouseMotionListener, ChangeListener {

    final JHotDrawInteractiveDisplay2D display;

    IntervalView intervalView;

    ObjectInfoTransformOverlay objectLabel = null;
    ObjectInfoOverlay objectInfo = null;
    LinkedHashMap<Point, ObjectInfo> objectMap = new LinkedHashMap<Point, ObjectInfo>();
    LinkedHashMap<Point, Integer> junctionPoint = new LinkedHashMap<Point, Integer>();
    LinkedHashMap<Integer, ObjectSetInfo> junctionMap = new LinkedHashMap<Integer, ObjectSetInfo>();

    ArrayList<ObjectSetInfo> objSetLists = null;
    ArrayList<ObjectInfo> objLists = null;

    public LabelObjectAction(JHotDrawInteractiveDisplay2D display, IntervalView view) {
        this.display = display;
        this.intervalView = view;

        objectLabel = new ObjectInfoTransformOverlay();
        objectInfo = new ObjectInfoOverlay();

        display.addOverlayRenderer(objectLabel);
        objectInfo.updateInfo("Detecting started.", new Date().toString());
        display.addOverlayRenderer(objectInfo);

        display.addMouseMotionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        detect();
    }

    public void detect()
    {
        // Green channel picked-up (Endpoint)
        objLists = detectEndpoints(Views.hyperSlice(intervalView, 2, 1));
        setEndpointVisible(true);

        // Red channel (Junction)
        objSetLists = detectJunctions(Views.hyperSlice(intervalView, 2, 2));
        setJunctionVisible(true);
    }

    public < T extends RealType< T > & NativeType< T > & Comparable<T> > ArrayList<ObjectSetInfo> detectJunctions(IntervalView<T> v)
    {
        junctionPoint.clear();
        junctionMap.clear();

        long[] dimensions = new long[] { v.dimension(0), v.dimension(1) };
        ArrayImgFactory<BitType> imgFactory = new ArrayImgFactory< BitType >();
        ArrayImg< BitType, ? > image = imgFactory.create( dimensions, new BitType() );
        Labeling< Integer > labeling = new NativeImgLabeling< Integer, IntType >( new NtreeImgFactory< IntType >().create( dimensions, new IntType() ) );


        ArrayList<ObjectSetInfo> objLists = new ArrayList<ObjectSetInfo>();

        IterableInterval< T > input = Views.iterable(v);
        Cursor< T > cursorInput = input.localizingCursor();
        Cursor< BitType > c = image.localizingCursor();

        T val;
        BitType b;

        net.imglib2.Point position = new net.imglib2.Point( v.numDimensions() );

        while(cursorInput.hasNext())
        {
            val = cursorInput.next();
            b = c.next();

            b.setZero();
            if(val.getRealDouble() > 0)
            {
                position.setPosition(cursorInput);
                image.randomAccess().setPosition(cursorInput);

                b.setOne();
            }
        }

        Iterator<Integer> names = AllConnectedComponents.getIntegerNames( 0 );
        long[][] structuringElement = new long[][] { {-1, -1}, {-1, 0}, {-1, 1}, {1, 0}, {1, -1}, {0, -1}, {0, 1}, {1, 1}};
        AllConnectedComponents.labelAllConnectedComponents( labeling, image, names, structuringElement );

        Cursor<LabelingType< Integer >> lc = labeling.localizingCursor();
        HashMap< Integer, HashSet<Point> > map = new HashMap< Integer, HashSet<Point> >();

        int[] pos = new int[ 2 ];
        while ( lc.hasNext() )
        {
            LabelingType<Integer> lt = lc.next();
            lc.localize( pos );
            List< Integer > labels = lt.getLabeling();
            if(labels.size() > 0) {
                final Integer value = labels.get(0);

                if(!map.containsKey(value)) {
                    HashSet<Point> hashMap = new HashSet<Point>();
                    map.put(value, hashMap);

                    ObjectSetInfo info = new ObjectSetInfo(
                            hashMap,
                            "Junction-" + value, "");
                    objLists.add(info);
                    junctionMap.put(value, info);
                }

                Point pt = new Point(pos[0], pos[1]);
                map.get(value).add(pt);
                junctionPoint.put(pt, value);
            }
        }

        Iterator<Integer> iter = map.keySet().iterator();
        while(iter.hasNext())
        {
            Integer i = iter.next();
            //System.out.println("" + i + ":" + map.get(i).size());
        }

        return objLists;
    }

    public < T extends RealType< T > & NativeType< T >> ArrayList<ObjectInfo> detectEndpoints(IntervalView<T> v)
    {
        objectMap.clear();
        ArrayList<ObjectInfo> objLists = new ArrayList<ObjectInfo>();

        IterableInterval< T > input = Views.iterable(v);
        net.imglib2.Cursor< T > cursorInput = input.localizingCursor();

        T val;

        int i = 0;

        while(cursorInput.hasNext())
        {
            val = cursorInput.next();

            if(val.getRealDouble() > 0)
            {
                net.imglib2.Point position = new net.imglib2.Point( v.numDimensions() );
                position.setPosition(cursorInput);

                ObjectInfo info = new ObjectInfo(
                        position.getIntPosition(0),
                        position.getIntPosition(1),
                        "Endpoint-" + i, "");
//                System.out.print("" + position.getDoublePosition(0) + "," + position.getDoublePosition(1));
//                System.out.println(val.getRealDouble());
                objLists.add(info);
                objectMap.put(new Point(position.getIntPosition(0), position.getIntPosition(1)), info);
                i++;
            }
        }

        return objLists;
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        Point2D.Double p = display.viewToDrawing(mouseEvent.getPoint());

        int x = (int)Math.round(p.getX());
        int y = (int)Math.round(p.getY());

        objectLabel.updateXY(x, y);
        Point pt = new Point(x, y);
        if(objectMap.containsKey(pt)) {
            ObjectInfo info = objectMap.get(pt);
            objectInfo.updateInfo(info.Label, "" + x + ", " + y);
        }
        else if(junctionPoint.containsKey(pt))  {
            ObjectSetInfo info = junctionMap.get(junctionPoint.get(pt));
            objectInfo.updateInfo(info.Label, "" + x + ", " + y);
        }
        else
            objectInfo.updateInfo("Mouse ", "" + x + ", " + y);


        display.repaint();
    }

    public void setJunctionVisible(boolean b)
    {
        if(b)
            objectLabel.setObjectSetList(objSetLists);
        else
            objectLabel.setObjectSetList(null);

        display.repaint();
    }

    public void setEndpointVisible(boolean b)
    {
        if(b)
            objectLabel.setObjectList(objLists);
        else
            objectLabel.setObjectList(null);

        display.repaint();
    }

    public void updateIntervalView(IntervalView v)
    {
        this.intervalView = v;
        detect();
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        if(changeEvent.getSource() instanceof JCheckBox) {
            JCheckBox cb = (JCheckBox) changeEvent.getSource();
            if (cb.getName().equals("Endpoints")) {
                setEndpointVisible(cb.isSelected());
            } else if (cb.getName().equals("Junctions")) {
                setJunctionVisible(cb.isSelected());
            }
        }
    }
}
