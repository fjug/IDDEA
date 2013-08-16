package view.ui;

import net.imglib2.converter.Converter;
import net.imglib2.display.AbstractLinearRange;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

/**
 * Same as original RealARGBConverter.
 * Try-catch block is added for handling ArrayIndexOutOfBoundsException.
 *
 * @version 0.1beta
 * @since 8/16/13 11:48 AM
 * @author HongKee Moon
 */
public class RealARGBConverter< R extends RealType< ? > > extends AbstractLinearRange implements Converter< R, ARGBType >
{
    public RealARGBConverter()
    {
        super();
    }

    public RealARGBConverter( final double min, final double max )
    {
        super( min, max );
    }

    @Override
    public void convert( final R input, final ARGBType output )
    {
        double a = 0d;
        try{
             a = input.getRealDouble();
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
        }

        final int b = Math.min( 255, roundPositive( Math.max( 0, ( ( a - min ) / scale * 255.0 ) ) ) );
        final int argb = 0xff000000 | ( ( ( b << 8 ) | b ) << 8 ) | b;
        output.set( argb );
    }
}
