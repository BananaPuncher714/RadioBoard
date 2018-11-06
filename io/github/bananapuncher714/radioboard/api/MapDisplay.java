package io.github.bananapuncher714.radioboard.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import io.github.bananapuncher714.radioboard.RadioObserver;

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
	
	public String getId() {
		return id;
	}
	
	public int getMapId() {
		return startId;
	}
	
	public int getMapWidth() {
		return mapWidth;
	}
	
	public int getMapHeight() {
		return mapHeight;
	}
	
	public MapDisplayProvider getSource() {
		return source;
	}
	
	public void setSource( MapDisplayProvider provider ) {
		if ( source != null ) {
			source.stopProviding();
		}
		this.source = provider;
		source.provideFor( this );
	}
	
	public void terminate() {
		if ( source != null ) {
			source.stopProviding();
		}
		source = null; 
	}
	
	public void addObserver( RadioObserver... obs ) {
		for ( RadioObserver observer : obs ) {
			observers.put( observer, false );
		}
		update( obs );
	}
	
	public void removeObserver( RadioObserver... obs ) {
		for ( RadioObserver observer : obs ) {
			observers.remove( observer );
		}
	}
	
	public boolean isObserving( RadioObserver observer ) {
		return observers.containsKey( observer );
	}
	
	public void removeObservers() {
		observers.clear();
	}
	
	public void onClick( Entity entity, DisplayInteract action, int id, int x, int y ) {
		id -= startId;
		int height = id / mapWidth;
		int width = id % mapWidth;
		// Call the interact at the X and Y starting from the top left corner
		source.interactAt( entity, action, ( width << 7 ) + x, ( height << 7 ) + y );
	}
	
	public abstract void clear();
	public abstract void update();
	public abstract void update( RadioObserver... observers );
	public abstract void update( Frame frame );
}
