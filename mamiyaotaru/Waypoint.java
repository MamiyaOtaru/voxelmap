package net.minecraft.src.mamiyaotaru;
public class Waypoint {
    public String name;
    public String imageSuffix = ""; 
    public int x;
    public int z;
    public int y;
    public boolean enabled;
    public boolean showInWorld = true; // same for all waypoints: if in world display is on
    public boolean isDead = false; // has been deleted
    public float red = 0.0F;
    public float green = 1.0F;
    public float blue = 0.0F;
  
    public Waypoint(String name, int x, int z, int y, boolean enabled, float red, float green, float blue, String suffix) {
        this.name = name;
        this.x = x;
        this.z = z;
        this.y = y;
        this.enabled = enabled;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.imageSuffix = suffix;
    }
    
    public int getUnified() {
    	return (255 << 24) + ((int)(this.red * 255) << 16) + ((int)(this.green * 255) << 8) + ((int)(this.blue * 255));
    }
        
    public void kill() {
    	this.setDisplayInWorld(false);
    	this.isDead = true;
    }
    
    public void setDisplayInWorld(boolean showInWorld) {
    	this.showInWorld = showInWorld;
    }

}
