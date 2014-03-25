package controller.plot;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.data.EnumeratedData;
import de.erichseifert.gral.data.statistics.Histogram1D;
import de.erichseifert.gral.data.statistics.Statistics;
import de.erichseifert.gral.plots.BarPlot;
import de.erichseifert.gral.ui.InteractivePanel;
import de.erichseifert.gral.util.GraphicsUtils;
import de.erichseifert.gral.util.Insets2D;
import de.erichseifert.gral.util.MathUtils;
import de.erichseifert.gral.util.Orientation;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @version 0.1beta
 * @since 3/25/14 1:59 PM
 * @author HongKee Moon
 */

/**
 * Created by moon on 25/03/14.
 */
public class HistogramPlot extends JPanel
{
    /** First corporate color used for normal coloring.*/
    protected static final Color COLOR1 = new Color( 55, 170, 200);
    /** Second corporate color used as signal color */
    protected static final Color COLOR2 = new Color(200,  80,  75);

    /**
     * Performs basic initialization of an example,
     * like setting a default size.
     */
    public HistogramPlot(ArrayList<Long> list) {
        super(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);

        // Create example data
        DataTable data = new DataTable(Long.class);

        Long max = Collections.max(list);
        Long min = Collections.min(list);
        int bin = max.intValue() - min.intValue() + 1;

        System.out.println("Bin: " + bin);
        System.out.println("Max: " + max + " Min: " + min + " Gap:" + 1);

        Number[] number = null;

        if(min != max)
        {
            number = new Number[bin + 1];

            for (int i = 0; i <= bin; i++)
                number[i] = min + i;
        }
        else
        {
            number = new Number[3];
            min -= 1;

            number[0] = min;
            number[1] = min + 1;
            number[2] = min + 2;
        }

        for (Long d : list)
        {
            data.add(d);
        }

        // Create histogram from data
        Histogram1D histogram = new Histogram1D(data, Orientation.VERTICAL, number);
        // Create a second dimension (x axis) for plotting
        DataSource histogram2d = new EnumeratedData(histogram, min, 1.0);

        // Create new bar plot
        BarPlot plot = new BarPlot(histogram2d);

        // Format plot
        plot.setInsets(new Insets2D.Double(20.0, 65.0, 50.0, 40.0));
        plot.getTitle().setText(
                String.format("Distribution of %d pixels", data.getRowCount()));
        plot.setBarWidth(0.78);

        // Format x axis
        plot.getAxisRenderer(BarPlot.AXIS_X).setTickAlignment(0.0);
        plot.getAxisRenderer(BarPlot.AXIS_X).setTickSpacing(1.0);
        plot.getAxisRenderer(BarPlot.AXIS_X).setMinorTicksVisible(false);
        //plot.getAxisRenderer(BarPlot.AXIS_X).set
        // Format y axis
        plot.getAxis(BarPlot.AXIS_Y).setRange(0.0,
                MathUtils.ceil(histogram.getStatistics().get(Statistics.MAX)*1.1, 25.0));
        plot.getAxisRenderer(BarPlot.AXIS_Y).setTickAlignment(0.0);
        plot.getAxisRenderer(BarPlot.AXIS_Y).setMinorTicksVisible(false);
        plot.getAxisRenderer(BarPlot.AXIS_Y).setIntersection(-4.4);

        // Format bars
        plot.getPointRenderer(histogram2d).setColor(
                GraphicsUtils.deriveWithAlpha(COLOR1, 128));
        plot.getPointRenderer(histogram2d).setValueVisible(true);

        // Add plot to Swing component
        InteractivePanel panel = new InteractivePanel(plot);
        panel.setPannable(true);
        panel.setZoomable(true);
        add(panel);
        showInFrame();
    }

    public JFrame showInFrame() {
        JFrame frame = new JFrame("Histogram");
        frame.getContentPane().add(this, BorderLayout.CENTER);
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(getPreferredSize());
        frame.setVisible(true);
        return frame;
    }
}
