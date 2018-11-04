package io.github.bananapuncher714.radioboard.providers;

import org.bukkit.entity.Player;

import io.github.bananapuncher714.radioboard.api.DisplayInteract;
import io.github.bananapuncher714.radioboard.api.Frame;
import io.github.bananapuncher714.radioboard.api.MapDisplay;
import io.github.bananapuncher714.radioboard.api.MapDisplayProvider;

public class SlideShowProvider implements MapDisplayProvider {

	@Override
	public Frame getSource() {
		return null;
	}

	@Override
	public void interactAt( Player player, DisplayInteract action, int x, int y ) {

	}

	@Override
	public void provideFor( MapDisplay display ) {

	}

	@Override
	public void stopProviding() {

	}

}
