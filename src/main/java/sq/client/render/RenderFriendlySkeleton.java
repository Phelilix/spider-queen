package sq.client.render;

import net.minecraft.client.renderer.entity.RenderSkeleton;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * Sets the texture on the friendly skeleton model pre-render.
 */
public class RenderFriendlySkeleton extends RenderSkeleton
{
	private final ResourceLocation texture = new ResourceLocation("sq:textures/entities/friendly-skeleton.png");

	@Override
	protected ResourceLocation getEntityTexture(Entity entity)
	{
		return texture;
	}
}
