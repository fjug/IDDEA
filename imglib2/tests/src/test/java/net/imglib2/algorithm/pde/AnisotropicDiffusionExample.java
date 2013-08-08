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
package net.imglib2.algorithm.pde;

import ij.ImageJ;
import ij.ImagePlus;

import java.io.File;

import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.io.ImgIOException;
import net.imglib2.io.ImgOpener;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

public class AnisotropicDiffusionExample {

	public static <T extends RealType<T> & NativeType< T >> void  main(String[] args) throws ImgIOException, IncompatibleTypeException {

		// Open file in imglib2
		//		File file = new File( "E:/Users/JeanYves/Desktop/Data/Y.tif");
		File file = new File( "/Users/tinevez/Desktop/Data/sYn.tif");
//		File file = new File( "/Users/tinevez/Desktop/Data/StarryNight.tif");
//		File file = new File( "/Users/tinevez/Desktop/Data/cross2.tif");

		ImgFactory< ? > imgFactory = new ArrayImgFactory< T >();
		
		Img< T > image = new ImgOpener().openImg( file.getAbsolutePath(), imgFactory );
		Img<T> copy = image.copy();

		// Display it via ImgLib using ImageJ
		new ImageJ();

		// Compute tensor
		
		MomentOfInertiaTensor2D<T> tensor = new MomentOfInertiaTensor2D<T>(image, 9);
//		CoherenceEnhancingDiffusionTensor2D<T> tensor = new CoherenceEnhancingDiffusionTensor2D<T>(image);
		
		
		tensor.process();
		Img<FloatType> diffusionTensor = tensor.getResult();

		ImagePlus imp = ImageJFunctions.wrap(image, "source");
		imp.show();

		// Instantiate diffusion solver
		
		NonNegativityDiffusionScheme2D<T> algo = new NonNegativityDiffusionScheme2D<T>(image, diffusionTensor);
//		StandardDiffusionScheme2D<T> algo = new StandardDiffusionScheme2D<T>(image, diffusionTensor);

		for (int i = 0; i < 20; i++) {
			System.out.println("Iteration "+i);
//			tensor.process();
//			diffusionTensor = tensor.getResult();
//			algo.setDiffusionTensor(diffusionTensor);
			
			algo.process();
			imp.getProcessor().setPixels(ImageJFunctions.wrap(image, "result").getProcessor().getPixelsCopy());
			imp.updateAndDraw();
		}

		//	ImageJFunctions.show(algo.getIncrement());
		ImageJFunctions.show(diffusionTensor);
		ImageJFunctions.show(copy);
	}

}
