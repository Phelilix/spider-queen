package sq.items;

import net.minecraft.item.ItemFood;
import sq.core.SpiderCore;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Nectar is simply a food item that heals 3 food points and has little saturation.
 */
public class ItemNectar extends ItemFood
{
	public ItemNectar()
	{
		super(3, 0.4F, false);
		setAlwaysEdible();
		
		final String name = "nectar";
		setUnlocalizedName(name);
		setTextureName("sq:" + name);
		setCreativeTab(SpiderCore.getCreativeTab());
		
		GameRegistry.registerItem(this, name);
	}
}
