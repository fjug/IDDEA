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

package net.imglib2.ops.function.real;

import net.imglib2.ops.function.Function;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.type.numeric.RealType;

// TODO
//   A convolution is really a GeneralBinaryOperation between an input function
//   and a kernel function. For efficiency this class exists. For generality a
//   kernel function could calculate in evaluate(neigh,point,output) the relation
//   of neigh and point and choose the correct index into the kernel. By doing
//   this we could have more flexibility in the definitions of kernels (rather
//   than just an array of user supplied reals).

/**
 * Computes the convolution of the values of another function over a region with
 * a set of weights. The weights are specified in the constructor. The order of
 * the kernel is defined spatially from upper left to lower right. Being
 * convolution the kernel is applied in reverse order. API users should choose
 * carefully between convolution and correlation (see
 * {@link RealCorrelationFunction}).
 * 
 * @author Barry DeZonia
 */
public class RealConvolutionFunction<T extends RealType<T>>
	implements Function<PointSet,T>
{
	// -- instance variables --
	
	private final Function<long[],T> otherFunc;
	private final double[] kernel;
	private PointSet ps;
	private PointSetIterator iter;
	private T tmp;
	private double sum;
	
	// -- constructor --
	
	public RealConvolutionFunction(Function<long[],T> otherFunc, double[] kernel)
	{
		this.otherFunc = otherFunc;
		this.kernel = kernel;
		this.ps = null;
		this.iter = null;
		this.tmp = createOutput();
	}
	
	// -- Function methods --
	
	@Override
	public void compute(PointSet input, T output) {
		if (ps != input) {
			ps = input;
			iter = ps.iterator();
		}
		long[] pos;
		int i = kernel.length - 1;
		sum = 0;
		iter.reset();
		while (iter.hasNext()) {
			pos = iter.next();
			otherFunc.compute(pos, tmp);
			sum += tmp.getRealDouble() * kernel[i--];
		}
		output.setReal(sum);
	}

	@Override
	public T createOutput() {
		return otherFunc.createOutput();
	}

	@Override
	public RealConvolutionFunction<T> copy() {
		return new RealConvolutionFunction<T>(otherFunc.copy(), kernel.clone());
	}

}
