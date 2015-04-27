package sqr.client.render;


import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import sqr.client.model.ModelJack;
import sqr.entity.EntityJack;

public class RenderJack extends RenderLiving
{

	public RenderJack()
	{
		super(new ModelJack(), 1.0F);
		this.setRenderPassModel(new ModelJack());
	}

	protected int setSpiderEyeBrightness(EntityJack entityspider, int i, float f)
	{
		if(i != 0)
		{
			return 0;
		} else
		{
			bindTexture(new ResourceLocation("/imgz/jackglow.png"));
			final float f1 = (1.0F - entityspider.getBrightness(1.0F)) * 0.5F;
			GL11.glEnable(3042 /*GL_BLEND*/);
			GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
			GL11.glBlendFunc(770, 771);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, f1);
			return 1;
		}
	}


	@Override
	protected int shouldRenderPass(EntityLivingBase entityliving, int i, float f)
	{
		return this.setSpiderEyeBrightness((EntityJack)entityliving, i, f);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
		// TODO Auto-generated method stub
		return null;
	}
}
