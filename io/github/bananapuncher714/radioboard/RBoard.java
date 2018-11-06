package io.github.bananapuncher714.radioboard;

import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import io.github.bananapuncher714.radioboard.api.Frame;
import io.github.bananapuncher714.radioboard.api.MapDisplay;
import io.github.bananapuncher714.radioboard.api.PacketHandler;

/**
 * A Minecraft implementation of a canvas of maps; keeps a cache of the display, and a set
 * of sources.
 * 
 * @author BananaPuncher714
 */
public class RBoard extends MapDisplay {
	protected UUID[] observerCache;
	protected ReentrantLock cacheLock = new ReentrantLock();
	protected byte[] display;
	
	protected final PacketHandler handler;

	/**
	 * Create a new RBoard
	 * 
	 * @param id
	 * This RBoard's ID
	 * @param startId
	 * The first map id
	 * @param mapWidth
	 * Amount of maps wide
	 * @param mapHeight
	 * Amount of maps high
	 */
	public RBoard( String id, int startId, int mapWidth, int mapHeight ) {
		super( id, startId, mapWidth, mapHeight );

		display = new byte[ ( mapWidth << 7 ) * ( mapHeight << 7 ) ];
		
		handler = RadioBoard.getInstance().getPacketHandler();
		
		clear();
	}

	@Override
	public void update() {
		Frame frame = source.getSource();
		if ( frame == null ) {
			cacheLock.lock();
			handler.display( observerCache, startId, mapWidth, mapHeight, display, mapWidth << 7, 0, 0 );
			cacheLock.unlock();
		} else {
			update( frame );
		}
	}

	@Override
	public void update( RadioObserver... observers ) {
		UUID[] cache = new UUID[ observers.length ];
		for ( int index = 0; index < observers.length; index++ ) {
			cache[ index ] = observers[ index ].getUUID();
		}
		
		handler.display( cache, startId, mapWidth, mapHeight, display, mapWidth << 7, 0, 0 );
	}
	
	@Override
	public void update( Frame frame ) {
		saveToDisplay( frame );
		cacheLock.lock();
		if ( frame.center() ) {
			handler.display( observerCache, startId, mapWidth, mapHeight, frame.getDisplay(), frame.width );
		} else {
			handler.display( observerCache, startId, mapWidth, mapHeight, frame.getDisplay(), frame.width, frame.x, frame.y );
		}
		cacheLock.unlock();
	}
	
	@Override
	public void clear() {
		display = new byte[ display.length ];
		handler.display( observerCache, startId, mapWidth, mapHeight, display, mapWidth << 7, 0, 0 );
	}
	
	private void saveToDisplay( Frame frame ) {
		// Save this to the internal buffer
		int px = mapWidth << 7;
		int py = mapHeight << 7;
		byte[] frameDisplay = frame.getDisplay();
		int frameHeight = frameDisplay.length / frame.width;
		
		int topX = Math.max( 0, frame.x );
		int topY = Math.max( 0, frame.y );
		
		int width = Math.min( px - frame.x, frame.width );
		int height = Math.min( py - frame.y, frameHeight );
		
		for ( int x = 0; x < width; x++ ) {
			for ( int y = 0; y < height; y++ ) {
				display[ x + topX + ( y + topY ) * px ] = frameDisplay[ x + y * frame.width ];
			}
		}
	}

	@Override
	public void addObserver( RadioObserver... obs ) {
		super.addObserver( obs );
		rebuildObserverCache();
	}

	@Override
	public void removeObserver( RadioObserver... obs ) {
		super.removeObserver( obs );
		rebuildObserverCache();
	}

	// In case the size of observers is different or has changed
	private void rebuildObserverCache() {
		cacheLock.lock();
		observerCache = new UUID[ observers.size() ];
		int i = 0;
		for ( RadioObserver observer : observers.keySet() ) {
			observerCache[ i++ ] = observer.getUUID();
		}
		cacheLock.unlock();
	}
}
