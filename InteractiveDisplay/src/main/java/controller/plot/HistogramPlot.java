package controller.plot;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;

import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.data.EnumeratedData;
import de.erichseifert.gral.data.filters.Convolution;
import de.erichseifert.gral.data.filters.Filter;
import de.erichseifert.gral.data.filters.Kernel;
import de.erichseifert.gral.data.filters.KernelUtils;
import de.erichseifert.gral.data.statistics.Histogram1D;
import de.erichseifert.gral.data.statistics.Statistics;
import de.erichseifert.gral.plots.BarPlot;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.areas.AreaRenderer;
import de.erichseifert.gral.plots.areas.DefaultAreaRenderer2D;
import de.erichseifert.gral.plots.areas.LineAreaRenderer2D;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.points.DefaultPointRenderer2D;
import de.erichseifert.gral.plots.points.PointRenderer;
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
        DataSeries ds = new DataSeries("Data", histogram2d, 0, 1);

        // Create new bar plot
        XYPlot plot = new XYPlot(ds);

        // Format plot
        plot.setInsets(new Insets2D.Double(20.0, 65.0, 50.0, 40.0));
        plot.getTitle().setText(
                String.format("Distribution of %d pixels\nBin: %d, Min: %d, Max:%d",
                        data.getRowCount(), bin, min, max));

        final double KERNEL_VARIANCE = 3.0;

        // Create a smoothed data series from a binomial (near-gaussian) convolution filter
        Kernel kernelLowpass = KernelUtils.getBinomial(KERNEL_VARIANCE).normalize();
        Filter dataLowpass = new Convolution(histogram2d, kernelLowpass, Filter.Mode.REPEAT, 1);
        DataSeries dsLowpass = new DataSeries("Lowpass", dataLowpass, 0, 1);
        plot.add(dsLowpass);

        formatLineArea(plot, ds, COLOR2);
        formatFilledArea(plot, dsLowpass, COLOR1);

        // Add plot to Swing component
        InteractivePanel panel = new InteractivePanel(plot);
        panel.setPannable(true);
        panel.setZoomable(true);
        add(panel);
        showInFrame();
    }

    private static void formatFilledArea(XYPlot plot, DataSource data, Color color) {
        plot.setPointRenderer(data, null);

        LineRenderer line = new DefaultLineRenderer2D();
        line.setColor(color);
        line.setGap(3.0);
        line.setGapRounded(true);
        plot.setLineRenderer(data, line);

        AreaRenderer area = new DefaultAreaRenderer2D();
        area.setColor(GraphicsUtils.deriveWithAlpha(color, 64));
        plot.setAreaRenderer(data, area);
    }

    private static void formatLineArea(XYPlot plot, DataSource data, Color color) {
        PointRenderer point = new DefaultPointRenderer2D();
        point.setColor(new Color(1f, 1f, 1f, 1f));
        point.setValueVisible(true);

        plot.setPointRenderer(data, point);

        plot.setLineRenderer(data, null);

        LineAreaRenderer2D area = new LineAreaRenderer2D();
        area.setGap(3.0);
        area.setColor(color);
        area.setStroke(new BasicStroke(5));
        plot.setAreaRenderer(data, area);
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
