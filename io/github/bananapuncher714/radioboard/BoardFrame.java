package io.github.bananapuncher714.radioboard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

/**
 * A Minecraft-world wall of maps for virtual displays
 * 
 * @author BananaPuncher714
 */
public class BoardFrame {
	protected List< UUID > frames = new ArrayList< UUID >();
	protected int width;
	protected int height;
	protected BlockFace face;
	protected Location topLeft;
	protected int id;
	
	/**
	 * Given an item frame and a map id, construct a new board
	 * 
	 * @param frame
	 * Any frame on a wall with the same material behind it
	 * @param id
	 * The id of the top left map
	 */
	public BoardFrame( ItemFrame frame, int id ) {
		this.id = id;
		width = 0;
		height = 0;
		face = frame.getAttachedFace();
		Material type = frame.getLocation().getBlock().getRelative( face ).getType();
		
		int x = - face.getModZ();
		int z = face.getModX();
		
		Location frameLoc = frame.getLocation().getBlock().getLocation();

		topLeft = frameLoc.clone();
		
		int y = frameLoc.getBlockY();
		
		while ( frameLoc.getBlock().getType() == Material.AIR && frameLoc.getBlock().getRelative( face ).getType() == type ) {
			while ( frameLoc.getBlock().getType() == Material.AIR && frameLoc.getBlock().getRelative( face ).getType() == type ) {
				ItemFrame fr = getNearestOrSpawnFrameAt( frameLoc );
				fr.setFacingDirection( frame.getFacing() );
				frames.add( fr.getUniqueId() );
				ItemStack mapItem = new ItemStack( Material.MAP );
				
				// Register this map id
				RadioBoard.getInstance().getPacketHandler().registerMap( id );
				
				mapItem.setDurability( ( short ) id++ );
				fr.setItem( mapItem );
				
				frameLoc.add( x, 0, z );
			}
			frameLoc.setX( frame.getLocation().getBlockX() );
			frameLoc.setY( --y );
			frameLoc.setZ( frame.getLocation().getBlockZ() );
			height++;
		}
		width = frames.size() / height;
	}
	
	protected BoardFrame( Location top, BlockFace face, int id, int width, int height ) {
		this.topLeft = top.clone();
		this.face = face;
		this.id = id;
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Gets the inverse face of the board
	 * 
	 * @return
	 */
	public BlockFace getFace() {
		return face;
	}
	
	/**
	 * Gets the location of the top left item frame
	 * 
	 * @return
	 */
	public Location getTopLeftCorner() {
		return topLeft.clone();
	}
	
	/**
	 * Gets the map id of the top left frame
	 * 
	 * @return
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Gets the width in map lengths of this board
	 * 
	 * @return
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Gets the height in map lengths of this board
	 * @return
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Gets a list of all the item frames in this board
	 * 
	 * @return
	 */
	public List< UUID > getFrames() {
		return frames;
	}
	
	/**
	 * Get an item frame at a given location
	 * 
	 * @param location
	 * The location should be part of the board
	 * @return
	 * A frame that belongs to this board, or null
	 */
	public ItemFrame getItemFrameAt( Location location ) {
		location = location.getBlock().getLocation();
		if ( !isPartOfBoard( location ) ) {
			return null;
		}
		for ( UUID uuid : frames ) {
			Entity entity = Bukkit.getEntity( uuid );
			if ( entity == null || !entity.isValid() ) {
				continue;
			}
			
			ItemFrame frame = ( ItemFrame ) entity;
			if ( frame.getLocation().getBlock().getLocation().equals( location ) && frame.getAttachedFace() == face ) {
				return frame;
			}
		}
		return null;
	}
	
	/**
	 * Tests whether a location is within the rectangle of the board
	 * 
	 * @param location
	 * Location to test
	 * @return
	 * If it falls within the edges of the board
	 */
	public boolean isPartOfBoard( Location location ) {
		if ( location.getWorld() != topLeft.getWorld() ) {
			return false;
		}
		// Get the X and Z modifiers if we rotate the face of the board 90 degrees counter-clockwise
		int x = - face.getModZ();
		int z = face.getModX();
		// Get the location of the opposite corner
		Location bottomMost = new Location( location.getWorld(), topLeft.getBlockX() + x * width - 1, topLeft.getBlockY() - ( height - 1 ), topLeft.getBlockZ() + z * width - 1 );
		if ( location.getBlockX() > Math.max( bottomMost.getBlockX(), topLeft.getBlockX() ) || location.getBlockX() < Math.min( bottomMost.getBlockX(), topLeft.getBlockX() ) ) {
			return false;
		}
		if ( location.getBlockY() > Math.max( bottomMost.getBlockY(), topLeft.getBlockY() ) || location.getBlockY() < Math.min( bottomMost.getBlockY(), topLeft.getBlockY() ) ) {
			return false;
		}
		if ( location.getBlockZ() > Math.max( bottomMost.getBlockZ(), topLeft.getBlockZ() ) || location.getBlockZ() < Math.min( bottomMost.getBlockZ(), topLeft.getBlockZ() ) ) {
			return false;
		}
		return true;
	}
	
	/**
	 * Remove all the item frames of this board
	 */
	public void terminate() {
		for ( UUID uuid : frames ) {
			Entity entity = Bukkit.getEntity( uuid );
			if ( entity != null ) {
				entity.remove();
			}
		}
	}
	
	/**
	 * Get the item frame at a location, or spawn one if it doesn't exist
	 * 
	 * @param location
	 * The location of an item frame
	 * @return
	 * The existing or newly spawned in item frame
	 */
	private static ItemFrame getNearestOrSpawnFrameAt( Location location ) {
		for ( Entity entity : location.getChunk().getEntities() ) {
			if ( entity.getLocation().getBlock().getLocation().equals( location ) && entity instanceof ItemFrame ) {
				return ( ItemFrame ) entity;
			}
		}
		return location.getWorld().spawn( location, ItemFrame.class );
	}
}
