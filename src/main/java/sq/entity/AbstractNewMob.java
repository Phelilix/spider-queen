package sq.entity;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import radixcore.util.RadixString;
import sq.entity.ai.AIAttackPlayerOnUnlike;
import sq.entity.creature.EntityHuman;

/**
 * Defines a general "new" mob that can be hostile to players.
 */
public abstract class AbstractNewMob extends EntityMob 
{
	private final String codeName;

	public AbstractNewMob(World world, String codeName)
	{
		super(world);
		this.codeName = codeName;

		getNavigator().setAvoidsWater(false);
		tasks.addTask(1, new EntityAIWander(this, getMoveSpeed()));
		tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		tasks.addTask(3, new EntityAILookIdle(this));

		if (!isPassive())
		{
			tasks.addTask(4, new EntityAIAttackOnCollide(this, getMoveSpeed(), false));

			if (this instanceof IRep)
			{
				targetTasks.addTask(1, new AIAttackPlayerOnUnlike(this));
			}

			else
			{
				targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
			}

			targetTasks.addTask(2, new EntityAIHurtByTarget(this, false));
			targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityHuman.class, 0, true));
		}

		appendAI();
	}

	@Override
	protected final void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(40.0D);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(getMoveSpeed());
		getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(getHitDamage());
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(getMobMaxHealth());
	}

	public abstract float getMobMaxHealth();

	public abstract float getHitDamage();

	public abstract double getMoveSpeed();

	@Override
	public boolean isAIEnabled()
	{
		return true;
	}

	@Override
	protected String getHurtSound() 
	{
		return "sq:" + codeName + ".hurt";
	}

	@Override
	protected String getDeathSound() 
	{
		return "sq:" + codeName + ".death";
	}

	@Override
	protected String getLivingSound()
	{
		return "sq:" + codeName + ".idle";
	}

	@Override
	public String getCommandSenderName() 
	{
		return RadixString.upperFirstLetter(codeName);
	}

	public void appendAI()
	{

	}

	public abstract boolean isPassive();
}
