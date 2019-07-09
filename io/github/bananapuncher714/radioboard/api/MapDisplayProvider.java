package io.github.bananapuncher714.radioboard.api;

import org.bukkit.entity.Entity;

import io.github.bananapuncher714.radioboard.BoardFrame;
/**
 * Interface for content that provides {@link Frame} to {@link MapDisplay}
 * 
 * @author BananaPuncher714
 */
public interface MapDisplayProvider {
	/**
	 * Get a {@link Frame} from this provider at any time
	 * 
	 * @return
	 * null if there are no new updates
	 */
	Frame getSource();
	
	/**
	 * Interaction with an entity on the board
	 * 
	 * @param frame
	 * The frame interacted with, or null if virtual interaction
	 * @param entity
	 * Either a player or a projectile
	 * @param action
	 * Will only be {@link DisplayInteract#PROJECTILE} if entity is a projectile
	 * @param x
	 * The X relative to the top left corner
	 * @param y
	 * The Y relative to the top left corner
	 */
	void interactAt( BoardFrame frame, Entity entity, DisplayInteract action, int x, int y );
	
	/**
	 * Start providing frames for the given display
	 * 
	 * @param display
	 */
	void provideFor( MapDisplay display );
	
	/**
	 * Terminate this MapDisplayProvider and stop providing frames for any display
	 */
	void stopProviding();
}
