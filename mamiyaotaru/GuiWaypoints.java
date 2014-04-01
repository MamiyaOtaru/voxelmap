package net.minecraft.src.mamiyaotaru;

import net.minecraft.src.EnumOptions;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSlider;
import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.StringTranslate;
import net.minecraft.src.ZanMinimap;

public class GuiWaypoints extends GuiScreen
{

    /**
     * A reference to the screen object that created this. Used for navigating between screens.
     */    
    private final GuiScreen parentScreen;
    
    /**
     * reference to the minimap (and all its public variables.. going to have to refactor)
     */
    private final ZanMinimap minimap;

    /** The title string that is displayed in the top-center of the screen. */
    protected String screenTitle = "Minimap Options";

    public GuiWaypoints(GuiScreen parentScreen, ZanMinimap minimap)
    {
    	this.parentScreen = parentScreen;
        this.minimap = minimap;
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
        
        for (int t = 0; t < minimap.wayPts.size(); ++t)
        {
                GuiSmallButtonMinimap waypointButton = new GuiSmallButtonMinimap(t, this.width / 2 - 155 + var2 % 2 * 160, this.height / 6 + 24 * (var2 >> 1), this.minimap.wayPts.get(t).name);

                this.controlList.add(waypointButton);
      //      }

            ++var2;
        }

//      this.controlList.add(new GuiButton(101, this.width / 2 - 152, this.height / 6 + 136 - 6, 150, 20, stringTranslate.translateKey("options.radar"))); // use if I ever get translate going
        this.controlList.add(new GuiButton(-1, this.width / 2 - 152, this.height / 6 + 136 - 6, 150, 20, "Add"));
        this.controlList.add(new GuiButton(-2, this.width / 2 + 2, this.height / 6 + 136 - 6, 150, 20, "Remove"));
        this.controlList.add(new GuiButton(-200, this.width / 2 - 100, this.height / 6 + 168, stringTranslate.translateKey("gui.done")));
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
                this.minimap.saveAll();
                //this.mc.displayGuiScreen(new GuiRadarSettings(this, minimap);
            }

            if (par1GuiButton.id == -2)
            {
                this.minimap.saveAll();
                //this.mc.displayGuiScreen(new GuiWaypointSettings(this, minimap);
            }
            
            if (par1GuiButton.id == -200)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(parentScreen);
            }
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 20, 16777215);
        super.drawScreen(par1, par2, par3);
    }
}
