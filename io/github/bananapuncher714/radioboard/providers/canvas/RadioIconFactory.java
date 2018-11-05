package io.github.bananapuncher714.radioboard.providers.canvas;

import java.awt.Image;
import java.io.File;

import io.github.bananapuncher714.radioboard.util.JetpImageUtil;

public class RadioIconFactory {

	public static RadioIconSwitch constructSwitch( Image on, Image off, int width, int height, String commandOn, String commandOff ) {
		Image onImage = JetpImageUtil.toBufferedImage( on ).getScaledInstance( width, height, Image.SCALE_SMOOTH );
		Image offImage = JetpImageUtil.toBufferedImage( off ).getScaledInstance( width, height, Image.SCALE_SMOOTH );
		
		int[] rgbOn = JetpImageUtil.getRGBArray( JetpImageUtil.toBufferedImage( onImage ) );
		int[] rgbOff = JetpImageUtil.getRGBArray( JetpImageUtil.toBufferedImage( offImage ) );
		
		return new RadioIconSwitch( rgbOn, rgbOff, width, commandOn, commandOff );
	}
	
	public static RadioIconImage constructImage( Image image, int width, int height ) {
		Image resized = JetpImageUtil.toBufferedImage( image ).getScaledInstance( width, height, Image.SCALE_SMOOTH );
		
		int[] rgb = JetpImageUtil.getRGBArray( JetpImageUtil.toBufferedImage( resized ) );
		
		return new RadioIconImage( rgb, width );
	}
	
	public static RadioIconButton constructButton( Image clicked, Image unclicked, int width, int height, String command, long buttonDelay ) {
		Image onImage = JetpImageUtil.toBufferedImage( clicked ).getScaledInstance( width, height, Image.SCALE_SMOOTH );
		Image offImage = JetpImageUtil.toBufferedImage( unclicked ).getScaledInstance( width, height, Image.SCALE_SMOOTH );
		
		int[] rgbClicked = JetpImageUtil.getRGBArray( JetpImageUtil.toBufferedImage( onImage ) );
		int[] rgbUnclicked = JetpImageUtil.getRGBArray( JetpImageUtil.toBufferedImage( offImage ) );
		
		return new RadioIconButton( rgbUnclicked, rgbClicked, width, command, buttonDelay );
	}
	
	public static RadioIconGif constructGif( File file, int width, int height ) {
		return new RadioIconGif( file, width, height );
	}
	
	public static RadioIconCloud constructCloud( int width, int height, int transparency, int delay ) {
		return new RadioIconCloud( width, height, transparency, delay );
	}
}
