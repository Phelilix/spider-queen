package sq.entity;

import net.minecraft.entity.EntityCreature;
import net.minecraft.world.World;

public class EntitySpiderQueen extends EntityCreature implements IWebClimber
{
	public EntitySpiderQueen(World world) 
	{
		super(world);
	}
}
