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

package net.imglib2.ops.pointset;

import net.imglib2.AbstractCursor;

/**
 * PointSetIntersection is a {@link PointSet} that consists of the set of
 * points that are in the intersection set of two other PointSets. Thus
 * those points that are members of both input PointSets.
 * 
 * @author Barry DeZonia
 */
public class PointSetIntersection extends AbstractPointSet {
	
	// -- instance variables --
	
	private final PointSet a, b;
	private final BoundsCalculator calculator;
	private boolean needsCalc;
	
	// -- constructor --
	
	public PointSetIntersection(PointSet a, PointSet b) {
		if (a.numDimensions() != b.numDimensions())
			throw new IllegalArgumentException();
		this.a = a;
		this.b = b;
		calculator = new BoundsCalculator();
		needsCalc = true;
	}
	
	// -- PointSet methods --
	
	@Override
	public long[] getOrigin() {
		return a.getOrigin();
	}
	
	@Override
	public void translate(long[] deltas) {
		a.translate(deltas);
		b.translate(deltas);
		needsCalc = true;
		invalidateBounds();
	}
	
	@Override
	public PointSetIterator iterator() {
		return new PointSetIntersectionIterator();
	}
	
	@Override
	public int numDimensions() { return a.numDimensions(); }
	
	@Override
	public boolean includes(long[] point) {
		return a.includes(point) && b.includes(point);
	}
	
	@Override
	protected long[] findBoundMin() {
		if (needsCalc) {
			calculator.calc(this);
			needsCalc = false;
		}
		return calculator.getMin();
	}

	@Override
	protected long[] findBoundMax() {
		if (needsCalc) {
			calculator.calc(this);
			needsCalc = false;
		}
		return calculator.getMax();
	}

	@Override
	public long size() {
		long numElements = 0;
		PointSetIterator iter = iterator();
		while (iter.hasNext()) {
			iter.next();
			numElements++;
		}
		return numElements;
	}

	@Override
	public PointSetIntersection copy() {
		return new PointSetIntersection(a.copy(), b.copy());
	}
	
	// -- private helpers --
	
	private class PointSetIntersectionIterator extends AbstractCursor<long[]>
		implements PointSetIterator
	{
		private final PointSetIterator aIter;
		private long[] curr;
		private long[] nextCache;
		
		public PointSetIntersectionIterator() {
			super(a.numDimensions());
			aIter = a.iterator();
			reset();
		}
		
		@Override
		public boolean hasNext() {
			if (nextCache != null) return true;
			return positionToNext();
		}
		
		@Override
		public void reset() {
			aIter.reset();
			curr = null;
			nextCache = null;
		}
		
		@Override
		public long[] get() {
			return curr;
		}

		@Override
		public void fwd() {
			if ((nextCache != null) || (positionToNext())) {
				if (curr == null) curr = new long[n];
				for (int i = 0; i < n; i++)
					curr[i] = nextCache[i];
				nextCache = null;
				return;
			}
			throw new IllegalArgumentException("fwd() cannot go beyond end");
		}

		@Override
		public void localize(long[] position) {
			for (int i = 0; i < n; i++) {
				position[i] = curr[i];
			}
		}

		@Override
		public long getLongPosition(int d) {
			return curr[d];
		}

		@Override
		public AbstractCursor<long[]> copy() {
			return new PointSetIntersectionIterator();
		}

		@Override
		public AbstractCursor<long[]> copyCursor() {
			return copy();
		}

		private boolean positionToNext() {
			nextCache = null;
			while (aIter.hasNext()) {
				long[] pos = aIter.next();
				if (b.includes(pos)) {
					nextCache = pos;
					return true;
				}
			}
			return false;
		}

	}
}

