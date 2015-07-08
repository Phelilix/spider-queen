package sq.entity.throwable;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import radixcore.constant.Particle;
import radixcore.constant.Time;
import radixcore.math.Point3D;
import radixcore.util.RadixLogic;
import sq.util.Utils;

/**
 * The freeze ball is a projectile that freezes a creature on impact with it. It also freezes water and douses flames.
 */
public class EntityFreezeBall extends EntityThrowable
{
	private EntityLivingBase shooter;

	public EntityFreezeBall(World world) 
	{
		super(world);
	}

	public EntityFreezeBall(World world, EntityPlayer shooter)
	{
		this(world);
		final Vec3 vec = shooter.getLookVec();

		this.shooter = shooter;
		renderDistanceWeight = 10.0D;
		setPosition(shooter.posX + vec.xCoord * 2, shooter.posY + 1 + vec.yCoord * 2, shooter.posZ + vec.zCoord * 2);
		motionX = vec.xCoord * 1.5D;
		motionY = vec.yCoord * 1.5D;
		motionZ = vec.zCoord * 1.5D;
	}

	public EntityFreezeBall(World world, EntityLivingBase shooter, EntityLivingBase target, float speed, float unknown)
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
			entityHit.attackEntityFrom(DamageSource.inWall, 6.0F);

			worldObj.playSoundAtEntity(shooter, "fireworks.largeBlast", 1.0F, 1.0F);
			worldObj.playSoundAtEntity(entityHit, "fireworks.largeBlast", 1.0F, 1.0F);

			Utils.spawnParticlesAroundEntityS(Particle.SNOWBALL, entityHit, 16);

			for (final Point3D point : RadixLogic.getNearbyBlocks(entityHit, Blocks.grass, 1))
			{
				worldObj.setBlock(point.iPosX, point.iPosY + 1, point.iPosZ, Blocks.snow_layer);
			}

			entityHit.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, Time.SECOND * 5, 5));
			entityHit.setPosition(Math.floor(entityHit.posX) + 0.5F, Math.floor(entityHit.posY), Math.floor(entityHit.posZ) + 0.5F);
			worldObj.setBlock((int)entityHit.posX, (int)entityHit.posY, (int)entityHit.posZ, Blocks.ice);
			worldObj.setBlock((int)entityHit.posX, (int)entityHit.posY + 1, (int)entityHit.posZ, Blocks.ice);
		}

		for (int i = -2; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				for (int k = -2; k < 2; k++)
				{
					final Block block = worldObj.getBlock(objectPosition.blockX + i, objectPosition.blockY + j, objectPosition.blockZ + k);

					if (block == Blocks.water)
					{
						worldObj.setBlock(objectPosition.blockX + i, objectPosition.blockY + j, objectPosition.blockZ + k, Blocks.ice);
					}

					else if (block == Blocks.fire)
					{
						worldObj.setBlock(objectPosition.blockX + i, objectPosition.blockY + j, objectPosition.blockZ + k, Blocks.air);
					}
				}
			}
		}

		setDead();
	}

	@Override
	public void onUpdate() 
	{
		super.onUpdate();
	}
}
