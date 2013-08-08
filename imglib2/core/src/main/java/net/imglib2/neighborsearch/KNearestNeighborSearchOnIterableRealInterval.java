/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2013 Stephan Preibisch, Tobias Pietzsch, Barry DeZonia,
 * Stephan Saalfeld, Albert Cardona, Curtis Rueden, Christian Dietz, Jean-Yves
 * Tinevez, Johannes Schindelin, Lee Kamentsky, Larry Lindsey, Grant Harris,
 * Mark Hiner, Aivar Grislis, Martin Horn, Nick Perry, Michael Zinsmaier,
 * Steffen Jaensch, Jan Funke, Mark Longair, and Dimiter Prodanov.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package net.imglib2.neighborsearch;

import net.imglib2.IterableRealInterval;
import net.imglib2.RealCursor;
import net.imglib2.RealLocalizable;
import net.imglib2.Sampler;

/**
 * <em>k</em>-nearest-neighbor search on {@link IterableRealInterval}
 * implemented as linear search.
 *
 * @author Stephan Saalfeld
 * @author Stephan Saalfeld <saalfeld@mpi-cbg.de>
 */
public class KNearestNeighborSearchOnIterableRealInterval< T > implements KNearestNeighborSearch< T >
{
	final protected IterableRealInterval< T > iterable;
	
	final protected int k, n;
	final protected RealCursor< T >[] elements;
	final protected double[] squareDistances;
	
	final protected double[] referenceLocation;
	
	/**
	 * Calculate the square Euclidean distance of a query location to the
	 * location stored in referenceLocation.
	 */
	final protected double squareDistance( final RealLocalizable query )
	{
		double squareSum = 0;
		for ( int d = 0; d < n; ++d )
		{
			final double distance = query.getDoublePosition( d ) - referenceLocation[ d ];
			squareSum += distance * distance;
		}
		return squareSum;
	}
	
	@SuppressWarnings( "unchecked" )
	public KNearestNeighborSearchOnIterableRealInterval( final IterableRealInterval< T > iterable, final int k )
	{
		this.iterable = iterable;
		this.k = k;
		n = iterable.numDimensions();
		
		elements = ( new RealCursor[ k ] );
		squareDistances = new double[ k ];
		referenceLocation = new double[ n ];
	}
	
	@Override
	public int numDimensions()
	{
		return n;
	}
	
	@Override
	public int getK() { return k; }
	
	@Override
	public void search( final RealLocalizable reference )
	{
		for ( int i = 0; i < k; ++i )
			squareDistances[ i ] = Double.MAX_VALUE;
		
		reference.localize( referenceLocation );
		
		final RealCursor< T > cursor = iterable.localizingCursor();
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			final double squareDistance = squareDistance( cursor );
			int i = k - 1;
			if ( squareDistances[ i ] > squareDistance )
			{
				final RealCursor< T > candidate = cursor.copyCursor();
				
				for ( int j = i - 1; i > 0 && squareDistances[ j ] > squareDistance; --i, --j )
				{
					squareDistances[ i ] = squareDistances[ j ];
					elements[ i ] = elements[ j ];
				}
				squareDistances[ i ] = squareDistance;
				elements[ i ] = candidate;
			}
		}
	}
	
	
	/* KNearestNeighborSearch */

	@Override
	public RealLocalizable getPosition( final int i )
	{
		return elements[ i ];
	}

	@Override
	public RealCursor< T > getSampler( final int i )
	{
		return elements[ i ];
	}
	
	@Override
	public double getSquareDistance( final int i )
	{
		return squareDistances[ i ];
	}

	@Override
	public double getDistance( final int i )
	{
		return Math.sqrt( squareDistances[ i ] );
	}
	
	
	/* NearestNeighborSearch */
	
	@Override
	public RealLocalizable getPosition()
	{
		return getPosition( 0 );
	}
	
	@Override
	public Sampler< T > getSampler()
	{
		return getSampler( 0 );
	}
	
	@Override
	public double getSquareDistance()
	{
		return getSquareDistance( 0 );
	}
	
	@Override
	public double getDistance()
	{
		return getDistance( 0 );
	}
	
	@Override
	public KNearestNeighborSearchOnIterableRealInterval< T > copy()
	{
		final KNearestNeighborSearchOnIterableRealInterval< T > copy = new KNearestNeighborSearchOnIterableRealInterval< T >( iterable, k );
		System.arraycopy( referenceLocation, 0, copy.referenceLocation, 0, referenceLocation.length );
		for ( int i = 0; i < k; ++i )
		{
			copy.elements[ i ] = elements[ i ];
			copy.squareDistances[ i ] = squareDistances[ i ];
		}
		return copy;
	}
}
