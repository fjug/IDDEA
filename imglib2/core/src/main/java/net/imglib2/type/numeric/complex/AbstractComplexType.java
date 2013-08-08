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

package net.imglib2.type.numeric.complex;

import net.imglib2.type.numeric.ComplexType;

/**
 * TODO
 *
 */
public abstract class AbstractComplexType<T extends AbstractComplexType<T>> implements ComplexType<T>
{
	@Override
	public void set( final T c )
	{
		setReal( c.getRealDouble() );
		setImaginary( c.getImaginaryDouble() );
	}

	@Override
	public void mul( final float c )
	{
		setReal( getRealFloat() * c );
		setImaginary( getImaginaryFloat() * c );
	}

	@Override
	public void mul( final double c )
	{
		setReal( getRealDouble() * c );
		setImaginary( getImaginaryDouble() * c );
	}

	@Override
	public void add( final T c )
	{
		setReal( getRealDouble() + c.getRealDouble() );
		setImaginary( getImaginaryDouble() + c.getImaginaryDouble() );
	}

	@Override
	public void div( final T c )
	{
		final double a1 = getRealDouble();
		final double b1 = getImaginaryDouble();
		final double c1 = c.getRealDouble();
		final double d1 = c.getImaginaryDouble();

		setReal( ( a1*c1 + b1*d1 ) / ( c1*c1 + d1*d1 ) );
		setImaginary( ( b1*c1 - a1*d1 ) / ( c1*c1 + d1*d1 ) );
	}

	@Override
	public void mul( final T t )
	{
		// a + bi
		final double a = getRealDouble();
		final double b = getImaginaryDouble();

		// c + di
		final double c = t.getRealDouble();
		final double d = t.getImaginaryDouble();

		setReal( a*c - b*d );
		setImaginary( a*d + b*c );
	}

	@Override
	public void sub( final T c )
	{
		setReal( getRealDouble() - c.getRealDouble() );
		setImaginary( getImaginaryDouble() - c.getImaginaryDouble() );
	}

	@Override
	public void complexConjugate(){ setImaginary( -getImaginaryDouble() ); }

	@Override
	public float getPowerFloat()
	{
		final double real = getRealDouble();
		final double imaginary = getImaginaryDouble();

		return (float)Math.sqrt( real * real + imaginary * imaginary );
	}

	@Override
	public double getPowerDouble()
	{
		final double real = getRealDouble();
		final double imaginary = getImaginaryDouble();

		return Math.sqrt( real * real + imaginary * imaginary );
	}

	@Override
	public float getPhaseFloat()
	{
		return (float)Math.atan2( getImaginaryDouble(), getRealDouble() );
	}

	@Override
	public double getPhaseDouble()
	{
		return Math.atan2( getImaginaryDouble(), getRealDouble() );
	}

	@Override
	public void setOne()
	{
		setReal( 1 );
		setImaginary( 0 );
	}

	@Override
	public void setZero()
	{
		setReal( 0 );
		setImaginary( 0 );
	}

	@Override
	public void setComplexNumber( final float r, final float i )
	{
		setReal( r );
		setImaginary( i );
	}

	@Override
	public void setComplexNumber( final double r, final double i )
	{
		setReal( r );
		setImaginary( i );
	}

	@Override
	public String toString(){ return "(" + getRealDouble() + ") + (" + getImaginaryDouble() + ")i"; }
}
