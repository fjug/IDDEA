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

package net.imglib2.type.label;

import net.imglib2.img.NativeImg;
import net.imglib2.img.NativeImgFactory;
import net.imglib2.img.basictypeaccess.CharAccess;
import net.imglib2.img.basictypeaccess.array.CharArray;
import net.imglib2.type.AbstractNativeType;
import net.imglib2.type.BasePairType;
import net.imglib2.type.label.BasePairBitType.Base;

/**
 * TODO
 *
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 */
public class BasePairCharType extends AbstractNativeType<BasePairCharType> implements BasePairType<BasePairCharType>
{
	@Override
	public int getEntitiesPerPixel() { return 1; } 

	final protected NativeImg<BasePairCharType, ? extends CharAccess> img;
	
	// the DataAccess that holds the information 
	protected CharAccess dataAccess;
	
	// this is the constructor if you want it to read from an array
	public BasePairCharType( NativeImg<BasePairCharType, ? extends CharAccess> charStorage )
	{
		img = charStorage;
	}

	// this is the constructor if you want it to be a variable
	public BasePairCharType( final Base value )
	{
		img = null;
		dataAccess = new CharArray( 1 );
		set( value );
	}

	// this is the constructor if you want it to be a variable
	public BasePairCharType( final char value )
	{	
		img = null;
		dataAccess = new CharArray( 1 );
		setChar( value );
	}

	// this is the constructor if you want it to be a variable
	public BasePairCharType() { this( Base.N ); }

	@Override
	public NativeImg<BasePairCharType, ? extends CharAccess> createSuitableNativeImg( final NativeImgFactory<BasePairCharType> storageFactory, final long dim[] )
	{
		// create the container
		final NativeImg<BasePairCharType, ? extends CharAccess> container = storageFactory.createCharInstance( dim, 1 );
		
		// create a Type that is linked to the container
		final BasePairCharType linkedType = new BasePairCharType( container );
		
		// pass it to the NativeContainer
		container.setLinkedType( linkedType );
		
		return container;
	}
	
	@Override
	public void updateContainer( final Object c ) { dataAccess = img.update( c ); }
	
	@Override
	public BasePairCharType duplicateTypeOnSameNativeImg() { return new BasePairCharType( img ); }
	
	public char getChar() { return dataAccess.getValue( i ); }
	public void setChar( final char f ) { dataAccess.setValue( i, f ); }

	@Override
	public void set( final Base base ) 
	{
		switch ( base )
		{
			case A: setChar('A'); return;
			case T: setChar('T'); return;
			case G: setChar('G'); return;
			case C: setChar('C'); return;
			case gap: setChar(' '); return;
			default: setChar('N'); return;
		}
	}
	
	@Override
	public Base get() 
	{
		final char value = getChar();
		
		switch ( value )
		{
			case 'A': return Base.A;
			case 'T': return Base.T;
			case 'G': return Base.G;
			case 'C': return Base.C;
			case ' ': return Base.gap;
			default: return Base.N;
		}
	}
	
	@Override
	public void set( final BasePairCharType c ) { dataAccess.setValue( i, c.getChar() ); }

	@Override
	public int compareTo( final BasePairCharType c ) 
	{ 
		final char input = getChar();
		final char compare = c.getChar();
		
		if ( input == compare )
		{
			return 0;
		}
		switch ( input )
		{
			case ' ': return -1; 
			case 'N': return compare == ' ' ? 1 : -1;
			case 'A': return compare == ' ' || compare == 'N' ? 1 : -1;
			case 'T': return compare == 'G' || compare == 'C' ? -1 : 1;
			case 'G': return compare == 'C' ? -1 : 1;
			default: return 1;
		}
	}

	@Override
	public void complement() 
	{
		final char base = getChar();
		switch ( base )
		{
			case 'A': setChar( 'T' ); break;
			case 'T': setChar( 'A' ); break;
			case 'G': setChar( 'C' ); break;
			case 'C': setChar( 'G' ); break;
		}
	}

	@Override
	public byte baseToValue()
	{
		final char base = getChar();
		
		switch ( base )
		{
			case 'N': return 1;
			case 'A': return 2;
			case 'T': return 3;
			case 'G': return 4;
			case 'C': return 5;
			default: return 0;
		}		
	}

	@Override
	public BasePairCharType createVariable(){ return new BasePairCharType( Base.N ); }
	
	@Override
	public BasePairCharType copy(){ return new BasePairCharType( get() ); }
	
	@Override
	public String toString() { return "" + get(); }
}
