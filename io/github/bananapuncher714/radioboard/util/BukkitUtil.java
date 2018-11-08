package io.github.bananapuncher714.radioboard.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;

public final class BukkitUtil {
	private BukkitUtil() {
	}
	
	/**
	 * Gets an item frame at a given location; ignores orientation
	 * 
	 * @param location
	 * @return
	 */
	public static ItemFrame getItemFrameAt( Location location ) {
		location = location.getBlock().getLocation();
		for ( Entity entity : location.getChunk().getEntities() ) {
			if ( entity instanceof ItemFrame && entity.getLocation().getBlock().getLocation().equals( location ) ) {
				return ( ItemFrame ) entity;
			}
		}
		return null;
	}
	
	/**
	 * Get a location from a string
	 * 
	 * @param string
	 * Follows the format "<world>%<x>%<y>%<z>%<yaw>%<pitch>" with all periods replaced with tildes(~)
	 * @return
	 */
	public static Location getLocation( String string ) {
		String[] ll = string.replace( "~", "." ).split( "%" );
		return new Location( Bukkit.getWorld( ll[ 0 ] ), Double.parseDouble( ll[ 1 ] ), Double.parseDouble( ll[ 2 ] ), Double.parseDouble( ll[ 3 ] ), Float.parseFloat( ll[ 4 ] ), Float.parseFloat( ll[ 5 ] ) );
	}
	
	/**
	 * Get a string from a location; follows the format "<world>%<x>%<y>%<z>%<yaw>%<pitch>" with all periods replaced with tildes(~)
	 * 
	 * @param location
	 * @return
	 */
	public static String getString( Location location ) {
		return ( location.getWorld().getName() + "%" + location.getX() + "%" + location.getY() + "%" + location.getZ() + "%" + location.getYaw() + "%" + location.getPitch() ).replace( ".", "~" );
	}
}
