package net.minecraft.src.mamiyaotaru;

import net.minecraft.src.Entity;

public class Contact {
	public double x;
	public double z;
	public int y;
	public float angle;
	public double distance;
	public float brightness;
	public int type;
	public String name = "_";
	public String skinURL = "";
	public int imageIndex = -1;
	public Entity entity = null;

	
	public Contact(double x, double z, int y, int type) {
		this.x = x;
		this.z = z;
		this.y =y;
		this.type = type;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setEntity(Entity entity) {
		this.entity = entity;
	}

}
