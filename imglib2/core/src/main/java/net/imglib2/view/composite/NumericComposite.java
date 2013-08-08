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
package net.imglib2.view.composite;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.list.ListImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

/**
 * A vector of {@link NumericType} scalars.  It is a {@link NumericType}
 * itself, implementing the {@link NumericType} algebra as element-wise
 * operations.
 *
 * @author Stephan Saalfeld <saalfeld@mpi-cbg.de>
 */
public class NumericComposite< T extends NumericType< T > > extends AbstractComposite< T > implements NumericType< NumericComposite< T > >
{
	final protected int length;
	
	static public class Factory< T extends NumericType< T > > implements CompositeFactory< T, NumericComposite< T > > 
	{
		final protected int numChannels;
		
		public Factory( final int numChannels )
		{
			this.numChannels = numChannels;
		}
		
		@Override
		public NumericComposite< T > create( final RandomAccess< T > sourceAccess )
		{
			return new NumericComposite< T >( sourceAccess, numChannels );
		}
	}
	
	public NumericComposite( final RandomAccess< T > sourceAccess, final int length )
	{
		super( sourceAccess );
		this.length = length;
	}

	/**
	 * Generates a 1D {@link ArrayImg}&lt;T&gt; 
	 */
	@Override
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public NumericComposite< T > createVariable()
	{
		final T t = sourceAccess.get();
		final Img< T > img;
		if ( NativeType.class.isInstance( t ) )
			img = ( ( NativeType )t ).createSuitableNativeImg( new ArrayImgFactory(), new long[]{ length } );
		else
			img = new ListImgFactory< T >().create( new long[]{ length }, t );
		return new NumericComposite< T >( img.randomAccess(), length );
	}

	@Override
	public NumericComposite< T > copy()
	{
		return new NumericComposite< T >( sourceAccess.copyRandomAccess(), length );
	}

	@Override
	public void set( final NumericComposite< T > c )
	{
		sourceAccess.setPosition( 0, d );
		c.sourceAccess.setPosition( 0, c.d );
		while ( sourceAccess.getLongPosition( d ) < length )
		{
			sourceAccess.get().set( c.sourceAccess.get() );
			sourceAccess.fwd( d );
			c.sourceAccess.fwd( c.d );
		}
	}

	@Override
	public void add( final NumericComposite< T > c )
	{
		sourceAccess.setPosition( 0, d );
		c.sourceAccess.setPosition( 0, d );
		while ( sourceAccess.getLongPosition( d ) < length )
		{
			sourceAccess.get().add( c.sourceAccess.get() );
			sourceAccess.fwd( d );
			c.sourceAccess.fwd( d );
		}
	}

	@Override
	public void sub( final NumericComposite< T > c )
	{
		sourceAccess.setPosition( 0, d );
		c.sourceAccess.setPosition( 0, d );
		while ( sourceAccess.getLongPosition( d ) < length )
		{
			sourceAccess.get().sub( c.sourceAccess.get() );
			sourceAccess.fwd( d );
			c.sourceAccess.fwd( d );
		}
	}

	@Override
	public void mul( final NumericComposite< T > c )
	{
		sourceAccess.setPosition( 0, d );
		c.sourceAccess.setPosition( 0, d );
		while ( sourceAccess.getLongPosition( d ) < length )
		{
			sourceAccess.get().mul( c.sourceAccess.get() );
			sourceAccess.fwd( d );
			c.sourceAccess.fwd( d );
		}
	}

	@Override
	public void div( final NumericComposite< T > c )
	{
		sourceAccess.setPosition( 0, d );
		c.sourceAccess.setPosition( 0, d );
		while ( sourceAccess.getLongPosition( d ) < length )
		{
			sourceAccess.get().div( c.sourceAccess.get() );
			sourceAccess.fwd( d );
			c.sourceAccess.fwd( d );
		}
	}

	@Override
	public void setZero()
	{
		sourceAccess.setPosition( 0, d );
		while ( sourceAccess.getLongPosition( d ) < length )
		{
			sourceAccess.get().setZero();
			sourceAccess.fwd( d );
		}
	}

	@Override
	public void setOne()
	{
		sourceAccess.setPosition( 0, d );
		while ( sourceAccess.getLongPosition( d ) < length )
		{
			sourceAccess.get().setOne();
			sourceAccess.fwd( d );
		}
	}

	@Override
	public void mul( final float c )
	{
		sourceAccess.setPosition( 0, d );
		while ( sourceAccess.getLongPosition( d ) < length )
		{
			sourceAccess.get().mul( c );
			sourceAccess.fwd( d );
		}
	}

	@Override
	public void mul( final double c )
	{
		sourceAccess.setPosition( 0, d );
		while ( sourceAccess.getLongPosition( d ) < length )
		{
			sourceAccess.get().mul( c );
			sourceAccess.fwd( d );
		}
	}
}
