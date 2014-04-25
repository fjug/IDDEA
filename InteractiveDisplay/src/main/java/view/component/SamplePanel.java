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
    private JButton bLoad3;

    public SamplePanel()
    {
        super(new BorderLayout());
        
        annotator = new IddeaComponent();
        annotator.setToolBarLocation(BorderLayout.WEST);
        annotator.setToolBarVisible(true);
        annotator.setPreferredSize(new Dimension(300, 200));

        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());

        bLoad = new JButton("Load clown");
        bLoad.addActionListener(this);
        p.add(bLoad);

        bLoad2 = new JButton("Load t1-head");
        bLoad2.addActionListener(this);
        p.add(bLoad2);

        bLoad3 = new JButton("Load confocal-series");
        bLoad3.addActionListener(this);
        p.add(bLoad3);


        add(annotator, BorderLayout.CENTER);
        add(p, BorderLayout.NORTH);
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

            annotator.setPreferredSize(new Dimension(imp.getWidth(), imp.getHeight()));
            annotator.setDoubleTypeSourceImage(Views.interval(imgOrig, imgOrig));

        }
        else if(actionEvent.getSource().equals(bLoad2))
        {
            File file = new File( "/Users/moon/Pictures/t1-head.tif" );

            // open a file with ImageJ
            final ImagePlus imp = new Opener().openImage( file.getAbsolutePath() );

            // wrap it into an ImgLib image (no copying)
            final Img<DoubleType> image = ImagePlusAdapter.wrap(imp);

            RandomAccessibleInterval imgOrig = Converters.convert(Views.interval(image, image),
                    new RealDoubleConverter(), new DoubleType());

            annotator.setPreferredSize(new Dimension(imp.getWidth(), imp.getHeight()));
            annotator.setDoubleTypeSourceImage(Views.interval(imgOrig, imgOrig));
        }
        else if(actionEvent.getSource().equals(bLoad3))
        {
            File file = new File( "/Users/moon/Pictures/confocal-series.tif" );

            // open a file with ImageJ
            final ImagePlus imp = new Opener().openImage( file.getAbsolutePath() );

            // wrap it into an ImgLib image (no copying)
            final Img<DoubleType> image = ImagePlusAdapter.wrap(imp);

            RandomAccessibleInterval imgOrig = Converters.convert(Views.interval(image, image),
                    new RealDoubleConverter(), new DoubleType());

            annotator.setPreferredSize(new Dimension(imp.getWidth(), imp.getHeight()));
            annotator.setDoubleTypeSourceImage(Views.interval(imgOrig, imgOrig));
        }
    }
}
