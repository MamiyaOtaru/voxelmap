package net.minecraft.src.mamiyaotaru;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiIngame;
import net.minecraft.src.GuiNewChat;
import net.minecraft.src.ZanMinimap;

public class MinimapGuiInGame extends GuiIngame {

	protected Minecraft mc;
	public ZanMinimap minimap;
	public GuiIngame realGui;
	
    public MinimapGuiInGame(Minecraft var1, ZanMinimap minimap, GuiIngame realGui)
    {
        super(var1);
        this.mc = var1;
        this.minimap = minimap;
        this.realGui = realGui;
    }

    /**
     * Render the map, after doing the regular hud.  (still before GuiScreen)
     */
    public void renderGameOverlay(float var1, boolean var2, int var3, int var4)
    {
    	if (realGui != null) // realGui is probably GuiIngame, but it could be AdvancedHud's replacement, set dynamically every time that mod_'s ontick is called
    		realGui.renderGameOverlay(var1, var2, var3, var4);
    	else
    		super.renderGameOverlay(var1, var2, var3, var4);
    	minimap.onTickInGame(this.mc);
    }
    
    public void updateTick() {
    	if (realGui != null) // realGui is probably GuiIngame, but it could be AdvancedHud's replacement, set dynamically every time that mod_'s ontick is called
    		realGui.updateTick();
    	else
    		super.updateTick();
    	
    }
    
    public GuiNewChat getChatGUI()
    {
    	if (realGui != null) {// realGui is probably GuiIngame, but it could be AdvancedHud's replacement, set dynamically every time that mod_'s ontick is called
    		return realGui.getChatGUI();
    	}
    	else
    		return super.getChatGUI();
    }
    
    public int getUpdateCounter()
    {
    	if (realGui != null) // realGui is probably GuiIngame, but it could be AdvancedHud's replacement, set dynamically every time that mod_'s ontick is called
    		return realGui.getUpdateCounter();
    	else
    		return super.getUpdateCounter();
    }
}
