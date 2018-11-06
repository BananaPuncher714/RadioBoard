package io.github.bananapuncher714.radioboard.providers;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

import org.bukkit.entity.Entity;

import io.github.bananapuncher714.radioboard.api.DisplayInteract;
import io.github.bananapuncher714.radioboard.api.Frame;
import io.github.bananapuncher714.radioboard.api.MapDisplay;
import io.github.bananapuncher714.radioboard.api.MapDisplayProvider;
import io.github.bananapuncher714.radioboard.util.GifDecoder;
import io.github.bananapuncher714.radioboard.util.GifDecoder.GifImage;
import io.github.bananapuncher714.radioboard.util.JetpImageUtil;

/**
 * Simple GIF player plays a GIF on a display.
 * 
 * @author BananaPuncher714
 */
public class GifPlayer extends Thread implements MapDisplayProvider {
	private volatile boolean RUNNING = true;
	
	protected BufferedImage[] images;
	protected byte[][] indexes;
	protected int[] delays;
	protected int width;
	
	protected MapDisplay display;
	
	public GifPlayer( File gif ) {
		try {
			GifImage image = GifDecoder.read( new FileInputStream( gif ) );
			
			int noi = image.getFrameCount();
			indexes = new byte[ noi ][];
			images = new BufferedImage[ noi ];
			delays = new int[ noi ];
			for ( int i = 0; i < noi; i++ ) {
				delays[ i ] = image.getDelay( i ) * 10;
				images[ i ] = image.getFrame( i );
		    }
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		int index = 0;
		long lastUpdated = System.currentTimeMillis();
		while ( RUNNING ) {
			if ( display != null ) {
				Frame frame = new Frame( indexes[ index ], width );
				display.update( frame );
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
		display = null;
	}
	
	public void 死んでる() {
		RUNNING = false;
	}

	@Override
	public Frame getSource() {
		return null;
	}

	@Override
	public void interactAt( Entity entity, DisplayInteract action, int x, int y ) {
	}

	@Override
	public void provideFor( MapDisplay display ) {
		this.display = display;
		
		width = display.getMapWidth() << 7;
		
		// Load each image to the proper size
		for ( int i = 0; i < images.length; i++ ) {
			BufferedImage image = images[ i ];
			
			int imgWidth = image.getWidth();
			int imgHeight = image.getHeight();
			double proportion = imgHeight / ( double ) imgWidth;
			int newHeight = ( int ) ( width * proportion );
			
			indexes[ i ] = JetpImageUtil.dither( image.getScaledInstance( width, newHeight, Image.SCALE_SMOOTH ) );
		}

		RUNNING = true;
		this.start();
	}

	@Override
	public void stopProviding() {
		死んでる();
	}
}
