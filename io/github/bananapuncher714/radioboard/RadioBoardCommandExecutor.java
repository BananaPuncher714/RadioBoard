package io.github.bananapuncher714.radioboard;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

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

import io.github.bananapuncher714.radioboard.api.MapDisplay;
import io.github.bananapuncher714.radioboard.api.MapDisplayProvider;
import io.github.bananapuncher714.radioboard.providers.GifPlayer;
import io.github.bananapuncher714.radioboard.providers.ImageProvider;
import io.github.bananapuncher714.radioboard.providers.canvas.RadioCanvas;
import io.github.bananapuncher714.radioboard.providers.canvas.RadioCanvasFactory;
import io.github.bananapuncher714.radioboard.util.BukkitUtil;

public class RadioBoardCommandExecutor implements CommandExecutor, TabCompleter {
	RadioBoard plugin;
	
	public RadioBoardCommandExecutor( RadioBoard plugin ) {
		this.plugin = plugin;
	}
	
	@Override
	public List< String > onTabComplete( CommandSender arg0, Command arg1, String arg2, String[] arg3 ) {
		return null;
	}

	@Override
	public boolean onCommand( CommandSender sender, Command command, String arg2, String[] args ) {
		try {
			if ( args.length == 0 ) {
				help( sender );
			} else if ( args.length > 0 ) {
				if ( args[ 0 ].equalsIgnoreCase( "board" ) ) {
					board( sender, pop( args ) );
				} else if ( args[ 0 ].equalsIgnoreCase( "show" ) ) {
					show( sender, args );
				} else if ( args[ 0 ].equalsIgnoreCase( "test" ) ) {
					test( sender, args );
				} else {
					help( sender );
				}
			}
		} catch ( IllegalArgumentException exception ) {
			sender.sendMessage( exception.getMessage() );
		} catch ( IOException e ) {
			e.printStackTrace();
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
		
		Location lookingAt = player.getLastTwoTargetBlocks( ( Set< Material > ) null, 100 ).get( 0 ).getLocation();
		ItemFrame frame = BukkitUtil.getItemFrameAt( lookingAt );
		Validate.isTrue( frame != null, ChatColor.RED + "You are not looking at any item frame!" );
		
		BoardFrame board = FrameManager.INSTANCE.getFrameAt( lookingAt );
		
		board = new BoardFrame( frame, mapId );
		
		FrameManager.INSTANCE.registerBoard( name, board );
		
		for ( Player worldPlayer : player.getWorld().getPlayers() ) {
			RadioBoard.getInstance().updateDisplaysFor( worldPlayer );
		}
		sender.sendMessage( ChatColor.GREEN + "Successfully set up a new board(" + name + ") with map id '" + mapId + "'" );
	}
	
	private void boardRemove( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "radioboard.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( args.length == 1, ChatColor.RED + "Incorrect usage! '/radioboard board remove <name>'" );
		
		String name = args[ 0 ];
		BoardFrame frame = FrameManager.INSTANCE.getFrame( name );
		
		Validate.isTrue( frame != null, ChatColor.RED + "'" + name + "' does not exist!" );
		
		FrameManager.INSTANCE.removeFrame( name );
		
		for ( Player worldPlayer : frame.getTopLeftCorner().getWorld().getPlayers() ) {
			RadioBoard.getInstance().updateDisplaysFor( worldPlayer );
		}
		
		sender.sendMessage( ChatColor.GREEN + "Successfully removed a board(" + name + ")!" );
	}
	
	private void displayCreate( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "radioboard.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( args.length == 2, ChatColor.RED + "Incorrect usage! '/radioboard board create <name> <map-id>'" );
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
		
		
	}
	
	private void show( CommandSender sender, String[] args ) throws IOException {
		Validate.isTrue( sender.hasPermission( "radioboard.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( sender instanceof Player, ChatColor.RED + "You must be a player to run this command!" );
		Player player = ( Player ) sender;
		Validate.isTrue( args.length == 5, ChatColor.RED + "Incorrect usage! '/radioboard show <name> <id> <file> <x:y>'" );
		File file = new File( plugin.getDataFolder() + "/images/" + args[ 3 ] );
		Validate.isTrue( file.exists(), ChatColor.RED + args[ 3 ] + " does not exist!" );
		sender.sendMessage( "Parsing..." );
		String name = args[ 1 ];
		
		int id = Integer.parseInt( args[ 2 ] );
		String[] split = args[ 4 ].split( ":" );
		int mapWidth = Integer.parseInt( split[ 0 ] );
		int mapHeight = Integer.parseInt( split[ 1 ] );
		
		MapDisplay display = new RBoard( name, id, mapWidth, mapHeight );
		
		FrameManager.INSTANCE.registerDisplay( display );
		
		MapDisplayProvider provider;
		if ( args[ 3 ].endsWith( ".gif" ) ) {
			provider = new GifPlayer( file );
			player.sendMessage( "Detected gif!" );
		} else {
			BufferedImage image = ImageIO.read( file );
			provider = new ImageProvider( image );
		}
		display.setSource( provider );
		
		display.addObserver( new RadioObserver( player.getUniqueId() ) );
		
		player.sendMessage( "Created map display!" );
	}
	
	private void test( CommandSender sender, String[] args ) throws IOException {
		Validate.isTrue( sender.hasPermission( "radioboard.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( sender instanceof Player, ChatColor.RED + "You must be a player to run this command!" );
		Player player = ( Player ) sender;
		Validate.isTrue( args.length == 5, ChatColor.RED + "Incorrect usage! '/radioboard test <name> <id> <file> <x:y>'" );
		File canvasFile = RadioBoard.getCanvasFile( args[ 3 ] );
		Validate.isTrue( canvasFile.exists(), ChatColor.RED + args[ 3 ] + " does not exist!" );
		
		String name = args[ 1 ];
		
		int id = Integer.parseInt( args[ 2 ] );
		String[] split = args[ 4 ].split( ":" );
		int mapWidth = Integer.parseInt( split[ 0 ] );
		int mapHeight = Integer.parseInt( split[ 1 ] );
		
		MapDisplay display = new RBoard( name, id, mapWidth, mapHeight );
		
		FrameManager.INSTANCE.registerDisplay( display );
		RadioBoard.getInstance().configBoards.put( name, args[ 3 ] );
		
		FileConfiguration config = YamlConfiguration.loadConfiguration( canvasFile );
		RadioCanvas canvas = RadioCanvasFactory.deserialize( config );
		
		display.setSource( canvas );
		
		for ( Player worldPlayer : Bukkit.getOnlinePlayers() ) {
			RadioBoard.getInstance().updateDisplaysFor( worldPlayer );
		}
		
		player.sendMessage( "Created map display!" );
	}
	
	private String[] pop( String[] array ) {
		String[] array2 = new String[ array.length - 1 ];
		for ( int i = 1; i < array.length; i++ ) {
			array2[ i - 1 ] = array[ i ];
		}
		return array2;
	}
}
