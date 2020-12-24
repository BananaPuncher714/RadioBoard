package io.github.bananapuncher714.radioboard.providers.canvas;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;

import org.bukkit.entity.Entity;

import io.github.bananapuncher714.radioboard.BoardFrame;
import io.github.bananapuncher714.radioboard.api.DisplayInteract;
import io.github.bananapuncher714.radioboard.util.GifDecoder;
import io.github.bananapuncher714.radioboard.util.GifDecoder.GifImage;
import io.github.bananapuncher714.radioboard.util.JetpImageUtil;

/**
 * Plays a gif
 * 
 * @author BananaPuncher714
 */
public class RadioIconGif extends Thread implements RadioIcon {
	private volatile boolean RUNNING = true;
	
	protected int[][] indexes;
	protected int[] delays;
	protected int width;
	protected int height;
	
	private int index = 0;
	
	protected RadioCanvas canvas;
	
	public RadioIconGif( File gif, int width, int height ) {
		try {
			GifImage image = GifDecoder.read( new FileInputStream( gif ) );
			this.width = width;
			this.height = height;
			
			int noi = image.getFrameCount();
			indexes = new int[ noi ][];
			delays = new int[ noi ];
			for ( int i = 0; i < noi; i++ ) {
				delays[ i ] = image.getDelay( i ) * 10;
				indexes[ i ] = JetpImageUtil.getRGBArray( JetpImageUtil.toBufferedImage( image.getFrame( i ).getScaledInstance( width, height, Image.SCALE_SMOOTH ) ) );
		    }
			
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		long lastUpdated = System.currentTimeMillis();
		while ( RUNNING ) {
			if ( canvas != null ) {
				canvas.update( this );
			}
			try {
				// Make up time lost for sending an update to the client
				Thread.sleep( Math.max( 0, delays[ index ] - ( System.currentTimeMillis() - lastUpdated ) ) );
			} catch ( InterruptedException e ) {
				e.printStackTrace();
			}
			lastUpdated = System.currentTimeMillis();
			index = ( index + 1 ) % indexes.length;
		}
		canvas = null;
	}
	
	@Override
	public int[] getDisplay() {
		return indexes[ index ];
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void init( RadioCanvas provider ) {
		canvas = provider;
		this.start();
	}

	@Override
	public void onClick( BoardFrame frame, Entity entity, DisplayInteract action, int x, int y) {
	}

	@Override
	public void terminate() {
		RUNNING = false;
		canvas = null;
	}
}
