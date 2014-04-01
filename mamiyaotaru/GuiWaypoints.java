package net.minecraft.src.mamiyaotaru;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EnumOptions;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiMultiplayer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiScreenAddServer;
import net.minecraft.src.GuiSlider;
import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.GuiYesNo;
import net.minecraft.src.ServerData;
import net.minecraft.src.StringTranslate;
import net.minecraft.src.Tessellator;
import net.minecraft.src.ZanMinimap;

public class GuiWaypoints extends GuiScreen
{
    /**  
     * The FontRenderer used by GuiScreen
     * needed here cause the super's is protected, and the slot class can't see it
     * */
	//protected FontRenderer fontRenderer;
	
    /**  
     * Reference to the Minecraft object.
     * needed here cause the super's is protected, and the slot class can't see it
     * */
    //protected Minecraft mc;
	
    /**
     * A reference to the screen object that created this. Used for navigating between screens.
     */    
    private final GuiScreen parentScreen;
    
    /**
     * reference to the minimap (and all its public variables.. going to have to refactor)
     */
    protected final ZanMinimap minimap;

    /** The title string that is displayed in the top-center of the screen. */
    protected String screenTitle = "Waypoints";
    
    /** This GUI's waypoint list. */
    private GuiSlotWaypoints waypointList;
    
    /** The 'Edit' button */
    private GuiButton buttonEdit;
    
    /** The 'Edit' button was clicked */
    protected boolean editClicked = false;

    /** The 'Delete' button */
    private GuiButton buttonDelete;
    
    /** The 'Delete' button was clicked */
    private boolean deleteClicked = false;
    
    /** The 'Teleport' button */
    private GuiButton buttonTeleport;
    
    /** The 'Add' button was clicked */
    protected boolean addClicked = false;
    
    /** This GUI's tooltip text or null if no slot is being hovered. */
    private String tooltip = null;
    
    protected Waypoint selectedWaypoint = null;
    
    protected Waypoint newWaypoint = null; 

	public boolean canTeleport = false;
 
	public FontRenderer getFontRenderer() {
		return this.fontRenderer;
	}

    public GuiWaypoints(GuiScreen parentScreen, ZanMinimap minimap)
    {
    	super();
    	this.parentScreen = parentScreen;
        this.minimap = minimap;
    //    this.fontRenderer = super.fontRenderer;
   //     this.mc = super.mc;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        StringTranslate stringTranslate = StringTranslate.getInstance();
        int var2 = 0;
        //this.screenTitle = stringTranslate.translateKey("options.title");
        this.screenTitle = "Waypoints";
        
        this.waypointList = new GuiSlotWaypoints(this);
        this.waypointList.registerScrollButtons(this.controlList, 7, 8);
        
  /*      for (int t = 0; t < minimap.wayPts.size(); ++t)
        {
                GuiSmallButtonMinimap waypointButton = new GuiSmallButtonMinimap(t, this.width / 2 - 155 + var2 % 2 * 160, this.height / 6 + 24 * (var2 >> 1), this.minimap.wayPts.get(t).name);

                this.controlList.add(waypointButton);
      //      }

            ++var2;
        }*/

//      this.controlList.add(new GuiButton(101, this.width / 2 - 152, this.height / 6 + 136 - 6, 150, 20, stringTranslate.translateKey("options.radar"))); // use if I ever get translate going
        
        
        buttonEdit = new GuiButton(-1, this.width / 2 - 154, this.height - 52, 100, 20, "Edit");
        this.controlList.add(buttonEdit);
        buttonDelete = new GuiButton(-2, this.width / 2 - 50, this.height - 52, 100, 20, "Remove");
        this.controlList.add(buttonDelete);
        buttonTeleport = new GuiButton(-3, this.width / 2 + 4 + 50, this.height - 52, 100, 20, "Teleport to");
        this.controlList.add(buttonTeleport);
        
        this.controlList.add(new GuiButton(-4, this.width / 2 - 154, this.height - 28, 152, 20, "New Waypoint"));
        this.controlList.add(new GuiButton(-200, this.width / 2 + 2, this.height - 28, 152, 20, stringTranslate.translateKey("gui.done")));
        boolean isSomethingSelected = this.selectedWaypoint != null;
        this.buttonEdit.enabled = isSomethingSelected;
        this.buttonDelete.enabled = isSomethingSelected;
        this.buttonTeleport.enabled = isSomethingSelected;
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        if (par1GuiButton.enabled)
        {
            if (par1GuiButton.id >= 0 && par1GuiButton instanceof GuiSmallButtonMinimap) //waypoint button
            {
                //this.minimap.setOptionValue(((GuiSmallButtonMinimap)par1GuiButton).returnEnumOptions(), 1);
                par1GuiButton.displayString = this.minimap.getKeyText(EnumOptionsMinimap.getEnumOptions(par1GuiButton.id));
            }

            if (par1GuiButton.id == -1)
            {
                this.editClicked = true;
                newWaypoint = new Waypoint(selectedWaypoint.name, selectedWaypoint.x, selectedWaypoint.z, selectedWaypoint.y, selectedWaypoint.enabled, selectedWaypoint.red, selectedWaypoint.green, selectedWaypoint.blue, selectedWaypoint.imageSuffix);
                this.mc.displayGuiScreen(new GuiScreenAddWaypoint(this, this.newWaypoint)); // = new ServerData(var9.serverName, var9.serverIP)));
                //edit shit
            }
            
            if (par1GuiButton.id == -4)
            {
                this.addClicked = true;
                newWaypoint = new Waypoint("", (minimap.game.thePlayer.dimension != -1)?minimap.xCoord():minimap.xCoord()*8, (minimap.game.thePlayer.dimension != -1)?minimap.zCoord():minimap.zCoord()*8, minimap.yCoord()-1, true, 0, 1, 0, "");
                this.mc.displayGuiScreen(new GuiScreenAddWaypoint(this, this.newWaypoint)); // = new ServerData(var9.serverName, var9.serverIP)));
                //edit shit
            }

            if (par1GuiButton.id == -2)
            {
                String var2 = this.selectedWaypoint.name;

                if (var2 != null)
                {
                    this.deleteClicked = true;
                    StringTranslate var3 = StringTranslate.getInstance();
                    String var4 = var3.translateKey("Are you sure you want to remove this waypoint?");
                    String var5 = "\'" + var2 + "\' " + var3.translateKey("selectServer.deleteWarning");
                    String var6 = var3.translateKey("selectServer.deleteButton");
                    String var7 = var3.translateKey("gui.cancel");
                    GuiYesNo var8 = new GuiYesNo(this, var4, var5, var6, var7, this.minimap.wayPts.indexOf(selectedWaypoint));
                    this.mc.displayGuiScreen(var8);
                }
            }
 
            if (par1GuiButton.id == -200)
            {
                this.mc.displayGuiScreen(parentScreen);
            }
        }
    }
    
    public void confirmClicked(boolean par1, int par2)
    {
        if (this.deleteClicked)
        {
            this.deleteClicked = false;

            if (par1)
            {
            	this.minimap.deleteWaypoint(selectedWaypoint);
            	this.selectedWaypoint = null;
            }

            this.mc.displayGuiScreen(this);
        }
        if (this.editClicked)
        {
            this.editClicked = false;

            if (par1)
            {
            	//waypoint indo already changed in waypoint edit gui, just save it
                selectedWaypoint.name = newWaypoint.name;
                selectedWaypoint.x = newWaypoint.x;
                selectedWaypoint.z = newWaypoint.z;
                selectedWaypoint.y = newWaypoint.y;
                selectedWaypoint.enabled = newWaypoint.enabled;
                selectedWaypoint.red = newWaypoint.red;
                selectedWaypoint.green = newWaypoint.green;
                selectedWaypoint.blue = newWaypoint.blue;
                selectedWaypoint.imageSuffix = newWaypoint.imageSuffix;
            	minimap.saveWaypoints();
            }

            this.mc.displayGuiScreen(this);
        }
        if (this.addClicked)
        {
            this.addClicked = false;

            if (par1)
            {
            	//save waypoint
                minimap.addWaypoint(newWaypoint);
            }

            this.mc.displayGuiScreen(this);
        }
    }
    
	protected void setSelectedWaypoint(Waypoint waypoint) {
		this.selectedWaypoint = waypoint;
	}
	
	protected void toggleWaypointVisibility() {
		selectedWaypoint.enabled = !selectedWaypoint.enabled;
		minimap.saveWaypoints();
	}

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
    	this.tooltip = null;
        this.waypointList.drawScreen(par1, par2, par3);
      //  this.drawDefaultBackground(); // called from the slot
        this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 20, 16777215);
        super.drawScreen(par1, par2, par3);
        if (this.tooltip != null)
        {
        	drawTooltip(tooltip, par1, par2);
        }
       // ((GuiButton)this.controlList.get(1)).enabled = this.waypointList.
    }

 /*   public void drawDefaultBackground()
    {
        // Draws either a gradient over the background screen (when it exists) or a flat gradient over background.png
        this.drawWorldBackground(0);
        // just draws the background png
        //this.drawBackground(0);
    } */
    	
    protected void drawTooltip(String par1Str, int par2, int par3)
    {
        if (par1Str != null)
        {
            int var4 = par2 + 12;
            int var5 = par3 - 12;
            int var6 = this.fontRenderer.getStringWidth(par1Str);
            this.drawGradientRect(var4 - 3, var5 - 3, var4 + var6 + 3, var5 + 8 + 3, -1073741824, -1073741824);
            this.fontRenderer.drawStringWithShadow(par1Str, var4, var5, -1);
        }
    }
    
   	static String setTooltip(GuiWaypoints par0GuiWaypoints, String par1Str)
    {
        return par0GuiWaypoints.tooltip = par1Str;
    }
   	
    /**
     * Return buttonEdit GuiButton
     */
    static GuiButton getButtonEdit(GuiWaypoints par0GuiWaypoints)
    {
        return par0GuiWaypoints.buttonEdit;
    }

    /**
     * Return buttonDelete GuiButton
     */
    static GuiButton getButtonDelete(GuiWaypoints par0GuiWaypoints)
    {
        return par0GuiWaypoints.buttonDelete;
    }
    
    /**
     * Return buttonTeleport GuiButton
     */
    static GuiButton getButtonTeleport(GuiWaypoints par0GuiWaypoints)
    {
        return par0GuiWaypoints.buttonTeleport;
    }

    
    
}
