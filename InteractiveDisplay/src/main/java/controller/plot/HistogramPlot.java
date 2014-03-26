package controller.plot;

import java.awt.*;
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
import de.erichseifert.gral.graphics.DrawableContainer;
import de.erichseifert.gral.graphics.TableLayout;
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
import de.erichseifert.gral.util.Orientation;

import javax.swing.*;

import model.util.DoubleArrayList;
import model.util.LongArrayList;

/**
 * Draw HistogramPlot Panel with Foreground list and Background list
 *
 * @version 0.1beta
 * @since 3/25/14 1:59 PM
 * @author HongKee Moon
 */
public class HistogramPlot extends JPanel
{
    /** First corporate color used for foreground color */
    protected static final Color COLOR1 = new Color(200,  80,  75);
    /** Second corporate color used for background color */
    protected static final Color COLOR2 = new Color(55 , 170, 200);

    /** Maximum Sample Size */
    static final int SAMPLE = 100;

    XYPlot foreground = null;
    XYPlot background = null;

    /**
     * Performs basic initialization of HistogramPlot for LongArrayList
     */
    public HistogramPlot(LongArrayList foregroundList, LongArrayList backgroundList) {
        super(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);

        if(foregroundList.size() > 0)
            foreground = createXYPlot("Foreground", foregroundList, COLOR1, Long.class);

        if(backgroundList.size() > 0)
            background = createXYPlot("Background", backgroundList, COLOR2, Long.class);

        DrawableContainer plots = new DrawableContainer(new TableLayout(1));
        if(foreground != null) plots.add(foreground);
        if(background != null) plots.add(background);

        // Connect the two plots, i.e. user (mouse) actions affect both plots
        //foreground.getNavigator().connect(background.getNavigator());

        // Add plot to Swing component
        InteractivePanel panel = new InteractivePanel(plots);
        add(panel);
        showInFrame();
    }

    /**
     * Performs basic initialization of HistogramPlot for DoubleArrayList
     */
    public HistogramPlot(DoubleArrayList foregroundList, DoubleArrayList backgroundList) {
        super(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);

        if(foregroundList.size() > 0)
            foreground = createXYPlot("Foreground", foregroundList, COLOR1, Double.class);

        if(backgroundList.size() > 0)
            background = createXYPlot("Background", backgroundList, COLOR2, Double.class);

        DrawableContainer plots = new DrawableContainer(new TableLayout(1));
        if(foreground != null) plots.add(foreground);
        if(background != null) plots.add(background);

        // Connect the two plots, i.e. user (mouse) actions affect both plots
        //foreground.getNavigator().connect(background.getNavigator());

        // Add plot to Swing component
        InteractivePanel panel = new InteractivePanel(plots);
        add(panel);
        showInFrame();
    }

    /**
     * Get a DataSource from Long type ArrayList
     * @param list
     * @return data
     */
    private DataSource getLongHistogramDataSource(ArrayList<Long> list)
    {
        // Create example data
        Long max = Collections.max(list);
        Long min = Collections.min(list);
        int bin = max.intValue() - min.intValue() + 1;
        Double gap = 1d;

        DataTable data = new DataTable(Long.class);

        System.out.println("Bin: " + bin + " SAMPLE:" + SAMPLE);
        System.out.println("Max: " + max + " Min: " + min + " Gap:" + 1);

        if(bin > SAMPLE) {
            bin = SAMPLE;
            gap = (max.doubleValue() - min.doubleValue()) / bin;
        }

        Number[] number = null;

        if(min != max)
        {
            number = new Number[bin + 1];

            for (int i = 0; i <= bin; i++)
                number[i] = min + i * gap;
        }
        else
        {
            number = new Number[3];
            min =  min - 1;

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
        return new EnumeratedData(histogram, min, gap);
    }

    /**
     * Get a DataSource for Double type ArrayList
     * @param list
     * @return data
     */
    private DataSource getDoubleHistogramDataSource(ArrayList<Double> list)
    {
        // Create example data
        DataTable data = new DataTable(Double.class);

        Double max = Collections.max(list);
        Double min = Collections.min(list);
        Double gap = (max.doubleValue() - min.doubleValue()) / SAMPLE;

        System.out.println("Bin: " + SAMPLE);
        System.out.println("Max: " + max + " Min: " + min + " Gap:" + gap);

        Number[] number = null;

        if(min != max)
        {
            number = new Number[SAMPLE + 1];

            for (int i = 0; i <= SAMPLE; i++)
                number[i] = min + i * gap;
        }
        else
        {
            number = new Number[3];
            min -= gap;

            number[0] = min;
            number[1] = min + gap;
            number[2] = min + gap;
        }

        for (Double d : list)
        {
            data.add(d);
        }

        // Create histogram from data
        Histogram1D histogram = new Histogram1D(data, Orientation.VERTICAL, number);
        // Create a second dimension (x axis) for plotting
        return new EnumeratedData(histogram, min, gap);
    }

    /**
     * creates a Plot for ctor()
     * @param title
     * @param list
     * @param color
     * @param clazz
     * @return
     */
    private XYPlot createXYPlot(String title, ArrayList<?> list, Color color, Class clazz)
    {
        // Create example data
        DataSource histogram2d = null;

        if(clazz == Long.class)
        {
            histogram2d = getLongHistogramDataSource((ArrayList<Long>)list);
        }
        else if(clazz == Double.class)
        {
            histogram2d = getDoubleHistogramDataSource((ArrayList<Double>)list);
        }

        DataSeries ds = new DataSeries("Data", histogram2d, 0, 1);

        // Create new bar plot
        XYPlot plot = new XYPlot(ds);
        plot.getAxis(plot.AXIS_X).setAutoscaled(false);
        plot.getAxis(plot.AXIS_Y).setAutoscaled(false);

        // Format plot
        plot.setInsets(new Insets2D.Double(20.0, 65.0, 50.0, 40.0));
        plot.getTitle().setText(
                String.format("%s (%d) pixels", title, list.size()));

        final double KERNEL_VARIANCE = 3.0;

        // Create a smoothed data series from a binomial (near-gaussian) convolution filter
        Kernel kernelLowpass = KernelUtils.getBinomial(KERNEL_VARIANCE).normalize();
        Filter dataLowpass = new Convolution(histogram2d, kernelLowpass, Filter.Mode.REPEAT, 1);
        DataSeries dsLowpass = new DataSeries("Lowpass", dataLowpass, 0, 1);
        plot.add(dsLowpass);

        formatLineArea(plot, ds, color);
        formatFilledArea(plot, dsLowpass, GraphicsUtils.deriveDarker(color));

        return plot;
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
        //point.setValueVisible(true);

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
