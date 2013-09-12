package plugin.samples;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.jhotdraw.draw.Figure;
import plugin.ProcessPlugin;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;
import net.imglib2.ui.OverlayRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.*;
import java.util.*;

/**
 * The sample code for skeleton process class
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/3/13
 */
public class SkeletonProcess extends ProcessPlugin {
    @Override
    public String getName() {
        return "SkeletonProcess";
    }

    @Override
    public String getAuthor() {
        return "HongKee Moon";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public void process(RandomAccessibleInterval source)
    {
        String inputstring = "public class aaaa extends ";
        Pattern pattern = Pattern.compile("[\\s]*public class (.*?) ");
        Matcher m = pattern.matcher(inputstring);
        while(m.find())
        {
            System.out.println(m.group(1));
        }
    }

    @Override
    public void process(RandomAccessibleInterval source, Set<Figure> figures)
    {

    }
}
