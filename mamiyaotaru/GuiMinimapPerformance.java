package net.minecraft.src.mamiyaotaru;

import org.lwjgl.input.Keyboard;

import net.minecraft.src.GameSettings;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.StringTranslate;
import net.minecraft.src.ZanMinimap;

public class GuiMinimapPerformance extends GuiScreen
{
    /**
     * An array of options that can be changed directly from the options GUI.
     */
    private static final EnumOptionsMinimap[] relevantOptions = new EnumOptionsMinimap[] {EnumOptionsMinimap.LIGHTING, EnumOptionsMinimap.TERRAIN, EnumOptionsMinimap.WATERTRANSPARENCY, EnumOptionsMinimap.BLOCKTRANSPARENCY, EnumOptionsMinimap.BIOMES, EnumOptionsMinimap.FILTERING};
    
    /**
     * A reference to the screen object that created this. Used for navigating between screens.
     */
    private GuiScreen parentScreen;

    /** The title string that is displayed in the top-center of the screen. */
    protected String screenTitle = "Details / Performance";

    /** Reference to the Minimap object. */
    private ZanMinimap options;

    /** The ID of the  button that has been pressed. */
    private int buttonId = -1;

    public GuiMinimapPerformance(GuiScreen par1GuiScreen, ZanMinimap minimap)
    {
        this.parentScreen = par1GuiScreen;
        this.options = minimap;
    }

    private int func_73907_g()
    {
        return this.width / 2 - 155;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        StringTranslate stringTranslate = StringTranslate.getInstance();
        this.screenTitle = "Details / Performance";
        
        int var1 = this.func_73907_g();
        int var2 = 0;

        for (int t = 0; t < relevantOptions.length; ++t)
        {
            EnumOptionsMinimap option = relevantOptions[t];

      //      if (option.getEnumFloat()) // slider would be a pain
      //      {
      //          this.controlList.add(new GuiSliderMinimap(option.returnEnumOrdinal(), this.width / 2 - 155 + var2 % 2 * 160, this.height / 6 + 24 * (var2 >> 1), option, this.minimap.getKeyBinding(option), this.minimap.getOptionFloatValue(option)));
      //      }
      //      else
      //      {
            String text = this.options.getKeyText(option);
            if ((option.returnEnumOrdinal() == 19 || option.returnEnumOrdinal()== 20 || option.returnEnumOrdinal()== 21) && !options.multicore && options.getOptionBooleanValue(option))
            	text = "\u00a7c"+text;
            GuiSmallButtonMinimap var7 = new GuiSmallButtonMinimap(option.returnEnumOrdinal(), var1 + var2 % 2 * 160, this.height / 6 + 24 * (var2 >> 1), option, text);

            this.controlList.add(var7);
            //      }

            ++var2;
        }

        //int buttonID = 0;
        //this.controlList.add(new GuiSmallButton(buttonID, var2 + buttonID % 2 * 160, this.height / 6 + 24 * (buttonID >> 1), 70, 20, "Location: "));
      
        this.controlList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168, stringTranslate.translateKey("gui.done")));

    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {

         //   ((GuiButton)this.controlList.get(var2)).displayString = this.options.getOptionDisplayString(var2);

        if (par1GuiButton.id < 100 && par1GuiButton instanceof GuiSmallButtonMinimap)
        {
            this.options.setOptionValue(((GuiSmallButtonMinimap)par1GuiButton).returnEnumOptions(), 1);
            String perfBomb = "";
            if ((par1GuiButton.id == 19 || par1GuiButton.id == 20 || par1GuiButton.id == 21) && !options.multicore && options.getOptionBooleanValue(EnumOptionsMinimap.getEnumOptions(par1GuiButton.id)))
            	perfBomb = "\u00a7c";
            //System.out.println(par1GuiButton.id + " " + EnumOptionsMinimap.getEnumOptions(par1GuiButton.id).ordinal());
            par1GuiButton.displayString = perfBomb + this.options.getKeyText(EnumOptionsMinimap.getEnumOptions(par1GuiButton.id));
        }
        if (par1GuiButton.id == 200)
        {
            this.mc.displayGuiScreen(this.parentScreen);
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
    	this.options.saveAll();
    }  
    
}
