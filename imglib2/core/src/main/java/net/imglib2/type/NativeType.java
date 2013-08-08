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

package net.imglib2.type;

import net.imglib2.Cursor;
import net.imglib2.img.NativeImg;
import net.imglib2.img.NativeImgFactory;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.cell.CellCursor;
import net.imglib2.img.cell.CellImg;

/**
 * A {@link NativeType} is a {@link Type} that that provides access to data
 * stored in Java primitive arrays. To this end, implementations maintain a
 * reference to the current storage array and the index of an element in that
 * array.
 *
 * The {@link NativeType} is positioned on the correct storage array and index
 * by accessors ({@link Cursor Cursors} and {@link RandomAccess RandomAccesses}
 * ).
 *
 * <p>
 * The {@link NativeType} is the only class that is aware of the actual data
 * type, i.e., which Java primitive type is used to store the data. On the other
 * hand it does not know the storage layout, i.e., how n-dimensional pixel
 * coordinates map to indices in the current array. It also doesn't know whether
 * and how the data is split into multiple chunks. This is determined by the
 * container implementation (e.g., {@link ArrayImg}, {@link CellImg}, ...).
 * Separating the storage layout from access and operations on the {@link Type}
 * avoids re-implementation for each container type.
 * </p>
 *
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public interface NativeType< T extends NativeType< T >> extends Type< T >
{
	/**
	 * Get the number of entities in the storage array required to store one
	 * pixel value. A pixel value may be spread over several entities. For
	 * example, a complex number may require 2 entries of a float[] array to
	 * store one pixel).
	 *
	 * @return the number of storage type entities required to store one pixel
	 *         value.
	 */
	public int getEntitiesPerPixel();

	/**
	 * The {@link NativeType} creates the {@link NativeImg} used for storing
	 * image data; based on the given storage strategy and its size. It
	 * basically only decides here which BasicType it uses (float, int, byte,
	 * bit, ...) and how many entities per pixel it needs (e.g. 2 floats per
	 * pixel for a complex number). This enables the separation of containers
	 * and the basic types.
	 *
	 * @param storageFactory
	 *            which storage strategy is used
	 * @param dim
	 *            the dimensions
	 *
	 * @return the instantiated {@link NativeImg} where only the {@link Type}
	 *         knows the BasicType it contains.
	 */
	public NativeImg< T, ? > createSuitableNativeImg( final NativeImgFactory< T > storageFactory, final long[] dim );

	/**
	 * Creates a new {@link NativeType} which stores in the same physical array.
	 * This is only used internally.
	 *
	 * @return a new {@link NativeType} instance working on the same
	 *         {@link NativeImg}
	 */
	public T duplicateTypeOnSameNativeImg();

	/**
	 * This method is used by an accessor (e.g., a {@link Cursor}) to request an
	 * update of the current data array.
	 *
	 * <p>
	 * As an example consider a {@link CellCursor} moving on a {@link CellImg}.
	 * The cursor maintains a {@link NativeType} which provides access to the
	 * image data. When the cursor moves from one cell to the next, the
	 * underlying data array of the {@link NativeType} must be switched to the
	 * data array of the new cell.
	 *
	 * <p>
	 * To achieve this, the {@link CellCursor} calls {@link updateContainer()}
	 * with itself as the argument. {@link updateContainer()} in turn will call
	 * {@link update()} on it's container, passing along the reference to the
	 * cursor. In this example, the container would be a {@link CellImg}. While
	 * the {@link NativeType} does not know about the type of the cursor, the
	 * container does. {@link CellImg} knows that it is passed a
	 * {@link CellCursor} instance, which can be used to figure out the current
	 * cell and the underlying data array, which is then returned to the
	 * {@link NativeType}.
	 *
	 * <p>
	 * The idea behind this concept is maybe not obvious. The {@link NativeType}
	 * knows which basic type is used (float, int, byte, ...). However, it does
	 * not know how the data is stored ({@link ArrayImg}, {@link CellImg}, ...).
	 * This prevents the need for multiple implementations of {@link NativeType}.
	 *
	 * @param c
	 *            reference to an accessor which can be passed on to the
	 *            container (which will know what to do with it).
	 */
	public void updateContainer( Object c );

	/**
	 * Set the index into the current data array.
	 *
	 * <p>
	 * This is used by accessors (e.g., a {@link Cursor}) to position the
	 * {@link NativeType} in the container.
	 *
	 * @param i
	 *            the new array index
	 */
	public void updateIndex( final int i );

	/**
	 * Get the current index into the current data array.
	 *
	 * <p>
	 * This is used by accessors (e.g., a {@link Cursor}) to position the
	 * {@link NativeType} in the container.
	 *
	 * @return the current index into the underlying data array
	 */
	public int getIndex();

	/**
	 * Increment the index into the current data array.
	 *
	 * <p>
	 * This is used by accessors (e.g., a {@link Cursor}) to position the
	 * {@link NativeType} in the container.
	 */
	public void incIndex();

	/**
	 * Increases the index into the current data array by {@link increment}
	 * steps.
	 *
	 * <p>
	 * This is used by accessors (e.g., a {@link Cursor}) to position the
	 * {@link NativeType} in the container.
	 *
	 * @param increment
	 *            how many steps
	 */
	public void incIndex( final int increment );

	/**
	 * Decrement the index into the current data array.
	 *
	 * <p>
	 * This is used by accessors (e.g., a {@link Cursor}) to position the
	 * {@link NativeType} in the container.
	 */
	public void decIndex();

	/**
	 * Decrease the index into the current data array by {@link decrement}
	 * steps.
	 *
	 * <p>
	 * This is used by accessors (e.g., a {@link Cursor}) to position the
	 * {@link NativeType} in the container.
	 *
	 * @param decrement
	 *            how many steps
	 */
	public void decIndex( final int decrement );
}
