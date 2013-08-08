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

package net.imglib2.img.cell;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.IntervalIndexer;

import org.junit.Before;
import org.junit.Test;

/**
 * TODO
 *
 */
public class CellCursorTest
{
	long[] dimensions;

	int numValues;

	int[] intData;

	long intDataSum;

	CellImg< IntType, ?, ? > intImg;

	@Before
	public void createSourceData()
	{
		dimensions = new long[] { 16, 37, 5, 13 };

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
		
		intImg = new CellImgFactory< IntType >( 4 ).create( dimensions, new IntType() );

		long[] pos = new long[ dimensions.length ];
		RandomAccess< IntType > a = intImg.randomAccess();

		for ( int i = 0; i < numValues; ++i )
		{
			IntervalIndexer.indexToPosition( i, dimensions, pos );
			a.setPosition( pos );
			a.get().set( intData[ i ] );
		}
	}

	@Test
	public void testSumWithCursor()
	{
		long sum = 0;
		Cursor< IntType > cursor = intImg.cursor();
		while ( cursor.hasNext() ) {
			sum += cursor.next().get();
		}

		assertTrue( sum == intDataSum );
	}

	@Test
	public void testResetWithCursor()
	{
		Cursor< IntType > cursor = intImg.cursor();
		int v1 = cursor.next().get();
		long[] p1 = new long[ dimensions.length ];
		cursor.localize( p1 );

		cursor.reset(); 
		int v2 = cursor.next().get();
		long[] p2 = new long[ dimensions.length ];
		cursor.localize( p2 );

		assertTrue( v1 == v2 );
		assertArrayEquals( p1, p2 );
	}

	@Test
	public void testJmpWithCursor()
	{
		int steps = 43;
		Cursor< IntType > cursor1 = intImg.cursor();
		for ( int i = 0; i < steps; ++i )
			cursor1.fwd();
		int v1 = cursor1.next().get();
		long[] p1 = new long[ dimensions.length ];
		cursor1.localize( p1 );

		Cursor< IntType > cursor2 = intImg.cursor();
		cursor2.jumpFwd( steps );
		int v2 = cursor2.next().get();
		long[] p2 = new long[ dimensions.length ];
		cursor2.localize( p2 );

		assertTrue( v1 == v2 );
		assertArrayEquals( p1, p2 );
	}

	@Test
	public void testSumWithLocalizingCursor()
	{
		long sum = 0;
		Cursor< IntType > cursor = intImg.localizingCursor();
		while ( cursor.hasNext() ) {
			sum += cursor.next().get();
		}

		assertTrue( sum == intDataSum );
	}
	@Test
	public void testResetWithLocalizingCursor()
	{
		Cursor< IntType > cursor = intImg.localizingCursor();
		int v1 = cursor.next().get();
		long[] p1 = new long[ dimensions.length ];
		cursor.localize( p1 );

		cursor.reset(); 
		int v2 = cursor.next().get();
		long[] p2 = new long[ dimensions.length ];
		cursor.localize( p2 );

		assertTrue( v1 == v2 );
		assertArrayEquals( p1, p2 );
	}

	@Test
	public void testJmpWithLocalizingCursor()
	{
		int steps = 43;
		Cursor< IntType > cursor1 = intImg.localizingCursor();
		for ( int i = 0; i < steps; ++i )
			cursor1.fwd();
		int v1 = cursor1.next().get();
		long[] p1 = new long[ dimensions.length ];
		cursor1.localize( p1 );

		Cursor< IntType > cursor2 = intImg.localizingCursor();
		cursor2.jumpFwd( steps );
		int v2 = cursor2.next().get();
		long[] p2 = new long[ dimensions.length ];
		cursor2.localize( p2 );

		assertTrue( v1 == v2 );
		assertArrayEquals( p1, p2 );
	}

	@Test
	public void testSumWithRandomAccess()
	{
		long sum = 0;
		RandomAccess< IntType > access = intImg.randomAccess();
		long[] position = new long[ dimensions.length ];
		for ( int d = 0; d < dimensions.length; ++d )
			position[ d ] = 0;

		for ( int i = 0; i < numValues; ++i )
		{
			access.setPosition( position );
			sum += access.get().get();
			for ( int d = 0; d < dimensions.length; ++d )
				if ( ++position[ d ] >= dimensions[ d ] ) position[ d ] = 0;
				else break;
		}
			
		assertTrue( sum == intDataSum );
	}
}
