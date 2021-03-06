package sq.core.forge;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import radixcore.constant.Font.Color;
import radixcore.data.WatchedInt;
import radixcore.util.RadixLogic;
import sq.core.ReputationHandler;
import sq.core.SpiderCore;
import sq.core.minecraft.ModAchievements;
import sq.core.minecraft.ModItems;
import sq.core.radix.PlayerData;
import sq.entity.AbstractNewMob;
import sq.entity.IRep;
import sq.entity.ai.PlayerExtension;
import sq.entity.ai.RepEntityExtension;
import sq.entity.ai.ReputationContainer;
import sq.entity.creature.EntityCocoon;
import sq.entity.creature.EntityHuman;
import sq.entity.creature.EntitySpiderEx;
import sq.entity.friendly.EntityFriendlyMandragora;
import sq.entity.friendly.IFriendlyEntity;
import sq.enums.EnumWatchedDataIDs;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * This class contains all event hooks that belong to Forge.
 */
public final class EventHooksForge
{
	@SubscribeEvent
	public void onPlayerSleepInBed(PlayerSleepInBedEvent event)
	{
		//Disable sleeping in normal beds.
		event.result = EntityPlayer.EnumStatus.NOT_POSSIBLE_HERE;
		event.entityPlayer.addChatMessage(new ChatComponentText("Spiders can't sleep in normal beds."));
	}

	/**
	 * When an entity is attacked by a player we check to see if this entity belongs to a reputation group. If so, the entity can be "angered"
	 * by being struck three times. onAttackEntity will increment that value if needed.
	 * 
	 * We also set the player's friendly entities and spiders to target the entity that was struck.
	 */
	@SubscribeEvent
	public void onAttackEntity(AttackEntityEvent event)
	{
		final EntityPlayer player = event.entityPlayer;
		final PlayerData data = SpiderCore.getPlayerData(player);

		if (event.target instanceof EntityLivingBase && !(event.target instanceof EntityCocoon))
		{
			final EntityLivingBase livingBase = (EntityLivingBase)event.target;
			final List<Entity> entities = RadixLogic.getAllEntitiesWithinDistanceOfCoordinates(player.worldObj, player.posX, player.posY, player.posZ, 20);
			final RepEntityExtension extension = (RepEntityExtension) event.target.getExtendedProperties(RepEntityExtension.ID);

			//Alert a player's nearby spiders when the player attacks something.
			for (final Entity entity : entities)
			{
				if (entity instanceof EntitySpiderEx)
				{
					final EntitySpiderEx spider = (EntitySpiderEx)entity;

					if (spider.getOwner().equals(player.getPersistentID()))
					{
						spider.setTarget(event.target);
					}
				}
			}

			//Also alert the friendly entities.
			for (final Entity entity : entities)
			{
				try
				{
					if (entity instanceof IFriendlyEntity && !(entity instanceof EntityFriendlyMandragora))
					{
						final IFriendlyEntity friendly = (IFriendlyEntity)entity;

						if (!entity.worldObj.isRemote && friendly.getFriendPlayerUUID().equals(player.getPersistentID()) && event.target instanceof EntityLivingBase)
						{
							friendly.setTarget((EntityLivingBase) event.target);
						}
					}
				}

				catch (final Exception e)
				{
					continue;
				}
			}

			//Check for the entity extension and up the number of times this entity has been attacked.
			if (extension != null)
			{
				//Guess what the new health will be as this is ran before attack damage takes effect.
				float calculatedHealth = livingBase.getHealth();
				final ItemStack stack = player.inventory.getCurrentItem();

				if (stack != null)
				{
					if (stack.getItem() instanceof ItemTool)
					{
						final ItemTool tool = (ItemTool)stack.getItem();
						final Item.ToolMaterial toolMaterial = Item.ToolMaterial.valueOf(tool.getToolMaterialName());
						calculatedHealth -= toolMaterial.getDamageVsEntity();
					}

					else if (stack.getItem() instanceof ItemSword)
					{
						final ItemSword sword = (ItemSword)stack.getItem();
						final Item.ToolMaterial toolMaterial = Item.ToolMaterial.valueOf(sword.getToolMaterialName());
						calculatedHealth -= 4.0F + toolMaterial.getDamageVsEntity(); //Swords add 4.0 to the damage.
					}
				}

				//Get reputation for anger check.
				int reputation = -1;
				final WatchedInt watchedLikeInstance = ReputationContainer.getLikeDataByClass(event.target.getClass(), data);

				if (event.entityLiving instanceof IRep)
				{
					reputation = ((IRep)event.target).getLikeData(data).getInt();
				}

				else if (watchedLikeInstance != null)
				{
					reputation = watchedLikeInstance.getInt();
				}

				//Check for anger.
				extension.setTimesHitByPlayer(extension.getTimesHitByPlayer() + 1);

				if (!player.worldObj.isRemote && extension.getTimesHitByPlayer() == 3 && calculatedHealth > 0.0F && reputation >= 0)
				{
					player.addChatComponentMessage(new ChatComponentText(Color.RED + "You have angered this " + event.target.getCommandSenderName() + "!"));
				}
			}

			if (event.target instanceof EntityGhast)
			{
				final EntityGhast ghast = (EntityGhast) event.target;

				//isInWeb
				if (ObfuscationReflectionHelper.getPrivateValue(Entity.class, ghast, 27))
				{
					if (!ghast.worldObj.isRemote && RadixLogic.getBooleanWithProbability(50))
					{
						ghast.dropItem(ModItems.ghastEgg, 1);
					}

					//Half the ghast's health to prevent farming eggs.
					ghast.setHealth(ghast.getHealth() / 2);
				}
			}
		}
	}

	/**
	 * In this event, we consistently set isInWeb to false for the player. This prevents
	 * slowdown in web from other mods, and Minecraft's cobwebs.
	 */
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		if (event.entityLiving instanceof EntityPlayer)
		{
			final EntityPlayer player = (EntityPlayer)event.entityLiving;

			//isInWeb
			ObfuscationReflectionHelper.setPrivateValue(Entity.class, player, false, 27);
		}
	}

	/**
	 * Its name may not suggest it, however this runs whenever a living creature takes damage.
	 * 
	 * With it, we check to see if the player is supposed to take fall damage, then check to see if they recently
	 * used the webslinger. This prevents damage in the case that the player has been hanging on the webslinger
	 * for a long period of time, and their fall distance has kept increasing on the server.
	 */
	@SubscribeEvent
	public void onLivingAttack(LivingAttackEvent event)
	{
		if (event.entityLiving instanceof EntityPlayer && event.source == DamageSource.fall)
		{
			final PlayerExtension extension = PlayerExtension.get((EntityPlayer)event.entityLiving);

			final boolean cancel = extension.webEntity != null || extension.slingerCooldown > 0;
			event.setCanceled(cancel);
		}
	}

	/**
	 * Whenever a creature dies, we check to see if it belongs to a reputation group. If so, there's a random chance to
	 * negatively affect the player's reputation with that creatures' reputation group. If the creature is a creeper, zombie, or skeleton,
	 * killing five will increase reputation with humans.
	 */
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event)
	{
		if (event.entityLiving != null && event.source.getEntity() instanceof EntityPlayer)
		{
			final EntityPlayer player = (EntityPlayer) event.source.getEntity();
			final PlayerData data = SpiderCore.getPlayerData(player);
			final PlayerExtension playerExtension = (PlayerExtension) player.getExtendedProperties(PlayerExtension.ID);
			final RepEntityExtension repExtension = (RepEntityExtension) event.entityLiving.getExtendedProperties(RepEntityExtension.ID);

			if (repExtension != null) //If they have an extension, they are a vanilla mob with a reputation entry.
			{
				final WatchedInt likeData = ReputationContainer.getLikeDataByClass(event.entityLiving.getClass(), data);
				final int chanceToAffectRep = 25;

				//Check if the player is invisible. If so, no potential for reputation penalty.
				if (likeData != null && RadixLogic.getBooleanWithProbability(chanceToAffectRep) && !player.isInvisible())
				{
					ReputationHandler.onReputationChange(player, event.entityLiving, -1);
				}

				//Increase number of monsters killed if not human.
				if (likeData != data.humanLike)
				{
					if (event.entityLiving instanceof EntityCreeper || event.entityLiving instanceof EntityZombie || event.entityLiving instanceof EntitySkeleton)
					{
						playerExtension.setMonstersKilled(playerExtension.getMonstersKilled() + 1);

						if (playerExtension.getMonstersKilled() >= 5)
						{
							player.addChatComponentMessage(new ChatComponentText(Color.GREEN + "Your attack on enemy mobs has earned the humans' trust."));
							playerExtension.setMonstersKilled(0); //Reset

							ReputationHandler.onReputationChange(player, new EntityHuman(player.worldObj), 1);
						}
					}
				}
			}
		}
	}

	/**
	 * When an entity is being constructed, we check to see if it is a player or it should belong to a reputation group.
	 * Players get the PlayerExtension, and reputation entities get the RepEntityExtension.
	 * 
	 * We also add target tasks to creatures extending EntityMob. These tasks will instruct them to attack human entities.
	 */
	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event)
	{
		final boolean injectExtension = EnumWatchedDataIDs.doesEntityHaveLikeStatus(event.entity) || event.entity instanceof EntityPlayer;

		if (injectExtension)
		{
			if (event.entity instanceof EntityPlayer)
			{
				PlayerExtension.register((EntityPlayer) event.entity);
			}

			else
			{
				RepEntityExtension.register(event.entity);
			}
		}

		if (event.entity instanceof EntityMob && !(event.entity instanceof AbstractNewMob))
		{
			final EntityMob mob = (EntityMob)event.entity;

			if (mob.targetTasks != null)
			{
				mob.targetTasks.addTask(3, new EntityAINearestAttackableTarget(mob, EntityHuman.class, 0, true));
			}
		}
	}

	@SubscribeEvent
	public void onLivingSetTarget(LivingSetAttackTargetEvent event)
	{
		if (event.target instanceof EntityPlayer && event.entityLiving instanceof EntityLiving)
		{
			final PlayerData data = SpiderCore.getPlayerData((EntityPlayer) event.target);
			final EntityLiving entityLiving = (EntityLiving)event.entityLiving;
			final RepEntityExtension extension = (RepEntityExtension) entityLiving.getExtendedProperties(RepEntityExtension.ID);
			final WatchedInt watchedLikeInstance = ReputationContainer.getLikeDataByClass(event.entityLiving.getClass(), data);
			int reputation = -1;

			if (event.entityLiving instanceof IRep)
			{
				reputation = ((IRep)entityLiving).getLikeData(data).getInt();
			}

			else if (event.entityLiving instanceof IFriendlyEntity)
			{
				final IFriendlyEntity entity = (IFriendlyEntity)event.entityLiving;

				if (entity.getFriendPlayerUUID().equals(event.target.getPersistentID()))
				{
					//Never attack the friend player.
					entityLiving.setAttackTarget(null);
					return;
				}
			}

			else if (watchedLikeInstance != null)
			{
				reputation = watchedLikeInstance.getInt();
			}

			if (reputation >= 0)
			{
				if (extension != null && extension.getTimesHitByPlayer() >= 3)
				{
					//Ignore unsetting liked target when they've been hit by the player too many times.
					return;
				}

				entityLiving.setAttackTarget(null);
			}
		}
	}

	@SubscribeEvent
	public void onEntityItemPickup(EntityItemPickupEvent event)
	{
		if (event.entityPlayer != null)
		{
			if (event.item.getEntityItem().getItem() == ModItems.spiderEgg)
			{
				event.entityPlayer.triggerAchievement(ModAchievements.acquireEgg);
			}
		}
	}
}
