package view.ui;

import net.imglib2.converter.Converter;
import net.imglib2.display.AbstractLinearRange;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
/**
 * Converts Real value to ARGB integer value.
 * It checks ArrayIndexOutOfBoundsException.
 *
 * @version 0.1beta
 * @since 8/16/13 3:19 PM
 * @author HongKee Moon
 */
public class RealARGBConverterDecode< R extends RealType< ? > > extends AbstractLinearRange implements Converter< R, ARGBType >
{
    public RealARGBConverterDecode()
    {
        super();
    }

    @Override
    public void convert( final R input, final ARGBType output )
    {
        int argb = 0xff000000;

        try{
            float a =  input.getRealFloat();
            int r = (int) a;

            argb = r;
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
        }
        output.set( argb );
    }

}
