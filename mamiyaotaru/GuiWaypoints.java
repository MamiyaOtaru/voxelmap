package net.minecraft.src.mamiyaotaru;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
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
    private boolean addClicked = false;
    
    /** This GUI's tooltip text or null if no slot is being hovered. */
    private String tooltip = null;
    
    protected Waypoint selectedWaypoint = null;
    
    protected Waypoint newWaypoint = null; 

	public FontRenderer getFontRenderer() {
		return this.fontRenderer;
	}

    public GuiWaypoints(GuiScreen parentScreen, ZanMinimap minimap)
    {
    	super();
    	this.parentScreen = parentScreen;
        this.minimap = minimap;
    //    this.fontRenderer = super.fontRenderer;
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
        
        this.controlList.add(buttonEdit = new GuiButton(-1, this.width / 2 - 154, this.height - 52, 100, 20, stringTranslate.translateKey("selectServer.edit")));
        this.controlList.add(buttonDelete = new GuiButton(-2, this.width / 2 - 50, this.height - 52, 100, 20, stringTranslate.translateKey("selectServer.delete")));
        this.controlList.add(buttonTeleport = new GuiButton(-3, this.width / 2 + 4 + 50, this.height - 52, 100, 20, "Teleport to"));
        
        this.controlList.add(new GuiButton(-4, this.width / 2 - 154, this.height - 28, 152, 20, "New Waypoint"));
        this.controlList.add(new GuiButton(-200, this.width / 2 + 2, this.height - 28, 152, 20, stringTranslate.translateKey("gui.done")));
        
        // GUI is inited every time it comes up including after leaving another GUI - like the "really delete?" screen.
        // if we answer yes there, WP is deleted, selectedWaypoint is set to null and appropriate buttons are thus disabled here
        boolean isSomethingSelected = this.selectedWaypoint != null;
        this.buttonEdit.enabled = isSomethingSelected;
        this.buttonDelete.enabled = isSomethingSelected;
        this.buttonTeleport.enabled = isSomethingSelected && this.canTeleport();
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
                //when is this called? herp
                par1GuiButton.displayString = this.minimap.getKeyText(EnumOptionsMinimap.getEnumOptions(par1GuiButton.id));
            }

            if (par1GuiButton.id == -1)
            {
            	editWaypoint(selectedWaypoint);
                //edit shit
            }
            
            if (par1GuiButton.id == -4)
            {
            	addWaypoint();
                //edit new waypoint
            }
            
            if (par1GuiButton.id == -3) 
            {
            	//teleport
            	if (this.minimap.game.isIntegratedServerRunning()) {
            		this.minimap.game.thePlayer.sendChatMessage("/ztp " + this.selectedWaypoint.name);
            		this.minimap.game.displayGuiScreen((GuiScreen)null);
            	}
            	else { // multiplayer
            		if (this.minimap.game.thePlayer.dimension != -1) { // not in nether
            			if (this.selectedWaypoint.y > 0) {// 3d waypoint
            				this.minimap.game.thePlayer.sendChatMessage("/tp " + this.minimap.game.thePlayer.username + " " + this.selectedWaypoint.x + " " + this.selectedWaypoint.y + " " + this.selectedWaypoint.z); 
                    		this.minimap.game.thePlayer.sendChatMessage("/tppos " + this.selectedWaypoint.x + " " + this.selectedWaypoint.y + " " + this.selectedWaypoint.z);
            			}
            			else { 
            				this.minimap.game.thePlayer.sendChatMessage("/tp " + this.minimap.game.thePlayer.username + " " + this.selectedWaypoint.x + " " + "128" + " " + this.selectedWaypoint.z); // hopefully above everything.  this one sends you to that y location, so don't do all the way up
            				this.minimap.game.thePlayer.sendChatMessage("/tppos " + this.selectedWaypoint.x + " " + "256" + " " + this.selectedWaypoint.z); // hopefully above everything.  this one puts you on the ground under the given y value, so 256 is fine
            			}
                		this.minimap.game.displayGuiScreen((GuiScreen)null);
            		}
            	}
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
            	//waypoint info already changed in waypoint edit gui, just save it.  actually no it isn't.  
            	//We work on a copy, so we can cancel.  If we don't cancel, don't discard and copy it over from the temp to selected
            	//Only necessary because of color.  Other info (X, Y, Name etc) is not applied unless one presses OK.  Cancel discards
            	//but selecting a color immediately applies it to the worked on waypoint (so it renders correctly) so we need to work on a copy
            	//could fix if I store the color value in the edit screen and not apply until OK and get the color to render from the local value
            	//done
                /*selectedWaypoint.name = newWaypoint.name;
                selectedWaypoint.x = newWaypoint.x;
                selectedWaypoint.z = newWaypoint.z;
                selectedWaypoint.y = newWaypoint.y;
                selectedWaypoint.enabled = newWaypoint.enabled;
                selectedWaypoint.red = newWaypoint.red;
                selectedWaypoint.green = newWaypoint.green;
                selectedWaypoint.blue = newWaypoint.blue;
                selectedWaypoint.imageSuffix = newWaypoint.imageSuffix;*/
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
                this.setSelectedWaypoint(newWaypoint);
            }
            //else { // if I want to deselect any previously selected waypoint on cancelling new
            // 	this.setSelectedWaypoint(null);
            //}

            this.mc.displayGuiScreen(this);
        }
    }
    
	protected void setSelectedWaypoint(Waypoint waypoint) {
		this.selectedWaypoint = waypoint;
        boolean isSomethingSelected = this.selectedWaypoint != null;
        buttonEdit.enabled = isSomethingSelected;
        buttonDelete.enabled = isSomethingSelected;
        buttonTeleport.enabled = isSomethingSelected && canTeleport();
	}
	
    protected void editWaypoint(Waypoint waypoint) {
        this.editClicked = true;
        this.mc.displayGuiScreen(new GuiScreenAddWaypoint(this, waypoint));
    }
    
    protected void addWaypoint() {
        this.addClicked = true;
        float r, g, b;
		if (this.minimap.wayPts.size() == 0) { // green for the first one
			r = 0;
			g = 1;
			b = 0;
		}
		else { // random for later ones
			r = minimap.generator.nextFloat();
			g = minimap.generator.nextFloat();
			b = minimap.generator.nextFloat();
		}
        newWaypoint = new Waypoint("", (minimap.game.thePlayer.dimension != -1)?minimap.xCoord():minimap.xCoord()*8, (minimap.game.thePlayer.dimension != -1)?minimap.zCoord():minimap.zCoord()*8, minimap.yCoord()-1, true, r, g, b, "");
        this.mc.displayGuiScreen(new GuiScreenAddWaypoint(this, this.newWaypoint));
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
   	
	public boolean canTeleport() {
		boolean notInNether = (this.minimap.game.thePlayer.dimension != -1);
		boolean singlePlayer = (this.minimap.game.isIntegratedServerRunning());
		if (singlePlayer)
			return (MinecraftServer.getServer().getConfigurationManager().areCommandsAllowed(this.minimap.game.thePlayer.username));
		else
			return notInNether;

		
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
