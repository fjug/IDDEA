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

package tests;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ijaux.Constants;
import ijaux.PixLib;
import ijaux.datatype.access.Access;
import ijaux.hypergeom.PixelCube;
import ijaux.hypergeom.index.BaseIndex;
import ijaux.iter.array.ByteForwardIterator;
import ijaux.iter.seq.RasterForwardIterator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.cell.CellCursor;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.cell.DefaultCell;
import net.imglib2.img.planar.PlanarCursor;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/**
 * Tests performance of uint8 image operations with raw byte array, ImageJ,
 * imglib and pixlib libraries.
 * 
 * @author Curtis Rueden
 * @author Dimiter Prodanov
 */
public class PerformanceBenchmark {

	private static final boolean SAVE_RESULTS_TO_DISK = true;

	private static final String METHOD_RAW = "Raw";
	private static final String METHOD_IMAGEJ = "ImageJ";
	private static final String METHOD_IMGLIB_ARRAY = "ImgLib2 (Array)";
	private static final String METHOD_IMGLIB_CELL = "ImgLib2 (Cell)";
	private static final String METHOD_IMGLIB_PLANAR = "ImgLib2 (Planar)";
	private static final String METHOD_IMGLIB_IMAGEPLUS = "ImgLib2 (ImagePlus)";
	private static final String METHOD_PIXLIB_RASTER = "PixLib (Raster Iterator)";
	private static final String METHOD_PIXLIB_GENERIC = "PixLib (Array Iterator)";
	private static final String METHOD_PIXLIB_BYTE =
		"PixLib (Byte Array Iterator)";
	private static final String METHOD_PIXLIB_ACCESS = "PixLib (Access)";

	private final int width, height;
	private final byte[] rawData;
	private final ByteProcessor byteProc;
	private final Img<UnsignedByteType> imgArray;
	private final Img<UnsignedByteType> imgCell;
	private final Img<UnsignedByteType> imgPlanar;
	private final Img<UnsignedByteType> imgImagePlus;
	private final PixelCube<Byte, BaseIndex> pixelCube;

	/**
	 * List of timing results.
	 * <p>
	 * Each element of the list represents an iteration. Each entry maps the
	 * method name to the time measured.
	 * </p>
	 */
	private final List<Map<String, Long>> results =
		new ArrayList<Map<String, Long>>();

	public static void main(final String[] args) throws IOException {
		final int iterations = 10;
		final int size;
		if (args.length > 0) size = Integer.parseInt(args[0]);
		else size = 4000;
		final PerformanceBenchmark bench = new PerformanceBenchmark(size);
		bench.testPerformance(iterations);
		System.exit(0);
	}

	/** Creates objects and measures memory usage. */
	public PerformanceBenchmark(final int imageSize) {
		width = height = imageSize;
		System.out.println();
		System.out.println("===== " + width + " x " + height + " =====");

		final List<Long> memUsage = new ArrayList<Long>();
		memUsage.add(getMemUsage());
		rawData = createRawData(width, height);
		memUsage.add(getMemUsage());
		byteProc = createByteProcessor(rawData, width, height);
		memUsage.add(getMemUsage());
		imgArray = createArrayImage(rawData, width, height);
		memUsage.add(getMemUsage());
		imgCell = createCellImage(rawData, width, height);
		memUsage.add(getMemUsage());
		imgPlanar = createPlanarImage(rawData, width, height);
		memUsage.add(getMemUsage());
		imgImagePlus = createImagePlusImage(byteProc);
		memUsage.add(getMemUsage());
		pixelCube = createPixelCube(byteProc);
		memUsage.add(getMemUsage());

		reportMemoryUsage(memUsage);
	}

	public void testPerformance(final int iterationCount) throws IOException {
		// initialize results map
		results.clear();
		for (int i = 0; i < iterationCount; i++) {
			final Map<String, Long> entry = new HashMap<String, Long>();
			results.add(entry);
		}
		testCheapPerformance(iterationCount);
		if (SAVE_RESULTS_TO_DISK) saveResults("cheap");
		testExpensivePerformance(iterationCount);
		if (SAVE_RESULTS_TO_DISK) saveResults("expensive");
	}

	/**
	 * Saves benchmark results to the given CSV file on disk.
	 * <p>
	 * The motivation is to produce two charts:
	 * </p>
	 * <ol>
	 * <li>performance by iteration number (on each size of image)
	 * <ul>
	 * <li>one line graph per method</li>
	 * <li>X axis = iteration number</li>
	 * <li>Y axis = time needed</li>
	 * </ul>
	 * </li>
	 * <li>average performance by image size (for first iteration, and 10th)
	 * <ul>
	 * <li>one line graph per method</li>
	 * <li>X axis = size of image</li>
	 * <li>Y axis = time needed</li>
	 * </ul>
	 * </li>
	 * </ol>
	 * <p>
	 * The CSV file produced enables graph #1 very easily. For graph #2, results
	 * from several files must be combined.
	 * </p>
	 */
	public void saveResults(final String prefix) throws IOException {
		final StringBuilder sb = new StringBuilder();

		// write header
		final Map<String, Long> firstEntry = results.get(0);
		final String[] methods = firstEntry.keySet().toArray(new String[0]);
		Arrays.sort(methods);
		sb.append("Iteration");
		for (final String method : methods) {
			sb.append("\t");
			sb.append(method);
		}
		sb.append("\n");

		// write data
		for (int iter = 0; iter < results.size(); iter++) {
			final Map<String, Long> entry = results.get(iter);
			sb.append(iter + 1);
			for (final String method : methods) {
				sb.append("\t");
				sb.append(entry.get(method));
			}
			sb.append("\n");
		}

		// write to disk
		final String path =
			"results-" + prefix + "-" + width + "x" + height + ".csv";
		final PrintWriter out = new PrintWriter(new FileWriter(path));
		out.print(sb.toString());
		out.close();
	}

	// -- Helper methods --

	/** Measures performance of a cheap operation (image inversion). */
	private void testCheapPerformance(final int iterationCount) {
		System.out.println();
		System.out.println("-- TIME PERFORMANCE - CHEAP OPERATION --");
		for (int i = 0; i < iterationCount; i++) {
			System.gc();
			System.out.println("Iteration #" + (i + 1) + "/" + iterationCount + ":");
			final List<Long> times = new ArrayList<Long>();
			times.add(System.currentTimeMillis());
			invertRaw(rawData);
			times.add(System.currentTimeMillis());
			invertImageProcessor(byteProc);
			times.add(System.currentTimeMillis());
			invertArrayImage(imgArray);
			times.add(System.currentTimeMillis());
			invertCellImage(imgCell);
			times.add(System.currentTimeMillis());
			invertPlanarImage(imgPlanar);
			times.add(System.currentTimeMillis());
			invertImagePlusImage(imgImagePlus);
			times.add(System.currentTimeMillis());
			invertPixelCubeRaster(pixelCube);
			times.add(System.currentTimeMillis());
			invertPixelCubeGenArray(pixelCube);
			times.add(System.currentTimeMillis());
			invertPixelCubeByteArray(pixelCube);
			times.add(System.currentTimeMillis());
			invertPixelCubeAccess(pixelCube);
			times.add(System.currentTimeMillis());
			logTimePerformance(i, times);
		}
	}

	/** Measures performance of a computationally more expensive operation. */
	private void testExpensivePerformance(final int iterationCount) {
		System.out.println();
		System.out.println("-- TIME PERFORMANCE - EXPENSIVE OPERATION --");
		for (int i = 0; i < iterationCount; i++) {
			System.gc();
			System.out.println("Iteration #" + (i + 1) + "/" + iterationCount + ":");
			final List<Long> times = new ArrayList<Long>();
			times.add(System.currentTimeMillis());
			randomizeRaw(rawData);
			times.add(System.currentTimeMillis());
			randomizeImageProcessor(byteProc);
			times.add(System.currentTimeMillis());
			randomizeArrayImage(imgArray);
			times.add(System.currentTimeMillis());
			randomizeCellImage(imgCell);
			times.add(System.currentTimeMillis());
			randomizePlanarImage(imgPlanar);
			times.add(System.currentTimeMillis());
			randomizeImagePlusImage(imgImagePlus);
			times.add(System.currentTimeMillis());
			randomizePixelCubeRaster(pixelCube);
			times.add(System.currentTimeMillis());
			randomizePixelCubeGenArray(pixelCube);
			times.add(System.currentTimeMillis());
			randomizePixelCubeByteArray(pixelCube);
			times.add(System.currentTimeMillis());
			randomizePixelCubeAccess(pixelCube);
			times.add(System.currentTimeMillis());
			logTimePerformance(i, times);
		}
	}

	private long getMemUsage() {
		final Runtime r = Runtime.getRuntime();
		System.gc();
		System.gc();
		return r.totalMemory() - r.freeMemory();
	}

	private void reportMemoryUsage(final List<Long> memUsage) {
		final long rawMem = computeDifference(memUsage);
		final long ipMem = computeDifference(memUsage);
		final long imgLibArrayMem = computeDifference(memUsage);
		final long imgLibCellMem = computeDifference(memUsage);
		final long imgLibPlanarMem = computeDifference(memUsage);
		final long imgLibImagePlusMem = computeDifference(memUsage);
		final long pixLibMem = computeDifference(memUsage);
		System.out.println();
		System.out.println("-- MEMORY OVERHEAD --");
		System.out.println(METHOD_RAW + ": " + rawMem + " bytes");
		System.out.println(METHOD_IMAGEJ + ": " + ipMem + " bytes");
		System.out.println(METHOD_IMGLIB_ARRAY + ": " + imgLibArrayMem + " bytes");
		System.out.println(METHOD_IMGLIB_CELL + ": " + imgLibCellMem + " bytes");
		System.out
			.println(METHOD_IMGLIB_PLANAR + ": " + imgLibPlanarMem + " bytes");
		System.out.println(METHOD_IMGLIB_IMAGEPLUS + ": " + imgLibImagePlusMem +
			" bytes");
		System.out.println("PixLib: " + pixLibMem + " bytes");
	}

	private void logTimePerformance(final int iter, final List<Long> times) {
		final long rawTime = computeDifference(times);
		final long ipTime = computeDifference(times);
		final long imgLibArrayTime = computeDifference(times);
		final long imgLibCellTime = computeDifference(times);
		final long imgLibPlanarTime = computeDifference(times);
		final long imgLibImagePlusTime = computeDifference(times);
		final long pixLibReflTime = computeDifference(times);
		final long pixLibGenArrayTime = computeDifference(times);
		final long pixLibByteArrayTime = computeDifference(times);
		final long pixLibAccessTime = computeDifference(times);

		final Map<String, Long> entry = results.get(iter);
		entry.put(METHOD_RAW, rawTime);
		entry.put(METHOD_IMAGEJ, ipTime);
		entry.put(METHOD_IMGLIB_ARRAY, imgLibArrayTime);
		entry.put(METHOD_IMGLIB_CELL, imgLibCellTime);
		entry.put(METHOD_IMGLIB_PLANAR, imgLibPlanarTime);
		entry.put(METHOD_IMGLIB_IMAGEPLUS, imgLibImagePlusTime);
		entry.put(METHOD_PIXLIB_RASTER, pixLibReflTime);
		entry.put(METHOD_PIXLIB_GENERIC, pixLibGenArrayTime);
		entry.put(METHOD_PIXLIB_BYTE, pixLibByteArrayTime);
		entry.put(METHOD_PIXLIB_ACCESS, pixLibAccessTime);

		reportTime(METHOD_RAW, rawTime, rawTime, ipTime);
		reportTime(METHOD_IMAGEJ, ipTime, rawTime, ipTime);
		reportTime(METHOD_IMGLIB_ARRAY, imgLibArrayTime, rawTime, ipTime);
		reportTime(METHOD_IMGLIB_CELL, imgLibCellTime, rawTime, ipTime);
		reportTime(METHOD_IMGLIB_PLANAR, imgLibPlanarTime, rawTime, ipTime);
		reportTime(METHOD_IMGLIB_IMAGEPLUS, imgLibImagePlusTime, rawTime, ipTime);
		reportTime(METHOD_PIXLIB_RASTER, pixLibReflTime, rawTime, ipTime);
		reportTime(METHOD_PIXLIB_GENERIC, pixLibGenArrayTime, rawTime, ipTime);
		reportTime(METHOD_PIXLIB_BYTE, pixLibByteArrayTime, rawTime, ipTime);
		reportTime(METHOD_PIXLIB_ACCESS, pixLibAccessTime, rawTime, ipTime);
	}

	private long computeDifference(final List<Long> list) {
		final long mem = list.remove(0);
		return list.get(0) - mem;
	}

	private void reportTime(final String label, final long time,
		final long... otherTimes)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("\t");
		sb.append(label);
		sb.append(": ");
		sb.append(time);
		sb.append(" ms");
		for (final long otherTime : otherTimes) {
			sb.append(", ");
			sb.append(time / (float) otherTime);
		}
		System.out.println(sb.toString());
	}

	// -- Creation methods --

	private byte[] createRawData(final int w, final int h) {
		final int size = w * h;
		final int max = w + h;
		final byte[] data = new byte[size];
		int index = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				data[index++] = (byte) (255 * (x + y) / max);
			}
		}
		return data;
	}

	private ByteProcessor createByteProcessor(final byte[] data, final int w,
		final int h)
	{
		return new ByteProcessor(w, h, data, null);
	}

	private PixelCube<Byte, BaseIndex> createPixelCube(final ByteProcessor bp) {
		final PixLib plib = new PixLib();
		final PixelCube<Byte, BaseIndex> pc =
			plib.cubeFrom(bp, Constants.BASE_INDEXING);
		return pc;
	}

	private Img<UnsignedByteType> createArrayImage(final byte[] data,
		final int w, final int h)
	{
		// return createImage(data, width, height, new ArrayContainerFactory());
		// NB: Avoid copying the data.
		final ByteArray byteAccess = new ByteArray(data);
		final ArrayImg<UnsignedByteType, ByteArray> array =
			new ArrayImg<UnsignedByteType, ByteArray>(byteAccess,
				new long[] { w, h }, 1);
		array.setLinkedType(new UnsignedByteType(array));
		return array;
	}

	private Img<UnsignedByteType> createPlanarImage(final byte[] data,
		final int w, final int h)
	{
		// return createImage(data, width, height, new PlanarContainerFactory());
		// NB: Avoid copying the data.
		final PlanarImg<UnsignedByteType, ByteArray> planarContainer =
			new PlanarImg<UnsignedByteType, ByteArray>(new long[] { w, h }, 1);
		planarContainer.setPlane(0, new ByteArray(data));
		planarContainer.setLinkedType(new UnsignedByteType(planarContainer));
		return planarContainer;
	}

	private Img<UnsignedByteType> createCellImage(final byte[] data, final int w,
		final int h)
	{
		return createImage(data, w, h, new CellImgFactory<UnsignedByteType>());
	}

	private Img<UnsignedByteType> createImagePlusImage(final ImageProcessor ip) {
		final ImagePlus imp = new ImagePlus("image", ip);
		return ImagePlusAdapter.wrapByte(imp);
	}

	private Img<UnsignedByteType> createImage(final byte[] data, final int w,
		final int h, final ImgFactory<UnsignedByteType> cf)
	{
		final long[] dim = { w, h };
		final Img<UnsignedByteType> img = cf.create(dim, new UnsignedByteType());
		int index = 0;
		for (final UnsignedByteType t : img)
			t.set(data[index++]);
		return img;
	}

	// -- Inversion methods --

	private void invertRaw(final byte[] data) {
		for (int i = 0; i < data.length; i++) {
			final int value = data[i] & 0xff;
			final int result = 255 - value;
			data[i] = (byte) result;
		}
	}

	private void invertImageProcessor(final ImageProcessor ip) {
		for (int i = 0; i < ip.getPixelCount(); i++) {
			final int value = ip.get(i);
			final int result = 255 - value;
			ip.set(i, result);
		}
	}

	/** Generic version. */
	@SuppressWarnings("unused")
	private void invertImage(final Img<UnsignedByteType> img) {
		for (final UnsignedByteType t : img) {
			final int value = t.get();
			final int result = 255 - value;
			t.set(result);
		}
	}

	/** Explicit array version. */
	private void invertArrayImage(final Img<UnsignedByteType> img) {
		final ArrayCursor<UnsignedByteType> c =
			(ArrayCursor<UnsignedByteType>) img.cursor();
		while (c.hasNext()) {
			final UnsignedByteType t = c.next();
			final int value = t.get();
			final int result = 255 - value;
			t.set(result);
		}
	}

	/** Explicit cell version. */
	private void invertCellImage(final Img<UnsignedByteType> img) {
		@SuppressWarnings("unchecked")
		final CellCursor<UnsignedByteType, ByteArray, DefaultCell<ByteArray>> c =
			(CellCursor<UnsignedByteType, ByteArray, DefaultCell<ByteArray>>) img
				.cursor();
		while (c.hasNext()) {
			final UnsignedByteType t = c.next();
			final int value = t.get();
			final int result = 255 - value;
			t.set(result);
		}
	}

	/** Explicit planar version. */
	private void invertPlanarImage(final Img<UnsignedByteType> img) {
		final PlanarCursor<UnsignedByteType> c =
			(PlanarCursor<UnsignedByteType>) img.cursor();
		while (c.hasNext()) {
			final UnsignedByteType t = c.next();
			final int value = t.get();
			final int result = 255 - value;
			t.set(result);
		}
	}

	/** Explicit ImagePlus version. */
	private void invertImagePlusImage(final Img<UnsignedByteType> img) {
		final PlanarCursor<UnsignedByteType> c =
			(PlanarCursor<UnsignedByteType>) img.cursor();
		while (c.hasNext()) {
			final UnsignedByteType t = c.next();
			final int value = t.get();
			final int result = 255 - value;
			t.set(result);
		}
	}

	private void invertPixelCubeRaster(final PixelCube<Byte, BaseIndex> pc) {
		pc.setIterationPattern(Constants.IP_FWD + Constants.IP_SINGLE);
		final RasterForwardIterator<Byte> iterIn =
			(RasterForwardIterator<Byte>) pc.iterator();
		while (iterIn.hasNext()) {
			final int value = iterIn.next() & 0xff;
			final int result = 255 - value;
			iterIn.dec();
			iterIn.put((byte) result);
		}
	}

	private void invertPixelCubeGenArray(final PixelCube<Byte, BaseIndex> pc) {
		pc.setIterationPattern(Constants.IP_PRIM + Constants.IP_FWD +
			Constants.IP_SINGLE);
		final ByteForwardIterator iter = (ByteForwardIterator) pc.iterator();
		for (final byte b : pc) {
			final int value = b & 0xff;
			final int result = 255 - value;
			iter.putInt(result);
		}
	}

	private void invertPixelCubeByteArray(final PixelCube<Byte, BaseIndex> pc) {
		pc.setIterationPattern(Constants.IP_PRIM + Constants.IP_FWD +
			Constants.IP_SINGLE);
		final ByteForwardIterator iter = (ByteForwardIterator) pc.iterator();
		while (iter.hasNext()) {
			final int value = iter.nextByte() & 0xff;
			final int result = 255 - value;
			iter.dec();
			iter.putInt(result);
		}
	}

	private void invertPixelCubeAccess(final PixelCube<Byte, BaseIndex> pc) {
		final Access<Byte> access = pc.getAccess();
		final int size = access.size()[0];
		for (int i = 0; i < size; i++) {
			final int value = access.elementInt(i);
			final int result = 255 - value;
			access.putInt(i, result);
		}
	}

	// -- Randomization methods --

	private void randomizeRaw(final byte[] data) {
		for (int i = 0; i < data.length; i++) {
			final int value = data[i] & 0xff;
			final double result = expensiveOperation(value);
			data[i] = (byte) result;
		}
	}

	private void randomizeImageProcessor(final ImageProcessor ip) {
		for (int i = 0; i < ip.getPixelCount(); i++) {
			final int value = ip.get(i);
			final double result = expensiveOperation(value);
			ip.set(i, (int) result);
		}
	}

	/** Generic version. */
	@SuppressWarnings("unused")
	private void randomizeImage(final Img<UnsignedByteType> img) {
		for (final UnsignedByteType t : img) {
			final int value = t.get();
			final double result = expensiveOperation(value);
			t.set((int) result);
		}
	}

	/** Explicit array version. */
	private void randomizeArrayImage(final Img<UnsignedByteType> img) {
		final ArrayCursor<UnsignedByteType> c =
			(ArrayCursor<UnsignedByteType>) img.cursor();
		while (c.hasNext()) {
			final UnsignedByteType t = c.next();
			final int value = t.get();
			final double result = expensiveOperation(value);
			t.set((int) result);
		}
	}

	/** Explicit cell version. */
	private void randomizeCellImage(final Img<UnsignedByteType> img) {
		@SuppressWarnings("unchecked")
		final CellCursor<UnsignedByteType, ByteArray, DefaultCell<ByteArray>> c =
			(CellCursor<UnsignedByteType, ByteArray, DefaultCell<ByteArray>>) img
				.cursor();
		while (c.hasNext()) {
			final UnsignedByteType t = c.next();
			final int value = t.get();
			final double result = expensiveOperation(value);
			t.set((int) result);
		}
	}

	/** Explicit planar version. */
	private void randomizePlanarImage(final Img<UnsignedByteType> img) {
		final PlanarCursor<UnsignedByteType> c =
			(PlanarCursor<UnsignedByteType>) img.cursor();
		while (c.hasNext()) {
			final UnsignedByteType t = c.next();
			final int value = t.get();
			final double result = expensiveOperation(value);
			t.set((int) result);
		}
	}

	/** Explicit ImagePlus version. */
	private void randomizeImagePlusImage(final Img<UnsignedByteType> img) {
		final PlanarCursor<UnsignedByteType> c =
			(PlanarCursor<UnsignedByteType>) img.cursor();
		while (c.hasNext()) {
			final UnsignedByteType t = c.next();
			final int value = t.get();
			final double result = expensiveOperation(value);
			t.set((int) result);
		}
	}

	private void randomizePixelCubeRaster(final PixelCube<Byte, BaseIndex> pc) {
		pc.setIterationPattern(Constants.IP_FWD + Constants.IP_SINGLE);
		final RasterForwardIterator<Byte> iterIn =
			(RasterForwardIterator<Byte>) pc.iterator();
		while (iterIn.hasNext()) {
			final int value = iterIn.next() & 0xff;
			final double result = expensiveOperation(value);
			iterIn.dec();
			iterIn.put((byte) result);
		}
	}

	private void randomizePixelCubeGenArray(final PixelCube<Byte, BaseIndex> pc) {
		final int iterType =
			Constants.IP_PRIM + Constants.IP_FWD + Constants.IP_SINGLE;
		pc.setIterationPattern(iterType);
		final ByteForwardIterator iter = (ByteForwardIterator) pc.iterator();
		for (final byte b : pc) {
			final int value = b & 0xff;
			final double result = expensiveOperation(value);
			iter.putDouble(result);
		}
	}

	private void randomizePixelCubeByteArray(final PixelCube<Byte, BaseIndex> pc)
	{
		final int iterType =
			Constants.IP_PRIM + Constants.IP_FWD + Constants.IP_SINGLE;
		pc.setIterationPattern(iterType);
		final ByteForwardIterator iter = (ByteForwardIterator) pc.iterator();
		while (iter.hasNext()) {
			final int value = iter.nextByte() & 0xff;
			final double result = expensiveOperation(value);
			iter.dec();
			iter.putDouble(result);
		}
	}

	private void randomizePixelCubeAccess(final PixelCube<Byte, BaseIndex> pc) {
		final Access<Byte> access = pc.getAccess();
		final int size = access.size()[0];
		for (int i = 0; i < size; i++) {
			final int value = access.elementInt(i);
			final double result = expensiveOperation(value);
			access.putDouble(i, result);
		}
	}

	private double expensiveOperation(final int value) {
		return 255 * Math.random() * Math.sin(value / 255.0);
	}

}
