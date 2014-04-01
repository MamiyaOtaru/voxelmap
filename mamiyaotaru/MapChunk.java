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
import net.minecraft.src.Chunk;
import net.minecraft.src.GLAllocation;
import net.minecraft.src.RenderEngine;
import net.minecraft.src.ZanMinimap;

public class MapChunk {

	public int x = 0;
	public int z = 0;
	private int width;
	private int height;
	private Chunk chunk;
	
	public boolean hasChanged;
	private boolean isLoaded = false;
	
	//MapData myData;
	
	public MapChunk(int x, int z) {
		this.x = x;
		this.z = z;
		hasChanged = true;
		//myData = new MapData(16, 16);
		chunk = ZanMinimap.instance.getWorld().getChunkFromChunkCoords(x, z);
		isLoaded = this.chunk.isChunkLoaded;
	}
	
	public void drawChunk() {
		checkIfChunkChanged();
		if (this.hasChanged) {
			//System.out.println(this.x + " " + this.z);
			int playerX = ZanMinimap.instance.xCoord();
			int playerZ = ZanMinimap.instance.zCoord();
			ZanMinimap.instance.renderChunk(chunk.xPosition*16, chunk.zPosition*16, chunk.xPosition*16+15, chunk.zPosition*16+15);
			// draw this section of image with full everything to catch changes
		}
		this.hasChanged = false;
		chunk.isModified = false;
	}
	
	public void checkIfChunkChanged() {
		/*Chunk check = ZanMinimap.instance.getWorld().getChunkFromChunkCoords(x, z);
		if (!chunk.equals(check)) {
			chunk = check;
			this.isLoaded = chunk.isChunkLoaded;
			System.out.println("loading new chunk");
			this.hasChanged = this.isLoaded;
		}*/
		if (!this.isLoaded) {
			chunk = ZanMinimap.instance.getWorld().getChunkFromChunkCoords(x, z);
			if (chunk.isChunkLoaded) {
				this.isLoaded = true;
				this.hasChanged = true;
				//System.out.println("chunk loaded " + this.x + " " + this.z);
			}
		}
		//chunk = ZanMinimap.instance.getWorld().getChunkFromChunkCoords(x, z);
		else if (chunk.isModified) {
			//System.out.println("chunk modified " + this.x + " " + this.z);
			this.hasChanged = true;
		}
	}

}
