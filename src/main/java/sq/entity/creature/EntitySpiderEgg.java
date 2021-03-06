package sq.entity.creature;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.Achievement;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import radixcore.math.Point3D;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;
import sq.core.minecraft.ModAchievements;
import sq.core.minecraft.ModItems;
import sq.enums.EnumSpiderType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * The spider egg is the entity placed by using the spider egg item on a block. It will hatch into a spider after
 * a random amount of time. Depending on any cocoons nearby, the type of spider that will hatch will change.
 */
public class EntitySpiderEgg extends EntityCreature
{
	private UUID	owner = new UUID(0, 0);
	private int		timeUntilEggHatch;

	public EntitySpiderEgg(World world)
	{
		super(world);
		setSize(0.15F, 0.15F);
	}

	public EntitySpiderEgg(World world, UUID owner)
	{
		super(world);
		this.owner = owner;
		renderDistanceWeight = 50.0F;
		setSize(0.15F, 0.15F);
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.0D);
		timeUntilEggHatch = RadixMath.getNumberInRange(500, 5000);
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
	}

	@Override
	public boolean isAIEnabled()
	{
		return false;
	}

	@Override
	protected boolean isMovementCeased()
	{
		return true;
	}

	@Override
	public AxisAlignedBB getCollisionBox(Entity entity)
	{
		return entity.boundingBox;
	}

	@Override
	public AxisAlignedBB getBoundingBox()
	{
		return boundingBox;
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return true;
	}

	@Override
	public boolean canBePushed()
	{
		return true;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if (!worldObj.isRemote)
		{
			if (timeUntilEggHatch <= 0)
			{
				final EntityCocoon cocoonToConsume = getConsumableCocoon();
				final EntitySpiderEx spider = consumeCocoon(cocoonToConsume);
				doHatch(spider);
			}

			else
			{
				timeUntilEggHatch--;
			}
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource damageSource, float damage)
	{
		final Entity entity = damageSource.getEntity();

		if (entity instanceof EntityPlayer && !entity.worldObj.isRemote)
		{
			setDead();
			entityDropItem(new ItemStack(ModItems.spiderEgg), entity.worldObj.rand.nextFloat());
		}

		return true;
	}

	@Override
	protected boolean canDespawn()
	{
		return false;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) 
	{
		super.writeEntityToNBT(nbt);
		nbt.setInteger("timeUntilEggHatch", timeUntilEggHatch);
		nbt.setLong("ownerMSB", owner.getMostSignificantBits());
		nbt.setLong("ownerLSB", owner.getLeastSignificantBits());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) 
	{
		super.readEntityFromNBT(nbt);
		timeUntilEggHatch = nbt.getInteger("timeUntilEggHatch");
		owner = new UUID(nbt.getLong("ownerMSB"), nbt.getLong("ownerLSB"));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double distance)
	{
		final int sqrtDistance = (int)Math.sqrt(distance);
		return sqrtDistance < 50;
	}

	private EntityCocoon getConsumableCocoon()
	{
		//Search for cocoons up to 5 blocks away. Return the nearest one that is not eaten.
		final List<Entity> nearbyCocoons = RadixLogic.getAllEntitiesOfTypeWithinDistance(EntityCocoon.class, this, 5);
		EntityCocoon nearestCocoon = null;
		double lowestDistance = 100D;

		for (final Entity entity : nearbyCocoons)
		{
			final EntityCocoon cocoon = (EntityCocoon)entity;
			final double distanceToCurrentEntity = RadixMath.getDistanceToEntity(this, cocoon);

			if (!cocoon.isEaten() && distanceToCurrentEntity < lowestDistance)
			{
				lowestDistance = distanceToCurrentEntity;
				nearestCocoon = cocoon;
			}
		}

		return nearestCocoon;
	}

	private EntitySpiderEx consumeCocoon(EntityCocoon cocoonToConsume)
	{
		EntitySpiderEx spider;

		//No cocoon, so the spider is a wimpy spider.
		if (cocoonToConsume == null)
		{
			spider = new EntitySpiderEx(worldObj, owner, EnumSpiderType.WIMPY);
		}

		else
		{
			//There is a cocoon, so look up the spider type that this cocoon yields.
			cocoonToConsume.setEaten(true);
			spider = new EntitySpiderEx(worldObj, owner, cocoonToConsume.getCocoonType().getSpiderTypeYield());
		}

		return spider;
	}

	protected void doHatch(EntitySpiderEx spider)
	{
		try
		{
			final EntityPlayer player = worldObj.func_152378_a(owner);

			worldObj.getBlock((int)posX,(int)posY,(int)posZ);
			Point3D spawnPoint = new Point3D(posX, posY, posZ);

			if (worldObj.getBlock(spawnPoint.iPosX + 1, spawnPoint.iPosY, spawnPoint.iPosZ) == Blocks.air) { spawnPoint = new Point3D(posX + 1, posY, posZ); }
			else if (worldObj.getBlock(spawnPoint.iPosX, spawnPoint.iPosY, spawnPoint.iPosZ + 1) == Blocks.air) { spawnPoint = new Point3D(posX, posY, posZ + 1); }
			else if (worldObj.getBlock(spawnPoint.iPosX - 1, spawnPoint.iPosY, spawnPoint.iPosZ) == Blocks.air) { spawnPoint = new Point3D(posX - 1, posY, posZ); }
			else if (worldObj.getBlock(spawnPoint.iPosX, spawnPoint.iPosY, spawnPoint.iPosZ - 1) == Blocks.air) { spawnPoint = new Point3D(posX, posY, posZ - 1); }

			spider.setLocationAndAngles(spawnPoint.iPosX, spawnPoint.iPosY, spawnPoint.iPosZ, rotationYaw, rotationPitch);

			//Spawn the spider and remove the egg.
			worldObj.spawnEntityInWorld(spider);
			setDead();

			//Trigger needed achievements.
			if (player != null)
			{
				if (spider.getSpiderType() == EnumSpiderType.WIMPY)
				{
					player.triggerAchievement(ModAchievements.hatchSpider);
				}

				else
				{
					player.triggerAchievement(ModAchievements.hatchSpiderByCocoon);

					Achievement specialAchievement = null;

					switch (spider.getSpiderType())
					{
					case BOOM: specialAchievement = ModAchievements.hatchBoomSpider;
					break;
					case ENDER: specialAchievement = ModAchievements.hatchEnderSpider;
					break;
					case NOVA: specialAchievement = ModAchievements.hatchNovaSpider;
					break;
					case PACK: specialAchievement = ModAchievements.hatchPackSpider;
					break;
					case RIDER: specialAchievement = ModAchievements.hatchRiderSpider;
					break;
					case SLINGER: specialAchievement = ModAchievements.hatchSlingerSpider;
					break;
					case TANK: specialAchievement = ModAchievements.hatchTankSpider;
					break;
					default:
						break;

					}

					if (specialAchievement != null)
					{
						player.triggerAchievement(specialAchievement);
					}
				}
			}
		}

		catch (final NullPointerException e)
		{
			//Happens when player is null - not logged in.
		}
	}
}