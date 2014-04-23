package view.component;

import ij.ImagePlus;
import ij.io.Opener;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealDoubleConverter;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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
    private JButton bLoad2;

    public SamplePanel()
    {
        super(new BorderLayout());

//        File file = new File( "/Users/moon/Documents/t63-head-1.tif" );
//
//        // open a file with ImageJ
//        final ImagePlus imp = new Opener().openImage( file.getAbsolutePath() );
//
//        // wrap it into an ImgLib image (no copying)
//        final Img<DoubleType> image = ImagePlusAdapter.wrap(imp);
//
//        RandomAccessibleInterval imgOrig = Converters.convert(Views.interval(image, image),
//                new RealDoubleConverter(), new DoubleType());
//
//        annotator = new IddeaComponent(Views.interval(imgOrig, imgOrig));
        annotator = new IddeaComponent();
        annotator.setToolBarLocation(BorderLayout.WEST);
        annotator.setToolBarVisible(true);
        annotator.setPreferredSize(new Dimension(300, 200));

        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());

        bLoad = new JButton("Load");
        bLoad.addActionListener(this);
        p.add(bLoad);

        bLoad2 = new JButton("Load");
        bLoad2.addActionListener(this);
        p.add(bLoad2);


        add(annotator, BorderLayout.CENTER);
        add(p, BorderLayout.NORTH);

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
        else if(actionEvent.getSource().equals(bLoad2))
        {
            File file = new File( "/Users/moon/Documents/t63-head-1.tif" );

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
