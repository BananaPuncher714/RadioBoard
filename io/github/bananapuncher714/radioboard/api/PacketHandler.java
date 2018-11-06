package io.github.bananapuncher714.radioboard.api;

import java.util.UUID;

import org.bukkit.entity.Player;

/**
 * Interface for basic NMS functions
 * 
 * @author BananaPuncher714
 */
public interface PacketHandler {
	/**
	 * Displays an image to all viewers with an offset
	 * 
	 * @param viewers
	 * The UUID of viewers
	 * @param map
	 * The first map id; Increments afterwards
	 * @param mapWidth
	 * The width in amount of maps
	 * @param mapHeight
	 * The height in amount of maps
	 * @param rgb
	 * The image in terms of Bukkit map colors
	 * @param videoWidth
	 * The width in pixels of the image
	 * @param xOffset
	 * The amount of pixels from the left the image should be displayed
	 * @param yOffset
	 * The amount of pixels from the top the image should be displayed
	 */
	void display( UUID[] viewers, int map, int mapWidth, int mapHeight, byte[] rgb, int videoWidth, int xOffset, int yOffset );
	
	/**
	 * Displays a centered image to all viewers
	 * 
	 * @param viewers
	 * The UUID of viewers
	 * @param map
	 * The first map id; Increments afterwards
	 * @param mapWidth
	 * The width in amount of maps
	 * @param mapHeight
	 * The height in amount of maps
	 * @param rgb
	 * The image in terms of Bukkit map colors
	 * @param videoWidth
	 * The width in pixels of the image
	 */
	void display( UUID[] viewers, int map, int mapWidth, int mapHeight, byte[] rgb, int videoWidth );
	Object onPacketInterceptOut( Player viewer, Object packet );
	Object onPacketInterceptIn( Player viewer, Object packet );
	void registerMap( int id );
	boolean isMapRegistered( int id );
	void unregisterMap( int id );
	void registerPlayer( Player player );
	void unregisterPlayer( UUID uuid );
}
