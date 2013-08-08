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
package net.imglib2.ops.operation.randomaccessibleinterval.unary;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.ops.operation.metadata.unary.CopyCalibratedSpace;
import net.imglib2.ops.operation.metadata.unary.CopyImageMetadata;
import net.imglib2.ops.operation.metadata.unary.CopyMetadata;
import net.imglib2.ops.operation.metadata.unary.CopyNamed;
import net.imglib2.ops.operation.metadata.unary.CopySourced;
import net.imglib2.ops.operation.subset.views.ImgPlusView;
import net.imglib2.ops.operation.subset.views.ImgView;
import net.imglib2.ops.operation.subset.views.LabelingView;
import net.imglib2.ops.operation.subset.views.SubsetViews;
import net.imglib2.type.Type;

/**
 * Applies a given Operation to each interval separately.
 * 
 * @author Christian Dietz (University of Konstanz)
 * @author Clemens Muething (University of Konstanz)
 */
public final class IterateUnaryOperation< T extends Type< T >, V extends Type< V >, S extends RandomAccessibleInterval< T >, U extends RandomAccessibleInterval< V >> implements UnaryOperation< S, U >
{

	private final ExecutorService m_service;

	private final UnaryOperation< S, U > m_op;

	private final Interval[] m_outIntervals;

	private final Interval[] m_inIntervals;

	public IterateUnaryOperation( UnaryOperation< S, U > op, Interval[] inIntervals )
	{
		this( op, inIntervals, inIntervals, null );
	}

	public IterateUnaryOperation( UnaryOperation< S, U > op, Interval[] inIntervals, Interval[] outIntervals )
	{
		this( op, inIntervals, outIntervals, null );
	}

	public IterateUnaryOperation( UnaryOperation< S, U > op, Interval[] inIntervals, ExecutorService service )
	{
		this( op, inIntervals, inIntervals, service );
	}

	public IterateUnaryOperation( UnaryOperation< S, U > op, Interval[] inIntervals, Interval[] outIntervals, ExecutorService service )
	{

		if ( inIntervals.length != outIntervals.length ) { throw new IllegalArgumentException( "In and out intervals do not match! Most likely an implementation error!" ); }

		m_op = op;
		m_inIntervals = inIntervals;
		m_outIntervals = outIntervals;
		m_service = service;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final U compute( final S in, final U out )
	{

		Future< ? >[] futures = new Future< ? >[ m_inIntervals.length ];

		for ( int i = 0; i < m_outIntervals.length; i++ )
		{

			if ( Thread.interrupted() )
				return out;

			OperationTask t = new OperationTask( m_op, createSubType( m_inIntervals[ i ], in ), createSubType( m_outIntervals[ i ], out ) );

			if ( m_service != null )
			{
				if ( m_service.isShutdown() )
					return out;
				futures[ i ] = m_service.submit( t );
			}
			else
			{
				t.run();
			}
		}

		if ( m_service != null )
		{
			try
			{
				for ( Future< ? > f : futures )
				{
					if ( f.isCancelled() ) { return out; }
					f.get();
				}
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
				return out;

			}
			catch ( ExecutionException e )
			{
				e.printStackTrace();
				return out;
			}
		}
		return out;
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	private synchronized < TT extends Type< TT >, II extends RandomAccessibleInterval< TT > > II createSubType( final Interval i, final II in )
	{
		if ( in instanceof Labeling ) { return ( II ) new LabelingView( SubsetViews.iterableSubsetView( in, i ), ( ( NativeImgLabeling ) in ).factory() ); }

		if ( in instanceof ImgPlus )
		{
			ImgPlusView< TT > imgPlusView = new ImgPlusView< TT >( SubsetViews.iterableSubsetView( in, i ), ( ( ImgPlus ) in ).factory() );
			new CopyMetadata( new CopyNamed(), new CopySourced(), new CopyImageMetadata(), new CopyCalibratedSpace( i ) ).compute( ( ImgPlus ) in, imgPlusView );
			return ( II ) imgPlusView;
		}

		if ( in instanceof Img ) { return ( II ) new ImgView< TT >( SubsetViews.iterableSubsetView( in, i ), ( ( Img ) in ).factory() ); }

		return ( II ) SubsetViews.iterableSubsetView( in, i );
	}

	@Override
	public UnaryOperation< S, U > copy()
	{
		return new IterateUnaryOperation< T, V, S, U >( m_op.copy(), m_inIntervals, m_outIntervals, m_service );
	}

	/**
	 * Future task
	 * 
	 * @author dietzc, muethingc
	 * 
	 */
	private class OperationTask implements Runnable
	{

		private final UnaryOperation< S, U > m_op;

		private final S m_in;

		private final U m_out;

		public OperationTask( final UnaryOperation< S, U > op, final S in, final U out )
		{
			m_in = in;
			m_out = out;
			m_op = op.copy();
		}

		@Override
		public void run()
		{
			m_op.compute( m_in, m_out );
		}

	}
}
