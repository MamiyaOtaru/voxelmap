package net.minecraft.src.mamiyaotaru;

import org.lwjgl.input.Keyboard;

public enum EnumOptionsMinimap
{
//	out.println("Zoom Key:" + Keyboard.getKeyName(zoomKey));
//	out.println("Menu Key:" + Keyboard.getKeyName(menuKey));

	
	/*COORDS("options.minimapCoords", false, true), // use if I ever do translations, or built in options
	SHOWNETHER("options.minimapShowInNether", false, true),
	CAVEMODE("options.minimapCaveMode", false, true),
	LIGHTING("option.minimapDynamicLighting", false, true),
	TERRAIN("option.minimapTerrainDepth", false, true),
	SQUARE("option.minimapSquareMap", false, true),
	OLDNORTH("option.minimapOldNorth", false, true),
	BEACONS("option.minimapBeacon", false, true),
	WELCOME("option.minimapWelcome", false, true),
	THREADING("option.minimapThreading", false, true),
	ZOOM("option.minimapZoom", false, true); */
	
	COORDS("Display Coordinates", false, true, false),
	HIDE("Hide Minimap", false, true, false),
	SHOWNETHER("Function in Nether", false, true, false),
	CAVEMODE("Enable Cave Mode", false, true, false),
	LIGHTING("Dynamic Lighting", false, true, false),
	TERRAIN("Terrain Depth", false, false, true),
	SQUARE("Square Map", false, true, false),
	OLDNORTH("Old North", false, true, false),
	BEACONS("Ingame Waypoints", false, false, true),
	WELCOME("Welcome Screen", false, true, false),
	THREADING("Threading", false, true, false),
	ZOOM("option.minimapZoom", false, true, false),
	MOTIONTRACKER("Motion Tracker", false, true, false),
	HIDERADAR("Hide Radar", false, true, false),
	SHOWHOSTILES("Show Hostiles", false, true, false),
	SHOWPLAYERS("Show Players", false, true, false),
	SHOWNEUTRALS("Show Neutrals", false, true, false), 
	LOCATION("Location", false, false, true),
	FILTERING("Filtering", false, true, false),
	TRANSPARENCY("Transparency", false, true, false),
	BIOMES("Biomes", false, true, false),
	SIZE("Size", false, false, true);

	
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
