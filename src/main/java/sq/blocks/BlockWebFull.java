package sq.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import radixcore.constant.Time;
import sq.core.minecraft.ModBlocks;
import sq.entity.IWebClimber;
import sq.enums.EnumWebType;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * The full web is the base block for the other types of web.
 */
public class BlockWebFull extends Block
{
	private EnumWebType webType;

	public BlockWebFull(EnumWebType type) 
	{
		super(Material.circuits);

		String name = "web-" + type.getName() + "-block";
		setWebType(type);
		setBlockName(name);
		setBlockTextureName("sq:" + name);
		setHardness(1.0F);

		//Change registry name based on which web we are working with.
		if (this instanceof BlockWebGround)
		{
			name += "-ground";
		}

		else if (this instanceof BlockWebSide)
		{
			name += "-side";
		}

		GameRegistry.registerBlock(this, name);
	}

	private void setWebType(EnumWebType type)
	{
		webType = type;
	}

	public EnumWebType getWebType()
	{
		return webType;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		//1 block high, crossed squares.
		return 1;
	}

	@Override
	public Item getItemDropped(int fortune, Random rand, int meta) 
	{
		//Do not drop the block, it is not obtainable.
		return Items.string;
	}

	/**
	 * Checks the provided area for the requirements of the web bed and spawns
	 * it if needed.
	 */
	private void checkForBed(World world, int x, int y, int z, int itr) {
		if (!isBeddishWeb(world.getBlock(x, y, z))) { return; }
		if (webType != EnumWebType.NORMAL) { return; }

		boolean xChanged = false;
		boolean zChanged = false;

		// check if neighboring to the (speculated) center of web bed is
		// web suitable for bedding
		if (!isBeddishWeb(world.getBlock(x - 1, y, z))) {
			// check if x-1 is outlineBlock or outlineBlock2 and adapt.
			// the center of web bed could only have a higher x if true.
			if (isLog(world.getBlock(x - 1, y, z))) {
				x++;
				xChanged = true;
			} else {
				// Stopping because it couldn't be a web bed. (not web, not
				// log/log2. Thus unacceptable)
				return;
			}
		}

		// This is a repetition. Tiny differences though.
		if (!isBeddishWeb(world.getBlock(x, y, z - 1))) {
			if (isLog(world.getBlock(x, y, z - 1))) {
				z++;
				zChanged = true;
			} else {
				return;
			}
		}
		
		// second z axis check. The difference is the chance of the second
		// neighbor log/log2 on the same axis.
		if (!isBeddishWeb(world.getBlock(x, y, z + 1))) {
			if (isLog(world.getBlock(x, y, z + 1))) {
				if (!zChanged) {
					z--;
				} else {
					//It couldn't be a web bed if zChanged was true. Cause that would mean a gap of just two web on the z axis.
					return;
				}
			} else {
				// Stopping because it couldn't be a web bed. (not web, not
				// log/log2. Thus unacceptable)
				return;
			}
		}

		// repetition of previous second z axis check, but then with the x
		// axis.
		if (!isBeddishWeb(world.getBlock(x + 1, y, z))) {
			if (isLog(world.getBlock(x + 1, y, z))) {
				if (!xChanged) {
					x--;
				} else {
					//It couldn't be a web bed if xChanged was true. Cause that would mean a gap of just two web on the x axis.
					return;
				}
			} else {
				// Stopping because it couldn't be a web bed. (not web, not
				// log/log2. Thus unacceptable)
				return;
			}
		}

		// At this point, the target of x, y, z can only be the center of a
		// web bed. Complete or not. If xChanged or zChanged is true, 
		// it would be too bothersome to check which ones are web or not. We just call checkForBed again with +1 itr.
		// On the other hand. If zChanged and xChanged is false, 
		// we would only need to continue checking.
		if (!zChanged && !xChanged) {
			//Checking the corners of the web bed.
			if (!isBeddishWeb(world.getBlock(x-1, y, z-1))) { return; }
			if (!isBeddishWeb(world.getBlock(x-1, y, z+1))) { return; }
			if (!isBeddishWeb(world.getBlock(x+1, y, z-1))) { return; }
			if (!isBeddishWeb(world.getBlock(x+1, y, z+1))) { return; }
			
			//Checking the outline of the web bed.
			if (!isLog(world.getBlock(x - 2, y, z - 2))) { return; }
			if (!isLog(world.getBlock(x - 2, y, z - 1))) { return; }
			if (!isLog(world.getBlock(x - 2, y, z    ))) { return; }
			if (!isLog(world.getBlock(x - 2, y, z + 1))) { return; }
			if (!isLog(world.getBlock(x - 2, y, z + 2))) { return; }
			if (!isLog(world.getBlock(x + 2, y, z - 2))) { return; }
			if (!isLog(world.getBlock(x + 2, y, z - 1))) { return; }
			if (!isLog(world.getBlock(x + 2, y, z    ))) { return; }
			if (!isLog(world.getBlock(x + 2, y, z + 1))) { return; }
			if (!isLog(world.getBlock(x + 2, y, z + 2))) { return; }
			if (!isLog(world.getBlock(x - 2, y, z - 2))) { return; }
			if (!isLog(world.getBlock(x - 1, y, z - 2))) { return; }
			if (!isLog(world.getBlock(x,     y, z - 2))) { return; }
			if (!isLog(world.getBlock(x + 1, y, z - 2))) { return; }
			if (!isLog(world.getBlock(x + 2, y, z - 2))) { return; }
			if (!isLog(world.getBlock(x - 2, y, z + 2))) { return; }
			if (!isLog(world.getBlock(x - 1, y, z + 2))) { return; }
			if (!isLog(world.getBlock(x,     y, z + 2))) { return; }
			if (!isLog(world.getBlock(x + 1, y, z + 2))) { return; }
			if (!isLog(world.getBlock(x + 2, y, z + 2))) { return; }
			
			world.setBlock(x - 1, y, z - 1, ModBlocks.webBed);
			world.setBlock(x - 1, y, z    , ModBlocks.webBed);
			world.setBlock(x - 1, y, z + 1, ModBlocks.webBed);
			world.setBlock(x,     y, z - 1, ModBlocks.webBed);
			world.setBlock(x,     y, z    , ModBlocks.webBed);
			world.setBlock(x,     y, z + 1, ModBlocks.webBed);
			world.setBlock(x + 1, y, z - 1, ModBlocks.webBed);
			world.setBlock(x + 1, y, z    , ModBlocks.webBed);
			world.setBlock(x + 1, y, z + 1, ModBlocks.webBed);
		} else {
			if (itr <= 0){
				checkForBed(world, x, y, z, itr+1);
			}
		}
	}

	//Checks if a block could be used in bed. :) (log and log2)
	private boolean isLog(Block block){
		return block == Blocks.log || block == Blocks.log2;
	}
	
	//Checks if a web-block could be used in bed. 
	//It could be a design choice to not recreate the bed
	//when it got damaged and the player repairs it. 
	//Well this makes it so that it does get replaced.
	private boolean isBeddishWeb(Block block){
		return block == ModBlocks.webBed || block == ModBlocks.webFull;
	}

	@Override
	public void onBlockAdded(World world, int posX, int posY, int posZ)
	{
		//Each time we're placed in the world, notify other blocks and check for the bed.
		checkForBed(world, posX, posY, posZ, 0);
		onNeighborBlockChange(world, posX, posY, posZ, 0);
	}

	private void onNeighborBlockChange(World world, int posX, int posY, int posZ, int meta)
	{
		if (world.getBlock(posX - 1, posY, posZ) != Blocks.air) { return; }
		if (world.getBlock(posX + 1, posY, posZ) != Blocks.air) { return; }
		if (world.getBlock(posX, posY - 1, posZ) != Blocks.air) { return; }
		if (world.getBlock(posX, posY + 1, posZ) != Blocks.air) { return; }
		if (world.getBlock(posX, posY, posZ - 1) != Blocks.air) { return; }
		if (world.getBlock(posX, posY, posZ + 1) != Blocks.air) { return; }

		world.setBlock(posX, posY, posZ, Blocks.air);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int posX, int posY, int posZ)
	{
		//No collision.
		return null;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int posX, int posY, int posZ)
	{
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) 
	{
		//Hinder the motion of entities that aren't spiders.
		if (entity instanceof EntitySpider || entity instanceof EntityPlayer || entity instanceof IWebClimber)
		{
			return;
		}

		else
		{
			entity.setInWeb();

			entity.motionX = entity.motionX * -0.1D;
			entity.motionZ = entity.motionZ * -0.1D;
			entity.motionY = entity.motionY * 0.1D;

			//If this web is poison, add a poison effect to the creature caught inside.
			if (webType == EnumWebType.POISON && entity instanceof EntityLivingBase)
			{
				final EntityLivingBase entityLiving = (EntityLivingBase)entity;

				if (entityLiving.getActivePotionEffect(Potion.poison) == null)
				{
					entityLiving.addPotionEffect(new PotionEffect(Potion.poison.id, Time.SECOND * 5));
				}
			}
		}
	}

	@Override
	public boolean isLadder(IBlockAccess world, int posX, int posY, int posZ, EntityLivingBase entity)
	{
		//Allow web climbers and players to climb this web like a ladder.
		if (entity instanceof EntityPlayer || entity instanceof EntitySpider || entity instanceof IWebClimber)
		{
			return true;
		}

		else
		{
			return false;
		}
	}
}
