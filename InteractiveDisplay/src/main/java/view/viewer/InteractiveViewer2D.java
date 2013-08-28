package view.viewer;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import view.JHotDrawInteractiveDisplay2D;
import net.imglib2.RandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.img.imageplus.ImagePlusImg;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.ui.AffineTransformType2D;
import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.TransformEventHandler2D;
import net.imglib2.ui.util.Defaults;
import net.imglib2.ui.util.InterpolatingSource;
import view.JHotDrawInteractiveDisplay2D;

/**
 * Interactive viewer for a 2D {@link RandomAccessible}.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class InteractiveViewer2D< T extends NumericType< T > > extends InteractiveRealViewer< T, AffineTransform2D, JHotDrawInteractiveDisplay2D< AffineTransform2D > >
{
	/**
	 * Create an interactive viewer for a 2D {@link RandomAccessible}.
	 *
	 * @param width
	 *            window width.
	 * @param height
	 *            window height.
	 * @param source
	 *            The source image to display. It is assumed that the source is
	 *            extended to infinity.
	 * @param sourceTransform
	 *            Transformation from source to global coordinates. This is
	 *            useful for pre-scaling when showing anisotropic data, for
	 *            example.
	 * @param converter
	 *            Converter from the source type to argb for rendering the
	 *            source.
	 */
	public InteractiveViewer2D( final int width, final int height, final RandomAccessible< T > source, final AffineTransform2D sourceTransform, final Converter< ? super T, ARGBType > converter )
	{
		this( width, height, new InterpolatingSource< T, AffineTransform2D >( source, sourceTransform, converter ) );
	}

	public InteractiveViewer2D( final int width, final int height, final RandomAccessible< T > source, final Converter< ? super T, ARGBType > converter )
	{
		this( width, height, source, new AffineTransform2D(), converter );
	}
	
	public InteractiveViewer2D( final int width, final int height, final InterpolatingSource< T, AffineTransform2D > interpolatingSource )
	{
		super( AffineTransformType2D.instance,
				new JHotDrawInteractiveDisplay2D< AffineTransform2D >( width, height, null, TransformEventHandler2D.factory() ),
				Defaults.rendererFactory( AffineTransformType2D.instance, interpolatingSource ) );

        this.source = interpolatingSource.getInterpolatedSource();

		// add KeyHandler for toggling interpolation
		display.addHandler( new KeyAdapter() {
			@Override
			public void keyPressed( final KeyEvent e )
			{
				if ( e.getKeyCode() == KeyEvent.VK_I )
				{
					interpolatingSource.switchInterpolation();
					requestRepaint();
				}
			}
		});
	}
}