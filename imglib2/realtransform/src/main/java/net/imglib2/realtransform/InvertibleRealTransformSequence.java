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

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;

/**
 * An {@link InvertibleRealTransform} that is a sequence of
 * {@link InvertibleRealTransform InvertibleRealTransforms}.
 *
 * @author Stephan Saalfeld <saalfeld@mpi-cbg.de>
 */
public class InvertibleRealTransformSequence extends AbstractRealTransformSequence< InvertibleRealTransform > implements InvertibleRealTransform
{
	@Override
	public void applyInverse( final double[] source, final double[] target )
	{
		assert source.length >= nSource && target.length >= nTarget : "Input dimensions too small.";
		
		final int s = transforms.size() - 1;
		if ( s > -1 )
		{
			if ( s > 0 )
			{
				transforms.get( s ).applyInverse( b, target );
				for ( int i = s - 1; i > 0; --i )
				{
					transforms.get( i ).applyInverse( a, b );
					switchAB();
				}
				transforms.get( 0 ).applyInverse( source, b );
			}
			else
				transforms.get( 0 ).applyInverse( source, target );
		}
	}

	@Override
	public void applyInverse( final float[] source, final float[] target )
	{
		assert source.length >= nSource && target.length >= nTarget : "Input dimensions too small.";
		
		final int s = transforms.size() - 1;
		if ( s > -1 )
		{
			for ( int d = Math.min( target.length, a.length ) - 1; d >= 0; --d )
				b[ d ] = target[ d ];
			
			for ( int i = s; i > -1; --i )
			{
				transforms.get( i ).applyInverse( a, b );
				switchAB();
			}
			
			for ( int d = Math.min( source.length, a.length ) - 1; d >= 0; --d )
				source[ d ] = ( float )b[ d ];
		}
	}

	@Override
	public void applyInverse( final RealPositionable source, final RealLocalizable target )
	{
		assert source.numDimensions() >= nSource && target.numDimensions() >= nTarget : "Input dimensions too small.";
		
		final int s = transforms.size() - 1;
		if ( s > -1 )
		{
			if ( s > 0 )
			{
				transforms.get( s ).applyInverse( pb, target );
				for ( int i = s - 1; i > 0; --i )
				{
					transforms.get( i ).applyInverse( a, b );
					switchAB();
				}
				transforms.get( 0 ).applyInverse( source, pb );
			}
			else
				transforms.get( 0 ).applyInverse( source, target );
		}
	}

	@Override
	public InvertibleRealTransform inverse()
	{
		return new InverseRealTransform( this );
	}
	
	@Override
	public InvertibleRealTransformSequence copy()
	{
		final InvertibleRealTransformSequence copy = new InvertibleRealTransformSequence();
		for ( final InvertibleRealTransform t : transforms )
			copy.add( t.copy() );
		return copy;
	}
}