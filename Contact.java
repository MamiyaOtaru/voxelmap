package net.minecraft.src;

import net.minecraft.src.Entity;

public class Contact {
	public int x;
	public int z;
	public int y;
	public int type;
	public String name = "_";
	public String skinURL = "";
	public int imageIndex = -1;
	public Entity entity = null;

	
	public Contact(int x, int z, int y, int type) {
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
