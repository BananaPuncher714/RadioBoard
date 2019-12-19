package io.github.bananapuncher714.radioboard;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.bananapuncher714.radioboard.api.MapDisplay;
import io.github.bananapuncher714.radioboard.api.MapDisplayProvider;
import io.github.bananapuncher714.radioboard.api.PacketHandler;
import io.github.bananapuncher714.radioboard.providers.canvas.RadioCanvasFactory;
import io.github.bananapuncher714.radioboard.tinyprotocol.TinyProtocol;
import io.github.bananapuncher714.radioboard.util.FileUtil;
import io.github.bananapuncher714.radioboard.util.ReflectionUtil;
import io.netty.channel.Channel;

/**
 * RadioBoard's core
 * 
 * @author BananaPuncher714
 */
public class RadioBoard extends JavaPlugin {
	private static RadioBoard INSTANCE;
	private static final String FILE_IMAGES = "/images/";
	private static final String FILE_CANVASES = "/providers/";
	
	private static int UPDATE_DELAY = 5;
	
	private PacketHandler packetHandler;
	private TinyProtocol tProtocol;
	
	private PlayerListener playerListener;
	
	protected Map< String, String > configBoards = new HashMap< String, String >();
	
	@Override
	public void onEnable() {
		INSTANCE = this;

		packetHandler = ReflectionUtil.getNewPacketHandlerInstance();
		tProtocol = new TinyProtocol( this ) {
			@Override
			public Object onPacketOutAsync( Player player, Channel channel, Object packet ) {
				return packetHandler.onPacketInterceptOut( player, packet );
			}

			@Override
			public Object onPacketInAsync( Player player, Channel channel, Object packet ) {
				return packetHandler.onPacketInterceptIn( player, packet );
			}
		};
		
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			packetHandler.registerPlayer( player );
		}
		
		saveDefaultConfig();
		saveResource( "boards.yml", false );
		
		getImageFile( "" ).mkdirs();
		getCanvasFile( "" ).mkdirs();
		
		File readme = new File( getDataFolder() + "/" + "README.md" );
		if ( !readme.exists() ) {
			// Save important files
			FileUtil.saveToFile( getResource( "README.md" ), readme, true );
			FileUtil.saveToFile( getResource( "LICENSE" ), new File( getDataFolder() + "/" + "LICENSE" ), true );
			
			// Save the example canvas
			FileUtil.saveToFile( getResource( "data/providers/example-canvas.yml" ), new File( getDataFolder() + FILE_CANVASES + "example-canvas.yml" ), true );
			
			// Save all pictures necessary
			FileUtil.saveToFile( getResource( "data/images/kurisu.gif" ), new File( getDataFolder() + FILE_IMAGES + "example/" + "kurisu.gif" ), true );
			FileUtil.saveToFile( getResource( "data/images/logo.png" ), new File( getDataFolder() + FILE_IMAGES + "example/" + "logo.png" ), true );
			FileUtil.saveToFile( getResource( "data/images/nyan_cat_background.jpg" ), new File( getDataFolder() + FILE_IMAGES + "example/" + "nyan_cat_background.jpg" ), true );
			FileUtil.saveToFile( getResource( "data/images/off-switch.png" ), new File( getDataFolder() + FILE_IMAGES + "example/" + "off-switch.png" ), true );
			FileUtil.saveToFile( getResource( "data/images/on-switch.png" ), new File( getDataFolder() + FILE_IMAGES + "example/" + "on-switch.png" ), true );
			FileUtil.saveToFile( getResource( "data/images/shinoa-smirk.png" ), new File( getDataFolder() + FILE_IMAGES + "example/" + "shinoa-smirk.png" ), true );
		}
		
		FileUtil.saveToFile( getResource( "config.yml" ), new File( getDataFolder() + "/config.yml" ), false );
		loadConfig();
		
		playerListener = new PlayerListener( this );
		Bukkit.getPluginManager().registerEvents( playerListener, this );
		Bukkit.getPluginManager().registerEvents( new BoardListener( this ), this );
		
		for ( Player player : Bukkit.getOnlinePlayers() ) {
			playerListener.updateMapsFor( player );
		}
		
		RadioBoardCommandExecutor command = new RadioBoardCommandExecutor( this );
		getCommand( "radioboard" ).setExecutor( command );
		getCommand( "radioboard" ).setTabCompleter( command );
	}

	@Override
	public void onDisable() {
		saveToConfig();
		FrameManager.INSTANCE.terminate();
	}
	
	private void loadConfig() {
		File boardCacheFile = new File( getDataFolder() + "/boards.yml" );
		if ( boardCacheFile.exists() ) {
			FileConfiguration boardCache = YamlConfiguration.loadConfiguration( boardCacheFile );
			if ( boardCache.contains( "displays" ) ) {
				loadBoardsFrom( boardCache.getConfigurationSection( "displays" ) );
			}
		}
		
		File frameCacheFile = new File( getDataFolder() + "/frame-cache.yml" );
		if ( frameCacheFile.exists() ) {
			FileConfiguration frameCache = YamlConfiguration.loadConfiguration( frameCacheFile );
			FrameManager.INSTANCE.loadBoards( frameCache.getConfigurationSection( "frames" ) );
		}
		
		File config = new File( getDataFolder() + "/config.yml" );
		if ( config.exists() ) {
			FileConfiguration configConfig = YamlConfiguration.loadConfiguration( config );
			UPDATE_DELAY = configConfig.getInt( "update-delay", 5 );
		}
	}
	
	private void loadBoardsFrom( ConfigurationSection section ) {
		for ( String name : section.getKeys( false ) ) {
			int id = section.getInt( name + ".id" );
			int width = section.getInt( name + ".width" );
			int height = section.getInt( name + ".height" );
			
			String presetFileName = section.getString( name + ".provider" );
			File preset = RadioBoard.getCanvasFile( presetFileName );
			
			if ( !preset.exists() ) {
				continue;
			}
			
			RBoard board = new RBoard( name, id, width, height );
			FileConfiguration presetSection = YamlConfiguration.loadConfiguration( preset );
			MapDisplayProvider provider = RadioCanvasFactory.deserialize( presetSection );
			board.setSource( provider );
			
			FrameManager.INSTANCE.registerDisplay( board );
			
			configBoards.put( name, presetFileName );
			
			getLogger().info( "Registered display " + name );
		}
	}
	
	private void saveBoardsTo( ConfigurationSection section ) {
		for ( String boardName : configBoards.keySet() ) {
			MapDisplay display = FrameManager.INSTANCE.getDisplay( boardName );
			if ( display == null ) {
				continue;
			}
			
			section.set( boardName + ".id", display.getMapId() );
			section.set( boardName + ".width", display.getMapWidth() );
			section.set( boardName + ".height", display.getMapHeight() );
			section.set( boardName + ".provider", configBoards.get( boardName ) );
		}
	}
	
	private void saveToConfig() {
		File boardCacheFile = new File( getDataFolder() + "/boards.yml" );
		if ( !boardCacheFile.exists() ) {
			try {
				boardCacheFile.createNewFile();
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}
		FileConfiguration boardCache = YamlConfiguration.loadConfiguration( boardCacheFile );
		boardCache.set( "displays", null );
		saveBoardsTo( boardCache.createSection( "displays" ) );
		try {
			boardCache.save( boardCacheFile );
		} catch ( IOException e1 ) {
			e1.printStackTrace();
		}
		
		File frameCacheFile = new File( getDataFolder() + "/frame-cache.yml" );
		if ( !frameCacheFile.exists() ) {
			frameCacheFile.getParentFile().mkdirs();
			try {
				frameCacheFile.createNewFile();
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}
		FileConfiguration frameCache = YamlConfiguration.loadConfiguration( frameCacheFile );
		frameCache.set( "frames", null );
		FrameManager.INSTANCE.saveBoards( frameCache.createSection( "frames" ) );
		
		try {
			frameCache.save( frameCacheFile );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get a list of this plugin's boards that were loaded from the boards.yml
	 * 
	 * @return
	 * The ids of default boards
	 */
	public Set< String > getCoreBoards() {
		return configBoards.keySet();
	}
	
	/**
	 * Update all default displays for a given player
	 * 
	 * @param player
	 * Player to update for
	 */
	public void updateDisplaysFor( Player player ) {
		playerListener.updateMapsFor( player );
	}

	/**
	 * Get the NMS implementation for sending map updates and stuff
	 * 
	 * @return
	 */
	public PacketHandler getPacketHandler() {
		return packetHandler;
	}
	
	public TinyProtocol getProtocol() {
		return tProtocol;
	}
	
	/**
	 * Get a file located in the image folder for this plugin
	 * 
	 * @param image
	 * The name of a file
	 * @return
	 * A new file in the image folder
	 */
	public static File getImageFile( String image ) {
		return new File( INSTANCE.getDataFolder() + FILE_IMAGES + image );
	}
	
	/**
	 * Get a file located in the canvas folder for this plugin
	 * 
	 * @param canvas
	 * The name of a file
	 * @return
	 * A new file in the canvas folder
	 */
	public static File getCanvasFile( String canvas ) {
		return new File( INSTANCE.getDataFolder() + FILE_CANVASES + canvas );
	}
	
	public static int getUpdateDelay() {
		return UPDATE_DELAY;
	}
	
	public static FrameManager getFrameManager() {
		return FrameManager.INSTANCE;
	}
	
	public static RadioBoard getInstance() {
		return INSTANCE;
	}
}
