package io.github.bananapuncher714.radioboard.providers.canvas;

import java.awt.Image;
import java.io.File;

import io.github.bananapuncher714.radioboard.util.JetpImageUtil;

/**
 * Construct various RadioIcons from given parameters
 * 
 * @author BananaPuncher714
 */
public final class RadioIconFactory {
	private RadioIconFactory() {
	}
	
	/**
	 * Gets a new RadioIconSwitch
	 * 
	 * @param on
	 * The image when the switch is on
	 * @param off
	 * The image when the switch is off
	 * @param width
	 * The width in pixels of the switch
	 * @param height
	 * The height in pixels of the switch
	 * @param commandOn
	 * A command that is ran when the switch is turned on
	 * @param commandOff
	 * A command that is ran when the switch is turned off
	 * @return
	 * A RadioIconSwitch made from all the parameters
	 */
	public static RadioIconSwitch constructSwitch( Image on, Image off, int width, int height, String commandOn, String commandOff ) {
		Image onImage = JetpImageUtil.toBufferedImage( on ).getScaledInstance( width, height, Image.SCALE_SMOOTH );
		Image offImage = JetpImageUtil.toBufferedImage( off ).getScaledInstance( width, height, Image.SCALE_SMOOTH );
		
		int[] rgbOn = JetpImageUtil.getRGBArray( JetpImageUtil.toBufferedImage( onImage ) );
		int[] rgbOff = JetpImageUtil.getRGBArray( JetpImageUtil.toBufferedImage( offImage ) );
		
		return new RadioIconSwitch( rgbOn, rgbOff, width, commandOn, commandOff );
	}
	
	/**
	 * Creates a static image RadioIconImage
	 * 
	 * @param image
	 * The image to show
	 * @param width
	 * The new width
	 * @param height
	 * The new height
	 * @return
	 */
	public static RadioIconImage constructImage( Image image, int width, int height ) {
		Image resized = JetpImageUtil.toBufferedImage( image ).getScaledInstance( width, height, Image.SCALE_SMOOTH );
		
		int[] rgb = JetpImageUtil.getRGBArray( JetpImageUtil.toBufferedImage( resized ) );
		
		return new RadioIconImage( rgb, width );
	}
	
	/**
	 * Construct a normal button
	 * 
	 * @param clicked
	 * The clicked image
	 * @param unclicked
	 * The unclicked image
	 * @param width
	 * Width of the button
	 * @param height
	 * Height of the button
	 * @param command
	 * Command that will be executed when pressed
	 * @param buttonDelay
	 * How long the button stays depressed
	 * @return
	 */
	public static RadioIconButton constructButton( Image clicked, Image unclicked, int width, int height, String command, long buttonDelay ) {
		Image onImage = JetpImageUtil.toBufferedImage( clicked ).getScaledInstance( width, height, Image.SCALE_SMOOTH );
		Image offImage = JetpImageUtil.toBufferedImage( unclicked ).getScaledInstance( width, height, Image.SCALE_SMOOTH );
		
		int[] rgbClicked = JetpImageUtil.getRGBArray( JetpImageUtil.toBufferedImage( onImage ) );
		int[] rgbUnclicked = JetpImageUtil.getRGBArray( JetpImageUtil.toBufferedImage( offImage ) );
		
		return new RadioIconButton( rgbUnclicked, rgbClicked, width, command, buttonDelay );
	}
	
	/**
	 * Create a new GIF icon
	 * 
	 * @param file
	 * The gif file
	 * @param width
	 * Width of the gif
	 * @param height
	 * Height of the gif
	 * @return
	 */
	public static RadioIconGif constructGif( File file, int width, int height ) {
		return new RadioIconGif( file, width, height );
	}
	
	/**
	 * Constructs a dynamic cloudy filter
	 * 
	 * @param width
	 * Width of the cloud effect
	 * @param height
	 * Height of the cloud effect
	 * @param transparency
	 * Transparency of the cloud
	 * @param delay
	 * How fast it updates in milliseconds, do not make this too fast or the client will timeout
	 * @return
	 */
	public static RadioIconCloud constructCloud( int width, int height, int transparency, int delay ) {
		return new RadioIconCloud( width, height, transparency, delay );
	}
}
