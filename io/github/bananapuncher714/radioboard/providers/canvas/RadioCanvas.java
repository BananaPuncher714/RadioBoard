package io.github.bananapuncher714.radioboard.providers.canvas;

import java.awt.Image;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.radioboard.api.DisplayInteract;
import io.github.bananapuncher714.radioboard.api.Frame;
import io.github.bananapuncher714.radioboard.api.MapDisplay;
import io.github.bananapuncher714.radioboard.api.MapDisplayProvider;
import io.github.bananapuncher714.radioboard.util.JetpImageUtil;

/**
 * A simple canvas that allows modification through RadioIcons
 * 
 * @author BananaPuncher714
 */
public class RadioCanvas implements MapDisplayProvider {
	protected int[] canvas;
	protected int[] buffer;
	protected int width;
	
	// Maintain some sort of ordering for these icons so that we can define which ones go on top
	protected List< RadioIcon > iconOrdering = new CopyOnWriteArrayList< RadioIcon >();
	// Maps out each radio icon to a coordinate
	protected Map< RadioIcon, int[] > icons = new ConcurrentHashMap< RadioIcon, int[] >();
	
	protected MapDisplay display;

	/**
	 * Construct a blank canvas
	 * 
	 * @param width
	 * @param height
	 */
	public RadioCanvas( int width, int height ) {
		this.width = width;
		canvas = new int[ width * height ];
		buffer = canvas.clone();
	}
	
	/**
	 * Construct a new canvas with the given image as the background
	 * 
	 * @param width
	 * @param height
	 * @param image
	 */
	public RadioCanvas( int width, int height, Image image ) {
		this.width = width;
		canvas = JetpImageUtil.getRGBArray( JetpImageUtil.toBufferedImage( JetpImageUtil.toBufferedImage( image ).getScaledInstance( width, height, Image.SCALE_SMOOTH ) ) );
		buffer = canvas.clone();
	}
	
	@Override
	public Frame getSource() {
		return new Frame( 0, 0, JetpImageUtil.dither( width, buffer ), width );
	}
	
	@Override
	public void interactAt( Player player, DisplayInteract action, int x, int y ) {
		for ( RadioIcon icon : iconOrdering ) {
			int[] coords = icons.get( icon );
			if ( x < coords[ 0 ] || x >= coords[ 0 ] + icon.getWidth() ) {
				continue;
			}
			if ( y < coords[ 1 ] || y >= coords[ 1 ] + icon.getHeight() ) {
				continue;
			}
			
			icon.onClick( player, action, x - coords[ 0 ], y - coords[ 1 ] );
		}
	}
	
	/**
	 * Load a given icon at specific coordinates
	 * 
	 * @param icon
	 * @param x
	 * @param y
	 */
	public void loadIcon( RadioIcon icon, int x, int y ) {
		icons.put( icon, new int[] { x, y } );
		iconOrdering.add( icon );
		
		// Initialize the RadioIcon and update
		icon.init( this );
		
		update( icon, true );
	}
	
	/**
	 * Update and send a radio icon, calls {@link #update(RadioIcon, boolean)}
	 * 
	 * @param icon
	 */
	public void update( RadioIcon icon ) {
		update( icon, true );
	}
	
	/**
	 * Update an icon and optionally update the map display; follows the same principle as {@link #updateBuffer()} but more complicated
	 * 
	 * @param icon
	 * @param send
	 */
	public void update( RadioIcon icon, boolean send ) {
		if ( icons.keySet().contains( icon ) ) {
			// First we calculate the position of the icon and the maximum width and height we can render
			int[] globalCoords = icons.get( icon );
			
			int bufferWidth = width;
			int bufferHeight = buffer.length / width;
			
			int[] globalWidthData = JetpImageUtil.getSubsegment( 0, bufferWidth, globalCoords[ 0 ], icon.getWidth() );
			int[] globalHeightData = JetpImageUtil.getSubsegment( 0, bufferHeight, globalCoords[ 1 ], icon.getHeight() );
			
			int globalWidthStart = globalWidthData[ 0 ];
			int globalWidthEnd = globalWidthData[ 1 ];
			int globalWidthLength = globalWidthEnd - globalWidthStart;
			
			int globalHeightStart = globalHeightData[ 0 ];
			int globalHeightEnd = globalHeightData[ 1 ];
			int globalHeightLength = globalHeightEnd - globalHeightStart;

			// Clear the buffer and redraw from the canvas
			for ( int y = globalHeightStart; y < globalHeightEnd; y++ ) {
				int canvasIndexY = y * width;
				for ( int x = globalWidthStart; x < globalWidthEnd; x++ ) {
					int canvasIndex = canvasIndexY + x;
					buffer[ canvasIndex ] = canvas[ canvasIndex ];
				}
			}
			
			// Draw each RadioIcon in order
			for ( RadioIcon nextIcon : iconOrdering ) {
				int[] localCoords = icons.get( nextIcon );
				int[] source = nextIcon.getDisplay();
				
				// Get the relative position of this icon to the icon we are updating
				// No sense in updating more than necessary
				int[] localWidthData = JetpImageUtil.getSubsegment( globalWidthStart, globalWidthLength, localCoords[ 0 ], nextIcon.getWidth() );
				int[] localHeightData = JetpImageUtil.getSubsegment( globalHeightStart, globalHeightLength, localCoords[ 1 ], nextIcon.getHeight() );
				
				int localWidthStart = localWidthData[ 0 ];
				int localWidthEnd = localWidthData[ 1 ];
				int localWidthLength = localWidthEnd - localWidthStart;
				
				int localHeightStart = localHeightData[ 0 ];
				int localHeightEnd = localHeightData[ 1 ];
				int localHeightLength = localHeightEnd - localHeightStart;
				
				int localWidth = localWidthStart - localCoords[ 0 ];
				int localHeight = localHeightStart - localCoords[ 1 ];
				// Get the local coordinate of the icon we are displaying and copy onto the buffer
				for ( int pixY = 0; pixY < localHeightLength; pixY++ ) {
					int bufferYVal = ( localHeightStart + pixY ) * bufferWidth + localWidthStart;
					int iconYVal = ( localHeight + pixY ) * nextIcon.getWidth() ;
					for ( int pixX = 0; pixX < localWidthLength; pixX++ ) {
						int bufferIndex = pixX + bufferYVal;
						
						buffer[ bufferIndex ] = JetpImageUtil.overwriteColor( buffer[ bufferIndex ], source[ localWidth + pixX + iconYVal ] );
					}
				}
			}
			
			if ( send && display != null ) {
				// Get the subimage from the buffer and send it
				int[] subImage = JetpImageUtil.getSubImage( globalCoords[ 0 ], globalCoords[ 1 ], icon.getWidth(), icon.getHeight(), buffer, width );
				display.update( new Frame( globalCoords[ 0 ], globalCoords[ 1 ], JetpImageUtil.dither( icon.getWidth(), subImage ), icon.getWidth() ) );
			}
		}
	}
	
	/**
	 * Update the buffer entirely without sending an update
	 */
	public void updateBuffer() {
		buffer = canvas.clone();
		
		// Just update every icon
		for ( RadioIcon icon : iconOrdering ) {
			int[] iconDisplay = icon.getDisplay();
			int[] coords = icons.get( icon );
			
			int px = width;
			int py = buffer.length / width;
			
			int[] widthData = JetpImageUtil.getSubsegment( 0, px, coords[ 0 ], icon.getWidth() );
			int[] heightData = JetpImageUtil.getSubsegment( 0, py, coords[ 1 ], icon.getHeight() );
			
			int widthStart = widthData[ 0 ];
			int widthEnd = widthData[ 1 ];
			int widthLength = widthEnd - widthStart;
			
			int heightStart = heightData[ 0 ];
			int heightEnd = heightData[ 1 ];
			int heightLength = heightEnd - heightStart;
			
			for ( int offY = 0; offY < heightLength; offY++ ) {
				int bufferYValue = ( heightStart + offY ) * width + widthStart;
				int iconYValue = ( heightStart - coords[ 1 ] + offY ) * icon.getWidth();
				for ( int offX = 0; offX < widthLength; offX++ ) {
					int bufferIndex = offX + bufferYValue;

					buffer[ bufferIndex ] = JetpImageUtil.overwriteColor( buffer[ bufferIndex ], iconDisplay[ widthStart - coords[ 0 ] + offX + iconYValue ] );
				}
			}
		}
	}
	
	/**
	 * Unload a given RadioIcon and remove it completely
	 * 
	 * @param icon
	 */
	public void unloadIcon( RadioIcon icon ) {
		if ( icons.containsKey( icon ) ) {
			int[] coords = icons.remove( icon );
			iconOrdering.remove( icon );

			updateBuffer();
			
			if ( display != null ) {
				int[] subImage = JetpImageUtil.getSubImage( coords[ 0 ], coords[ 1 ], icon.getWidth(), icon.getHeight(), buffer, width );
				display.update( new Frame( coords[ 0 ], coords[ 1 ], JetpImageUtil.dither( icon.getWidth(), subImage ), icon.getWidth() ) );
			}
			icon.terminate();
		}
	}

	public List< RadioIcon > getIcons() {
		return iconOrdering;
	}
	
	@Override
	public void provideFor( MapDisplay display ) {
		this.display = display;
		display.update( getSource() );
	}

	@Override
	public void stopProviding() {
		// Prevent any resource leaks
		for ( RadioIcon icon : iconOrdering ) {
			icon.terminate();
		}
		this.display = null;
	}
}
