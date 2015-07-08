package sq.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import sq.entity.throwable.EntityBoomBall;
import sq.entity.throwable.EntityFreezeBall;
import sq.entity.throwable.EntityJackBall;

/**
 * Actually renders a throwable textured object in the world. Used for projectiles.
 */
public class RenderThrowable extends Render
{
	private final ResourceLocation jackBallLocation = new ResourceLocation("sq:textures/items/jackball.png");
	private final ResourceLocation boomBallLocation = new ResourceLocation("sq:textures/items/boomball.png");
	private final ResourceLocation octoBallLocation = new ResourceLocation("sq:textures/items/octoball.png");
	private final ResourceLocation freezeBallLocation = new ResourceLocation("sq:textures/items/freezeball.png");

	public RenderThrowable()
	{

	}

	@Override
	public void doRender(Entity entity, double posX, double posY, double posZ, float rotationPitch, float rotationYaw)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float)posX, (float)posY, (float)posZ);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glScalef(0.5F, 0.5F, 0.5F);
		bindEntityTexture(entity);
		final float f = 0;
		final float f1 = 1;
		final float f2 = 0;
		final float f3 = 1;
		final float f4 = 1.0F;
		final float f5 = 0.5F;
		final float f6 = 0.25F;
		GL11.glRotatef(180.0F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

		final Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.setNormal(0.0F, 1.0F, 0.0F);
		tess.addVertexWithUV(0.0F - f5, 0.0F - f6, 0.0D, f, f3);
		tess.addVertexWithUV(f4 - f5, 0.0F - f6, 0.0D, f1, f3);
		tess.addVertexWithUV(f4 - f5, f4 - f6, 0.0D, f1, f2);
		tess.addVertexWithUV(0.0F - f5, f4 - f6, 0.0D, f, f2);
		tess.draw();

		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity)
	{
		if (entity instanceof EntityJackBall)
		{
			return jackBallLocation;
		}

		else if (entity instanceof EntityBoomBall)
		{
			return boomBallLocation;
		}

		else if (entity instanceof EntityFreezeBall)
		{
			return freezeBallLocation;
		}

		else
		{
			return null;
		}
	}
}