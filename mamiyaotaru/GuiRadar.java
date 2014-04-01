package net.minecraft.src.mamiyaotaru;

import net.minecraft.src.EnumOptions;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSlider;
import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.StringTranslate;
import net.minecraft.src.ZanMinimap;
import net.minecraft.src.ZanRadar;

public class GuiRadar extends GuiScreen
{
    /**
     * An array of options that can be changed directly from the options GUI.
     */
    private static final EnumOptionsMinimap[] relevantOptions = new EnumOptionsMinimap[] {EnumOptionsMinimap.HIDERADAR, EnumOptionsMinimap.SHOWHOSTILES, EnumOptionsMinimap.SHOWPLAYERS, EnumOptionsMinimap.SHOWNEUTRALS, EnumOptionsMinimap.RADARFILTERING, EnumOptionsMinimap.RADAROUTLINES, EnumOptionsMinimap.SHOWHELMETS};

    /**
     * A reference to the screen object that created this. Used for navigating between screens.
     */
    private final GuiScreen parent;
        
    private final ZanRadar radar;

    /** The title string that is displayed in the top-center of the screen. */
    protected String screenTitle = "Radar Options";

    public GuiRadar(GuiScreen parent,  ZanRadar radar)
    {
    	this.parent = parent;
        this.radar = radar;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        StringTranslate stringTranslate = StringTranslate.getInstance();
        int var2 = 0;
        //this.screenTitle = stringTranslate.translateKey("options.title");
        this.screenTitle = "Radar " + stringTranslate.translateKey("options.title");
        
        for (int t = 0; t < relevantOptions.length; ++t)
        {
            EnumOptionsMinimap option = relevantOptions[t];

      //      if (option.getEnumFloat()) // slider would be a pain
      //      {
      //          this.controlList.add(new GuiSliderMinimap(option.returnEnumOrdinal(), this.width / 2 - 155 + var2 % 2 * 160, this.height / 6 + 24 * (var2 >> 1), option, this.minimap.getKeyBinding(option), this.minimap.getOptionFloatValue(option)));
      //      }
      //      else
      //      {
                GuiSmallButtonMinimap var7 = new GuiSmallButtonMinimap(option.returnEnumOrdinal(), this.width / 2 - 155 + var2 % 2 * 160, this.height / 6 + 24 * (var2 >> 1), option, this.radar.getKeyText(option));

                this.controlList.add(var7);
      //      }

            ++var2;
        }

//      this.controlList.add(new GuiButton(101, this.width / 2 - 152, this.height / 6 + 136 - 6, 150, 20, stringTranslate.translateKey("options.radar"))); // use if I ever get translate going
        //														-152 or plus 2
        this.controlList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168, stringTranslate.translateKey("gui.done")));
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
                this.radar.setOptionValue(((GuiSmallButtonMinimap)par1GuiButton).returnEnumOptions(), 1);
                par1GuiButton.displayString = this.radar.getKeyText(EnumOptionsMinimap.getEnumOptions(par1GuiButton.id));
            }
   
            if (par1GuiButton.id == 200)
            {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(parent);
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
    
    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed() {
    	ZanMinimap.instance.saveAll();
    }
}
