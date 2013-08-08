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

package net.imglib2.img.cell;

import net.imglib2.AbstractCursor;
import net.imglib2.Cursor;
import net.imglib2.type.NativeType;

/**
 * {@link Cursor} on a {@link CellImg}.
 *
 * @author ImgLib2 developers
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class CellCursor< T extends NativeType< T >, A, C extends AbstractCell< A > > extends AbstractCursor< T > implements AbstractCellImg.CellContainerSampler< T, A, C >
{
	protected final T type;

	protected final Cursor< C > cursorOnCells;

	protected int lastIndexInCell;

	/**
	 * The current index of the type. It is faster to duplicate this here than
	 * to access it through type.getIndex().
	 */
	protected int index;

	/**
	 * Caches cursorOnCells.hasNext().
	 */
	protected boolean isNotLastCell;

	protected CellCursor( final CellCursor< T, A, C > cursor )
	{
		super( cursor.numDimensions() );

		this.type = cursor.type.duplicateTypeOnSameNativeImg();
		this.cursorOnCells = cursor.cursorOnCells.copyCursor();
		isNotLastCell = cursor.isNotLastCell;
		lastIndexInCell = cursor.lastIndexInCell;
		index = cursor.index;

		type.updateContainer( this );
		type.updateIndex( index );
	}

	public CellCursor( final AbstractCellImg< T, A, C, ? > container )
	{
		super( container.numDimensions() );

		this.type = container.createLinkedType();
		this.cursorOnCells = container.cells.cursor();

		reset();
	}

	@Override
	public C getCell()
	{
		return cursorOnCells.get();
	}

	@Override
	public T get()
	{
		return type;
	}

	@Override
	public CellCursor< T, A, C > copy()
	{
		return new CellCursor< T, A, C >( this );
	}

	@Override
	public CellCursor< T, A, C > copyCursor()
	{
		return copy();
	}

	@Override
	public boolean hasNext()
	{
		return ( index < lastIndexInCell ) || isNotLastCell;
	}

	@Override
	public void jumpFwd( final long steps )
	{
		long newIndex = index + steps;
		while ( newIndex > lastIndexInCell )
		{
			newIndex -= lastIndexInCell + 1;
			cursorOnCells.fwd();
			isNotLastCell = cursorOnCells.hasNext();
			lastIndexInCell = ( int ) ( getCell().size() - 1 );
		}
		index = ( int ) newIndex;
		type.updateIndex( index );
		type.updateContainer( this );
	}

	@Override
	public void fwd()
	{
		if ( ++index > lastIndexInCell )
		{
			moveToNextCell();
			index = 0;
		}
		type.updateIndex( index );
	}

	@Override
	public void reset()
	{
		cursorOnCells.reset();
		moveToNextCell();
		type.updateIndex( index );
	}

	@Override
	public String toString()
	{
		return type.toString();
	}

	@Override
	public long getLongPosition( final int dim )
	{
		return getCell().indexToGlobalPosition( index, dim );
	}

	@Override
	public void localize( final long[] position )
	{
		getCell().indexToGlobalPosition( index, position );
	}

	/**
	 * Move cursor right before the first element of the next cell. Update type
	 * and index variables.
	 */
	private void moveToNextCell()
	{
		cursorOnCells.fwd();
		isNotLastCell = cursorOnCells.hasNext();
		lastIndexInCell = ( int ) ( getCell().size() - 1 );
		index = -1;
		type.updateContainer( this );
	}
}
