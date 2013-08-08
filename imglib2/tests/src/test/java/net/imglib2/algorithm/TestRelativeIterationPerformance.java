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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

package net.imglib2.algorithm;

import ij.ImageJ;

import java.io.File;

import net.imglib2.Cursor;
import net.imglib2.ExtendedRandomAccessibleInterval;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.io.ImgIOException;
import net.imglib2.io.ImgOpener;
import net.imglib2.outofbounds.OutOfBounds;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class TestRelativeIterationPerformance<T extends RealType<T>> implements Benchmark, OutputAlgorithm<Img<FloatType>> {

	private long processingTime;
	private final Img<T> input;
	private Img<FloatType> output;

	public IterationMethod method = IterationMethod.RANDOM_ACCESS;

	public static enum IterationMethod {
		RANDOM_ACCESS,
		RANDOM_ACCESS_SPLIT,
		RANDOM_ACCESS_NO_EXTEND,
		TRANSLATE_VIEW,
		TRANSLATE_VIEW_CURSOR,
		TRANSLATE_VIEW_SPLIT
	}

	/*
	 * CONSTRUCTOR
	 */

	public TestRelativeIterationPerformance(final Img<T> input) {
		this.input = input;
		try {
			output = input.factory().imgFactory(new FloatType()).create( input, new FloatType() );
		} catch (final IncompatibleTypeException e) {
			e.printStackTrace();
		}

	}

	/*
	 * METHODS
	 */

	@Override
	public boolean checkInput() {
		return true;
	}

	@Override
	public boolean process() {
		final long start = System.currentTimeMillis();

		switch (method) {
		case RANDOM_ACCESS:
			iterateWithRandoAccessible();
			break;
		case RANDOM_ACCESS_SPLIT:
			iterateWithRandomAccessibleSplit();
			break;
		case TRANSLATE_VIEW:
			iterateWithViews();
			break;
		case TRANSLATE_VIEW_CURSOR:
			iterateWithViewsCursor();
			break;
		case TRANSLATE_VIEW_SPLIT:
			iterateWithViewsSplit();
			break;
		case RANDOM_ACCESS_NO_EXTEND:
			iterateWithRandoAccessibleNoOutOfBounds();
			break;

		}

		processingTime = System.currentTimeMillis() - start;

		return true;
	}

	private void iterateWithRandoAccessibleNoOutOfBounds() {
		final int n = input.numDimensions();
		final long[] min = new long[ n ];
		final long[] max = new long[ n ];
		input.min( min );
		input.max( max );
		for ( int d = 0; d < n; ++d )
		{
			min[d] += 1;
			max[d] -= 1;
		}
		final Cursor<FloatType> oc = Views.iterable(Views.interval(output, min, max)).localizingCursor();
		final RandomAccess<T> ra = input.randomAccess();
		float I, In, Ine, Ie, Ise, Is, Isw, Iw, Inw;

		while(oc.hasNext()) {

			oc.fwd();

			ra.setPosition(oc);

			I = ra.get().getRealFloat();
			ra.bck(1);
			In = ra.get().getRealFloat();
			ra.fwd(0);
			Ine = ra.get().getRealFloat();
			ra.fwd(1);
			Ie = ra.get().getRealFloat();
			ra.fwd(1);
			Ise = ra.get().getRealFloat();
			ra.bck(0);
			Is = ra.get().getRealFloat();
			ra.bck(0);
			Isw = ra.get().getRealFloat();
			ra.bck(1);
			Iw = ra.get().getRealFloat();
			ra.bck(1);
			Inw = ra.get().getRealFloat();

			oc.get().set( I - 1/8f * (In+Ine+Ie+Ise+Is+Isw+Iw+Inw));
		}

	}

	private void iterateWithViews() {

		final ExtendedRandomAccessibleInterval<T, Img<T>> extended = Views.extendMirrorSingle(input);

		final Cursor<T> northCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {0, -1}), input) ).cursor();
		final Cursor<T> northEastCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {1, -1}), input) ).cursor();
		final Cursor<T> eastCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {1, 0}), input) ).cursor();
		final Cursor<T> southEastCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {1, 1}), input) ).cursor();
		final Cursor<T> southCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {0, 1}), input) ).cursor();
		final Cursor<T> southWestCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {-1, 1}), input) ).cursor();
		final Cursor<T> westCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {-1, 0}), input) ).cursor();
		final Cursor<T> northWestCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {-1, 1}), input) ).cursor();

		final Cursor<T> cursor = input.localizingCursor();
		final RandomAccess<FloatType> oc = output.randomAccess();

		float I, In, Ine, Ie, Ise, Is, Isw, Iw, Inw;
		while (cursor.hasNext()) {

			I 	= cursor.next().getRealFloat();
			In 	= northCursor.next().getRealFloat();
			Ine = northEastCursor.next().getRealFloat();
			Ie 	= eastCursor.next().getRealFloat();
			Ise = southEastCursor.next().getRealFloat();
			Is 	= southCursor.next().getRealFloat();
			Isw	= southWestCursor.next().getRealFloat();
			Iw 	= westCursor.next().getRealFloat();
			Inw	= northWestCursor.next().getRealFloat();

			oc.setPosition(cursor);
			oc.get().set( I - 1/8f * (In+Ine+Ie+Ise+Is+Isw+Iw+Inw));
		}

	}

	private void iterateWithViewsCursor() {

		final ExtendedRandomAccessibleInterval<T, Img<T>> extended = Views.extendMirrorSingle(input);

		final Cursor<T> northCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {0, -1}), input) ).cursor();
		final Cursor<T> northEastCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {1, -1}), input) ).cursor();
		final Cursor<T> eastCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {1, 0}), input) ).cursor();
		final Cursor<T> southEastCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {1, 1}), input) ).cursor();
		final Cursor<T> southCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {0, 1}), input) ).cursor();
		final Cursor<T> southWestCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {-1, 1}), input) ).cursor();
		final Cursor<T> westCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {-1, 0}), input) ).cursor();
		final Cursor<T> northWestCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {-1, 1}), input) ).cursor();

		final Cursor<T> cursor = input.cursor();
		final Cursor<FloatType> oc = output.cursor();

		float I, In, Ine, Ie, Ise, Is, Isw, Iw, Inw;
		while (cursor.hasNext()) {

			I 	= cursor.next().getRealFloat();
			In 	= northCursor.next().getRealFloat();
			Ine = northEastCursor.next().getRealFloat();
			Ie 	= eastCursor.next().getRealFloat();
			Ise = southEastCursor.next().getRealFloat();
			Is 	= southCursor.next().getRealFloat();
			Isw	= southWestCursor.next().getRealFloat();
			Iw 	= westCursor.next().getRealFloat();
			Inw	= northWestCursor.next().getRealFloat();

			oc.next().set( I - 1/8f * (In+Ine+Ie+Ise+Is+Isw+Iw+Inw));
		}

	}

	private void iterateWithViewsSplit()
	{
		final int n = input.numDimensions();
		final long[] min = new long[ n ];
		final long[] max = new long[ n ];

		// process central part
		min[ 0 ] = input.min( 0 ) + 1;
		max[ 0 ] = input.max( 0 ) - 1;
		min[ 1 ] = input.min( 1 ) + 1;
		max[ 1 ] = input.max( 1 ) - 1;
		iterateWithViewsPartial( new FinalInterval( min, max ) );

		// process first line
		min[ 0 ] = input.min( 0 );
		max[ 0 ] = input.max( 0 );
		min[ 1 ] = input.min( 1 );
		max[ 1 ] = input.min( 1 ) + 1;
		iterateWithViewsPartial( new FinalInterval( min, max ) );

		// process last line
		min[ 0 ] = input.min( 0 );
		max[ 0 ] = input.max( 0 );
		min[ 1 ] = input.max( 1 ) - 1;
		max[ 1 ] = input.max( 1 );
		iterateWithViewsPartial( new FinalInterval( min, max ) );

		// process first column
		min[ 0 ] = input.min( 0 );
		max[ 0 ] = input.min( 0 ) + 1;
		min[ 1 ] = input.min( 1 ) + 1;
		max[ 1 ] = input.max( 1 ) - 1;
		iterateWithViewsPartial( new FinalInterval( min, max ) );

		// process last column
		min[ 0 ] = input.max( 0 ) - 1;
		max[ 0 ] = input.max( 0 );
		min[ 1 ] = input.min( 1 ) + 1;
		max[ 1 ] = input.max( 1 ) - 1;
		iterateWithViewsPartial( new FinalInterval( min, max ) );
	}

	private void iterateWithViewsPartial( final Interval interval ) {

		final ExtendedRandomAccessibleInterval<T, Img<T>> extended = Views.extendMirrorSingle(input);

		final Cursor<T> northCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {0, -1}), interval) ).cursor();
		final Cursor<T> northEastCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {1, -1}), interval) ).cursor();
		final Cursor<T> eastCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {1, 0}), interval) ).cursor();
		final Cursor<T> southEastCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {1, 1}), interval) ).cursor();
		final Cursor<T> southCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {0, 1}), interval) ).cursor();
		final Cursor<T> southWestCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {-1, 1}), interval) ).cursor();
		final Cursor<T> westCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {-1, 0}), interval) ).cursor();
		final Cursor<T> northWestCursor = Views.iterable(Views.interval(Views.offset(extended, new long[] {-1, 1}), interval) ).cursor();

		final Cursor<T> cursor = Views.iterable(Views.interval( extended, interval )).cursor();
		final Cursor<FloatType> oc = Views.iterable(Views.interval( output, interval )).cursor();

		float I, In, Ine, Ie, Ise, Is, Isw, Iw, Inw;
		while (cursor.hasNext()) {

			I 	= cursor.next().getRealFloat();
			In 	= northCursor.next().getRealFloat();
			Ine = northEastCursor.next().getRealFloat();
			Ie 	= eastCursor.next().getRealFloat();
			Ise = southEastCursor.next().getRealFloat();
			Is 	= southCursor.next().getRealFloat();
			Isw	= southWestCursor.next().getRealFloat();
			Iw 	= westCursor.next().getRealFloat();
			Inw	= northWestCursor.next().getRealFloat();

			oc.next().set( I - 1/8f * (In+Ine+Ie+Ise+Is+Isw+Iw+Inw));
		}

	}

	private void iterateWithRandomAccessibleSplit()
	{
		final int n = input.numDimensions();
		final long[] min = new long[ n ];
		final long[] max = new long[ n ];

		// process central part
		min[ 0 ] = input.min( 0 ) + 1;
		max[ 0 ] = input.max( 0 ) - 1;
		min[ 1 ] = input.min( 1 ) + 1;
		max[ 1 ] = input.max( 1 ) - 1;
		iterateWithRandomAccessiblePartial( new FinalInterval( min, max ) );

		// process first line
		min[ 0 ] = input.min( 0 );
		max[ 0 ] = input.max( 0 );
		min[ 1 ] = input.min( 1 );
		max[ 1 ] = input.min( 1 ) + 1;
		iterateWithRandomAccessiblePartial( new FinalInterval( min, max ) );

		// process last line
		min[ 0 ] = input.min( 0 );
		max[ 0 ] = input.max( 0 );
		min[ 1 ] = input.max( 1 ) - 1;
		max[ 1 ] = input.max( 1 );
		iterateWithRandomAccessiblePartial( new FinalInterval( min, max ) );

		// process first column
		min[ 0 ] = input.min( 0 );
		max[ 0 ] = input.min( 0 ) + 1;
		min[ 1 ] = input.min( 1 ) + 1;
		max[ 1 ] = input.max( 1 ) - 1;
		iterateWithRandomAccessiblePartial( new FinalInterval( min, max ) );

		// process last column
		min[ 0 ] = input.max( 0 ) - 1;
		max[ 0 ] = input.max( 0 );
		min[ 1 ] = input.min( 1 ) + 1;
		max[ 1 ] = input.max( 1 ) - 1;
		iterateWithRandomAccessiblePartial( new FinalInterval( min, max ) );
	}

	private void iterateWithRandomAccessiblePartial( final Interval interval ) {
		final ExtendedRandomAccessibleInterval<T, Img<T>> extended = Views.extendMirrorSingle(input);
		final Cursor<FloatType> oc = Views.iterable(Views.interval(output, interval)).localizingCursor();

		final int n = input.numDimensions();
		final long[] min = new long[ n ];
		final long[] max = new long[ n ];
		interval.min( min );
		interval.max( max );
		for ( int d = 0; d < n; ++d )
		{
			min[d] -= 1;
			max[d] += 1;
		}
		final Interval raInterval = new FinalInterval( min, max );
		final RandomAccess<T> ra = extended.randomAccess( raInterval );
		float I, In, Ine, Ie, Ise, Is, Isw, Iw, Inw;

		while(oc.hasNext()) {

			oc.fwd();

			ra.setPosition(oc);

			I = ra.get().getRealFloat();
			ra.bck(1);
			In = ra.get().getRealFloat();
			ra.fwd(0);
			Ine = ra.get().getRealFloat();
			ra.fwd(1);
			Ie = ra.get().getRealFloat();
			ra.fwd(1);
			Ise = ra.get().getRealFloat();
			ra.bck(0);
			Is = ra.get().getRealFloat();
			ra.bck(0);
			Isw = ra.get().getRealFloat();
			ra.bck(1);
			Iw = ra.get().getRealFloat();
			ra.bck(1);
			Inw = ra.get().getRealFloat();

			oc.get().set( I - 1/8f * (In+Ine+Ie+Ise+Is+Isw+Iw+Inw));
		}

	}

	private void iterateWithRandoAccessible() {

		final OutOfBounds<T> ra = Views.extendMirrorSingle(input).randomAccess();
		final Cursor<T> cursor = input.localizingCursor();
		final RandomAccess<FloatType> oc = output.randomAccess();

		float I, In, Ine, Ie, Ise, Is, Isw, Iw, Inw;

		while (cursor.hasNext()) {

			cursor.fwd();
			oc.setPosition(cursor);
			ra.setPosition(cursor);

			I = cursor.get().getRealFloat();
			ra.bck(1);
			In = ra.get().getRealFloat();
			ra.fwd(0);
			Ine = ra.get().getRealFloat();
			ra.fwd(1);
			Ie = ra.get().getRealFloat();
			ra.fwd(1);
			Ise = ra.get().getRealFloat();
			ra.bck(0);
			Is = ra.get().getRealFloat();
			ra.bck(0);
			Isw = ra.get().getRealFloat();
			ra.bck(1);
			Iw = ra.get().getRealFloat();
			ra.bck(1);
			Inw = ra.get().getRealFloat();

			oc.get().set( I - 1/8f * (In+Ine+Ie+Ise+Is+Isw+Iw+Inw));

		}

	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public Img<FloatType> getResult() {
		return output;
	}

	@Override
	public long getProcessingTime() {
		return processingTime;
	}

	public static <T extends RealType<T> & NativeType< T >> void benchmark( final IterationMethod method, final String msg, final int niter, final Img< T > image )
	{
		// Init algo
		final TestRelativeIterationPerformance<T> algo = new TestRelativeIterationPerformance<T>(image);

		algo.method = method;

		System.out.println( msg );
		final long start = System.currentTimeMillis();
		for (int i = 0; i < niter; i++) {
			algo.process();
		}
		final long totalTime = System.currentTimeMillis() - start;
		ImageJFunctions.show(algo.getResult());
		System.out.println(String.format("Time taken: %.2f ms/iteration.", (float) totalTime / niter));
		final long width = image.dimension(0);
		final long height = image.dimension(1);
		System.out.println(String.format("or: %.2f µs/pixel.", 1000f * totalTime / ((float) niter * width * height)));
		System.out.println();
	}

	/*
	 * MAIN METHOD
	 */

	public static <T extends RealType<T> & NativeType< T >> void  main(final String[] args) throws ImgIOException, IncompatibleTypeException {

//		File file = new File( "E:/Users/JeanYves/Desktop/Data/Y.tif");
		final File file = new File( "/home/tobias/Desktop/Y.tif");
		final int niter = 1000;

		// Open file in imglib2
		final ImgFactory< ? > imgFactory = new ArrayImgFactory< T >();
		final Img< T > image = new ImgOpener().openImg( file.getAbsolutePath(), imgFactory );

		// Display it via ImgLib using ImageJ
		new ImageJ();
		ImageJFunctions.show( image );

		benchmark( IterationMethod.TRANSLATE_VIEW, "With translated views:", niter, image );
		benchmark( IterationMethod.TRANSLATE_VIEW_CURSOR, "With translated views (Cursors only):", niter, image );
		benchmark( IterationMethod.TRANSLATE_VIEW_SPLIT, "With translated views (split into center and borders):", niter, image );
		benchmark( IterationMethod.RANDOM_ACCESS, "With random access:", niter, image );
		benchmark( IterationMethod.RANDOM_ACCESS_SPLIT, "With random access  (split into center and borders):", niter, image );
		benchmark( IterationMethod.RANDOM_ACCESS_NO_EXTEND, "With random access, no out of bounds access:", niter, image );
	}

}
