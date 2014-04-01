package net.minecraft.src.mamiyaotaru;

import net.minecraft.src.EnumOptions;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSlider;
import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.StringTranslate;
import net.minecraft.src.ZanMinimap;

public class GuiMinimap extends GuiScreen
{
    /**
     * An array of options that can be changed directly from the options GUI.
     */
    private static EnumOptionsMinimap[] relevantOptions;// = new EnumOptionsMinimap[] {EnumOptionsMinimap.COORDS, EnumOptionsMinimap.HIDE, EnumOptionsMinimap.SHOWNETHER, EnumOptionsMinimap.CAVEMODE, EnumOptionsMinimap.LIGHTING, EnumOptionsMinimap.TERRAIN, EnumOptionsMinimap.SQUARE, EnumOptionsMinimap.OLDNORTH, EnumOptionsMinimap.BEACONS, EnumOptionsMinimap.THREADING};

    /**
     * A reference to the screen object that created this. Used for navigating between screens.
     */    
    private final ZanMinimap minimap;

    /** The title string that is displayed in the top-center of the screen. */
    protected String screenTitle = "Minimap Options";

    public GuiMinimap(ZanMinimap minimap)
    {
        this.minimap = minimap;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
    	if (minimap.motionTrackerExists)
    		relevantOptions = new EnumOptionsMinimap[] {EnumOptionsMinimap.COORDS, EnumOptionsMinimap.HIDE, EnumOptionsMinimap.LOCATION, EnumOptionsMinimap.SIZE, EnumOptionsMinimap.SQUARE, EnumOptionsMinimap.OLDNORTH, EnumOptionsMinimap.BEACONS, EnumOptionsMinimap.CAVEMODE, EnumOptionsMinimap.MOTIONTRACKER};
    	else
    		relevantOptions = new EnumOptionsMinimap[] {EnumOptionsMinimap.COORDS, EnumOptionsMinimap.HIDE, EnumOptionsMinimap.LOCATION, EnumOptionsMinimap.SIZE, EnumOptionsMinimap.SQUARE, EnumOptionsMinimap.OLDNORTH, EnumOptionsMinimap.BEACONS, EnumOptionsMinimap.CAVEMODE};
    		
        StringTranslate stringTranslate = StringTranslate.getInstance();
        int var2 = 0;
        //this.screenTitle = stringTranslate.translateKey("options.title");
        this.screenTitle = stringTranslate.translateKey("options.minimap.title");
        
        for (int t = 0; t < relevantOptions.length; ++t)
        {
            EnumOptionsMinimap option = relevantOptions[t];

      //      if (option.getEnumFloat()) // slider would be a pain
      //      {
      //          this.controlList.add(new GuiSliderMinimap(option.returnEnumOrdinal(), this.width / 2 - 155 + var2 % 2 * 160, this.height / 6 + 24 * (var2 >> 1), option, this.minimap.getKeyBinding(option), this.minimap.getOptionFloatValue(option)));
      //      }
      //      else
      //      {
                GuiSmallButtonMinimap var7 = new GuiSmallButtonMinimap(option.returnEnumOrdinal(), this.width / 2 - 155 + var2 % 2 * 160, this.height / 6 + 24 * (var2 >> 1), option, this.minimap.getKeyText(option));

                this.controlList.add(var7);
                
                if (option.equals(EnumOptionsMinimap.CAVEMODE)) var7.enabled = this.minimap.cavesAllowed;
      //      }

            ++var2;
        }

        GuiButton radarOptionsButton = new GuiButton(101, this.width / 2 - 155, this.height / 6 + 120 - 6, 150, 20, stringTranslate.translateKey("options.minimap.radar"));
        radarOptionsButton.enabled = (this.minimap.radar != null && this.minimap.radarAllowed); // deactivate button if class is missing, or if radar is disabled
        this.controlList.add(radarOptionsButton);
        this.controlList.add(new GuiButton(103, this.width / 2 + 5, this.height / 6 + 120 - 6, 150, 20, stringTranslate.translateKey("options.minimap.detailsperformance"))); 
        this.controlList.add(new GuiButton(102, this.width / 2 - 155, this.height / 6 + 144 - 6, 150, 20, stringTranslate.translateKey("options.controls"))); 
        this.controlList.add(new GuiButton(100, this.width / 2 + 5, this.height / 6 + 144 - 6, 150, 20, stringTranslate.translateKey("options.minimap.waypoints"))); 
        this.controlList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168, stringTranslate.translateKey("menu.returnToGame")));


    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        if (par1GuiButton.enabled)
        {
            if (par1GuiButton.id < 100 && par1GuiButton instanceof GuiSmallButtonMinimap)
            {
                this.minimap.setOptionValue(((GuiSmallButtonMinimap)par1GuiButton).returnEnumOptions(), 1);
                par1GuiButton.displayString = this.minimap.getKeyText(EnumOptionsMinimap.getEnumOptions(par1GuiButton.id));
            }
            
            if (par1GuiButton.id == 103)
            {
           		this.mc.displayGuiScreen(new GuiMinimapPerformance(this, minimap));
            }

            if (par1GuiButton.id == 102)
            {
           		this.mc.displayGuiScreen(new GuiMinimapControls(this, minimap));
            }

            if (par1GuiButton.id == 101)
            {
           		this.mc.displayGuiScreen(new GuiRadar(this, minimap.radar));
            }

            if (par1GuiButton.id == 100)
            {
                this.mc.displayGuiScreen(new GuiWaypoints(this, minimap));
            }
            
            if (par1GuiButton.id == 200)
            {
                this.mc.displayGuiScreen((GuiScreen)null);
            }
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
    	//this.minimap.onTickInGame(this.mc); // if we give up on controlling ingamegui
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 20, 16777215);
        super.drawScreen(par1, par2, par3);
    }
    
    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed() {
    	this.minimap.saveAll();
    }    
    
}
