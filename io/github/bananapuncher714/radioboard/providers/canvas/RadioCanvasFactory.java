package io.github.bananapuncher714.radioboard.providers.canvas;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.bukkit.configuration.ConfigurationSection;

import io.github.bananapuncher714.radioboard.RadioBoard;

/**
 * Utility class used to deserialize ConfigurationSections into RadioCanvases; Internal usage mostly
 * 
 * @author BananaPuncher714
 */
public final class RadioCanvasFactory {
	private RadioCanvasFactory() {
	}

	/**
	 * Deserialize a section into a RadioCanvas with corresponding components
	 * 
	 * @param section
	 * Must conform to the RadioBoard canvas format
	 * @return
	 */
	public static RadioCanvas deserialize( ConfigurationSection section ) {
		int width = section.getInt( "width" );
		int height = section.getInt( "height" );
		RadioCanvas canvas;
		if ( section.contains( "background-image" ) ) {
			File background = RadioBoard.getImageFile( section.getString( "background-image" ) );
			try {
				canvas = new RadioCanvas( width, height, ImageIO.read( background ) );
			} catch ( IOException e ) {
				e.printStackTrace();
				canvas = new RadioCanvas( width, height );
			}
		} else {
			canvas = new RadioCanvas( width, height );
		}
		
		List< Map< String, Object > > elements = ( List< Map< String, Object > > ) section.getList( "elements" );
		for ( Map< String, Object > element : elements ) {
			String type = ( String ) element.get( "type" );
			int x = ( int ) element.get( "x" );
			int y = ( int ) element.get( "y" );
			int elWidth = ( int ) element.get( "width" );
			int elHeight = ( int ) element.get( "height" );

			RadioIcon icon = null;
			
			element = ( Map< String, Object > ) element.get( "data" );
			if ( type.equalsIgnoreCase( "switch" ) ) {
				String onImagePath = ( String ) element.get( "on-image" );
				String offImagePath = ( String ) element.get( "off-image" );
				String onCommand = ( String ) element.get( "on-command" );
				String offCommand = ( String ) element.get( "off-command" );
				
				File onImage = RadioBoard.getImageFile( onImagePath );
				File offImage = RadioBoard.getImageFile( offImagePath );
				
				if ( onImage.exists() && offImage.exists() ) {
					try {
						icon = RadioIconFactory.constructSwitch( ImageIO.read( onImage ), ImageIO.read( offImage ), elWidth, elHeight, onCommand, offCommand );
					} catch ( IOException e ) {
						e.printStackTrace();
					}
				}
				
			} else if ( type.equalsIgnoreCase( "button" ) ) {
				String unclickedImagePath = ( String ) element.get( "unclicked-image" );
				String clickedImagePath = ( String ) element.get( "clicked-image" );
				String command = ( String ) element.get( "command" );
				long delay = ( long ) ( int ) element.get( "button-delay" );
				
				File unclickedFile = RadioBoard.getImageFile( unclickedImagePath );
				File clickedFile = RadioBoard.getImageFile( clickedImagePath );
				
				if ( unclickedFile.exists() && clickedFile.exists() ) {
					try {
						icon = RadioIconFactory.constructButton( ImageIO.read( clickedFile ), ImageIO.read( unclickedFile ), elWidth, elHeight, command, delay );
					} catch ( IOException e ) {
						e.printStackTrace();
					}
				}
			} else if ( type.equalsIgnoreCase( "image" ) ) {
				String imageFile = ( String ) element.get( "image" );
				File file = RadioBoard.getImageFile( imageFile );
				if ( file.exists() ) {
					try {
						icon = RadioIconFactory.constructImage( ImageIO.read( file ), elWidth, elHeight );
					} catch ( IOException e ) {
						e.printStackTrace();
					}
				}
			} else if ( type.equalsIgnoreCase( "gif" ) ) {
				String imageFile = ( String ) element.get( "gif" );
				File file = RadioBoard.getImageFile( imageFile );
				if ( file.exists() ) {
					icon = RadioIconFactory.constructGif( file, elWidth, elHeight );
				}
			} else if ( type.equalsIgnoreCase( "cloud" ) ) {
				int transparency = ( int ) element.get( "transparency" );
				int delay  = ( int ) element.get( "delay" );
				
				icon = RadioIconFactory.constructCloud( elWidth, elHeight, transparency, delay );
			}

			if ( icon != null ) {
				canvas.loadIcon( icon, x, y );
			}
		}
		return canvas;
	}
}
