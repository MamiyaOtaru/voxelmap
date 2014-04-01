package net.minecraft.src;

import net.minecraft.client.Minecraft;
import net.minecraft.src.mamiyaotaru.EntityWaypoint;
import net.minecraft.src.mamiyaotaru.MinimapGuiInGame;
import net.minecraft.src.mamiyaotaru.RenderWaypoint;

public class mod_ZanMinimap extends BaseMod {
	
	ZanMinimap minimap;
	GuiIngame realGui;
	MinimapGuiInGame fakeGui;
	boolean haveRenderManager = false;
	
    public String getVersion()
    {
        return "3.0";
    }
    
    public void load() {
    	minimap = new ZanMinimap();
    	ModLoader.setInGameHook(this, true, false);
    	realGui = ModLoader.getMinecraftInstance().ingameGUI;
    	fakeGui = new MinimapGuiInGame(ModLoader.getMinecraftInstance(), minimap, realGui);
    }
    


    public String ModName()
    {
        return "Zan's Minimap";
    }

    public boolean onTickInGame(float f, Minecraft var1) {
    	if (!haveRenderManager) { // only run once.  we do go through onTick multiple times to catch when advancedHUD gets called after this and takes over ingameGUI
    		Object renderManager = RenderManager.instance; // NetClientHandler
    		if (renderManager == null) {
    			//	System.out.println("failed to get render manager");
    		}
    		else {
    			//Object entityRenderMap = getPrivateFieldByName(renderManager, "o" /*"entityRenderMap"*/); // Map - fieldname needs to be obfuscated name
    			Object entityRenderMap = getPrivateFieldByType(renderManager, java.util.Map.class); // Map - fieldname needs to be obfuscated name
    			if (entityRenderMap == null) {
    				//	System.out.println("could not get entityRenderMap");
    			}
    			else {
    				RenderWaypoint renderWaypoint = new RenderWaypoint();
    				((java.util.HashMap)entityRenderMap).put(EntityWaypoint.class, renderWaypoint);
    				renderWaypoint.setRenderManager(RenderManager.instance);
    			}
    		}
    	}
    	if (var1.ingameGUI.getClass() != MinimapGuiInGame.class) {
    		realGui = var1.ingameGUI;
    		fakeGui.realGui = realGui;
    		var1.ingameGUI = fakeGui;
    	}

    	//	minimap.onTickInGame(var1);    	return true;    }

	public Object getPrivateFieldByType (Object o, Class classtype) {   
		return getPrivateFieldByType(o, classtype, 0);
	}
	
	public Object getPrivateFieldByType (Object o, Class classtype, int index) {   
		// Go and find the private field... 
		int counter = 0;
		final java.lang.reflect.Field fields[] = o.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; ++i) {
			if (classtype.equals(fields[i].getType())) {
				if (counter == index) {
					try {
						fields[i].setAccessible(true);
						return fields[i].get(o);
					} 
					catch (IllegalAccessException ex) {
					}
				}
				counter++;
			}
		}
		return null;
	}

}