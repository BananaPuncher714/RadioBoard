package io.github.bananapuncher714.radioboard.providers;

import java.awt.Image;
import java.awt.image.BufferedImage;

import org.bukkit.entity.Entity;

import io.github.bananapuncher714.radioboard.BoardFrame;
import io.github.bananapuncher714.radioboard.api.DisplayInteract;
import io.github.bananapuncher714.radioboard.api.Frame;
import io.github.bananapuncher714.radioboard.api.MapDisplay;
import io.github.bananapuncher714.radioboard.api.MapDisplayProvider;
import io.github.bananapuncher714.radioboard.util.JetpImageUtil;

/**
 * Simple image provider for {@link MapDisplay}; will show an image with the given height and width
 * Also allows simple dot drawing with left and right click.
 * 
 * @author BananaPuncher714
 */
public class ImageProvider implements MapDisplayProvider {
	protected MapDisplay display;
	
	protected BufferedImage original;
	protected int width;
	protected byte[] dithered;
	
	public ImageProvider( BufferedImage image ) {
		original = image;
	}
	
	@Override
	public Frame getSource() {
		return new Frame( dithered, width );
	}

	@Override
	public void interactAt( BoardFrame frame, Entity entity, DisplayInteract action, int x, int y ) {
		// Demonstration of interaction
		if ( action == DisplayInteract.RIGHT_CLICK ) {
			display.update( new Frame( x - 1, y - 1, new byte[] { 17, 17, 17, 17, 17, 17, 17, 17, 17 }, 3 ) );
		} else if ( action == DisplayInteract.LEFT_CLICK ) {
			display.update( new Frame( x - 1, y - 1, new byte[] { -126, -126, -126, -126, -126, -126, -126, -126, -126 }, 3 ) );
		}
	}

	@Override
	public void provideFor( MapDisplay display ) {
		this.display = display;
		
		width = display.getMapWidth() << 7;
		
		int imgWidth = original.getWidth();
		int imgHeight = original.getHeight();
		double proportion = imgHeight / ( double ) imgWidth;
		int newHeight = ( int ) ( width * proportion );
		
		dithered = JetpImageUtil.dither( original.getScaledInstance( width, newHeight, Image.SCALE_SMOOTH ) );
		
		display.update( new Frame( dithered, width ) );
	}
	
	public void setImage( BufferedImage image ) {
		original = image;
		if ( display != null ) {
			display.clear();
			
			width = display.getMapWidth() << 7;
			
			int imgWidth = original.getWidth();
			int imgHeight = original.getHeight();
			double proportion = imgHeight / ( double ) imgWidth;
			int newHeight = ( int ) ( width * proportion );
			
			dithered = JetpImageUtil.dither( original.getScaledInstance( width, newHeight, Image.SCALE_SMOOTH ) );
			
			display.update( new Frame( dithered, width ) );
		}
	}

	@Override
	public void stopProviding() {
		display = null;
	}
}
