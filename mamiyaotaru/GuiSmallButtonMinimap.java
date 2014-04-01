package net.minecraft.src.mamiyaotaru;

import net.minecraft.src.GuiButton;

public class GuiSmallButtonMinimap extends GuiButton
{
    private final EnumOptionsMinimap enumOptions;

    public GuiSmallButtonMinimap(int par1, int par2, int par3, String par4Str)
    {
        this(par1, par2, par3, (EnumOptionsMinimap)null, par4Str);
    }

    public GuiSmallButtonMinimap(int par1, int par2, int par3, int par4, int par5, String par6Str)
    {
        super(par1, par2, par3, par4, par5, par6Str);
        this.enumOptions = null;
    }

    public GuiSmallButtonMinimap(int par1, int par2, int par3, EnumOptionsMinimap par4EnumOptions, String par5Str)
    {
        super(par1, par2, par3, 150, 20, par5Str);
        this.enumOptions = par4EnumOptions;
    }

    public EnumOptionsMinimap returnEnumOptions()
    {
        return this.enumOptions;
    }
}
