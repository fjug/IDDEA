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

package net.imglib2.converter;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Sampler;
import net.imglib2.converter.read.ConvertedIterableInterval;
import net.imglib2.converter.read.ConvertedRandomAccessible;
import net.imglib2.converter.read.ConvertedRandomAccessibleInterval;
import net.imglib2.converter.readwrite.SamplerConverter;
import net.imglib2.converter.readwrite.WriteConvertedIterableInterval;
import net.imglib2.converter.readwrite.WriteConvertedIterableRandomAccessibleInterval;
import net.imglib2.converter.readwrite.WriteConvertedRandomAccessible;
import net.imglib2.converter.readwrite.WriteConvertedRandomAccessibleInterval;
import net.imglib2.type.Type;

/**
 * Convenience factory methods for sample conversion.
 *
 * @author Stephan Saalfeld <saalfeld@mpi-cbg.de>
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class Converters
{
	/**
	 * Create a {@link RandomAccessible} whose {@link RandomAccess
	 * RandomAccesses} {@link RandomAccess#get()} you a converted sample.
	 * Conversion is done on-the-fly when reading values. Writing to the
	 * converted {@link RandomAccessibleInterval} has no effect.
	 *
	 * @param source
	 * @param converter
	 * @param b
	 * @return a converted {@link RandomAccessible} whose {@link RandomAccess
	 *         RandomAccesses} perform on-the-fly value conversion using the
	 *         provided converter.
	 */
	@SuppressWarnings( "unchecked" )
	final static public < A, B extends Type< B > > RandomAccessible< B > convert(
			final RandomAccessible< A > source,
			final Converter< A, B > converter,
			final B b )
	{
		if ( TypeIdentity.class.isInstance( converter ) )
			return ( RandomAccessible< B > )source;
		return new ConvertedRandomAccessible< A, B >( source, converter, b );
	}

	/**
	 * Create a {@link RandomAccessible} whose {@link RandomAccess
	 * RandomAccesses} {@link RandomAccess#get()} you a converted sample.
	 * Conversion is done on-the-fly both when reading and writing values.
	 *
	 * @param source
	 * @param converter
	 * @return a converted {@link RandomAccessible} whose {@link RandomAccess
	 *         RandomAccesses} perform on-the-fly value conversion using the
	 *         provided converter.
	 */
	final static public < A, B extends Type< B > > WriteConvertedRandomAccessible< A, B > convert(
			final RandomAccessible< A > source,
			final SamplerConverter< A, B > converter )
	{
		return new WriteConvertedRandomAccessible< A, B >( source, converter );
	}

	/**
	 * Create a {@link RandomAccessibleInterval} whose {@link RandomAccess
	 * RandomAccesses} {@link RandomAccess#get()} you a converted sample.
	 * Conversion is done on-the-fly when reading values. Writing to the
	 * converted {@link RandomAccessibleInterval} has no effect.
	 *
	 * @param source
	 * @param converter
	 * @param b
	 * @return a converted {@link RandomAccessibleInterval} whose
	 *         {@link RandomAccess RandomAccesses} perform on-the-fly value
	 *         conversion using the provided converter.
	 */
	@SuppressWarnings( "unchecked" )
	final static public < A, B extends Type< B > > RandomAccessibleInterval< B > convert(
			final RandomAccessibleInterval< A > source,
			final Converter< A, B > converter,
			final B b )
	{
		if ( TypeIdentity.class.isInstance( converter ) )
			return ( RandomAccessibleInterval< B > )source;
		return new ConvertedRandomAccessibleInterval< A, B >( source, converter, b );
	}

	/**
	 * Create a {@link RandomAccessibleInterval} whose {@link RandomAccess
	 * RandomAccesses} {@link RandomAccess#get()} you a converted sample.
	 * Conversion is done on-the-fly both when reading and writing values.
	 *
	 * @param source
	 * @param converter
	 * @return a converted {@link RandomAccessibleInterval} whose
	 *         {@link RandomAccess RandomAccesses} perform on-the-fly value
	 *         conversion using the provided converter.
	 */
	final static public < A, B extends Type< B > > WriteConvertedRandomAccessibleInterval< A, B > convert(
			final RandomAccessibleInterval< A > source,
			final SamplerConverter< A, B > converter )
	{
		return new WriteConvertedRandomAccessibleInterval< A, B >( source, converter );
	}

	/**
	 * Create a {@link IterableInterval} whose {@link Cursor Cursors}
	 * {@link Cursor#get()} you a converted sample. Conversion is done
	 * on-the-fly when reading values. Writing to the converted
	 * {@link IterableInterval} has no effect.
	 *
	 * @param source
	 * @param converter
	 * @param b
	 * @return a converted {@link IterableInterval} whose {@link Cursor Cursors}
	 *         perform on-the-fly value conversion using the provided converter.
	 */
	@SuppressWarnings( "unchecked" )
	final static public < A, B extends Type< B > > IterableInterval< B > convert(
			final IterableInterval< A > source,
			final Converter< A, B > converter,
			final B b )
	{
		if ( TypeIdentity.class.isInstance( converter ) )
			return ( IterableInterval< B > )source;
		return new ConvertedIterableInterval< A, B >( source, converter, b );
	}

	/**
	 * Create an {@link IterableInterval} whose {@link Cursor Cursors}
	 * {@link Cursor#get()} you a converted sample. Conversion is done
	 * on-the-fly both when reading and writing values.
	 *
	 * @param source
	 * @param converter
	 * @return a converted {@link IterableInterval} whose {@link Cursor Cursors}
	 *         perform on-the-fly value conversion using the provided converter.
	 */
	final static public < A, B extends Type< B > > WriteConvertedIterableInterval< A, B > convert(
			final IterableInterval< A > source,
			final SamplerConverter< A, B > converter )
	{
		return new WriteConvertedIterableInterval< A, B >( source, converter );
	}

	/**
	 * Create an {@link WriteConvertedIterableRandomAccessibleInterval} whose
	 * {@link RandomAccess RandomAccesses} and {@link Cursor Cursors}
	 * {@link Cursor#get()} you a converted sample. Conversion is done
	 * on-the-fly both when reading and writing values.
	 *
	 * @param source
	 * @param converter
	 * @return a {@link WriteConvertedIterableRandomAccessibleInterval} whose
	 *         {@link Sampler Samplers} perform on-the-fly value conversion
	 *         using the provided converter.
	 */
	final static public < A, B extends Type< B >, S extends RandomAccessible< A > & IterableInterval< A > >
		WriteConvertedIterableRandomAccessibleInterval< A, B, S > convertRandomAccessibleIterableInterval(
			final S source,
			final SamplerConverter< A, B > converter )
	{
		return new WriteConvertedIterableRandomAccessibleInterval< A, B, S >( source, converter );
	}
}
