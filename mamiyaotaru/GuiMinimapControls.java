package net.minecraft.src.mamiyaotaru;

import org.lwjgl.input.Keyboard;

import net.minecraft.src.GameSettings;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.StringTranslate;
import net.minecraft.src.ZanMinimap;

public class GuiMinimapControls extends GuiScreen
{
    /**
     * A reference to the screen object that created this. Used for navigating between screens.
     */
    private GuiScreen parentScreen;

    /** The title string that is displayed in the top-center of the screen. */
    protected String screenTitle = "Controls";

    /** Reference to the Minimap object. */
    private ZanMinimap options;

    /** The ID of the  button that has been pressed. */
    private int buttonId = -1;

    public GuiMinimapControls(GuiScreen par1GuiScreen, ZanMinimap minimap)
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
        StringTranslate var1 = StringTranslate.getInstance();
        int var2 = this.func_73907_g();

        for (int var3 = 0; var3 < this.options.keyBindings.length; ++var3)
        {
            this.controlList.add(new GuiSmallButton(var3, var2 + var3 % 2 * 160, this.height / 6 + 24 * (var3 >> 1), 70, 20, this.options.getOptionDisplayString(var3)));
        }

        this.controlList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168, var1.translateKey("gui.done")));
        this.screenTitle = var1.translateKey("controls.minimap.title");
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        for (int var2 = 0; var2 < this.options.keyBindings.length; ++var2)
        {
            ((GuiButton)this.controlList.get(var2)).displayString = this.options.getOptionDisplayString(var2);
        }

        if (par1GuiButton.id == 200)
        {
            this.mc.displayGuiScreen(this.parentScreen);
        }
        else
        {
            this.buttonId = par1GuiButton.id;
            par1GuiButton.displayString = "> " + this.options.getOptionDisplayString(par1GuiButton.id) + " <"; // what is this even for.. it gets overwritten in drawScreen
        }
    }

    /**
     * Called when the mouse is clicked.
     */
    protected void mouseClicked(int par1, int par2, int par3)
    {
        if (this.buttonId >= 0)
        {
        	// no setting minimap keybinds to mouse button.  Can do so if wanted if I change ZanMinimap to not send every input to Keyboard for processing.  Check if it's mouse first
        //    this.options.setKeyBinding(this.buttonId, -100 + par3);
        //    ((GuiButton)this.controlList.get(this.buttonId)).displayString = this.options.getOptionDisplayString(this.buttonId);
            this.buttonId = -1;
        //    KeyBinding.resetKeyBindingArrayAndHash();
        }
        else
        {
            super.mouseClicked(par1, par2, par3);
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char par1, int par2)
    {
        if (this.buttonId >= 0)
        {
        	if (par2 != Keyboard.KEY_ESCAPE)
        		this.options.setKeyBinding(this.buttonId, par2);
        	else // pressed escape
        		if (buttonId != 1) // do not allow to unbind the menu key
        			this.options.setKeyBinding(this.buttonId, Keyboard.KEY_NONE);// allow us to unbind a key
            ((GuiButton)this.controlList.get(this.buttonId)).displayString = this.options.getOptionDisplayString(this.buttonId);
            this.buttonId = -1;
            KeyBinding.resetKeyBindingArrayAndHash();
        }
        else
        {
            super.keyTyped(par1, par2);
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
    	StringTranslate var1 = StringTranslate.getInstance();
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 20, 16777215);
        int var4 = this.func_73907_g();
        int var5 = 0;

        while (var5 < this.options.keyBindings.length)
        {
            boolean var6 = false;
            int var7 = 0;

            while (true)
            {
                if (var7 < this.options.keyBindings.length) // checking for collisions in the minimap's bindings, and the game's bindings from 0 to the size of the minimap's bindings
                {
                    if (this.options.keyBindings[var5].keyCode == Keyboard.KEY_NONE || (var7 == var5 || this.options.keyBindings[var5].keyCode != this.options.keyBindings[var7].keyCode) && (this.options.keyBindings[var5].keyCode != this.options.game.gameSettings.keyBindings[var7].keyCode))
                    {
                        ++var7;
                        continue;
                    }

                    var6 = true; // collision
                }
                
                if (var7 < this.options.game.gameSettings.keyBindings.length) // continue checking for collisions only among the standard game's keybindings - an array larger than that of the minimap.
                {
                    if (this.options.keyBindings[var5].keyCode == Keyboard.KEY_NONE || this.options.keyBindings[var5].keyCode != this.options.game.gameSettings.keyBindings[var7].keyCode)
                    {
                        ++var7;
                        continue;
                    }

                    var6 = true; // collision
                }

                if (this.buttonId == var5) // buttonId is currently being edited button.  Draw > ??? <
                {
                    ((GuiButton)this.controlList.get(var5)).displayString = "\u00a7f> \u00a7e??? \u00a7f<";
                }
                else if (var6) // key collision, draw red
                {
                    ((GuiButton)this.controlList.get(var5)).displayString = "\u00a7c" + this.options.getOptionDisplayString(var5);
                }
                else // just show current binding
                {
                    ((GuiButton)this.controlList.get(var5)).displayString = this.options.getOptionDisplayString(var5);
                }

                this.drawString(this.fontRenderer, this.options.getKeyBindingDescription(var5), var4 + var5 % 2 * 160 + 70 + 6, this.height / 6 + 24 * (var5 >> 1) + 7, -1);
                ++var5;
                break;
            }
        }
        
        this.drawCenteredString(this.fontRenderer, var1.translateKey("controls.minimap.unbind1"), this.width / 2, this.height / 6 + 115, 16777215);
        this.drawCenteredString(this.fontRenderer, var1.translateKey("controls.minimap.unbind2"), this.width / 2, this.height / 6 + 129, 16777215);


        super.drawScreen(par1, par2, par3);
    }
    
    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed() {
    	this.options.saveAll();
    }  
}
