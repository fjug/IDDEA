package view.ui;

import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;


/**
 * Convert ARGBType to FloatType for using RealRandomAccessible object.
 * Also, it's checking for ArrayIndexOutOfBoundsException.
 *
 * @version 0.1beta
 * @since 8/16/13 2:46 PM
 * @author HongKee Moon
 */
public class ARGBRealConverter implements Converter<ARGBType, FloatType>
{
    @Override
    public void convert( final ARGBType input, final FloatType output )
    {
        float b = 0;
        try{
            int a = input.get();
            b = a;
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {

        }
        output.setReal(b);
    }
}
