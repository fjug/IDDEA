package view.viewer;

/**
 * Created with IntelliJ IDEA.
 *
 * @version 0.1beta
 * @since 3/24/14 4:47 PM
 * @author HongKee Moon
 */

import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.ARGBType;

/**
 * Created by moon on 24/03/14.
 */
public interface Injectable<T>
{
    public void injectSource(RealRandomAccessible<T> source);

    public void injectConverter(Converter< ? super T, ARGBType > converter);
}
