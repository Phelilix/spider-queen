package sq.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

/**
 * Stores extra data about creatures that can have a reputation with the player.
 */
public class RepEntityExtension implements IExtendedEntityProperties
{
	public static final String ID = "SpiderQueenRepEntityExtension";
	private int timesHitByPlayer;

	public RepEntityExtension(Entity entity)
	{
	}

	public int getTimesHitByPlayer()
	{
		return timesHitByPlayer;
	}

	public void setTimesHitByPlayer(int value)
	{
		timesHitByPlayer = value;
	}

	@Override
	public void saveNBTData(NBTTagCompound nbt) 
	{
		nbt.setInteger("timesHitByPlayer", timesHitByPlayer);
	}

	@Override
	public void loadNBTData(NBTTagCompound nbt) 
	{
		timesHitByPlayer = nbt.getInteger("timesHitByPlayer");
	}

	@Override
	public void init(Entity entity, World world) 
	{
	}

	public static final void register(Entity entity)
	{
		entity.registerExtendedProperties(ID, new RepEntityExtension(entity));
	}
}
