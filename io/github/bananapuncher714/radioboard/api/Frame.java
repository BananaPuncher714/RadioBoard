package io.github.bananapuncher714.radioboard.api;

/**
 * Simple frame representing a rectangle of color fit for a wall of Minecraft maps
 * 
 * @author BananaPuncher714
 */
public class Frame {
	// The time of creation
	public final long timestamp = System.currentTimeMillis();
	
	// Simple final variables
	public final int x;
	public final int y;
	public final int width;
	protected boolean center = false;
	protected final byte[] display;
	
	public Frame( int x, int y, byte[] map, int width ) {
		this.x = x;
		this.y = y;
		this.display = map;
		this.width = width;
	}
	
	public Frame( byte[] map, int width ) {
		this( 0, 0, map, width );
		center = true;
	}
	
	/**
	 * Gives a mutable array of bytes
	 * 
	 * @return
	 * Contains bytes for Minecraft's map colors
	 */
	public byte[] getDisplay() {
		return display;
	}
	
	public boolean center() {
		return center;
	}
	
	public Frame setCenter( boolean center ) {
		this.center = center;
		return this;
	}
}
