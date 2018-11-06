package io.github.bananapuncher714.radioboard.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class VectorUtil {
	/**
	 * Calculate the exact location at which a vector intersects a given plane
	 * 
	 * @param planeLoc
	 * A point on the plane
	 * @param plane
	 * A vector normal to the plane
	 * @param origin
	 * The origin of the ray
	 * @param direction
	 * A normal direction of the ray
	 * @return
	 * null if it never intersects
	 */
	public static Location calculateVector( Location planeLoc, Vector plane, Location origin, Vector direction ) {
		if ( plane.dot( direction ) == 0 ) {
			return null;
		}
		
		double distance = ( plane.dot( planeLoc.toVector() ) - plane.dot( origin.toVector() ) ) / plane.dot( direction );
		return origin.clone().add( direction.multiply( distance ) );
	}
	
	/**
	 * Round a location's values rounded to the sixteenth
	 * 
	 * @param location
	 * @return
	 */
	public static Location sixteenth( Location location ) {
		location.setX( location.getX() * 128 / 128 );
		location.setY( location.getY() * 128 / 128 );
		location.setZ( location.getZ() * 128 / 128 );
		return location;
	}
}
