package io.github.bananapuncher714.radioboard.dependency;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DependencyManager {
	private static boolean placeholderAPI = Bukkit.getPluginManager().isPluginEnabled( "PlaceholderAPI" );
	
	public static void init() {
	}
	
	public static String parse( Player player, String input ) {
		String result = input;
		if ( placeholderAPI ) {
			result = ClipsPlaceholder.parse( player, result );
		}
		return result;
	}
}
