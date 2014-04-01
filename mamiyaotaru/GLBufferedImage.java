package net.minecraft.src.mamiyaotaru;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GLAllocation;
import net.minecraft.src.RenderEngine;

public class GLBufferedImage extends BufferedImage{

	private ByteBuffer buffer;
	public byte[] bytes;
	public int index = 0;
	private Object lock = new Object();
	
	public GLBufferedImage(int width, int height, int imageType) {
		super(width, height, imageType);
		bytes = ((DataBufferByte) (this.getRaster().getDataBuffer())).getData();
		buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
	}

	public void write() {
		if (index != 0) 
			GL11.glDeleteTextures(index);

		index = GL11.glGenTextures();
		buffer.clear();
		synchronized (lock) {
			buffer.put(this.bytes);
			this.buffer.position(0).limit(bytes.length);
		}
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, index);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, this.getWidth(), this.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		//buffer.flip();
	}

	public void setRGB(int x, int y, int color24) {
		int index = (x + y * this.getWidth()) * 4;
		//synchronized (lock) {
			this.bytes[index] = (byte)(color24 >> 16);
			this.bytes[index+1] = (byte)(color24 >> 8);
			this.bytes[index+2] = (byte)(color24 >> 0);
			this.bytes[index+3] = (byte)(color24 >> 24);
		//}
	}

	public void moveX(int offset) {
		synchronized (lock) {
			if (offset > 0)
				System.arraycopy(this.bytes, offset*4, this.bytes, 0, this.bytes.length-offset*4);
			else if (offset < 0) 
				System.arraycopy(this.bytes, 0, this.bytes, -offset*4, this.bytes.length+offset*4);
		}
	}

	public void moveY(int offset) {
		synchronized (lock) {
			if (offset > 0)
				System.arraycopy(this.bytes, offset*this.getWidth()*4, this.bytes, 0, this.bytes.length-offset*this.getWidth()*4);
			else if (offset < 0)
				System.arraycopy(this.bytes, 0, this.bytes, -offset*this.getWidth()*4, this.bytes.length+offset*this.getWidth()*4);
		}
	}
	
/*	public void setBuffer(ByteBuffer newBuffer) {
		int[] bOffs = { 0, 1, 2, 3 };
		ColorModel colorModel =
		        new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
		            new int[] { 8, 8, 8, 8 },
		            true,
		            false,
		            Transparency.TRANSLUCENT,
		            DataBuffer.TYPE_BYTE);
		this.buffer = newBuffer;
		System.out.println("bound: " + Minecraft.getMinecraft().renderEngine.getTexture("/terrain.png"));

		//this.buffer = Minecraft.getMinecraft().renderEngine.field_94154_l.func_94246_d().func_94273_h();
		//this.buffer = Minecraft.getMinecraft().renderEngine.field_94154_l.field_94250_g.field_94228_a.func_94273_h();
		//Minecraft.getMinecraft().renderEngine.field_94154_l.func_94246_d().func_94279_c("quartz.png");

        byte[] bytes = new byte[this.getWidth() * this.getHeight() * 4];
        buffer.position(0);
        buffer.get(bytes);
		

		int length = bytes.length;
		DataBufferByte dbb = new DataBufferByte(bytes, length);
        WritableRaster raster = Raster.createInterleavedRaster(dbb,
                    this.getWidth(),
                    this.getHeight(),
                    this.getWidth() * 4,
                    4,
                    bOffs,
                    null);
           BufferedImage bfImage = new BufferedImage(colorModel, raster, false, null);
			try {
				String path = "minecraft/mods/zan/";
				File outFile = new File(Minecraft.getAppDir(path), "quartz" + ".png");
				outFile.createNewFile();
				ImageIO.write(bfImage, "png", outFile);
			}
			catch (Exception e) {
				System.out.println("failed writing quartz " + e.getLocalizedMessage());
			}
	}*/
	
	//does the same as
	/*
    public void func_94279_c(String par1Str)
    {
        BufferedImage var2 = new BufferedImage(this.width, this.height, BufferedImage.TYPE_4BYTE_ABGR);
        ByteBuffer var3 = this.func_94273_h();
        byte[] var4 = new byte[this.width * this.height * 4];
        var3.position(0);
        var3.get(var4);

        for (int var5 = 0; var5 < this.width; ++var5)
        {
            for (int var6 = 0; var6 < this.height; ++var6)
            {
                int var7 = var6 * this.width * 4 + var5 * 4;
                byte var8 = 0;
                int var10 = var8 | (var4[var7 + 2] & 255) << 0;
                var10 |= (var4[var7 + 1] & 255) << 8;
                var10 |= (var4[var7 + 0] & 255) << 16;
                var10 |= (var4[var7 + 3] & 255) << 24;
                var2.setRGB(var5, var6, var10);
            }
        }

        try
        {
            ImageIO.write(var2, "png", new File(Minecraft.getMinecraftDir(), par1Str));
        }
        catch (IOException var9)
        {
            var9.printStackTrace();
        }
    }*/

}
