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

package net.imglib2.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;

import org.junit.Test;

/**
 * TODO
 *
 */
public class KthElementTest
{
	@Test
	public void testMedianByte()
	{
		byte[] values = new byte[] {2, -1, 1, 100, 123, 12};
		byte[] sortedValues = values.clone();

		final int i = 1;
		final int j = 3;
		int k = i + ( j - i ) / 2;
		KthElement.kthElement( i, j, k, values );
		Arrays.sort( sortedValues, i, j+1 );

		// the elements at the k-th positions should be equal 
		assertEquals( values[ k ], sortedValues[ k ] );

		// the elements before the k-th should be equal or smaller than the k-th
		for ( int p = i; p < k; ++p )
			assertTrue( values[ p ] <= values[ k ] );

		// the elements after the k-th should be equal or greater than the k-th
		for ( int p = k + 1; p <= j; ++p )
			assertTrue( values[ p ] >= values[ k ] );

		// all elements should be contained in list, i.e., after sorting lists should be identical
		Arrays.sort( values, i, j+1 );
		assertArrayEquals( sortedValues, values );
	}

	@Test
	public void testMedianByteFull()
	{
		byte[] values = new byte[] {2, -1, 1, 100, 123, 12};
		byte[] sortedValues = values.clone();

		final int i = 0;
		final int j = values.length - 1;
		int k = i + ( j - i ) / 2;
		KthElement.kthElement( k, values );
		Arrays.sort( sortedValues );

		// the elements at the k-th positions should be equal 
		assertEquals( values[ k ], sortedValues[ k ] );

		// the elements before the k-th should be equal or smaller than the k-th
		for ( int p = i; p < k; ++p )
			assertTrue( values[ p ] <= values[ k ] );

		// the elements after the k-th should be equal or greater than the k-th
		for ( int p = k + 1; p <= j; ++p )
			assertTrue( values[ p ] >= values[ k ] );

		// all elements should be contained in list, i.e., after sorting lists should be identical
		Arrays.sort( values );
		assertArrayEquals( sortedValues, values );
	}

	@Test
	public void testMinByteFull()
	{
		byte[] values = new byte[] {2, -1, 1, 100, 123, 12};
		byte[] sortedValues = values.clone();

		final int i = 0;
		final int j = values.length - 1;
		int k = 0;
		KthElement.kthElement( k, values );
		Arrays.sort( sortedValues );

		// the elements at the k-th positions should be equal 
		assertEquals( values[ k ], sortedValues[ k ] );

		// the elements before the k-th should be equal or smaller than the k-th
		for ( int p = i; p < k; ++p )
			assertTrue( values[ p ] <= values[ k ] );

		// the elements after the k-th should be equal or greater than the k-th
		for ( int p = k + 1; p <= j; ++p )
			assertTrue( values[ p ] >= values[ k ] );

		// all elements should be contained in list, i.e., after sorting lists should be identical
		Arrays.sort( values );
		assertArrayEquals( sortedValues, values );
	}

	@Test
	public void testMaxByteFull()
	{
		byte[] values = new byte[] {2, -1, 1, 100, 123, 12};
		byte[] sortedValues = values.clone();

		final int i = 0;
		final int j = values.length - 1;
		int k = j;
		KthElement.kthElement( k, values );
		Arrays.sort( sortedValues );

		// the elements at the k-th positions should be equal 
		assertEquals( values[ k ], sortedValues[ k ] );

		// the elements before the k-th should be equal or smaller than the k-th
		for ( int p = i; p < k; ++p )
			assertTrue( values[ p ] <= values[ k ] );

		// the elements after the k-th should be equal or greater than the k-th
		for ( int p = k + 1; p <= j; ++p )
			assertTrue( values[ p ] >= values[ k ] );

		// all elements should be contained in list, i.e., after sorting lists should be identical
		Arrays.sort( values );
		assertArrayEquals( sortedValues, values );
	}
	
	@Test
	public void testMedianShortFull()
	{
		short[] values = new short[] {2, -1, 1, 100, 123, 12, 19, 12183, 123, 12, 6453, 233};
		short[] sortedValues = values.clone();

		final int i = 0;
		final int j = values.length - 1;
		int k = i + ( j - i ) / 2;
		KthElement.kthElement( k, values );
		Arrays.sort( sortedValues );

		// the elements at the k-th positions should be equal 
		assertEquals( values[ k ], sortedValues[ k ] );

		// the elements before the k-th should be equal or smaller than the k-th
		for ( int p = i; p < k; ++p )
			assertTrue( values[ p ] <= values[ k ] );

		// the elements after the k-th should be equal or greater than the k-th
		for ( int p = k + 1; p <= j; ++p )
			assertTrue( values[ p ] >= values[ k ] );

		// all elements should be contained in list, i.e., after sorting lists should be identical
		Arrays.sort( values );
		assertArrayEquals( sortedValues, values );
	}

	@Test
	public void testMedianIntFull()
	{
		int[] values = new int[] {2, 233};
		int[] sortedValues = values.clone();

		final int i = 0;
		final int j = values.length - 1;
		int k = i + ( j - i ) / 2;
		KthElement.kthElement( k, values );
		Arrays.sort( sortedValues );

		// the elements at the k-th positions should be equal 
		assertEquals( values[ k ], sortedValues[ k ] );

		// the elements before the k-th should be equal or smaller than the k-th
		for ( int p = i; p < k; ++p )
			assertTrue( values[ p ] <= values[ k ] );

		// the elements after the k-th should be equal or greater than the k-th
		for ( int p = k + 1; p <= j; ++p )
			assertTrue( values[ p ] >= values[ k ] );

		// all elements should be contained in list, i.e., after sorting lists should be identical
		Arrays.sort( values );
		assertArrayEquals( sortedValues, values );
	}

	@Test
	public void testMedianLongFull()
	{
		long[] values = new long[] {2, -123890123, 12, 6453, 233, 1, 1, 1, 1, 1};
		long[] sortedValues = values.clone();

		final int i = 0;
		final int j = values.length - 1;
		int k = i + ( j - i ) / 2;
		KthElement.kthElement( k, values );
		Arrays.sort( sortedValues );

		// the elements at the k-th positions should be equal 
		assertEquals( values[ k ], sortedValues[ k ] );

		// the elements before the k-th should be equal or smaller than the k-th
		for ( int p = i; p < k; ++p )
			assertTrue( values[ p ] <= values[ k ] );

		// the elements after the k-th should be equal or greater than the k-th
		for ( int p = k + 1; p <= j; ++p )
			assertTrue( values[ p ] >= values[ k ] );

		// all elements should be contained in list, i.e., after sorting lists should be identical
		Arrays.sort( values );
		assertArrayEquals( sortedValues, values );
	}

	@Test
	public void testMedianFloatFull()
	{
		float[] values = new float[] {2, -123890123, 12, 6453, 233, 1, 1, 1, 1, 1};
		float[] sortedValues = values.clone();

		final int i = 0;
		final int j = values.length - 1;
		int k = i + ( j - i ) / 2;
		KthElement.kthElement( k, values );
		Arrays.sort( sortedValues );

		// the elements at the k-th positions should be equal 
		assertEquals( values[ k ], sortedValues[ k ], 0 );

		// the elements before the k-th should be equal or smaller than the k-th
		for ( int p = i; p < k; ++p )
			assertTrue( values[ p ] <= values[ k ] );

		// the elements after the k-th should be equal or greater than the k-th
		for ( int p = k + 1; p <= j; ++p )
			assertTrue( values[ p ] >= values[ k ] );

		// all elements should be contained in list, i.e., after sorting lists should be identical
		Arrays.sort( values );
		assertArrayEquals( sortedValues, values, 0 );
	}
	
	@Test
	public void testMedianDoubleFull()
	{
		double[] values = new double[] {2, 453, 233, 1, 1, 1, 1, 1, 0.7};
		double[] sortedValues = values.clone();

		final int i = 0;
		final int j = values.length - 1;
		int k = i + ( j - i ) / 2;
		KthElement.kthElement( k, values );
		Arrays.sort( sortedValues );

		// the elements at the k-th positions should be equal 
		assertEquals( values[ k ], sortedValues[ k ], 0 );

		// the elements before the k-th should be equal or smaller than the k-th
		for ( int p = i; p < k; ++p )
			assertTrue( values[ p ] <= values[ k ] );

		// the elements after the k-th should be equal or greater than the k-th
		for ( int p = k + 1; p <= j; ++p )
			assertTrue( values[ p ] >= values[ k ] );

		// all elements should be contained in list, i.e., after sorting lists should be identical
		Arrays.sort( values, i, j+1 );
		assertArrayEquals( sortedValues, values, 0 );
	}
	
	@Test
	public void testMedianFloatObject()
	{
		ArrayList< Float > values = new ArrayList< Float >();
		for( float f : new float[] {123, 21, 12912, 321, 32, 12345, 249, 5823, 834, 10, 23, 329 } ) {
			values.add( f );
		}
		@SuppressWarnings( "unchecked" )
		ArrayList< Float > sortedValues = ( ArrayList< Float > ) values.clone();

		final int i = 4;
		final int j = 9;
		int k = i + ( j - i ) / 2;
		KthElement.kthElement( i, j, k, values );
		Collections.sort( sortedValues.subList( i, j+1 ) );

		// the elements at the k-th positions should be equal 
		assertEquals( values.get( k ), sortedValues.get( k ), 0 );

		// the elements before the k-th should be equal or smaller than the k-th
		for ( int p = i; p < k; ++p )
			assertTrue( values.get( p ) <= values.get( k ) );

		// the elements after the k-th should be equal or greater than the k-th
		for ( int p = k + 1; p <= j; ++p )
			assertTrue( values.get( p ) >= values.get( k ) );

		// all elements should be contained in list, i.e., after sorting lists should be identical
		Collections.sort( values.subList( i, j+1 ) );
		for ( int p = i; p <= j; ++p )
			assertTrue( values.get( p ).equals( sortedValues.get( p ) ) );
	}
	
	@Test
	public void testMedianFloatObjectFull()
	{
		ArrayList< Float > values = new ArrayList< Float >();
		for( float f : new float[] {123, 21, 12912, 321, 32, 12345, 249, 5823, 834, 10, 23, 329 } ) {
			values.add( f );
		}
		@SuppressWarnings( "unchecked" )
		ArrayList< Float > sortedValues = ( ArrayList< Float > ) values.clone();

		final int i = 0;
		final int j = values.size() - 1;
		int k = i + ( j - i ) / 2;
		KthElement.kthElement( k, values );
		Collections.sort( sortedValues );

		// the elements at the k-th positions should be equal 
		assertEquals( values.get( k ), sortedValues.get( k ), 0 );

		// the elements before the k-th should be equal or smaller than the k-th
		for ( int p = i; p < k; ++p )
			assertTrue( values.get( p ) <= values.get( k ) );

		// the elements after the k-th should be equal or greater than the k-th
		for ( int p = k + 1; p <= j; ++p )
			assertTrue( values.get( p ) >= values.get( k ) );

		// all elements should be contained in list, i.e., after sorting lists should be identical
		Collections.sort( values );
		for ( int p = i; p <= j; ++p )
			assertTrue( values.get( p ).equals( sortedValues.get( p ) ) );
	}
	
	public static class ComparableComparator< T extends Comparable< T > > implements Comparator< T >
	{
		@Override
		public int compare( T o1, T o2 )
		{
			return o1.compareTo( o2 );
		}
	}

	@Test
	public void testMedianFloatObjectFullComparator()
	{
		ArrayList< Float > values = new ArrayList< Float >();
		for( float f : new float[] {123, 21, 12912, 321, 32, 12345, 249, 5823, 834, 10, 23, 329 } ) {
			values.add( f );
		}
		@SuppressWarnings( "unchecked" )
		ArrayList< Float > sortedValues = ( ArrayList< Float > ) values.clone();

		final int i = 0;
		final int j = values.size() - 1;
		int k = i + ( j - i ) / 2;
		KthElement.kthElement( k, values, new ComparableComparator< Float >() );
		Collections.sort( sortedValues );

		// the elements at the k-th positions should be equal 
		assertEquals( values.get( k ), sortedValues.get( k ), 0 );

		// the elements before the k-th should be equal or smaller than the k-th
		for ( int p = i; p < k; ++p )
			assertTrue( values.get( p ) <= values.get( k ) );

		// the elements after the k-th should be equal or greater than the k-th
		for ( int p = k + 1; p <= j; ++p )
			assertTrue( values.get( p ) >= values.get( k ) );

		// all elements should be contained in list, i.e., after sorting lists should be identical
		Collections.sort( values );
		for ( int p = i; p <= j; ++p )
			assertTrue( values.get( p ).equals( sortedValues.get( p ) ) );
	}
	
	@Test
	public void testMedianFloatObjectIterator()
	{
		ArrayList< Float > values = new ArrayList< Float >();
		for( float f : new float[] {123, 21, 12912, 321, 32, 12345, 249, 5823, 834, 10, 23, 329 } ) {
			values.add( f );
		}
		@SuppressWarnings( "unchecked" )
		ArrayList< Float > sortedValues = ( ArrayList< Float > ) values.clone();

		final int i = 4;
		final int j = 9;
		ListIterator< Float > iIterator = values.listIterator( i );
		ListIterator< Float > jIterator = values.listIterator( j + 1 );
		int k = i + ( j - i ) / 2;
		KthElement.kthElement( iIterator, jIterator, k );
		Collections.sort( sortedValues.subList( i, j+1 ) );
		
		// iIterator should be at k
		assertEquals( iIterator.nextIndex() - 1, k );

		// the elements at the k-th positions should be equal 
		assertEquals( values.get( k ), sortedValues.get( k ), 0 );

		// the elements before the k-th should be equal or smaller than the k-th
		for ( int p = i; p < k; ++p )
			assertTrue( values.get( p ) <= values.get( k ) );

		// the elements after the k-th should be equal or greater than the k-th
		for ( int p = k + 1; p <= j; ++p )
			assertTrue( values.get( p ) >= values.get( k ) );

		// all elements should be contained in list, i.e., after sorting lists should be identical
		Collections.sort( values.subList( i, j+1 ) );
		for ( int p = i; p <= j; ++p )
			assertTrue( values.get( p ).equals( sortedValues.get( p ) ) );
	}

	@Test
	public void testMedianFloatObjectFullIteratorComparator()
	{
		ArrayList< Float > values = new ArrayList< Float >();
		for( float f : new float[] {123, 21, 12912, 321, 32, 12345, 249, 5823, 834, 10, 23, 329 } ) {
			values.add( f );
		}
		@SuppressWarnings( "unchecked" )
		ArrayList< Float > sortedValues = ( ArrayList< Float > ) values.clone();

		final int i = 0;
		final int j = values.size() - 1;
		ListIterator< Float > iIterator = values.listIterator( i );
		ListIterator< Float > jIterator = values.listIterator( j + 1 );
		int k = i + ( j - i ) / 2;
		KthElement.kthElement( iIterator, jIterator, k, new ComparableComparator< Float >() );
		Collections.sort( sortedValues );

		// the elements at the k-th positions should be equal 
		assertEquals( values.get( k ), sortedValues.get( k ), 0 );

		// the elements before the k-th should be equal or smaller than the k-th
		for ( int p = i; p < k; ++p )
			assertTrue( values.get( p ) <= values.get( k ) );

		// the elements after the k-th should be equal or greater than the k-th
		for ( int p = k + 1; p <= j; ++p )
			assertTrue( values.get( p ) >= values.get( k ) );

		// all elements should be contained in list, i.e., after sorting lists should be identical
		Collections.sort( values );
		for ( int p = i; p <= j; ++p )
			assertTrue( values.get( p ).equals( sortedValues.get( p ) ) );
	}

	@Test
	public void testMedianFloatObjectFullPermutation()
	{
		ArrayList< Float > values = new ArrayList< Float >();
		for( float f : new float[] {123, 21, 12912, 321, 32, 12345, 249, 5823, 834, 10, 23, 329 } ) {
			values.add( f );
		}
		@SuppressWarnings( "unchecked" )
		ArrayList< Float > origvalues = ( ArrayList< Float > ) values.clone();

		final int[] permutation = new int[ values.size() ];
		for( int k = 0; k < permutation.length; ++k )
			permutation[ k ] = k;

		final int i = 0;
		final int j = values.size() - 1;
		int k = i + ( j - i ) / 2;
		KthElement.kthElement( k, values, permutation );

		// the elements before the k-th should be equal or smaller than the k-th
		for ( int p = i; p < k; ++p )
			assertTrue( values.get( p ) <= values.get( k ) );

		// the elements after the k-th should be equal or greater than the k-th
		for ( int p = k + 1; p <= j; ++p )
			assertTrue( values.get( p ) >= values.get( k ) );

		for( int p = 0; p < permutation.length; ++p )
			assertTrue( values.get( p ).equals( origvalues.get( permutation[ p ] ) ) );
	}
	
	@Test
	public void testMedianFloatObjectFullComparatorPermutation()
	{
		ArrayList< Float > values = new ArrayList< Float >();
		for( float f : new float[] {123, 21, 12912, 321, 32, 12345, 249, 5823, 834, 10, 23, 329 } ) {
			values.add( f );
		}
		@SuppressWarnings( "unchecked" )
		ArrayList< Float > origvalues = ( ArrayList< Float > ) values.clone();

		final int[] permutation = new int[ values.size() ];
		for( int k = 0; k < permutation.length; ++k )
			permutation[ k ] = k;

		final int i = 0;
		final int j = values.size() - 1;
		int k = i + ( j - i ) / 2;
		KthElement.kthElement( k, values, permutation, new ComparableComparator< Float >() );

		// the elements before the k-th should be equal or smaller than the k-th
		for ( int p = i; p < k; ++p )
			assertTrue( values.get( p ) <= values.get( k ) );

		// the elements after the k-th should be equal or greater than the k-th
		for ( int p = k + 1; p <= j; ++p )
			assertTrue( values.get( p ) >= values.get( k ) );

		for( int p = 0; p < permutation.length; ++p )
			assertTrue( values.get( p ).equals( origvalues.get( permutation[ p ] ) ) );
	}

	@Test
	public void testMedianFloatObjectFullIteratorComparatorPermutation()
	{
		ArrayList< Float > values = new ArrayList< Float >();
		for( float f : new float[] {123, 21, 12912, 321, 32, 12345, 249, 5823, 834, 10, 23, 329 } ) {
			values.add( f );
		}
		@SuppressWarnings( "unchecked" )
		ArrayList< Float > origvalues = ( ArrayList< Float > ) values.clone();

		final int[] permutation = new int[ values.size() ];
		for( int k = 0; k < permutation.length; ++k )
			permutation[ k ] = k;

		final int i = 0;
		final int j = values.size() - 1;
		ListIterator< Float > iIterator = values.listIterator( i );
		ListIterator< Float > jIterator = values.listIterator( j + 1 );
		int k = i + ( j - i ) / 2;
		KthElement.kthElement( iIterator, jIterator, k, permutation, new ComparableComparator< Float >() );

		// the elements before the k-th should be equal or smaller than the k-th
		for ( int p = i; p < k; ++p )
			assertTrue( values.get( p ) <= values.get( k ) );

		// the elements after the k-th should be equal or greater than the k-th
		for ( int p = k + 1; p <= j; ++p )
			assertTrue( values.get( p ) >= values.get( k ) );

		for( int p = 0; p < permutation.length; ++p )
			assertTrue( values.get( p ).equals( origvalues.get( permutation[ p ] ) ) );
	}
	
	@Test
	public void testMedianFloatObjectFullIteratorPermutation()
	{
		ArrayList< Float > values = new ArrayList< Float >();
		for( float f : new float[] {123, 21, 12912, 321, 32, 12345, 249, 5823, 834, 10, 23, 329 } ) {
			values.add( f );
		}
		@SuppressWarnings( "unchecked" )
		ArrayList< Float > origvalues = ( ArrayList< Float > ) values.clone();

		final int[] permutation = new int[ values.size() ];
		for( int k = 0; k < permutation.length; ++k )
			permutation[ k ] = k;

		final int i = 0;
		final int j = values.size() - 1;
		ListIterator< Float > iIterator = values.listIterator( i );
		ListIterator< Float > jIterator = values.listIterator( j + 1 );
		int k = i + ( j - i ) / 2;
		KthElement.kthElement( iIterator, jIterator, k, permutation );

		// the elements before the k-th should be equal or smaller than the k-th
		for ( int p = i; p < k; ++p )
			assertTrue( values.get( p ) <= values.get( k ) );

		// the elements after the k-th should be equal or greater than the k-th
		for ( int p = k + 1; p <= j; ++p )
			assertTrue( values.get( p ) >= values.get( k ) );

		for( int p = 0; p < permutation.length; ++p )
			assertTrue( values.get( p ).equals( origvalues.get( permutation[ p ] ) ) );
	}
}
