package io.github.bananapuncher714.radioboard.api;

/**
 * Simple frame representing a rectangle of color fit for a wall of Minecraft maps
 * 
 * @author BananaPuncher714
 */
public class Frame {
	/**
	 * The time of creation
	 */
	public final long timestamp = System.currentTimeMillis();
	
	// Simple final variables
	public final int x;
	public final int y;
	public final int width;
	protected boolean center = false;
	protected final byte[] display;
	
	/**
	 * Create a new non-centered frame
	 * 
	 * @param x
	 * The top left X of the given frame, can be any number
	 * @param y
	 * The top left Y of the given frame, can be any number
	 * @param map
	 * The map data, in MinecraftColor bytes
	 * @param width
	 * The width of this frame
	 */
	public Frame( int x, int y, byte[] map, int width ) {
		this.x = x;
		this.y = y;
		this.display = map;
		this.width = width;
	}
	
	/**
	 * Creates a new centered frame
	 * 
	 * @param map
	 * The map data, in MinecraftColor bytes
	 * @param width
	 * The width of this frame
	 */
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
	
	/**
	 * Whether or not this frame should be centered
	 * @return
	 */
	public boolean center() {
		return center;
	}
	
	/**
	 * Set the centered option
	 * 
	 * @param center
	 * @return
	 * The frame itself
	 */
	public Frame setCenter( boolean center ) {
		this.center = center;
		return this;
	}
}
