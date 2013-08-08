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

package net.imglib2.img;

import net.imglib2.img.basictypeaccess.BitAccess;
import net.imglib2.img.basictypeaccess.ByteAccess;
import net.imglib2.img.basictypeaccess.CharAccess;
import net.imglib2.img.basictypeaccess.DoubleAccess;
import net.imglib2.img.basictypeaccess.FloatAccess;
import net.imglib2.img.basictypeaccess.IntAccess;
import net.imglib2.img.basictypeaccess.LongAccess;
import net.imglib2.img.basictypeaccess.ShortAccess;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;

/**
 * TODO
 *
 */
public abstract class NativeImgFactory< T extends NativeType< T > > extends ImgFactory< T >
{
	/**
	 * This class will ask the {@link Type} to create a 
	 * suitable {@link Img} for the {@link Type} and the dimensionality.
	 * 
	 * {@link Type} will then call one of the abstract methods defined below to create the 
	 * {@link NativeImg}
	 * 
	 * @return {@link Img} - the instantiated Container
	 */
	@Override
	public NativeImg< T, ? > create( final long[] dim, final T type )
	{
		return type.createSuitableNativeImg( this, dim );
	}

	/* basic type containers */
	public abstract NativeImg< T, ? extends BitAccess > createBitInstance( long[] dimensions, int entitiesPerPixel );

	public abstract NativeImg< T, ? extends ByteAccess > createByteInstance( long[] dimensions, int entitiesPerPixel );

	public abstract NativeImg< T, ? extends CharAccess > createCharInstance( long[] dimensions, int entitiesPerPixel );

	public abstract NativeImg< T, ? extends ShortAccess > createShortInstance( long[] dimensions, int entitiesPerPixel );

	public abstract NativeImg< T, ? extends IntAccess > createIntInstance( long[] dimensions, int entitiesPerPixel );

	public abstract NativeImg< T, ? extends LongAccess > createLongInstance( long[] dimensions, int entitiesPerPixel );

	public abstract NativeImg< T, ? extends FloatAccess > createFloatInstance( long[] dimensions, int entitiesPerPixel );

	public abstract NativeImg< T, ? extends DoubleAccess > createDoubleInstance( long[] dimensions, int entitiesPerPixel );
}
