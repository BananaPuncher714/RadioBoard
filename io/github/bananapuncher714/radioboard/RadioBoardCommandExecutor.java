package io.github.bananapuncher714.radioboard;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.lang.Validate;
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
					board( sender, args );
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
		sender.sendMessage( ChatColor.RED + "Incorrect usage! '/radioboard <board|show> ..." );
	}
	
	private void board( CommandSender sender, String[] args ) {
		Validate.isTrue( sender.hasPermission( "radioboard.admin" ), ChatColor.RED + "You do not have permission to run this command!" );
		Validate.isTrue( sender instanceof Player, ChatColor.RED + "You must be a player to run this command!" );
		Validate.isTrue( args.length == 3, ChatColor.RED + "Incorrect usage! '/radioboard board <name> <map-id>'" );
		int id;
		try {
			id = Integer.parseInt( args[ 2 ] );
		} catch ( NumberFormatException exception ) {
			throw new IllegalArgumentException( ChatColor.RED + "'" + args[ 2 ] + "' must be an integer!" );
		}
		Player player = ( Player ) sender;
		String name = args[ 1 ];
		
		Location lookingAt = player.getLastTwoTargetBlocks( ( Set< Material > ) null, 100 ).get( 0 ).getLocation();
		ItemFrame frame = BukkitUtil.getItemFrameAt( lookingAt );
		Validate.isTrue( frame != null, ChatColor.RED + "You are not looking at any item frame!" );
		
		BoardFrame board = FrameManager.INSTANCE.getFrameAt( lookingAt );
		
		board = new BoardFrame( frame, id );
		
		FrameManager.INSTANCE.registerBoard( name, board );
		sender.sendMessage( "Successfully set up a new board with map id '" + id + "'" );
		
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
		
		display.addObserver( new RadioObserver( player.getUniqueId() ) );

		player.sendMessage( "Created map display!" );
	}
}
