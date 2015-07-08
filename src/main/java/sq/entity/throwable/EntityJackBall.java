package sq.entity.throwable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

/**
 * The jack ball is the projectile thrown by Jack. It injures the player for 6 points.
 */
public class EntityJackBall extends EntityThrowable
{
	private EntityLivingBase shooter;

	public EntityJackBall(World world) 
	{
		super(world);
	}

	public EntityJackBall(World world, EntityLivingBase shooter, EntityLivingBase target, float speed, float unknown)
	{
		this(world);
		this.shooter = shooter;
		renderDistanceWeight = 10.0D;

		posY = shooter.posY + shooter.getEyeHeight() - 0.10000000149011612D;
		final double d0 = target.posX - shooter.posX;
		final double d1 = target.boundingBox.minY + target.height / 3.0F - posY;
		final double d2 = target.posZ - shooter.posZ;
		final double d3 = MathHelper.sqrt_double(d0 * d0 + d2 * d2);

		if (d3 >= 1.0E-7D)
		{
			final float f2 = (float)(Math.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
			final float f3 = (float)-(Math.atan2(d1, d3) * 180.0D / Math.PI);
			final double d4 = d0 / d3;
			final double d5 = d2 / d3;
			setLocationAndAngles(shooter.posX + d4, posY, shooter.posZ + d5, f2, f3);
			yOffset = 0.0F;
			final float f4 = (float)d3 * 0.2F;
			setThrowableHeading(d0, d1 + f4, d2, speed, unknown);
		}
	}

	@Override
	protected void onImpact(MovingObjectPosition objectPosition) 
	{
		if (objectPosition.entityHit instanceof EntityLivingBase && objectPosition.entityHit != shooter)
		{
			final EntityLivingBase entityHit = (EntityLivingBase)objectPosition.entityHit;
			entityHit.attackEntityFrom(DamageSource.causeMobDamage(shooter), 6.0F);
			setDead();
		}
	}
}
