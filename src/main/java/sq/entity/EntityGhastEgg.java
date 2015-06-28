package sq.entity;

import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import radixcore.math.Point3D;
import radixcore.util.RadixMath;
import sq.core.minecraft.ModItems;

public class EntityGhastEgg extends EntitySpiderEgg
{
	private UUID	owner;
	private int		timeUntilEggHatch;

	public EntityGhastEgg(World world)
	{
		super(world);
		setSize(0.15F, 0.15F);
	}

	public EntityGhastEgg(World world, UUID owner)
	{
		super(world);
		this.owner = owner;
		this.renderDistanceWeight = 50.0F;
		setSize(0.15F, 0.15F);
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.0D);
		timeUntilEggHatch = RadixMath.getNumberInRange(500, 5000);
	}

	@Override
	public boolean attackEntityFrom(DamageSource damageSource, float damage)
	{
		final Entity entity = damageSource.getEntity();

		if (entity instanceof EntityPlayer && !entity.worldObj.isRemote)
		{
			setDead();
			entityDropItem(new ItemStack(ModItems.ghastEgg), entity.worldObj.rand.nextFloat());
		}

		return true;
	}

	@Override
	protected void doHatch(EntitySpiderEx spider)
	{
		try
		{
			final EntityPlayer player = worldObj.func_152378_a(owner);

			final Block spawnBlock = worldObj.getBlock((int)posX,(int)posY,(int)posZ);
			Point3D spawnPoint = new Point3D(posX, posY, posZ);

			if (worldObj.getBlock(spawnPoint.iPosX + 1, spawnPoint.iPosY, spawnPoint.iPosZ) == Blocks.air) { spawnPoint = new Point3D(posX + 1, posY, posZ); }
			else if (worldObj.getBlock(spawnPoint.iPosX, spawnPoint.iPosY, spawnPoint.iPosZ + 1) == Blocks.air) { spawnPoint = new Point3D(posX, posY, posZ + 1); }
			else if (worldObj.getBlock(spawnPoint.iPosX - 1, spawnPoint.iPosY, spawnPoint.iPosZ) == Blocks.air) { spawnPoint = new Point3D(posX - 1, posY, posZ); }
			else if (worldObj.getBlock(spawnPoint.iPosX, spawnPoint.iPosY, spawnPoint.iPosZ - 1) == Blocks.air) { spawnPoint = new Point3D(posX, posY, posZ - 1); }

			EntityMiniGhast ghast = new EntityMiniGhast(worldObj, owner);
			ghast.setLocationAndAngles(spawnPoint.iPosX, spawnPoint.iPosY, spawnPoint.iPosZ, rotationYaw, rotationPitch);
			worldObj.spawnEntityInWorld(ghast);
			setDead();
		}

		catch (NullPointerException e)
		{
			//Happens when player is null - not logged in.
		}
	}
}