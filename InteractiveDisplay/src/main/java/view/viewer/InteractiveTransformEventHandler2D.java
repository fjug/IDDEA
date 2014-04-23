package view.viewer;
/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2014 Stephan Preibisch, Tobias Pietzsch, Barry DeZonia,
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
 * #L%
 */

import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformEventHandler2D;
import net.imglib2.ui.TransformEventHandlerFactory;
import net.imglib2.ui.TransformListener;


/**
 * This class is inherited from {@link TransformEventHandler2D} overriding setCanvasSize which is
 * able to scale it according to the minimum value between height and width.
 *
 * @author HongKee Moon
 */
public class InteractiveTransformEventHandler2D extends TransformEventHandler2D
{
    final static private TransformEventHandlerFactory< AffineTransform2D > factory = new TransformEventHandlerFactory< AffineTransform2D >()
    {
        @Override
        public TransformEventHandler< AffineTransform2D > create( final TransformListener< AffineTransform2D > transformListener )
        {
            return new InteractiveTransformEventHandler2D( transformListener );
        }
    };

    public static TransformEventHandlerFactory< AffineTransform2D > factory()
    {
        return factory;
    }

    public InteractiveTransformEventHandler2D( final TransformListener< AffineTransform2D > listener )
    {
        super(listener);
    }

    @Override
    public void setCanvasSize( final int width, final int height, final boolean updateTransform )
    {
        if ( updateTransform )
        {
            synchronized ( affine )
            {
                affine.set( affine.get( 0, 2 ) - canvasW / 2, 0, 2 );
                affine.set( affine.get( 1, 2 ) - canvasH / 2, 1, 2 );

                if(width - height < 0)
                    affine.scale( ( double ) width / canvasW );
                else
                    affine.scale( ( double ) height / canvasH );

                affine.set( affine.get( 0, 2 ) + width / 2, 0, 2 );
                affine.set( affine.get( 1, 2 ) + height / 2, 1, 2 );
                update();
            }
        }
        canvasW = width;
        canvasH = height;
        centerX = width / 2;
        centerY = height / 2;
    }
}
