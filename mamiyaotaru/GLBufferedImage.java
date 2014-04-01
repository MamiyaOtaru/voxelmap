package net.minecraft.src.mamiyaotaru;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.src.GLAllocation;

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

}
