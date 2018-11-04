package io.github.bananapuncher714.radioboard.providers;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import io.github.bananapuncher714.radioboard.api.DisplayInteract;
import io.github.bananapuncher714.radioboard.api.Frame;
import io.github.bananapuncher714.radioboard.api.MapDisplay;
import io.github.bananapuncher714.radioboard.api.MapDisplayProvider;
import io.github.bananapuncher714.radioboard.util.GifDecoder;
import io.github.bananapuncher714.radioboard.util.JetpImageUtil;
import io.github.bananapuncher714.radioboard.util.GifDecoder.GifImage;

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
		long lastUpdated = 0;
		while ( RUNNING ) {
			if ( System.currentTimeMillis() - lastUpdated > delays[ index ] ) {
				Frame frame = new Frame( indexes[ index ], width );
				display.update( frame );
				index = ( index + 1 ) % indexes.length;
				lastUpdated = System.currentTimeMillis();
			}
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
	public void interactAt( Player player, DisplayInteract action, int x, int y ) {
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
