package io.github.bananapuncher714.radioboard.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Entity;

import io.github.bananapuncher714.radioboard.RadioObserver;

/**
 * Simple class for virtual displays
 * 
 * @author BananaPuncher714
 */
public abstract class MapDisplay {
	protected final String id;
	protected int startId;
	protected int mapWidth;
	protected int mapHeight;
	
	protected MapDisplayProvider source;
	protected Map< RadioObserver, Object > observers = new ConcurrentHashMap< RadioObserver, Object >();
	
	public MapDisplay( String id, int startId, int mapWidth, int mapHeight ) {
		this.id = id;
		this.startId = startId;
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
	}
	
	/**
	 * Simple any case string identifier
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Top left map id
	 * 
	 * @return
	 */
	public int getMapId() {
		return startId;
	}
	
	/**
	 * Width of this display in map lengths
	 * 
	 * @return
	 */
	public int getMapWidth() {
		return mapWidth;
	}
	
	/**
	 * Height of this display in map lengths
	 * 
	 * @return
	 */
	public int getMapHeight() {
		return mapHeight;
	}
	
	/**
	 * Get the {@link MapDisplayProvider}
	 * 
	 * @return
	 */
	public MapDisplayProvider getSource() {
		return source;
	}
	
	/**
	 * Set the {@link MapDisplayProvider}
	 * @param provider
	 */
	public void setSource( MapDisplayProvider provider ) {
		if ( source != null ) {
			source.stopProviding();
		}
		this.source = provider;
		source.provideFor( this );
	}
	
	/**
	 * Kill this MapDisplay
	 */
	public void terminate() {
		if ( source != null ) {
			source.stopProviding();
		}
		source = null; 
	}
	
	/**
	 * Add an observer
	 * 
	 * @param obs
	 */
	public void addObserver( RadioObserver... obs ) {
		for ( RadioObserver observer : obs ) {
			observers.put( observer, false );
		}
		update( obs );
	}
	
	/**
	 * Remove an observer, if they are observing
	 * 
	 * @param obs
	 */
	public void removeObserver( RadioObserver... obs ) {
		for ( RadioObserver observer : obs ) {
			observers.remove( observer );
		}
	}
	
	/**
	 * Check if an observer is observing this map; does not necessarily have to be online
	 * 
	 * @param observer
	 * An observer
	 * @return
	 */
	public boolean isObserving( RadioObserver observer ) {
		return observers.containsKey( observer );
	}
	
	/**
	 * Remove all observers
	 */
	public void removeObservers() {
		observers.clear();
	}
	
	/**
	 * On interact, either a player or a projectile
	 * 
	 * @param entity
	 * The entity that interacts, should only be a projectile if action is {@value DisplayInteract#PROJECTILE}
	 * @param action
	 * The action that occurs
	 * @param id
	 * The id of the map pressed
	 * @param x
	 * The X relative to the map
	 * @param y
	 * The Y relative to the map
	 */
	public void onClick( Entity entity, DisplayInteract action, int id, int x, int y ) {
		id -= startId;
		int height = id / mapWidth;
		int width = id % mapWidth;
		// Call the interact at the X and Y starting from the top left corner
		source.interactAt( entity, action, ( width << 7 ) + x, ( height << 7 ) + y );
	}
	
	/**
	 * Clear this map's buffer completely
	 */
	public abstract void clear();
	
	/**
	 * Update either the {@link MapDisplayProvider}'s current frame or the buffer
	 */
	public abstract void update();
	
	/**
	 * Update for the specified observers only; they do NOT have to be observing
	 * @param observers
	 * An array of observers
	 */
	public abstract void update( RadioObserver... observers );
	
	/**
	 * Update the display with a given frame
	 * 
	 * @param frame
	 * An arbitary frame
	 */
	public abstract void update( Frame frame );
}
