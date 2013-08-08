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

package net.imglib2.algorithm.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import net.imglib2.algorithm.Benchmark;
import net.imglib2.algorithm.OutputAlgorithm;
import net.imglib2.img.Img;
import net.imglib2.img.ImgCursor;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgRandomAccess;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
/**
 * This class implements a very simple peak-picker, with optional ellipsoidal peak suppression.
 * Peaks are found by taking the sign of the difference operator in each dimension, differentiating
 * between negative and non-negative differences, then finding transitions from non-negative to 
 * negative.  This is accomplished in a random-access manner, in other words, with one
 * {@link LocalizableCursor} irrespective of how it traverses the {@link Image}, and a
 * {@link LocalizableByDimCursor} that is set to its 2^n-connected neighbors (where n is
 * dimensionality).
 * 
 * The result is that this is a fairly simple, (hopefully) fast peak-picker, but it is accurate
 * only for strict peaks, that is, peaks that have no neighbors of equal value.
 * 
 * This picker does no pre-processing, so it may be advisable to smooth your peak image before 
 * using this. 
 * 
 * @param <T> the {@link ComparableType} representing information stored in the {@link Image} to
 * pick peaks from.
 *
 * @author Larry Lindsey
 */
public class PickImagePeaks <T extends RealType<T>> implements OutputAlgorithm<Img<BitType>>, Benchmark
{
	private final Img<T> image;
	private long pTime;
	private Img<BitType> peakImage;
	private ImgFactory<BitType> peakContainerFactory;
	final private ArrayList<int[]> peakLocList;
	private final double[] suppressAxis;
	private double suppressSum;

	private class Peak implements Comparable<Peak>
	{
		private final T peakVal;
		private final int[] pos;

		public Peak(final int[] inPos, final T val)
		{
			peakVal = val;
			pos = inPos;
		}
		
		@Override
		public int compareTo(final Peak inPeak) {
			/*
			 * You're probably wondering why this is negated.
			 * It is because Collections.sort() sorts only in the forward direction.
			 * I want these to be sorted in the descending order, and the Collections.sort
			 * method is the only thing that should ever touch Peak.compareTo.
			 * This is faster than sorting, then reversing.
			 */
			//return -(this.peakVal.compareTo(inPeak.peakVal));
			//float hereVal = peakVal.
			//f//loat thereVal = inPeak.peakVal.getReal();
			if (peakVal.compareTo(inPeak.peakVal) == 1)
			{
				return -1;
			}
			else if (peakVal.compareTo(inPeak.peakVal) == 0)
			{
				return 0;
			}
			else
			{
				return 1;
			}
		}
		
		public int[] getPosition()
		{
			return pos;
		}
		
		public double distanceFactor(final int inPos[])
		{
			double val = 0;
			for (int i = 0; i < pos.length; i++)
			{
				val += Math.pow((((double) pos[i] - inPos[i]) / (double)suppressAxis[i]), 2.0);
			}
			val =  Math.sqrt(val);
			
			return val;
		}
		
	}
	
	public PickImagePeaks(final Img<T> inputImage)
	{
		image = inputImage;
		pTime = 0;
		peakContainerFactory = null;
		peakLocList = new ArrayList<int[]>();
		peakImage = null;
		suppressAxis = new double[inputImage.numDimensions()];
		Arrays.fill(suppressAxis, 0);
		suppressSum = 0;
	}
		
	/**
	 * Carries out ellipsoidal peak suppression.
	 * This works by first sorting the peaks in peakList by their corresponding magnitudes,
	 * clearing peakList, then adding the peaks back in, one-by-one, only if they are not
	 * within the suppression ellipsoid of any other peaks that have already been added, 
	 * as defined by suppressAxis.
	 */
	private void doSuppression()
	{
		/*
		 * I have a great suspicion that this code, as I write it, will be fairly slow.
		 * Here there be type casts (of ints, doubles, and such), among other Bad Things.
		 */
		if (peakLocList.size() > 0 && suppressSum >= 1)
		{
			final ArrayList<Peak> suppressionList = new ArrayList<Peak>();
			final ImgRandomAccess<T> imCursor = image.randomAccess();
			
			T type;
			//populate the suppression list.
			for (int[] pos : peakLocList)				
			{
				imCursor.setPosition(pos);
				type = imCursor.get().copy();
				suppressionList.add(new Peak(pos, type));
			}
			//sort the list.
			Collections.sort(suppressionList);
			peakLocList.clear();
			//AHH! ~O(n^2)!
			for (Peak p : suppressionList)
			{
				boolean isOK = true;
				for (int[] pos : peakLocList)
				{
					if (p.distanceFactor(pos) < 1)
					{
						isOK = false;
						break;
					}
					
				}
				
				if (isOK)
				{
					peakLocList.add(p.getPosition());
				}
			
			}
		}
	}
	
	@Override
	public boolean checkInput() {		
		return true;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public boolean process() {
		final long sTime = System.currentTimeMillis();
		
		final ImgCursor<T> cursor = image.localizingCursor();
		final ImgRandomAccess<T> localCursor = image.randomAccess();
		ImgRandomAccess<BitType> peakImageCursor;
		//InterMediate Image Cursor
		ImgCursor<BitType> imImagePullCursor;
		ImgRandomAccess<BitType> imImagePushCursor;
		final long[] dimensions = Util.intervalDimensions(image);
		final int[] pos = new int[dimensions.length];
		final int[] checkPos = new int[pos.length];
		final ImgFactory<BitType> peakFactory =
			null == peakContainerFactory ?
				new ArrayImgFactory<BitType>()
				: peakContainerFactory;
		/* Create an intermediate image.  This image will contain a sort of signum operation of the difference  
		 * along a given dimension of the input image.  "Sort of" because 1 corresponds to greater than or
		 * equal to zero, while 0 corresponds to less than 0, rather than the traditions signum.
		 * I've written this method in this way in order that we don't have to care what order the
		 * cursor traverses the Image. 
		*/
		Img<BitType> imImage;
		T t0, tc;

		peakImage = peakFactory.create(dimensions, new BitType());
		imImage = peakFactory.create(dimensions, new BitType());
		imImagePullCursor = imImage.localizingCursor();	
		imImagePushCursor = imImage.randomAccess();
		//imImagePushCursor is kind of a misnomer.  it'll be used for pulling, too, later.
		
		peakImageCursor = peakImage.randomAccess();
				
		peakLocList.clear();
		
		//Iterate Over Dimension
		for (int d = 0; d < pos.length; ++d)
		{
			cursor.reset();
			//first step: take the "signum of diff" down this dimension			
			while(cursor.hasNext())
			{				
				cursor.fwd();			
				imImagePushCursor.setPosition(cursor);				
				System.arraycopy(pos, 0, checkPos, 0, pos.length);
				checkPos[d] -= 1;
				
				if (checkPos[d] < 0)
				{
					imImagePushCursor.get().set(false);
				}
				else
				{					
					localCursor.setPosition(checkPos);
					t0 = cursor.get();
					tc = localCursor.get();
					imImagePushCursor.get().set(tc.compareTo(t0) >= 0);
				}								
			}
			/* OK. Now we should have a signum-diff image corresponding to
			 * dimension d in our current image.
			*/
			//ImageJFunctions.displayAsVirtualStack(imImage).show();
			imImagePullCursor.reset();
			while(imImagePullCursor.hasNext())
			{
				imImagePullCursor.fwd();
				peakImageCursor.setPosition( imImagePullCursor );
				System.arraycopy(pos, 0, checkPos, 0, pos.length);
				checkPos[d] += 1;
				
				if (checkPos[d] >= dimensions[d])
				{
					//No peaks around the boundary of the image.
					peakImageCursor.get().set(false);
				}
				else if (d == 0 || peakImageCursor.get().get())
				{					
					/* (d == 0 || peakImageCursor.getType().get():
					 *   If d == 0, peakImage is assumed to be full of garbage.
					 *   Otherwise, we only want to change the value there if it currently true 
					*/
					imImagePushCursor.setPosition(checkPos);
					peakImageCursor.get().set(!imImagePullCursor.get().get() && imImagePushCursor.get().get());
				}				
			}
		}				
		pTime = System.currentTimeMillis() - sTime;
		return true;
	}

	@Override
	public long getProcessingTime() {
		return pTime;
	}

	@Override
	public Img<BitType> getResult() {
		return peakImage;
	}
	
	/**
	 * Returns an ArrayList containing the locations of peaks in the image associated with this
	 * peak picker, as calculated by running the process() method.  This ArrayList will be 
	 * populated if it is not already.
	 * 
	 * In the case that there is no peak suppression, the locations are placed in the list as
	 * they are returned by calling the LocalizableCursor.getPosition() method, and are not
	 * guaranteed to have anything like a natural order.
	 * 
	 * In the case that there is peak suppression, an additional step is taken.  The peaks are
	 * first collected, then sorted by magnitude.  These are then selected in order, and the
	 * peaks within a distance defined by the suppressAxis array are rejected.
	 * 
	 * There is no peak suppression if the sum of all values in suppressAxis is less than 1.
	 * 
	 * @return an ArrayList containing peak locations
	 */
	public ArrayList<int[]> getPeakList()
	{
		if (peakLocList.isEmpty() && peakImage!=null)
		{			
			final ImgCursor<BitType> pkCursor = peakImage.localizingCursor();
			peakLocList.clear();
			
			while (pkCursor.hasNext())
			{
				pkCursor.fwd();
				if (pkCursor.get().get())
				{
					final int[] pos = new int[peakImage.numDimensions()];
					pkCursor.localize(pos);
					peakLocList.add(pos);
				}				
			}

			doSuppression();
			
		}
		
		return peakLocList;
	}
	
	/**
	 * Sets the peak suppression region to a spheroid of radius r.
	 * @param r the radius of the spheroid of peak suppression.
	 */
	public void setSuppression(final double r)
	{
		Arrays.fill(suppressAxis, r);
		suppressSum = suppressAxis.length * r;
		
	}
	
	/**
	 * Sets the peak suppression region to an ellipsoid with dimensional axes corresponding to the
	 * elements in r.
	 * @param r an array with as many elements as there are dimensions in the {@link Image} with
	 * which this {@link PickImagePeaks} was created, and represents the extent of the ellipsoid
	 */
	public void setSuppression(final double[] r)
	{
		System.arraycopy(r, 0, suppressAxis, 0, suppressAxis.length);
		suppressSum = 0;
		for (double a : r)
		{
			suppressSum += a;
		}
	}
	
}
