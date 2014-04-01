package net.minecraft.src.mamiyaotaru;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.src.GuiMultiplayer;
import net.minecraft.src.GuiSlot;
import net.minecraft.src.ScaledResolution;
import net.minecraft.src.StringTranslate;
import net.minecraft.src.Tessellator;
import net.minecraft.src.ZanMinimap;

class GuiSlotWaypoints extends GuiSlot
{
    private ArrayList<Waypoint> waypoints;
    private ZanMinimap minimap;

    final GuiWaypoints parentGui;

    public GuiSlotWaypoints(GuiWaypoints par1GuiWaypoints)
    {
        super(par1GuiWaypoints.minimap.game /*mc is protected in guiScreen, from which guiWaypoints inherits :-/ */, par1GuiWaypoints.width, par1GuiWaypoints.height, 32, par1GuiWaypoints.height - 65 + 4, 18);
        this.parentGui = par1GuiWaypoints;
        this.minimap = parentGui.minimap;
        this.waypoints = this.minimap.wayPts;
        
/*		for(Waypoint pt:waypoints) {
			if I need to add to a local array or something instead of directly using minimap.wayPts
		}*/

    }

    /**
     * Gets the size of the current slot list.
     */
    protected int getSize()
    {
        return this.waypoints.size();
    }

    /**
     * the element in the slot that was clicked, boolean for wether it was double clicked or not
     */
    protected void elementClicked(int par1, boolean par2)
    {
    	
        this.parentGui.setSelectedWaypoint(this.waypoints.get(par1));
        // enable add/delete?  or do in waypointGUI if something (anything) is selected..
        
        // actual position in pixels in window (varies with size of window)
        //System.out.println("*****Mousex: " + Mouse.getX() + ", Mousey: " + Mouse.getY());
        
        // minecraft position of mouse, as seen in guiSlot for instance.  
        /*
		ScaledResolution var8 = new ScaledResolution(this.minimap.game.gameSettings, this.minimap.game.displayWidth, this.minimap.game.displayHeight);
        int var9 = var8.getScaledWidth();
        int var10 = var8.getScaledHeight();
        int var11 = Mouse.getX() * var9 / this.minimap.game.displayWidth;
        int var13 = var10 - Mouse.getY() * var10 / this.minimap.game.displayHeight - 1;
        
        System.out.println("*****aMousex: " + var11 + ", aMousey: " + var13);
        */
        
        GuiWaypoints.getButtonEdit(this.parentGui).enabled = true;
        GuiWaypoints.getButtonDelete(this.parentGui).enabled = true;
        GuiWaypoints.getButtonTeleport(this.parentGui).enabled = parentGui.canTeleport();
        
        int leftEdge = this.parentGui.width / 2 - 92 - 16;
        byte var10 = 4;
        //System.out.println("width: " + parentGui.width + " leftedge: " + leftEdge);
        int width = 215;
    	if (this.mouseX >= leftEdge + width - 16 - var10 && this.mouseX <= leftEdge + width + var10)
    	{
    		this.parentGui.toggleWaypointVisibility();
    	}

    }

    /**
     * returns true if the element passed in is currently selected
     */
    protected boolean isSelected(int par1)
    {
        return this.waypoints.get(par1).equals(this.parentGui.selectedWaypoint);
    }

    /**
     * return the height of the content being scrolled
     */
    protected int getContentHeight()
    {
        return this.getSize() * 18;
    }

    protected void drawBackground()
    {
        this.parentGui.drawDefaultBackground();
    }

    // here's where we draw each slot. Add images, colors, whatever.  see serverSlot for image example.  
    // par2 is left edge of slot
    protected void drawSlot(int par1, int par2, int par3, int par4, Tessellator par5Tessellator)
    {
    	Waypoint waypoint = this.waypoints.get(par1);
        this.parentGui.drawCenteredString(this.parentGui.getFontRenderer(), waypoint.name, this.parentGui.width / 2, par3 + 3, waypoint.getUnified());
        
        // tooltip(s? maybe have different ones when moused over display or not icon)
        //System.out.println("par1: " + par1 + " x: " + this.mouseX + " par2: " + par2 + " y: " + this.mouseY + " par3: " + par3 + " par4: " + par4);
        byte var10 = 4;
    	if (this.mouseX >= par2 + var10 && this.mouseY >= par3 - var10 && this.mouseX <= par2 + 215 + var10 && this.mouseY <= par3 + 8 + var10)
    	{
    		String tooltip = "X: " + waypoint.x + " Z: " + waypoint.z;
    		if (waypoint.y > 0) 
    			tooltip += " Y: " + waypoint.y;
    		GuiWaypoints.setTooltip(this.parentGui, tooltip);
    	}
    	// draw enabled or not icon
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        /*
        // use my own image, on and off in same image (32x16 for 16x16icons)
        this.parentGui.minimap.game.renderEngine.bindTexture(this.parentGui.minimap.game.renderEngine.getTexture("/mamiyaotaru/visible.png"));
        int offset = waypoint.enabled?0:16;
        this.drawTexturedModalRect(par2 + 205, par3, offset, 0, 16, 16);
         x, y, u, v, w, h  (u and v are eventually made into fractions.  either have to know the image dimensions in destination function, or put in as fractions here)
         */ 
        this.parentGui.minimap.game.renderEngine.bindTexture(this.parentGui.minimap.game.renderEngine.getTexture("/gui/inventory.png"));
        int xOffset = waypoint.enabled?72:90;
        int yOffset = 216;
        parentGui.drawTexturedModalRect(par2 + 198, par3-2, xOffset, yOffset, 16, 16);
    }
    
    /**
     * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height
     */
    // can use for drawing my own icons if I don't want to get from inventory.png
    /*
    public void drawTexturedModalRect(int par1, int par2, int par3, int par4, int par5, int par6)
    {
        float var7 = 32; // width of image
        float var8 = 16; // height of image
        Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV((double)(par1 + 0), (double)(par2 + par6), 0.0D, (double)((float)(par3 + 0) / var7), (double)((float)(par4 + par6) / var8));
        var9.addVertexWithUV((double)(par1 + par5), (double)(par2 + par6), 0.0D, (double)((float)(par3 + par5) / var7), (double)((float)(par4 + par6) / var8));
        var9.addVertexWithUV((double)(par1 + par5), (double)(par2 + 0), 0.0D, (double)((float)(par3 + par5) / var7), (double)((float)(par4 + 0) / var8));
        var9.addVertexWithUV((double)(par1 + 0), (double)(par2 + 0), 0.0D, (double)((float)(par3 + 0) / var7), (double)((float)(par4 + 0) / var8));
        var9.draw();
    }
    */
      
}
