package io.github.bananapuncher714.radioboard;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import io.github.bananapuncher714.radioboard.api.MapDisplay;
import io.github.bananapuncher714.radioboard.api.PacketHandler;
import io.github.bananapuncher714.radioboard.command.RadioBoardCommandExecutor;
import io.github.bananapuncher714.radioboard.dependency.DependencyManager;
import io.github.bananapuncher714.radioboard.providers.GifPlayer;
import io.github.bananapuncher714.radioboard.tinyprotocol.TinyProtocol;
import io.github.bananapuncher714.radioboard.util.JetpImageUtil;
import io.github.bananapuncher714.radioboard.util.ReflectionUtil;
import io.github.bananapuncher714.radioboard.util.VectorUtil;
import io.netty.channel.Channel;

public class RadioBoard extends JavaPlugin {
	private static RadioBoard INSTANCE;
	private static final String FILE_IMAGES = "/images/";
	
	private PacketHandler packetHandler;
	private TinyProtocol tProtocol;
	
	protected GifPlayer player;
	protected int currentId, mapWidth, mapHeight, width;
	protected byte[] data;
	
	protected BoardFrame fBoard;
	
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
	
	public static RadioBoard getInstance() {
		return INSTANCE;
	}
}
