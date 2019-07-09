package io.github.bananapuncher714.radioboard.providers.canvas;

import org.bukkit.entity.Entity;

import io.github.bananapuncher714.radioboard.BoardFrame;
import io.github.bananapuncher714.radioboard.api.DisplayInteract;

/**
 * Displays a simple image
 * 
 * @author BananaPuncher714
 */
public class RadioIconImage implements RadioIcon {
	protected int[] image;
	protected int width;
	
	protected RadioCanvas provider;
	
	public RadioIconImage( int[] image, int width ) {
		this.image = image;
		this.width = width;
	}

	@Override
	public int[] getDisplay() {
		return image;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return image.length / width;
	}

	@Override
	public void init( RadioCanvas provider ) {
		this.provider = provider;
	}

	@Override
	public void onClick( BoardFrame frame, Entity entity, DisplayInteract action, int x, int y ) {
	}

	@Override
	public void terminate() {
		provider = null;
	}
}
