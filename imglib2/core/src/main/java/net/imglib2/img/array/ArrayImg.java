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

package net.imglib2.img.array;

import net.imglib2.FlatIterationOrder;
import net.imglib2.Interval;
import net.imglib2.img.AbstractNativeImg;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.util.IntervalIndexer;

/**
 * This {@link Img} stores an image in a single linear array of basic
 * types.  By that, it provides the fastest possible access to data while
 * limiting the number of basic types stored to {@link Integer#MAX_VALUE}.
 * Keep in mind that this does not necessarily reflect the number of pixels,
 * because a pixel can be stored in less than or more than a basic type entry.
 *
 * @param <T>
 * @param <A>
 *
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 * @author Stephan Saalfeld <saalfeld@mpi-cbg.de>
 */
public class ArrayImg< T extends NativeType< T >, A > extends AbstractNativeImg< T, A >
{
	final int[] steps, dim;

	// the DataAccess created by the ArrayContainerFactory
	final private A data;

	/**
	 * TODO check for the size of numPixels being < Integer.MAX_VALUE?
	 * TODO Type is suddenly not necessary anymore
	 *
	 * @param factory
	 * @param data
	 * @param dim
	 * @param entitiesPerPixel
	 */
	public ArrayImg( final A data, final long[] dim, final int entitiesPerPixel )
	{
		super( dim, entitiesPerPixel );
		this.dim = new int[ n ];
		for ( int d = 0; d < n; ++d )
			this.dim[ d ] = ( int )dim[ d ];

		this.steps = new int[ n ];
		IntervalIndexer.createAllocationSteps( this.dim, this.steps );
		this.data = data;
	}

	@Override
	public A update( final Object o )
	{
		return data;
	}

	@Override
	public ArrayCursor< T > cursor() { return new ArrayCursor< T >( this ); }

	@Override
	public ArrayLocalizingCursor< T > localizingCursor() { return new ArrayLocalizingCursor< T >( this ); }

	@Override
	public ArrayRandomAccess< T > randomAccess() { return new ArrayRandomAccess< T >( this ); }

	@Override
	public ArrayRandomAccess< T > randomAccess( final Interval interval ){ return randomAccess(); }

	@Override
	public FlatIterationOrder iterationOrder()
	{
		return new FlatIterationOrder( this );
	}

	@Override
	public ArrayImgFactory<T> factory() { return new ArrayImgFactory<T>(); }

	@Override
	public ArrayImg< T, ? > copy()
	{
		final ArrayImg< T, ? > copy = factory().create( dimension, firstElement().createVariable() );

		final ArrayCursor< T > source = this.cursor();
		final ArrayCursor< T > target = copy.cursor();

		while ( source.hasNext() )
			target.next().set( source.next() );

		return copy;
	}
}
