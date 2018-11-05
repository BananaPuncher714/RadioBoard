package io.github.bananapuncher714.radioboard.providers.canvas;

import org.bukkit.entity.Player;
import org.bukkit.util.noise.NoiseGenerator;
import org.bukkit.util.noise.SimplexNoiseGenerator;

import io.github.bananapuncher714.radioboard.api.DisplayInteract;

public class RadioIconCloud extends Thread implements RadioIcon {
	private volatile boolean RUNNING = true;
	
	protected int[] noise;
	protected int width;
	protected int height;
	protected int delay = 125;
	protected NoiseGenerator generator = new SimplexNoiseGenerator( 0 );

	protected RadioCanvas canvas;
	
	protected double xIncrease = .05;
	protected double yIncrease = .03;
	
	protected double x = 0;
	protected double y = 0;
	
	protected int transparency;
	
	public RadioIconCloud( int width, int height, int transparency, int delay ) {
		this.width = width;
		this.height = height;
		this.transparency = transparency;
		this.delay = delay;
		noise = new int[ width * height ];
	}
	
	@Override
	public void run() {
		while ( RUNNING ) {
			if ( canvas != null ) {
				regenNoise();
				canvas.update( this );
				x += xIncrease;
				y += yIncrease;
			}
			try {
				Thread.sleep( delay );
			} catch ( InterruptedException e ) {
				e.printStackTrace();
			}
		}
		canvas = null;
	}
	
	private void regenNoise() {
		for ( int x = 0; x < width; x++ ) {
			for ( int y = 0; y < height; y++ ) {
				int noise = ( int ) ( ( generator.noise( x * .00325 + this.x, y * .00325 + this.y ) + 1 ) / 2 * 255 );
				this.noise[ x + y * width ] = transparency << 24 | noise << 16 | noise << 8 | noise;
			}
		}
	}
	
	@Override
	public int[] getDisplay() {
		return noise;
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
