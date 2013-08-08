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

package net.imglib2.img.imageplus;

import ij.ImagePlus;
import net.imglib2.exception.ImgLibException;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.type.NativeType;

/**
 * A container that stores data in an array of 2d-slices each as a
 * linear array of basic types.  For types that are supported by ImageJ (byte,
 * short, int, float), an actual ImagePlus is created or used to store the
 * data.  Alternatively, an {@link ImagePlusImg} can be created using
 * an already existing {@link ImagePlus} instance.
 *
 * {@link ImagePlusImg ImagePlusContainers} provides a legacy layer to
 * apply imglib-based algorithm implementations directly on the data stored in
 * an ImageJ {@link ImagePlus}.  For all types that are supported by ImageJ, the
 * {@link ImagePlusImg} provides access to the pixels of an
 * {@link ImagePlus} instance that can be accessed via {@link getImagePlus}().
 *
 *   Johannes Schindelin
 *
 * @author Funke
 * @author Preibisch
 * @author Rueden
 * @author Saalfeld
 * @author Schindelin
 * @author Jan Funke
 * @author Tobias Pietzsch
 * @author Stephan Preibisch
 * @author Curtis Rueden
 * @author Stephan Saalfeld
 */
public class ImagePlusImg< T extends NativeType< T >, A extends ArrayDataAccess<A> > extends PlanarImg< T, A >
{
	final protected int width, height, depth, frames, channels;

	protected ImagePlusImg(
			final int width,
			final int height,
			final int depth,
			final int frames,
			final int channels,
			final int entitiesPerPixel )
	{
		super( reduceDimensions( new long[] { width, height, channels, depth, frames } ), entitiesPerPixel );

		this.width = width;
		this.height = height;
		this.depth = depth;
		this.frames = frames;
		this.channels = channels;
	}

	/**
	 * Standard constructor as called by default factories.
	 *
	 * <em>Note that this constructor does not know about the meaning of
	 * dimensions > 1, and will use them in the {@link ImagePlus} default order
	 * x,y,c,z,t.  That is, from two dimensions, it will create an x,y image,
	 * from three dimensions, an x,y,c image, and from four dimensions, an
	 * x,y,c,z image.</em>
	 *
	 * @param factory
	 * @param dim
	 * @param entitiesPerPixel
	 */
	ImagePlusImg( final long[] dim, final int entitiesPerPixel )
	{
		super( dim, entitiesPerPixel );

		assert dim.length < 6 : "ImagePlusContainer can only handle up to 5 dimensions.";

		if ( dim.length > 0 )
			width = ( int ) dim[ 0 ];
		else
			width = 1;

		if ( dim.length > 1 )
			height = ( int ) dim[ 1 ];
		else
			height = 1;

		if ( dim.length > 2 )
			channels = ( int ) dim[ 2 ];
		else
			channels = 1;

		if ( dim.length > 3 )
			depth = ( int ) dim[ 3 ];
		else
			depth = 1;

		if ( dim.length > 4 )
			frames = ( int ) dim[ 4 ];
		else
			frames = 1;
	}

	ImagePlusImg( final A creator, final long[] dim, final int entitiesPerPixel )
	{
		this( dim, entitiesPerPixel );

		mirror.clear();

		for ( int i = 0; i < numSlices; ++i )
			mirror.add( creator.createArray( width * height * entitiesPerPixel ) );
	}

	public ImagePlus getImagePlus() throws ImgLibException
	{
		throw new ImgLibException( this, "has no ImagePlus instance, it is not a standard type of ImagePlus" );
	}

	/*
	protected static long[] expandDimensions( final long[] dimensions )
	{
		if(dimensions.length >= 5)
			return dimensions;

		final long[] dim = new long[ 5 ];
		for ( int d = 0; d < 5; ++d )
			dim[ d ] = ( dimensions.length >= d ) ? dimensions[ d ] : 1;
		return dim;
	}
	*/

	/**
	 * Compute the minimal required number of dimensions for a given
	 * {@link ImagePlus}, whereas width and height are always first.
	 *
	 * E.g. a gray-scale 2d time series would have three dimensions
	 * [width,height,frames], a gray-scale 3d stack [width,height,depth] and a
	 * 2d composite image [width,height,channels] as well.  A composite 3d
	 * stack has four dimensions [width,height,channels,depth], as a time
	 * series five [width,height,channels,depth,frames].
	 */
	protected static long[] reduceDimensions( final ImagePlus imp )
	{
		final int[] dimensions = imp.getDimensions();
		final long[] impDimensions = new long[ dimensions.length ];
		for ( int d = 0; d < dimensions.length; ++d )
			impDimensions[ d ] = dimensions[ d ];
		return reduceDimensions( impDimensions );
	}

	protected static long[] reduceDimensions( final long[] impDimensions )
	{
		/* ImagePlus is at least 2d, x,y are mapped to an index on a stack slice */
		int n = 2;
		for ( int d = 2; d < impDimensions.length; ++d )
			if ( impDimensions[ d ] > 1 ) ++n;

		final long[] dim = new long[ n ];
		dim[ 0 ] = impDimensions[ 0 ];
		dim[ 1 ] = impDimensions[ 1 ];

		n = 1;

		/* channels */
		if ( impDimensions[ 2 ] > 1 )
			dim[ ++n ] = impDimensions[ 2 ];

		/* depth */
		if ( impDimensions[ 3 ] > 1 )
			dim[ ++n ] = impDimensions[ 3 ];

		/* frames */
		if ( impDimensions[ 4 ] > 1 )
			dim[ ++n ] = impDimensions[ 4 ];

		return dim;
	}

	public int getWidth() { return width; }

	public int getHeight() { return height; }

	public int getChannels() { return channels; }

	public int getDepth() { return depth; }

	public int getFrames() { return frames; }

	@Override
	public ImagePlusImgFactory< T > factory()
	{
		return new ImagePlusImgFactory< T >();
	}

	/**
	 * Free resources.
	 * The container can no longer be used after calling close().
	 *
	 * Subclasses override this to close the underlying {@link ImagePlus}.
	 */
	public void close()
	{}
}
