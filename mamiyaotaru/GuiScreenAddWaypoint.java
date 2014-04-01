package net.minecraft.src.mamiyaotaru;

import java.util.Random;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiTextField;
import net.minecraft.src.StringTranslate;
import net.minecraft.src.Tessellator;
import net.minecraft.src.ZanMinimap;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class GuiScreenAddWaypoint extends GuiScreen
{
    /** This GUI's parent GUI. */
    private GuiWaypoints parentGui;
    private ZanMinimap minimap;
    private GuiTextField waypointName;
    private GuiTextField waypointX;
    private GuiTextField waypointZ;
    private GuiTextField waypointY;
    private GuiButton buttonEnabled;
    private Waypoint waypoint;
    private boolean choosingColor = false;
    private float red;
    private float green;
    private float blue;
    private boolean enabled;
    
	private Random generator = new Random();

    public GuiScreenAddWaypoint(GuiWaypoints par1GuiScreen, Waypoint par2Waypoint)
    {
        this.parentGui = par1GuiScreen;
        this.minimap = ZanMinimap.getInstance();
        this.waypoint = par2Waypoint;
        // get colors of waypoint to display and act on (applied when accepted)
        // actually applying them directly now.  store here to restore to waypoint on cancel (or escape)
        red = waypoint.red;
        green = waypoint.green;
        blue = waypoint.blue;
        enabled = waypoint.enabled;
    }
    
    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        this.waypointName.updateCursorCounter();
        this.waypointX.updateCursorCounter();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        StringTranslate var1 = StringTranslate.getInstance();
        Keyboard.enableRepeatEvents(true);
        this.controlList.clear();
    
        this.controlList.add(new GuiButton(0,  this.width / 2 - 155, this.height / 6 + 168, 150, 20, var1.translateKey("addServer.add")));
        this.controlList.add(new GuiButton(1, this.width / 2 + 5, this.height / 6 + 168, 150, 20, var1.translateKey("gui.cancel")));
        this.waypointName = new GuiTextField(this.fontRenderer, this.width / 2 - 100, this.height / 6 + 41 * 0 + 13, 200, 20);
        this.waypointName.setFocused(true);
        //if (this.waypoint == null) System.out.println("fail");
        this.waypointName.setText(this.waypoint.name);
        this.waypointX = new GuiTextField(this.fontRenderer, this.width / 2 - 100, this.height / 6 + 41 * 1 + 13, 56, 20);
        this.waypointX.setMaxStringLength(128);
        this.waypointX.setText("" + this.waypoint.x);
        this.waypointZ = new GuiTextField(this.fontRenderer, this.width / 2 - 28, this.height / 6 + 41 * 1 + 13, 56, 20);
        this.waypointZ.setMaxStringLength(128);
        this.waypointZ.setText("" + this.waypoint.z);
        this.waypointY = new GuiTextField(this.fontRenderer, this.width / 2 + 44, this.height / 6 + 41 * 1 + 13, 56, 20);
        this.waypointY.setMaxStringLength(128);
        this.waypointY.setText("" + this.waypoint.y);
        this.controlList.add(buttonEnabled = new GuiButton(2, this.width / 2 - 50, this.height / 6 + 41 * 2 + 6, 100, 20, "Enabled: " + ((waypoint.enabled)?"On":"Off")));
        ((GuiButton)this.controlList.get(0)).enabled = this.waypointName.getText().length() > 0; // && this.waypointX.getText().length() > 0 && this.waypointX.getText().split(":").length > 0; // what's this stuff..  
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        if (par1GuiButton.enabled)
        {
        	if (par1GuiButton.id == 2)
        	{
        		waypoint.enabled = !waypoint.enabled;
        	}
            if (par1GuiButton.id == 1)
            {
    			waypoint.red = red;
    			waypoint.green = green;
    			waypoint.blue = blue;
    			waypoint.enabled = enabled; 
            	if (parentGui != null) // called from WaypointGUI
            		this.parentGui.confirmClicked(false, 0);
            	else // called from elsewhere, like the minimap class itself
            		minimap.game.displayGuiScreen(null);
            }
            else if (par1GuiButton.id == 0)
            {
            	// accept waypoint
            	this.waypoint.name = this.waypointName.getText();
            	this.waypoint.x = Integer.parseInt(this.waypointX.getText());
            	this.waypoint.z = Integer.parseInt(this.waypointZ.getText());
            	this.waypoint.y = Integer.parseInt(this.waypointY.getText());
            	if (parentGui != null) // called from WaypointGUI
            		this.parentGui.confirmClicked(true, 0);
            	else { // called from elsewhere, like ZanMinimap
            		minimap.addWaypoint(waypoint);
            		minimap.game.displayGuiScreen(null);
            	}
            }
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char par1, int par2)
    {
        this.waypointName.textboxKeyTyped(par1, par2);
        this.waypointX.textboxKeyTyped(par1, par2);
        this.waypointZ.textboxKeyTyped(par1, par2);
        this.waypointY.textboxKeyTyped(par1, par2);

        if (par1 == 9)
        {
            if (this.waypointName.isFocused())
            {
                this.waypointName.setFocused(false);
                this.waypointX.setFocused(true);
                this.waypointZ.setFocused(false);
                this.waypointY.setFocused(false);
            }
            else if (this.waypointX.isFocused())
            {
                this.waypointName.setFocused(false);
                this.waypointX.setFocused(false);
                this.waypointZ.setFocused(true);
                this.waypointY.setFocused(false);
            }
            else if (this.waypointZ.isFocused())
            {
                this.waypointName.setFocused(false);
                this.waypointX.setFocused(false);
                this.waypointZ.setFocused(false);
                this.waypointY.setFocused(true);
            }
            else if (this.waypointY.isFocused())
            {
                this.waypointName.setFocused(true);
                this.waypointX.setFocused(false);
                this.waypointZ.setFocused(false);
                this.waypointY.setFocused(false);
            }
        }

        if (par1 == 13)
        {
            this.actionPerformed((GuiButton)this.controlList.get(0));
        }
        boolean acceptable = this.waypointName.getText().length() > 0;
        try {
        	int x = Integer.parseInt(this.waypointX.getText());
        	acceptable = (acceptable && true);
        }
        catch (NumberFormatException e) {
        	//System.out.println("bad x");
        	acceptable = false;
        }
        try {
        	int z = Integer.parseInt(this.waypointZ.getText());
        	acceptable = (acceptable && true);
        }
        catch (NumberFormatException e) {
        	//System.out.println("bad z");
        	acceptable = false;
        }
        try {
        	int y = Integer.parseInt(this.waypointY.getText());
        	acceptable = (acceptable && true);
        }
        catch (NumberFormatException e) {
        	//System.out.println("bad y");
        	acceptable = false;
        }
        ((GuiButton)this.controlList.get(0)).enabled = acceptable;
        if (par2 == Keyboard.KEY_ESCAPE) { // undo changes.  still deciding if I do this or store changes locally and apply on accept
			waypoint.red = red;
			waypoint.green = green;
			waypoint.blue = blue;
			waypoint.enabled = enabled; 
        }
        super.keyTyped(par1, par2); // allows us to escape close.  Needed line since we are overriding GuiScreen's keyTyped with this one, so that method's handling of escape never runs unless we call super's version of the method here

    }

    /**
     * Called when the mouse is clicked.
     */
    protected void mouseClicked(int par1, int par2, int par3)
    {
    	if (!choosingColor) { 
    		super.mouseClicked(par1, par2, par3);
    		this.waypointName.mouseClicked(par1, par2, par3);
    		this.waypointX.mouseClicked(par1, par2, par3);
    		this.waypointZ.mouseClicked(par1, par2, par3);
    		this.waypointY.mouseClicked(par1, par2, par3);
    		if (par1 >= this.width / 2 + 29 && par1 <= this.width / 2 + 45 && par2 >= this.height / 6 + 41 * 3 - 5 && par2 <= this.height / 6 + 41 * 3 + 5)
    		{
    			this.choosingColor = true;
    			//waypoint.red = generator.nextFloat();
    			//waypoint.green = generator.nextFloat();
    			//waypoint.blue = generator.nextFloat();
    		}
    	}
    	else {
    		if (par1 >= this.width / 2 -128 && par1 <= this.width / 2 + 128 && par2 >= this.height / 2 - 128 && par2 <= this.height / 2 + 128) { // clicked on the color picker
    			// check if color is chosen.  check x, y, get color of pixel at corresponding spot on color picker image
    			int color = this.minimap.colorManager.colorPicker.getRGB(par1 - (this.width / 2 - 128), par2 - (this.height / 2 - 128));
    			waypoint.red = (float)(color >> 16 & 255)/255;
    			waypoint.green = (float)(color >> 8 & 255)/255;
    			waypoint.blue = (float)(color >> 0 & 255)/255;
    			this.choosingColor = false;
    		}    		
    	}
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
    	buttonEnabled.displayString = "Enabled: " + ((waypoint.enabled)?"On":"Off");
        StringTranslate var4 = StringTranslate.getInstance();
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, ((this.parentGui != null) && this.parentGui.editClicked)?"Edit Waypoint":"New Waypoint", this.width / 2, 20, 16777215);
        //  this.height / 6 + 24 * 
        this.drawString(this.fontRenderer, var4.translateKey("Waypoint Name"), this.width / 2 - 100, this.height / 6 + 41 * 0, 10526880);
        this.drawString(this.fontRenderer, var4.translateKey("X"), this.width / 2 - 100, this.height / 6 + 41 * 1, 10526880);
        this.drawString(this.fontRenderer, var4.translateKey("Z"), this.width / 2 - 28, this.height / 6 + 41 * 1, 10526880);
        this.drawString(this.fontRenderer, var4.translateKey("Y"), this.width / 2 + 44, this.height / 6 + 41 * 1, 10526880);
        this.drawString(this.fontRenderer, "Choose Color: ", this.width / 2 - 46, this.height / 6 + 41 * 3 - 4, 10526880);

        this.waypointName.drawTextBox();
        this.waypointX.drawTextBox();
        this.waypointZ.drawTextBox();
        this.waypointY.drawTextBox();
   		GL11.glColor4f(waypoint.red, waypoint.green, waypoint.blue, 1.0F);
        this.minimap.game.renderEngine.bindTexture(this.minimap.game.renderEngine.getTexture("/mamiyaotaru/color.png"));
        drawTexturedModalRect(this.width / 2 + 29, this.height / 6 + 41 * 3 - 5, 0, 0, 16, 10);
        super.drawScreen(par1, par2, par3);
        if (choosingColor) {
        	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        	this.minimap.game.renderEngine.bindTexture(this.minimap.game.renderEngine.getTexture("/mamiyaotaru/colorPicker.png"));
        	drawTexturedModalRect(this.width / 2 -128, this.height / 2 - 128, 0, 0, 256, 256);
        }
    }
    
}
