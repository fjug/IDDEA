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

package net.imglib2.histogram;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.histogram.BinMapper1d;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.Integer1dBinMapper;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.Test;


/**
 * @author Barry DeZonia
 */
public class Histogram1dTest {

	@Test
	public void test() {

		List<UnsignedByteType> data = getData1();

		BinMapper1d<UnsignedByteType> binMapper =
			new Integer1dBinMapper<UnsignedByteType>(0, 256, false);

		Histogram1d<UnsignedByteType> hist =
			new Histogram1d<UnsignedByteType>(data, binMapper);

		assertEquals(256, hist.getBinCount());
		assertEquals(11, hist.totalCount());
		assertEquals(1, hist.frequency(new UnsignedByteType(3)));
		assertEquals(3, hist.frequency(new UnsignedByteType(5)));
		assertEquals(1, hist.frequency(new UnsignedByteType(7)));
		assertEquals(3, hist.frequency(new UnsignedByteType(9)));
		assertEquals(3, hist.frequency(new UnsignedByteType(10)));
		assertEquals(0, hist.lowerTailCount());
		assertEquals(0, hist.upperTailCount());

		binMapper = new Integer1dBinMapper<UnsignedByteType>(4, 8, true);

		hist = new Histogram1d<UnsignedByteType>(data, binMapper);

		assertEquals(8, hist.getBinCount());
		assertEquals(11, hist.distributionCount());
		assertEquals(1, hist.frequency(new UnsignedByteType(3)));
		assertEquals(3, hist.frequency(new UnsignedByteType(5)));
		assertEquals(1, hist.frequency(new UnsignedByteType(7)));
		assertEquals(3, hist.frequency(new UnsignedByteType(9)));
		assertEquals(3, hist.frequency(new UnsignedByteType(10)));
		assertEquals(1, hist.lowerTailCount());
		assertEquals(3, hist.upperTailCount());

		binMapper = new Integer1dBinMapper<UnsignedByteType>(5, 5, false);

		hist = new Histogram1d<UnsignedByteType>(data, binMapper);

		assertEquals(5, hist.getBinCount());
		assertEquals(7, hist.distributionCount());
		assertEquals(0, hist.frequency(new UnsignedByteType(3)));
		assertEquals(3, hist.frequency(new UnsignedByteType(5)));
		assertEquals(1, hist.frequency(new UnsignedByteType(7)));
		assertEquals(3, hist.frequency(new UnsignedByteType(9)));
		assertEquals(0, hist.frequency(new UnsignedByteType(10)));
		assertEquals(0, hist.lowerTailCount());
		assertEquals(0, hist.upperTailCount());
	}

	private List<UnsignedByteType> getData1() {
		List<UnsignedByteType> data = new ArrayList<UnsignedByteType>();
		data.add(new UnsignedByteType(5));
		data.add(new UnsignedByteType(3));
		data.add(new UnsignedByteType(5));
		data.add(new UnsignedByteType(9));
		data.add(new UnsignedByteType(10));
		data.add(new UnsignedByteType(7));
		data.add(new UnsignedByteType(10));
		data.add(new UnsignedByteType(10));
		data.add(new UnsignedByteType(9));
		data.add(new UnsignedByteType(9));
		data.add(new UnsignedByteType(5));
		return data;
	}
}
