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

package net.imglib2.ops.operation.real.unary;

import net.imglib2.type.numeric.RealType;

/**
 * Sets the real component of an output real number to a scaling of the real
 * component of an input real number into a new range. The range parameters
 * are specified in the constructor.
 * 
 * @author Barry DeZonia
 */
public final class RealConvert<I extends RealType<I>, O extends RealType<O>>
	implements RealUnaryOperation<I,O>
{
	private double scale;
	private double inputMin;
	private double inputMax;
	private double outputMin;
	private double outputMax;

	/**
	 * Constructor.
	 * @param inputMin - the minimum value of the input range
	 * @param inputMax - the maximum value of the input range
	 * @param outputMin - the minimum value of the output range
	 * @param outputMax - the maximum value of the output range
	 */
	public RealConvert(double inputMin, double inputMax, double outputMin,
			double outputMax) {
		this.inputMin = inputMin;
		this.inputMax = inputMax;
		this.outputMin = outputMin;
		this.outputMax = outputMax;
		this.scale = (outputMax - outputMin) / (inputMax - inputMin);
	}

	@Override
	public O compute(I x, O output) {
		double value = (x.getRealDouble() - inputMin) * scale + outputMin;
		output.setReal(value);

		return output;
	}

	@Override
	public RealConvert<I,O> copy() {
		return new RealConvert<I,O>(inputMin, inputMax, outputMin, outputMax);
	}
}
