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

package net.imglib2.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import loci.formats.FormatTools;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.CharArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Helper methods for converting between SCIFIO and ImgLib2 data structures.
 * 
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 * @author Curtis Rueden
 */
public final class ImgIOUtils {

	private static final int BUFFER_SIZE = 256 * 1024; // 256K

	private ImgIOUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Downloads the given URL and caches it to a temporary file, which is deleted
	 * upon JVM shutdown. This is useful in conjuction with {@link ImgOpener} to
	 * open a URL as an {@link Img}.
	 * <p>
	 * Data compressed with zip or gzip is supported. In the case of zip, the
	 * first file in the archive is cached.
	 * </p>
	 */
	public static String cacheId(final String urlPath) throws ImgIOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			final URL url = new URL(urlPath);
			final String path = url.getPath();
			final boolean zip = path.endsWith(".zip");
			final boolean gz = path.endsWith(".gz");
			String filename = path.substring(path.lastIndexOf("/") + 1);

			// save URL to temporary file
			ZipInputStream inZip = null;
			in = url.openStream();
			if (zip) {
				in = inZip = new ZipInputStream(in);
				final ZipEntry zipEntry = inZip.getNextEntry();
				filename = zipEntry.getName(); // use filename in the zip archive
			}
			if (gz) {
				in = new GZIPInputStream(in);
				filename = filename.substring(0, filename.length() - 3); // strip .gz
			}
			final int dot = filename.lastIndexOf(".");
			final String prefix = dot < 0 ? filename : filename.substring(0, dot);
			final String suffix = dot < 0 ? "" : "." + filename.substring(dot + 1);
			final File tmpFile = File.createTempFile(prefix + "-", suffix);
			tmpFile.deleteOnExit();
			out = new FileOutputStream(tmpFile);
			final byte[] buf = new byte[BUFFER_SIZE];
			while (true) {
				final int r = in.read(buf);
				if (r < 0) break; // eof
				out.write(buf, 0, r);
			}
			return tmpFile.getAbsolutePath();
		}
		catch (final IOException e) {
			throw new ImgIOException(e);
		}
		finally {
			try {
				if (in != null) in.close();
			}
			catch (final IOException e) {
				throw new ImgIOException(e);
			}
			try {
				if (out != null) out.close();
			}
			catch (final IOException e) {
				throw new ImgIOException(e);
			}
		}
	}

	/** Obtains planar access instance backing the given img, if any. */
	@SuppressWarnings("unchecked")
	public static PlanarAccess<ArrayDataAccess<?>> getPlanarAccess(
		final ImgPlus<?> img)
	{
		if (img.getImg() instanceof PlanarAccess) {
			return (PlanarAccess<ArrayDataAccess<?>>) img.getImg();
		}
		return null;
	}

	/** Converts SCIFIO pixel type to ImgLib2 Type object. */
	@SuppressWarnings("unchecked")
	public static <T extends RealType<T>> T makeType(final int pixelType) {
		final RealType<?> type;
		switch (pixelType) {
			case FormatTools.UINT8:
				type = new UnsignedByteType();
				break;
			case FormatTools.INT8:
				type = new ByteType();
				break;
			case FormatTools.UINT16:
				type = new UnsignedShortType();
				break;
			case FormatTools.INT16:
				type = new ShortType();
				break;
			case FormatTools.UINT32:
				type = new UnsignedIntType();
				break;
			case FormatTools.INT32:
				type = new IntType();
				break;
			case FormatTools.FLOAT:
				type = new FloatType();
				break;
			case FormatTools.DOUBLE:
				type = new DoubleType();
				break;
			default:
				type = null;
		}
		return (T) type;
	}

	/**
	 * Converts ImgLib2 Type object to SCIFIO pixel type.
	 */
	public static int makeType(final RealType type) throws ImgIOException {
		int pixelType = FormatTools.UINT8;
		if (type instanceof UnsignedByteType) {
			pixelType = FormatTools.UINT8;
		}
		else if (type instanceof ByteType) {
			pixelType = FormatTools.INT8;
		}
		else if (type instanceof UnsignedShortType) {
			pixelType = FormatTools.UINT16;
		}
		else if (type instanceof ShortType) {
			pixelType = FormatTools.INT16;
		}
		else if (type instanceof UnsignedIntType) {
			pixelType = FormatTools.UINT32;
		}
		else if (type instanceof IntType) {
			pixelType = FormatTools.INT32;
		}
		else if (type instanceof FloatType) {
			pixelType = FormatTools.FLOAT;
		}
		else if (type instanceof DoubleType) {
			pixelType = FormatTools.DOUBLE;
		}
		else {
			throw new ImgIOException("Pixel type not supported. "
				+ "Please convert your image to a supported type.");
		}

		return pixelType;
	}

	/** Wraps raw primitive array in ImgLib2 Array object. */
	public static ArrayDataAccess<?> makeArray(final Object array) {
		final ArrayDataAccess<?> access;
		if (array instanceof byte[]) {
			access = new ByteArray((byte[]) array);
		}
		else if (array instanceof char[]) {
			access = new CharArray((char[]) array);
		}
		else if (array instanceof double[]) {
			access = new DoubleArray((double[]) array);
		}
		else if (array instanceof int[]) {
			access = new IntArray((int[]) array);
		}
		else if (array instanceof float[]) {
			access = new FloatArray((float[]) array);
		}
		else if (array instanceof short[]) {
			access = new ShortArray((short[]) array);
		}
		else if (array instanceof long[]) {
			access = new LongArray((long[]) array);
		}
		else access = null;
		return access;
	}
}
