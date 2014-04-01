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

public class MapData {

	private int width;
	private int height;
	private Object lock = new Object();
	
	private static int DATABITS = 15;
	
	private static int HEIGHTPOS = 0;
	private static int MATERIALPOS = 1;
	private static int METADATAPOS = 2;
	private static int TINTPOS = 3;
	private static int LIGHTPOS = 4;
	private static int OCEANFLOORHEIGHTPOS = 5;
	private static int OCEANFLOORMATERIALPOS = 6;
	private static int OCEANFLOORMETADATAPOS = 7;
	private static int OCEANFLOORTINTPOS = 8;
	private static int OCEANFLOORLIGHTPOS = 9;
	private static int TRANSPARENTHEIGHTPOS = 10;
	private static int TRANSPARENTMATERIALPOS = 11;
	private static int TRANSPARENTMETADATAPOS = 12;
	private static int TRANSPARENTTINTPOS = 13;
	private static int TRANSPARENTLIGHTPOS = 14;
	
	/*Array of blockHeights*/
	private int[] data;
		
	public MapData(int width, int height) {
		this.width = width;
		this.height = height;
		data = new int[width*height*DATABITS];
	}
	
	public int getHeight(int x, int z) {
		return getData(x, z, HEIGHTPOS);
	}
	
	public int getMaterial(int x, int z) {
		return getData(x, z, MATERIALPOS);
	}
	
	public int getMetadata(int x, int z) {
		return getData(x, z, METADATAPOS);
	}
	
	public int getBiomeTint(int x, int z) {
		return getData(x, z, TINTPOS);
	}
	
	public int getLight(int x, int z) {
		return getData(x, z, LIGHTPOS);
	}
	
	public int getOceanFloorHeight(int x, int z) {
		return getData(x, z, OCEANFLOORHEIGHTPOS);
	}
	
	public int getOceanFloorMaterial(int x, int z) {
		return getData(x, z, OCEANFLOORMATERIALPOS);
	}
	
	public int getOceanFloorMetadata(int x, int z) {
		return getData(x, z, OCEANFLOORMETADATAPOS);
	}
	
	public int getOceanFloorBiomeTint(int x, int z) {
		return getData(x, z, OCEANFLOORTINTPOS);
	}
	
	public int getOceanFloorLight(int x, int z) {
		return getData(x, z, OCEANFLOORLIGHTPOS);
	}
	
	public int getTransparentHeight(int x, int z) {
		return getData(x, z, TRANSPARENTHEIGHTPOS);
	}
	
	public int getTransparentMaterial(int x, int z) {
		return getData(x, z, TRANSPARENTMATERIALPOS);
	}
	
	public int getTransparentMetadata(int x, int z) {
		return getData(x, z, TRANSPARENTMETADATAPOS);
	}
	
	public int getTransparentBiomeTint(int x, int z) {
		return getData(x, z, TRANSPARENTTINTPOS);
	}
	
	public int getTransparentLight(int x, int z) {
		return getData(x, z, TRANSPARENTLIGHTPOS);
	}

	public int getData(int x, int z, int bit) {
		int index = (x + z * this.width) * DATABITS + bit;
		return this.data[index];
	}
	
	public void setHeight(int x, int z, int value) {
		setData(x, z, HEIGHTPOS, value);
	}
	
	public void setMaterial(int x, int z, int value) {
		setData(x, z, MATERIALPOS, value);
	}
	
	public void setMetadata(int x, int z, int value) {
		setData(x, z, METADATAPOS, value);
	}
	
	public void setBiomeTint(int x, int z, int value) {
		setData(x, z, TINTPOS, value);
	}
	
	public void setLight(int x, int z, int value) {
		setData(x, z, LIGHTPOS, value);
	}
	
	public void setOceanFloorHeight(int x, int z, int value) {
		setData(x, z, OCEANFLOORHEIGHTPOS, value);
	}
	
	public void setOceanFloorMaterial(int x, int z, int value) {
		setData(x, z, OCEANFLOORMATERIALPOS, value);
	}
	
	public void setOceanFloorMetadata(int x, int z, int value) {
		setData(x, z, OCEANFLOORMETADATAPOS, value);
	}
	
	public void setOceanFloorBiomeTint(int x, int z, int value) {
		setData(x, z, OCEANFLOORTINTPOS, value);
	}
	
	public void setOceanFloorLight(int x, int z, int value) {
		setData(x, z, OCEANFLOORLIGHTPOS, value);
	}
	
	public void setTransparentHeight(int x, int z, int value) {
		setData(x, z, TRANSPARENTHEIGHTPOS, value);
	}
	
	public void setTransparentMaterial(int x, int z, int value) {
		setData(x, z, TRANSPARENTMATERIALPOS, value);
	}
	
	public void setTransparentMetadata(int x, int z, int value) {
		setData(x, z, TRANSPARENTMETADATAPOS, value);
	}
	
	public void setTransparentBiomeTint(int x, int z, int value) {
		setData(x, z, TRANSPARENTTINTPOS, value);
	}
	
	public void setTransparentLight(int x, int z, int value) {
		setData(x, z, TRANSPARENTLIGHTPOS, value);
	}
	
	public void setData(int x, int z, int bit, int value) {
		int index = (x + z * this.width) * DATABITS + bit;
		this.data[index] = value;
	}

	public void moveX(int offset) {
		synchronized (lock) {
			if (offset > 0)
				System.arraycopy(this.data, offset*DATABITS, this.data, 0, this.data.length-offset*DATABITS);
			else if (offset < 0) 
				System.arraycopy(this.data, 0, this.data, -offset*DATABITS, this.data.length+offset*DATABITS);
		}
	}

	public void moveZ(int offset) {
		synchronized (lock) {
			if (offset > 0)
				System.arraycopy(this.data, offset*this.width*DATABITS, this.data, 0, this.data.length-offset*this.width*DATABITS);
			else if (offset < 0)
				System.arraycopy(this.data, 0, this.data, -offset*this.width*DATABITS, this.data.length+offset*this.width*DATABITS);
		}
	}

	

}
