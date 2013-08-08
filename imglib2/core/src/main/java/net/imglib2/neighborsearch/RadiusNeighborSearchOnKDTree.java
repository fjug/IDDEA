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

package net.imglib2.neighborsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.imglib2.RealLocalizable;
import net.imglib2.Sampler;
import net.imglib2.collection.KDTree;
import net.imglib2.collection.KDTreeNode;
import net.imglib2.util.ValuePair;

/**
 * Implementation of {@link RadiusNeighborSearch} search for kd-trees.
 * 
 * @author Tobias Pietzsch
 */
public class RadiusNeighborSearchOnKDTree< T > implements RadiusNeighborSearch< T >
{
	protected KDTree< T > tree;

	protected final int n;

	protected final double[] pos;

	protected ArrayList< ValuePair< KDTreeNode< T >, Double > > resultPoints;

	public RadiusNeighborSearchOnKDTree( KDTree< T > tree )
	{
		this.tree = tree;
		this.n = tree.numDimensions();
		this.pos = new double[ n ];
		this.resultPoints = new ArrayList< ValuePair< KDTreeNode< T >, Double > >();
	}
	
	@Override
	public void search( final RealLocalizable reference, final double radius, final boolean sortResults )
	{
		assert radius >= 0;
		reference.localize( pos );
		resultPoints.clear();
		searchNode( tree.getRoot(), radius * radius );
		if ( sortResults )
		{
			Collections.sort( resultPoints, new Comparator< ValuePair< KDTreeNode< T >, Double > >()
			{
				@Override
				public int compare( ValuePair< KDTreeNode< T >, Double > o1, ValuePair< KDTreeNode< T >, Double > o2 )
				{
					return Double.compare( o1.b, o2.b );
				}
			} );
		}
	}

	@Override
	public int numDimensions() { return n; }
	
	protected void searchNode( KDTreeNode< T > current, final double squRadius )
	{
		// consider the current node
		final double squDistance = current.squDistanceTo( pos );
		if ( squDistance <= squRadius )
		{
			resultPoints.add( new ValuePair< KDTreeNode< T >, Double >( current, squDistance ) );
		}

		final double axisDiff = pos[ current.getSplitDimension() ] - current.getSplitCoordinate();
		final double axisSquDistance = axisDiff * axisDiff;
		final boolean leftIsNearBranch = axisDiff < 0;

		// search the near branch
		final KDTreeNode< T > nearChild = leftIsNearBranch ? current.left : current.right;
		final KDTreeNode< T > awayChild = leftIsNearBranch ? current.right : current.left;
		if ( nearChild != null )
			searchNode( nearChild, squRadius );

		// search the away branch - maybe
		if ( ( axisSquDistance <= squRadius ) && ( awayChild != null ) )
			searchNode( awayChild, squRadius );
	}

	@Override
	public int numNeighbors()
	{
		return resultPoints.size();
	}

	@Override
	public Sampler< T > getSampler( int i )
	{
		return resultPoints.get( i ).a;
	}

	@Override
	public RealLocalizable getPosition( int i )
	{
		return resultPoints.get( i ).a;
	}

	@Override
	public double getSquareDistance( int i )
	{
		return resultPoints.get( i ).b;
	}

	@Override
	public double getDistance( int i )
	{
		return Math.sqrt( resultPoints.get( i ).b );
	}
}
