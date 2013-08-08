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

package net.imglib2;

/**
 * An implementation of dimensionality that can wrap a long[] array. The same principle
 * for wrapping as in Point is used.
 * 
 * @author Stephan Preibisch
 */
public class FinalDimensions implements Dimensions
{
	final long[] dimensions;
	
	/**
	 * Protected constructor that can re-use the passed position array.
	 *
	 * @param dimensions - array used to store the position.
	 * @param copy - flag indicating whether position array should be duplicated.
	 */
	protected FinalDimensions( final long[] dimensions, final boolean copy )
	{
		if ( copy ) 
			this.dimensions = dimensions.clone();
		else
			this.dimensions = dimensions;
	}

	/**
	 * Creates a FinalDimensions object with size zero in all dimensions
	 * 
	 * @param n - number of dimensions
	 */
	public FinalDimensions( final int n ) { this.dimensions = new long[ n ]; }
	
	/**
	 * Create a FinalDimensions with a defined size
	 * 
	 * @param dimensions - the size
	 */
	public FinalDimensions( final long... dimensions ) { this( dimensions, true ); }

	/**
	 * Create a FinalDimensions with a defined size
	 * 
	 * @param dimensions - the size
	 */
	public FinalDimensions( final int... dimensions )
	{
		this.dimensions = new long[ dimensions.length ];
		
		for ( int d = 0; d < dimensions.length; ++d )
			this.dimensions[ d ] = dimensions[ d ];
	}

	@Override
	public int numDimensions() { return dimensions.length; }

	@Override
	public void dimensions( final long[] dims )
	{
		for ( int d = 0; d < dims.length; ++d )
			dims[ d ] = this.dimensions[ d ];
	}

	@Override
	public long dimension( final int d ) { return dimensions[ d ]; }
	
	/**
	 * Create a FinalDimensions object that stores its coordinates in the provided position
	 * array.
	 *
	 * @param dimensions -array to use for storing the position.
	 */
	public static FinalDimensions wrap( final long[] dimensions )
	{
		return new FinalDimensions( dimensions, false );
	}
}
