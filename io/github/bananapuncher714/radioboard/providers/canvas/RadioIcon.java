package io.github.bananapuncher714.radioboard.providers.canvas;

import org.bukkit.entity.Entity;

import io.github.bananapuncher714.radioboard.api.DisplayInteract;

/**
 * Can be added to {@link RadioCanvas} to display images
 * 
 * @author BananaPuncher714
 */
public interface RadioIcon {
	/**
	 * Get the main representation of this icon
	 * 
	 * @return
	 * An image with size width * height in an int color array
	 */
	int[] getDisplay();
	
	/**
	 * Get the overall width of this icon
	 * 
	 * @return
	 */
	int getWidth();
	
	/**
	 * Get the overall height of this icon
	 * 
	 * @return
	 */
	int getHeight();
	
	/**
	 * Called when a RadioCanvas is ready to receive input from this icon
	 * 
	 * @param provider
	 * A RadioCanvas
	 */
	void init( RadioCanvas provider );
	
	/**
	 * Called interaction from either a player or a projectile
	 * 
	 * @param entity
	 * The entity, either a player or a projectile
	 * @param action
	 * Will always be a player unless action is {@link DisplayInteract#PROJECTILE}
	 * @param x
	 * The X coordinate of the click relative to the top left of this icon
	 * @param y
	 * The Y coordinate of the click relative to the top left of this icon
	 */
	void onClick( Entity entity, DisplayInteract action, int x, int y );
	
	/**
	 * Called when the RadioCanvas is disabling itself; Most likely end of use
	 */
	void terminate();
}
