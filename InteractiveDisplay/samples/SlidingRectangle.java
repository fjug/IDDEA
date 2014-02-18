import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.type.numeric.integer.LongType;
import plugin.ModelPlugin;

import net.imglib2.ui.OverlayRenderer;

import java.awt.*;
import java.util.*;
import java.util.Random;


/**
 * Sample Mandelbrot model plugin for IDDEA
 * You can copy and paste it in SourceDesigner
 * Process -> Inject Source
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/3/13
 */
public class LifeModel extends ModelPlugin<LongType> {

    LifeRealRandomAccessible source = new LifeRealRandomAccessible();

    public LifeModel()
    {
        // Overlay painters are added here
        painters.add(new SourceInfoOverlay());
    }

    boolean go = true;

    @Override
    public String getName() {
        return "MandelbrotModel";
    }

    @Override
    public String getAuthor() {
        return "HongKee Moon";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public RealRandomAccessible<LongType> getSource() {
        return source;
    }

    @Override
    public void animate()
    {
		float[] pos = source.getXY();
		float x = pos[0];
		float y = pos[1];

		source.setXY(x + 10, y + 10);
    }

    public class SourceInfoOverlay implements OverlayRenderer {
	    protected String sourceName = "Sliding Rectangle Source Example";
	
	    protected String timepointString = new Date().toString();
	
	    /**
	     * Update data to show in the overlay.
	     */
	    public synchronized void updateInfo( final String source, final String time )
	    {
	        sourceName = source;
	        timepointString = time;
	    }
	
	    @Override
	    public void drawOverlays(Graphics g) {
	        Color c = g.getColor();
	        g.setColor(Color.white);
	        g.setFont( new Font( "Monospaced", Font.PLAIN, 12 ) );
	        g.drawString( sourceName, ( int ) g.getClipBounds().getWidth() / 2 - 100, 12 );
	        g.drawString( timepointString, ( int ) g.getClipBounds().getWidth() - 240, 12 );
	        g.setColor(c);
	    }
	
	    @Override
	    public void setCanvasSize(int width, int height) {
	        // Change canvas size
	    }
    }

    // Inner class MandelbrotRealRandomAccessible
    class LifeRealRandomAccessible implements RealRandomAccessible< LongType >
    {
		final Random rnd = new Random( System.currentTimeMillis() );	// we simulate with 5 races
		
		final int numRaces = 5;
		
		// the overall growth of all races per round
		final float growth = 1.05f;
		
		// all races above this weight will die of lack of food
		final float maxWeight = 1.1f;
		
		// chance for a epedemic (in percent)
		final float epidemic = 0.1f;
		
		// the sigma of the gaussian convolution, determines how far each race spreads from a spot
		final float sigma = 2.5f;

		float posX = 0; 
		float posY = 0;

		public void setXY(float x, float y)
		{
			this.posX = x;
			this.posY = y;	
		}

		public float[] getXY()
		{
			return new float[] {posX, posY};
		}

		public LifeRealRandomAccessible()
		{
		}
		
		public LifeRealRandomAccessible( final long maxIterations )
		{
		}
		
		final private long getLife(final double x, final double y)
		{	
			if(x > posX && x < posX + 20 && y > posY && y < posY + 20)
			{
				return (long) (x - posX + y - posY);
			}
			else if ( (y - x) > 1 )
			{
				return 255;
			}
			else
				return 0;
		}

		public class LifeRealRandomAccess extends RealPoint implements RealRandomAccess< LongType >
		{
			final protected LongType t;
			public LifeRealRandomAccess()
			{
				super( 2 ); // number of dimensions is 2
				t = new LongType();
			}
			
			@Override
			public LongType get()
			{
				t.set( getLife( position[ 0 ], position[ 1 ]) );
				return t;
			}
			
			@Override
			public LifeRealRandomAccess copyRealRandomAccess()
			{
				return copy();
			}
			
			@Override
			public LifeRealRandomAccess copy()
			{
				final LifeRealRandomAccess a = new LifeRealRandomAccess();
				a.setPosition( this );
				return a;
			}
		}

		@Override
		public int numDimensions()
		{
			return 2;
		}
		
		@Override
		public LifeRealRandomAccess realRandomAccess()
		{
			return new LifeRealRandomAccess();
		}
		
		@Override
		public LifeRealRandomAccess realRandomAccess( final RealInterval interval )
		{
			return realRandomAccess();
		}
	}
}
