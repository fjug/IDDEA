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

package tests;

import ij.ImageJ;
import ij.ImagePlus;

import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;

import mpicbg.models.AffineModel3D;
import mpicbg.models.TranslationModel3D;
import net.imglib2.algorithm.transformation.ImageTransform;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;

/**
 * 
 *
 * @version 0.1a
 *
 * @author Stephan Saalfeld <saalfeld@mpi-cbg.de>
 */
public class ShapeListBehavior
{
	/**
	 * @param args
	 */
	public static < T extends RealType< T > > void main( String[] args )
	{
		new ImageJ();
		
		final int depth = 50;
		

		/* Create ShapeList */
		final ShapeList< ByteType > shapeList = new ShapeListCached<ByteType>( new int[]{ 200, 200, depth },  new ByteType( ) );
		final Image< ByteType > shapeListImage = new Image< ByteType >( shapeList, shapeList.getBackground(), "ShapeListContainer" ); 
		
		/* add some shapes */
		for ( int i = 0; i < depth; ++i )
		{
			shapeList.addShape( new Rectangle( 10 + i, 20, 40, 70 + 2 * i ), new ByteType( ( byte )64 ), new int[]{ i } );
			shapeList.addShape( new Polygon( new int[]{ 90 + i, 180 - 2 * i, 190 - 4 * i, 120 - 2 * i }, new int[]{ 90, 80 + i, 140 - 3 * i, 130 - 2 * i }, 4 ), new ByteType( ( byte )127 ), new int[]{ i } );
		}
		
		shapeListImage.getDisplay().setMinMax();
		final ImagePlus shapeListImp = ImageJFunctions.displayAsVirtualStack( shapeListImage );
		shapeListImp.show();
		//shapeListImp.getProcessor().setMinAndMax( 0, 255 );
		//shapeListImp.updateAndDraw();
		/* ----------------------------------------------------------------- */

		
		
		/* Copy content into another container */
		final ArrayContainerFactory arrayFactory = new ArrayContainerFactory();
		final Image< ByteType > arrayImage = new ImageFactory< ByteType >( new ByteType(), arrayFactory ).createImage( new int[]{ 200, 200, depth }, "ArrayContainer" );
		final LocalizableCursor< ByteType > cArray = arrayImage.createLocalizableCursor();
		final LocalizableByDimCursor< ByteType > cShapeList = shapeListImage.createLocalizableByDimCursor();
		
		while ( cArray.hasNext() )
		{
			cArray.fwd();
			cShapeList.moveTo( cArray );
			cArray.getType().set( cShapeList.getType() );
		}

		arrayImage.getDisplay().setMinMax();
		final ImagePlus arrayImp = ImageJFunctions.displayAsVirtualStack( arrayImage );
		arrayImp.show();
		//arrayImp.getProcessor().setMinAndMax( 0, 255 );
		//arrayImp.updateAndDraw();
		/* ----------------------------------------------------------------- */
		
		
		/* Copy content rotated into another container */
		final CellImgFactory cellFactory = new CellImgFactory();
		final AffineModel3D affine = new AffineModel3D();
		affine.set(
				0.7660444f, -0.6427875f, 0.0f, 0.0f,
				0.6330221f, 0.75440645f, -0.17364818f, 0.0f,
				0.111618884f, 0.1330222f, 0.9848077f, 0.0f );
		final TranslationModel3D translation = new TranslationModel3D();
		translation.set( 100, 0, -15 );
		
		affine.preConcatenate( translation );
		
		final ImageTransform<ByteType> transform = new ImageTransform<ByteType>( shapeListImage, affine, new NLinearInterpolatorFactory<ByteType>( new OutOfBoundsStrategyValueFactory<ByteType>() ) );
		transform.setOutputImageFactory( new ImageFactory< ByteType >( new ByteType(), cellFactory ) );
		
		if ( !transform.checkInput() || !transform.process() )
		{
			System.out.println( transform.getErrorMessage() );
			return;
		}
		
		final Image<ByteType> cellImage = transform.getResult();
		
		/*
		final Image< ByteType > cellImage = new ImageFactory< ByteType >( new ByteType(), cellFactory ).createImage( new int[]{ 200, 200, depth }, "Rotated CellContainer" );
		final LocalizableCursor< ByteType > cCell = cellImage.createLocalizableCursor();
		
		final int[] iLocation = new int[ cellImage.getNumDimensions() ];
		final float[] fLocation = new float[ cellImage.getNumDimensions() ];
		
		while ( cCell.hasNext() )
		{
			cCell.fwd();
			cCell.getPosition( iLocation );
			
			for ( int d = 0; d < iLocation.length; ++d )
				fLocation[ d ] = iLocation[ d ];
			
			try { affine.applyInverseInPlace( fLocation ); }
			catch ( final NoninvertibleModelException e ){}
			
			for ( int d = 0; d < iLocation.length; ++d )
				iLocation[ d ] = Math.round( fLocation[ d ] );
			
			cShapeList.setPosition( iLocation );
			//cShapeList.moveTo( iLocation );
			
			try { cCell.getType().set( cShapeList.getType() ); }
			catch ( final IndexOutOfBoundsException e ){}
		}
		*/
		
		cellImage.getDisplay().setMinMax();
		final ImagePlus cellImp = ImageJFunctions.displayAsVirtualStack( cellImage );
		cellImp.show();
		//cellImp.getProcessor().setMinAndMax( 0, 255 );
		//cellImp.updateAndDraw();
		/* ----------------------------------------------------------------- */

		
		shapeListImp.updateAndDraw();
		arrayImp.updateAndDraw();
		cellImp.updateAndDraw();
		
		
		try
		{
			Thread.sleep( 1000 );
		}
		catch ( final InterruptedException e ){}
	}
}
