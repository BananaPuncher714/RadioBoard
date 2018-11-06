package io.github.bananapuncher714.radioboard.implementation.v1_11_R1;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.map.MapView;

import io.github.bananapuncher714.radioboard.RadioBoard;
import io.github.bananapuncher714.radioboard.api.PacketHandler;
import io.netty.channel.Channel;
import net.minecraft.server.v1_11_R1.MapIcon;
import net.minecraft.server.v1_11_R1.PacketPlayOutMap;
import net.minecraft.server.v1_11_R1.PlayerConnection;

public class NMSHandler implements PacketHandler {
	public static final int パケットの限界 = 0;

	private static Field[] 地図の変数 = new Field[ 9 ];

	static {
		try {
			地図の変数[ 0 ] = PacketPlayOutMap.class.getDeclaredField( "a" );
			地図の変数[ 1 ] = PacketPlayOutMap.class.getDeclaredField( "b" );
			地図の変数[ 2 ] = PacketPlayOutMap.class.getDeclaredField( "c" );
			地図の変数[ 3 ] = PacketPlayOutMap.class.getDeclaredField( "d" );
			地図の変数[ 4 ] = PacketPlayOutMap.class.getDeclaredField( "e" );
			地図の変数[ 5 ] = PacketPlayOutMap.class.getDeclaredField( "f" );
			地図の変数[ 6 ] = PacketPlayOutMap.class.getDeclaredField( "g" );
			地図の変数[ 7 ] = PacketPlayOutMap.class.getDeclaredField( "h" );
			地図の変数[ 8 ] = PacketPlayOutMap.class.getDeclaredField( "i" );

			for ( Field 変数 : 地図の変数 ) {
				変数.setAccessible( true );
			}
		} catch ( Exception 過失 ) {
			過失.printStackTrace();
		}
	}
	
	private class PacketPlayOutRadioMap extends PacketPlayOutMap {
		protected final PacketPlayOutMap packet;
		
		protected PacketPlayOutRadioMap( PacketPlayOutMap packet ) {
			this.packet = packet;
		}
	}

	private final Map< UUID, PlayerConnection > 選手接続リスト = new ConcurrentHashMap< UUID, PlayerConnection >();
	private final Map< UUID, Long > lastUpdated = new ConcurrentHashMap< UUID, Long >();
	private final boolean[] 地図リスト = new boolean[ Short.MAX_VALUE ];
	
	@Override
	public Object onPacketInterceptOut( Player viewer, Object packet ) {
		if ( packet instanceof PacketPlayOutRadioMap ) {
			return ( ( PacketPlayOutRadioMap ) packet ).packet;
		} else if ( packet instanceof PacketPlayOutMap ) {
			try {
				int id = 地図の変数[ 0 ].getInt( packet );
				if ( 地図リスト[ id ] ) {
					return null;
				}
			} catch ( IllegalArgumentException | IllegalAccessException e ) {
				e.printStackTrace();
			}
		}
		return packet;
	}
	
	@Override
	public Object onPacketInterceptIn( Player viewer, Object packet ) {
		return packet;
	}
	
	/**
	 * 布団好きです
	 */
	@Override
	public void display( UUID[] 氏名リスト, int 地図冒頭, int 地図の幅, int 地図の丈, byte[] 絵の具, int ビデオの幅 ) {
		int ビデオの丈 = 絵の具.length / ビデオの幅;
		int ピクセルの丈 = 地図の丈 << 7;
		int ピクセルの幅 = 地図の幅 << 7;
		int ｘの相殺 = ( ピクセルの幅 - ビデオの幅 ) >> 1;
		int ｙの相殺 = ( ピクセルの丈 - ビデオの丈 ) >> 1;
		display( 氏名リスト, 地図冒頭, 地図の幅, 地図の丈, 絵の具, ビデオの幅, ｘの相殺, ｙの相殺 );
	}

	@Override
	public void display( UUID[] 氏名リスト, int 地図冒頭, int 地図の幅, int 地図の丈, byte[] 絵の具, int ビデオの幅, int ｘの相殺, int ｙの相殺 ) {
		// First get the full height of the video
		int vidHeight = 絵の具.length / ビデオの幅;
		// Get the entire width in pixels of the canvas
		int pixW = 地図の幅 << 7;
		// Find the left-most and bottom-most coordinates of the picture
		int negXOff = ｘの相殺 + ビデオの幅;
		int negYOff = ｙの相殺 + vidHeight;
		// Get the difference between the width of the canvas and the actual width of the video, *should* be positive
		int xDif = pixW - ビデオの幅;
		int top = ｙの相殺 * pixW + ｘの相殺;
		int mapWidth = ( int ) Math.min( 地図の幅, Math.ceil( negXOff / 128.0 ) );
		int mapHeight = ( int ) Math.min( 地図の丈, Math.ceil( negYOff / 128.0 ) );

		PacketPlayOutMap[] パケットリスト = new PacketPlayOutMap[ ( int ) ( ( mapWidth - Math.max( 0, ｘの相殺 >> 7 ) ) * ( mapHeight - Math.max( 0, ｙの相殺 >> 7 ) ) ) ];
		int arrIndex = 0;

		for ( int x = Math.max( 0, ｘの相殺 >> 7 ); x < mapWidth; x++ ) {
			int relX = x << 7;
			for ( int y = Math.max( 0, ｙの相殺 >> 7 ); y < mapHeight; y++ ) {
				int relY = y << 7;

				int topX = Math.max( 0, ｘの相殺 - relX );
				int topY = Math.max( 0, ｙの相殺 - relY );
				int xDiff = Math.min( 128 - topX, negXOff - ( relX + topX ) );
				int yDiff = Math.min( 128 - topY, negYOff - ( relY + topY ) );

				byte[] mapData = new byte[ xDiff * yDiff ];
				for ( int ix = topX; ix < xDiff + topX; ix++ ) {
					int xPos = relX + ix;
					for ( int iy = topY; iy < yDiff + topY; iy++ ) {
						int yPos = relY + iy;
						int normalizedSlot = ( yPos * pixW + xPos ) - top;
						int index = normalizedSlot - ( int ) ( Math.floor( normalizedSlot / pixW ) * xDif );
						int val = ( iy - topY ) * xDiff + ix - topX;
						mapData[ val ] = 絵の具[ index ];
					}
				}

				int 新地図冒頭 = 地図冒頭 + 地図の幅 * y + x;
				PacketPlayOutMap パケット = new PacketPlayOutMap();

				try {
					地図の変数[ 0 ].set( パケット, 新地図冒頭 );
					地図の変数[ 1 ].set( パケット, ( byte ) 0 );
					地図の変数[ 2 ].set( パケット, false );
					地図の変数[ 3 ].set( パケット, new MapIcon[ 0 ] );
					地図の変数[ 4 ].set( パケット, topX );
					地図の変数[ 5 ].set( パケット, topY );
					地図の変数[ 6 ].set( パケット, xDiff );
					地図の変数[ 7 ].set( パケット, yDiff );
					地図の変数[ 8 ].set( パケット, mapData );
				} catch ( Exception 過失 ) {
					過失.printStackTrace();
				}

				パケットリスト[ arrIndex++ ] = new PacketPlayOutRadioMap( パケット );
			}
		}
		
		if ( 氏名リスト == null ) {
			for ( UUID 氏名 : 選手接続リスト.keySet() ) {
				Object val = lastUpdated.get( 氏名 );
				if ( val == null || System.currentTimeMillis() - ( long ) val > パケットの限界 ) {
					lastUpdated.put( 氏名, System.currentTimeMillis() );
					PlayerConnection connection = 選手接続リスト.get( 氏名 );
					Channel channel = RadioBoard.getInstance().getProtocol().getChannel( 氏名, connection );
					for ( PacketPlayOutMap パケット : パケットリスト ) {
						RadioBoard.getInstance().getProtocol().sendPacket( channel, パケット );
					}
				}
			}
		} else {
			for ( UUID 氏名 : 氏名リスト ) {
				Object val = lastUpdated.get( 氏名 );
				if ( val == null || System.currentTimeMillis() - ( long ) val > パケットの限界 ) {
					lastUpdated.put( 氏名, System.currentTimeMillis() );
					PlayerConnection connection = 選手接続リスト.get( 氏名 );
					if ( connection != null ) {
						Channel channel = RadioBoard.getInstance().getProtocol().getChannel( 氏名, connection );
						for ( PacketPlayOutMap パケット : パケットリスト ) {
							RadioBoard.getInstance().getProtocol().sendPacket( channel, パケット );
						}
					}
				}
			}
		}
	}

	@Override
	public void registerPlayer( Player 選手 ) {
		選手接続リスト.put( 選手.getUniqueId(), ( ( CraftPlayer ) 選手 ).getHandle().playerConnection );
	}

	@Override
	public void unregisterPlayer( UUID 身元 ) {
		選手接続リスト.remove( 身元 );
		RadioBoard.getInstance().getProtocol().removeChannel( 身元 );
	}

	@Override
	public boolean isMapRegistered( int 身元 ) {
		return 地図リスト[ 身元 ];
	}
	
	@Override
	public void registerMap( int 身元 ) {
		地図リスト[ 身元 ] = true;
		MapView view = Bukkit.getMap( ( short ) 身元 );
		if ( view != null ) {
			view.getRenderers().clear();
		}
	}

	@Override
	public void unregisterMap( int 身元 ) {
		地図リスト[ 身元 ] = false;
	}
}
