package io.github.bananapuncher714.radioboard.api;

/**
 * Player interactions with in-game displays
 * 
 * @author BananaPuncher714
 */
public enum DisplayInteract {
	/**
	 * Occurs when a player left clicks a BoardFrame
	 */
	LEFT_CLICK,
	/**
	 * Occurs when a player right clicks a BoardFrame
	 */
	RIGHT_CLICK,
	/**
	 * Occurs when a player is looking at a BoardFrame
	 */
	LOOK,
	/**
	 * Occurs when a projectile hits a BoardFrame
	 */
	PROJECTILE;
}
