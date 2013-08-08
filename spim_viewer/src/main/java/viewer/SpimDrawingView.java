

package viewer;


import edu.umd.cs.findbugs.annotations.Nullable;
import mpicbg.spim.data.SequenceDescription;
import net.imglib.display.RealARGBColorConverter;
import net.imglib.ui.OverlayRenderer;
import net.imglib.ui.PainterThread;
import net.imglib.ui.component.InteractiveDisplay3DCanvas;
import net.imglib2.Interval;
import net.imglib2.Pair;
import net.imglib2.Positionable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealPositionable;
import net.imglib2.histogram.DiscreteFrequencyDistribution;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.ui.TransformEventHandler3D;
import net.imglib2.ui.TransformListener3D;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

import org.jhotdraw.draw.*;
import org.jhotdraw.draw.event.FigureSelectionEvent;
import org.jhotdraw.draw.event.FigureSelectionListener;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.draw.event.HandleListener;
import org.jhotdraw.draw.event.HandleEvent;
import org.jhotdraw.draw.event.FigureListener;
import org.jhotdraw.draw.event.FigureAdapter;
import org.jhotdraw.draw.event.FigureEvent;
import org.jhotdraw.draw.event.CompositeFigureListener;
import org.jhotdraw.draw.event.CompositeFigureEvent;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.*;

import org.jhotdraw.util.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;

import org.jhotdraw.gui.EditableComponent;
import org.xml.sax.SAXException;

import viewer.SpimViewer.AlignPlane;
import viewer.SpimViewer.MouseCoordinateListener;
import viewer.TextOverlayAnimator.TextPosition;
import viewer.crop.CropDialog;
import viewer.gui.brightness.ConverterSetup;
import viewer.gui.brightness.MinMaxGroup;
import viewer.gui.brightness.NewBrightnessDialog;
import viewer.gui.brightness.SetupAssignments;
import viewer.render.Interpolation;
import viewer.render.MultiResolutionRenderer;
import viewer.render.Source;
import viewer.render.SourceAndConverter;
import viewer.render.SourceState;
import viewer.render.ViewerState;
import viewer.render.overlay.MultiBoxOverlayRenderer;
import viewer.render.overlay.SourceInfoOverlayRenderer;
import viewer.util.AbstractTransformAnimator;
import static org.jhotdraw.draw.AttributeKeys.*;

import java.awt.image.VolatileImage;


public class SpimDrawingView extends DefaultDrawingView 
	implements OverlayRenderer, TransformListener3D, PainterThread.Paintable
{

	final KeyStroke brightnessKeystroke = KeyStroke.getKeyStroke( KeyEvent.VK_S, 0 );

	final KeyStroke helpKeystroke = KeyStroke.getKeyStroke( KeyEvent.VK_F1, 0 );

	final KeyStroke HelpKeystroke2 = KeyStroke.getKeyStroke( KeyEvent.VK_H, 0 );

	final KeyStroke cropKeystroke = KeyStroke.getKeyStroke( KeyEvent.VK_F5, 0 );
	
	private int contentWidth;
	private int contentHeight;

	SpimDrawingView viewer;

	SetupAssignments setupAssignments;

	NewBrightnessDialog brightnessDialog;

	CropDialog cropDialog;
	
	JSlider sliderTime;
	
	public SpimDrawingView(JSlider sliderTime)
	{
		super();
		this.sliderTime = sliderTime;
		
	
		try {
			SequenceLoad("/Users/Moon/Desktop/MPI-CBG_course/tiffs/export.xml");	
		} catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void toggleBrightnessDialog()
	{
		brightnessDialog.setVisible( ! brightnessDialog.isVisible() );
	}

	public void showHelp()
	{
		new HelpFrame();
	}

	public void crop()
	{
		cropDialog.setVisible( ! cropDialog.isVisible() );
	}
	
	public void SequenceLoad(final String xmlFilename)
			throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		final int width = 800;
		final int height = 600;

		final SequenceViewsLoader loader = new SequenceViewsLoader( xmlFilename );
		final SequenceDescription seq = loader.getSequenceDescription();

		final ArrayList< ConverterSetup > converterSetups = new ArrayList< ConverterSetup >();
		final ArrayList< SourceAndConverter< ? > > sources = new ArrayList< SourceAndConverter< ? > >();
		for ( int setup = 0; setup < seq.numViewSetups(); ++setup )
		{
			final RealARGBColorConverter< UnsignedShortType > converter = new RealARGBColorConverter< UnsignedShortType >( 0, 65535 );
			converter.setColor( new ARGBType( ARGBType.rgba( 255, 255, 255, 255 ) ) );
			sources.add( new SourceAndConverter< UnsignedShortType >( new SpimSource( loader, setup, "angle " + seq.setups.get( setup ).getAngle() ), converter ) );
			final int id = setup;
			converterSetups.add( new ConverterSetup()
			{
				@Override
				public void setDisplayRange( final int min, final int max )
				{
					converter.setMin( min );
					converter.setMax( max );
					viewer.requestRepaint();
				}

				@Override
				public void setColor( final ARGBType color )
				{
					converter.setColor( color );
					viewer.requestRepaint();
				}

				@Override
				public int getSetupId()
				{
					return id;
				}

				@Override
				public int getDisplayRangeMin()
				{
					return ( int ) converter.getMin();
				}

				@Override
				public int getDisplayRangeMax()
				{
					return ( int ) converter.getMax();
				}

				@Override
				public ARGBType getColor()
				{
					return converter.getColor();
				}
			} );
		}
		
		viewer = this;

		initSpimViewer( width, height, sources, seq.numTimepoints() );
		
		viewer.addKeyAction( brightnessKeystroke, new AbstractAction( "brightness settings" )
		{
			@Override
			public void actionPerformed( final ActionEvent arg0 )
			{
				toggleBrightnessDialog();
			}

			private static final long serialVersionUID = 1L;
		} );

		final AbstractAction helpAction = new AbstractAction( "help" )
		{
			@Override
			public void actionPerformed( final ActionEvent arg0 )
			{
				showHelp();
			}

			private static final long serialVersionUID = 1L;
		};
		viewer.addKeyAction( helpKeystroke, helpAction );
		viewer.addKeyAction( HelpKeystroke2, helpAction );

		viewer.addKeyAction( cropKeystroke, new AbstractAction( "crop" )
		{
			@Override
			public void actionPerformed( final ActionEvent arg0 )
			{
				try
				{
					crop();
				}
				catch ( final Exception e )
				{
					e.printStackTrace();
				}
			}

			private static final long serialVersionUID = 1L;
		} );

		setupAssignments = new SetupAssignments( converterSetups, 0, 65535 );
		final MinMaxGroup group = setupAssignments.getMinMaxGroups().get( 0 );
		for ( final ConverterSetup setup : setupAssignments.getConverterSetups() )
			setupAssignments.moveSetupToGroup( setup, group );
		Window parentWindow = SwingUtilities.windowForComponent(this); 
		
		brightnessDialog = new NewBrightnessDialog( parentWindow, setupAssignments );
		//viewer.installKeyActions( brightnessDialog.getRootPane() );

		cropDialog = new CropDialog( parentWindow, viewer, seq );
		//viewer.installKeyActions( cropDialog.getRootPane() );


		initTransform( width, height );
		initBrightness( 0.001, 0.999 );
	}
		
	
	void initTransform( final int viewerWidth, final int viewerHeight )
	{
		final int cX = viewerWidth / 2;
		final int cY = viewerHeight / 2;

		final ViewerState state = viewer.getState();
		final SourceState< ? > source = state.getSources().get( state.getCurrentSource() );
		final int timepoint = state.getCurrentTimepoint();
		final AffineTransform3D sourceTransform = source.getSpimSource().getSourceTransform( timepoint, 0 );

		final Interval sourceInterval = source.getSpimSource().getSource( timepoint, 0 );
		final double sX0 = sourceInterval.min( 0 );
		final double sX1 = sourceInterval.max( 0 );
		final double sY0 = sourceInterval.min( 1 );
		final double sY1 = sourceInterval.max( 1 );
		final double sZ0 = sourceInterval.min( 2 );
		final double sZ1 = sourceInterval.max( 2 );
		final double sX = ( sX0 + sX1 + 1 ) / 2;
		final double sY = ( sY0 + sY1 + 1 ) / 2;
		final double sZ = ( sZ0 + sZ1 + 1 ) / 2;

		final double[][] m = new double[3][4];

		// rotation
		final double[] qSource = new double[ 4 ];
		final double[] qViewer = new double[ 4 ];
		RotationAnimator.extractApproximateRotationAffine( sourceTransform, qSource, 2 );
		LinAlgHelpers.quaternionInvert( qSource, qViewer );
		LinAlgHelpers.quaternionToR( qViewer, m );

		// translation
		final double[] centerSource = new double[] { sX, sY, sZ };
		final double[] centerGlobal = new double[ 3 ];
		final double[] translation = new double[ 3 ];
		sourceTransform.apply( centerSource, centerGlobal );
		LinAlgHelpers.quaternionApply( qViewer, centerGlobal, translation );
		LinAlgHelpers.scale( translation, -1, translation );
		LinAlgHelpers.setCol( 3, translation, m );

		final AffineTransform3D viewerTransform = new AffineTransform3D();
		viewerTransform.set( m );

		// scale
		final double[] pSource = new double[] { sX1 + 0.5, sY1 + 0.5, sZ };
		final double[] pGlobal = new double[ 3 ];
		final double[] pScreen = new double[ 3 ];
		sourceTransform.apply( pSource, pGlobal );
		viewerTransform.apply( pGlobal, pScreen );
		final double scaleX = cX / pScreen[ 0 ];
		final double scaleY = cY / pScreen[ 1 ];
		final double scale = Math.min( scaleX, scaleY );
		viewerTransform.scale( scale );

		// window center offset
		viewerTransform.set( viewerTransform.get( 0, 3 ) + cX, 0, 3 );
		viewerTransform.set( viewerTransform.get( 1, 3 ) + cY, 1, 3 );

		viewer.setCurrentViewerTransform( viewerTransform );
	}

	void initBrightness( final double cumulativeMinCutoff, final double cumulativeMaxCutoff )
	{
		final ViewerState state = viewer.getState();
		final Source< ? > source = state.getSources().get( state.getCurrentSource() ).getSpimSource();
		final RandomAccessibleInterval< UnsignedShortType > img = ( RandomAccessibleInterval ) source.getSource( state.getCurrentTimepoint(), source.getNumMipmapLevels() - 1 );
		final long z = ( img.min( 2 ) + img.max( 2 ) + 1 ) / 2;

		final int numBins = 6535;
		final Histogram1d< UnsignedShortType > histogram = new Histogram1d< UnsignedShortType >( Views.iterable( Views.hyperSlice( img, 2, z ) ), new Real1dBinMapper< UnsignedShortType >( 0, 65535, numBins, false ) );
		final DiscreteFrequencyDistribution dfd = histogram.dfd();
		final long[] bin = new long[] {0};
		double cumulative = 0;
		int i = 0;
		for ( ; i < numBins && cumulative < cumulativeMinCutoff; ++i )
		{
			bin[ 0 ] = i;
			cumulative += dfd.relativeFrequency( bin );
		}
		final int min = i * 65535 / numBins;
		for ( ; i < numBins && cumulative < cumulativeMaxCutoff; ++i )
		{
			bin[ 0 ] = i;
			cumulative += dfd.relativeFrequency( bin );
		}
		final int max = i * 65535 / numBins;
		final MinMaxGroup minmax = setupAssignments.getMinMaxGroups().get( 0 );
		minmax.getMinBoundedValue().setCurrentValue( min );
		minmax.getMaxBoundedValue().setCurrentValue( max );
	}
	
	
	// SpimViewer
	protected ViewerState state;

	protected MultiResolutionRenderer imageRenderer;

	protected MultiBoxOverlayRenderer multiBoxOverlayRenderer;

	protected SourceInfoOverlayRenderer sourceInfoOverlayRenderer;

	/**
	 * Transformation set by the interactive viewer.
	 */
	protected AffineTransform3D viewerTransform;


	/**
	 * Thread that triggers repainting of the display.
	 */
	protected PainterThread painterThread;

	protected MouseCoordinateListener mouseCoordinates;
	
	protected MouseAdapter mouseFocusAdapter;
	
	protected ComponentListener componentResizeListener;

	protected ArrayList< Pair< KeyStroke, Action > > keysActions;

	protected AbstractTransformAnimator currentAnimator = null;
	
	public void initSpimViewer(final int width, final int height, final Collection< SourceAndConverter< ? > > sources, final int numTimePoints)
	{
		state = new ViewerState( sources, numTimePoints );
		if ( ! sources.isEmpty() )
			state.setCurrentSource( 0 );
		multiBoxOverlayRenderer = new MultiBoxOverlayRenderer( width, height );
		sourceInfoOverlayRenderer = new SourceInfoOverlayRenderer();

		painterThread = new PainterThread( this );
		viewerTransform = new AffineTransform3D();
		//display = new InteractiveDisplay3DCanvas( width, height, this, this );
		//display = new InteractiveDisplay3DCanvas( width, height, this, this );
		//System.out.println(width + "-" + height);
		this.setPreferredSize( new Dimension( width, height ) );
		this.setContentHeight(height); this.setContentWidth(width);
		//System.out.println(this.getWidth() + "-" + this.getHeight());
		this.bufferedImage = null;
		this.renderer = this;
		this.renderTransformListener = this;
		
		componentResizeListener =  new ComponentListener()
		{
			@Override
			public void componentShown( final ComponentEvent e ) {}

			@Override
			public void componentMoved( final ComponentEvent e ) {}

			@Override
			public void componentHidden( final ComponentEvent e ) {}

			int oldW = width;

			int oldH = height;

			AffineTransform3D tmp = new AffineTransform3D();

			@Override
			public void componentResized( final ComponentEvent e )
			{
				final int w = getContentWidth();
				final int h = getContentHeight();

				tmp.set( handler.getTransform() );
				tmp.set( tmp.get( 0, 3 ) - oldW/2, 0, 3 );
				tmp.set( tmp.get( 1, 3 ) - oldH/2, 1, 3 );
				tmp.scale( ( double ) w / oldW );
				tmp.set( tmp.get( 0, 3 ) + w/2, 0, 3 );
				tmp.set( tmp.get( 1, 3 ) + h/2, 1, 3 );
				handler.setTransform( tmp );
				handler.setWindowCenter( w / 2, h / 2 );
				renderTransformListener.transformChanged( tmp );
				enableEvents( AWTEvent.MOUSE_MOTION_EVENT_MASK );

				oldW = w;
				oldH = h;
			}
		};

		mouseFocusAdapter = 
				new MouseAdapter()
				{
					@Override
					public void mousePressed( final MouseEvent e )
					{
						requestFocusInWindow();
					}
				};
		
		handler = new TransformEventHandler3D( renderTransformListener );
		handler.setWindowCenter( width / 2, height / 2 );
		//addHandler( handler );

		final double[] screenScales = new double[] { 1, 0.75, 0.5, 0.25, 0.125 };
		final long targetRenderNanos = 30 * 1000000;
		final long targetIoNanos = 10 * 1000000;
		final int badIoFrameBlockFrames = 5;
		final boolean doubleBuffered = true;
		final int numRenderingThreads = 3;
		imageRenderer = new MultiResolutionRenderer( this, painterThread, screenScales, targetRenderNanos, targetIoNanos, badIoFrameBlockFrames, doubleBuffered, numRenderingThreads );

		mouseCoordinates = new MouseCoordinateListener() ;
		//addHandler( mouseCoordinates );

		sliderTime.setMaximum(numTimePoints - 1);
		sliderTime.addChangeListener( new ChangeListener() {
			@Override
			public void stateChanged( final ChangeEvent e )
			{
				if ( e.getSource().equals( sliderTime ) )
					updateTimepoint( sliderTime.getValue() );
			}
		} );
		
//		final GraphicsConfiguration gc = GuiHelpers.getSuitableGraphicsConfiguration( ARGBScreenImage.ARGB_COLOR_MODEL );
		final GraphicsConfiguration gc = GuiHelpers.getSuitableGraphicsConfiguration( GuiHelpers.RGB_COLOR_MODEL );
		
//		frame = new JFrame( "multi-angle viewer", gc );
//		frame.getRootPane().setDoubleBuffered( true );
//		final Container content = frame.getContentPane();
//		content.add( display, BorderLayout.CENTER );
//		content.add( sliderTime, BorderLayout.SOUTH );
//		frame.pack();
//		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
//		frame.addWindowListener( new WindowAdapter()
//		{
//			@Override
//			public void windowClosing( final WindowEvent e )
//			{
//				painterThread.interrupt();
//			}
//		} );
//		frame.setVisible( true );
		
		//this.getRootPane().setDoubleBuffered( true );

		keysActions = new ArrayList< Pair< KeyStroke, Action > >();
		createKeyActions();
		//painterThread.start();

		animatedOverlay = new TextOverlayAnimator( "Press <F1> for help.", 3000, TextPosition.CENTER );
	}
	
	public void ActivateMouseHandlers()
	{
		addHandler( handler );
		addHandler( mouseCoordinates );
		addMouseListener(mouseFocusAdapter);
		addComponentListener(componentResizeListener);
		installKeyActions( this.getRootPane() );
	}
	
	public void DeactivateMouseHandlers()
	{
		removeHandler(handler);
		removeHandler(mouseCoordinates);
		removeMouseListener(mouseFocusAdapter);
		removeComponentListener(componentResizeListener);
		removeKeyActions( this.getRootPane() );
	}
	
	public void startThread()
	{
		painterThread.start();
	}

	public void addHandler( final Object handler )
	{	
		if ( KeyListener.class.isInstance( handler ) )
			addKeyListener( ( KeyListener ) handler );

		if ( MouseMotionListener.class.isInstance( handler ) )
			addMouseMotionListener( ( MouseMotionListener ) handler );

		if ( MouseListener.class.isInstance( handler ) )
			addMouseListener( ( MouseListener ) handler );

		if ( MouseWheelListener.class.isInstance( handler ) )
			addMouseWheelListener( ( MouseWheelListener ) handler );
		
//		if ( KeyListener.class.isInstance( handler ) )
//			this.addKeyListener( ( KeyListener ) handler );
	}
	
	public void removeHandler(final Object handler)
	{
		if ( KeyListener.class.isInstance( handler ) )
			removeKeyListener( ( KeyListener ) handler );

		if ( MouseMotionListener.class.isInstance( handler ) )
			removeMouseMotionListener( ( MouseMotionListener ) handler );

		if ( MouseListener.class.isInstance( handler ) )
			removeMouseListener( ( MouseListener ) handler );

		if ( MouseWheelListener.class.isInstance( handler ) )
			removeMouseWheelListener( ( MouseWheelListener ) handler );
	}

	public void getGlobalMouseCoordinates( final RealPositionable gPos )
	{
		assert gPos.numDimensions() == 3;
		final RealPoint lPos = new RealPoint( 3 );
		mouseCoordinates.getMouseCoordinates( lPos );
		viewerTransform.applyInverse( gPos, lPos );
	}

	@Override
	public void paint()
	{
		imageRenderer.paint( state );

		synchronized( this )
		{
			if ( currentAnimator != null )
			{
				final TransformEventHandler3D handler = this.getTransformEventHandler();
				final AffineTransform3D transform = currentAnimator.getCurrent( System.currentTimeMillis() );
				handler.setTransform( transform );
				transformChanged( transform );
				if ( currentAnimator.isComplete() )
					currentAnimator = null;
			}
		}

		this.repaint();
	}
	
    @Override
    public void paintComponent(Graphics gr) {
        Graphics2D g = (Graphics2D) gr;

        setViewRenderingHints(g);
        drawBackground(g);
        drawCanvas(g);
        
		final BufferedImage bi;
		synchronized ( this )
		{
			bi = bufferedImage;
		}
		if ( bi != null )
		{
//			( (Graphics2D ) g).setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
			g.drawImage( bi, 0, 0, getWidth(), getHeight(), null );
			renderer.drawOverlays( g );
		}
		
        drawConstrainer(g);
        if (isDrawingDoubleBuffered()) {
            if (DefaultDrawingView.isWindows) {
                drawDrawingNonvolatileBuffered(g);
            } else {
                drawDrawingVolatileBuffered(g);
            }
        } else {
            drawDrawing(g);
        }
        drawHandles(g);
        drawTool(g);
        
    }

	// TODO remove?
	public void requestRepaint()
	{
		imageRenderer.requestRepaint();
	}

	TextOverlayAnimator animatedOverlay = null;

	@Override
	public void drawOverlays( final Graphics g )
	{
		multiBoxOverlayRenderer.setViewerState( state );
		multiBoxOverlayRenderer.updateVirtualScreenSize( this.getWidth(), this.getHeight() );
		multiBoxOverlayRenderer.paint( ( Graphics2D ) g );

		sourceInfoOverlayRenderer.setViewerState( state );
		sourceInfoOverlayRenderer.paint( ( Graphics2D ) g );

		final RealPoint gPos = new RealPoint( 3 );
		getGlobalMouseCoordinates( gPos );
		final String mousePosGlobalString = String.format( "(%6.1f,%6.1f,%6.1f)", gPos.getDoublePosition( 0 ), gPos.getDoublePosition( 1 ), gPos.getDoublePosition( 2 ) );

		g.setFont( new Font( "Monospaced", Font.PLAIN, 12 ) );
		g.drawString( mousePosGlobalString, ( int ) g.getClipBounds().getWidth() - 170, 25 );

		if ( animatedOverlay != null )
		{
			animatedOverlay.paint( ( Graphics2D ) g, System.currentTimeMillis() );
			if ( animatedOverlay.isComplete() )
				animatedOverlay = null;
			else
				this.repaint();
		}
		if ( multiBoxOverlayRenderer.isHighlightInProgress() )
			this.repaint();
	}

	@Override
	public synchronized void transformChanged( final AffineTransform3D transform )
	{
		viewerTransform.set( transform );
		state.setViewerTransform( transform );
		requestRepaint();
	}


	static enum AlignPlane
	{
		XY,
		ZY,
		XZ
	}

	private final static double c = Math.cos( Math.PI / 4 );
	private final static double[] qAlignXY = new double[] { 1,  0,  0, 0 };
	private final static double[] qAlignZY = new double[] { c,  0, -c, 0 };
	private final static double[] qAlignXZ = new double[] { c,  c,  0, 0 };

	protected synchronized void align( final AlignPlane plane )
	{
		final SourceState< ? > source = state.getSources().get( state.getCurrentSource() );
		final AffineTransform3D sourceTransform = source.getSpimSource().getSourceTransform( state.getCurrentTimepoint(), 0 );

		final double[] qSource = new double[ 4 ];
		RotationAnimator.extractRotationAnisotropic( sourceTransform, qSource );

		final double[] qTmpSource;
		if ( plane == AlignPlane.XY )
		{
			RotationAnimator.extractApproximateRotationAffine( sourceTransform, qSource, 2 );
			qTmpSource = qSource;
		}
		else
		{
			qTmpSource = new double[4];
			if ( plane == AlignPlane.ZY )
			{
				RotationAnimator.extractApproximateRotationAffine( sourceTransform, qSource, 0 );
				LinAlgHelpers.quaternionMultiply( qSource, qAlignZY, qTmpSource );
			}
			else // if ( plane == AlignPlane.XZ )
			{
				RotationAnimator.extractApproximateRotationAffine( sourceTransform, qSource, 1 );
				LinAlgHelpers.quaternionMultiply( qSource, qAlignXZ, qTmpSource );
			}
		}

		final double[] qTarget = new double[ 4 ];
		LinAlgHelpers.quaternionInvert( qTmpSource, qTarget );

		final AffineTransform3D transform = this.getTransformEventHandler().getTransform();
		currentAnimator = new RotationAnimator( transform, mouseCoordinates.getX(), mouseCoordinates.getY(), qTarget, 300 );
		currentAnimator.setTime( System.currentTimeMillis() );
		transformChanged( transform );
	}

	protected synchronized void updateTimepoint( final int timepoint )
	{
		if ( state.getCurrentTimepoint() != timepoint )
		{
			state.setCurrentTimepoint( timepoint );
			requestRepaint();
		}
	}

	final int indicatorTime = 800;

	protected synchronized void toggleInterpolation()
	{
		final Interpolation interpolation = state.getInterpolation();
		if ( interpolation == Interpolation.NEARESTNEIGHBOR )
		{
			state.setInterpolation( Interpolation.NLINEAR );
			animatedOverlay = new TextOverlayAnimator( "tri-linear interpolation", indicatorTime );
		}
		else
		{
			state.setInterpolation( Interpolation.NEARESTNEIGHBOR );
			animatedOverlay = new TextOverlayAnimator( "nearest-neighbor interpolation", indicatorTime );
		}
		requestRepaint();
	}

	public synchronized void toggleSingleSourceMode()
	{
		final boolean singleSourceMode = ! state.isSingleSourceMode();
		state.setSingleSourceMode( singleSourceMode );
		animatedOverlay = new TextOverlayAnimator( singleSourceMode ? "single-source mode" : "fused mode", indicatorTime );
		requestRepaint();
	}

	public synchronized void toggleVisibility( final int sourceIndex )
	{
		if ( sourceIndex >= 0 && sourceIndex < state.numSources() )
		{
			final SourceState< ? > source = state.getSources().get( sourceIndex );
			source.setActive( !source.isActive() );
			multiBoxOverlayRenderer.highlight( sourceIndex );
			if ( ! state.isSingleSourceMode() )
				requestRepaint();
			else
				this.repaint();
		}
	}

	/**
	 * Set the index of the source to display.
	 */
	public synchronized void setCurrentSource( final int sourceIndex )
	{
		if ( sourceIndex >= 0 && sourceIndex < state.numSources() )
		{
			state.setCurrentSource( sourceIndex );
			multiBoxOverlayRenderer.highlight( sourceIndex );
			if ( state.isSingleSourceMode() )
				requestRepaint();
			else
				this.repaint();
		}
	}

	/**
	 * Set the viewer transform.
	 */
	public synchronized void setCurrentViewerTransform( final AffineTransform3D viewerTransform )
	{
		this.getTransformEventHandler().setTransform( viewerTransform );
		transformChanged( viewerTransform );
	}

	/**
	 * Get a copy of the current {@link ViewerState}.
	 *
	 * @return a copy of the current {@link ViewerState}.
	 */
	public synchronized ViewerState getState()
	{
		return state.copy();
	}

	/**
	 * Get the viewer canvas.
	 *
	 * @return the viewer canvas.
	 */
	public SpimDrawingView getDisplay()
	{
		return this;
	}

	/**
	 * Create Keystrokes and corresponding Actions.
	 *
	 * @return list of KeyStroke-Action-pairs.
	 */
	protected void createKeyActions()
	{
		KeyStroke key = KeyStroke.getKeyStroke( KeyEvent.VK_I, 0 );
		Action action = new AbstractAction( "toogle interpolation" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				toggleInterpolation();
			}

			private static final long serialVersionUID = 1L;
		};
		keysActions.add( new ValuePair< KeyStroke, Action >( key, action ) );

		key = KeyStroke.getKeyStroke( KeyEvent.VK_F, 0 );
		action = new AbstractAction( "toogle display mode" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				toggleSingleSourceMode();
			}

			private static final long serialVersionUID = 1L;
		};
		keysActions.add( new ValuePair< KeyStroke, Action >( key, action ) );

		final int[] numkeys = new int[] { KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9, KeyEvent.VK_0 };

		for ( int i = 0; i < numkeys.length; ++i )
		{
			final int index = i;

			key = KeyStroke.getKeyStroke( numkeys[ i ], 0 );
			action = new AbstractAction( "set current source " + i )
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					setCurrentSource( index );
				}

				private static final long serialVersionUID = 1L;
			};
			keysActions.add( new ValuePair< KeyStroke, Action >( key, action ) );

			key = KeyStroke.getKeyStroke( numkeys[ i ], KeyEvent.SHIFT_DOWN_MASK );
			action = new AbstractAction( "toggle source visibility " + i )
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					toggleVisibility( index );
				}

				private static final long serialVersionUID = 1L;
			};
			keysActions.add( new ValuePair< KeyStroke, Action >( key, action ) );
		}

		key = KeyStroke.getKeyStroke( KeyEvent.VK_Z, KeyEvent.SHIFT_DOWN_MASK );
		action = new AbstractAction( "align XY plane" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				align( AlignPlane.XY );
			}

			private static final long serialVersionUID = 1L;
		};
		keysActions.add( new ValuePair< KeyStroke, Action >( key, action ) );

		key = KeyStroke.getKeyStroke( KeyEvent.VK_X, KeyEvent.SHIFT_DOWN_MASK );
		action = new AbstractAction( "align ZY plane" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				align( AlignPlane.ZY );
			}

			private static final long serialVersionUID = 1L;
		};
		keysActions.add( new ValuePair< KeyStroke, Action >( key, action ) );

		key = KeyStroke.getKeyStroke( KeyEvent.VK_Y, KeyEvent.SHIFT_DOWN_MASK );
		action = new AbstractAction( "align XZ plane" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				align( AlignPlane.XZ );
			}

			private static final long serialVersionUID = 1L;
		};
		keysActions.add( new ValuePair< KeyStroke, Action >( key, action ) );
		key = KeyStroke.getKeyStroke( KeyEvent.VK_A, KeyEvent.SHIFT_DOWN_MASK );
		keysActions.add( new ValuePair< KeyStroke, Action >( key, action ) );

		key = KeyStroke.getKeyStroke( KeyEvent.VK_CLOSE_BRACKET, 0, false );
		action = new AbstractAction( "next timepoint" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				sliderTime.setValue( sliderTime.getValue() + 1 );
			}

			private static final long serialVersionUID = 1L;
		};
		keysActions.add( new ValuePair< KeyStroke, Action >( key, action ) );
		key = KeyStroke.getKeyStroke( KeyEvent.VK_M, 0 );
		keysActions.add( new ValuePair< KeyStroke, Action >( key, action ) );

		key = KeyStroke.getKeyStroke( KeyEvent.VK_OPEN_BRACKET, 0, false );
		action = new AbstractAction( "previous timepoint" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				sliderTime.setValue( sliderTime.getValue() - 1 );
			}

			private static final long serialVersionUID = 1L;
		};
		keysActions.add( new ValuePair< KeyStroke, Action >( key, action ) );
		key = KeyStroke.getKeyStroke( KeyEvent.VK_N, 0 );
		keysActions.add( new ValuePair< KeyStroke, Action >( key, action ) );
	}

	public void addKeyAction( final KeyStroke keystroke, final Action action )
	{
		keysActions.add( new ValuePair< KeyStroke, Action >( keystroke, action ) );
		//installKeyActions( this.getRootPane() );
	}

	/**
	 * Add Keystrokes and corresponding Actions from {@link #keysActions} to a container.
	 */
	public void installKeyActions( final JComponent rootpane )
	{
		final ActionMap am = rootpane.getActionMap();
		final InputMap im = rootpane.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		for ( final Pair< KeyStroke, Action > keyAction : keysActions )
		{
			final KeyStroke key = keyAction.getA();
			final Action action = keyAction.getB();
			im.put( key, action.getValue( Action.NAME ) );
			am.put( action.getValue( Action.NAME ), action );
		}
	}
	
	/**
	 * Remove Keystrokes and corresponding Actions from {@link #keysActions} to a container.
	 */
	public void removeKeyActions( final JComponent rootpane )
	{
		final ActionMap am = rootpane.getActionMap();
		final InputMap im = rootpane.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		for ( final Pair< KeyStroke, Action > keyAction : keysActions )
		{
			final KeyStroke key = keyAction.getA();
			final Action action = keyAction.getB();
			im.remove( key );
			am.remove( action.getValue(Action.NAME) );
		}
	}

	protected class MouseCoordinateListener implements MouseMotionListener
	{
		private int x;

		private int y;

		public synchronized void getMouseCoordinates( final Positionable p )
		{
			p.setPosition( x, 0 );
			p.setPosition( y, 1 );
		}

		@Override
		public synchronized void mouseDragged( final MouseEvent e )
		{
			x = e.getX();
			y = e.getY();
		}

		@Override
		public synchronized void mouseMoved( final MouseEvent e )
		{
			x = e.getX();
			y = e.getY();
			viewer.repaint(); // TODO: only when overlays are visible
		}

		public synchronized int getX()
		{
			return x;
		}

		public synchronized int getY()
		{
			return y;
		}
	}
	
	// InteractiveDisplay3DCanvas
	/**
	 * Mouse/Keyboard handler that manipulates the viewer transformation.
	 */
	protected TransformEventHandler3D handler;

	protected OverlayRenderer renderer;

	protected TransformListener3D renderTransformListener;

	/**
	 * The {@link BufferedImage} that is actually drawn on the canvas. Depending
	 * on {@link #discardAlpha} this is either the {@link BufferedImage}
	 * obtained from {@link #screenImage}, or {@link #screenImage}s buffer
	 * re-wrapped using a RGB color model.
	 */
	protected BufferedImage bufferedImage;

	/**
	 * The {@link BufferedImage} that is to be drawn on the canvas.
	 *
	 * @param bufferedImage image to draw.
	 */
	public synchronized void setBufferedImage( final BufferedImage bufferedImage )
	{
		this.bufferedImage = bufferedImage;
	}

	public TransformEventHandler3D getTransformEventHandler()
	{
		return handler;
	}
	

	/**
	 * @return the w
	 */
	public int getContentWidth() {
		return contentWidth;
	}

	/**
	 * @param w
	 *            the w to set
	 */
	public void setContentWidth( final int w ) {
		this.contentWidth = w;
	}

	/**
	 * @return the h
	 */
	public int getContentHeight() {
		return contentHeight;
	}

	/**
	 * @param h
	 *            the h to set
	 */
	public void setContentHeight( final int h ) {
		this.contentHeight = h;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension( contentWidth, contentHeight );
	}
}
