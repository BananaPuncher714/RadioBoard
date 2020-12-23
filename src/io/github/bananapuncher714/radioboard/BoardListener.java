package io.github.bananapuncher714.radioboard;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

import io.github.bananapuncher714.radioboard.api.DisplayInteract;
import io.github.bananapuncher714.radioboard.api.MapDisplay;
import io.github.bananapuncher714.radioboard.util.VectorUtil;

/**
 * Internal use only
 * 
 * @author BananaPuncher714
 */
public class BoardListener implements Listener {
	private class BoardCoord {
		private final BoardFrame board;
		private final ItemFrame frame;
		private final Location clicked;
		private final int map;
		private final int x;
		private final int y;
		
		private BoardCoord( BoardFrame board, ItemFrame frame, Location clicked, int map, int x, int y ) {
			this.board = board;
			this.frame = frame;
			this.clicked = clicked;
			this.map = map;
			this.x = x;
			this.y = y;
		}
	}
	
	protected BoardListener( RadioBoard plugin ) {
		Bukkit.getScheduler().runTaskTimer( plugin, this::run, 0, 4 );
	}
	
	private void run() {
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			calculate( player, DisplayInteract.LOOK );
		}
	}
	
	@EventHandler( ignoreCancelled = false )
	private void onPlayerInteractEvent( PlayerInteractEvent event ) {
		// First we need the player and the action
		Player player = event.getPlayer();
		Action action = event.getAction();
		// Ignore this event if the action is not the player clicking
		if ( action == Action.PHYSICAL || event.getHand() != EquipmentSlot.HAND ) {
			return;
		}

		DisplayInteract displayAction = DisplayInteract.LEFT_CLICK;
		if ( action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK ) {
			displayAction = DisplayInteract.RIGHT_CLICK;
		}
		
		if ( calculate( player, displayAction ) ) {
			event.setCancelled( true );
		}
	}
	
	@EventHandler( ignoreCancelled = false )
	private void onHangingBreakEvent( HangingBreakEvent event ) {
		Hanging hanging = event.getEntity();
		
		if ( !( hanging instanceof ItemFrame ) ) {
			return;
		}
		
		BoardFrame frame = FrameManager.INSTANCE.getFrameAt( hanging.getLocation() );
		if ( frame != null ) {
			event.setCancelled( true );
		}
	}
	
	@EventHandler( ignoreCancelled = false )
	private void onPlayerInteractEntityEvent( PlayerInteractEntityEvent event ) {
		Entity entity = event.getRightClicked();
		
		if ( !( entity instanceof ItemFrame ) ) {
			return;
		}
		
		BoardFrame frame = FrameManager.INSTANCE.getFrameAt( entity.getLocation() );
		if ( frame != null ) {
			event.setCancelled( true );
			calculate( event.getPlayer(), DisplayInteract.RIGHT_CLICK );
		}
	}
	
	@EventHandler( ignoreCancelled = false )
	private void onEntityDamageFrameEvent( EntityDamageEvent event ) {
		Entity entity = event.getEntity();
		
		if ( !( entity instanceof ItemFrame ) ) {
			return;
		}
		
		BoardFrame frame = FrameManager.INSTANCE.getFrameAt( entity.getLocation() );
		if ( frame != null ) {
			event.setCancelled( true );
			if ( event instanceof EntityDamageByEntityEvent ) {
				EntityDamageByEntityEvent damageEvent = ( EntityDamageByEntityEvent ) event;
				Entity damager = damageEvent.getDamager();
				if ( damager instanceof Player ) {
					calculate( ( Player ) damager, DisplayInteract.LEFT_CLICK );
				}
			}
		}
	}
	
	@EventHandler
	private void onProjectileHitEvent( ProjectileHitEvent event ) {
		Entity entity = event.getEntity();
		BoardCoord coords = getBoardCoords( entity.getLocation(), entity.getVelocity().normalize() );
		if ( coords == null ) {
			return;
		}
		for ( MapDisplay display : FrameManager.INSTANCE.getDisplays() ) {
			if ( display.getMapId() == coords.board.getId() ) {
				display.onClick( coords.board, entity, DisplayInteract.PROJECTILE, coords.map, coords.x, coords.y );
			}
		}
	}
	
	@EventHandler
	private void onMapInitializeEvent( MapInitializeEvent event ) {
		short id = ( short ) event.getMap().getId();
		if ( RadioBoard.getInstance().getPacketHandler().isMapRegistered( id ) ) {
			event.getMap().getRenderers().clear();
		}
	}
	
	private BoardCoord getBoardCoords( Location origin, Vector direction ) {
		for ( BoardFrame board : FrameManager.INSTANCE.getBoardFrames() ) {
			Location originLocation = origin.clone();
			Vector originDirection = direction.clone();
			
			if ( originLocation.getWorld() != board.getTopLeftCorner().getWorld() ) {
				continue;
			}
			
			BlockFace face = board.getFace();
			// Get the normal vector to the board
			Vector normal = new Vector( face.getModX(), face.getModY(), face.getModZ() );
			// Check to see if the player is actually looking at the board
			if ( normal.dot( originDirection ) < 0 ) {
//				player.sendMessage( "You are looking at the back of the board!" );
				continue;
			}
			// Get the point of the board
			Vector positive = normal.clone();
			positive.multiply( positive ).multiply( 8 / 16.0 );
			positive.add( normal.clone().multiply( 6.9 / 16.0 ) );
			Location point = board.getTopLeftCorner().clone().add( positive );

			// Calculate point of click
			Location location = VectorUtil.calculateVector( point, normal, originLocation, originDirection );
			if ( location == null ) {
				continue;
			}
			Location originalLocation = location.clone();
			
			// Check to see if it is a clicked frame
			ItemFrame frame = board.getItemFrameAt( location );
			if ( frame == null ) {
//				player.sendMessage( "No frame in sight!" );
				continue;
			}
			// Make sure the player can actually see the frame
//			if ( !player.hasLineOfSight( frame ) ){
//				player.sendMessage( "You cannot see the item frame!" );
//				continue;
//			}
			int id = board.getFrames().indexOf( frame.getUniqueId() );
			if ( id < 0 ) {
//				player.sendMessage( "You clicked on an invalid map!" );
				continue;
			}
			id += board.getId();
//			player.sendMessage( "You clicked on map with id of " + id );
			int x = 0;
			int y = 0;
			location.subtract( location.getBlockX(), location.getBlockY(), location.getBlockZ() );
			if ( normal.getX() != 0 ) {
				x = ( int ) Math.abs( ( location.getZ() - ( .5 - .5 * normal.getX() ) ) * 128 );
				y = 127 - ( int ) ( location.getY() * 128 );
			} else if ( normal.getY() != 0 ) {
				// Not sure if this works, probably needs more math
				x = ( int ) ( location.getZ() * 128 );
				y = 127 - ( int ) ( location.getX() * 128 );
			} else {
				// If it's on the floor...
				x = ( int ) Math.abs( ( location.getX() - ( .5 + .5 * normal.getZ() ) ) * 128 );
				y = 127 - ( int ) ( location.getY() * 128 );
			}
			return new BoardCoord( board, frame, originalLocation, id, x, y );
		}
		return null;
	}
	
	private boolean calculate( Player player, DisplayInteract action ) {
		BoardCoord coord = getBoardCoords( player.getEyeLocation(), player.getLocation().getDirection() );
		if ( coord == null || !player.hasLineOfSight( coord.frame ) || coord.clicked.distanceSquared( player.getEyeLocation() ) > 4096 ) {
			return false;
		}
		for ( MapDisplay display : FrameManager.INSTANCE.getDisplays() ) {
			if ( display.getMapId() == coord.board.getId() ) {
				if ( display.isObserving( new RadioObserver( player.getUniqueId() ) ) ) {
					if ( RadioBoard.getInstance().getCoreBoards().contains( display.getId() ) && !player.hasPermission( "radioboard.board." + display.getId() + ".interact" ) ) {
						continue;
					}
					display.onClick( coord.board, player, action, coord.map, coord.x, coord.y );
					return true;
				}
			}
		}
		return false;
	}
}
