package view.converter;

import net.imglib2.converter.Converter;
import net.imglib2.display.AbstractLinearRange;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.AbstractIntegerType;


/**
 * Created with IntelliJ IDEA.
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 8/30/13
 */
public class LUTConverter < R extends RealType< ? >> extends AbstractLinearRange implements Converter< R, ARGBType >
{

    private ColorTable lut = null;

    public LUTConverter() {
        super();
    }

    public LUTConverter(final double min, final double max,
                            final ColorTable lut)
    {
        super(min, max);
        setLUT(lut);
    }

    public ColorTable getLUT() {
        return lut;
    }

    public void setLUT(final ColorTable lut) {
        this.lut = lut == null ? new ColorTable8() : lut;
    }

    @Override
    public void convert(final R input, final ARGBType output) {
        final double a = input.getRealDouble();
        final int argb = lut.lookupARGB(min, max, a);
        output.set(argb);
    }

}
