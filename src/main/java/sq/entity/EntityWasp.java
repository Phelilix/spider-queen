package sq.entity;

import net.minecraft.world.World;

public class EntityWasp extends AbstractFlyingMob
{
	public EntityWasp(World world) 
	{
		super(world, "wasp");
		setSize(1.2F, 1.5F);
	}

	@Override
	public void onUpdate() 
	{
		super.onUpdate();

		if (entityToAttack != null)
		{
			double sqDistanceTo = Math.sqrt(Math.pow(entityToAttack.posX - posX, 2) + Math.pow(entityToAttack.posZ - posZ, 2));
			float moveAmount = 0.0F;
			
			if(sqDistanceTo < 8F) 
			{ 
				moveAmount = ((8F - (float)sqDistanceTo) / 8F)*4F; 
			}
			
			if (entityToAttack.posY + 0.2F < posY)
			{
				motionY = motionY - 0.05F * moveAmount;
			}
			
			if(entityToAttack.posY - 0.5F > posY)
			{
				motionY = motionY + 0.01F * moveAmount;
			}
		}
	}

	@Override
	public boolean isAIEnabled() 
	{
		return false;
	}
	
	@Override
	public float getMobMaxHealth() 
	{
		return 20.0F;
	}

	@Override
	public float getHitDamage() 
	{
		return 4.0F;
	}

	@Override
	public double getMoveSpeed() 
	{
		return 0.9F;
	}
	
	@Override
	public boolean isPassive()
	{
		return false;
	}
	
	@Override
	public boolean getCanSpawnHere() 
	{
		return true;
	}
}

