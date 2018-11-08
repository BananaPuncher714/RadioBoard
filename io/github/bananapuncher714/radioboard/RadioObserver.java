package io.github.bananapuncher714.radioboard;

import java.util.UUID;

/**
 * A player's UUID for async operations
 * 
 * @author BananaPuncher714
 */
public class RadioObserver {
	protected final UUID uuid;
	
	public RadioObserver( UUID uuid ) {
		this.uuid = uuid;
	}
	
	public UUID getUUID() {
		return uuid;
	}

	@Override
	public int hashCode() {
		return uuid.hashCode();
	}

	@Override
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		RadioObserver other = ( RadioObserver ) obj;
		if ( uuid == null ) {
			if ( other.uuid != null )
				return false;
		} else if ( !uuid.equals( other.uuid ) )
			return false;
		return true;
	}
	
	
}
