package sq.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import sq.client.model.ModelSpiderEx;
import sq.entity.creature.EntitySpiderEx;
import sq.enums.EnumSpiderType;

/**
 * Sets the texture on the extended spiders' models pre-render.
 * Also applies effects on each render pass.
 */
public class RenderSpiderEx extends RenderLiving
{
	private static ResourceLocation[][] textures;
	private static ResourceLocation charge = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
	private static ResourceLocation eyes;

	private final ModelSpiderEx model = new ModelSpiderEx();

	public RenderSpiderEx()
	{
		super(new ModelSpiderEx(), 1.0F);
		setRenderPassModel(new ModelSpiderEx());
	}

	@Override
	protected float getDeathMaxRotation(EntityLivingBase entityLivingBase)
	{
		return 180.0F;
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity)
	{
		return getEntityTexture((EntitySpiderEx) entity);
	}

	protected ResourceLocation getEntityTexture(EntitySpiderEx spider)
	{
		final int level = spider.getLevel();
		return textures[spider.getSpiderType().getId()][level - 1];
	}

	@Override
	protected int shouldRenderPass(EntityLivingBase entitySpider, int passNumber, float partialTickTime)
	{
		final EntitySpiderEx spider = (EntitySpiderEx)entitySpider;

		//When the spider is a powered boom spider, we render the "charged" effect over them.
		if (spider.getPowered())
		{
			if (entitySpider.isInvisible())
			{
				GL11.glDepthMask(false);
			}

			else
			{
				GL11.glDepthMask(true);
			}

			if (passNumber == 1)
			{
				final float f1 = spider.ticksExisted + partialTickTime;
				Minecraft.getMinecraft().renderEngine.bindTexture(charge);
				GL11.glMatrixMode(GL11.GL_TEXTURE);
				GL11.glLoadIdentity();
				final float f2 = f1 * 0.01F;
				final float f3 = f1 * 0.01F;
				GL11.glTranslatef(f2, f3, 0.0F);
				setRenderPassModel(model);
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				GL11.glEnable(GL11.GL_BLEND);
				final float f4 = 0.5F;
				GL11.glColor4f(f4, f4, f4, 1.0F);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
				return 1;
			}

			else if (passNumber == 2)
			{
				GL11.glMatrixMode(GL11.GL_TEXTURE);
				GL11.glLoadIdentity();
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_BLEND);
			}
		}

		final char c0 = 61680;
		final int j = c0 % 65536;
		final int k = c0 / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j / 1.0F, k / 1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		//On each pass greater than 0, we render the spider eyes without lighting taking effect. 
		//This makes spider eyes glow in the dark.
		if (passNumber > 0)
		{
			bindTexture(eyes);
			GL11.glTranslatef(0.0F, 0.0F, -0.0001F); //Prevent Z clipping.
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
			return 1;
		}

		return -1;
	}

	@Override
	protected void preRenderCallback(EntityLivingBase entityLiving, float partialTickTime)
	{
		final EntitySpiderEx spider = (EntitySpiderEx) entityLiving;

		//Scale according to the spider's type.
		if (spider.getSpiderType() == EnumSpiderType.WIMPY)
		{
			GL11.glScaled(0.5D, 0.5D, 0.5D);
		}

		else if (spider.getSpiderType() == EnumSpiderType.TANK)
		{
			final int level = spider.getLevel();
			double scale = 1.0D;

			switch (level)
			{
			case 1:
				scale = 1.0D;
				break;
			case 2:
				scale = 1.3D;
				break;
			case 3:
				scale = 1.5D;
				break;
			default:
				scale = 1.3D;
				break;
			}

			GL11.glScaled(scale, scale, scale);
		}

		else if (spider.getSpiderType() == EnumSpiderType.ENDER)
		{
			GL11.glScaled(1.3D, 1.3D, 1.3D);
		}

		else
		{
			super.preRenderCallback(entityLiving, partialTickTime);
		}
	}

	static
	{
		textures = new ResourceLocation[EnumSpiderType.values().length][3];
		eyes = new ResourceLocation("sq:textures/entities/spider-eyes.png");

		for (final EnumSpiderType type : EnumSpiderType.values())
		{
			if (type != EnumSpiderType.NONE)
			{
				for (int i = 1; i < 4; i++)
				{
					//Textures are organized as: [id][textures for levels 1, 2, and 3].
					textures[type.getId()][i - 1] = new ResourceLocation("sq:textures/entities/spider-" + type.name().toLowerCase() + "-" + i + ".png");
				}
			}
		}
	}
}