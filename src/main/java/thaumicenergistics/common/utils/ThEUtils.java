package thaumicenergistics.common.utils;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumicenergistics.common.network.packet.client.Packet_C_Sync;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Houses commonly used methods.
 * 
 * @author Nividica
 * 
 */
public final class ThEUtils
{
	/**
	 * The squared reach distance.
	 */
	private static final double SQUARED_REACH = 64.0D;

	/**
	 * Plays a sound for this player.
	 */
	@SideOnly(Side.CLIENT)
	private static final void playLocalSound( final ResourceLocation sound )
	{
		Minecraft.getMinecraft().getSoundHandler()
						.playSound( PositionedSoundRecord.func_147674_a( sound, 1.0F ) );
	}

	/**
	 * Returns true if the tile still exists and the player is within reach range.
	 * 
	 * @param player
	 * @param tile
	 * @return
	 */
	public static final boolean canPlayerInteractWith( @Nonnull final EntityPlayer player, @Nonnull final TileEntity tile )
	{
		return ( tile.getWorldObj().getTileEntity( tile.xCoord, tile.yCoord, tile.zCoord ) == tile )
						&& ( player.getDistanceSq( tile.xCoord + 0.5D, tile.yCoord + 0.5D, tile.zCoord + 0.5D ) <= SQUARED_REACH );

	}

	/**
	 * Returns the value to which the specified key is mapped, or defaultValue if the map contains no mapping for the key.
	 * <hr>
	 * NOTE: This is included for java 7 compatibility.
	 * <hr>
	 * 
	 * @param map
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static final <K, V> V getOrDefault( final Map<K, V> map, final K key, final V defaultValue )
	{
		if( map.containsKey( key ) )
		{
			return map.get( key );
		}

		return defaultValue;
	}

	/**
	 * Returns true if the item stack is a wand, scepter, or staff if they are
	 * allowed.
	 * 
	 * @param stack
	 * @param allowStaves
	 * @return
	 */
	public static final boolean isItemValidWand( final ItemStack stack, final boolean allowStaves )
	{
		Item potentialWand;

		// Ensure it is not null
		if( ( stack == null ) || ( ( potentialWand = stack.getItem() ) == null ) )
		{
			return false;
		}

		// Ensure it is a casting wand
		if( !( potentialWand instanceof ItemWandCasting ) )
		{
			return false;
		}

		// Ensure it is not a staff
		if( ( !allowStaves ) && ( ( (ItemWandCasting)potentialWand ).isStaff( stack ) ) )
		{
			return false;
		}

		// Get the wand
		ItemWandCasting wand = (ItemWandCasting)stack.getItem();

		// Validate internals
		try
		{
			wand.getAspectsWithRoom( stack );
		}
		catch( Exception e )
		{
			// Internal failure
			return false;
		}

		// Valid wand
		return true;
	}

	/**
	 * Ping pongs a value back and forth from min -> max -> min.
	 * The base speed of this effect is 1 second per transition, 2 seconds
	 * total.
	 * 
	 * @param speedReduction
	 * The higher this value, the slower the effect. The smaller this value, the
	 * faster the effect.
	 * PingPong time (1) = 2 Seconds; (0.5) = 1 Second; (2) = 4 Seconds;
	 * @param minValue
	 * @param maxValue
	 * @return
	 */
	public static final float pingPongFromTime( double speedReduction, final float minValue, final float maxValue )
	{
		// Sanity check for situations like pingPongFromTime( ?, 1.0F, 1.0F )
		if( minValue == maxValue )
		{
			return minValue;
		}

		// Bounds check speed reduction
		if( speedReduction <= 0 )
		{
			speedReduction = Float.MIN_VALUE;
		}

		// Get the time modulated to 2000, then reduced
		float time = (float)( ( System.currentTimeMillis() / speedReduction ) % 2000.F );

		// Offset by -1000 and take the abs
		time = Math.abs( time - 1000.0F );

		// Convert time to a percentage
		float timePercentage = time / 1000.0F;

		// Get the position in the range we are now at
		float rangePercentage = ( maxValue - minValue ) * timePercentage;

		// Add the range position back to min
		return minValue + rangePercentage;
	}

	/**
	 * Plays a sound only the specified player can hear.
	 * 
	 * @param player
	 * Can be null on client side.
	 * @param soundLocation
	 */
	public static final void playClientSound( @Nullable final EntityPlayer player, final String soundLocation )
	{
		// Ensure there is a sound
		if( ( soundLocation == null ) || ( soundLocation == "" ) )
		{
			return;
		}

		if( EffectiveSide.isClientSide() )
		{
			// Create the resource
			ResourceLocation sound = new ResourceLocation( soundLocation );

			// Play the sound
			playLocalSound( sound );
		}
		else
		{
			// Send to the player
			if( player != null )
			{
				Packet_C_Sync.sendPlaySound( player, soundLocation );
			}
		}
	}
}
