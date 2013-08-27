package view;


import net.imglib2.RealRandomAccessible;

/**
 * Created with IntelliJ IDEA.
 *
 * @version 0.1beta
 * @since 8/27/13 9:11 AM
 * @author HongKee Moon
 */
public interface SourceAccessible<T> {
    public RealRandomAccessible<T> getSource();
}
