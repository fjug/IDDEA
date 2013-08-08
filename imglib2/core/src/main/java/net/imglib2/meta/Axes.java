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

package net.imglib2.meta;

import java.util.Hashtable;

import net.imglib2.img.ImgPlus;

/**
 * An enumeration of common dimensional axis types, for describing the
 * dimensions of a {@link CalibratedSpace} object (such as an {@link ImgPlus}).
 * 
 *
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 * @author Curtis Rueden
 */
public enum Axes implements AxisType {

	/**
	 * Identifies the <i>X</i> dimensional type, representing a dimension in the
	 * first (X) spatial dimension.
	 */
	X("X"),

	/**
	 * Identifies the <i>Y</i> dimensional type, representing a dimension in the
	 * second (Y) spatial dimension.
	 */
	Y("Y"),

	/**
	 * Identifies the <i>Z</i> dimensional type, representing a dimension in the
	 * third (Z) spatial dimension.
	 */
	Z("Z"),

	/**
	 * Identifies the <i>Time</i> dimensional type, representing a dimension
	 * consisting of time points.
	 */
	TIME("Time"),

	/**
	 * Identifies the <i>Channel</i> dimensional type, representing a generic
	 * channel dimension.
	 */
	CHANNEL("Channel"),

	/**
	 * Identifies the <i>Spectra</i> dimensional type, representing a dimension
	 * consisting of spectral channels.
	 */
	SPECTRA("Spectra"),

	/**
	 * Identifies the <i>Lifetime</i> dimensional type, representing a dimension
	 * consisting of a lifetime histogram.
	 */
	LIFETIME("Lifetime"),

	/**
	 * Identifies the <i>Polarization</i> dimensional type, representing a
	 * dimension consisting of polarization states.
	 */
	POLARIZATION("Polarization"),

	/**
	 * Identifies the <i>Phase</i> dimensional type, representing a dimension
	 * consisting of phases.
	 */
	PHASE("Phase"),

	/**
	 * Identifies the <i>Frequency</i> dimensional type, representing a dimension
	 * consisting of frequencies.
	 */
	FREQUENCY("Frequency"),

	/** Represents an unknown dimensional type. */
	UNKNOWN("Unknown");

	private static Hashtable<String, AxisType> axes =
		new Hashtable<String, AxisType>();

	static {
		for (final AxisType axis : Axes.values()) {
			axes.put(axis.getLabel(), axis);
		}
	}

	public synchronized static AxisType get(final String label) {
		AxisType axis = axes.get(label);
		if (axis == null) {
			axis = new CustomAxisType(label);
			axes.put(label, axis);
		}
		return axis;
	}

	public static boolean isXY(final AxisType dimLabel) {
		return dimLabel == Axes.X || dimLabel == Axes.Y;
	}

	private String label;

	private Axes(final String label) {
		this.label = label;
	}

	// -- Axis methods --

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public boolean isXY() {
		return this == Axes.X || this == Axes.Y;
	}

	@Override
	public boolean isSpatial() {
		return isXY() || this == Axes.Z;
	}

	// -- Object methods --

	@Override
	public String toString() {
		return label;
	}

	// -- Helper classes --

	/**
	 * A custom dimensional axis type, for describing the dimensional axes of a
	 * {@link CalibratedSpace} object (such as an {@link ImgPlus}).
	 */
	public static class CustomAxisType implements AxisType {

		private final String label;

		public CustomAxisType(final String label) {
			this.label = label;
		}

		// -- Axis methods --

		@Override
		public String getLabel() {
			return label;
		}

		@Override
		public boolean isXY() {
			return false;
		}

		@Override
		public boolean isSpatial() {
			return false;
		}

		// -- Object methods --

		@Override
		public String toString() {
			return label;
		}

	}

}
