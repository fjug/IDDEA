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
import java.util.Collections;
import java.util.Random;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.IntervalIndexer;

/**
 * TODO
 *
 */
public class PlanarRandomAccessBenchmark
{
	long[] dimensions;

	int numValues;

	int[] intData;

	long intDataSum;

	PlanarImg< IntType, ? > intImg;
	Img< IntType > intImgCopy;

	public void createSourceData()
	{
		dimensions = new long[] { 480, 480, 102 };

		numValues = 1;
		for ( int d = 0; d < dimensions.length; ++d )
			numValues *= dimensions[ d ];

		intData = new int[ numValues ];
		intDataSum = 0;
		Random random = new Random( 0 );
		for ( int i = 0; i < numValues; ++i )
		{
			intData[ i ] = random.nextInt();
			intDataSum += intData[ i ];
		}

		intImg = new PlanarImgFactory< IntType >().create( dimensions, new IntType() );
	}

	/**
	 * Fill intImg (a CellContainer with 40x40x40 cells) with data using flat array iteration order.
	 */
	public void fillImage()
	{
		int[] pos = new int[ dimensions.length ];
		RandomAccess< IntType > a = intImg.randomAccess();

		int[] idim = new int[ dimensions.length ];
		for ( int d = 0; d < dimensions.length; ++d )
			idim[ d ] = ( int ) dimensions[ d ];
		
		for ( int i = 0; i < numValues; ++i )
		{
			IntervalIndexer.indexToPosition( i, idim, pos );
			a.setPosition( pos );
			a.get().set( intData[ i ] );
		}
	}

	
	public void copyWithSourceIteration(Img< IntType > srcImg, Img< IntType > dstImg)
	{
		int[] pos = new int[ dimensions.length ];
		Cursor< IntType > src = srcImg.localizingCursor();
		RandomAccess< IntType > dst = dstImg.randomAccess();
		while( src.hasNext() ) {
			src.fwd();
			src.localize( pos );
			dst.setPosition( pos );
			dst.get().set( src.get() );
		}
	}

	public static Long median( ArrayList<Long> values )
	{
		Collections.sort(values);

		if (values.size() % 2 == 1)
			return values.get((values.size() + 1) / 2 - 1);

		long lower = values.get(values.size() / 2 - 1);
		long upper = values.get(values.size() / 2);

		return (lower + upper) / 2;
	}

	public interface Benchmark
	{
		public void run();
	}

	public static void benchmark( Benchmark b )
	{
		ArrayList<Long> times = new ArrayList<Long>( 100 );
		final int numRuns = 20;
		for ( int i = 0; i < numRuns; ++i )
		{
			long startTime = System.currentTimeMillis();
			b.run();
			long endTime = System.currentTimeMillis();
			times.add( endTime - startTime );
		}
		for ( int i = 0; i < numRuns; ++i )
		{
			System.out.println( "run " + i + ": " + times.get( i ) + " ms" );
		}
		System.out.println();
		System.out.println( "median: " + median( times ) + " ms" );
		System.out.println();
	}

	public static void main( String[] args )
	{
		final PlanarRandomAccessBenchmark randomAccessBenchmark = new PlanarRandomAccessBenchmark();
		randomAccessBenchmark.createSourceData();

		System.out.println( "benchmarking fill" );
		benchmark( new Benchmark()
		{
			@Override
			public void run()
			{
				randomAccessBenchmark.fillImage();
			}
		} );
		randomAccessBenchmark.intData = null;
		
		randomAccessBenchmark.intImgCopy = new PlanarImgFactory< IntType >().create( randomAccessBenchmark.dimensions, new IntType() );
		System.out.println( "benchmarking copy planar to planar" );
		benchmark( new Benchmark()
		{
			@Override
			public void run()
			{
				randomAccessBenchmark.copyWithSourceIteration( randomAccessBenchmark.intImg, randomAccessBenchmark.intImgCopy );
			}
		} );
		randomAccessBenchmark.intImgCopy = null;

		randomAccessBenchmark.intImgCopy = new ArrayImgFactory< IntType >().create( randomAccessBenchmark.dimensions, new IntType() );
		System.out.println( "benchmarking copy planar to array" );
		benchmark( new Benchmark()
		{
			@Override
			public void run()
			{
				randomAccessBenchmark.copyWithSourceIteration( randomAccessBenchmark.intImg, randomAccessBenchmark.intImgCopy );
			}
		} );

		System.out.println( "benchmarking copy array to planar" );
		benchmark( new Benchmark()
		{
			@Override
			public void run()
			{
				randomAccessBenchmark.copyWithSourceIteration( randomAccessBenchmark.intImgCopy, randomAccessBenchmark.intImg );
			}
		} );
		randomAccessBenchmark.intImgCopy = null;
	}
}
