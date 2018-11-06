package io.github.bananapuncher714.radioboard.providers.canvas;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.radioboard.api.DisplayInteract;

public interface RadioIcon {
	int[] getDisplay();
	int getWidth();
	int getHeight();
	void init( RadioCanvas provider );
	void onClick( Entity entity, DisplayInteract action, int x, int y );
	void terminate();
}
