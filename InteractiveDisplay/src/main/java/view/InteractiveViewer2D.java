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

package view;

import java.awt.Graphics;


import net.imglib2.display.ARGBScreenImage;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GraphicsConfiguration;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.ui.*;
import net.imglib2.concatenate.Concatenable;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineSet;
import net.imglib2.ui.AffineTransformType;
import net.imglib2.ui.InteractiveDisplayCanvas;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.RenderSource;
import net.imglib2.ui.TransformListener;
import net.imglib2.ui.PainterThread.Paintable;
import net.imglib2.ui.util.GuiUtil;
/**
 *
 * @param <T>
 *
 * @author TobiasPietzsch <tobias.pietzsch@gmail.com>
 * @author Stephan Saalfeld <saalfeld@mpi-cbg.de>
 */
public class InteractiveViewer2D< T > implements TransformListener< AffineTransform2D >, PainterThread.Paintable
{
    final protected RenderSource< T, AffineTransform2D > source;

    /**
     * Transformation set by the interactive viewer.
     */
    final protected AffineTransform2D viewerTransform;

    /**
     * Canvas used for displaying the rendered {@link #screenImages screen image}.
     */
    final protected JHotDrawInteractiveDisplay2D< AffineTransform2D > display;

    /**
     * Thread that triggers repainting of the display.
     */
    final protected PainterThread painterThread;

    final protected MultiResolutionRenderer< AffineTransform2D > imageRenderer;

    public InteractiveViewer2D( final int width, final int height, final RenderSource< T, AffineTransform2D > source )
    {
        this.source = source;
        viewerTransform = new AffineTransform2D();

        display = new JHotDrawInteractiveDisplay2D<AffineTransform2D>( width, height, TransformEventHandler2D.factory() );
        display.addTransformListener( this );

        painterThread = new PainterThread( this );

        final boolean doubleBuffered = true;
        final int numRenderingThreads = 3;
        final double[] screenScales = new double[] { 1, 0.5, 0.25, 0.125 };
        final long targetRenderNanos = 15 * 1000000;
        imageRenderer = new MultiResolutionRenderer< AffineTransform2D >( AffineTransformType2D.instance, display, painterThread, screenScales, targetRenderNanos, doubleBuffered, numRenderingThreads );

        painterThread.start();
    }

    @Override
    public void paint()
    {
        imageRenderer.paint( source, viewerTransform );
        display.repaint();
    }


    @Override
    public void transformChanged( final AffineTransform2D transform )
    {
        viewerTransform.set( transform );
        requestRepaint();
    }

    public void requestRepaint()
    {
        imageRenderer.requestRepaint();
    }

    public JHotDrawInteractiveDisplay2D getDisplay()
    {
        return display;
    }
}
