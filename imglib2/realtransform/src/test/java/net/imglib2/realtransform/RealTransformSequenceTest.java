package net.imglib2.realtransform;

import java.util.Random;

import net.imglib2.RealPoint;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class RealTransformSequenceTest
{
	static private AffineTransform3D a1, a2, a3, a123;
	static private double[] x = new double[ 3 ];
	static private double[] y = new double[ 3 ];
	static private RealPoint px = RealPoint.wrap( x );
	
	final static Random rnd = new Random( 0 );
	final static private double r()
	{
		return 10 * 2 * ( rnd.nextDouble() - 0.5 );
	}
	
	final static void add( final RealTransformSequence l )
	{
		l.add( a1 );
		l.add( a2 );
		l.add( a3 );
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		a1 = new AffineTransform3D();
		a2 = new AffineTransform3D();
		a3 = new AffineTransform3D();
		
		
		a1.set(
				r(), r(), r(), r(),
				r(), r(), r(), r(),
				r(), r(), r(), r() );
		a2.set(
				r(), r(), r(), r(),
				r(), r(), r(), r(),
				r(), r(), r(), r() );
		a3.set(
				r(), r(), r(), r(),
				r(), r(), r(), r(),
				r(), r(), r(), r() );
		
		a123 = new AffineTransform3D();
		a123.preConcatenate( a1 );
		a123.preConcatenate( a2 );
		a123.preConcatenate( a3 );
		
		x[ 0 ] = r();
		x[ 1 ] = r();
		x[ 2 ] = r();
		
		a123.apply( x, y );
	}

	@Test
	public void testNumSourceDimensions()
	{
		final RealTransformSequence l = new RealTransformSequence();
		Assert.assertEquals( 0, l.numSourceDimensions() );
		add( l );
		Assert.assertEquals( 3, l.numSourceDimensions() );
	}

	@Test
	public void testNumTargetDimensions()
	{
		final RealTransformSequence l = new RealTransformSequence();
		Assert.assertEquals( 0, l.numTargetDimensions() );
		add( l );
		Assert.assertEquals( 3, l.numTargetDimensions() );
	}

	@Test
	public void testApplyDoubleArrayDoubleArray()
	{
		final RealTransformSequence l = new RealTransformSequence();
		add( l );
		final double[] y1 = new double[ 3 ];
		
		l.apply( x, y1 );
		Assert.assertArrayEquals( y, y1, 0.001 );
	}

	@Test
	public void testApplyFloatArrayFloatArray()
	{
		final RealTransformSequence l = new RealTransformSequence();
		add( l );
		final float[] x1 = new float[ 3 ];
		x1[ 0 ] = ( float )x[ 0 ];
		x1[ 1 ] = ( float )x[ 1 ];
		x1[ 2 ] = ( float )x[ 2 ];
		final float[] y1 = new float[ 3 ];
		
		l.apply( x1, y1 );
		
		final double[] y1d = new double[ 3 ];
		y1d[ 0 ] = y1[ 0 ];
		y1d[ 1 ] = y1[ 1 ];
		y1d[ 2 ] = y1[ 2 ];
		
		Assert.assertArrayEquals( y, y1d, 0.001 );
	}

	@Test
	public void testApplyRealLocalizableRealPositionable()
	{
		final RealTransformSequence l = new RealTransformSequence();
		add( l );
		final double[] y1 = new double[ 3 ];
		final RealPoint py1 = RealPoint.wrap( y1 );
		
		l.apply( px, py1 );
		Assert.assertArrayEquals( y, y1, 0.001 );
	}
	
	@Test
	public void testCopy()
	{
		final RealTransformSequence l = new RealTransformSequence();
		add( l );
		
		final RealTransformSequence lCopy = l.copy();
		
		/* copy is a deep copy */
		Assert.assertNotSame( l, lCopy );
		for ( int i = 0; i < l.transforms.size(); ++i )
			Assert.assertNotSame( l.transforms.get( i ), lCopy.transforms.get( i ) );
		
		/* copy performs correctly */
		final double[] y1 = new double[ 3 ];
		lCopy.apply( x, y1 );
		Assert.assertArrayEquals( y, y1, 0.001 );
	}
}
