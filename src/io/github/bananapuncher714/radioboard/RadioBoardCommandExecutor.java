package io.github.bananapuncher714.radioboard;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import io.github.bananapuncher714.radioboard.api.MapDisplay;
import io.github.bananapuncher714.radioboard.providers.canvas.RadioCanvas;
import io.github.bananapuncher714.radioboard.providers.canvas.RadioCanvasFactory;
import io.github.bananapuncher714.radioboard.util.BukkitUtil;

/**
 * For internal use only
 * 
 * @author BananaPuncher714
 */
public class RadioBoardCommandExecutor implements CommandExecutor, TabCompleter {
	private RadioBoard plugin;

	protected RadioBoardCommandExecutor( RadioBoard plugin ) {
		this.plugin = plugin;
	}

	@Override
	public List< String > onTabComplete( CommandSender sender, Command command, String label, String[] args ) {
		List< String > aos = new ArrayList< String >();
		if ( !sender.hasPermission( "radioboard.admin" ) ) {
			return aos;
		}

		if ( args.length == 1 ) {
			aos.add( "board" );
			aos.add( "list" );
			aos.add( "display" );
		} else if ( args.length == 2 ) {
			if ( args[ 0 ].equalsIgnoreCase( "board" ) || args[ 0 ].equalsIgnoreCase( "display" ) ) {
				aos.add( "create" );
				aos.add( "remove" );
			} else if ( args[ 0 ].equalsIgnoreCase( "list" ) ) {
				aos.add( "boards" );
				aos.add( "displays" );
			}
		} else if ( args.length == 3 ) {
			if ( args[ 1 ].equalsIgnoreCase( "remove" ) ) {
				if ( args[ 0 ].equalsIgnoreCase( "board" ) ) {
					aos.addAll( FrameManager.INSTANCE.boards.keySet() );
				} else if ( args[ 0 ].equalsIgnoreCase( "display" ) ) {
					aos.addAll( FrameManager.INSTANCE.displays.keySet() );
				}
			}
		}

		List< String > completions = new ArrayList< String >();
		StringUtil.copyPartialMatches( args[ args.length - 1 ], aos, completions );
		Collections.sort( completions );
		return completions;
	}

	@Override
	public boolean onCommand( CommandSender sender, Command command, String arg2, String[] args ) {
		try {
			if ( args.length == 0 ) {
				help( sender );
			} else if ( args.length > 0 ) {
				String option = args[ 0 ];
				args = pop( args );
				if ( option.equalsIgnoreCase( "board" ) ) {
					board( sender, args );
				} else if ( option.equalsIgnoreCase( "display" ) ) {
					display( sender, args );
				} else if ( option.equalsIgnoreCase( "list" ) ) {
					list( sender, args );
				} else {
					help( sender );
				}
			}
		} catch ( IllegalArgumentException exception ) {
			sender.sendMessage( exception.getMessage() );
		}
		return false;
	}

	private void help( CommandSender sender ) {
		Validate.isTrue( sender.hasPermission( "radioboard.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		sender.sendMessage( ChatColor.RED + "Incorrect usage! '/radioboard <board|display|list> ..." );
	}

	private void board( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "radioboard.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( args.length > 0, ChatColor.RED + "Incorrect usage! '/radioboard board <create|remove> ...'" );

		String option = args[ 0 ];
		args = pop( args );
		if ( option.equalsIgnoreCase( "create" ) ) {
			boardCreate( sender, args );
		} else if ( option.equalsIgnoreCase( "remove" ) ) {
			boardRemove( sender, args );
		} else {
			throw new IllegalArgumentException( ChatColor.RED + "Incorrect usage! '/radioboard board <create|remove> ...'" );
		}
	}

	private void boardCreate( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "radioboard.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( sender instanceof Player, ChatColor.RED + "You must be a player to run this command!" );
		Validate.isTrue( args.length == 2, ChatColor.RED + "Incorrect usage! '/radioboard board create <name> <map-id>'" );
		int mapId;
		try {
			mapId = Integer.parseInt( args[ 1 ] );
		} catch ( NumberFormatException exception ) {
			throw new IllegalArgumentException( ChatColor.RED + "'" + args[ 1 ] + "' is not a valid map id!" );
		}

		String name = args[ 0 ];

		Player player = ( Player ) sender;

		sender.sendMessage( ChatColor.GREEN + "Creating board..." );

		Location lookingAt = player.getLastTwoTargetBlocks( ( Set< Material > ) null, 100 ).get( 0 ).getLocation();
		ItemFrame frame = BukkitUtil.getItemFrameAt( lookingAt );
		Validate.isTrue( frame != null, ChatColor.RED + "You are not looking at any item frame!" );

		BoardFrame board = FrameManager.INSTANCE.getFrameAt( lookingAt );

		board = new BoardFrame( frame, mapId );

		FrameManager.INSTANCE.registerBoard( name, board );

		for ( Player worldPlayer : player.getWorld().getPlayers() ) {
			plugin.updateDisplaysFor( worldPlayer );
		}
		sender.sendMessage( ChatColor.GREEN + "Successfully set up a new board(" + name + ") with map id '" + mapId + "'" );
	}

	private void boardRemove( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "radioboard.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( args.length == 1, ChatColor.RED + "Incorrect usage! '/radioboard board remove <name>'" );

		String name = args[ 0 ];
		BoardFrame frame = FrameManager.INSTANCE.getFrame( name );

		Validate.isTrue( frame != null, ChatColor.RED + "'" + name + "' does not exist!" );

		FrameManager.INSTANCE.removeFrame( name ).terminate();

		for ( Player worldPlayer : frame.getTopLeftCorner().getWorld().getPlayers() ) {
			plugin.updateDisplaysFor( worldPlayer );
		}

		sender.sendMessage( ChatColor.GREEN + "Successfully removed a board(" + name + ")!" );
	}

	private void display( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "radioboard.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( args.length > 0, ChatColor.RED + "Incorrect usage! '/radioboard display <create|remove> ...'" );

		String option = args[ 0 ];
		args = pop( args );
		if ( option.equalsIgnoreCase( "create" ) ) {
			displayCreate( sender, args );
		} else if ( option.equalsIgnoreCase( "remove" ) ) {
			displayRemove( sender, args );
		} else {
			throw new IllegalArgumentException( ChatColor.RED + "Incorrect usage! '/radioboard display <create|remove> ...'" );
		}
	}

	private void displayCreate( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "radioboard.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( args.length == 5, ChatColor.RED + "Incorrect usage! '/radioboard display create <name> <map-id> <file-name> <width> <height>'" );
		File file = RadioBoard.getCanvasFile( args[ 2 ] );
		Validate.isTrue( file.exists(), ChatColor.RED + "'" + args[ 2 ] + "' does not exist!" );
		int mapId;
		try {
			mapId = Integer.parseInt( args[ 1 ] );
		} catch ( NumberFormatException exception ) {
			throw new IllegalArgumentException( ChatColor.RED + "'" + args[ 1 ] + "' is not a valid map id!" );
		}

		int width;
		try {
			width = Integer.parseInt( args[ 3 ] );
		} catch ( NumberFormatException exception ) {
			throw new IllegalArgumentException( ChatColor.RED + "'" + args[ 3 ] + "' is not a valid width!" );
		}

		int height;
		try {
			height = Integer.parseInt( args[ 4 ] );
		} catch ( NumberFormatException exception ) {
			throw new IllegalArgumentException( ChatColor.RED + "'" + args[ 4 ] + "' is not a valid height!" );
		}
		String name = args[ 0 ];

		sender.sendMessage( ChatColor.GREEN + "Creating display..." );

		MapDisplay display = new RBoard( name, mapId, width, height );

		FrameManager.INSTANCE.registerDisplay( display );
		plugin.configBoards.put( name, args[ 3 ] );

		FileConfiguration config = YamlConfiguration.loadConfiguration( file );
		RadioCanvas canvas = RadioCanvasFactory.deserialize( config );

		display.setSource( canvas );

		plugin.configBoards.put( display.getId(), args[ 2 ] );

		for ( Player worldPlayer : Bukkit.getOnlinePlayers() ) {
			plugin.updateDisplaysFor( worldPlayer );
		}

		sender.sendMessage( ChatColor.GREEN + "Successfully created a new display(" + name + ") with the template '" + file.getName() + "'" );
	}

	private void displayRemove( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "radioboard.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( args.length == 1, ChatColor.RED + "Incorrect usage! '/radioboard display remove <name>'" );

		String name = args[ 0 ];
		MapDisplay frame = FrameManager.INSTANCE.getDisplay( name );

		Validate.isTrue( frame != null, ChatColor.RED + "'" + name + "' does not exist!" );

		FrameManager.INSTANCE.removeDisplay( name ).terminate();

		sender.sendMessage( ChatColor.GREEN + "Successfully removed a board(" + name + ")!" );
	}

	private void list( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "radioboard.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( args.length == 1, ChatColor.RED + "Incorrect usage! '/radioboard list <boards|displays>'" );

		if ( args[ 0 ].equalsIgnoreCase( "boards" ) ) {
			if ( FrameManager.INSTANCE.boards.isEmpty() ) {
				sender.sendMessage( ChatColor.GREEN + "There are no boards registered!" );
			} else {
				sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&bBoards(&aname&b, &eid&b, &fwidth and height&b, &dlocation&b):" ) );
				for ( String key : FrameManager.INSTANCE.boards.keySet() ) {
					BoardFrame frame = FrameManager.INSTANCE.boards.get( key );
					String info = ChatColor.GREEN + key;
					info += ChatColor.YELLOW + " " + frame.getId();
					info += ChatColor.WHITE + " " + frame.getWidth() + "x" + frame.getHeight();
					Location location = frame.getTopLeftCorner();
					info += ChatColor.LIGHT_PURPLE + " " + location.getWorld().getName();
					info += " " + location.getBlockX() + ", " + location.getBlockY() + " " + location.getBlockZ();
					sender.sendMessage( info );
				}
			}
		} else if ( args[ 0 ].equalsIgnoreCase( "displays" ) ) {
			if ( FrameManager.INSTANCE.getDisplays().isEmpty() ) {
				sender.sendMessage( ChatColor.GREEN + "There are no displays registered!" );
			} else {
				sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&bDisplays(&aname&b, &eid&b, &fwidth and height&b, &dprovider&b):" ) );
				for ( MapDisplay display : FrameManager.INSTANCE.getDisplays() ) {
					String info = ChatColor.GREEN + display.getId();
					info += ChatColor.YELLOW + " " + display.getMapId();
					info += ChatColor.WHITE + " " + display.getMapWidth() + "x" + display.getMapHeight();
					if ( RadioBoard.getInstance().configBoards.containsKey( display.getId() ) ) {
						info += ChatColor.LIGHT_PURPLE + " " + RadioBoard.getInstance().configBoards.get( display.getId() );
					} else {
						info += ChatColor.LIGHT_PURPLE + " ?";
					}
					sender.sendMessage( info );
				}
			}
		} else {
			throw new IllegalArgumentException( ChatColor.RED + "Incorrect usage! '/radioboard list <boards|displays>'" );
		}
	}


	private String[] pop( String[] array ) {
		String[] array2 = new String[ Math.max( 0, array.length - 1 ) ];
		for ( int i = 1; i < array.length; i++ ) {
			array2[ i - 1 ] = array[ i ];
		}
		return array2;
	}
}
