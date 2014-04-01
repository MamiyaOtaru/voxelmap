package net.minecraft.src;

import java.util.Iterator;
import java.util.List;

import net.minecraft.src.EntityWeatherEffect;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;

public class EntityWaypoint extends EntityWeatherEffect
{
	private Waypoint waypoint;
	public boolean isActive = false;
	private boolean inNether = false;
//	private Chunk chunk = null;

    public EntityWaypoint(World par1World, Waypoint waypoint, boolean inNether)
    {
        super(par1World);
        this.waypoint = waypoint;
        this.inNether = inNether;
        if (inNether) {
            this.posX=waypoint.x/8;
            this.posZ=waypoint.z/8;
        }
        else {
        	this.posX=waypoint.x;
        	this.posZ=waypoint.z;
        }
        this.posY=0;
        this.lastTickPosX=posX;
        this.lastTickPosZ=posZ;
        this.lastTickPosY=0;
   //     this.chunk = this.worldObj.getChunkFromBlockCoords(((int)(this.posX)), ((int)(this.posY)));
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        //super.onUpdate();
        this.isActive = waypoint.enabled;
        this.isDead = waypoint.isDead;
        //this.posY = this.worldObj.getHeightValue(((int)(this.posX)), ((int)(this.posY)));
        //this.lastTickPosY=posY;
        //this.setPosition(this.posX, this.posY, this.posZ);
    }

    protected void entityInit() {}
    
    public Waypoint getWaypoint() {
    	return this.waypoint;
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {}

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {}

    /**
     * Checks using a Vec3d to determine if this entity is within range of that vector to be rendered. Args: vec3D
     */
    public boolean isInRangeToRenderVec3D(Vec3 par1Vec3) {
    	//return true;
    	//return super.isInRangeToRenderVec3D(par1Vec3);
        /*double var2 = this.posX - par1Vec3.xCoord;
        double var4 = this.posY - par1Vec3.yCoord;
        double var6 = this.posZ - par1Vec3.zCoord;
        double var8 = var2 * var2 + var4 * var4 + var6 * var6;
        return var8 < 64 * 64; */
        return this.worldObj.getChunkFromBlockCoords(((int)(this.posX)), ((int)(this.posZ))).isChunkLoaded;
    }
}
