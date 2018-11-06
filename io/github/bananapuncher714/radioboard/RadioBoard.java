package io.github.bananapuncher714.radioboard;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.bananapuncher714.radioboard.api.PacketHandler;
import io.github.bananapuncher714.radioboard.command.RadioBoardCommandExecutor;
import io.github.bananapuncher714.radioboard.providers.GifPlayer;
import io.github.bananapuncher714.radioboard.tinyprotocol.TinyProtocol;
import io.github.bananapuncher714.radioboard.util.FileUtil;
import io.github.bananapuncher714.radioboard.util.ReflectionUtil;
import io.netty.channel.Channel;

public class RadioBoard extends JavaPlugin {
	private static RadioBoard INSTANCE;
	private static final String FILE_IMAGES = "/images/";
	private static final String FILE_CANVASES = "/providers/";
	
	private PacketHandler packetHandler;
	private TinyProtocol tProtocol;
	
	protected Set< String > configBoards = new HashSet< String >();
	
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
		
		loadConfig();
		
		Bukkit.getPluginManager().registerEvents( new PlayerListener( this ), this );
		Bukkit.getPluginManager().registerEvents( new BoardListener( this ), this );
		
		getCommand( "video" ).setExecutor( new RadioBoardCommandExecutor( this ) );
	}

	@Override
	public void onDisable() {
		saveToConfig();
		FrameManager.INSTANCE.terminate();
	}
	
	private void loadConfig() {
		File frameCacheFile = new File( getDataFolder() + "/frame-cache.yml" );
		if ( frameCacheFile.exists() ) {
			FileConfiguration frameCache = YamlConfiguration.loadConfiguration( frameCacheFile );
			FrameManager.INSTANCE.loadBoards( frameCache.getConfigurationSection( "frames" ) );
		}
	}
	
	private void saveToConfig() {
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
	
	public static File getCanvasFile( String canvas ) {
		return new File( INSTANCE.getDataFolder() + FILE_CANVASES + canvas );
	}
	
	public static RadioBoard getInstance() {
		return INSTANCE;
	}
}
