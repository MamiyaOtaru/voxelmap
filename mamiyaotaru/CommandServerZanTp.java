package net.minecraft.src.mamiyaotaru;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Block;
import net.minecraft.src.BlockHalfSlab;
import net.minecraft.src.BlockStairs;
import net.minecraft.src.Chunk;
import net.minecraft.src.CommandBase;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.Material;
import net.minecraft.src.PlayerNotFoundException;
import net.minecraft.src.World;
import net.minecraft.src.WrongUsageException;
import net.minecraft.src.ZanMinimap;

public class CommandServerZanTp extends CommandBase {
	
	private ZanMinimap zans;
	
	public CommandServerZanTp (ZanMinimap zans) {
		super();
		this.zans = zans;
	}
	
	public String getCommandName() {
		return "ztp";
	}

	public String getCommandUsage(ICommandSender par1ICommandSender) {
		// return par1ICommandSender.translateString("commands.tp.usage", new Object[0]);
		return "/ztp [waypointName]" /* OR /ztp [target player] [waypointName]"*/;
	}

	public void processCommand(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {
		if (par2ArrayOfStr.length < 1) {
			//throw new WrongUsageException("commands.tp.usage", new Object[0]);c
			throw new WrongUsageException("/ztp [waypointName]" /* OR /ztp [target player] [waypointName]"*/, new Object[0]);
		} 
		else {
			MinecraftServer server = MinecraftServer.getServer();
			EntityPlayerMP player = null;

			// use this if we want to teleport other players
		//	if (par2ArrayOfStr.length > 1) { // check if first string is a playername 
		//		player = server.getConfigurationManager().getPlayerForUsername(par2ArrayOfStr[0]);
		//	}
			if (player == null) { // if not, we will act on whoever sent the command
				player = (EntityPlayerMP) getCommandSenderAsPlayer(par1ICommandSender);
			}
			if (player == null) {
				throw new PlayerNotFoundException();
			}
		
			String waypointName = par2ArrayOfStr[0];
			for (int t = 1; t < par2ArrayOfStr.length; t++) {
				waypointName += " ";
				waypointName += par2ArrayOfStr[t];
			}
			
			/*ZanMinimap zans = null;
			if (MinimapGuiInGame.class.isInstance(Minecraft.getMinecraft().ingameGUI)) { // modloader version, ingameGUI is ours
					zans = ((MinimapGuiInGame)(Minecraft.getMinecraft().ingameGUI)).minimap;
			}
			else {
				zans = (ZanMinimap)(this.getPrivateFieldByType(Minecraft.getMinecraft().ingameGUI, ZanMinimap.class));
			}
			
			if (zans == null) 
				return;*/
			
			
			
			
			/*		else {
			int y = starty;
			//if (world.getBlockMaterial(x, y, z) == Material.air) {  // anything not air.  too much
			//if (!world.isBlockOpaqueCube(x, y, z)) { // anything not see through (no lava, water).  too little
			if (Block.lightOpacity[world.getBlockId(x, y, z)] == 0) { // material that blocks (at least partially) light - solids, liquids, not flowers or fences.  just right!
				while (y > 0) {
					y--;
					if (Block.lightOpacity[world.getBlockId(x, y, z)] > 0) 
						return y + 1;
				}
			}
			else {
				while ((y <= starty+10) && (y < 127)) {
					y++;
					if (Block.lightOpacity[world.getBlockId(x, y, z)] == 0)
						return y;
				}
			}
			return -1;
			//				return this.zCoord() + 1; // if it's solid all the way down we'll just take the block at the player's level for drawing
		}*/
			
			ArrayList<Waypoint> waypoints = zans.wayPts;
			Waypoint waypoint = null;
			for(Waypoint wpt:waypoints) {
				if (wpt.name.equalsIgnoreCase(waypointName))
					waypoint = wpt;
			}
			
			boolean inNether = (player.dimension == -1); //we are in the nether
			
			if (waypoint != null && player.worldObj != null) {
				int bound = 30000000;
				int x = parseIntBounded(par1ICommandSender, "" + waypoint.x, -bound, bound);
				int z = parseIntBounded(par1ICommandSender, "" + waypoint.z, -bound, bound);
				int y = waypoint.y; //parseIntBounded(par1ICommandSender, "" + 128, 0, 256);
				if (inNether) {
					x=x/8;
					z=z/8;
					Chunk chunk = player.worldObj.getChunkFromBlockCoords(x, z); // just make sure chunk is loaded
					player.worldObj.getChunkProvider().loadChunk(x, z);
					int safeY = -1;
					for (int t = 0; t < 127; t++) {
						// ie if block is solid (no liquid) and two blocks above are air
						if ( (y + t < 127) && isBlockStandable(player.worldObj, x, y + t, z) && isBlockOpen(player.worldObj,x, y + t + 1, z) && isBlockOpen(player.worldObj,x, y + t + 2, z)) {
							safeY = y+t+1;
							t = 128; // break
						}
						if ( (y - t > 0) && isBlockStandable(player.worldObj, x, y - t, z) && isBlockOpen(player.worldObj,x, y - t + 1, z) && isBlockOpen(player.worldObj,x, y - t + 2, z)) {
							safeY = y-t+1;
							t = 128; // break
						}
					}
					if (safeY == -1)
						return; // don't do it, nothing found
					else 
						y = safeY;
				}
				else { // not in nether, get y based on world height at coords
					if (waypoint.y == -1) {
						y = player.worldObj.getHeightValue(x, z);
					}
					if (y == 0) { // only load chunk if it wasn't already
						Chunk chunk = player.worldObj.getChunkFromBlockCoords(x, z);
						player.worldObj.getChunkProvider().loadChunk(x, z);
						y = player.worldObj.getHeightValue(x, z);
					}
				}
				player.setPositionAndUpdate((double) ((float) x + 0.5F), (double) y, (double) ((float) z + 0.5F));
				//player.serverForThisPlayer.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
				
				//notifyAdmins(par1ICommandSender, "commands.tp.success", new Object[] { player.getEntityName() });
			}
		}
	}

	/**
	 * Adds the strings available in this command to the given list of tab
	 * completion options.
	 */
	public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {
		return par2ArrayOfStr.length != 1 && par2ArrayOfStr.length != 2 ? null
				: getListOfStringsMatchingLastWord(par2ArrayOfStr,
						MinecraftServer.getServer().getAllUsernames());
	}
	
    private boolean isBlockStandable(World worldObj, int par1, int par2, int par3)
    {
        if (worldObj.getBlockMaterial(par1, par2, par3) == Material.air) // can stand on fence
        	return (worldObj.getBlockId(par1, par2-1, par3) == Block.fence.blockID || worldObj.getBlockId(par1, par2-1, par3) == Block.netherFence.blockID);
        Block block = Block.blocksList[worldObj.getBlockId(par1, par2, par3)];
        return block == null ? false : (block.blockMaterial.isOpaque());
    }
    
    private boolean isBlockOpen(World worldObj, int par1, int par2, int par3)
    {
        if (worldObj.getBlockMaterial(par1, par2, par3) == Material.air) // can stand on fence
        	return !(worldObj.getBlockId(par1, par2-1, par3) == Block.fence.blockID || worldObj.getBlockId(par1, par2-1, par3) == Block.netherFence.blockID);
        Block block = Block.blocksList[worldObj.getBlockId(par1, par2, par3)];
        return block == null ? true : (Block.lightOpacity[block.blockID] == 0);
    }
	
	
// not command related, this is just to get a handle on zan's so we can get waypoints	
/*	public Object getPrivateFieldByType (Object o, Class classtype) {   
		return getPrivateFieldByType(o, classtype, 0);
	}
	
	public Object getPrivateFieldByType (Object o, Class classtype, int index) {   
		// Go and find the private field... 
		int counter = 0;
		final java.lang.reflect.Field fields[] = o.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; ++i) {
			if (classtype.equals(fields[i].getType())) {
				if (counter == index) {
					try {
						fields[i].setAccessible(true);
						return fields[i].get(o);
					} 
					catch (IllegalAccessException ex) {
					}
				}
				counter++;
			}
		}
		return null;
	}
	*/
	
}
