package io.github.bananapuncher714.radioboard.providers.canvas;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import io.github.bananapuncher714.radioboard.BoardFrame;
import io.github.bananapuncher714.radioboard.RadioBoard;
import io.github.bananapuncher714.radioboard.api.DisplayInteract;
import io.github.bananapuncher714.radioboard.dependency.DependencyManager;

/**
 * A button that stays pressed for a certain amount of time and can run a command
 * 
 * @author BananaPuncher714
 */
public class RadioIconButton implements RadioIcon {
	int[] unclicked;
	int[] clicked;
	protected List< String > commands;
	int width;
	long delay;
	boolean pressed = false;
	
	protected RadioCanvas provider;
	
	public RadioIconButton( int[] unclicked, int[] clicked, int width, List< String > command, long delay ) {
		this.width = width;
		this.unclicked = unclicked;
		this.clicked = clicked;
		this.commands = command;
		this.delay = delay;
	}
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return clicked.length / width;
	}
	
	@Override
	public int[] getDisplay() {
		return pressed ? clicked : unclicked;
	}
	
	@Override
	public void init( RadioCanvas provider ) {
		this.provider = provider;
	}
	
	@Override
	public void onClick( BoardFrame frame, Entity entity, DisplayInteract action, int x, int y ) {
		if ( action == DisplayInteract.LOOK || action == DisplayInteract.PROJECTILE ) {
			return;
		}
		if ( !( entity instanceof Player ) ) {
			return;
		}
		if ( pressed ) {
			return;
		}
		Player player = ( Player ) entity;
		pressed = true;
		provider.update( this );

		for ( String command : commands ) {
			String cmd = command.replace( "%player_name%", player.getName() );
			CommandSender sender = Bukkit.getConsoleSender();
			if ( cmd.startsWith( "/" ) ) {
				cmd = cmd.substring( 1 );
				sender = player;
			}
			Bukkit.dispatchCommand( sender, DependencyManager.parse( player, cmd ) );
		}
		
		Bukkit.getScheduler().runTaskAsynchronously( RadioBoard.getInstance(), new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep( delay );
				} catch ( InterruptedException e ) {
					e.printStackTrace();
				}
				unpress();
			}
		} );
	}
	
	private void unpress() {
		pressed = false;
		if ( provider != null ) {
			provider.update( this );
		}
	}
	
	@Override
	public void terminate() {
		provider = null;
	}
}
