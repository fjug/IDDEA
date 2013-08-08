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

package net.imglib2.view;

import net.imglib2.AbstractLocalizable;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.transform.Transform;

/**
 * Wrap a {@code source} RandomAccess which is related to this by a generic
 * {@link Transform} {@code transformToSource}.
 *
 *
 * @author Tobias Pietzsch
 */
public final class TransformRandomAccess< T > extends AbstractLocalizable implements RandomAccess< T >
{
	/**
	 * source RandomAccess. note that this is the <em>target</em> of the
	 * transformToSource.
	 */
	private final RandomAccess< T > source;

	private final Transform transformToSource;

	private final long[] tmp;

	TransformRandomAccess( final RandomAccess< T > source, final Transform transformToSource )
	{
		super( transformToSource.numSourceDimensions() );
		this.source = source;
		this.transformToSource = transformToSource;
		this.tmp = new long[ transformToSource.numTargetDimensions() ];
	}

	protected TransformRandomAccess( final TransformRandomAccess< T > randomAccess )
	{
		super( randomAccess.numDimensions() );
		this.source = randomAccess.source.copyRandomAccess();
		this.transformToSource = randomAccess.transformToSource;
		this.tmp = new long[ randomAccess.tmp.length ];
	}

	@Override
	public void fwd( final int d )
	{
		assert d < n;
		position[ d ] += 1;
		transformToSource.apply( position, tmp );
		source.setPosition( tmp );
	}

	@Override
	public void bck( final int d )
	{
		assert d < n;
		position[ d ] -= 1;
		transformToSource.apply( position, tmp );
		source.setPosition( tmp );
	}

	@Override
	public void move( final int distance, final int d )
	{
		assert d < n;
		position[ d ] += distance;
		transformToSource.apply( position, tmp );
		source.setPosition( tmp );
	}

	@Override
	public void move( final long distance, final int d )
	{
		assert d < n;
		position[ d ] += distance;
		transformToSource.apply( position, tmp );
		source.setPosition( tmp );
	}

	@Override
	public void move( final Localizable localizable )
	{
		assert localizable.numDimensions() >= n;

		for ( int d = 0; d < n; ++d )
			position[ d ] += localizable.getLongPosition( d );
		transformToSource.apply( position, tmp );
		source.setPosition( tmp );
	}

	@Override
	public void move( final int[] distance )
	{
		assert distance.length >= n;

		for ( int d = 0; d < n; ++d )
			position[ d ] += distance[ d ];
		transformToSource.apply( position, tmp );
		source.setPosition( tmp );
	}

	@Override
	public void move( final long[] distance )
	{
		assert distance.length >= n;

		for ( int d = 0; d < n; ++d )
			position[ d ] += distance[ d ];
		transformToSource.apply( position, tmp );
		source.setPosition( tmp );
	}

	@Override
	public void setPosition( final Localizable localizable )
	{
		assert localizable.numDimensions() == n;

		localizable.localize( position );
		transformToSource.apply( position, tmp );
		source.setPosition( tmp );
	}

	@Override
	public void setPosition( final int[] pos )
	{
		assert pos.length >= n;

		for ( int d = 0; d < n; ++d )
			position[ d ] = pos[ d ];
		transformToSource.apply( position, tmp );
		source.setPosition( tmp );
	}

	@Override
	public void setPosition( final long[] pos )
	{
		assert pos.length >= n;

		for ( int d = 0; d < n; ++d )
			position[ d ] = pos[ d ];
		transformToSource.apply( position, tmp );
		source.setPosition( tmp );
	}

	@Override
	public void setPosition( final int pos, final int d )
	{
		assert d < n;
		position[ d ] = pos;
		transformToSource.apply( position, tmp );
		source.setPosition( tmp );
	}

	@Override
	public void setPosition( final long pos, final int d )
	{
		assert d < n;
		position[ d ] = pos;
		transformToSource.apply( position, tmp );
		source.setPosition( tmp );
	}

	@Override
	public T get()
	{
		return source.get();
	}

	@Override
	public TransformRandomAccess< T > copy()
	{
		return new TransformRandomAccess< T >( this );
	}

	@Override
	public TransformRandomAccess< T > copyRandomAccess()
	{
		return copy();
	}
}
