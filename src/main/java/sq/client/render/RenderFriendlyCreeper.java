package sq.client.render;

import net.minecraft.client.renderer.entity.RenderCreeper;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * Sets the texture on the friendly creeper model pre-render.
 */
public class RenderFriendlyCreeper extends RenderCreeper
{
	private final ResourceLocation texture = new ResourceLocation("sq:textures/entities/friendly-creeper.png");

	@Override
	protected ResourceLocation getEntityTexture(Entity entity)
	{
		return texture;
	}
}
