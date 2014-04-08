package view.converter;

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

import net.imglib2.converter.Converter;
import net.imglib2.converter.RealARGBConverter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import java.util.ArrayList;

import static view.converter.ChannelARGBConverter.Channel.A;
import static view.converter.ChannelARGBConverter.Channel.B;
import static view.converter.ChannelARGBConverter.Channel.G;
import static view.converter.ChannelARGBConverter.Channel.R;

import java.util.ArrayList;

import net.imglib2.display.projector.composite.CompositeXYProjector;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;

/**
 * Convert UnsignedByteType into one channel of {@link net.imglib2.type.numeric.ARGBType}.
 *
 * {@link #converterListRGBA} can be used in {@link net.imglib2.display.projector.composite.CompositeXYProjector} to
 * convert a 4-channel (R,G,B,A) {@link net.imglib2.type.numeric.integer.UnsignedByteType} into composite
 * {@link net.imglib2.type.numeric.ARGBType}.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public final class ChannelARGBConverter implements Converter<UnsignedShortType, ARGBType >
{
    public ChannelARGBConverter( final Channel channel )
    {
        this.shift = channel.shift;
    }

    /**
     * {@link #converterListRGBA} can be used in {@link net.imglib2.display.projector.composite.CompositeXYProjector} to
     * convert a 4-channel {@link UnsignedByteType} into composite
     * {@link ARGBType}.
     */
    public static final ArrayList< Converter< UnsignedShortType, ARGBType > > converterListRGBA;
    static
    {
        converterListRGBA = new ArrayList< Converter< UnsignedShortType, ARGBType > >();
        converterListRGBA.add( new RealARGBConverter< UnsignedShortType >( 0, 10000 ) );
        converterListRGBA.add( new ChannelARGBConverter( R ) );
        converterListRGBA.add( new ChannelARGBConverter( G ) );
        converterListRGBA.add( new ChannelARGBConverter( B ) );
    }

    public static enum Channel
    {
        A( 24 ), R( 16 ), G( 8 ), B( 0 );

        private final int shift;

        Channel( final int shift )
        {
            this.shift = shift;
        }
    }

    final private int shift;

    @Override
    public void convert( final UnsignedShortType input, final ARGBType output )
    {
        output.set( input.get() << shift );
    }
}
