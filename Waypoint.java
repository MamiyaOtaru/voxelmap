package net.minecraft.src;
public class Waypoint {
    public String name;
    public int x;
    public int z;
    public boolean enabled;
    public float red = 0.0F;
    public float green = 1.0F;
    public float blue = 0.0F;

    public Waypoint(String name, int x, int z, boolean enabled) {
        this.name = name;
        this.x = x;
        this.z = z;
        this.enabled = enabled;
    }

    public Waypoint(String name, int x, int z, boolean enabled, float red, float green, float blue) {
        this.name = name;
        this.x = x;
        this.z = z;
        this.enabled = enabled;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
}
