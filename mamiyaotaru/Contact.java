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
	public int armorValue = -1;
	public int armorColor = -1;

	
	public Contact(Entity entity, double x, double z, int y, int type) {
		this.entity = entity;
		this.x = x;
		this.z = z;
		this.y =y;
		this.type = type;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setArmor(int armorValue) {
		this.armorValue = armorValue;
	}
	
	public void setArmorColor(int armorColor) {
		this.armorColor = armorColor;
	}
	
	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	public void updateLocation() {
		this.x = entity.posX;
		this.y = (int)entity.posY;
		this.z = entity.posZ;
	}

}
