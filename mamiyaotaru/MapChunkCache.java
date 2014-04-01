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

public class MapChunkCache {

	private int width;
	private int height;
	
	private Chunk lastPlayerChunk = null;
	
	private MapChunk[] mapChunks;
	
	private boolean loaded = false;
	
	public MapChunkCache(int width, int height) {
		this.width = width;
		this.height = height;
		mapChunks = new MapChunk[width*height];
	}
	
	public void setChunk(int x, int z, MapChunk chunk) {
		mapChunks[x+z*width] = chunk;
	}
	
	public void checkIfChunksChanged(int playerX, int playerZ) {
		Chunk currentChunk = ZanMinimap.instance.getWorld().getChunkFromBlockCoords(playerX, playerZ);
		if (currentChunk != this.lastPlayerChunk){
			if (lastPlayerChunk == null) {
				fillAllChunks(playerX, playerZ);
				this.lastPlayerChunk = currentChunk;
				return;
			}
			int middleX = width / 2;
			int middleZ = height / 2;
			int movedX = currentChunk.xPosition - lastPlayerChunk.xPosition;
			int movedZ = currentChunk.zPosition - lastPlayerChunk.zPosition;
			if (Math.abs(movedX) < width && Math.abs(movedZ) < height) {
				this.moveX(movedX);
				this.moveZ(movedZ);
				
				for (int z = ((movedZ>0)?height - movedZ:0); z < ((movedZ>0)?height:-movedZ); z++) {			
					for (int x = 0; x < width; x++) {
						mapChunks[x+z*width] = new MapChunk(currentChunk.xPosition - (middleX - x), currentChunk.zPosition - (middleZ - z));
					}
				}

				for (int z = 0; z < height; z++) {			
					for (int x = ((movedX>0)?width - movedX:0); x < ((movedX>0)?width:-movedX); x++) {
						mapChunks[x+z*width] = new MapChunk(currentChunk.xPosition - (middleX - x), currentChunk.zPosition - (middleZ - z));
					}
				}
			}
			else {
				fillAllChunks(playerX, playerZ);
			}
			this.lastPlayerChunk = currentChunk;
		}

		for (int t = 0; t < this.width*height; t++) {
			mapChunks[t].checkIfChunkChanged();
		}
	}
	
	private void fillAllChunks(int playerX, int playerZ) {
		Chunk currentChunk = ZanMinimap.instance.getWorld().getChunkFromBlockCoords(playerX, playerZ);
		int middleX = width / 2;
		int middleZ = height / 2;
		for (int z = 0; z < height; z++) {			
			for (int x = 0; x < width; x++) {
				mapChunks[x+z*width] = new MapChunk(currentChunk.xPosition - (middleX - x), currentChunk.zPosition - (middleZ - z));
			}
		}
		this.loaded = true;
	}
	
	public void moveX(int offset) {
		if (offset > 0)
			System.arraycopy(this.mapChunks, offset, this.mapChunks, 0, this.mapChunks.length-offset);
		else if (offset < 0) 
			System.arraycopy(this.mapChunks, 0, this.mapChunks, -offset, this.mapChunks.length+offset);
	}

	public void moveZ(int offset) {
		if (offset > 0)
			System.arraycopy(this.mapChunks, offset*this.width, this.mapChunks, 0, this.mapChunks.length-offset*this.width);
		else if (offset < 0)
			System.arraycopy(this.mapChunks, 0, this.mapChunks, -offset*this.width, this.mapChunks.length+offset*this.width);
	}
	
	public void drawChunks(boolean oldNorth) {
		if (!loaded) 
			return;
		for (int z = 0; z < this.height; z++) {
			if (oldNorth) // old north reverses X-1 to X+1
				for (int x = this.width-1; x >= 0; x--) {
					mapChunks[x+z*width].drawChunk();
				}
			else {
				for (int x = 0; x < this.width; x++) {
					mapChunks[x+z*width].drawChunk();
				}
			}
		}
	}

}
