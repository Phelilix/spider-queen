package sq.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import sq.core.SpiderCore;
import sq.core.minecraft.ModBlocks;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Defines the mandragora seeds, which place mandragora crop blocks on farmland when used.
 */
public class ItemMandSeeds extends Item
{
	public ItemMandSeeds()
	{
		super();
		
		final String name = "mandragora-seeds";
		setUnlocalizedName(name);
		setTextureName("sq:" + name);
		setCreativeTab(SpiderCore.getCreativeTab());
		setMaxStackSize(64);
		
		GameRegistry.registerItem(this, name);
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int posX, int posY, int posZ, int meta, float xOffset, float yOffset, float zOffset)
	{
		if (!world.isRemote && world.isAirBlock(posX, posY + 1, posZ) && world.getBlock(posX, posY, posZ) == Blocks.farmland)
		{
			if (!player.capabilities.isCreativeMode)
			{
				stack.stackSize--;
			}
			
			world.setBlock(posX, posY + 1, posZ, ModBlocks.cropMand);
		}
		
		return super.onItemUse(stack, player, world, posX, posY, posZ, meta, xOffset, yOffset, zOffset);
	}
}
