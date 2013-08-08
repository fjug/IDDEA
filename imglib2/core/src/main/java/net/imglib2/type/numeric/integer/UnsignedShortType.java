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

package net.imglib2.type.numeric.integer;

import net.imglib2.img.NativeImg;
import net.imglib2.img.NativeImgFactory;
import net.imglib2.img.basictypeaccess.ShortAccess;
import net.imglib2.util.Util;

/**
 * TODO
 *
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 */
public class UnsignedShortType extends GenericShortType<UnsignedShortType>
{
	// this is the constructor if you want it to read from an array
	public UnsignedShortType( final NativeImg<UnsignedShortType, ? extends ShortAccess> img ) { super( img ); }

	// this is the constructor if you want it to be a variable
	public UnsignedShortType( final int value ) { super( getCodedSignedShortChecked(value) ); }

	// this is the constructor if you want to specify the dataAccess
	public UnsignedShortType( final ShortAccess access ) { super( access ); }

	// this is the constructor if you want it to be a variable
	public UnsignedShortType() { this( 0 ); }

	public static short getCodedSignedShortChecked( int unsignedShort )
	{
		if ( unsignedShort < 0 )
			unsignedShort = 0;
		else if ( unsignedShort > 65535 )
			unsignedShort = 65535;

		return getCodedSignedShort( unsignedShort );
	}
	public static short getCodedSignedShort( final int unsignedShort ) { return (short)( unsignedShort & 0xffff );	}
	public static int getUnsignedShort( final short signedShort ) { return signedShort & 0xffff; }

	@Override
	public NativeImg<UnsignedShortType, ? extends ShortAccess> createSuitableNativeImg( final NativeImgFactory<UnsignedShortType> storageFactory, final long dim[] )
	{
		// create the container
		final NativeImg<UnsignedShortType, ? extends ShortAccess> container = storageFactory.createShortInstance( dim, 1 );

		// create a Type that is linked to the container
		final UnsignedShortType linkedType = new UnsignedShortType( container );

		// pass it to the NativeContainer
		container.setLinkedType( linkedType );

		return container;
	}

	@Override
	public UnsignedShortType duplicateTypeOnSameNativeImg() { return new UnsignedShortType( img ); }

	@Override
	public void mul( final float c )
	{
		set( Util.round( get() * c ) );
	}

	@Override
	public void mul( final double c )
	{
		set( ( int )Util.round( get() * c ) );
	}

	@Override
	public void add( final UnsignedShortType c )
	{
		set( get() + c.get() );
	}

	@Override
	public void div( final UnsignedShortType c )
	{
		set( get() / c.get() );
	}

	@Override
	public void mul( final UnsignedShortType c )
	{
		set( get() * c.get() );
	}

	@Override
	public void sub( final UnsignedShortType c )
	{
		set( get() - c.get() );
	}

	@Override
	public void inc()
	{
		set( get() + 1 );
	}

	@Override
	public void dec()
	{
		set( get() - 1 );
	}

	public int get() { return getUnsignedShort( getValue() ); }
	public void set( final int f ) { setValue( getCodedSignedShort( f ) ); }

	@Override
	public int getInteger(){ return get(); }
	@Override
	public long getIntegerLong() { return get(); }
	@Override
	public void setInteger( final int f ){ set( f ); }
	@Override
	public void setInteger( final long f ){ set( (int)f ); }

	@Override
	public double getMaxValue() { return -Short.MIN_VALUE + Short.MAX_VALUE; }
	@Override
	public double getMinValue()  { return 0; }

	@Override
	public int compareTo( final UnsignedShortType c )
	{
		final int a = get();
		final int b = c.get();

		if ( a > b )
			return 1;
		else if ( a < b )
			return -1;
		else
			return 0;
	}

	@Override
	public UnsignedShortType createVariable(){ return new UnsignedShortType( 0 ); }

	@Override
	public UnsignedShortType copy(){ return new UnsignedShortType( get() ); }

	@Override
	public String toString() { return "" + get(); }
}
