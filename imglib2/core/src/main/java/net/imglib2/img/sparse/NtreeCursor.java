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

package net.imglib2.img.sparse;

import net.imglib2.Cursor;
import net.imglib2.img.sparse.NtreeImg.PositionProvider;
import net.imglib2.iterator.LocalizingIntervalIterator;
import net.imglib2.type.NativeType;

/**
 * @author Tobias Pietzsch
 * 
 */
public final class NtreeCursor<T extends NativeType<T>> extends
		LocalizingIntervalIterator implements Cursor<T>, PositionProvider {
	private final NtreeImg<T, ?> img;

	private final T type;

	public NtreeCursor(final NtreeImg<T, ?> img) {
		super(img);

		this.img = img;
		this.type = img.createLinkedType();

		for (int d = 0; d < n; d++)
			position[d] = 0;

		position[0]--;
		type.updateContainer(this);
	}

	private NtreeCursor(final NtreeCursor<T> cursor) {
		super(cursor);

		this.img = cursor.img;
		this.type = img.createLinkedType();

		for (int d = 0; d < n; d++)
			position[d] = cursor.position[d];

		type.updateContainer(this);
	}

	@Override
	public T get() {
		return type;
	}

	@Override
	public T next() {
		fwd();
		return get();
	}

	@Override
	public void remove() {
	}

	@Override
	public NtreeCursor<T> copy() {
		return new NtreeCursor<T>(this);
	}

	@Override
	public NtreeCursor<T> copyCursor() {
		return copy();
	}

	@Override
	public long[] getPosition() {
		return position;
	}
}
