package view.component;

import controller.tool.SpimTool;
import ij.ImagePlus;
import ij.io.Opener;
import model.figure.DrawFigureFactory;
import model.source.MandelbrotRealRandomAccessible;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealDoubleConverter;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

/**
 * SamplePanel for demonstrating IDDEA component.
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/4/13
 */
public class SamplePanel extends JPanel implements ActionListener {

    IddeaComponent annotator;

    private JButton bLoad;

    public SamplePanel()
    {
        super(new BorderLayout());

        annotator = new IddeaComponent();
        annotator.setToolBarLocation(BorderLayout.WEST);
        annotator.setToolBarVisible(true);

        annotator.setPreferredSize(new Dimension(30, 20));

        bLoad = new JButton("Load");
        bLoad.addActionListener(this);

        add(annotator, BorderLayout.CENTER);
        add(bLoad, BorderLayout.NORTH);

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

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if(actionEvent.getSource().equals(bLoad))
        {
            File file = new File( "/Users/moon/Documents/clown.tif" );

            // open a file with ImageJ
            final ImagePlus imp = new Opener().openImage( file.getAbsolutePath() );

            // wrap it into an ImgLib image (no copying)
            final Img<DoubleType> image = ImagePlusAdapter.wrap(imp);

            RandomAccessibleInterval imgOrig = Converters.convert(Views.interval(image, image),
                    new RealDoubleConverter(), new DoubleType());

            //Views.interval(imgOrig, imgOrig)

            annotator.setDoubleTypeScreenImage(Views.interval(imgOrig, imgOrig));
        }
    }
}
