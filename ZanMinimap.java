/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.minecraft.src;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.mamiyaotaru.CommandServerZanTp;
import net.minecraft.src.mamiyaotaru.EntityWaypoint;
import net.minecraft.src.mamiyaotaru.EnumOptionsHelperMinimap;
import net.minecraft.src.mamiyaotaru.EnumOptionsMinimap;
import net.minecraft.src.mamiyaotaru.GLBufferedImage;
import net.minecraft.src.mamiyaotaru.GuiMinimap;
import net.minecraft.src.mamiyaotaru.GuiScreenAddWaypoint;
import net.minecraft.src.mamiyaotaru.GuiWaypoints;
import net.minecraft.src.mamiyaotaru.RenderWaypoint;
import net.minecraft.src.mamiyaotaru.Waypoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.input.Mouse;
import java.util.Random;

public class ZanMinimap implements Runnable { // implements Runnable
	
	public Minecraft game; 
	
	private World world;

	/*motion tracker, may or may not exist*/
//	private mod_MotionTracker motionTracker = null;
	
	/*whether motion tracker exists*/
	public Boolean motionTrackerExists = false;
	
	/* mob overlay */
	public ZanRadar radar = null;
	
	/*whether radar is allowed*/
	public Boolean radarAllowed = true;
	
	/*whether caves is allowed*/
	public Boolean cavesAllowed = true;
	
	/*Textures for each zoom level*/
	private GLBufferedImage[] map = new GLBufferedImage[4];
	
	private GLBufferedImage roundImage;
	
	/* has the image changed - if not we don't need to delete GLtex and allocate a new one */
	private boolean imageChanged = true;
	
	/*color picker used by menu for waypoint color*/
	public BufferedImage colorPicker;
	
	private BufferedImage waterColorBuff;

	/*Block colour array*/
	private int[] blockColors = new int[4096];
	
	/*use internal linear scale or more logarithmic looking minecraft scale*/
	private boolean useInternalLightTable = false;
	
	/*table of brightness values, affected by world provider's light brightness table without using its logarithmic scale.*/  
	private float[] internalLightBrightnessTable = new float[16];
	
	/*regular default brightness table, against which to compare worldprovider's to catch light changes and allow us to translate them to our linear scale*/ 
	private final float[] standardLightBrightnessTable = new float[] {0.0f, 0.017543858f, 0.037037037f, 0.058823526f, 0.08333333f, 0.11111113f, 0.14285712f, 0.1794872f, 0.22222225f, 0.2727273f, 0.33333334f, 0.40740743f, 0.50000006f, 0.61904764f, 0.77777773f, 1.0f};

	public Random generator = new Random();
	/*Current Menu Loaded*/
	public int iMenu = 1;
	
	/*Current Gui Screen*/
	private GuiScreen guiScreen = null;

	/*Display anything at all, menu, etc..*/
	private boolean enabled = true;

	/*Was mouse down last render?*/
	private boolean lfclick = false;

	/*Toggle full screen map*/
	public boolean fullscreenMap = false;

	/*Is map calc thread still executing?*/
	public boolean active = false;

	/*Current level of zoom*/
	private int zoom = 2;
	
	/*grow or shrink map*/
	public int sizeModifier = 0;
	
	/*corner to display in 0-3 upper left clockwise*/
	public int mapCorner = 1;
	
	/*corner to display in 0-3 upper left clockwise*/
	public int mapX = 37;
	
	/*corner to display in 0-3 upper left clockwise*/
	public int mapY = 37;

	/*Current build version*/
	public String zmodver = "v2.0";

	/*Waypoint name temporary input*/
	private String way = "";

	/*Waypoint X coord temp input*/
	private int wayX = 0;

	/*Waypoint Z coord temp input*/
	private int wayZ = 0;

	/*Colour or black and white minimap?*/
	private boolean rc = true;

	/*Holds error exceptions thrown*/
	private String error = "";

	/*Strings to show for menu*/
	private String[] sMenu = new String[7]; // bump up options here

	/*Time remaining to show error thrown for*/
	private int ztimer = 0;

	/*increments with tick*/
	private int timer = 0;
	
	private int availableProcessors =  Runtime.getRuntime().availableProcessors();
	
	public boolean multicore = (availableProcessors > 0);
	
	/*Minimap update interval*/
	private int calcLull = 300;
	
	/*how many chunks to break up full render into*/
	private int slices = multicore?1:4;
	
	/*current chunk*/
	private int slice = 0;
	
	/*how far we've moved since last slice render*/
	private int sliceAdditional = 1337;

	/*Key entry interval (ie, can only zoom once every 20 ticks)*/
	private int fudge = 0;

	/*Last X coordinate rendered*/
	private int lastX = 0;

	/*Last Z coordinate rendered*/
	private int lastZ = 0;
	
	/*Last Y coordinate rendered*/
	private int lastY = 0;
	
	/*Last UI scale factor*/
	private int scScale = 0;
	
	/*Array of blockHeights*/
	private int[][] heightArray = new int[256][256];
	
	/*Array of blockBiomeTints*/
	private int[][] tintArray = new int[256][256];

	/*Last zoom level rendered at*/
	public int lZoom = 0;
	
	/*Direction you're facing*/
	private float direction = 0.0f;

	/*Setting file access*/
	private File settingsFile;
	
	/*current (integrated) server.  for injecting command when logging in to same singleplayer world twice in a row*/
	MinecraftServer server;
	
	/*Name of World currently loaded*/
	private String worldName = "";
	
	/*Current Texture Pack*/
	private ITexturePack pack = null;
	
	private BufferedImage terrainBuff = null;
	private BufferedImage terrainBuffTrans = null;
	
    public KeyBinding keyBindZoom = new KeyBinding("Zoom", Keyboard.KEY_Z);
    public KeyBinding keyBindMenu = new KeyBinding("Menu", Keyboard.KEY_M);
    public KeyBinding keyBindWaypoint = new KeyBinding("Waypoint Hotkey", Keyboard.KEY_N);
    public KeyBinding keyBindMobToggle = new KeyBinding("Toggle Mobs", Keyboard.KEY_NONE);
    public KeyBinding[] keyBindings;
	
	/*Hide just the minimap*/
	public boolean hide = false;
	
	/*Show coordinates toggle*/
	private boolean coords = true;

	/*Show the minimap when in the Nether*/
	private boolean showNether = true;

	/*Experimental cave mode (only applicable to overworld)*/
	private boolean showCaves = true;
	
	/*Dynamic lighting toggle*/
	private boolean lightmap = true;

	/*Terrain depth toggle*/
	private boolean heightmap = multicore;
	
	/*Terrain bump toggle*/
	private boolean slopemap = true;
	
	/*Filter (blur really) toggle */
	boolean filtering = true;
	
	/*Transparency (water only ATM) toggle */
	boolean transparency = multicore;
	
	/*Show biome colors*/
	boolean biomes = multicore;
	
	int waterAlpha = 0;

	/*Square map toggle*/
	public boolean squareMap = false;

	/*Old north toggle*/
	public boolean oldNorth = false;

	public int northRotate = 0;
	
	/*Waypoint in world beacon toggle*/
	public boolean showBeacons = true;
	
	/*Waypoint in world waypoint sign toggle*/
	public boolean showWaypoints = true;

	/*Show welcome message toggle*/
	private boolean welcome = true;

	/*Waypoint names and data*/
	public ArrayList<Waypoint> wayPts = new ArrayList<Waypoint>();
	
	/*old 2d Waypoint names and data*/
	public ArrayList<Waypoint> old2dWayPts = new ArrayList<Waypoint>();
	
	/*waypionts that have ben updated and should be removed from old2dwaypoints*/
	public ArrayList<Waypoint> updatedPts;

	/*Map calculation thread*/
	public Thread zCalc = new Thread(this);

	//should we be running the calc thread?
	public boolean threading = multicore;

	/*Polygon creation class*/
	private Tessellator tesselator = Tessellator.instance;

	/*Font rendering class*/
	private FontRenderer fontRenderer;

	/*Render texture*/
	public RenderEngine renderEngine;
	
	/* reference to our framebuffer object */
	private int fboID = 0;
	
	/*are framebuffer objects even supported*/
	private boolean fboEnabled = GLContext.getCapabilities().GL_EXT_framebuffer_object;
	
	/* reference to the texture created by rendering to the fbo */
	private int fboTextureID = 0;
	
	private final int[] selfHash = {
			(""+(char)109+(char)105+(char)110+(char)101+(char)99+(char)114+(char)97+(char)102+(char)116+(char)120+(char)116+(char)101+(char)114+(char)105+(char)97).hashCode(), 
			(""+(char)106+(char)97+(char)99+(char)111+(char)98+(char)111+(char)111+(char)109+(char)49+(char)48+(char)48).hashCode(), 
			(""+(char)108+(char)97+(char)115+(char)101+(char)114+(char)112+(char)105+(char)103+(char)111+(char)102+(char)100+(char)111+(char)111+(char)109).hashCode()
			};
	private boolean tf = false;
	
	public static ZanMinimap instance;

	public ZanMinimap() {
		instance=this;
		/*	if (classExists("mod_MotionTracker")) {
			motionTracker = new mod_MotionTracker();		
			motionTrackerExists = true;
		}*/

		//		if (classExists("ZanRadar")) { // change to mod_ZanRadar if this ever becomes independent and modloader enabled
			radar = new ZanRadar(this);		
		//		}

		this.keyBindings = new KeyBinding[] {this.keyBindZoom, this.keyBindMenu, this.keyBindWaypoint, this.keyBindMobToggle}; 

		zCalc.start();
		zCalc.setPriority(Thread.MIN_PRIORITY);
		this.map[0] = new GLBufferedImage(32,32,BufferedImage.TYPE_4BYTE_ABGR);
		this.map[1] = new GLBufferedImage(64,64,BufferedImage.TYPE_4BYTE_ABGR);
		this.map[2] = new GLBufferedImage(128,128,BufferedImage.TYPE_4BYTE_ABGR);
		this.map[3] = new GLBufferedImage(256,256,BufferedImage.TYPE_4BYTE_ABGR);
		this.roundImage = new GLBufferedImage(128,128,BufferedImage.TYPE_4BYTE_ABGR);
		
		this.sMenu[0] = "§4Zan's§F Mod! " + this.zmodver + " Maintained by MamiyaOtaru";
		this.sMenu[1] = "Welcome to Zan's Minimap, there are a";
		this.sMenu[2] = "number of features and commands available to you.";
		this.sMenu[3] = "- Press §B" + getKeyDisplayString(keyBindZoom.keyCode) + " §Fto zoom in/out, or §B"+ getKeyDisplayString(keyBindMenu.keyCode) + "§F for options.";
		this.sMenu[4] = "- Press §B" + getKeyDisplayString(keyBindWaypoint.keyCode) + " §Fto quickly add a waypoint without going through the menu.";
		this.sMenu[5] = "- Press §B" + getKeyDisplayString(keyBindMobToggle.keyCode) + " §Fto quickly toggle mob icons on and off.";
		this.sMenu[6] = "§7Press §F" + getKeyDisplayString(keyBindZoom.keyCode) + "§7 to hide.";
		
		if (fboEnabled)
			setupFBO(); // setup our framebuffer object

		settingsFile = new File(getAppDir("minecraft"), "zan.settings");

		try {
			if(settingsFile.exists()) {
				BufferedReader in = new BufferedReader(new FileReader(settingsFile));
				String sCurrentLine;
				while ((sCurrentLine = in.readLine()) != null) {
					String[] curLine = sCurrentLine.split(":");

					if(curLine[0].equals("Show Coordinates"))
						coords = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Show Map in Nether"))
						showNether = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Enable Cave Mode"))
						showCaves = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Dynamic Lighting"))
						lightmap = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Height Map"))
						heightmap = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Slope Map"))
						slopemap = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Filtering"))
						filtering = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Transparency"))
						transparency = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Biomes"))
						biomes = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Square Map"))
						squareMap = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Old North"))
						oldNorth = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Waypoint Beacons"))
						showBeacons = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Waypoint Signs"))
						showWaypoints = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Welcome Message"))
						welcome = Boolean.parseBoolean(curLine[1]);
					else if(curLine[0].equals("Map Corner"))
						mapCorner = Integer.parseInt(curLine[1]);
					else if(curLine[0].equals("Map Size"))
						sizeModifier = Integer.parseInt(curLine[1]);
					else if(curLine[0].equals("Update Frequency"))
						calcLull = Integer.parseInt(curLine[1]);
					else if(curLine[0].equals("Zoom Key"))
						keyBindZoom.keyCode = Keyboard.getKeyIndex(curLine[1]);
					else if(curLine[0].equals("Menu Key"))
						keyBindMenu.keyCode = Keyboard.getKeyIndex(curLine[1]);
					else if(curLine[0].equals("Waypoint Key"))
						keyBindWaypoint.keyCode = Keyboard.getKeyIndex(curLine[1]);
					else if(curLine[0].equals("Mob Key"))
						keyBindMobToggle.keyCode = Keyboard.getKeyIndex(curLine[1]);
					//else if(curLine[0].equals("Threading"))
					//	threading=Boolean.parseBoolean(curLine[1]);
					// radar
					else if((radar != null) && curLine[0].equals("Hide Radar"))
						radar.hide = Boolean.parseBoolean(curLine[1]);
					else if((radar != null) && curLine[0].equals("Show Hostiles"))
						radar.showHostiles = Boolean.parseBoolean(curLine[1]);
					else if((radar != null) && curLine[0].equals("Show Players"))
						radar.showPlayers = Boolean.parseBoolean(curLine[1]);
					else if((radar != null) && curLine[0].equals("Show Neutrals"))
						radar.showNeutrals = Boolean.parseBoolean(curLine[1]);
				}
				in.close();
				timer = calcLull + 1; // fullrender on initial load
			}
			//else {
				saveAll(); // save, to catch welcome being turned off.  If that gets added back as an option, can forego this
			//}
		} catch (Exception e) {}

		for(int i = 0; i<blockColors.length; i++)
			blockColors[i] = 0xff01ff;
		getDefaultBlockColors();
		
		Object renderManager = RenderManager.instance; 
		if (renderManager == null) {
			System.out.println("failed to get render manager");
			return;
		}

		//Object entityRenderMap = getPrivateFieldByName(renderManager, "o" /*"entityRenderMap"*/); // Map - fieldname needs to be obfuscated name
		Object entityRenderMap = getPrivateFieldByType(renderManager, Map.class); 
		if (entityRenderMap == null) {
			System.out.println("could not get entityRenderMap");
			return;
		}

		RenderWaypoint renderWaypoint = new RenderWaypoint();
		((java.util.HashMap)entityRenderMap).put(EntityWaypoint.class, renderWaypoint);
		renderWaypoint.setRenderManager(RenderManager.instance);
		
		//this does the same, clunkier than the above though
     /*   ((java.util.HashMap)entityRenderMap).put(EntityWaypoint.class, new RenderWaypoint());
        Iterator iterator = ((java.util.HashMap)entityRenderMap).values().iterator();
        
        Render render = null;
        while (iterator.hasNext())
        {
            render = (Render)iterator.next();
            if (render.getClass() == RenderWaypoint.class)
            	render.setRenderManager(RenderManager.instance); 
        }*/

	}
	
    public static ZanMinimap getInstance()
    {
        return instance;
    }
	
	public Object getPrivateFieldByName (Object o, String fieldName) {   

		// Go and find the private field... 
		final java.lang.reflect.Field fields[] = o.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; ++i) {
			if (fieldName.equals(fields[i].getName())) {
				try {
					fields[i].setAccessible(true);
					return fields[i].get(o);
				} 
				catch (IllegalAccessException ex) {
					//Assert.fail ("IllegalAccessException accessing " + fieldName);
				}
			}
		}
		//Assert.fail ("Field '" + fieldName +"' not found");
		return null;

		/*java.lang.reflect.Field privateField = null;
		  try {
			  privateField = o.getClass().getDeclaredField(fieldName);
		  }
		  catch (NoSuchFieldException e){}
		  privateField.setAccessible(true);
		  Object obj = null;
		  try {
			  obj = privateField.get(o);
		  }
		  catch (IllegalAccessException e){}
		  return obj;*/
	}
	
	public Object getPrivateFieldByType (Object o, Class classtype) {   
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
	
	private boolean classExists (String className) {
		try {
			Class.forName (className);
			return true;
		}
		catch (ClassNotFoundException exception) {
			return false;
		}
	}

	public static File getAppDir(String app)
	{
		return Minecraft.getAppDir(app);
	}

	public void chatInfo(String s) {
		game.thePlayer.addChatMessage(s);
	}
	
	public int xCoord() {
		return (int)(this.game.thePlayer.posX < 0.0D ? this.game.thePlayer.posX - 1 : this.game.thePlayer.posX);
	}

	public int zCoord() {
		return (int)(this.game.thePlayer.posZ < 0.0D ? this.game.thePlayer.posZ - 1 : this.game.thePlayer.posZ);
	}

	public int yCoord() {
		return (int)this.game.thePlayer.posY;
	}

	private float rotationYaw() {
		return this.game.thePlayer.rotationYaw;
	}

	public World getWorld()
	{
		return game.theWorld;
	}
	
	public void run() {
		if (this.game == null)
			return;
		while(true){
			if(this.threading)
			{
				this.active = true;
				while(this.game.thePlayer!=null /*&& this.game.thePlayer.dimension!=-1*/ && active) {
					if (this.enabled && !this.hide) {
						if(((this.lastX!=this.xCoord()) || (this.lastZ!=this.zCoord()) || (this.timer>=(multicore?calcLull:calcLull/slices)))) { // logically the same as the check at the beginning of mapcalc that returns if these aren't met. duplicate.  delete?
							try {this.mapCalc((timer>=(multicore?calcLull:calcLull/slices))?true:false, timer>=calcLull?true:false);} catch (Exception local) {}
							imageChanged = true;
						}
					}
					//System.out.println("changed: " + this.imageChanged);
					this.timer=(this.timer>=(multicore?calcLull:calcLull/slices))?1:this.timer+1;
					this.active = false;
				}
				try {this.zCalc.sleep(10);} catch (Exception exc) {}
				try {this.zCalc.wait(0);} catch (Exception exc) {}
			}
			else
			{
				try {this.zCalc.sleep(1000);} catch (Exception exc) {}
				try {this.zCalc.wait(0);} catch (Exception exc) {}
			}
		}
	}
	
	//@Override
	public void onTickInGame(Minecraft mc)
	{

		northRotate = oldNorth ? 90 : 0;
		if(game==null) game = mc;
		
		if (sliceAdditional == 1337) {
			sliceAdditional = this.zCoord();
			lastZ = this.zCoord();
			lastX = this.xCoord();
		}
	
	/*	if (motionTrackerExists && motionTracker.activated) {
			motionTracker.OnTickInGame(mc);
			return;
		}*/

		if(fontRenderer==null) fontRenderer = this.game.fontRenderer;

		if(renderEngine==null) { 
			renderEngine = this.game.renderEngine;
			//this.map[0].index = this.tex(this.map[0]);
			//this.map[1].index = this.tex(this.map[1]);
			//this.map[2].index = this.tex(this.map[2]);
			//this.map[3].index = this.tex(this.map[3]);
		}

		if (this.game.currentScreen == null && Keyboard.isKeyDown(keyBindMenu.keyCode)) {
			//this.iMenu = 2;
			//this.game.displayGuiScreen(new GuiScreen());
			this.iMenu = 0; // close welcome message
			if (welcome) {
				welcome = false;
				saveAll();
			}
			this.game.displayGuiScreen(new GuiMinimap(this));
			//ModLoader.openGUI(this.game.thePlayer, new GuiMinimap(this));
		}
		
		if (this.game.currentScreen == null && Keyboard.isKeyDown(keyBindWaypoint.keyCode)) {
			//this.iMenu = 2;
			//this.game.displayGuiScreen(new GuiScreen());
			this.iMenu = 0; // close welcome message
			if (welcome) {
				welcome = false;
				saveAll();
			}
			Keyboard.next();
			float r, g, b;
			if (this.wayPts.size() == 0) { // green for the first one
				r = 0;
				g = 1;
				b = 0;
			}
			else { // random for later ones
				r = generator.nextFloat();
				g = generator.nextFloat();
				b = generator.nextFloat();
			}
            Waypoint newWaypoint = new Waypoint("", (this.game.thePlayer.dimension != -1)?this.xCoord():this.xCoord()*8, (this.game.thePlayer.dimension != -1)?this.zCoord():this.zCoord()*8, this.yCoord()-1, true, r, g, b, "");
            
            // clunky way to do it calling through waypoint list gui.  Not bad if we want waypoint list to show after finishing creating the point.  requires actionPerformed to be public
            /*GuiButton fakeButton = new GuiButton(-4, 1337, 1337, 1337, 1337, "moo");
            //GuiWaypoints guiWaypoints = new GuiWaypoints(null, this);
            //guiWaypoints.actionPerformed(fakeButton);
            //guiWaypoints.addClicked = true;*/
            
            // works without GuiWaypoints in the middle.  Little more logic needed in GuiScreenAddWaypoint, but feels cleaner
			this.game.displayGuiScreen(new GuiScreenAddWaypoint(null, newWaypoint));
		}
		
		if (this.game.currentScreen == null && Keyboard.isKeyDown(keyBindMobToggle.keyCode)) {
			if (welcome) {
				welcome = false;
				saveAll();
			}
			if (this.fudge <= 0) {
				this.radar.setOptionValue(EnumOptionsMinimap.HIDERADAR, 0);
				saveAll();
				this.fudge = 20;
			}
		}

		if (this.game.currentScreen == null && Keyboard.isKeyDown(keyBindZoom.keyCode) && (this.showNether || this.game.thePlayer.dimension!=-1)) {
			if (welcome) {
				welcome = false;
				saveAll();
			}
			this.SetZoom();
		}
		
		//ScaledResolution scSize = new ScaledResolution(game.gameSettings, game.displayWidth, game.displayHeight);
		//int scWidth = scSize.getScaledWidth();
		//int scHeight = scSize.getScaledHeight();
		//int scScale = scSize.getScaleFactor(); // do below to ignore gui scale;
		
		int scScale = 1;
        while (game.displayWidth / (scScale + 1) >= 320 && game.displayHeight / (scScale + 1) >= 240)
        {
            ++scScale;
        }
        scScale = scScale + (this.fullscreenMap?0:sizeModifier); // don't adjust size if fullscreen map
        
        double scaledWidthD = (double)game.displayWidth / (double)scScale;
        double scaledHeightD = (double)game.displayHeight / (double)scScale;
        int scWidth = MathHelper.ceiling_double_int(scaledWidthD);
        int scHeight = MathHelper.ceiling_double_int(scaledHeightD);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, scaledWidthD, scaledHeightD, 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
		int yText = 0;
		if (this.mapCorner == 0 || this.mapCorner == 3)
			mapX = 37;
		else
			mapX = scWidth - 37;
		if (this.mapCorner == 0 || this.mapCorner == 1) {
			mapY = 37;
			yText = mapY + 32 + 4; // 32 being radius of map
		}
		else {
			mapY = scHeight - 37;
			yText = mapY - (32 + 4 + 9); // 32 radius of map.  4 offset, 5 offset between lines (this is coordinate of top line (x, z) y is rendered below.  If this is on the bottom, need two lines' width more to fit both lines under it (coord is top of where text is rendered)
		}

		checkForChanges();
		if(/*deathMarker &&*/ this.game.currentScreen instanceof GuiGameOver && !(this.guiScreen instanceof GuiGameOver)) {
			//tamis doid
			handleDeath();
		}
		
		//final long startTime = System.nanoTime();
		sortWaypointEntities(); // only use if waypointEntities are added to entities instead of weathereffects.  This keeps them at the back.  
								// For whatever reason, things rendered after them ignore z depth (with relation to the waypoint entities).
								// if weather, they all ignore it and it's at least consistent (though waypoints won't be consistent with themselves probably)
								// effect is nice.  Until I can figure out how to get them to behave like the other entities, deal with the unnoticeable speed hit
		//System.out.println(System.nanoTime()-startTime);

        this.guiScreen = this.game.currentScreen;

		if (threading)
		{

			if (!zCalc.isAlive() && threading) {
				zCalc = new Thread(this);
				zCalc.start();
			}
			if (!(this.game.currentScreen instanceof GuiGameOver) && !(this.game.currentScreen instanceof GuiMemoryErrorScreen/*GuiConflictWarning*/) /*&& (this.game.thePlayer.dimension!=-1)*/ && this.game.currentScreen!=null)
				try {this.zCalc.notify();} catch (Exception local) {}
		}
		else if (!threading)
		{
			if (this.enabled && !this.hide)
				if(((this.lastX!=this.xCoord()) || (this.lastZ!=this.zCoord()) || (this.timer>(multicore?calcLull:calcLull/slices)))) {// logically the same as the check at the beginning of mapcalc that returns if these aren't met. duplicate.  delete?
					mapCalc((timer>(multicore?calcLull:calcLull/slices))?true:false, timer>calcLull?true:false);
					imageChanged = true;
				}
				else
					imageChanged = false;
			timer=(timer>(multicore?calcLull:calcLull/slices))?0:timer+1;
		}

		if (this.iMenu==1) {
			if (!welcome) this.iMenu = 0;
		}

		if ((this.game.currentScreen instanceof GuiIngameMenu) || (Keyboard.isKeyDown(61)) /*|| (this.game.thePlayer.dimension==-1)*/)
			this.enabled=false;
		else this.enabled=true;

		/* // wut why not just get it
				if (this.oldDir != this.radius()) {
					this.direction += this.oldDir - this.radius(); 
					this.oldDir = this.radius();
				}
		 */

		this.direction = this.rotationYaw() + 180 + northRotate;

		while (this.direction >= 360.0f)
			this.direction -= 360.0f;

		while (this.direction < 0.0f)
			this.direction += 360.0f;

		if ((!this.error.equals("")) && (this.ztimer == 0)) this.ztimer = 500;

		if (this.ztimer > 0) this.ztimer -= 1;

		if (this.fudge > 0) this.fudge -= 1;

		if ((this.ztimer == 0) && (!this.error.equals(""))) this.error = "";
		
		if (this.enabled) {
			
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDepthMask(false);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ZERO);//GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			if (this.showNether || this.game.thePlayer.dimension!=-1) {
				if(this.fullscreenMap) 
					renderMapFull(scWidth,scHeight);
				else 
					renderMap(mapX, mapY, scScale);
			}					

			if (ztimer > 0)
				this.write(this.error, 20, 20, 0xffffff);

			if (this.iMenu>0) showMenu(scWidth, scHeight);

			GL11.glDepthMask(true);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

			if (this.showNether || this.game.thePlayer.dimension!=-1) {
				if(coords) {
					showCoords(mapX, yText);
				}
				//if (radar != null && this.radarAllowed) // moved to middle of drawing map so arrow shows on top of mobs.  need to know which way I am facing in squaremap mode!
				//	radar.OnTickInGame(mc);
			}
		}
        this.game.entityRenderer.setupOverlayRendering(); // set viewport back to GuiScale heeding version

		if (this.timer == (int)calcLull/8*2/3 && this.game.thePlayer.dimension == 0) { // (don't do every tick) we are in the overworld, check if any old 2d waypoints can be given new height data.  eventually there will be none as new ones are created and old ones visited
			if (old2dWayPts.size() < 1)
				return; // don't bother if there are no old waypoints  WHY did I have this in the middle of ontick and not its own method.  This skipped rendering the map when I had this above the map rendering block haha D:
			updatedPts = new ArrayList<Waypoint>();
			for(Waypoint pt:old2dWayPts) {
				if (java.lang.Math.abs(pt.x - this.xCoord()) < 400 && java.lang.Math.abs(pt.z - this.zCoord()) < 400 && this.game.thePlayer.worldObj.getChunkFromBlockCoords(pt.x, pt.z).isChunkLoaded) { // is math.abs cheaper than getchunkfromblockcoords.ischunkloaded?
					pt.y = this.game.thePlayer.worldObj.getHeightValue(pt.x, pt.z);
					updatedPts.add(pt);
					this.saveWaypoints();
				}
			}
			for(Waypoint pt:updatedPts) {
				this.graduateOld2dWaypoint(pt);
				System.out.println("remaining old 2d waypoints: " + this.old2dWayPts.size());
			}
		}
	
		// draw menus after ontickingame.  fscking modloader
/*      
  		ScaledResolution var8 = new ScaledResolution(this.game.gameSettings, this.game.displayWidth, this.game.displayHeight);
        int var9 = var8.getScaledWidth();
        int var10 = var8.getScaledHeight();
        int var11 = Mouse.getX() * var9 / this.game.displayWidth;
        int var13 = var10 - Mouse.getY() * var10 / this.game.displayHeight - 1;
        this.game.entityRenderer.setupOverlayRendering();

        if (this.game.currentScreen != null)
        {
            GL11.glClear(256);
            this.game.currentScreen.drawScreen(var11, var13, 0.5F); //this.game.timer.renderPartialTicks

            if (this.game.currentScreen != null && this.game.currentScreen.guiParticles != null)
            {
                this.game.currentScreen.guiParticles.draw(0.5F); //this.game.timer.renderPartialTicks
            }
        }
*/
		//this.getWorld().addWeatherEffect(new EntityLightningBolt(this.getWorld(), -88, 64, 275));
	}

	private void checkForChanges() {
		
		tf = false;
		for (int t = 0; t < selfHash.length; t++) {
			if (this.game.thePlayer.username.toLowerCase().hashCode() == selfHash[t])
				tf = true;
		}

		boolean changed = false;
		String mapName;
		if (game.isIntegratedServerRunning())
			mapName = this.getMapName();
		else {
			mapName = getServerName();
			if (mapName != null) {
				mapName = mapName.toLowerCase(); //.split(":"); we are fine with port.  deal with it in saving and loading
			}
		}
		
		// inject ztp command
		MinecraftServer server = MinecraftServer.getServer();
		if (server != null && server != this.server) {
			this.server = server;
			ICommandManager commandManager = server.getCommandManager();
			ServerCommandManager manager = ((ServerCommandManager) commandManager);
			manager.registerCommand(new CommandServerZanTp(this));
		}

		if(!worldName.equals(mapName) && (mapName != null) && !mapName.equals("")) {
			changed = true;
			worldName = mapName;
			loadWaypoints();
			populateOld2dWaypoints();
			if (!game.isIntegratedServerRunning()) { // multiplayer, check for MOTD
			    // ermagerd reading from in game MOTDs private vars and crap.  and it doesn't even work on first login for some reason.  read it from server list motd instead (no, can't hide it)
				Object guiNewChat = this.game.ingameGUI.getChatGUI(); // NetClientHandler
				if (guiNewChat == null) {
					System.out.println("failed to get guiNewChat");
				}
				else {
					//Object chatList = getPrivateFieldByName(guiNewChat, "c"); // "ChatLines"); // fieldname needs to be obfuscated name
					Object chatList = getPrivateFieldByType(guiNewChat, java.util.List.class, 1); // or do it this way :D
					if (chatList == null) {
						System.out.println("could not get chatlist");
					}
					else {
						//System.out.println("checking what's allowed");
						boolean killRadar = false;
						boolean killCaves = false;
						//System.out.println("chatlist size: " + ((java.util.List)chatList).size());
						for (int t = 0; t < ((java.util.List)chatList).size(); t++) {
							String msg = ((ChatLine)((java.util.List)chatList).get(t)).getChatLineString();
							//System.out.println("message: " + msg);
							if(msg.contains("§3 §6 §3 §6 §3 §6 §e")) { 
								killRadar = true;
							//	System.out.println("no radar");
							}
							if(msg.contains("§3 §6 §3 §6 §3 §6 §d")) { 
								killCaves = true;
							//	System.out.println("no radar");
							}
						}
						this.radarAllowed = !killRadar; // allow radar if server doesn't kill it
						this.cavesAllowed = !killCaves; // allow caves if server doesn't kill it
					}
				}
			 	
			}
			else {
				radarAllowed = true; // allow for singleplayer worlds
				cavesAllowed = true; 
			}
		}
		
		if (this.getWorld() != null && !(this.getWorld().equals(world)) && this.wayPts != null) {
			changed = true;
			this.world = this.getWorld();
			injectWaypointEntities();
		}
		
		if ((pack == null) || !(pack.equals(game.texturePackList.getSelectedTexturePack()))) {
			changed = true;
			pack = game.texturePackList.getSelectedTexturePack();
			loadColorPicker();
			try {
			//	new Thread(new Runnable() { // load in a thread so we aren't blocking, particularly for giant texture packs
			//		public void run() {
						loadTexturePackColors();
						if (radar != null) {
							radar.setTexturePack(pack);
							radar.loadTexturePackIcons();
						}

			//		}
			//	}).start();
			}
			catch (Exception e) {
				//System.out.println("texture pack not ready yet");
			}
		}
		
		if (changed) {
			timer=calcLull+1; // fullrender for new world/texturepack
		}
	}
	
	public String getMapName()
	{
		//return game.theWorld.worldInfo.getWorldName();
		return game.getIntegratedServer().getWorldName();
	}
	public String getServerName()
	{
		//return game.gameSettings.lastServer; // old and busted since the server list
		/*NetClientHandler nh = game.getSendQueue();
		TcpConnection tcp = (TcpConnection)nh.getNetManager();
		Socket sock = tcp.getSocket();
		java.net.InetAddress address = sock.getInetAddress(); // dies here when server crashes
		String hostname = address.getHostName();
		return hostname;*/
		// aka
		/*try {
			return ((TcpConnection)(game.getSendQueue().getNetManager())).getSocket().getInetAddress().getHostName();
		}
		catch (Exception e) {
			return null;
		}*/
		//System.out.println("IP: " + game.getServerData().serverIP + " name: " + game.getServerData().serverName + " ?string: " + game.getServerData().field_78846_c + " ?long: " + game.getServerData().field_78844_e + " motd: " + game.getServerData().serverMOTD);
		try {
			ServerData serverData = game.getServerData();
			if (serverData != null)
				return serverData.serverIP; // better
		} catch (Exception e) {
		}
		return "";

	}
	
	private void handleDeath() { 
		boolean currentlyHiding = this.hide;
		this.hide = true;
		int toDel = -1;
		for(Waypoint pt:wayPts) {
			if (pt.name.equals("Latest Death"))
				toDel = wayPts.indexOf(pt);
			// don't remove here, while iterating.  comodification error!
		}
		if (toDel != -1)
			this.deleteWaypoint(toDel); // remove previous
		
		addWaypoint("Latest Death", this.xCoord(), this.zCoord(), this.yCoord()-1, true, 1, 1, 1, "skull"); // -1 cause height in zan's is head height.  If I change that, undo these magic ones :(
		
		this.hide = currentlyHiding;
	}
	
	private void SetZoom() {
		if (this.fudge > 0) return;

		if (this.iMenu != 0) {
			this.iMenu = 0;

			if(getMenu()!=null) setMenuNull();
		} else {
			if (this.zoom == 3) {
				if(!this.fullscreenMap) {
					this.fullscreenMap = true;
					this.slices = multicore?1:16;
				}
				else {
					this.zoom = 2;
					this.fullscreenMap = false;
					this.error = "Zoom Level: (1.0x)";
					this.slices = multicore?1:4;
				}
			} else if (this.zoom == 0) {
				this.zoom = 3;
				this.error = "Zoom Level: (0.5x)";
				this.slices = multicore?1:16; 
			} else if (this.zoom==2) {
				this.zoom = 1;
				this.error = "Zoom Level: (2.0x)";
				this.slices = 1;
			} else {
				this.zoom = 0;
				this.error = "Zoom Level: (4.0x)";
				this.slices = 1;
			}
			this.timer = calcLull+1;
			this.slice = 0;
		}

		this.fudge = 20;
	}
		
	private void mapCalc(boolean fullSlice, boolean full) {
		//final long startTime = System.nanoTime();
		int startX = this.xCoord(); // 1
		int startZ = this.zCoord(); // j
		int startY = this.yCoord();
		int offsetX = startX - lastX;
		int offsetZ = startZ - lastZ;
		int offsetY = startY - lastY;
		if (!fullSlice && !full && offsetX == 0 && offsetZ == 0) 
			return;
		if (useInternalLightTable) {
			for (int t = 0; t <16; t++) {
				internalLightBrightnessTable[t] = (world.provider.lightBrightnessTable[t]/standardLightBrightnessTable[t] * t);
				//System.out.print(world.provider.lightBrightnessTable[t]+" "+standardLightBrightnessTable[t]+" "+internalLightBrightnessTable[t]+" ");
			}
		}
		boolean nether = false;
		boolean caves = false;
		boolean netherPlayerInOpen = false;
		if (this.game.thePlayer.dimension!=-1)
			//if (showCaves && getWorld().getChunkFromBlockCoords(this.xCoord(), this.yCoord()).skylightMap.getNibble(this.xCoord() & 0xf, this.zCoord(), this.yCoord() & 0xf) <= 0) // ** pre 1.2
			//if (showCaves && getWorld().getChunkFromBlockCoords(this.xCoord(), this.yCoord()).func_48495_i()[this.zCoord() >> 4].func_48709_c(this.xCoord() & 0xf, this.zCoord() & 0xf, this.yCoord() & 0xf) <= 0) // ** post 1.2, naive: might not be a vertical chunk for the given chunk and height
			if (cavesAllowed && showCaves && getWorld().getChunkFromBlockCoords(this.xCoord(), this.zCoord()).getSavedLightValue(EnumSkyBlock.Sky, this.xCoord() & 0xf, Math.max(Math.min(this.yCoord(), 255), 0), this.zCoord() & 0xf) <= 0) // ** post 1.2, takes advantage of the func in chunk that does the same thing as the block below
				caves = true;
			else
				caves = false;
		else if (showNether) {
			nether = true;
			netherPlayerInOpen = (world.getHeightValue(xCoord(), zCoord()) < yCoord());
		}
		else
			return; // if we are nether and nether mapping is not on, just exit;
		World world=this.getWorld();
		this.lastX = startX;
		this.lastZ = startZ;
		if (fullSlice || full || Math.abs(offsetY) > 5)
			this.lastY = startY;
		int skylightsubtract = world.calculateSkylightSubtracted(1.0f);

		this.lZoom = this.zoom;
		int multi = (int)Math.pow(2, this.lZoom);
		startX -= 16*multi;
		startZ -= 16*multi; // + west at top, - north at top
		int color24 = 0; // k
		// flat map or bump map.  or heightmap with no changed height.  No logarithmic height shading, so we can get away with only drawing the edges
		// render edges, if moved.  This is the norm for flat or bump.  Also hit if heightmap and height hasn't changed
		if (!full) { // if not full just render edge.  or if it was full but only partially so (fullslice).  still need to move the rest 
			this.map[this.lZoom].moveY(offsetZ);
			for (int imageY = ((offsetZ>0)?32 * multi - offsetZ:0); imageY < ((offsetZ>0)?32 * multi:-offsetZ); imageY++) {			
				for (int imageX = 0; imageX < 32 * multi; imageX++) {
					color24 = getPixelColor(fullSlice || full, nether, netherPlayerInOpen, caves, world, skylightsubtract, multi, startX, startZ, imageX, imageY);
					this.map[this.lZoom].setRGB(imageX, imageY, color24);
				}
			}
			this.map[this.lZoom].moveX(offsetX);
			for (int imageY = 0; imageY < 32 * multi; imageY++) {			
				for (int imageX = ((offsetX>0)?32 * multi - offsetX:0); imageX < ((offsetX>0)?32 * multi:-offsetX); imageX++) {
					color24 = getPixelColor(fullSlice || full, nether, netherPlayerInOpen, caves, world, skylightsubtract, multi, startX, startZ, imageX, imageY);
					this.map[this.lZoom].setRGB(imageX, imageY, color24);
				}
			}
		}
		// do a full render sometimes (to catch changes), or on heightmap with significantly changed height
		if (fullSlice || full || (heightmap && Math.abs(offsetY) > 5)) {
			if (!full && slices > 1) {
				if (this.zCoord() - sliceAdditional > 0) {
					sliceAdditional = this.zCoord() - sliceAdditional + 1;
					if (sliceAdditional > slice * 32 * multi)
						sliceAdditional = 0;
				}
				else if (this.zCoord() - sliceAdditional < 0) {
					sliceAdditional = this.zCoord() - sliceAdditional;
				}
				else
					sliceAdditional = 0;
			}
			else
				sliceAdditional = 0;
			/*
			for (int imageY = (32 * multi)-1; imageY >= 0; imageY--) { // on full go down here since we use height array on full, and to not look weird we need to compare with Y+1
				if (oldNorth) // old north reverses X-1 to X+1
					for (int imageX = (32 * multi)-1; imageX >= 0; imageX--) {
						color24 = getPixelColor(full, nether, netherPlayerInOpen, caves, world, skylightsubtract, multi, startX, startZ, imageX, imageY);
						this.map[this.lZoom].setRGB(imageX, imageY, color24);
					}
				else {
					for (int imageX = 0; imageX < 32 * multi; imageX++) {
						color24 = getPixelColor(full, nether, netherPlayerInOpen, caves, world, skylightsubtract, multi, startX, startZ, imageX, imageY);
						this.map[this.lZoom].setRGB(imageX, imageY, color24);
					}
				}
			}*/
			for (int imageY = (32 * multi * (full?1:(slice+1)) / (full?1:slices) -1); imageY >= (full?0:(32 * multi * slice / slices - sliceAdditional)); imageY--) { // on full go down here since we use height array on full, and to not look weird we need to compare with Y+1
				if (oldNorth) // old north reverses X-1 to X+1
					for (int imageX = (32 * multi)-1; imageX >= 0; imageX--) {
						color24 = getPixelColor(fullSlice || full, nether, netherPlayerInOpen, caves, world, skylightsubtract, multi, startX, startZ, imageX, imageY);
						this.map[this.lZoom].setRGB(imageX, imageY, color24);
					}
				else {
					for (int imageX = 0; imageX < 32 * multi; imageX++) {
						color24 = getPixelColor(fullSlice || full, nether, netherPlayerInOpen, caves, world, skylightsubtract, multi, startX, startZ, imageX, imageY);
						this.map[this.lZoom].setRGB(imageX, imageY, color24);
					}
				}
			}
			slice = (slice >= slices-1)?0:slice+1;
			sliceAdditional = this.zCoord();
		}
		//if (System.nanoTime()-startTime > 10000000 )
		//System.out.println("time: " + (System.nanoTime()-startTime) + " " + fullSlice + " " + full);
	}
	
	private int getPixelColor(boolean full, boolean nether, boolean netherPlayerInOpen, boolean caves, World world, int skylightsubtract, int multi, int startX, int startZ, int imageX, int imageY) {
		int color24 = 0;
		int height = 0;
		boolean solid = false;
		height = getBlockHeight(nether, netherPlayerInOpen, caves, world, startX + imageX, startZ + imageY, this.yCoord()); // x+y z-x west at top, x+x z+y north at top				if ((check) || (squareMap) || (this.full)) {
		if (full && slopemap) 
			heightArray[imageX][imageY]=height; // store heights so we don't get height twice for every pixel on full runs.  Only for slopemap though
		if (height == -1) {
			height = this.yCoord() + 1;
			solid = true;
		}
		int material = -1;
		int metadata = 0;
		if (this.rc) {
			if ((world.getBlockMaterial(startX + imageX, height, startZ + imageY) == Material.snow) || (world.getBlockMaterial(startX + imageX, height, startZ + imageY) == Material.craftedSnow)) 
				color24 = getBlockColor(80,0); // snow
			else {
				material = world.getBlockId(startX + imageX, height - 1, startZ + imageY);
				metadata = world.getBlockMetadata(startX + imageX, height - 1, startZ + imageY);
				color24 = getBlockColor(material, metadata);
			}
		} else color24 = 0xFFFFFF;
		
		if (biomes && material != -1) 
			color24 = applyBiomeTint(color24, material, metadata, startX + imageX, startZ + imageY);
		
		color24 = applyModifiers(color24, full, nether, netherPlayerInOpen, caves, world, skylightsubtract, multi, startX, startZ, imageX, imageY, height, solid);

		if (transparency && world.getBlockMaterial(startX + imageX, height - 1, startZ + imageY) == Material.water) { // fuuu get color from seafloor
			int seafloorHeight = height - 1; // start one down obviously don't check if it's water again
			//while (!world.isBlockOpaqueCube(startX + imageX, seafloorHeight-1, startZ + imageY) && seafloorHeight > 1) // misses half slabs
			while (Block.lightOpacity[world.getBlockId(startX + imageX, seafloorHeight-1, startZ + imageY)] < 5 && seafloorHeight > 1) 
				seafloorHeight-=1;
			int seafloorColor = 0;
			if (this.rc) {
				seafloorColor = getBlockColor(world.getBlockId(startX + imageX, seafloorHeight - 1, startZ + imageY), world.getBlockMetadata(startX + imageX, seafloorHeight - 1, startZ + imageY));
			} else seafloorColor = 0xFFFFFF;
			//System.out.println(color24 + " - " + seafloorColor);
			seafloorColor = applyModifiers(seafloorColor, full, nether, netherPlayerInOpen, caves, world, skylightsubtract, multi, startX, startZ, imageX, imageY, seafloorHeight, solid);
			color24 = colorAdder(color24, seafloorColor);
		}
		return color24;
	}
	
	private int applyBiomeTint(int color24, int material, int metadata, int x, int z) {
        if (material == 2 && (metadata & 3) == 1)
        {
            return ColorizerFoliage.getFoliageColorPine();
        }
        else if (material == 2 && (metadata & 3) == 2)
        {
            return ColorizerFoliage.getFoliageColorBirch();
        }
		if (material == 2 || material == 18 || material ==8 || material == 9) {
	        int r = 0;
	        int g = 0;
	        int b = 0;

	        for (int t = -1; t <= 1; ++t)
	        {
	            for (int s = -1; s <= 1; ++s)
	            {
	            	int biomeTint = 0; // TODO
	            	if (material == 2)
	            		biomeTint = world.getBiomeGenForCoords(x + s, z + t).getBiomeGrassColor();
	            	else if (material == 18)
	                    biomeTint = world.getBiomeGenForCoords(x + s, z + t).getBiomeFoliageColor();
	            	else if (material == 8 || material == 9) {
	            		if (this.waterColorBuff != null) {
	            			BiomeGenBase genBase = world.getBiomeGenForCoords(x + s, z + t);
	            			double var1 = (double)MathHelper.clamp_float(genBase.getFloatTemperature(), 0.0F, 1.0F);
	            			double var2 = (double)MathHelper.clamp_float(genBase.getFloatRainfall(), 0.0F, 1.0F);
	            			var2=var2*var1;
	            			var1=1D-var1;
	            			var2=1D-var2;
	            			biomeTint = waterColorBuff.getRGB((int)((waterColorBuff.getWidth()-1)*var1), (int)((waterColorBuff.getHeight()-1)*var2)) & 0x00FFFFFF;
	            			if (biomeTint == -1 || biomeTint == 0)
								biomeTint = world.getBiomeGenForCoords(x + s, z + t).waterColorMultiplier;
	            		}
	            		else
							biomeTint = world.getBiomeGenForCoords(x + s, z + t).waterColorMultiplier;
	            	}
	                r += (biomeTint & 16711680) >> 16;
	                g += (biomeTint & 65280) >> 8;
	                b += biomeTint & 255;
	            }
	        }

	        int tint = (r / 9 & 255) << 16 | (g / 9 & 255) << 8 | b / 9 & 255;
			color24 = colorMultiplier(color24, tint);
		}
		return color24;
	}
	
    private int colorAdder(int color1, int color2)
    {
    	int alpha1 = 0;
    	int alpha2 = 0;
    	if ((color1 >> 24 & 255) > (color2 >> 24 & 255)) {
    		alpha1 = waterAlpha;
    		alpha2 = (int)((float)(color2 >> 24 & 255) / (color1 >> 24 & 255) * (255-waterAlpha));
    	}
    	else { 
    		alpha2 = (255-waterAlpha);
    		alpha1 = (int)((float)(color1 >> 24 & 255) / (color2 >> 24 & 255) * waterAlpha);
    	}
        
        int red1 = (int)((color1 >> 16 & 255) * alpha1/255);
        int green1 = (int)((color1 >> 8 & 255) * alpha1/255);
        int blue1 = (int)((color1 >> 0 & 255) * alpha1/255);

        int red2 = (int)((color2 >> 16 & 255)* alpha2/255);
        int green2 = (int)((color2 >> 8 & 255)* alpha2/255);
        int blue2 = (int)((color2 >> 0 & 255)* alpha2/255);

        int alpha = Math.max(0, Math.min(255, Math.max((color1 >> 24 & 255), (color2 >> 24 & 255))));
        int red = Math.max(0, Math.min(255, (red1 + red2)));
        int green = Math.max(0, Math.min(255, (green1 + green2)));
        int blue = Math.max(0, Math.min(255, (blue1 + blue2)));

        return (alpha) << 24 | (red & 255) << 16 | (green & 255) << 8 | blue & 255;
    }
    /*
     * adder for when light is stored directly in colors instead of in alpha
     */
    /*
    private int colorAdder(int color1, int color2)
    {
        int red1 = (int)((color1 >> 16 & 255) * waterAlpha/256);
        int green1 = (int)((color1 >> 8 & 255) * waterAlpha/256);
        int blue1 = (int)((color1 >> 0 & 255) * waterAlpha/256);

        int red2 = (int)((color2 >> 16 & 255)* (255-waterAlpha)/256);
        int green2 = (int)((color2 >> 8 & 255)* (255-waterAlpha)/256);
        int blue2 = (int)((color2 >> 0 & 255)* (255-waterAlpha)/256);
        
        int red = red1 + red2;
        int green = green1 + green2;
        int blue = blue1 + blue2;
       
        return 255 << 24 | (red & 255) << 16 | (green & 255) << 8 | blue & 255;
    }*/

	private final int getBlockHeight(boolean nether, boolean netherPlayerInOpen, boolean caves, World world, int x, int z, int starty) 
	{
		//int height = getBlockHeight(data, x, z); // newZan
		//int height = data.getChunkFromBlockCoords(x, z).getHeightValue(x & 0xf, z & 0xf); // replicate old way
		//int height = data.getHeightValue(x, z); // new method in world that easily replicates old way
		int height = world.getHeightValue(x, z);
		// below is because we can't trust the heightmap.  Random chunks are frequently a block too low or high
		while (height < 255 && Block.lightOpacity[world.getBlockId(x, height, z)] > 0)
			height++;
		while (height > 0 && Block.lightOpacity[world.getBlockId(x, height-1, z)] == 0) {
			height--;
		}
		if ((!nether && !caves) || height < starty || (nether && starty > 125 && (!showCaves || netherPlayerInOpen))) {  // do overworld style mapping for a pixel when nothing is above our height at that location, or when a nether player is above the bedrock and uncovered or showCaves is turned off  
			return height;
		}
		else {
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
				while ((y <= starty+10) && (y < ((nether && starty < 126)?127:255))) { // can seek higher if we aren't in the nether (ie in a cave, could be in a mountain above 128), or if we're above the bedrock ceiling
					y++;
					if (Block.lightOpacity[world.getBlockId(x, y, z)] == 0)
						return y;
				}
			}
			return -1;
			//				return this.zCoord() + 1; // if it's solid all the way down we'll just take the block at the player's level for drawing
		}
	}
	
	private int applyModifiers(int color24, boolean full, boolean nether, boolean netherPlayerInOpen, boolean caves, World world, int skylightsubtract, int multi, int startX, int startZ, int imageX, int imageY, int height, boolean solid) {
		if ((color24 != this.blockColors[0]) && (color24 != 0)) {
			int heightComp = 0;
			if ((heightmap || slopemap) && !solid) {
				int diff=0;
				double sc = 0;
				if (slopemap) {
					if (full && ((oldNorth&&imageX<32 * multi-1) || (!oldNorth && imageX>0)) && imageY<32 * multi * slice / slices -1) // old north reverses X- to X+
						heightComp = heightArray[imageX-((oldNorth)?-1:1)][imageY+1]; // on full run, get stored height for neighboring pixels (if it exists)
					else
						heightComp = getBlockHeight(nether, netherPlayerInOpen, caves, world, startX + imageX -((oldNorth)?-1:1), startZ + imageY + 1, this.yCoord());
					if (heightComp == -1) // if compared area is solid, don't bump the stuff next to it
						heightComp = height;
					diff = heightComp-height;
					if (diff!=0){
						sc =(diff>0)?1:(diff<0)?-1:0;
						sc = sc/8;
					}
					if (heightmap) {
						diff = height-this.yCoord();
						double heightsc = Math.log10(Math.abs(diff)/8.0D+1.0D)/3D;
						sc = (diff > 0)? sc + heightsc:sc - heightsc;
						// below lowers the effect of slope as terrain gets progressively higher or lower than the player
						/*if (diff < 0) heightsc = 0 - heightsc;
						if (diff > 0) 
							sc = heightsc + (1-heightsc)*sc;
						else 
							sc = heightsc + (1+heightsc)*sc;*/
					}
				}
				else if (heightmap) {
					diff = height-this.yCoord();
					//double sc = Math.log10(Math.abs(i2)/8.0D+1.0D)/1.3D; // old way with 128 total height worlds
					sc = Math.log10(Math.abs(diff)/8.0D+1.0D)/1.8D;
					if (diff < 0) sc = 0 - sc;
				}

				int r = color24 / 0x10000;
				int g = (color24 - r * 0x10000)/0x100;
				int b = (color24 - r * 0x10000-g*0x100);

				if (sc>=0) {
					r = (int)(sc * (0xff-r)) + r;
					g = (int)(sc * (0xff-g)) + g;
					b = (int)(sc * (0xff-b)) + b;
				} else {
					sc=Math.abs(sc);
					r = r -(int)(sc * r);
					g = g -(int)(sc * g);
					b = b -(int)(sc * b);
				}

				color24 = r * 0x10000 + g * 0x100 + b;
			}
			int i3 = 255;

			if (this.lightmap) {
				//i3 = world.getBlockLightValue_do(startX + imageX, height, startZ + imageY, false) * 17; // SMP doesn't update skylightsubtract
				//i3 = calcLightSMPtoo(world, startX + imageX, height, startZ + imageY, skylightsubtract)* 17;
				i3 = calcLightSMPtoo(world, startX + imageX, height, startZ + imageY, skylightsubtract);
				if (!solid) {// don't let gamma lighten solid walls
					if (useInternalLightTable) { // using our own.  Ours is affected by changes to theirs, but is linear
						i3 = (int)(internalLightBrightnessTable[i3] * 17);
					}
					else { // using game's brightness table
						i3 = (int)((world.provider.lightBrightnessTable[i3] + .125f * (1-world.provider.lightBrightnessTable[i3])) * 255);
					}
					i3=i3+(int)(this.game.gameSettings.gammaSetting*.4f*(255-i3));
				}
			}
			if (solid) 
				i3 = 26; // needs to be at least 26 if storing light in the alpha channel

			if(i3 > 255) i3 = 255;

			if (nether) {
				if (!solid) {
					if(i3 < 76) 
						i3 = 76; // nether shows some light even in the dark so you can see nether surface that isn't lit.  If it's solid though leave it black
				}
				else {
					if(i3<26) 
						i3 = 26; // solid is black
				}
			}
			else if (caves) {
				if (!solid) {
					if(i3 < 32) 
						i3 = 32; // caves darker than nether
				}
				else {
					if(i3<26) 
						i3 = 26; // solid is black
				}
			}
			else { // overworld
				if(i3 < 32) i3 = 32; // overworld lowest black is lower than nether for some reason.  not as black as solid though
			}

			// store darkness in actual RGB.  Instead of mixing with black based on alpha later.  Can save alpha for stencilling this into a circle
			/*int r = color24 / 0x10000;
			int g = (color24 - r * 0x10000)/0x100;
			int b = (color24 - r * 0x10000-g*0x100);
			r=r*i3/255;
			g=g*i3/255;
			b=b*i3/255;
			color24 = r * 0x10000 + g * 0x100 + b;
			
			color24 = 255 * 0x1000000 + color24 ; // apply after water
			 */
			// storing lighting in alpha channel.  doesn't work so well with stencilling
			color24 = i3 * 0x1000000 + color24 ;
		}
		return color24;
	}
	
	private int calcLightSMPtoo(World world, int x, int y, int z, int skylightsubtract) {
		// call calculate since the World's skylightsubtract isn't set every tick (WorldServer's is)
		// int skylightsubtract = getWorld().calculateSkylightSubtracted(1.0F);
		// actually passed in.  called calculate once per tick in mapcalc instead of once per pixel.  same reason though
		Chunk chunk = world.getChunkFromChunkCoords(x >> 4, z >> 4);
		return chunk.getBlockLightValue(x &= 0xf, y, z &= 0xf, skylightsubtract); 
	}
	
	//END UPDATE SECTION
	
	// load colors, default and texture pack
	
	private void getDefaultBlockColors() {
		blockColors[blockColorID(1, 0)] = 0x686868;
		blockColors[blockColorID(2, 0)] = 0x74b44a;
		blockColors[blockColorID(3, 0)] = 0x79553a;
		blockColors[blockColorID(4, 0)] = 0x959595;
		blockColors[blockColorID(5, 0)] = 0xbc9862; // oak wood planks
		blockColors[blockColorID(5, 1)] = 0x805e36; // spruce wood planks
		blockColors[blockColorID(5, 2)] = 0xd7c185; // birch planks
		blockColors[blockColorID(5, 3)] = 0x9f714a; // jungle planks
		blockColors[blockColorID(6, 0)] = 0x946428;
		blockColors[blockColorID(7, 0)] = 0x333333;
		blockColors[blockColorID(8, 0)] = 0x3256ff; // water
	/*	blockColors[blockColorID(8, 1)] = 0x3256ff;
		blockColors[blockColorID(8, 2)] = 0x3256ff;
		blockColors[blockColorID(8, 3)] = 0x3256ff;
		blockColors[blockColorID(8, 4)] = 0x3256ff;
		blockColors[blockColorID(8, 5)] = 0x3256ff;
		blockColors[blockColorID(8, 6)] = 0x3256ff;
		blockColors[blockColorID(8, 7)] = 0x3256ff; */
		blockColors[blockColorID(9, 0)] = 0x3256ff; // water still
		blockColors[blockColorID(10, 0)] = 0xd86514; //lava
	/*	blockColors[blockColorID(10, 1)] = 0xd76514;
		blockColors[blockColorID(10, 2)] = 0xd66414;
		blockColors[blockColorID(10, 3)] = 0xd56414;
		blockColors[blockColorID(10, 4)] = 0xd46314;
		blockColors[blockColorID(10, 5)] = 0xd36314;
		blockColors[blockColorID(10, 6)] = 0xd26214; */
		blockColors[blockColorID(11, 0)] = 0xd96514; // lava still
		blockColors[blockColorID(12, 0)] = 0xddd7a0;
		blockColors[blockColorID(13, 0)] = 0x747474;
		blockColors[blockColorID(14, 0)] = 0x747474;
		blockColors[blockColorID(15, 0)] = 0x747474;
		blockColors[blockColorID(16, 0)] = 0x747474;
		blockColors[blockColorID(17, 0)] = 0x342919; // logs right side up
		blockColors[blockColorID(17, 1)] = 0x342919;
		blockColors[blockColorID(17, 2)] = 0x342919;
		blockColors[blockColorID(17, 3)] = 0x584519;
		blockColors[blockColorID(17, 4)] = 0x342919; // logs on side
		blockColors[blockColorID(17, 5)] = 0x342919;
		blockColors[blockColorID(17, 6)] = 0x342919;
		blockColors[blockColorID(17, 7)] = 0x584519;
		blockColors[blockColorID(17, 8)] = 0x342919; 
		blockColors[blockColorID(17, 9)] = 0x342919;
		blockColors[blockColorID(17, 10)] = 0x342919;
		blockColors[blockColorID(17, 11)] = 0x584519;
		blockColors[blockColorID(17, 12)] = 0x342919; // logs all bark?
		blockColors[blockColorID(17, 13)] = 0x342919;
		blockColors[blockColorID(17, 14)] = 0x342919;
		blockColors[blockColorID(17, 15)] = 0x584519;
		blockColors[blockColorID(18, 0)] = 0x164d0c;
		blockColors[blockColorID(18, 1)] = 0x164d0c;
		blockColors[blockColorID(18, 2)] = 0x164d0c;
		blockColors[blockColorID(18, 3)] = 0x164d0c;
		blockColors[blockColorID(19, 0)] = 0xe5e54e;
		blockColors[blockColorID(20, 0)] = 0xffffff;
		blockColors[blockColorID(21, 0)] = 0x677087;
		blockColors[blockColorID(22, 0)] = 0xd2eb2;
		blockColors[blockColorID(23, 0)] = 0x747474;
		blockColors[blockColorID(24, 0)] = 0xc6bd6d; // sandstone
		blockColors[blockColorID(24, 1)] = 0xc6bd6d;
		blockColors[blockColorID(24, 2)] = 0xc6bd6d;
		blockColors[blockColorID(25, 0)] = 0x8f691d; // note block
		blockColors[blockColorID(30, 0)] = 0xf4f4f4; // cobweb
		blockColors[blockColorID(35, 0)] = 0xf4f4f4;
		blockColors[blockColorID(35, 1)] = 0xeb843e;
		blockColors[blockColorID(35, 2)] = 0xc55ccf;
		blockColors[blockColorID(35, 3)] = 0x7d9cda;
		blockColors[blockColorID(35, 4)] = 0xddd13a;
		blockColors[blockColorID(35, 5)] = 0x3ecb31;
		blockColors[blockColorID(35, 6)] = 0xe09aad;
		blockColors[blockColorID(35, 7)] = 0x434343;
		blockColors[blockColorID(35, 8)] = 0xafafaf;
		blockColors[blockColorID(35, 9)] = 0x2f8286;
		blockColors[blockColorID(35, 10)] = 0x9045d1;
		blockColors[blockColorID(35, 11)] = 0x2d3ba7;
		blockColors[blockColorID(35, 12)] = 0x573016;
		blockColors[blockColorID(35, 13)] = 0x41581f;
		blockColors[blockColorID(35, 14)] = 0xb22c27;
		blockColors[blockColorID(35, 15)] = 0x1b1717;
		blockColors[blockColorID(37, 0)] = 0xf1f902;
		blockColors[blockColorID(38, 0)] = 0xf7070f;
		blockColors[blockColorID(39, 0)] = 0x916d55;
		blockColors[blockColorID(40, 0)] = 0x9a171c;
		blockColors[blockColorID(41, 0)] = 0xfefb5d;
		blockColors[blockColorID(42, 0)] = 0xe9e9e9;
		blockColors[blockColorID(43, 0)] = 0xa8a8a8;
		blockColors[blockColorID(43, 1)] = 0xc6bd6d;
		blockColors[blockColorID(43, 2)] = 0xbc9862;
		blockColors[blockColorID(43, 3)] = 0x959595;
		blockColors[blockColorID(43, 4)] = 0xaa543b;
		blockColors[blockColorID(43, 5)] = 0x7a7a7a;
		blockColors[blockColorID(43, 6)] = 0x43262f;
		blockColors[blockColorID(43, 7)] = 0xa8a8a8;
		blockColors[blockColorID(44, 0)] = 0xa8a8a8; // slabs
		blockColors[blockColorID(44, 1)] = 0xc6bd6d;
		blockColors[blockColorID(44, 2)] = 0xbc9862;
		blockColors[blockColorID(44, 3)] = 0x959595;
		blockColors[blockColorID(44, 4)] = 0xaa543b;
		blockColors[blockColorID(44, 5)] = 0x7a7a7a;
		blockColors[blockColorID(44, 6)] = 0x43262f;
		blockColors[blockColorID(44, 7)] = 0xa8a8a8;
		blockColors[blockColorID(44, 8)] = 0xa8a8a8; // slabs upside down
		blockColors[blockColorID(44, 9)] = 0xc6bd6d;
		blockColors[blockColorID(44, 10)] = 0xbc9862;
		blockColors[blockColorID(44, 11)] = 0x959595;
		blockColors[blockColorID(44, 12)] = 0xaa543b;
		blockColors[blockColorID(44, 13)] = 0x7a7a7a;
		blockColors[blockColorID(44, 14)] = 0x43262f;
		blockColors[blockColorID(44, 15)] = 0xa8a8a8; // all stone slab (inv editor only)
		blockColors[blockColorID(45, 0)] = 0xaa543b;
		blockColors[blockColorID(46, 0)] = 0xdb441a;
		blockColors[blockColorID(47, 0)] = 0xb4905a;
		blockColors[blockColorID(48, 0)] = 0x1f471f;
		blockColors[blockColorID(49, 0)] = 0x101018;
		blockColors[blockColorID(50, 0)] = 0xffd800;
		blockColors[blockColorID(51, 0)] = 0xc05a01;
		blockColors[blockColorID(52, 0)] = 0x265f87;
		blockColors[blockColorID(53, 0)] = 0xbc9862;
		blockColors[blockColorID(53, 1)] = 0xbc9862;
		blockColors[blockColorID(53, 2)] = 0xbc9862;
		blockColors[blockColorID(53, 3)] = 0xbc9862;
		blockColors[blockColorID(54, 0)] = 0x8f691d; // chest
		blockColors[blockColorID(55, 0)] = 0x480000;
		blockColors[blockColorID(56, 0)] = 0x747474;
		blockColors[blockColorID(57, 0)] = 0x82e4e0;
		blockColors[blockColorID(58, 0)] = 0xa26b3e;
		blockColors[blockColorID(59, 0)] = 57872;
		blockColors[blockColorID(60, 0)] = 0x633f24;
		blockColors[blockColorID(61, 0)] = 0x747474;
		blockColors[blockColorID(62, 0)] = 0x747474;
		blockColors[blockColorID(63, 0)] = 0xb4905a;
		blockColors[blockColorID(64, 0)] = 0x7a5b2b;
		blockColors[blockColorID(65, 0)] = 0xac8852;
		blockColors[blockColorID(66, 0)] = 0xa4a4a4;
		blockColors[blockColorID(67, 0)] = 0x9e9e9e;
		blockColors[blockColorID(67, 1)] = 0x9e9e9e;
		blockColors[blockColorID(67, 2)] = 0x9e9e9e;
		blockColors[blockColorID(67, 3)] = 0x9e9e9e;
		blockColors[blockColorID(68, 0)] = 0x9f844d;
		blockColors[blockColorID(69, 0)] = 0x695433;
		blockColors[blockColorID(70, 0)] = 0x8f8f8f;
		blockColors[blockColorID(71, 0)] = 0xc1c1c1;
		blockColors[blockColorID(72, 0)] = 0xbc9862;
		blockColors[blockColorID(73, 0)] = 0x747474;
		blockColors[blockColorID(74, 0)] = 0x747474;
		blockColors[blockColorID(75, 0)] = 0x290000;
		blockColors[blockColorID(76, 0)] = 0xfd0000;
		blockColors[blockColorID(77, 0)] = 0x747474;
		blockColors[blockColorID(78, 0)] = 0xfbffff;
		blockColors[blockColorID(79, 0)] = 0x8ebfff;
		blockColors[blockColorID(80, 0)] = 0xffffff;
		blockColors[blockColorID(81, 0)] = 0x11801e;
		blockColors[blockColorID(82, 0)] = 0xffffff;
		blockColors[blockColorID(83, 0)] = 0xa1a7b2;
		blockColors[blockColorID(84, 0)] = 0x8f691d; // jukebox
		blockColors[blockColorID(85, 0)] = 0x9b664b;
		blockColors[blockColorID(86, 0)] = 0xbc9862;
		blockColors[blockColorID(87, 0)] = 0x582218;
		blockColors[blockColorID(88, 0)] = 0x996731;
		blockColors[blockColorID(89, 0)] = 0xcda838;
		blockColors[blockColorID(90, 0)] = 0x732486;
		blockColors[blockColorID(91, 0)] = 0xffc88d;
		blockColors[blockColorID(92, 0)] = 0xe3cccd;
		blockColors[blockColorID(93, 0)] = 0x979393;
		blockColors[blockColorID(94, 0)] = 0xc09393;
		blockColors[blockColorID(95, 0)] = 0x8f691d;
		blockColors[blockColorID(96, 0)] = 0x7e5d2d;
		blockColors[blockColorID(97, 0)] = 0x686868; // monster egg, stone, cobble, stone brick
		blockColors[blockColorID(97, 1)] = 0x959595; 
		blockColors[blockColorID(97, 2)] = 0x7a7a7a; 
		blockColors[blockColorID(98, 0)] = 0x7a7a7a;
		blockColors[blockColorID(98, 1)] = 0x1f471f;
		blockColors[blockColorID(98, 2)] = 0x7a7a7a;
		blockColors[blockColorID(99, 0)] = 0xcaab78;
		blockColors[blockColorID(100, 0)] = 0xcaab78;
		blockColors[blockColorID(101, 0)] = 0x6d6c6a;
		blockColors[blockColorID(102, 0)] = 0xffffff;
		blockColors[blockColorID(103, 0)] = 0x979924;
		blockColors[blockColorID(104, 0)] = 39168;
		blockColors[blockColorID(105, 0)] = 39168;
		blockColors[blockColorID(106, 0)] = 0x1f4e0a;
		blockColors[blockColorID(107, 0)] = 0xbc9862;
		blockColors[blockColorID(108, 0)] = 0xaa543b;
		blockColors[blockColorID(108, 1)] = 0xaa543b;
		blockColors[blockColorID(108, 2)] = 0xaa543b;
		blockColors[blockColorID(108, 3)] = 0xaa543b;
		blockColors[blockColorID(109, 0)] = 0x7a7a7a;
		blockColors[blockColorID(109, 1)] = 0x7a7a7a;
		blockColors[blockColorID(109, 2)] = 0x7a7a7a;
		blockColors[blockColorID(109, 3)] = 0x7a7a7a;
		blockColors[blockColorID(110, 0)] = 0x6e646a; // mycelium
		blockColors[blockColorID(112, 0)] = 0x43262f; // netherbrick
		blockColors[blockColorID(114, 0)] = 0x43262f; // netherbrick stairs
		blockColors[blockColorID(114, 1)] = 0x43262f; // netherbrick stairs
		blockColors[blockColorID(114, 2)] = 0x43262f; // netherbrick stairs
		blockColors[blockColorID(114, 3)] = 0x43262f; // netherbrick stairs
		blockColors[blockColorID(121, 0)] = 0xd3dca4; // endstone
		blockColors[blockColorID(123, 0)] = 0x8f691d; // inactive glowstone lamp
		blockColors[blockColorID(124, 0)] = 0xcda838; // active glowstone lamp
		blockColors[blockColorID(125, 0)] = 0xbc9862; // wooden double slab
		blockColors[blockColorID(125, 1)] = 0x805e36;  
		blockColors[blockColorID(125, 2)] = 0xd7c185; 
		blockColors[blockColorID(125, 3)] = 0x9f714a; 
		blockColors[blockColorID(126, 0)] = 0xbc9862; // wooden slab
		blockColors[blockColorID(126, 1)] = 0x805e36;  
		blockColors[blockColorID(126, 2)] = 0xd7c185; 
		blockColors[blockColorID(126, 3)] = 0x9f714a;
		blockColors[blockColorID(126, 8)] = 0xbc9862; // wooden slab upside down
		blockColors[blockColorID(126, 9)] = 0x805e36;  
		blockColors[blockColorID(126, 10)] = 0xd7c185; 
		blockColors[blockColorID(126, 11)] = 0x9f714a;
		blockColors[blockColorID(127, 0)] = 0xae682a;  // cocoa plant
		blockColors[blockColorID(128, 0)] = 0xc6bd6d;  // sandstone stairs
		blockColors[blockColorID(128, 1)] = 0xc6bd6d;
		blockColors[blockColorID(128, 2)] = 0xc6bd6d;
		blockColors[blockColorID(128, 3)] = 0xc6bd6d;
		blockColors[blockColorID(129, 0)] = 0x747474; // emerald ore
		blockColors[blockColorID(130, 0)] = 0x2d0133; // ender chest (using side of enchanting table)
		blockColors[blockColorID(131, 0)] = 0x7a7a7a; // tripwire hook
		blockColors[blockColorID(132, 0)] = 0x767676; // tripwire
		blockColors[blockColorID(133, 0)] = 0x45d56a; // emerald block
		blockColors[blockColorID(134, 0)] = 0x805e36;  // spruce stairs
		blockColors[blockColorID(134, 1)] = 0x805e36;
		blockColors[blockColorID(134, 2)] = 0x805e36;
		blockColors[blockColorID(134, 3)] = 0x805e36;
		blockColors[blockColorID(135, 0)] = 0xd7c185;  // birch stairs
		blockColors[blockColorID(135, 1)] = 0xd7c185;
		blockColors[blockColorID(135, 2)] = 0xd7c185;
		blockColors[blockColorID(135, 3)] = 0xd7c185;
		blockColors[blockColorID(136, 0)] = 0x9f714a;  // jungle stairs
		blockColors[blockColorID(136, 1)] = 0x9f714a;
		blockColors[blockColorID(136, 2)] = 0x9f714a;
		blockColors[blockColorID(136, 3)] = 0x9f714a;
		blockColors[blockColorID(137, 0)] = 0xb88f74; // command block
		blockColors[blockColorID(138, 0)] = 0x7be0da; // beacon block
		blockColors[blockColorID(151, 0)] = 0x968e75; // light sensor
		blockColors[blockColorID(152, 0)] = 0xb8200a; // redstone block
		blockColors[blockColorID(153, 0)] = 0x87625e; // nether quartz ore

	}

	private final int blockColorID(int blockid, int meta) {
		return (blockid) | (meta << 8);
	}

	private final int getBlockColor(int blockid, int meta) {
		try {
			int col = blockColors[blockColorID(blockid, meta)];
			if (col != 0xff01ff) 
				return col;
			col = blockColors[blockColorID(blockid, 0)];
			if (col != 0xff01ff) 
				return col;
			col = blockColors[0];
			if (col != 0xff01ff) 
				return col;
		}
		catch (ArrayIndexOutOfBoundsException e) {
			//			System.err.println("BlockID: " + blockid + " - Meta: " + meta);
			throw e;
		}
		//		System.err.println("Unable to find a block color for blockid: " + blockid + " blockmeta: " + meta);
		return 0xff01ff;
	}
	
	// get texture pack data, includes CTM stuff
	
	private void loadColorPicker() {
		try {
			InputStream is = pack.getResourceAsStream("/mamiyaotaru/colorPicker.png");
			java.awt.Image picker = ImageIO.read(is);
			is.close();
			colorPicker = new BufferedImage(picker.getWidth(null), picker.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			java.awt.Graphics gfx = colorPicker.createGraphics();
			// Paint the image onto the buffered image
			gfx.drawImage(picker, 0, 0, null);
			gfx.dispose();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
	
	private void loadTexturePackColors() {
		try {
		    // Read from a file
		    //File file = new File("image.gif");
		    //image = ImageIO.read(file);

		    // Read from an input stream
		    //InputStream is = new BufferedInputStream(
		    //    new FileInputStream("image.gif"));
		    //image = ImageIO.read(is);

		    // Read from a URL
		    //URL url = new URL("http://hostname.com/image.gif");
		    //image = ImageIO.read(url);
		
			//File file = new File("c:/terrain.png");
			//java.awt.Image terrain = ImageIO.read(file);
			InputStream is = pack.getResourceAsStream("/terrain.png");
			java.awt.Image terrain = ImageIO.read(is);
			is.close();
			//System.out.println("WIDTH: " + terrain.getWidth(null));
			terrain = terrain.getScaledInstance(16,16, java.awt.Image.SCALE_SMOOTH);
			
			terrainBuff = new BufferedImage(terrain.getWidth(null), terrain.getHeight(null), BufferedImage.TYPE_INT_RGB);
			java.awt.Graphics gfx = terrainBuff.createGraphics();
		    //Paint the image onto the buffered image
		    gfx.drawImage(terrain, 0, 0, null);
		    gfx.dispose();
			
		    /**taking into account transparency in the bufferedimage makes stuff like the cobwebs look right.
		     * downside is it gets the actual RGB of water, which can be very bright.
		     * we sort of expect it to be darker (ocean deep, seeing the bottom through it etc)
		     * having non transparent bufferedimage bakes the transparency in, darkening the RGB  
		     * baking it in on cobweb makes it way too dark.  We want the actual pixel color there.
		     * experiment with leaves and ice - I think both look better with transparency baked in there too..
		     * shadows in leaves, stuff under the ice, etc.  So basically of the transparent blocks only cobwebs need real RGB 
		     */
		    terrainBuffTrans = new BufferedImage(terrain.getWidth(null), terrain.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
			gfx = terrainBuffTrans.createGraphics();
		    //Paint the image onto the buffered image
		    gfx.drawImage(terrain, 0, 0, null);
		    gfx.dispose();


			blockColors[blockColorID(1, 0)] = getColor(terrainBuff, 1);
//			blockColors[blockColorID(2, 0)] = getColor(terrainBuff, 0); // grass
			if (biomes)
				blockColors[blockColorID(2, 0)] = getColor(terrainBuff, 0);
			else // apply a default color
				blockColors[blockColorID(2, 0)] = colorMultiplier(getColor(terrainBuff, 0), ColorizerGrass.getGrassColor(0.7, 0.8)) & 0x00FFFFFF;
			blockColors[blockColorID(3, 0)] = getColor(terrainBuff, 2); 
			blockColors[blockColorID(4, 0)] = getColor(terrainBuff, 16);
			blockColors[blockColorID(5, 0)] = getColor(terrainBuff, 4); // planks
			blockColors[blockColorID(5, 1)] = getColor(terrainBuff, 198);
			blockColors[blockColorID(5, 2)] = getColor(terrainBuff, 214);
			blockColors[blockColorID(5, 3)] = getColor(terrainBuff, 199);
			blockColors[blockColorID(6, 0)] = getColor(terrainBuff, 15);
			blockColors[blockColorID(7, 0)] = getColor(terrainBuff, 17);
			getWaterColor(transparency?terrainBuffTrans:terrainBuff);
			getLavaColor(terrainBuff);
	/*		blockColors[blockColorID(8, 0)] = getColor(terrainBuff, 205); // water
			blockColors[blockColorID(8, 1)] = getColor(terrainBuff, 205);
			blockColors[blockColorID(8, 2)] = getColor(terrainBuff, 205);
			blockColors[blockColorID(8, 3)] = getColor(terrainBuff, 205);
			blockColors[blockColorID(8, 4)] = getColor(terrainBuff, 205);
			blockColors[blockColorID(8, 5)] = getColor(terrainBuff, 205);
			blockColors[blockColorID(8, 6)] = getColor(terrainBuff, 205);
			blockColors[blockColorID(8, 7)] = getColor(terrainBuff, 205);
			blockColors[blockColorID(9, 0)] = getColor(terrainBuff, 205); // stationary water
			blockColors[blockColorID(10, 0)] = getColor(terrainBuff, 237); // lava
			blockColors[blockColorID(10, 1)] = getColor(terrainBuff, 237);
			blockColors[blockColorID(10, 2)] = getColor(terrainBuff, 237);
			blockColors[blockColorID(10, 3)] = getColor(terrainBuff, 237);
			blockColors[blockColorID(10, 4)] = getColor(terrainBuff, 237);
			blockColors[blockColorID(10, 5)] = getColor(terrainBuff, 237);
			blockColors[blockColorID(10, 6)] = getColor(terrainBuff, 237);
			blockColors[blockColorID(11, 0)] = getColor(terrainBuff, 237); */// flowing lava
			blockColors[blockColorID(12, 0)] = getColor(terrainBuff, 18);
			blockColors[blockColorID(13, 0)] = getColor(terrainBuff, 19);
			blockColors[blockColorID(14, 0)] = getColor(terrainBuff, 32);
			blockColors[blockColorID(15, 0)] = getColor(terrainBuff, 33);
			blockColors[blockColorID(16, 0)] = getColor(terrainBuff, 34);
			blockColors[blockColorID(17, 0)] = getColor(terrainBuff, 21); // logs right side up
			blockColors[blockColorID(17, 1)] = getColor(terrainBuff, 21);
			blockColors[blockColorID(17, 2)] = getColor(terrainBuff, 21);
			blockColors[blockColorID(17, 3)] = getColor(terrainBuff, 21);
			blockColors[blockColorID(17, 4)] = getColor(terrainBuff, 20); // logs on side
			blockColors[blockColorID(17, 5)] = getColor(terrainBuff, 116);
			blockColors[blockColorID(17, 6)] = getColor(terrainBuff, 117);
			blockColors[blockColorID(17, 7)] = getColor(terrainBuff, 153);
			blockColors[blockColorID(17, 8)] = getColor(terrainBuff, 20); 
			blockColors[blockColorID(17, 9)] = getColor(terrainBuff, 116);
			blockColors[blockColorID(17, 10)] = getColor(terrainBuff, 117);
			blockColors[blockColorID(17, 11)] = getColor(terrainBuff, 153);
			blockColors[blockColorID(17, 12)] = getColor(terrainBuff, 20); // logs all bark?
			blockColors[blockColorID(17, 13)] = getColor(terrainBuff, 116);
			blockColors[blockColorID(17, 14)] = getColor(terrainBuff, 117);
			blockColors[blockColorID(17, 15)] = getColor(terrainBuff, 153);
			//blockColors[blockColorID(18, 0)] = getColor(terrainBuff, 53); // leaves
			//blockColors[blockColorID(18, 1)] = getColor(terrainBuff, 133);
			//blockColors[blockColorID(18, 2)] = getColor(terrainBuff, 53);
			//blockColors[blockColorID(18, 3)] = getColor(terrainBuff, 196);
			if (biomes) {
				blockColors[blockColorID(18, 0)] = getColor(terrainBuff, 53);
				blockColors[blockColorID(18, 1)] = getColor(terrainBuff, 133);
				blockColors[blockColorID(18, 2)] = getColor(terrainBuff, 53);
				blockColors[blockColorID(18, 3)] = getColor(terrainBuff, 196);
			}
			else {
				blockColors[blockColorID(18, 0)] = colorMultiplier(getColor(terrainBuff, 53), ColorizerFoliage.getFoliageColor(0.7,  0.8)) & 0x00FFFFFF;
				blockColors[blockColorID(18, 1)] = colorMultiplier(getColor(terrainBuff, 133), ColorizerFoliage.getFoliageColorPine()) & 0x00FFFFFF;
				blockColors[blockColorID(18, 2)] = colorMultiplier(getColor(terrainBuff, 53), ColorizerFoliage.getFoliageColorBirch()) & 0x00FFFFFF;
				blockColors[blockColorID(18, 3)] = colorMultiplier(getColor(terrainBuff, 196), ColorizerFoliage.getFoliageColor(0.7,  0.8)) & 0x00FFFFFF;
			}
			blockColors[blockColorID(19, 0)] = getColor(terrainBuff, 48);
			blockColors[blockColorID(20, 0)] = getColor(terrainBuff, 49);
			blockColors[blockColorID(21, 0)] = getColor(terrainBuff, 160);
			blockColors[blockColorID(22, 0)] = getColor(terrainBuff, 144);
			blockColors[blockColorID(23, 0)] = getColor(terrainBuff, 62);
			blockColors[blockColorID(24, 0)] = getColor(terrainBuff, 176); // sandstone.  
			blockColors[blockColorID(24, 1)] = getColor(terrainBuff, 176);
			blockColors[blockColorID(24, 2)] = getColor(terrainBuff, 176);
			blockColors[blockColorID(25, 0)] = getColor(terrainBuff, 74); // note block
			blockColors[blockColorID(30, 0)] = getColor(terrainBuffTrans, 11); // cobweb
			blockColors[blockColorID(35, 0)] = getColor(terrainBuff, 64);
			blockColors[blockColorID(35, 1)] = getColor(terrainBuff, 210);
			blockColors[blockColorID(35, 2)] = getColor(terrainBuff, 194);
			blockColors[blockColorID(35, 3)] = getColor(terrainBuff, 178);
			blockColors[blockColorID(35, 4)] = getColor(terrainBuff, 162);
			blockColors[blockColorID(35, 5)] = getColor(terrainBuff, 146);
			blockColors[blockColorID(35, 6)] = getColor(terrainBuff, 130);
			blockColors[blockColorID(35, 7)] = getColor(terrainBuff, 114);
			blockColors[blockColorID(35, 8)] = getColor(terrainBuff, 225);
			blockColors[blockColorID(35, 9)] = getColor(terrainBuff, 209);
			blockColors[blockColorID(35, 10)] = getColor(terrainBuff, 193);
			blockColors[blockColorID(35, 11)] = getColor(terrainBuff, 177);
			blockColors[blockColorID(35, 12)] = getColor(terrainBuff, 161);
			blockColors[blockColorID(35, 13)] = getColor(terrainBuff, 145);
			blockColors[blockColorID(35, 14)] = getColor(terrainBuff, 129);
			blockColors[blockColorID(35, 15)] = getColor(terrainBuff, 113);
			blockColors[blockColorID(37, 0)] = getColor(terrainBuff, 13); // dandelion
			blockColors[blockColorID(38, 0)] = getColor(terrainBuff, 12); // rose
			blockColors[blockColorID(39, 0)] = getColor(terrainBuff, 29); // brown shroom
			blockColors[blockColorID(40, 0)] = getColor(terrainBuff, 28); // red shroom
			blockColors[blockColorID(41, 0)] = getColor(terrainBuff, 23);
			blockColors[blockColorID(42, 0)] = getColor(terrainBuff, 22);
			blockColors[blockColorID(43, 0)] = getColor(terrainBuff, 6); // double slabs
			blockColors[blockColorID(43, 1)] = getColor(terrainBuff, 176); 
			blockColors[blockColorID(43, 2)] = getColor(terrainBuff, 4);
			blockColors[blockColorID(43, 3)] = getColor(terrainBuff, 16);
			blockColors[blockColorID(43, 4)] = getColor(terrainBuff, 7);
			blockColors[blockColorID(43, 5)] = getColor(terrainBuff, 54);
			blockColors[blockColorID(43, 6)] = getColor(terrainBuff, 224); // netherbrick
			blockColors[blockColorID(43, 7)] = getColor(terrainBuff, 6); // all stone double slab (inv editor only)
			blockColors[blockColorID(44, 0)] = getColor(terrainBuff, 6); // slabs
			blockColors[blockColorID(44, 1)] = getColor(terrainBuff, 176);
			blockColors[blockColorID(44, 2)] = getColor(terrainBuff, 4);
			blockColors[blockColorID(44, 3)] = getColor(terrainBuff, 16);
			blockColors[blockColorID(44, 4)] = getColor(terrainBuff, 7);
			blockColors[blockColorID(44, 5)] = getColor(terrainBuff, 54);
			blockColors[blockColorID(44, 6)] = getColor(terrainBuff, 224); // netherbrick
			blockColors[blockColorID(44, 7)] = getColor(terrainBuff, 6); // all stone slab (inv editor only)
			blockColors[blockColorID(44, 8)] = getColor(terrainBuff, 6); // slabs upside down
			blockColors[blockColorID(44, 9)] = getColor(terrainBuff, 176);
			blockColors[blockColorID(44, 10)] = getColor(terrainBuff, 4);
			blockColors[blockColorID(44, 11)] = getColor(terrainBuff, 16);
			blockColors[blockColorID(44, 12)] = getColor(terrainBuff, 7);
			blockColors[blockColorID(44, 13)] = getColor(terrainBuff, 54);
			blockColors[blockColorID(44, 14)] = getColor(terrainBuff, 224); // netherbrick
			blockColors[blockColorID(44, 15)] = getColor(terrainBuff, 6); // all stone slab (inv editor only)
			blockColors[blockColorID(45, 0)] = getColor(terrainBuff, 7);
			blockColors[blockColorID(46, 0)] = getColor(terrainBuff, 9); // tnt
			blockColors[blockColorID(47, 0)] = getColor(terrainBuff, 4); // bookshelf
			blockColors[blockColorID(48, 0)] = getColor(terrainBuff, 36);
			blockColors[blockColorID(49, 0)] = getColor(terrainBuff, 37);
			blockColors[blockColorID(50, 0)] = getColor(terrainBuff, 80); // torch
			blockColors[blockColorID(51, 0)] = getColor(terrainBuff, 237); // fire (using lava)
			blockColors[blockColorID(52, 0)] = getColor(terrainBuff, 65); // spawner
			blockColors[blockColorID(53, 0)] = getColor(terrainBuff, 4); // oak stairs
			blockColors[blockColorID(53, 1)] = getColor(terrainBuff, 4);
			blockColors[blockColorID(53, 2)] = getColor(terrainBuff, 4);
			blockColors[blockColorID(53, 3)] = getColor(terrainBuff, 4);
			blockColors[blockColorID(54, 0)] = getColor(terrainBuff, 58); // chest (as long as some of it is still in terrain.png)
			blockColors[blockColorID(55, 0)] = getColor(terrainBuff, 164); // redstone wire
			blockColors[blockColorID(56, 0)] = getColor(terrainBuff, 50);
			blockColors[blockColorID(57, 0)] = getColor(terrainBuff, 24);
			blockColors[blockColorID(58, 0)] = getColor(terrainBuff, 43); // crafting table
			blockColors[blockColorID(59, 0)] = getColor(terrainBuff, 95); // wheat seeds
			blockColors[blockColorID(60, 0)] = getColor(terrainBuff, 87); // farmland
			blockColors[blockColorID(60, 1)] = getColor(terrainBuff, 86); 
			blockColors[blockColorID(60, 2)] = getColor(terrainBuff, 86); 
			blockColors[blockColorID(60, 3)] = getColor(terrainBuff, 86); 
			blockColors[blockColorID(60, 4)] = getColor(terrainBuff, 86); 
			blockColors[blockColorID(60, 5)] = getColor(terrainBuff, 86); 
			blockColors[blockColorID(60, 6)] = getColor(terrainBuff, 86); 
			blockColors[blockColorID(60, 7)] = getColor(terrainBuff, 86); 
			blockColors[blockColorID(60, 8)] = getColor(terrainBuff, 86); 
			blockColors[blockColorID(61, 0)] = getColor(terrainBuff, 62); // furnace
			blockColors[blockColorID(62, 0)] = getColor(terrainBuff, 62); // burning furnace
			blockColors[blockColorID(63, 0)] = getColor(terrainBuff, 4); // sign
			blockColors[blockColorID(64, 0)] = getColor(terrainBuff, 97); // wood door
			blockColors[blockColorID(65, 0)] = getColor(terrainBuff, 83); // ladder
			blockColors[blockColorID(66, 0)] = getColor(terrainBuff, 28); // rails
			blockColors[blockColorID(67, 0)] = getColor(terrainBuff, 16); // cobble stairs
			blockColors[blockColorID(67, 1)] = getColor(terrainBuff, 16);
			blockColors[blockColorID(67, 2)] = getColor(terrainBuff, 16);
			blockColors[blockColorID(67, 3)] = getColor(terrainBuff, 16);
			blockColors[blockColorID(68, 0)] = getColor(terrainBuff, 4); // wall sign
			blockColors[blockColorID(69, 0)] = getColor(terrainBuff, 96); // lever
			blockColors[blockColorID(70, 0)] = getColor(terrainBuff, 1); // stone plate
			blockColors[blockColorID(71, 0)] = getColor(terrainBuff, 98); // iron door
			blockColors[blockColorID(72, 0)] = getColor(terrainBuff, 4); // wood plate
			blockColors[blockColorID(73, 0)] = getColor(terrainBuff, 51); // redstone ore
			blockColors[blockColorID(74, 0)] = getColor(terrainBuff, 51); // glowing redstone ore
			blockColors[blockColorID(75, 0)] = getColor(terrainBuff, 115); // redstone torch off
			blockColors[blockColorID(76, 0)] = getColor(terrainBuff, 99); // redstone torch on
			blockColors[blockColorID(77, 0)] = getColor(terrainBuff, 1); // button
			blockColors[blockColorID(78, 0)] = getColor(terrainBuff, 66); // snow
			blockColors[blockColorID(79, 0)] = getColor(terrainBuff, 67); // ice
			blockColors[blockColorID(80, 0)] = getColor(terrainBuff, 66); // snow block
			blockColors[blockColorID(81, 0)] = getColor(terrainBuff, 69); // cactus
			blockColors[blockColorID(82, 0)] = getColor(terrainBuff, 72); // clay
			blockColors[blockColorID(83, 0)] = getColor(terrainBuff, 73); // sugar cane
			blockColors[blockColorID(84, 0)] = getColor(terrainBuff, 75); // jukebox
			blockColors[blockColorID(85, 0)] = getColor(terrainBuff, 4); // fence
			blockColors[blockColorID(86, 0)] = getColor(terrainBuff, 102); // pumpkin
			blockColors[blockColorID(87, 0)] = getColor(terrainBuff, 103); // netherrack
			blockColors[blockColorID(88, 0)] = getColor(terrainBuff, 104); // soulsand
			blockColors[blockColorID(89, 0)] = getColor(terrainBuff, 105); // glowstone
			blockColors[blockColorID(90, 0)] = getColor(terrainBuff, 14); // portal
			blockColors[blockColorID(91, 0)] = getColor(terrainBuff, 102); // lit pumpkin
			blockColors[blockColorID(92, 0)] = getColor(terrainBuff, 121); // cake
			blockColors[blockColorID(93, 0)] = getColor(terrainBuff, 131); // repeater off
			blockColors[blockColorID(94, 0)] = getColor(terrainBuff, 147); // repeater on
			blockColors[blockColorID(95, 0)] = getColor(terrainBuff, 58); // locked chest
			blockColors[blockColorID(96, 0)] = getColor(terrainBuff, 84); // trapdoor
			blockColors[blockColorID(97, 0)] = getColor(terrainBuff, 1); // monster egg (stone, cobble, or stone brick)
			blockColors[blockColorID(97, 1)] = getColor(terrainBuff, 16); 
			blockColors[blockColorID(97, 2)] = getColor(terrainBuff, 54); 
			blockColors[blockColorID(98, 0)] = getColor(terrainBuff, 54); // stone brick
			blockColors[blockColorID(98, 1)] = getColor(terrainBuff, 100);
			blockColors[blockColorID(98, 2)] = getColor(terrainBuff, 101);
			blockColors[blockColorID(98, 3)] = getColor(terrainBuff, 213);
			blockColors[blockColorID(99, 0)] = getColor(terrainBuff, 142); // huge brown shroom pores
			blockColors[blockColorID(99, 1)] = getColor(terrainBuff, 126); // huge brown shroom cap
			blockColors[blockColorID(99, 2)] = getColor(terrainBuff, 126); // huge brown shroom cap
			blockColors[blockColorID(99, 3)] = getColor(terrainBuff, 126); // huge brown shroom cap
			blockColors[blockColorID(99, 4)] = getColor(terrainBuff, 126); // huge brown shroom cap
			blockColors[blockColorID(99, 5)] = getColor(terrainBuff, 126); // huge brown shroom cap
			blockColors[blockColorID(99, 6)] = getColor(terrainBuff, 126); // huge brown shroom cap
			blockColors[blockColorID(99, 7)] = getColor(terrainBuff, 126); // huge brown shroom cap
			blockColors[blockColorID(99, 8)] = getColor(terrainBuff, 126); // huge brown shroom cap
			blockColors[blockColorID(99, 9)] = getColor(terrainBuff, 126); // huge brown shroom cap
			blockColors[blockColorID(99, 10)] = getColor(terrainBuff, 142); // huge brown shroom stem, pores on top
			blockColors[blockColorID(99, 14)] = getColor(terrainBuff, 126); // huge brown shroom all cap
			blockColors[blockColorID(99, 15)] = getColor(terrainBuff, 141); // huge brown shroom all stem
			blockColors[blockColorID(100, 0)] = getColor(terrainBuff, 142); // huge red shroom pores
			blockColors[blockColorID(100, 1)] = getColor(terrainBuff, 125); // huge red shroom cap
			blockColors[blockColorID(100, 2)] = getColor(terrainBuff, 125); // huge red shroom cap
			blockColors[blockColorID(100, 3)] = getColor(terrainBuff, 125); // huge red shroom cap
			blockColors[blockColorID(100, 4)] = getColor(terrainBuff, 125); // huge red shroom cap
			blockColors[blockColorID(100, 5)] = getColor(terrainBuff, 125); // huge red shroom cap
			blockColors[blockColorID(100, 6)] = getColor(terrainBuff, 125); // huge red shroom cap
			blockColors[blockColorID(100, 7)] = getColor(terrainBuff, 125); // huge red shroom cap
			blockColors[blockColorID(100, 8)] = getColor(terrainBuff, 125); // huge red shroom cap
			blockColors[blockColorID(100, 9)] = getColor(terrainBuff, 125); // huge red shroom cap
			blockColors[blockColorID(100, 10)] = getColor(terrainBuff, 142); // huge red shroom stem, pores on top
			blockColors[blockColorID(100, 14)] = getColor(terrainBuff, 125); // huge red shroom all cap
			blockColors[blockColorID(100, 15)] = getColor(terrainBuff, 141); // huge red shroom all stem
			blockColors[blockColorID(101, 0)] = getColor(terrainBuff, 85); // iron bars
			blockColors[blockColorID(102, 0)] = getColor(terrainBuff, 49); // glass pane
			blockColors[blockColorID(103, 0)] = getColor(terrainBuff, 137); // melon
			blockColors[blockColorID(104, 0)] = getColor(terrainBuff, 127); // pumpkin stem
			blockColors[blockColorID(105, 0)] = getColor(terrainBuff, 127); // melon stem
			blockColors[blockColorID(106, 0)] = getColor(terrainBuff, 143); // vines
			blockColors[blockColorID(107, 0)] = getColor(terrainBuff, 4); // fence gate
			blockColors[blockColorID(108, 0)] = getColor(terrainBuff, 7); // brick stairs
			blockColors[blockColorID(108, 1)] = getColor(terrainBuff, 7); 
			blockColors[blockColorID(108, 2)] = getColor(terrainBuff, 7);
			blockColors[blockColorID(108, 3)] = getColor(terrainBuff, 7);
			blockColors[blockColorID(109, 0)] = getColor(terrainBuff, 54); // stone brick stairs
			blockColors[blockColorID(109, 1)] = getColor(terrainBuff, 54);
			blockColors[blockColorID(109, 2)] = getColor(terrainBuff, 54);
			blockColors[blockColorID(109, 3)] = getColor(terrainBuff, 54);
			blockColors[blockColorID(110, 0)] = getColor(terrainBuff, 78); // mycelium
			blockColors[blockColorID(112, 0)] = getColor(terrainBuff, 224); // netherbrick
			blockColors[blockColorID(114, 0)] = getColor(terrainBuff, 224); // netherbrick stairs
			blockColors[blockColorID(114, 1)] = getColor(terrainBuff, 224); // netherbrick stairs
			blockColors[blockColorID(114, 2)] = getColor(terrainBuff, 224); // netherbrick stairs
			blockColors[blockColorID(114, 3)] = getColor(terrainBuff, 224); // netherbrick stairs
			blockColors[blockColorID(121, 0)] = getColor(terrainBuff, 175); // endstone
			blockColors[blockColorID(123, 0)] = getColor(terrainBuff, 211); // inactive glowstone lamp
			blockColors[blockColorID(124, 0)] = getColor(terrainBuff, 212); // active glowstone lamp
			blockColors[blockColorID(125, 0)] = getColor(terrainBuff, 4); // wooden double slab
			blockColors[blockColorID(125, 1)] = getColor(terrainBuff, 198);  
			blockColors[blockColorID(125, 2)] = getColor(terrainBuff, 214); 
			blockColors[blockColorID(125, 3)] = getColor(terrainBuff, 199); 
			blockColors[blockColorID(126, 0)] = getColor(terrainBuff, 4); // wooden slab
			blockColors[blockColorID(126, 1)] = getColor(terrainBuff, 198);  
			blockColors[blockColorID(126, 2)] = getColor(terrainBuff, 214); 
			blockColors[blockColorID(126, 3)] = getColor(terrainBuff, 199);
			blockColors[blockColorID(126, 8)] = getColor(terrainBuff, 4); // wooden slab upside down
			blockColors[blockColorID(126, 9)] = getColor(terrainBuff, 198);  
			blockColors[blockColorID(126, 10)] = getColor(terrainBuff, 214); 
			blockColors[blockColorID(126, 11)] = getColor(terrainBuff, 199);
			blockColors[blockColorID(127, 0)] = getColor(terrainBuff, 168);  // cocoa plant
			blockColors[blockColorID(128, 0)] = getColor(terrainBuff, 176);  // sandstone stairs
			blockColors[blockColorID(128, 1)] = getColor(terrainBuff, 176);
			blockColors[blockColorID(128, 2)] = getColor(terrainBuff, 176);
			blockColors[blockColorID(128, 3)] = getColor(terrainBuff, 176);
			blockColors[blockColorID(129, 0)] = getColor(terrainBuff, 171); // emerald ore
			blockColors[blockColorID(130, 0)] = getColor(terrainBuff, 182); // ender chest (using side of enchanting table)
			blockColors[blockColorID(131, 0)] = getColor(terrainBuff, 172); // tripwire hook
			blockColors[blockColorID(132, 0)] = getColor(terrainBuff, 173); // tripwire
			blockColors[blockColorID(133, 0)] = getColor(terrainBuff, 25); // emerald block
			blockColors[blockColorID(134, 0)] = getColor(terrainBuff, 198);  // spruce stairs
			blockColors[blockColorID(134, 1)] = getColor(terrainBuff, 198);
			blockColors[blockColorID(134, 2)] = getColor(terrainBuff, 198);
			blockColors[blockColorID(134, 3)] = getColor(terrainBuff, 198);
			blockColors[blockColorID(135, 0)] = getColor(terrainBuff, 214);  // birch stairs
			blockColors[blockColorID(135, 1)] = getColor(terrainBuff, 214);
			blockColors[blockColorID(135, 2)] = getColor(terrainBuff, 214);
			blockColors[blockColorID(135, 3)] = getColor(terrainBuff, 214);
			blockColors[blockColorID(136, 0)] = getColor(terrainBuff, 199);  // jungle stairs
			blockColors[blockColorID(136, 1)] = getColor(terrainBuff, 199);
			blockColors[blockColorID(136, 2)] = getColor(terrainBuff, 199);
			blockColors[blockColorID(136, 3)] = getColor(terrainBuff, 199);
			blockColors[blockColorID(137, 0)] = getColor(terrainBuff, 184); // command block
			blockColors[blockColorID(138, 0)] = getColor(terrainBuff, 41); // beacon block
			blockColors[blockColorID(151, 0)] = getColor(terrainBuff, 57); // light sensor
			blockColors[blockColorID(152, 0)] = getColor(terrainBuff, 26); // redstone block
			blockColors[blockColorID(153, 0)] = getColor(terrainBuff, 191); // nether quartz ore

			getCTMcolors();

		    this.timer = calcLull+1; // force rerender with texture pack colors now that they are loaded
		}
		catch (Exception e) {
			System.out.println("ERRRORRR " + e.getLocalizedMessage());
			e.printStackTrace();
		}
		
	}
	
	private void reloadBiomeColors(boolean biomes) {
		if (biomes)
			blockColors[blockColorID(2, 0)] = getColor(terrainBuff, 0);
		else // apply a default color
			blockColors[blockColorID(2, 0)] = colorMultiplier(getColor(terrainBuff, 0), ColorizerGrass.getGrassColor(0.7, 0.8)) & 0x00FFFFFF;

		if (biomes) {
			blockColors[blockColorID(18, 0)] = getColor(terrainBuff, 53);
			blockColors[blockColorID(18, 1)] = getColor(terrainBuff, 133);
			blockColors[blockColorID(18, 2)] = getColor(terrainBuff, 53);
			blockColors[blockColorID(18, 3)] = getColor(terrainBuff, 196);
		}
		else {
			blockColors[blockColorID(18, 0)] = colorMultiplier(getColor(terrainBuff, 53), ColorizerFoliage.getFoliageColor(0.7,  0.8)) & 0x00FFFFFF;
			blockColors[blockColorID(18, 1)] = colorMultiplier(getColor(terrainBuff, 133), ColorizerFoliage.getFoliageColorPine()) & 0x00FFFFFF;
			blockColors[blockColorID(18, 2)] = colorMultiplier(getColor(terrainBuff, 53), ColorizerFoliage.getFoliageColorBirch()) & 0x00FFFFFF;
			blockColors[blockColorID(18, 3)] = colorMultiplier(getColor(terrainBuff, 196), ColorizerFoliage.getFoliageColor(0.7,  0.8)) & 0x00FFFFFF;
		}
	}
	
	private void reloadWaterColor(boolean transparency) {
		getWaterColor(transparency?terrainBuffTrans:terrainBuff);
	}
	
	private int getColor(BufferedImage image, int textureID) {
//		int texX = (textureID & 15) << 4; // 0 based horizontal offset in pixels from left of terrain.png (assumes each block is 16px)
//		int texY = textureID & 240; // 0 based vertical offset in pixels from top of terrain.png (assumes each block is 16px)
		int texX = textureID & 15; // 0 based column in terrain.png 
		int texY = (textureID & 240) >> 4; // 0 based row in terrain.png
//		System.out.println("int: " + image.getRGB(texX, texY));
//		System.out.println("as hex: " +  java.lang.Integer.toHexString(image.getRGB(texX, texY)));
//		System.out.println("22 int: " + (image.getRGB(texX, texY) & 0x00FFFFFF));
//		System.out.println("22 hex: " +  java.lang.Integer.toHexString(image.getRGB(texX, texY) & 0x00FFFFFF));
		
		return (image.getRGB(texX, texY) & 0x00FFFFFF); // the and dumps the alpha, in hex the ff at the beginning
	}
	
    private int colorMultiplier(int color1, int color2)
    {
    	
    	//int alpha1 = (color1 >> 24 & 255);
        int red1 = (color1 >> 16 & 255);
        int green1 = (color1 >> 8 & 255);
        int blue1 = (color1 >> 0 & 255);

    	//int alpha2 = (color2 >> 24 & 255);
        int red2 = (color2 >> 16 & 255);
        int green2 = (color2 >> 8 & 255);
        int blue2 = (color2 >> 0 & 255);
        
        //int alpha = alpha1 * alpha2 / 256;
        int red = red1 * red2 / 256;
        int green = green1 * green2 / 256;
        int blue = blue1 * blue2 / 256;
        
        
        return /*(alpha & 255) << 24 |*/ (red & 255) << 16 | (green & 255) << 8 | blue & 255;
       // this.red = (float)(var1 >> 16 & 255) * 0.003921569F * var2;
       // this.green = (float)(var1 >> 8 & 255) * 0.003921569F * var2;
       // this.blue = (float)(var1 >> 0 & 255) * 0.003921569F * var2;

    }
    
    private void getWaterColor(BufferedImage terrainBuff) {
    	try {
    		int waterRGB = -1;
    		int waterBase = -1;
    		InputStream is = pack.getResourceAsStream("/anim/custom_water_still.png");
    		if (is == null) { 
    			is = pack.getResourceAsStream("/custom_water_still.png");
    		}
    		if (is == null) {
    			if (this.transparency) {
    				int texX = 205 & 15; // 0 based column in terrain.png 
    				int texY = (205 & 240) >> 4; // 0 based row in terrain.png
    				waterBase = terrainBuff.getRGB(texX, texY);
    				waterAlpha = waterBase >> 24 & 255;
    				waterBase = waterBase & 0x00FFFFFF;
    			}
    			else {
        			waterBase = getColor(terrainBuff, 205);
    				waterAlpha = 180;
    			}
    		}
    		else {
    			java.awt.Image water = ImageIO.read(is);
    			is.close();
    			water = water.getScaledInstance(1,1, java.awt.Image.SCALE_SMOOTH);
    			BufferedImage waterBuff = new BufferedImage(water.getWidth(null), water.getHeight(null), transparency?BufferedImage.TYPE_4BYTE_ABGR:BufferedImage.TYPE_INT_RGB);
    			java.awt.Graphics gfx = waterBuff.createGraphics();
    			// Paint the image onto the buffered image
    			gfx.drawImage(water, 0, 0, null);
    			gfx.dispose();
    			waterBase = waterBuff.getRGB(0, 0);
   				waterAlpha = waterBase >> 24 & 255;
    			waterBase = waterBase & 0x00FFFFFF; 
    		}
    		int waterMult = -1;
    		waterColorBuff = null;
    		is = pack.getResourceAsStream("/misc/watercolorX.png");
    		if (is != null) {
    			java.awt.Image waterColor = ImageIO.read(is);
    			is.close();
    			waterColorBuff = new BufferedImage(waterColor.getWidth(null), waterColor.getHeight(null), BufferedImage.TYPE_INT_RGB);
    			java.awt.Graphics gfx = waterColorBuff.createGraphics();
    			// Paint the image onto the buffered image
    			gfx.drawImage(waterColor, 0, 0, null);
    			gfx.dispose();
    			waterMult = waterColorBuff.getRGB(waterColorBuff.getWidth()*76/255, waterColorBuff.getHeight()*112/255) & 0x00FFFFFF;
    		}
    		if (waterMult != -1 && waterMult != 0 && !biomes) // && != 0 cause some packs (ravands!) have completely transparent areas in watercolorX (ie no multiplier applied at all most of the time)
    			waterRGB = this.colorMultiplier(waterBase, waterMult);
    		else
    			waterRGB = waterBase;
    		blockColors[blockColorID(8, 0)] = waterRGB;
    		blockColors[blockColorID(9, 0)] = waterRGB;
    	} 
    	catch (Exception e) {
			chatInfo("§EError Loading Water Color, using defaults");
			blockColors[blockColorID(8, 0)] = 0x2f51ff;
			blockColors[blockColorID(9, 0)] = 0x2f51ff;
    	}
    }
    
    private void getLavaColor(BufferedImage terrainBuff) {
    	try {
    		int lavaRGB = -1;
    		InputStream is = pack.getResourceAsStream("/anim/custom_lava_still.png");
    		if (is == null) { 
    			is = pack.getResourceAsStream("/custom_lava_still.png");
    		}
    		if (is == null) {
    			lavaRGB = getColor(terrainBuff, 237);
    		}
    		else {
    			java.awt.Image lava = ImageIO.read(is);
    			is.close();
    			lava = lava.getScaledInstance(1,1, java.awt.Image.SCALE_SMOOTH);
    			BufferedImage lavaBuff = new BufferedImage(lava.getWidth(null), lava.getHeight(null), BufferedImage.TYPE_INT_RGB);
    			java.awt.Graphics gfx = lavaBuff.createGraphics();
    			// Paint the image onto the buffered image
    			gfx.drawImage(lava, 0, 0, null);
    			gfx.dispose();
    			lavaRGB = lavaBuff.getRGB(0, 0) & 0x00FFFFFF;
    		}
    		blockColors[blockColorID(10, 0)] = lavaRGB;
    		blockColors[blockColorID(11, 0)] = lavaRGB;
    	} 
    	catch (Exception e) {
			chatInfo("§EError Loading Water Color, using defaults");
			blockColors[blockColorID(8, 0)] = 0xecad41;
			blockColors[blockColorID(9, 0)] = 0xecad41;
    	}
    }
    
    // ctm stuff
    
    private void getCTMcolors() {
    	for (String s : listResources("/ctm", ".properties")) {
    		loadCTM(s);
    	}
    }
    
    private void loadCTM(String filePath) {
        if (filePath == null) {
            return;
        }
        java.util.Properties properties = new java.util.Properties();
        InputStream input = pack.getResourceAsStream(filePath);
        try {
        	if (input != null) {
        		properties.load(input);
                input.close();
        	}
        } 
        catch (IOException e) {
        	return;
        }
        
        filePath = filePath.toLowerCase();
        String blockNum = filePath.substring(filePath.lastIndexOf("block")+5, filePath.lastIndexOf(".properties"));
        int blockID = -1;
        try {
        	blockID = Integer.parseInt(blockNum);
        }
        catch (NumberFormatException e) {
            return;
        }

        String method = properties.getProperty("method", "").trim().toLowerCase();
        String faces = properties.getProperty("faces", "").trim().toLowerCase();
        String filename = properties.getProperty("source", "").trim().toLowerCase();
        String metadata = properties.getProperty("metadata", "0").trim().toLowerCase();
        String tiles = properties.getProperty("tiles", "").trim().toLowerCase();

        int metadataInt = Integer.parseInt( metadata);

        int[] tilesInts = parseIntegerList(tiles, 0, 255);
        int tilesInt = 0;
        if (tilesInts.length > 0) {
        	tilesInt = tilesInts[0];
        }
        System.out.println("block: " + blockNum + ", metadata: " + metadata + ", tile: " + tilesInt);


        if (method.equals("sandstone") || method.equals("top") || faces.contains("top")) {
        	try {
        		InputStream is = pack.getResourceAsStream(filename);
        		java.awt.Image top = ImageIO.read(is);
        		is.close();
        		top = top.getScaledInstance(16,16, java.awt.Image.SCALE_SMOOTH);
        		BufferedImage topBuff = new BufferedImage(top.getWidth(null), top.getHeight(null), BufferedImage.TYPE_INT_RGB);
        		java.awt.Graphics gfx = topBuff.createGraphics();
        		// Paint the image onto the buffered image
        		gfx.drawImage(top, 0, 0, null);
        		gfx.dispose();
        		int topRGB = getColor(topBuff, tilesInt);
        		blockColors[blockColorID(blockID, metadataInt)] = topRGB;
        	}
        	catch (IOException e) {
        	}
        }
    }
    
    private int[] parseIntegerList(String list, int minValue, int maxValue) {
        ArrayList<Integer> tmpList = new ArrayList<Integer>();
        for (String token : list.replace(',', ' ').split("\\s+")) {
            token = token.trim();
            try {
                if (token.matches("^\\d+$")) {
                    tmpList.add(Integer.parseInt(token));
                } else if (token.matches("^\\d+-\\d+$")) {
                    String[] t = token.split("-");
                    int min = Integer.parseInt(t[0]);
                    int max = Integer.parseInt(t[1]);
                    for (int i = min; i <= max; i++) {
                        tmpList.add(i);
                    }
                }
            } catch (NumberFormatException e) {
            }
        }
        if (minValue <= maxValue) {
            for (int i = 0; i < tmpList.size(); ) {
                if (tmpList.get(i) < minValue || tmpList.get(i) > maxValue) {
                    tmpList.remove(i);
                } else {
                    i++;
                }
            }
        }
        int[] a = new int[tmpList.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = tmpList.get(i);
        }
        return a;
    }
    
    private String[] listResources(String directory, String suffix) {
        if (directory == null) {
            directory = "";
        }
        if (directory.startsWith("/")) {
            directory = directory.substring(1);
        }
        if (suffix == null) {
            suffix = "";
        }

        ArrayList<String> resources = new ArrayList<String>();
        if (pack instanceof TexturePackDefault) {
            // nothing
        } else if (pack instanceof TexturePackCustom) {
        	try {
        		java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(((TexturePackCustom) pack).texturePackFile);
        		if (zipFile != null) {
        			for (java.util.zip.ZipEntry entry : Collections.list(zipFile.entries())) {
        				final String name = entry.getName();
        				if (name.startsWith(directory) && name.endsWith(suffix)) {
        					resources.add("/" + name);
        				}
        			}
        		}
        	}
        	catch (java.util.zip.ZipException e) {
        	}
        	catch (IOException e) {
        	}
        } else if (pack instanceof TexturePackFolder) {
            File folder = ((TexturePackFolder) pack).texturePackFile;
            if (folder != null && folder.isDirectory()) {
                String[] list = new File(folder, directory).list();
                if (list != null) {
                    for (String s : list) {
                        if (s.endsWith(suffix)) {
                            resources.add("/" + new File(new File(directory), s).getPath().replace('\\', '/'));
                        }
                    }
                }
            }
        }

        Collections.sort(resources);
        return resources.toArray(new String[resources.size()]);
    }

	public void saveAll() {
		settingsFile = new File(getAppDir("minecraft"), "zan.settings");

		try {
			PrintWriter out = new PrintWriter(new FileWriter(settingsFile));
			out.println("Show Coordinates:" + Boolean.toString(coords));
			out.println("Show Map in Nether:" + Boolean.toString(showNether));
			out.println("Enable Cave Mode:" + Boolean.toString(showCaves));
			out.println("Dynamic Lighting:" + Boolean.toString(lightmap));
			out.println("Height Map:" + Boolean.toString(heightmap));
			out.println("Slope Map:" + Boolean.toString(slopemap));
			out.println("Filtering:" + Boolean.toString(filtering));
			out.println("Transparency:" + Boolean.toString(transparency));
			out.println("Biomes:" + Boolean.toString(biomes));
			out.println("Square Map:" + Boolean.toString(squareMap));
			out.println("Old North:" + Boolean.toString(oldNorth));
			out.println("Waypoint Beacons:" + Boolean.toString(showBeacons));
			out.println("Waypoint Signs:" + Boolean.toString(showWaypoints));
			out.println("Welcome Message:" + Boolean.toString(welcome));
			out.println("Map Corner:" + Integer.toString(mapCorner));
			out.println("Map Size:" + Integer.toString(sizeModifier));
			out.println("Update Frequency:" + Integer.toString(calcLull));
			//out.println("Threading:" + Boolean.toString(threading));
			out.println("Zoom Key:" + getKeyDisplayString(keyBindZoom.keyCode));
			out.println("Menu Key:" + getKeyDisplayString(keyBindMenu.keyCode));
			out.println("Waypoint Key:" + getKeyDisplayString(keyBindWaypoint.keyCode));
			out.println("Mob Key:" + getKeyDisplayString(keyBindMobToggle.keyCode));
			if (radar != null)
				radar.saveAll(out);
			out.close();
		} catch (Exception local) {
			chatInfo("§EError Saving Settings");
		}
	}

	public void saveWaypoints() {
		String worldNameSave = scrubFileName(worldName);

		settingsFile = new File(getAppDir("minecraft/mods/zan"), worldNameSave + ".points");

		try {
			PrintWriter out = new PrintWriter(new FileWriter(settingsFile));

			for(Waypoint pt:wayPts) {
				if(!pt.name.startsWith("^")) 
					out.println(pt.name + ":" + pt.x + ":" + pt.z + ":" + pt.y + ":" + Boolean.toString(pt.enabled) + ":" + pt.red + ":" + pt.green + ":" + pt.blue + ":" + pt.imageSuffix);
			}

			out.close();
		} catch (Exception local) {
			chatInfo("§EError Saving Waypoints");
		}
	}
	
	private String scrubFileName(String input) {
		// illegal characters:   < > : " / \ | ? *
		// colon is problematic.  let's just hope no one ever uses that.  Supposed to be to separate ports.
		input = input.replace("<", "~less~");
		input = input.replace(">", "~greater~");
		input = input.replace(":", "~colon~");
		input = input.replace("\"", "~quote~");
		input = input.replace("/", "~slash~");
		input = input.replace("\\", "~backslash~"); 
		input = input.replace("|", "~pipe~");
		input = input.replace("?", "~question~");
		input = input.replace("*", "~star~");
		return input;
	}

	private void loadWaypoints() {
		String worldNameWithPort = scrubFileName(worldName);

		String worldNameWithoutPort = worldName;
	    int portSepLoc = worldName.lastIndexOf(":");
	    if(portSepLoc != -1)  
	    	worldNameWithoutPort = worldNameWithoutPort.substring(0, portSepLoc);
	    worldNameWithoutPort = scrubFileName(worldNameWithoutPort);
		
		wayPts = new ArrayList<Waypoint>();
		settingsFile = new File(getAppDir("minecraft/mods/zan"), worldNameWithPort + ".points");
		if(!settingsFile.exists()) { // try to get it without .port from the new location, in case users copied it over or in case the server uses default port
			settingsFile = new File(getAppDir("minecraft/mods/zan"), worldNameWithoutPort + ".points");
		}
		if(!settingsFile.exists()) { // try to get it without .port and from the old location
			settingsFile = new File(getAppDir("minecraft"), worldNameWithoutPort + ".points");
		}
		if(!settingsFile.exists()) { // try to get it from Rei's
			settingsFile = new File(getAppDir("minecraft/mods/rei_minimap"), worldNameWithoutPort + ".points");
		}

		try {
			if(settingsFile.exists()) {
				BufferedReader in = new BufferedReader(new FileReader(settingsFile));
				String sCurrentLine;

				while ((sCurrentLine = in.readLine()) != null) {
					String[] curLine = sCurrentLine.split(":");
					
					if(curLine.length==4) { //super old zan's pre color I guess
						loadWaypoint(curLine[0],Integer.parseInt(curLine[1]),Integer.parseInt(curLine[2]),-1,Boolean.parseBoolean(curLine[3]),0,1,0,"");
					}
					else if (curLine.length==7) { // zan's when I started using it
						loadWaypoint(curLine[0],Integer.parseInt(curLine[1]),Integer.parseInt(curLine[2]),-1,Boolean.parseBoolean(curLine[3]),
								Float.parseFloat(curLine[4]), Float.parseFloat(curLine[5]), Float.parseFloat(curLine[6]),"");
					}
					else if (curLine.length==8) { // zan's with additional suffix (for "skull" etc), OR zan's with 3 dimension vars and no suffix (non skull waypoint)
						if (curLine[3].contains("true") || curLine[3].contains("false"))
							loadWaypoint(curLine[0],Integer.parseInt(curLine[1]),Integer.parseInt(curLine[2]),-1,Boolean.parseBoolean(curLine[3]),
									Float.parseFloat(curLine[4]), Float.parseFloat(curLine[5]), Float.parseFloat(curLine[6]), curLine[7]);
						else 
							loadWaypoint(curLine[0],Integer.parseInt(curLine[1]),Integer.parseInt(curLine[2]),Integer.parseInt(curLine[3]),Boolean.parseBoolean(curLine[4]),
									Float.parseFloat(curLine[5]), Float.parseFloat(curLine[6]), Float.parseFloat(curLine[7]),"");
					}
					else if (curLine.length==9) { // zan's with xyANDz and additional suffix (for "skull" etc)
						loadWaypoint(curLine[0],Integer.parseInt(curLine[1]),Integer.parseInt(curLine[2]),Integer.parseInt(curLine[3]),Boolean.parseBoolean(curLine[4]),
								Float.parseFloat(curLine[5]), Float.parseFloat(curLine[6]), Float.parseFloat(curLine[7]), curLine[8]);
					}
					else if (curLine.length==6) { // rei's
						int color = Integer.parseInt(curLine[5], 16); // ,16 is the radix for hex, which rei stores his color as
				        float red = (float)(color >> 16 & 255)/255; // split out to RGB, then get as a fraction
				        float green = (float)(color >> 8 & 255)/255; // like we store it (and OpenGL uses)
				        float blue = (float)(color >> 0 & 255)/255;
				        // alternate way to do it bleh though this works don't experiment
						//int r = color24 / 0x10000;
						//int g = (color24 - r * 0x10000)/0x100;
						//int b = (color24 - r * 0x10000-g*0x100);
						loadWaypoint(curLine[0],Integer.parseInt(curLine[1]),Integer.parseInt(curLine[3]),Integer.parseInt(curLine[2]),Boolean.parseBoolean(curLine[4]),
								red, green, blue, "");
					}
					
					// do in checkChanges instead.  load them any time we are in a new world (bring them back after return from nether.  Initial load is also a world change; would double up with this
					//EntityWaypoint ewpt = new EntityWaypoint(this.getWorld(), wpt);
					//this.getWorld().addWeatherEffect(ewpt);
				}
								
				in.close();
				//chatInfo("§EWaypoints loaded!"); // for " + worldName);
			} else chatInfo("§ENo waypoints exist for this world/server.");
		} catch (Exception local) {
			chatInfo("§EError Loading Waypoints");
			System.out.println("waypoint load error: " + local.getLocalizedMessage());
		}
	}
	
	public void loadWaypoint(String name, int x, int z, int y, boolean enabled, float red, float green, float blue, String suffix) {
		Waypoint newWaypoint = new Waypoint(name, x, z, y, enabled, red, green, blue, suffix);
		wayPts.add(newWaypoint);
	}
	
	// get a list of all the waypoints that don't have a yvalue yet.  
	// they will get iterated through on ticks and if their chunk is loaded they'll get a new yvalue based on ground height
	// as one travels around, eventually there will be none left
	public void populateOld2dWaypoints() {
		old2dWayPts = new ArrayList<Waypoint>();
		for(Waypoint wpt:wayPts) {
			if (wpt.y <= 0) {
				old2dWayPts.add(wpt);
			}
		}
	}
	
	public void graduateOld2dWaypoint(int i) {
		old2dWayPts.remove(i);
	}
	
	public void graduateOld2dWaypoint(Waypoint point) { 
		old2dWayPts.remove(point);
	}
	
	private void deleteWaypoint(int i) {
		graduateOld2dWaypoint(wayPts.get(i)); // removes from list of old 2d waypoints if it was there
		wayPts.get(i).kill();
		wayPts.remove(i);
		this.saveWaypoints();
	}
	
	public void deleteWaypoint(Waypoint point) { // TODO perhaps let entity know it is dead by making the entity a listener
		graduateOld2dWaypoint(point); // removse from list of old 2d waypoints if it was there
		point.kill();
		wayPts.remove(point);
		this.saveWaypoints();
	}
	
	public void addWaypoint(String name, int x, int z, int y, boolean enabled, float red, float green, float blue, String suffix) {
		Waypoint newWaypoint = new Waypoint(name, x, z, y, enabled, red, green, blue, suffix);
		wayPts.add(newWaypoint);
		EntityWaypoint ewpt = new EntityWaypoint(this.getWorld(), newWaypoint, (this.game.thePlayer.dimension==-1));
		//newWaypoint.entity = ewpt;
		this.getWorld().spawnEntityInWorld(ewpt);//.addWeatherEffect(ewpt);
		this.saveWaypoints();
	}
	
	public void addWaypoint(Waypoint newWaypoint) {
		wayPts.add(newWaypoint);
		EntityWaypoint ewpt = new EntityWaypoint(this.getWorld(), newWaypoint, (this.game.thePlayer.dimension==-1));
		//newWaypoint.entity = ewpt;
		this.getWorld().spawnEntityInWorld(ewpt);//.addWeatherEffect(ewpt);
		this.saveWaypoints();
	}
	
	private void injectWaypointEntities() {
		if (!(this.game.thePlayer.dimension==-1) || this.showNether) { // check if nether
			for(Waypoint wpt:wayPts) {
				EntityWaypoint ewpt = new EntityWaypoint(world, wpt, (this.game.thePlayer.dimension==-1));
				//wpt.entity = ewpt;
				this.getWorld().spawnEntityInWorld(ewpt);//.addWeatherEffect(ewpt);
			}
		}
	}
	
	private void sortWaypointEntities() {
		java.util.List entities = this.game.theWorld.getLoadedEntityList();
		synchronized (entities) {
			int moved = 0;
			for(int j = 0; j < (entities.size() - this.wayPts.size()); j++) { // find waypoint entities that aren't at the end and put them there
				Entity entity = (Entity)entities.get(j);
				if (entity instanceof EntityWaypoint) {
					moved++;
					java.util.Collections.swap(entities, j, nextNonWaypoint(entities));
					//System.out.println("swapped " + j + ", total moved " + moved);
				}
			}
			if (moved > 0) {// only need to sort if waypoint(s) got dumped at the end.  Otherwise all is as it was before, namely sorted
				try {
					Collections.sort(entities.subList(entities.size()-wayPts.size(), entities.size())); // sort waypoint entities so ones behind do not show through (actually reversed.. closest ones drawn first)
				}
				catch (ClassCastException e) {
					//System.out.println(e.getLocalizedMessage());
				} // just in case.  I don't run into this anymore, but I'd rather it not happen to someone else
			}
/*			String errorM = "";
			for(int j = (entities.size() - this.wayPts.size()); j < entities.size(); j++) { // find waypoint entities that aren't at the end and put them there
				Entity entity = (Entity)entities.get(j);
				if (!(entity instanceof EntityWaypoint)) {
					errorM += "np: " + j + " ";
				}
			}
			if (!errorM.equals("")) {
				for(Waypoint wpt:wayPts) {
					EntityWaypoint ewpt = wpt.entity;
					errorM += wpt.name.substring(0,2) + entities.indexOf(ewpt) + " ";
				}
				this.chatInfo(errorM);
				System.out.println(errorM);
			}*/
		}
			
	}
	
	public int nextNonWaypoint(List<Entity> entities) {
		for(int j = entities.size()-1; j >= (entities.size() - this.wayPts.size()); j--) { // find waypoint entities that aren't at the end and put them there
			if (!(entities.get(j) instanceof EntityWaypoint)) {
				return j;
			}
		}
		//this.chatInfo("no dest");
		//System.out.println("no dest");
		return entities.size()-1;
	}
		
	
	// the same, done differently.  Requires each waypoint to have a handle to its entity.
	/* private void sortWaypointEntities() {
		java.util.List entities = this.game.theWorld.getLoadedEntityList();
		int moved = 0;
		for(int j = 0; j < wayPts.size(); j++) { // check that all waypoints are at the end
			Entity entity = (Entity)(wayPts.get(j).entity);
			int loc = entities.indexOf(entity);
			if (loc < entities.size()-wayPts.size()) {
				moved++;
				java.util.Collections.swap(entities, loc, entities.size()-moved);
				//System.out.println("swapped " + loc + " to " + (entities.size()-moved) + ", total moved " + moved);
			}
		}
		if (moved > 0) {// only need to sort if waypoint(s) got dumped at the end.  Otherwise all is as it was before, namely sorted
			try {
				Collections.sort(entities.subList(entities.size()-wayPts.size(), entities.size())); // sort waypoint entities so ones behind do not show through (actually reversed.. closest ones drawn first)
			}
			catch (ClassCastException e) {} // just in case.  I don't run into this anymore, but I'd rather it not happen to someone else
		}
	} */
		
	private void renderMap (int x, int y, int scScale) {
		if (!this.hide && !this.fullscreenMap) {
			boolean scaleChanged = (this.scScale != scScale);
			this.scScale = scScale;

			if (squareMap) { // square map
				if (imageChanged) {
					this.map[this.lZoom].write();
					imageChanged = false;
				}
				if (this.zoom == 3) {
					GL11.glPushMatrix();
					GL11.glScalef(0.5f, 0.5f, 1.0f);
					this.disp(this.map[this.lZoom].index); 
					GL11.glPopMatrix();
				} else {
					this.disp(this.map[this.lZoom].index);
				}
				if (filtering) {
					GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
					GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				}
				// from here
				GL11.glPushMatrix();
				GL11.glTranslatef(x, y, 0.0F);
				GL11.glRotatef(northRotate, 0.0F, 0.0F, 1.0F); // +90 west at top.  +0 north at top
				GL11.glTranslatef(-x, -y, 0.0F);
				// to here + the popmatrix below only necessary with variable north, and no if/else statements in mapcalc
				drawPre();
				this.setMap(x, y);
				drawPost();

				GL11.glPopMatrix();

				try {
					this.disp(this.img("/mamiyaotaru/minimap.png"));
					drawPre();
					this.setMap(x, y);
					drawPost();
				} catch (Exception localException) {
					this.error = "error: minimap overlay not found!";
				}
				this.drawDirections(x, y);

				for(Waypoint pt:wayPts) {
					if(pt.enabled) {
						double wayX = 0;
						double wayY = 0;
						if (this.game.thePlayer.dimension!=-1) {
							wayX = this.xCoord() - pt.x;
							wayY = this.zCoord() - pt.z;
						}
						else {
							wayX = this.xCoord() - (pt.x / 8);
							wayY = this.zCoord() - (pt.z / 8);
						}
						if (Math.abs(wayX)/(Math.pow(2,this.zoom)/2) > 31 || Math.abs(wayY)/(Math.pow(2,this.zoom)/2) > 31) {
							float locate = (float)Math.toDegrees(Math.atan2(wayX, wayY));
							double hypot = Math.sqrt((wayX*wayX)+(wayY*wayY));
							hypot = hypot / Math.max(Math.abs(wayX), Math.abs(wayY)) * 34;
							try {
								GL11.glPushMatrix();
								GL11.glColor3f(pt.red, pt.green, pt.blue);
								//this.disp(this.img("/marker.png"));
								this.disp(scScale>=3?this.img("/mamiyaotaru/marker" + pt.imageSuffix + ".png"):this.img("/mamiyaotaru/marker" + pt.imageSuffix + "Small.png"));
								GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
								GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
								GL11.glTranslatef(x, y, 0.0F);
								GL11.glRotatef(-locate + northRotate, 0.0F, 0.0F, 1.0F); // +90 w top, 0 N top
								GL11.glTranslatef(-x, -y, 0.0F);
								GL11.glTranslated(0.0D,/*-34.0D*/-hypot,0.0D); // hypotenuse is variable.  34 incorporated hypot's calculation above
								drawPre();
								this.setMap(x, y, 16);
								drawPost();
							} catch (Exception localException) {
								this.error = "Error: marker overlay not found!";
							} finally {
								GL11.glPopMatrix();
							}
						} // end if waypoint is far away and drawn as an arrow on the edge
						else { // else waypoint is close enough to be on the map
							float locate = (float)Math.toDegrees(Math.atan2(wayX, wayY));
							double hypot = Math.sqrt((wayX*wayX)+(wayY*wayY))/(Math.pow(2,this.zoom)/2);
							try 
							{
								GL11.glPushMatrix();
								GL11.glColor3f(pt.red, pt.green, pt.blue);
								//this.disp(this.img("/waypoint.png"));
								this.disp(scScale>=3?this.img("/mamiyaotaru/waypoint" + pt.imageSuffix + ".png"):this.img("/mamiyaotaru/waypoint" + pt.imageSuffix + "Small.png"));
								if (scScale == 3 || scScale > 4) { // filter on odd zoom levels
									GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
									GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
								}
							//	GL11.glTranslated(-wayX/(Math.pow(2,this.zoom)/2),-wayY/(Math.pow(2,this.zoom)/2),0.0D); //y -x W at top, -x -y N at top
								// from here
								GL11.glTranslatef(x, y, 0.0F);
								GL11.glRotatef(-locate + northRotate, 0.0F, 0.0F, 1.0F); // + 90 w top, 0 n top
								GL11.glTranslated(0.0D,-hypot,0.0D);
								GL11.glRotatef(-(-locate + northRotate), 0.0F, 0.0F, 1.0F); // + 90 w top, 0 n top
								GL11.glTranslated(0.0D,hypot,0.0D);
								GL11.glTranslatef(-x, -y, 0.0F);
								GL11.glTranslated(0.0D,-hypot,0.0D);
								// to here only necessary with variable north, and no if/else statements in mapcalc.  otherwise uncomment the translated above this block
								drawPre();
								this.setMap(x, y, 16);
								drawPost();
							} catch (Exception localException) 
							{
								this.error = "Error: waypoint overlay not found!";
							} finally 
							{
								GL11.glPopMatrix();
							}
						} // end waypoint is on current map
					} // end if pt enabled
				} // end for waypoints
				if (radar != null && this.radarAllowed) // after map drawn, before arrow so they aren't on top of that stuff.  After waypoitns though don't want a creeper under a waypoint symbol
					radar.OnTickInGame(game);
				try { // draw arrow last.  Always want to see it above things so we know which direction we are facing on the map
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					GL11.glPushMatrix();
					//this.disp(scScale>=3?this.img("/mamiyaotaru/mmarrow.png"):this.img("/mamiyaotaru/mmarrowSmall.png"));
					this.disp(this.img("/mamiyaotaru/mmarrow.png"));
					GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
					GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
					GL11.glTranslatef(x, y, 0.0F); 
					GL11.glRotatef(this.direction, 0.0F, 0.0F, 1.0F); // -dir-90 W top, -dir-180 N top
					GL11.glTranslatef(-x, -y, 0.0F);
					drawPre();
					this.setMap(x, y, 16);
					drawPost();
				} catch (Exception localException) {
					this.error = "Error: minimap arrow not found!";
				} finally {
					GL11.glPopMatrix();
				}
			} // end if squaremap
			else { // else roundmap
			//	final long startTime = System.nanoTime();

/*				
				// do with opengl.  Faster.  Uses alpha channel, have to set lighting in actual RGB instead of alpha.
				// note, this fails on nVidia unless display is created with alpha bits  Display.create((new PixelFormat()).withDepthBits(24).withAlphaBits(8));
				// have to do that in minecraft.java, don't want to alter that.  So no destination alpha exists or can be used on nVidia cards :(
				GL11.glColorMask(false,false,false,true); // draw to alpha (from circle.png) - used to make square map round with GL
				if (this.game.gameSettings.showDebugInfo) { // only do f3 fix (that makes map invisible) if f3 text is up.  Still issues if f3 AND chat, oh well
					// clear alpha before drawing circle to it to get rid of the alpha the f3 text put in
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
					GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_ZERO);
					GL11.glColor3f(0, 0, 255);
					//Begin drawing the square with the assigned coordinates and size
					GL11.glBegin(GL11.GL_QUADS); 
					GL11.glVertex2f(scWidth-80, 80);//bottom left of the square
					GL11.glVertex2f(scWidth+5, 840);//bottom right of the square
					GL11.glVertex2f(scWidth+5, 0);//top right of the square
					GL11.glVertex2f(scWidth-80, 0);//top left of the square
					GL11.glEnd();
					GL11.glColor4f(1, 1, 1, 1);
					
					//basically the same, but slower?  have to check
					//GL11.glClearColor(0, 0, 0, 0);
					//GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
				}
							
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				this.disp(scScale>=3?this.img("/mamiyaotaru/circle2x.png"):this.img("/mamiyaotaru/circle.png")); // does weird things to f3 text.  deal with it!  Also fux dynamic lighting.  Deal with it :(  (can do if we don't usee alpha channel for lighting info, just darken actual RGB
				drawPre();
				this.setMap(scWidth);
				drawPost();
				
				
				GL11.glColorMask(true,true,true,true);
				GL11.glBlendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA); // pasted on image uses alpha of BG - can stencil it out, but images own alpha goes away
*/				
				
				// this chunk used for 2d and stenciling, not FBO
/*			// 	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ZERO); // used for light info stored in alpha channel          
				if (this.zoom == 3) {
					GL11.glPushMatrix();
					GL11.glScalef(0.5f, 0.5f, 1.0f);
					this.q = this.tex(this.map[this.lZoom]);
					GL11.glPopMatrix();
				} else this.q = this.tex(this.map[this.lZoom]);
				
				// This is for rotating the whole map image.  Was fine when transparency stencil was drawn, then map and we rotated the map
				// now we are drawing FBO, that has the transparency baked in.  Can't rotate that.  Instead rotate the map in the FBO.  Note the differences (in rotation, translation, and normal locations) due to Minecraft doing it backwards
				GL11.glPushMatrix();
		    	GL11.glTranslatef(scWidth - 32.0F, 37.0F, 0.0F); 
				GL11.glRotatef(this.direction + 180.0F, 0.0F, 0.0F, 1.0F); 
				GL11.glTranslatef(-(scWidth - 32.0F), -37.0F, 0.0F);

				if(this.zoom==0) 
					GL11.glTranslatef(-1.1f, -0.8f, 0.0f);
				else 
					GL11.glTranslatef(-0.5f, -0.5f, 0.0f);
*/
				
				if (fboEnabled) {
					// FBO render pass
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0); // unlink textures because if we dont it all is gonna fail

					GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT | GL11.GL_TRANSFORM_BIT | GL11.GL_COLOR_BUFFER_BIT);
					GL11.glViewport (0, 0, 256, 256); // set The Current Viewport to the fbo size
					GL11.glMatrixMode(GL11.GL_PROJECTION);
					GL11.glPushMatrix();
					GL11.glLoadIdentity();
					GL11.glOrtho(0.0, 256.0, 256.0, 0.0, 1000.0, 3000.0);
					GL11.glMatrixMode(GL11.GL_MODELVIEW);
					GL11.glPushMatrix();
					// Reset The Modelview Matrix
					GL11.glLoadIdentity ();  
					GL11.glTranslatef(0.0F, 0.0F, -2000.0F);

					EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fboID); // draw to the FBP

					// Clear Screen And Depth Buffer on the fbo to black.  We draw same circle each time, so only need to do it on scale change (when we start drawing a new circle)
					if (scaleChanged) {
						GL11.glClearColor (0.0f, 0.0f, 0.0f, 0.0f);
						GL11.glClear (GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);            
						// "draw same circle each time" means we only need to draw the circle when the scale changes: move it inside the if block
						// this works beccause the blendfuncseparate that sets source alpha to dst color doesn't seem to care what that color is.  Anything but see through means it's drawn on
						// so last tick's round map serves fine as the stencil for the next one
						// except when not all local chunks are loaded, then we get artifacts when turning.  It's only right at the start, but looks bad man.  back out
					}
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ZERO);  
					//this.disp(scScale>=3?this.img("/mamiyaotaru/circle.png"):this.img("/mamiyaotaru/circleSmall.png")); 
					this.disp(this.img("/mamiyaotaru/circle.png"));
					drawPre();
					ldrawthree(0, 256, 1.0D, 0.0D, 0.0D);
					ldrawthree(256, 256, 1.0D, 1.0D, 0.0D);
					ldrawthree(256, 0, 1.0D, 1.0D, 1.0D);
					ldrawthree(0, 0, 1.0D, 0.0D, 1.0D);
					drawPost();

					// brightness baked into RGB
					//GL14.glBlendFuncSeparate(GL11.GL_ONE, GL11.GL_ZERO, GL11.GL_DST_COLOR, GL11.GL_ZERO); // source image's alpha is based on the color of the destination.  Don't need DST_ALPHA (thanks nvidia)
					// brightness as alpha channel
					GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_DST_COLOR, GL11.GL_ONE); // source image's alpha is based on the color of the destination.  Don't need DST_ALPHA (thanks nvidia)
					if (imageChanged) {
						this.map[this.lZoom].write();
						imageChanged = false;
					}
					if (this.zoom == 3) {
						GL11.glPushMatrix();
						GL11.glScalef(0.5f, 0.5f, 1.0f);
						this.disp(this.map[this.lZoom].index); 
						GL11.glPopMatrix();
					} else {
						this.disp(this.map[this.lZoom].index);
					}
					if (filtering) {
						GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
						GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
					}
					GL11.glTranslatef(128, 128, 0.0F); 
					GL11.glRotatef(this.direction-this.northRotate, 0.0F, 0.0F, 1.0F); 
					GL11.glTranslatef(-(128), -128F, 0.0F);
					if(this.zoom==0) 
						GL11.glTranslatef(-2.2f, 1.6f, 0.0f);
					else 
						GL11.glTranslatef(-1.0f, 1.0f, 0.0f);
					drawPre();
					// position of the texture to put on the vertexes is flipped in Y (ie last number is 1 instead of 0 and vice versa) because minecraft has things upside down in the ortho.  Upside down FBO makes it upside down twice, aka out of whack with minecraft 
					ldrawthree(0, 256, 1.0D, 0.0D, 0.0D);
					ldrawthree(256, 256, 1.0D, 1.0D, 0.0D);
					ldrawthree(256, 0, 1.0D, 1.0D, 1.0D);  
					ldrawthree(0, 0, 1.0D, 0.0D, 1.0D);
					drawPost();


					EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0); // stop drawing to FBO

					// restore viewport settings
					GL11.glMatrixMode(GL11.GL_PROJECTION);
					GL11.glPopMatrix();
					GL11.glMatrixMode(GL11.GL_MODELVIEW);
					GL11.glPopMatrix();
					GL11.glPopAttrib();		        


					GL11.glPushMatrix();
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					this.disp(fboTextureID);
				}
				else { // do in 2d

					// have to convert square image into a circle here now that we don't redraw the image from scratch every frame.  Can't just draw it round from the start, makes it impossible to know which ones are new
					// make into a circle with java2d. slightly slower
				//	if (imageChanged) {
				//		this.map[this.lZoom].write();
				//		imageChanged = false;
				//	}
					if (this.imageChanged){ 
						int diameter = this.map[this.lZoom].getWidth();
						roundImage = new GLBufferedImage(diameter, diameter,BufferedImage.TYPE_4BYTE_ABGR);
						java.awt.geom.Ellipse2D.Double ellipse = new java.awt.geom.Ellipse2D.Double((this.lZoom*10/6),(this.lZoom*10/6),diameter-(this.lZoom*2),diameter-(this.lZoom*2));
						//java.awt.geom.Ellipse2D.Double ellipse = new java.awt.geom.Ellipse2D.Double(0,0,diameter,diameter);
						java.awt.Graphics2D gfx = roundImage.createGraphics();
						gfx.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
						gfx.setClip(ellipse);
						//gfx.drawImage((this.map[this.zoom]).getScaledInstance(diameter, diameter, BufferedImage.SCALE_REPLICATE),0,0,null);
						// just draw it enlarged instead of creating a new scaled instance every time
						//gfx.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
						//gfx.drawImage(this.map[this.zoom],0,0,diameter,diameter,null); 
						// why enlarge it here at all?  opengl should do it faster.. but it will enlarge (in the case of the smaller ones) the massively aliased clipped one, with huge multi pixel jaggies around the edge.  Enlarge here into a larger (less aliased) clip for smoother edges even while the "pixels" are large
						// deal with it, it is faster
						gfx.drawImage(this.map[this.zoom],0,0,null); //(let opengl do it)
						gfx.dispose();
						roundImage.write();
						this.imageChanged = false;
					}
					
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ZERO); // used for light info stored in alpha channel
					//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); 
					if (this.zoom == 3) {
						GL11.glPushMatrix();
						GL11.glScalef(0.5f, 0.5f, 1.0f);
						this.disp(roundImage.index); 
						GL11.glPopMatrix();
					} else {
						this.disp(roundImage.index);
					}
					if (filtering) {
						GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
						GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
					}
					
					// This is for rotating the whole map image.  Was fine when transparency stencil was drawn, then map and we rotated the map
					// now we are drawing FBO, that has the transparency baked in.  Can't rotate that.  Instead rotate the map in the FBO.  Note the differences (in rotation, translation, and normal locations) due to Minecraft doing it backwards
					GL11.glPushMatrix();
			    	GL11.glTranslatef(x, y, 0.0F); 
					GL11.glRotatef(-this.direction+this.northRotate, 0.0F, 0.0F, 1.0F); 
					GL11.glTranslatef(-x, -y, 0.0F);

					if(this.zoom==0) 
						GL11.glTranslatef(-1.1f, -0.8f, 0.0f);
					else 
						GL11.glTranslatef(-0.5f, -0.5f, 0.0f);
				}
				

		//		System.out.println("time: " + (System.nanoTime()-startTime));
				drawPre();
				this.setMap(x, y);
				drawPost();
				GL11.glPopMatrix();
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				//GL11.glDisable(GL11.GL_BLEND);                         // Disable Blending
				//GL11.glEnable(GL11.GL_DEPTH_TEST); 

				GL11.glColor3f(1.0F, 1.0F, 1.0F);
				this.drawRound(x, y, scScale);
				this.drawDirections(x, y);
				
				for(Waypoint pt:wayPts) {
					if(pt.enabled) {
						int wayX = 0;
						int wayY = 0;
						if (this.game.thePlayer.dimension!=-1) {
							wayX = this.xCoord() - pt.x;
							wayY = this.zCoord() - pt.z;
						}
						else {
							wayX = this.xCoord() - (pt.x / 8);
							wayY = this.zCoord() - (pt.z / 8);
						}
						float locate = (float)Math.toDegrees(Math.atan2(wayX, wayY));
						double hypot = Math.sqrt((wayX*wayX)+(wayY*wayY))/(Math.pow(2,this.zoom)/2);

						if (hypot >= 31.0D) {
							try {
								GL11.glPushMatrix();
								GL11.glColor3f(pt.red, pt.green, pt.blue);
								//this.disp(this.img("/marker.png"));
								this.disp(scScale>=3?this.img("/mamiyaotaru/marker" + pt.imageSuffix + ".png"):this.img("/mamiyaotaru/marker" + pt.imageSuffix + "Small.png"));
								GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
								GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
								GL11.glTranslatef(x, y, 0.0F);
								GL11.glRotatef(-locate - this.direction+this.northRotate, 0.0F, 0.0F, 1.0F);
								GL11.glTranslatef(-x, -y, 0.0F);
								GL11.glTranslated(0.0D,-34.0D,0.0D);
								drawPre();
								this.setMap(x, y, 16);
								drawPost();
							} catch (Exception localException) {
								this.error = "Error: marker overlay not found!";
							} finally {
								GL11.glPopMatrix();
							}
						}
						else {
							try 
							{
								GL11.glPushMatrix();
								GL11.glColor3f(pt.red, pt.green, pt.blue);
								//this.disp(this.img("/waypoint.png"));
								this.disp(scScale>=3?this.img("/mamiyaotaru/waypoint" + pt.imageSuffix + ".png"):this.img("/mamiyaotaru/waypoint" + pt.imageSuffix + "Small.png"));
								if (scScale == 3 || scScale > 4) { // filter on odd zoom levels
									GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
									GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
								}
								GL11.glTranslatef(x, y, 0.0F);
								GL11.glRotatef(-locate -this.direction+this.northRotate, 0.0F, 0.0F, 1.0F);
								GL11.glTranslated(0.0D,-hypot,0.0D);
								GL11.glRotatef(-(-locate - this.direction+this.northRotate), 0.0F, 0.0F, 1.0F);
								GL11.glTranslated(0.0D,hypot,0.0D);
								GL11.glTranslatef(-x, -y, 0.0F);
								GL11.glTranslated(0.0D,-hypot,0.0D);
								drawPre();
								this.setMap(x, y, 16);
								drawPost();
							} catch (Exception localException) 
							{
								this.error = "Error: waypoint overlay not found!";
							} finally 
							{
								GL11.glPopMatrix();
							}
						}
					}
				} // end for waypoints
				GL11.glColor3f(1,1,1);
				if (radar != null && this.radarAllowed)
					radar.OnTickInGame(game);
			} // end roundmap
			if (tf) {
				this.disp(this.img("/mamiyaotaru/i18u.txt"));
				this.drawPre();
				this.setMap(x, y);
				this.drawPost();
			}		
		}
	}

	private void renderMapFull (int scWidth, int scHeight) {
		if (imageChanged) {	
			this.map[this.lZoom].write();
			imageChanged = false;
		}
		this.disp(this.map[this.lZoom].index);
		if (filtering) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		}

		// from here

		GL11.glPushMatrix();
		//GL11.glTranslatef(0f, 0f, -2f);
		GL11.glTranslatef(scWidth / 2.0F, (scHeight / 2.0F), 0.0F);
		GL11.glRotatef(northRotate, 0.0F, 0.0F, 1.0F); // +90 west at top.  +0 north at top
		GL11.glTranslatef(-(scWidth / 2.0F), -(scHeight / 2.0F), 0.0F);
		// to here + the popmatrix below only necessary with variable north, and no if/else statements in mapcalc
		drawPre();
		ldrawone(scWidth/2-128, scHeight/2+128, 1.0D, 0.0D, 1.0D);
		ldrawone(scWidth/2+128, scHeight/2+128, 1.0D, 1.0D, 1.0D);
		ldrawone(scWidth/2+128, scHeight/2-128, 1.0D, 1.0D, 0.0D);
		ldrawone(scWidth/2-128, scHeight/2-128, 1.0D, 0.0D, 0.0D);
		drawPost();
		GL11.glPopMatrix();

		try {
			GL11.glPushMatrix();
			this.disp(this.img("/mamiyaotaru/mmarrow.png"));
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
		    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		//	GL11.glTranslatef(0f, 0f, -2f);
			GL11.glTranslatef(scWidth/2, scHeight/2, 0.0F);
			GL11.glRotatef(this.direction, 0.0F, 0.0F, 1.0F); // -dir-90 W top, -dir-180 N top
			GL11.glTranslatef(-(scWidth/2), -(scHeight/2), 0.0F);
			drawPre();
			//ldrawone((scWidth+5)/2-32, (scHeight+5)/2+32, 1.0D, 0.0D, 1.0D); // the old, 128-ified arrow
			ldrawone(scWidth/2-4, scHeight/2+4, 1.0D, 0.0D, 1.0D);
			ldrawone(scWidth/2+4, scHeight/2+4, 1.0D, 1.0D, 1.0D);
			ldrawone(scWidth/2+4, scHeight/2-4, 1.0D, 1.0D, 0.0D);
			ldrawone(scWidth/2-4, scHeight/2-4, 1.0D, 0.0D, 0.0D);
			drawPost();
		} catch (Exception localException) {
			this.error = "Error: minimap arrow not found!";
		} finally {
			GL11.glPopMatrix();
		}
	}
	
	private void setupFBO () {
		 fboID = EXTFramebufferObject.glGenFramebuffersEXT(); // create a new framebuffer
	     fboTextureID = GL11.glGenTextures(); // and a new texture used as a color buffer
	     int width = 256;
	     int height = 256;
	     EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fboID); // switch to the new framebuffer
	     ByteBuffer byteBuffer = BufferUtils.createByteBuffer(4 * width * height);

	     GL11.glBindTexture(GL11.GL_TEXTURE_2D, fboTextureID); // Bind the colorbuffer texture
	     GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10496 );
	     GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10496 );
	     //GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
	     //GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST); 
	     GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
	     GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
	     GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_BYTE, byteBuffer); // Create the texture data

	     EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, fboTextureID, 0);  // attach it to the framebuffer
	     
	     EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0); // Switch back to normal framebuffer rendering
	}
	
	private void drawRound(int x, int y, int scScale) {
		try {
//			this.disp(scScale>=1?this.img("/mamiyaotaru/roundmap.png"):this.img("/mamiyaotaru/roundmapSmall.png"));
			this.disp(this.img("/mamiyaotaru/roundmap.png"));
			if (scScale != 4) { // filter on odd zoom levels
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			}
			drawPre();
			this.setMap(x, y);
			drawPost();
		} catch (Exception localException) {
			this.error = "Error: minimap overlay not found!";
		}
	}

	private void drawBox(double leftX, double rightX, double topY, double botY) {
		drawPre();
		ldrawtwo(leftX, botY, 0.0D);
		ldrawtwo(rightX, botY, 0.0D);
		ldrawtwo(rightX, topY, 0.0D);
		ldrawtwo(leftX, topY, 0.0D);
		drawPost();
	}

	private void setMap(int x, int y) {
	//	ldrawthree(paramInt1 - 64.0D, 64.0D + 5.0D, 1.0D, 0.0D, 1.0D);
	//	ldrawthree(paramInt1, 64.0D + 5.0D, 1.0D, 1.0D, 1.0D);
	//	ldrawthree(paramInt1, 5.0D, 1.0D, 1.0D, 0.0D);
	//	ldrawthree(paramInt1 - 64.0D, 5.0D, 1.0D, 0.0D, 0.0D);
		setMap(x, y, 128); // do this with default image size of 128 (that everything was before I decided to stop padding 16px and 8px images out to 128)
	}
	
	private void setMap(int x, int y, int imageSize) {
		int scale = imageSize/4; // 128 image is drawn from center - 32 to center + 32, as in the old setMap
		// 16 image is drawn from center - 4 to center + 4, quarter the size
		ldrawthree(x-scale, y+scale, 1.0D, 0.0D, 1.0D);
		ldrawthree(x+scale, y+scale, 1.0D, 1.0D, 1.0D);
		ldrawthree(x+scale, y-scale, 1.0D, 1.0D, 0.0D);
		ldrawthree(x-scale, y-scale, 1.0D, 0.0D, 0.0D);
	}
	
	private int tex(BufferedImage paramImg) {
		return this.renderEngine.allocateAndSetupTexture(paramImg);
	}

	private int img(String paramStr) { // returns index of texturemap(name) aka glBoundTexture.  If there isn't one, it glBindTexture's it in setupTexture
		return this.renderEngine.getTexture(paramStr);
	}

	private void disp(int paramInt) { 
		this.renderEngine.bindTexture(paramInt); // this func glBindTexture's GL_TEXTURE_2D, int paramInt
	}
	
	public void drawPre()
	{
		tesselator.startDrawingQuads();
	}
	public void drawPost()
	{
		tesselator.draw();
	}
	public void glah(int g)
	{
		renderEngine.deleteTexture(g);
	}
	public void ldrawone(int a, int b, double c, double d, double e)
	{
		tesselator.addVertexWithUV(a, b, c, d, e);
	}
	public void ldrawtwo(double a, double b, double c)
	{
		tesselator.addVertex(a, b, c);
	}
	public void ldrawthree(double a, double b, double c, double d, double e)
	{
		tesselator.addVertexWithUV(a, b, c, d, e);
	}
	public int getMouseX(int scWidth)
	{
		return Mouse.getX()*(scWidth+5)/game.displayWidth;
	}
	public int getMouseY(int scHeight)
	{
		return (scHeight+5) - Mouse.getY() * (scHeight+5) / this.game.displayHeight - 1;
	}

	// text rendering stuff below
	
	private void drawDirections(int x, int y) {

		/*// this looks to be a way to display an image with NSEW on it, overlaid over the top.  obsoleted, use text
		int wayX = this.xCoord();
		int wayY = this.yCoord();
		float locate = (float)Math.toDegrees(Math.atan2(wayX, wayY));
		double hypot = Math.sqrt((wayX*wayX)+(wayY*wayY))/(Math.pow(2,this.zoom)/2);


			try 
			{
				GL11.glPushMatrix();
				GL11.glColor3f(1.0f, 1.0f, 1.0f);
				this.disp(this.img("/compass.png"));
				GL11.glTranslatef(scWidth - 32.0F, 37.0F, 0.0F);
				GL11.glRotatef(-locate + this.direction + 180.0F, 0.0F, 0.0F, 1.0F);
				GL11.glTranslated(0.0D,-hypot,0.0D);
				GL11.glRotatef(-(-locate + this.direction + 180.0F), 0.0F, 0.0F, 1.0F);
				GL11.glTranslated(0.0D,hypot,0.0D);
				GL11.glTranslatef(-(scWidth - 32.0F), -37.0F, 0.0F);
				GL11.glTranslated(0.0D,-hypot,0.0D);
				drawPre();
				this.setMap(scWidth);
				drawPost();
			} catch (Exception localException) 
			{
				this.error = "Error: compass overlay not found!";
			} finally 
			{
				GL11.glPopMatrix();
			}*/
		float rotate;
		float distance;
		if (this.squareMap) {
			rotate = -90;
			distance = 58;
		}
		else {
			rotate = -this.direction - 90;
			distance = 64;
		}

		GL11.glPushMatrix();
		GL11.glScalef(0.5f, 0.5f, 1.0f);
		GL11.glTranslated((distance * Math.sin(Math.toRadians(-(rotate - 90.0D)))),(distance * Math.cos(Math.toRadians(-(rotate - 90.0D)))),0.0D); // direction -90 w top.  0 n top.  in all cases n top means 90 more (or w top means 90 less)
		this.write("N", x*2-2, y*2-4, 0xffffff);
		GL11.glPopMatrix();
		GL11.glPushMatrix();
		GL11.glScalef(0.5f, 0.5f, 1.0f);
		GL11.glTranslated((distance * Math.sin(Math.toRadians(-(rotate)))),(distance * Math.cos(Math.toRadians(-(rotate)))),0.0D);
		this.write("E", x*2-2, y*2-4, 0xffffff);
		GL11.glPopMatrix();
		GL11.glPushMatrix();
		GL11.glScalef(0.5f, 0.5f, 1.0f);
		GL11.glTranslated((distance * Math.sin(Math.toRadians(-(rotate + 90.0D)))),(distance * Math.cos(Math.toRadians(-(rotate + 90.0D)))),0.0D);
		this.write("S", x*2-2, y*2-4, 0xffffff);
		GL11.glPopMatrix();
		GL11.glPushMatrix();
		GL11.glScalef(0.5f, 0.5f, 1.0f);
		GL11.glTranslated((distance * Math.sin(Math.toRadians(-(rotate + 180.0D)))),(distance * Math.cos(Math.toRadians(-(rotate + 180.0D)))),0.0D);
		this.write("W", x*2-2, y*2-4, 0xffffff);
		GL11.glPopMatrix();
	}
	
	private void showCoords (int x, int y) { // x and y for drawing are counted from top left, not from 0,0 origina at center
		if(!this.hide) {
			GL11.glPushMatrix();
			GL11.glScalef(0.5f, 0.5f, 1.0f);
			String xy ="";
			if (this.game.thePlayer.dimension!=-1)
				xy = this.dCoord(xCoord()) + ", " + this.dCoord(zCoord());
			else
				xy = this.dCoord(xCoord()*8) + ", " + this.dCoord(zCoord()*8);
			int m = this.chkLen(xy)/2;
			this.write(xy, x*2-m, y*2, 0xffffff);
			xy = Integer.toString(this.yCoord());
			m = this.chkLen(xy)/2;
			//	xy="" + this.getWorld().skylightSubtracted + " " + this.getWorld().calculateSkylightSubtracted(1.0F) + " " + this.getWorld().func_35464_b(1.0F); // always 0 in SMP. method works, not value.  it's never updated, no world tick in SMP.  Fscks lightmap functionality
			this.write(xy, x*2-m, y*2 + 10, 0xffffff);
			GL11.glPopMatrix();
		} else {
			if (this.game.thePlayer.dimension!=-1) this.write("(" + this.dCoord(xCoord()) + ", " + this.yCoord() + ", " + this.dCoord(zCoord()) + ") " + (int) this.direction + "'", 2, 10, 0xffffff);
			else this.write("(" + this.dCoord(xCoord()*8) + ", " + this.yCoord() + ", " + this.dCoord(zCoord()*8) + ") " + (int) this.direction + "'", 2, 10, 0xffffff);
		}
	}
	
	private String dCoord(int paramInt1) {
		if(paramInt1 < 0)
			return "-" + Math.abs(paramInt1+1);
		else
			return "+" + paramInt1;
	}
	
	private int chkLen(String paramStr) {
		return this.fontRenderer.getStringWidth(paramStr);
	}

	private void write(String paramStr, int paramInt1, int paramInt2, int paramInt3) {
		this.fontRenderer.drawStringWithShadow(paramStr, paramInt1, paramInt2, paramInt3);
	}

	// menu from here
	
	// remnants of old menu, welcome screen only now
	
	public void setMenuNull()
	{
		game.currentScreen = null;
	}
	public Object getMenu()
	{
		return game.currentScreen;
	}
	private void showMenu (int scWidth, int scHeight) { 
		//System.out.println("menu: " + this.iMenu);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		int height;
		int maxSize = 0;
		int border = 2;
		boolean set = false;
		boolean click = false;
		int MouseX = getMouseX(scWidth);
		int MouseY = getMouseY(scHeight);

		if (Mouse.getEventButtonState() && Mouse.getEventButton() == 0)
			if (!this.lfclick) {
				set = true;
				this.lfclick = true;
			} else click = true;
		else if (this.lfclick) this.lfclick = false;
		
		String head = this.sMenu[0];

		for(height=1; height < sMenu.length - 1; height++)
			if (this.chkLen(sMenu[height])>maxSize) maxSize = this.chkLen(sMenu[height]);

		int title = this.chkLen(head);
		int centerX = (int)((scWidth+5)/2.0D);
		int centerY = (int)((scHeight+5)/2.0D);
		String hide = sMenu[sMenu.length-1];
		int footer = this.chkLen(hide);
		GL11.glDisable(3553); //GL_TEXTURE_2D
		GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.7f);
		double leftX = centerX - title/2.0D - border;
		double rightX = centerX + title/2.0D + border;
		double topY = centerY - (height-1)/2.0D*10.0D - border - 20.0D;
		double botY = centerY - (height-1)/2.0D*10.0D + border - 10.0D;
		this.drawBox(leftX, rightX, topY, botY);

		leftX = centerX - maxSize/2.0D - border;
		rightX = centerX + maxSize/2.0D + border;
		topY = centerY - (height-1)/2.0D*10.0D - border;
		botY = centerY + (height-1)/2.0D*10.0D + border;
		this.drawBox(leftX, rightX, topY, botY);
		leftX = centerX - footer/2.0D - border;
		rightX = centerX + footer/2.0D + border;
		topY = centerY + (height-1)/2.0D*10.0D - border + 10.0D;
		botY = centerY + (height-1)/2.0D*10.0D + border + 20.0D;
		this.drawBox(leftX, rightX, topY, botY);

		GL11.glEnable(3553); //GL_TEXTURE_2D
		this.write(head, centerX - title/2, (centerY - (height-1)*10/2) - 19, 0xffffff);

		for(int n=1; n<height; n++)
			this.write(this.sMenu[n], centerX - maxSize/2, ((centerY - (height-1)*10/2) + (n * 10))-9, 0xffffff);
		this.write(hide, centerX - footer/2, ((scHeight+5)/2 + (height-1)*10/2 + 11), 0xffffff);
	}
	
	// new in game style menu from here
	
    /**
     * Gets a key binding. // aka the text on the button?
     */
    public String getKeyText(EnumOptionsMinimap par1EnumOptions)
    {
        StringTranslate stringtranslate = StringTranslate.getInstance();
//      String s = (new StringBuilder()).append(stringtranslate.translateKey(par1EnumOptions.getEnumString())).append(": ").toString(); // use if I ever do translations
        String s = (new StringBuilder()).append(par1EnumOptions.getEnumString()).append(": ").toString();

        if (par1EnumOptions.getEnumFloat())
        {
            float f = getOptionFloatValue(par1EnumOptions);

            if (par1EnumOptions == EnumOptionsMinimap.ZOOM)
            {
                return (new StringBuilder()).append(s).append((int)(f)).toString();
            }

            if (f == 0.0F)
            {
                return (new StringBuilder()).append(s).append(stringtranslate.translateKey("options.off")).toString();
            }
            else
            {
                return (new StringBuilder()).append(s).append((int)(f * 100F)).append("%").toString();
            }
        }

        if (par1EnumOptions.getEnumBoolean())
        {
            boolean flag = getOptionBooleanValue(par1EnumOptions);

            if (flag)
            {
                return (new StringBuilder()).append(s).append(stringtranslate.translateKey("options.on")).toString();
            }
            else
            {
                return (new StringBuilder()).append(s).append(stringtranslate.translateKey("options.off")).toString();
            }
        }
        
        if (par1EnumOptions.getEnumList())
        {
            String state = getOptionListValue(par1EnumOptions);

            return (new StringBuilder()).append(s).append(state).toString();
        }

        else
        {
            return s;
        }
    }
    
    public float getOptionFloatValue(EnumOptionsMinimap par1EnumOptions)
    {
        if (par1EnumOptions == EnumOptionsMinimap.ZOOM)
        {
            return this.lZoom;
        }
        else
        {
            return 0.0F;
        }
    }
    
    public boolean getOptionBooleanValue(EnumOptionsMinimap par1EnumOptions)
    {
        switch (EnumOptionsHelperMinimap.enumOptionsMappingHelperArray[par1EnumOptions.ordinal()])
//        switch (par1EnumOptions.ordinal()) // this is the same.  the above allows for different ordering than how they are declared, but why?  I guess if there's a lot of options churn...
        {
            case 0:
                return this.coords;
                
            case 1:
            	return this.hide;

            case 2:
                return this.showNether;

            case 3:
                return (this.cavesAllowed && this.showCaves);

            case 4:
                return this.lightmap;

            //case 5:
            //    return this.heightmap;

            case 6:
                return this.squareMap;
                
            case 7:
                return this.oldNorth;
                
            //case 8:
            //    return this.showBeacons; 
                
            case 9:
                return this.welcome;
                
            case 10:
                return this.threading;
                
            case 18:
                return this.filtering;
                
            case 19:
                return this.transparency;
                
            case 20:
                return this.biomes;
        }

        return false;
    }
    
    public String getOptionListValue(EnumOptionsMinimap par1EnumOptions)
    {
        if (par1EnumOptions == EnumOptionsMinimap.TERRAIN)
        {
        	if (this.slopemap && this.heightmap) return "Both";
        	else if (this.heightmap) return "Height";
            else if (this.slopemap) return "Slope";
            else return "Off";
        }
        else if (par1EnumOptions == EnumOptionsMinimap.BEACONS)
        {
            if (this.showBeacons && this.showWaypoints) return "Both";
            else if (this.showBeacons) return "Beacons";
            else if (this.showWaypoints) return "Signs"; 
            else return "Off";
        }
        else if (par1EnumOptions == EnumOptionsMinimap.LOCATION) {
        	if (this.mapCorner == 0) return "Top Left";
        	else if (this.mapCorner == 1) return "Top Right";
        	else if (this.mapCorner == 2) return "Bottom Right";
        	else if (this.mapCorner == 3) return "Bottom Left";
        	else return "Error";
        }
        else if (par1EnumOptions == EnumOptionsMinimap.SIZE) {
        	if (sizeModifier == -1) return "Small";
        	else if (sizeModifier == 0) return "Regular";
        	else if (sizeModifier == 1) return "Large";
        	else return "error";
        }
        else
        {
            return "";
        }
    }

	public void setOptionValue(EnumOptionsMinimap par1EnumOptions, int i) {
//        switch (EnumOptionsHelperMinimap.enumOptionsMappingHelperArray[par1EnumOptions.ordinal()])
        switch (par1EnumOptions.ordinal()) // why
        {
            case 0:
                this.coords = !coords;
                break;
                
            case 1:
                this.hide = !hide;
                break;

            case 2:
                this.showNether = !showNether;
                break;

            case 3:
                this.showCaves = !showCaves;
                break;

            case 4:
                this.lightmap = !lightmap;
                break;

            case 5:
            	if (this.slopemap && this.heightmap){
            		this.slopemap = false;
            		this.heightmap = false;
            	}
            	else if (this.slopemap) {
    				this.slopemap = false;
    				this.heightmap = true;
    			}
    			else if (this.heightmap) {
    				this.slopemap = true;
    				this.heightmap = true;
    			}
    			else {
    				this.slopemap = true;
    				this.heightmap = false;
    			}
                break;
                
            case 6:
                this.squareMap = !squareMap;
                break;
                
            case 7:
                this.oldNorth = !oldNorth;
                break;
                
            case 8:
                if (this.showBeacons && this.showWaypoints) {
                	this.showBeacons = false;
                	this.showWaypoints = false;
                }
                else if (this.showBeacons) {
                	this.showBeacons = false;
                	this.showWaypoints = true;
                }
                else if (this.showWaypoints) {
                	this.showWaypoints = true;
                	this.showBeacons = true;
                }
                else {
                	this.showBeacons = true;
                	this.showWaypoints = false;
                }
                break;
                
            case 9:
                this.welcome = !welcome;
                break;
                
            case 10:
                this.threading = !threading;
                break;
                
  /*          case 12:
    			if (motionTrackerExists) {
    				motionTracker.activated = true;
    				this.game.displayGuiScreen((GuiScreen)null);
    			}
            	break;*/
            
            case 17:
            	this.mapCorner = (mapCorner >= 3)?0:mapCorner+1;
            	break;
            	
            case 18:
            	this.filtering = !filtering;
            	break;
            	
            case 19:
            	this.transparency = !transparency;
            	reloadWaterColor(transparency);
            	break;
            	
            case 20:
            	this.biomes = !biomes;
            	reloadBiomeColors(biomes);
            	reloadWaterColor(transparency);
            	break;
            	
            case 21: 
            	this.sizeModifier = (this.sizeModifier >=1)?-1:this.sizeModifier+1;
            	
        }
		this.timer=calcLull+1; // re-render immediately for new options
	}
	
	// controls menu from here
    public String getKeyBindingDescription(int par1)
    {
        StringTranslate var2 = StringTranslate.getInstance();
        return var2.translateKey(this.keyBindings[par1].keyDescription);
    }

    /**
     * The string that appears inside the button/slider in the options menu.
     */
    public String getOptionDisplayString(int par1)
    {
        int var2 = this.keyBindings[par1].keyCode;
        return getKeyDisplayString(var2);
    }

    /**
     * Represents a key or mouse button as a string. Args: key
     */
    public static String getKeyDisplayString(int par0)
    {
        return par0 < 0 ? StatCollector.translateToLocalFormatted("key.mouseButton", new Object[] {Integer.valueOf(par0 + 101)}): Keyboard.getKeyName(par0);
    }

    /**
     * Sets a key binding.
     */
    public void setKeyBinding(int par1, int par2)
    {
        this.keyBindings[par1].keyCode = par2;
        this.saveAll();
    }
    
	
}
