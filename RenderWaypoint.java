package net.minecraft.src;

import net.minecraft.src.Entity;
import net.minecraft.src.Render;
import net.minecraft.src.Tessellator;

import org.lwjgl.opengl.GL11;

public class RenderWaypoint extends Render
{
    /**
     * Actually renders the waypoint beacon. This method is called through the doRender method.
     */
    public void doRenderWaypoint(EntityWaypoint par1EntityWaypoint, double baseX, double baseY, double baseZ, float par8, float par9)
    {
    	if (!(par1EntityWaypoint.getWaypoint().enabled && par1EntityWaypoint.getWaypoint().showInWorld)) // only render if this waypoint is active and in world display is on
    		return;
        Tessellator tesselator = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        int height = 256;
    	float brightness = 1.0F;
        double topWidthFactor = 1.05D;
        double bottomWidthFactor = 1.05D;
        float r = par1EntityWaypoint.getWaypoint().red;
        float b = par1EntityWaypoint.getWaypoint().blue;
        float g = par1EntityWaypoint.getWaypoint().green;

        for (int width = 0; width < 4; ++width)
        {
        	tesselator.startDrawing(5);
        	tesselator.setColorRGBA_F(r * brightness, g * brightness, b * brightness, 0.8F);

        	double var32 = 0.1D + (double)width * 0.2D;
        	var32 *= topWidthFactor;

        	double var34 = 0.1D + (double)width * 0.2D;
        	var34 *= bottomWidthFactor;

        	for (int side = 0; side < 5; ++side)
        	{
        		double vertX2 = baseX + 0.5D - var32;
        		double vertZ2 = baseZ + 0.5D - var32;

        		if (side == 1 || side == 2)
        		{
        			vertX2 += var32 * 2.0D;
        		}

        		if (side == 2 || side == 3)
        		{
        			vertZ2 += var32 * 2.0D;
        		}

        		double vertX1 = baseX + 0.5D - var34;
        		double vertZ1 = baseZ + 0.5D - var34;

        		if (side == 1 || side == 2)
        		{
        			vertX1 += var34 * 2.0D;
        		}

        		if (side == 2 || side == 3)
        		{
        			vertZ1 += var34 * 2.0D;
        		}

        		tesselator.addVertex(vertX1, baseY + (double)(0), vertZ1);
        		tesselator.addVertex(vertX2, baseY + (double)(height), vertZ2);
        	}

        	tesselator.draw();

        	// }
        }
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(true);

    }


    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void doRender(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
        this.doRenderWaypoint((EntityWaypoint)par1Entity, par2, par4, par6, par8, par9);
    }
}
