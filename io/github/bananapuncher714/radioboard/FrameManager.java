package io.github.bananapuncher714.radioboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import io.github.bananapuncher714.radioboard.api.MapDisplay;
import io.github.bananapuncher714.radioboard.util.BukkitUtil;

public enum FrameManager {
	INSTANCE;
	
	protected Map< String, BoardFrame > boards = new HashMap< String, BoardFrame >();
	protected Map< String, MapDisplay > displays = new HashMap< String, MapDisplay >();
		
	public void registerBoard( String id, BoardFrame frame ) {
		if ( boards.containsKey( id ) ) {
			boards.remove( id ).terminate();
		}
		boards.put( id, frame );
	}
	
	public BoardFrame getFrame( String id ) {
		return boards.containsKey( id ) ? boards.get( id ) : null;
	}
	
	public BoardFrame removeFrame( String id ) {
		return boards.containsKey( id ) ? boards.remove( id ) : null;
	}
	
	public Collection< BoardFrame > getBoardFrames() {
		return boards.values();
	}
	
	public void registerDisplay( MapDisplay display ) {
		if ( displays.containsKey( display.getId() ) ) {
			displays.remove( display.getId() ).terminate();
		}
		displays.put( display.getId(), display );
	}
	
	public MapDisplay getDisplay( String id ) {
		return displays.containsKey( id ) ? displays.get( id ) : null;
	}

	public MapDisplay removeDisplay( String id ) {
		return displays.containsKey( id ) ? displays.remove( id ) : null;
	}
	
	public Collection< MapDisplay > getDisplays() {
		return displays.values();
	}
	
	public void terminate() {
		for ( MapDisplay display : displays.values() ) {
			display.terminate();
		}
		displays.clear();
	}
	
	/**
	 * Fetch a given BoardFrame at a given coordinate
	 * @param location
	 * The location of a possible board
	 * @return
	 * The first board that exists at the given coordinate, or null if none exists
	 */
	public BoardFrame getFrameAt( Location location ) {
		for ( BoardFrame frame : boards.values() ) {
			if ( frame.isPartOfBoard( location ) ) {
				return frame;
			}
		}
		return null;
	}
	
	protected void saveBoards( ConfigurationSection section ) {
		for ( String id : boards.keySet() ) {
			BoardFrame frame = boards.get( id );
			section.set( id + ".face", frame.getFace().name() );
			section.set( id + ".id", frame.getId() );
			section.set( id + ".width", frame.getWidth() );
			section.set( id + ".height", frame.getHeight() );
			section.set( id + ".top-left", BukkitUtil.getString( frame.getTopLeftCorner() ) );
			List< String > ids = new ArrayList< String >();
			for ( UUID uuid : frame.getFrames() ) {
				ids.add( uuid.toString() );
			}
			section.set( id + ".frames", ids );
		}
	}
	
	protected void loadBoards( ConfigurationSection section ) {
		for ( String key : section.getKeys( false ) ) {
			BlockFace face = Enum.valueOf( BlockFace.class, section.getString( key + ".face" ) );
			int id = section.getInt( key + ".id" );
			int width = section.getInt( key + ".width" );
			int height = section.getInt( key + ".height" );
			Location topLeft = BukkitUtil.getLocation( section.getString( key + ".top-left" ) );

			BoardFrame frame = new BoardFrame( topLeft, face, id, width, height );
			for ( String uuidStr : section.getStringList( key + ".frames" ) ) {
				frame.getFrames().add( UUID.fromString( uuidStr ) );
			}
			
			int amount = ( width * height ) + id;
			for ( int i = id; i < amount; i++ ) {
				RadioBoard.getInstance().getPacketHandler().registerMap( i );
			}
			
			registerBoard( key, frame );
		}
	}
}
