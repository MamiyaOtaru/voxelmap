package net.minecraft.src.mamiyaotaru;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Render;
import net.minecraft.src.RenderManager;
import net.minecraft.src.ScaledResolution;
import net.minecraft.src.Tessellator;
import net.minecraft.src.ZanMinimap;

import org.lwjgl.opengl.GL11;

public class RenderWaypoint extends Render
{
    /**
     * Actually renders the waypoint beacon. This method is called through the doRender method.
     */
    public void doRenderWaypoint(EntityWaypoint par1EntityWaypoint, double baseX, double baseY, double baseZ, float par8, float par9)
    {
    	if (!par1EntityWaypoint.getWaypoint().enabled) 
    		return;
    	if (ZanMinimap.getInstance().showBeacons && par1EntityWaypoint.worldObj.getChunkFromBlockCoords(((int)(par1EntityWaypoint.posX)), ((int)(par1EntityWaypoint.posZ))).isChunkLoaded) { // draw beacon, if chunk is loaded
    		double bottomOfWorld = 0 - RenderManager.instance.renderPosY;
    		Tessellator tesselator = Tessellator.instance;
    		GL11.glDisable(GL11.GL_TEXTURE_2D);
    		GL11.glDisable(GL11.GL_LIGHTING);
    		GL11.glDepthMask(false); // if true, can't see entities and water behind the beam
    		GL11.glEnable(GL11.GL_BLEND); // if not enabled, beam is basically black
    		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
    		int height = 256;
    		float brightness = 0.06F;
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

    				tesselator.addVertex(vertX1, /*baseY*/ bottomOfWorld + (double)(0), vertZ1); // 0 instead of base cause we want the beam to go from bottom to top, and now waypoints store Y
    				tesselator.addVertex(vertX2, /*baseY*/ bottomOfWorld + (double)(height), vertZ2);
    			}

    			tesselator.draw();

    			// }
    		}
    		GL11.glDisable(GL11.GL_BLEND);
    		GL11.glEnable(GL11.GL_LIGHTING);
    		GL11.glEnable(GL11.GL_TEXTURE_2D);
    		GL11.glDepthMask(true);
    	}
    	if (ZanMinimap.getInstance().showWaypoints) { // point
          //  Minecraft.getMinecraft().entityRenderer.disableLightmap(0.0D); // can either do this (for beam too btw) or override getBrightness* in the entity to return fullbright
    		String label = par1EntityWaypoint.getWaypoint().name;
    		renderLabel(par1EntityWaypoint, label, baseX, baseY, baseZ, 64);
          //  Minecraft.getMinecraft().entityRenderer.enableLightmap(0.0D);
        }
    }
    
    protected void renderLabel(EntityWaypoint par1EntityWaypoint, String par2Str, double par3, double par5, double par7, int par9)
    {
        double var10 = Math.sqrt(par1EntityWaypoint.getDistanceSqToEntity(this.renderManager.livingPlayer));
        if (var10 <= 1000)
        {
        	par2Str+=" (" + (int)var10 + "m)";
        
        	double maxDistance = (256 >> ZanMinimap.getInstance().game.gameSettings.renderDistance) * .75;
        	if (var10 > maxDistance) {
        		par3 = par3 / var10 * maxDistance;
        		par5 = par5 / var10 * maxDistance;
        		par7 = par7 / var10 * maxDistance;
        		var10 = maxDistance;
        	}        

            FontRenderer var12 = this.getFontRendererFromRenderManager();
            //float var13 = 1.6F; // the usual label rendering size
            //float var14 = 0.016666668F * var13;
            float var14 = ((float)var10 * 0.1F + 1.0F) * 0.0266F;  //lower first higher second exaggerates the difference

            GL11.glPushMatrix();
            GL11.glTranslatef((float)par3 + 0.5F, (float)par5 + 1.3F, (float)par7 + 0.5F);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            GL11.glScalef(-var14, -var14, var14);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            Tessellator var15 = Tessellator.instance;
            byte var16 = 0;
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            int var17 = var12.getStringWidth(par2Str) / 2;
            { // added to what is otherwise mostly renderLivingLabel
            	GL11.glEnable(GL11.GL_DEPTH_TEST); // colored bit can be behind
            	GL11.glDepthMask(true);
            	var15.startDrawingQuads();
            	var15.setColorRGBA_F(par1EntityWaypoint.getWaypoint().red, par1EntityWaypoint.getWaypoint().green, par1EntityWaypoint.getWaypoint().blue, 0.75F);
            	var15.addVertex((double)(-var17 - 2), (double)(-2 + var16), 0.0D);
            	var15.addVertex((double)(-var17 - 2), (double)(9 + var16), 0.0D);
            	var15.addVertex((double)(var17 + 2), (double)(9 + var16), 0.0D);
            	var15.addVertex((double)(var17 + 2), (double)(-2 + var16), 0.0D);
            	var15.draw();
            	GL11.glDisable(GL11.GL_DEPTH_TEST); // grey background shows always
            	GL11.glDepthMask(false);
            }
            var15.startDrawingQuads();
            var15.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
            var15.addVertex((double)(-var17 - 1), (double)(-1 + var16), 0.0D);
            var15.addVertex((double)(-var17 - 1), (double)(8 + var16), 0.0D);
            var15.addVertex((double)(var17 + 1), (double)(8 + var16), 0.0D);
            var15.addVertex((double)(var17 + 1), (double)(-1 + var16), 0.0D);
            var15.draw();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            //var12.drawString(par2Str, -var12.getStringWidth(par2Str) / 2, var16, 0xffaaaaaa); // draw grey with no depth then white with depth.  White shows if it's in front, grey otherwise
            //GL11.glEnable(GL11.GL_DEPTH_TEST); // except we comment out the grey, and just draw the white in front of everything
            //GL11.glDepthMask(true);
            var12.drawString(par2Str, -var12.getStringWidth(par2Str) / 2, var16, -1);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();
        }
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
