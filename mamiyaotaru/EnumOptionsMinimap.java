package net.minecraft.src.mamiyaotaru;

import org.lwjgl.input.Keyboard;

public enum EnumOptionsMinimap
{
//	out.println("Zoom Key:" + Keyboard.getKeyName(zoomKey));
//	out.println("Menu Key:" + Keyboard.getKeyName(menuKey));
	
	COORDS("options.minimap.showcoordinates", false, true, false),
	HIDE("options.minimap.hideminimap", false, true, false),
	SHOWNETHER("Function in Nether", false, true, false),
	CAVEMODE("options.minimap.cavemode", false, true, false),
	LIGHTING("options.minimap.dynamiclighting", false, true, false),
	TERRAIN("options.minimap.terraindepth", false, false, true),
	SQUARE("options.minimap.squaremap", false, true, false),
	OLDNORTH("options.minimap.oldnorth", false, true, false),
	BEACONS("options.minimap.ingamewaypoints", false, false, true),
	WELCOME("Welcome Screen", false, true, false),
	THREADING("Threading", false, true, false),
	ZOOM("option.minimapZoom", false, true, false),
	MOTIONTRACKER("Motion Tracker", false, true, false),
	LOCATION("options.minimap.location", false, false, true),
	SIZE("options.minimap.size", false, false, true),
	FILTERING("options.minimap.filtering", false, true, false),
	WATERTRANSPARENCY("options.minimap.watertransparency", false, true, false),
	BLOCKTRANSPARENCY("options.minimap.blocktransparency", false, true, false),
	BIOMES("options.minimap.biomes", false, true, false),
	HIDERADAR("options.minimap.radar.hideradar", false, true, false),
	SHOWHOSTILES("options.minimap.radar.showhostiles", false, true, false),
	SHOWPLAYERS("options.minimap.radar.showplayers", false, true, false),
	SHOWNEUTRALS("options.minimap.radar.showneutrals", false, true, false),
	RADAROUTLINES("options.minimap.radar.iconoutlines", false, true, false),
	RADARFILTERING("options.minimap.radar.iconfiltering", false, true, false), 
	SHOWHELMETS("options.minimap.radar.showmphelmets", false, true, false); 


	
    private final boolean enumFloat;
    private final boolean enumBoolean;
    private final boolean enumList;
    private final String enumString;

    public static EnumOptionsMinimap getEnumOptions(int par0)
    {
        EnumOptionsMinimap[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3)
        {
            EnumOptionsMinimap var4 = var1[var3];

            if (var4.returnEnumOrdinal() == par0)
            {
                return var4;
            }
        }
        return null;
    }

    private EnumOptionsMinimap(String par3Str, boolean par4, boolean par5, boolean par6)
    {
        this.enumString = par3Str;
        this.enumFloat = par4;
        this.enumBoolean = par5;
        this.enumList = par6;
    }

    public boolean getEnumFloat()
    {
        return this.enumFloat;
    }

    public boolean getEnumBoolean()
    {
        return this.enumBoolean;
    }
    
    public boolean getEnumList()
    {
        return this.enumList;
    }

    public int returnEnumOrdinal()
    {
        return this.ordinal();
    }

    public String getEnumString()
    {
        return this.enumString;
    }
}
