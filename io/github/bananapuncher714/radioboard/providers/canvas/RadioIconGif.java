package io.github.bananapuncher714.radioboard.providers.canvas;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.radioboard.api.DisplayInteract;
import io.github.bananapuncher714.radioboard.util.GifDecoder;
import io.github.bananapuncher714.radioboard.util.GifDecoder.GifImage;
import io.github.bananapuncher714.radioboard.util.JetpImageUtil;

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
		long lastUpdated = 0;
		while ( RUNNING ) {
			if ( System.currentTimeMillis() - lastUpdated > delays[ index ] ) {
				if ( canvas != null ) {
					canvas.update( this );
				}
				index = ( index + 1 ) % indexes.length;
				lastUpdated = System.currentTimeMillis();
			}
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
	public void onClick(Player player, DisplayInteract action, int x, int y) {
	}

	@Override
	public void terminate() {
		RUNNING = false;
		canvas = null;
	}

}
