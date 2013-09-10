package view.component;

import controller.tool.SpimTool;
import ij.ImagePlus;
import ij.io.Opener;
import model.figure.DrawFigureFactory;
import model.source.MandelbrotRealRandomAccessible;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.FloatType;
import org.jhotdraw.draw.*;
import org.jhotdraw.draw.action.ButtonFactory;
import org.jhotdraw.draw.io.DOMStorableInputOutputFormat;
import org.jhotdraw.draw.tool.BezierTool;
import org.jhotdraw.util.ResourceBundleUtil;
import view.converter.ColorTables;
import view.converter.LUTConverter;
import view.display.InteractiveDrawingView;
import view.viewer.InteractiveRealViewer;
import view.viewer.InteractiveRealViewer2D;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/4/13
 */
public class SamplePanel extends JPanel {

    IddeaComponent annotator;

    public SamplePanel()
    {
        super(new BorderLayout());

        File file = new File( "/Users/moon/Projects/ScientificPlatform/ImgLib2/imglib/examples/DrosophilaWing.tif" );

        // open a file with ImageJ
        final ImagePlus imp = new Opener().openImage( file.getAbsolutePath() );

        // wrap it into an ImgLib image (no copying)
        final Img<FloatType> image = ImagePlusAdapter.wrap(imp);

        annotator = new IddeaComponent(image);
        annotator.setToolBarLocation(BorderLayout.WEST);
        annotator.setToolBarVisible(true);

        annotator.setPreferredSize(new Dimension(30, 20));

        add(annotator, BorderLayout.CENTER);

        //Working
//        annotator.loadAnnotations("/Users/moon/Desktop/1.xml");
//        annotator.saveAnnotations("/Users/moon/Desktop/4.xml");
//        annotator.setImg(image);

        // Doing
//        annotator.enableTool(HongKeeViewer.HAND_TOOL);
//        annotator.enableTool(HongKeeViewer.ZOOM_TOOL);
//        annotator.enableTool(HongKeeViewer.BEZIER_TOOL);
//
    }

    public static void main( final String[] args ) {

        SamplePanel panel = new SamplePanel();

        final JFrame f = new JFrame();
        f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        f.getContentPane().add(panel);
        f.setSize(800, 600);
        f.setVisible( true );
    }
}
