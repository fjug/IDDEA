package viewer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import viewer.util.AbstractAnimator;

public class TextOverlayAnimator extends AbstractAnimator
{
	protected final Font font;

	protected final String text;

	protected final double fadeInTime;

	protected final double fadeOutTime;

	public static enum TextPosition
	{
		CENTER,
		BOTTOM_RIGHT
	}

	protected final TextPosition position;

	public TextOverlayAnimator( final String text, final long duration )
	{
		this( text, duration, TextPosition.BOTTOM_RIGHT );
	}

	public TextOverlayAnimator( final String text, final long duration, final TextPosition position )
	{
		this( text, duration, position, 0.2, 0.5 );
	}

	public TextOverlayAnimator( final String text, final long duration, final TextPosition position, final double fadeInTime, final double fadeOutTime )
	{
		this( text, duration, position, fadeInTime, fadeOutTime, new Font( "SansSerif", Font.BOLD, 20 ) );
	}

	public TextOverlayAnimator( final String text, final long duration, final TextPosition position, final double fadeInTime, final double fadeOutTime, final Font font )
	{
		super( duration );
		this.text = text;
		this.font = font;
		this.fadeInTime = fadeInTime;
		this.fadeOutTime = fadeOutTime;
		this.position = position;
	}

	public void paint( final Graphics2D g, final long time )
	{
		setTime( time );

		final FontRenderContext frc = g.getFontRenderContext();
		final TextLayout layout = new TextLayout( text, font, frc );
		final Rectangle2D bounds = layout.getBounds();
		final float x, y;
		if ( position == TextPosition.BOTTOM_RIGHT )
		{
			x = ( float ) ( g.getClipBounds().getWidth() - bounds.getWidth() - 10 );
			y = ( float ) ( g.getClipBounds().getHeight() - 10 );
		}
		else // if ( position == TextPosition.CENTER )
		{
			x = ( float ) ( g.getClipBounds().getWidth() - bounds.getWidth() ) / 2;
			y = ( float ) ( g.getClipBounds().getHeight() - bounds.getHeight() ) / 2;
		}

		final double t = ratioComplete();
		final float alpha;
		if ( t <= fadeInTime )
			alpha = ( float ) Math.sin( ( Math.PI / 2 ) * t / fadeInTime );
		else if ( t >= 1.0 - fadeOutTime )
			alpha = ( float ) Math.sin( ( Math.PI / 2 ) * ( 1.0 - t ) / ( fadeOutTime ) );
		else
			alpha = 1;

		g.setColor( new Color( 1f, 1f, 1f, alpha ) );
		layout.draw( g, x, y );
	}
}
