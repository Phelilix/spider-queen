package sq.entity.friendly;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAICreeperSwell;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import radixcore.constant.Time;
import radixcore.util.RadixMath;
import sq.core.ReputationHandler;
import sq.core.minecraft.ModItems;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * The friendly creeper is gifted to the player with a high creeper reputation. 
 * By gifting the friendly creeper a heart, the player will receive gunpowder.
 */
public class EntityFriendlyCreeper extends EntityCreeper implements IFriendlyEntity
{
	private int timeUntilSpeak = Time.MINUTE * 5;
	private UUID friendPlayerUUID = new UUID(0, 0);;
	private boolean hasPlayedSound;
	private boolean isImprisoned;

	public EntityLivingBase target;

	public EntityFriendlyCreeper(World world)
	{
		super(world);

		dataWatcher.addObject(23, 0);
	}

	public EntityFriendlyCreeper(World world, EntityPlayer friendPlayer)
	{
		this(world);
		friendPlayerUUID = friendPlayer.getUniqueID();

		//Clear old task entries.
		tasks.taskEntries.clear();
		targetTasks.taskEntries.clear();

		//Add custom tasks.
		tasks.addTask(1, new EntityAISwimming(this));
		tasks.addTask(2, new EntityAICreeperSwell(this));
		tasks.addTask(3, new EntityAIAvoidEntity(this, EntityOcelot.class, 6.0F, 1.0D, 1.2D));
		tasks.addTask(4, new EntityAIAttackOnCollide(this, 1.0D, false));
		tasks.addTask(5, new EntityAIWander(this, 0.8D));
		tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		tasks.addTask(6, new EntityAILookIdle(this));
		targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
		targetTasks.addTask(2, new EntityAIHurtByTarget(this, false));
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		FriendlyEntityHelper.onUpdate(this);

		if (!worldObj.isRemote)
		{
			if (target != null && RadixMath.getDistanceToEntity(this, target) <= 2)
			{
				if (!hasPlayedSound)
				{
					playSound("creeper.primed", 1.0F, 0.5F);
					hasPlayedSound = true;
				}

				setExplosionTicks(getExplosionTicks() + 1);

				if (getExplosionTicks() >= 40)
				{
					worldObj.createExplosion(this, posX, posY, posZ, 2, false);
					setDead();
				}
			}

			else if (target == null || RadixMath.getDistanceToEntity(this, target) > 2)
			{
				setExplosionTicks(0);
				hasPlayedSound = false;
			}
		}
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.55D);
	}

	@Override
	public UUID getFriendPlayerUUID()
	{
		return friendPlayerUUID;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getCreeperFlashIntensity(float partialTickTime)
	{
		return getExplosionTicks() / 32.0F * partialTickTime;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) 
	{
		super.writeEntityToNBT(nbt);

		nbt.setLong("friendPlayerUUID-lsb", friendPlayerUUID.getLeastSignificantBits());
		nbt.setLong("friendPlayerUUID-msb", friendPlayerUUID.getMostSignificantBits());
		nbt.setBoolean("isImprisoned", isImprisoned);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);

		friendPlayerUUID = new UUID(nbt.getLong("friendPlayerUUID-msb"), nbt.getLong("friendPlayerUUID-lsb"));
		isImprisoned = nbt.getBoolean("isImprisoned");
	}

	@Override
	public boolean attackEntityFrom(DamageSource damageSource, float damageAmount)
	{
		super.attackEntityFrom(damageSource, damageAmount);
		return FriendlyEntityHelper.attackEntityFrom(this, damageSource, damageAmount);
	}

	@Override
	public boolean interact(EntityPlayer entity) 
	{
		final ItemStack heldItem = entity.inventory.getCurrentItem();

		if (isImprisoned)
		{
			ReputationHandler.handleInteractWithImprisoned(entity, this);
		}

		if (heldItem != null && heldItem.getItem() == ModItems.heart)
		{
			heldItem.stackSize--;

			if (!entity.worldObj.isRemote)
			{
				dropItem(Items.gunpowder, 10);
			}
		}

		return super.interact(entity);
	}

	@Override
	public EntityCreature getInstance() 
	{
		return this;
	}

	@Override
	public EntityLivingBase getTarget() 
	{
		return target;
	}

	@Override
	public void setTarget(EntityLivingBase target) 
	{
		this.target = target;
	}

	@Override
	public boolean doManualAttack(Entity entityBeingAttacked, float damageAmount) 
	{
		return false;
	}

	@Override
	protected boolean canDespawn() 
	{
		return false;
	}

	public int getExplosionTicks()
	{
		return dataWatcher.getWatchableObjectInt(23);
	}

	public void setExplosionTicks(int value)
	{
		dataWatcher.updateObject(23, value);
	}

	@Override
	public void setFriendPlayerUUID(UUID value) 
	{
		friendPlayerUUID = value;
	}

	@Override
	public int getTimeUntilSpeak() 
	{
		return timeUntilSpeak;
	}

	@Override
	public void setTimeUntilSpeak(int value) 
	{
		timeUntilSpeak = value;
	}

	@Override
	public String getSpeakId() 
	{
		return "creeper";
	}

	@Override
	public boolean isImprisoned() 
	{
		return isImprisoned;
	}

	@Override
	public void setImprisoned(boolean value) 
	{
		isImprisoned = value;
	}

	@Override
	public Class getNonFriendlyClass() 
	{
		return EntityCreeper.class;
	}

	@Override
	public String getCommandSenderName() 
	{
		return "Creeper";
	}
}