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

package net.imglib2.algorithm.gauss3;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

/**
 * Gaussian convolution.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public final class Gauss3
{
	/**
	 * Apply Gaussian convolution to source and write the result to output.
	 * In-place operation (source==target) is supported.
	 *
	 * <p>
	 * If the target type T is {@link DoubleType}, all calculations are done in
	 * double precision. For all other target {@link RealType RealTypes} float
	 * precision is used. General {@link NumericType NumericTypes} are computed
	 * in their own precision. The source type S and target type T are either
	 * both {@link RealType RealTypes} or both the same type.
	 *
	 * @param sigma
	 *            standard deviation of isotropic Gaussian.
	 * @param source
	 *            source image, must be sufficiently padded (e.g.
	 *            {@link Views#extendMirrorSingle(RandomAccessibleInterval)}) to
	 *            provide values for the target interval plus a border of half
	 *            the kernel size.
	 * @param target
	 *            target image
	 * @param <S>
	 *            source type
	 * @param <T>
	 *            target type
	 * @throws IncompatibleTypeException
	 *             if source and target type are not compatible (they must be
	 *             either both {@link RealType RealTypes} or the same type).
	 */
	public static < S extends NumericType< S >, T extends NumericType< T > > void gauss( final double sigma, final RandomAccessible< S > source, final RandomAccessibleInterval< T > target ) throws IncompatibleTypeException
	{
		final int n = source.numDimensions();
		final double[] s = new double[ n ];
		for ( int d = 0; d < n; ++d )
			s[ d ] = sigma;
		gauss( s, source, target );
	}

	/**
	 * Apply Gaussian convolution to source and write the result to output.
	 * In-place operation (source==target) is supported.
	 *
	 * <p>
	 * If the target type T is {@link DoubleType}, all calculations are done in
	 * double precision. For all other target {@link RealType RealTypes} float
	 * precision is used. General {@link NumericType NumericTypes} are computed
	 * in their own precision. The source type S and target type T are either
	 * both {@link RealType RealTypes} or both the same type.
	 *
	 * @param sigma
	 *            standard deviation in every dimension.
	 * @param source
	 *            source image, must be sufficiently padded (e.g.
	 *            {@link Views#extendMirrorSingle(RandomAccessibleInterval)}) to
	 *            provide values for the target interval plus a border of half
	 *            the kernel size.
	 * @param target
	 *            target image
	 * @param <S>
	 *            source type
	 * @param <T>
	 *            target type
	 * @throws IncompatibleTypeException
	 *             if source and target type are not compatible (they must be
	 *             either both {@link RealType RealTypes} or the same type).
	 */
	public static < S extends NumericType< S >, T extends NumericType< T > > void gauss( final double[] sigma, final RandomAccessible< S > source, final RandomAccessibleInterval< T > target ) throws IncompatibleTypeException
	{
		final double[][] halfkernels = halfkernels( sigma );
		final int numthreads = Runtime.getRuntime().availableProcessors();
		SeparableSymmetricConvolution.convolve( halfkernels, source, target, numthreads );
	}

	public static double[][] halfkernels( final double[] sigma )
	{
		final int n = sigma.length;
		final double[][] halfkernels = new double[ n ][];
		for( int i = 0; i < n; ++i )
		{
            final int size = Math.max( 2, (int) (3 * sigma[ i ] + 0.5) + 1 );
            halfkernels[ i ] = halfkernel( sigma[ i ], size, true );
		}
		return halfkernels;
	}

	public static double[] halfkernel( final double sigma, final int size, final boolean normalize )
	{
		final double two_sq_sigma = 2 * sigma * sigma;
		final double[] kernel = new double[ size ];

		kernel[ 0 ] = 1;
		for ( int x = 1; x < size; ++x )
			kernel[ x ] = Math.exp( -( x * x ) / two_sq_sigma );

		if ( normalize )
		{
			double sum = 0.5;
			for ( int x = 1; x < size; ++x )
				sum += kernel[ x ];
			sum *= 2;

			for ( int x = 0; x < size; ++x )
				kernel[ x ] /= sum;
		}

		return kernel;
	}
}
