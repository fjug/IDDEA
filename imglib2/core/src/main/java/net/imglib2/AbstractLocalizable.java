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
 * An abstract class that implements the {@link Localizable} interface using a
 * long[] array to maintain position.
 *
 * @author ImgLib2 developers
 * @author Stephan Preibisch
 * @author Stephan Saalfeld <saalfeld@mpi-cbg.de>
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public abstract class AbstractLocalizable extends AbstractEuclideanSpace implements Localizable
{
	/**
	 * The {@link Localizable} interface is implemented using the position
	 * stored here. {@link Positionable} subclasses, such as {@link Point},
	 * modify this array.
	 */
	protected final long[] position;

	/**
	 * @param n
	 *            number of dimensions.
	 */
	public AbstractLocalizable( final int n )
	{
		super( n );
		position = new long[ n ];
	}

	/**
	 * Protected constructor that re-uses the passed position array. This is
	 * intended to allow subclasses to provide a way to wrap a long[] array.
	 *
	 * @param position
	 *            position array to use.
	 */
	protected AbstractLocalizable( final long[] position )
	{
		super( position.length );
		this.position = position;
	}

	@Override
	public void localize( final float[] pos )
	{
		for ( int d = 0; d < n; ++d )
			pos[ d ] = position[ d ];
	}

	@Override
	public void localize( final double[] pos )
	{
		for ( int d = 0; d < n; ++d )
			pos[ d ] = position[ d ];
	}

	@Override
	public void localize( final int[] pos )
	{
		for ( int d = 0; d < n; ++d )
			pos[ d ] = ( int ) position[ d ];
	}

	@Override
	public void localize( final long[] pos )
	{
		for ( int d = 0; d < n; d++ )
			pos[ d ] = position[ d ];
	}

	@Override
	public float getFloatPosition( final int d )
	{
		return position[ d ];
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return position[ d ];
	}

	@Override
	public int getIntPosition( final int d )
	{
		return ( int ) position[ d ];
	}

	@Override
	public long getLongPosition( final int d )
	{
		return position[ d ];
	}
}
