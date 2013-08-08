/**
 * Copyright (c) 2009--2013, ImgLib2 developers
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.  Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials
 * provided with the distribution.  Neither the name of the imglib project nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.imglib2.realtransform;

import net.imglib2.concatenate.Concatenable;
import net.imglib2.concatenate.PreConcatenable;


/**
 * 
 *
 * @author Stephan Saalfeld <saalfeld@mpi-cbg.de>
 */
public class Translation extends AbstractTranslation implements Concatenable< TranslationGet >, PreConcatenable< TranslationGet >
{
	final protected Translation inverse;
	
	protected Translation( final double[] t, final Translation inverse )
	{
		super( t, inverse.ds );
		this.inverse = inverse;
	}
	
	public Translation( final int n )
	{
		super( n );
		this.inverse = new Translation( new double[ n ], this );
	}
	
	public Translation( final double... t )
	{
		this( t.length );
		set( t );
	}
	
	/**
	 * Set the translation vector.
	 * 
	 * @param t t.length <= the number of dimensions of this {@link Translation}
	 */
	@Override
	public void set( final double... t )
	{
		for ( int d = 0; d < t.length; ++d )
		{
			this.t[ d ] = t[ d ];
			inverse.t[ d ] = -t[ d ];
		}
	}
	
	/**
	 * Set one value of the translation vector.
	 * 
	 * @param t t.length <= the number of dimensions of this {@link Translation}
	 */
	@Override
	public void set( final double t, final int d )
	{
		assert d >= 0 && d < numDimensions() : "Dimension index out of bounds.";
		
		this.t[ d ] = t;
		inverse.t[ d ] = -t;
	}
	
	@Override
	public Translation copy()
	{
		return new Translation( t );
	}

	@Override
	public Translation inverse()
	{
		return inverse;
	}
	
	@Override
	public Translation preConcatenate( final TranslationGet a )
	{
		for ( int d = 0; d < numDimensions(); ++d )
			set( t[ d ] + a.getTranslation( d ) );
		
		return this;
	}

	@Override
	public Class< TranslationGet > getPreConcatenableClass()
	{
		return TranslationGet.class;
	}

	@Override
	public Translation concatenate( final TranslationGet a )
	{
		for ( int d = 0; d < numDimensions(); ++d )
			set( t[ d ] + a.getTranslation( d ) );
		
		return this;
	}

	@Override
	public Class< TranslationGet > getConcatenableClass()
	{
		return TranslationGet.class;
	}
}
