package view.viewer;

import net.imglib2.RealRandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.ARGBType;

/**
 * Injectable interface provides source & converter updates without generating a DisplayInterface
 *
 * @param <T> the generic type
 * @author HongKee Moon
 */
public interface Injectable< T > {

    public void injectSource( RealRandomAccessible< T > source );

    public void injectConverter( Converter< ? super T, ARGBType > converter );
}
