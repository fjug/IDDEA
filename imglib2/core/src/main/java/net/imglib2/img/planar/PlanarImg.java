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

package net.imglib2.img.planar;

import java.util.ArrayList;

import net.imglib2.FlatIterationOrder;
import net.imglib2.img.AbstractNativeImg;
import net.imglib2.img.NativeImg;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.type.NativeType;

/**
 * A {@link NativeImg} that stores data in an array of 2d-slices each as a
 * linear array of basic types.  For types that are supported by ImageJ (byte,
 * short, int, float), an actual Planar is created or used to store the
 * data.  Alternatively, an {@link PlanarImg} can be created using
 * an already existing {@link Planar} instance.
 *
 * {@link PlanarImg PlanarContainers} provides a legacy layer to
 * apply imglib-based algorithm implementations directly on the data stored in
 * an ImageJ {@link Planar}.  For all types that are supported by ImageJ, the
 * {@link PlanarImg} provides access to the pixels of an
 * {@link Planar} instance that can be accessed ({@link #getPlanar()}.
 *
 * @author Funke
 * @author Preibisch
 * @author Saalfeld
 * @author Schindelin
 * @author Jan Funke
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 * @author Johannes Schindelin
 * @author Tobias Pietzsch
 */
public class PlanarImg< T extends NativeType< T >, A extends ArrayDataAccess<A> > extends AbstractNativeImg< T, A > implements PlanarAccess< A >
{
	final protected int numSlices;

	/*
	 * duplicate of long[] dimension as an int array.
	 */
	final protected int[] dimensions;

	final protected int[] sliceSteps;

	final protected ArrayList< A > mirror;

	public PlanarImg( final long[] dim, final int entitiesPerPixel )
	{
		this( null, dim, entitiesPerPixel );
	}

	PlanarImg( final A creator, final long[] dim, final int entitiesPerPixel )
	{
		super( dim, entitiesPerPixel );

		dimensions = new int[ n ];
		for ( int d = 0; d < n; ++d )
			dimensions[ d ] = ( int ) dim[ d ];

		if ( n > 2 )
		{
			sliceSteps = new int[ n ];
			sliceSteps[ 2 ] = 1;
			for ( int i = 3; i < n; ++i )
			{
				final int j = i - 1;
				sliceSteps[ i ] = dimensions[ j ] * sliceSteps[ j ];
			}
		}
		else
		{
			sliceSteps = null;
		}

		// compute number of slices
		int s = 1;
		for ( int d = 2; d < n; ++d )
			s *= dimensions[ d ];
		numSlices = s;

		mirror = new ArrayList< A >( numSlices );

		if ( creator == null)
		{
			for ( int i = 0; i < numSlices; ++i )
				mirror.add( null );
		}
		else
		{
			final int entitiesPerSlice = ( ( n > 1 ) ? dimensions[ 1 ] : 1 )  *  dimensions[ 0 ] * entitiesPerPixel;
			for ( int i = 0; i < numSlices; ++i )
				mirror.add( creator.createArray( entitiesPerSlice ) );
		}
	}

	/**
	 * This interface is implemented by all samplers on the {@link PlanarImg}.
	 * It allows the container to ask for the slice the sampler is currently in.
	 */
	public interface PlanarContainerSampler
	{
		/**
		 * @return the index of the slice the sampler is currently accessing.
		 */
		public int getCurrentSliceIndex();
	}

	@Override
	public A update( final Object c )
	{
		final int i = ( ( PlanarContainerSampler ) c ).getCurrentSliceIndex();
		return mirror.get( i < 0 ? 0 : ( i >= numSlices ? numSlices - 1 : i ) );
	}

	/**
   * @return total number of image planes
	 */
	public int numSlices() { return numSlices; }

	/**
	 * For a given >=2d location, estimate the pixel index in the stack slice.
	 *
	 * @param l
	 * @return
	 *
	 * TODO: remove this method? (it doesn't seem to be used anywhere)
	 */
	public final int getIndex( final int[] l )
	{
		if ( n > 1 )
			return l[ 1 ] * dimensions[ 0 ] + l[ 0 ];
		return l[ 0 ];
	}

	/**
	 * Compute a global position from the index of a slice and an index within that slice.
	 *
	 * @param sliceIndex    index of slice
	 * @param indexInSlice  index of element within slice
	 * @param position      receives global position of element
	 *
	 * TODO: move this method to AbstractPlanarCursor? (that seems to be the only place where it is needed)
	 */
	public void indexToGlobalPosition( int sliceIndex, final int indexInSlice, final int[] position )
	{
		if (n > 1)
		{
			position[ 1 ] = indexInSlice / dimensions[ 0 ];
			position[ 0 ] = indexInSlice - position[ 1 ] * dimensions[ 0 ];

			if ( n > 2 )
			{
				final int maxDim = n - 1;

				for ( int d = 2; d < maxDim; ++d )
				{
					final int j = sliceIndex / dimensions[ d ];
					position[ d ] = sliceIndex - j * dimensions[ d ];
					sliceIndex = j;
				}
				position[ maxDim ] = sliceIndex;
			}
		} else {
			position[ 0 ] = indexInSlice;
		}
	}

	/**
	 * Compute a global position from the index of a slice and an index within that slice.
	 *
	 * @param sliceIndex    index of slice
	 * @param indexInSlice  index of element within slice
	 * @param dim           which dimension of the position we are interested in
	 * @return              dimension dim of global position
	 *
	 * TODO: move this method to AbstractPlanarCursor? (that seems to be the only place where it is needed)
	 */
	public int indexToGlobalPosition( final int sliceIndex, final int indexInSlice, final int dim )
	{
		if ( dim == 0 )
			return indexInSlice % dimensions[ 0 ];
		else if ( dim == 1 )
			return indexInSlice / dimensions[ 0 ];
		else if ( dim < n )
			return ( sliceIndex / sliceSteps[ dim ] ) % dimensions[ dim ];
		else
			return 0;
	}

	@Override
	public PlanarCursor<T> cursor()
	{
		if ( n == 1 )
			return new PlanarCursor1D< T >( this );
		else if ( n == 2 )
			return new PlanarCursor2D< T >( this );
		else
			return new PlanarCursor< T >( this );
	}

	@Override
	public PlanarLocalizingCursor<T> localizingCursor()
	{
		if ( n == 1 )
			return new PlanarLocalizingCursor1D< T >( this );
		else if ( n == 2 )
			return new PlanarLocalizingCursor2D< T >( this );
		else
			return new PlanarLocalizingCursor<T>( this );
	}

	@Override
	public PlanarRandomAccess<T> randomAccess()
	{
		if ( n == 1 )
			return new PlanarRandomAccess1D<T>( this );
		return new PlanarRandomAccess<T>( this );
	}

	@Override
	public FlatIterationOrder iterationOrder()
	{
		return new FlatIterationOrder( this );
	}

	@Override
	public A getPlane( final int no ) { return mirror.get( no ); }

	@Override
	public void setPlane( final int no, final A plane ) { mirror.set( no, plane ); }

	@Override
	public PlanarImgFactory< T > factory() { return new PlanarImgFactory<T>(); }

	@Override
	public PlanarImg< T, ? > copy()
	{
		final PlanarImg< T, ? > copy = factory().create( dimension, firstElement().createVariable() );

		final PlanarCursor< T > cursor1 = this.cursor();
		final PlanarCursor< T > cursor2 = copy.cursor();

		while ( cursor1.hasNext() )
			cursor2.next().set( cursor1.next() );

		return copy;
	}
}
