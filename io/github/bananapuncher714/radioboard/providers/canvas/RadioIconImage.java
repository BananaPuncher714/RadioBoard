package io.github.bananapuncher714.radioboard.providers.canvas;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.radioboard.api.DisplayInteract;

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
	public void onClick( Player player, DisplayInteract action, int x, int y ) {
	}

	@Override
	public void terminate() {
		provider = null;
	}
}
