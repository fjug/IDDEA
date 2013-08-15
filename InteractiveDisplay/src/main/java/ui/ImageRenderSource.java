package ui;

import net.imglib2.RandomAccessible;
import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.ui.util.InterpolatingSource;

/**
 * Created with IntelliJ IDEA.
 *
 * @version 0.1beta
 * @since 8/14/13 4:50 PM
 * @author HongKee Moon
 */
public interface ImageRenderSource< T, A >
{
    public A getSourceTransform();

    /**
     * Get the {@link Converter} (converts {@link #source} type T to ARGBType
     * for display).
     */
    public Converter< ? super T, ARGBType > getConverter();
}