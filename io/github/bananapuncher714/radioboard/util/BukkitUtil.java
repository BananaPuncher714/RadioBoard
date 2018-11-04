package io.github.bananapuncher714.radioboard.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;

public final class BukkitUtil {
	public static ItemFrame getItemFrameAt( Location location ) {
		location = location.getBlock().getLocation();
		for ( Entity entity : location.getChunk().getEntities() ) {
			if ( entity instanceof ItemFrame && entity.getLocation().getBlock().getLocation().equals( location ) ) {
				return ( ItemFrame ) entity;
			}
		}
		return null;
	}
	
	public static Location getLocation( String string ) {
		String[] ll = string.replace( "~", "." ).split( "%" );
		return new Location( Bukkit.getWorld( ll[ 0 ] ), Double.parseDouble( ll[ 1 ] ), Double.parseDouble( ll[ 2 ] ), Double.parseDouble( ll[ 3 ] ), Float.parseFloat( ll[ 4 ] ), Float.parseFloat( ll[ 5 ] ) );
	}
	
	public static String getString( Location location ) {
		return ( location.getWorld().getName() + "%" + location.getX() + "%" + location.getY() + "%" + location.getZ() + "%" + location.getYaw() + "%" + location.getPitch() ).replace( ".", "~" );
	}
}
