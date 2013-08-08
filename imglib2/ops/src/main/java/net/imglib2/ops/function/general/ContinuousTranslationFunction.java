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

package net.imglib2.ops.function.general;

import net.imglib2.ops.function.Function;

/**
 * Translates the coordinate values of one Function into the
 * coordinate space of another. Values can then be pulled out
 * of the alternate space. Continuous (double[]) version.
 * 
 * @author Barry DeZonia
 */
public class ContinuousTranslationFunction<T>
	implements Function<double[],T>
{
	// -- instance variables --
	
	private final Function<double[],T> otherFunc;
	private final double[] deltas;
	private final double[] localPoint;
	
	// -- constructor --
	
	public ContinuousTranslationFunction(
			Function<double[],T> otherFunc, double[] deltas)
	{
		this.otherFunc = otherFunc;
		this.deltas = deltas;
		this.localPoint = new double[deltas.length];
	}
	
	// -- Function methods --
	
	@Override
	public void compute(double[] point, T output) {
		for (int i = 0; i < localPoint.length; i++)
			localPoint[i] = point[i] + deltas[i];
		otherFunc.compute(localPoint, output);
	}

	@Override
	public T createOutput() {
		return otherFunc.createOutput();
	}
	
	@Override
	public ContinuousTranslationFunction<T> copy() {
		return new ContinuousTranslationFunction<T>(
				otherFunc.copy(), deltas.clone());
	}
}
