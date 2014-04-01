package net.minecraft.src;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D; //
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream; //
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.src.mamiyaotaru.Contact;
import net.minecraft.src.mamiyaotaru.EnumOptionsHelperMinimap;
import net.minecraft.src.mamiyaotaru.EnumOptionsMinimap;

public class ZanRadar {
	
	private Minecraft game; 
	
	private Tessellator tesselator = Tessellator.instance;
	
	/*Render texture*/
	private RenderEngine renderEngine;
	
	/*Font rendering class*/
	private FontRenderer fontRenderer;
	
	public ZanMinimap minimap = null;
		
	/*Display anything at all, menu, etc..*/
	private boolean enabled = true;
	
	/*Hide just the tracker*/ // if integrated with Zan's, equivalent to disabling mob overlay
	public boolean hide = false;
	
	public boolean showHostiles = true;
	
	public boolean showPlayers = true;
	
	public boolean showNeutrals = false;
	
	public boolean outlines = true;
	
	public boolean filtering = true;
	
	public boolean showHelmets = true;
	
	/*Have we finished loading icons (done in thread)*/
	private boolean completedLoading = false;
		
	/*Holds error exceptions thrown*/
	private String error = "";
	
	/*Moblist update interval*/
	private int timer = 500;
	
	/*Time remaining to show error thrown for*/
	private int ztimer = 0;
	
	/*Direction you're facing*/
	private float direction = 0.0f;
	
	/*Last X coordinate rendered*/
	private double lastX = 0;

	/*Last Z coordinate rendered*/
	private double lastZ = 0;
	
	/*Last Y coordinate rendered*/
	private int lastY = 0;
	
	private int lastZoom;
	
	/*list of moving contacts*/
	private ArrayList<Contact> contacts = new ArrayList(40);
	
	public TreeMap<String, Integer> mpContacts = new TreeMap(String.CASE_INSENSITIVE_ORDER);

	private ITexturePack pack = null;
	
	private final int BLANK = 0; // for spiders being ridden (rendered by the skeleton)
	private final int BLANKHOSTILE = 1; // unknown (mod) mobs.  at least show red / blue / green for hostile / neutral / tame
	private final int BLANKNEUTRAL = 2;
	private final int BLANKTAME = 3;
	private final int BAT = 4;
	private final int BLAZE = 5;
	private final int CATBLACK = 6;
	private final int CATRED = 7;
	private final int CATSIAMESE = 8;
	private final int CAVESPIDER = 9;
	private final int CHICKEN = 10;
	private final int COW = 11;
	private final int CREEPER = 12;
	private final int ENDERDRAGON = 13;
	private final int ENDERMAN = 14;
	private final int GHAST = 15;
	private final int GHASTATTACKING = 16;
	private final int IRONGOLEM = 17;
	private final int MAGMA = 18;
	private final int MOOSHROOM = 19;
	private final int OCELOT = 20;
	private final int PIG = 21;
	private final int PIGZOMBIE = 22;
	private final int PLAYER = 23;
	private final int SHEEP = 24;
	private final int SILVERFISH = 25;
	private final int SKELETON = 26;
	private final int SKELETONWITHER = 27;
	private final int SLIME = 28;
	private final int SNOWGOLEM = 29;
	private final int SPIDER = 30;
	private final int SPIDERJOCKEY = 31;
	private final int SPIDERJOCKEYWITHER = 32;
	private final int SQUID = 33;
	private final int VILLAGER = 34;
	private final int WITCH = 35;
	private final int WITHER = 36;
	private final int WITHERINVULNERABLE = 37;
	private final int WOLF = 38;
	private final int WOLFANGRY = 39;
	private final int WOLFTAME = 40;
	private final int ZOMBIE = 41;
	private final int ZOMBIEVILLAGER = 42;
	
	private BufferedImage[][] icons = new BufferedImage[43][2];
	
	private int[][] imageRef = new int[44][2];
	
	/** hardcoding size here instead of after squarifiying images, in case I ever want to allow higher def images in icons 
	 * 	currently full screen is showing icons at double their resolution.  Can allow higher.  Reading size then would just
	 * display them even bigger, and still double their resolution.  Code here what they should be, then can worry about resolution later
	 */
	// size with smallest power of two that leaves blank area on all sides
	private int[] size = {4, 16, 16, 16, 16, 16, 8, 8, 8, 16, 8, 16, 16, 32, 16, 32, 32, 16, 16, 16, 8, 16, 16, 16, 8, 8, 16, 16, 16, 16, 16, 32, 32, 16, 16, 16, 32, 32, 16, 16, 16, 16, 16};
	// size before squarifying
	private int[] sizeBase = {2, 8, 8, 8, 8, 8, 5, 5, 5, 8, 6, 10, 8, 16, 8, 16, 16, 8, 8, 10, 5, 8, 8, 8, 6, 6, 8, 8, 8, 8, 8, 8, 8, 6, 8, 10, 24, 24, 6, 6, 6, 8, 8};
	// size with smallest power of two that holds the image.  Image can be right up to the edge - possibly less desirable for filtering
	//private int[] size = {2, 8, 8, 8, 16, 8, 8, 8, 8, 8, 8, 16, 8, 32, 8, 16, 16, 16, 8, 16, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 16, 16, 8, 16, 16, 32, 32, 8, 8, 8, 8, 16};//new int[26]; 
	// bat is 8 without ears 16 with.  cow (and mooshroom) is 8 without horns 16 with.  dragon is 16 without crests 32 with
	
	private final int CLOTH = 0;
	//private final int CLOTHOVERLAY = 1;
	//private final int CLOTHOUTER = 2;
	//private final int CLOTHOVERLAYOUTER = 3;
	private final int CHAIN = 4;
	private final int IRON = 6;
	private final int GOLD = 8;
	private final int DIAMOND = 10;
	
	private BufferedImage[][] armorIcons = new BufferedImage[12][2];
	
	private int[][] armorImageRef = new int[12][2];

	public ZanRadar(ZanMinimap minimap) {
		this.minimap = minimap;
		this.renderEngine = minimap.renderEngine;
		for (int t = 0; t < icons.length; t++) {
			imageRef[t][0]=-1;
			imageRef[t][1]=-1;
		}
		for (int t = 0; t < armorIcons.length; t++) {
			armorImageRef[t][0]=-1;
			armorImageRef[t][1]=-1;
		}
		loadDefaultIcons();
	//	loadTexturePackIcons();
	}
	
	private void loadDefaultIcons() {
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
	}
	
	public void loadTexturePackIcons() {
		//System.out.println("loading icons " + pack.func_77531_d() + " * " + pack.func_77536_b() + " * " + pack.func_77537_e() + " * " + pack.func_77538_c());

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

			//System.out.println("WIDTH: " + terrain.getWidth(null));
			icons[BLANK][0] = blankImage("bat", 2, 2);
			icons[BLANKHOSTILE][0] = addCharacter(blankImage("zombie", 8, 8, 255, 0, 0, 255), "?");
			icons[BLANKNEUTRAL][0] = addCharacter(blankImage("cow", 8, 8, 85, 100, 255, 255), "?");
			icons[BLANKTAME][0] = addCharacter(blankImage("wolf", 8, 8, 0, 255, 0, 255), "?");
			//icons[BAT][0] = loadImage("bat", 6, 6, 6, 6, 64, 64); // without initial whitespace
			icons[BAT][0] = addImages(addImages(addImages(blankImage("bat", 8, 10, 64, 64), loadImage("bat", 25, 1, 3, 4), 0, 0, 8, 10), flipHorizontal(loadImage("bat", 25, 1, 3, 4)), 5, 0, 8, 10), loadImage("bat", 6, 6, 6, 6), 1, 2, 8, 10); // ears then head
			icons[BLAZE][0] = loadImage("fire", 8, 8, 8, 8);
			icons[CATBLACK][0] = addImages(addImages(addImages(addImages(blankImage("cat_black", 5, 5), loadImage("cat_black", 5, 5, 5, 4), 0, 1, 5, 5), loadImage("cat_black", 2, 26, 3, 2), 1, 3, 5, 5), loadImage("cat_black", 2, 12, 1, 1), 1, 0, 5, 5), loadImage("cat_black", 8, 12, 1, 1), 3, 0, 5, 5);;
			icons[CATRED][0] = addImages(addImages(addImages(addImages(blankImage("cat_red", 5, 5), loadImage("cat_red", 5, 5, 5, 4), 0, 1, 5, 5), loadImage("cat_red", 2, 26, 3, 2), 1, 3, 5, 5), loadImage("cat_red", 2, 12, 1, 1), 1, 0, 5, 5), loadImage("cat_red", 8, 12, 1, 1), 3, 0, 5, 5);;
			icons[CATSIAMESE][0] = addImages(addImages(addImages(addImages(blankImage("cat_siamese", 5, 5), loadImage("cat_siamese", 5, 5, 5, 4), 0, 1, 5, 5), loadImage("cat_siamese", 2, 26, 3, 2), 1, 3, 5, 5), loadImage("cat_siamese", 2, 12, 1, 1), 1, 0, 5, 5), loadImage("cat_siamese", 8, 12, 1, 1), 3, 0, 5, 5);;
			icons[CAVESPIDER][0] = addImages(addImages(blankImage("cavespider", 8, 8), loadImage("cavespider", 6, 6, 6, 6), 1, 1, 8, 8), loadImage("cavespider", 40, 12, 8, 8), 0, 0, 8, 8); // head first (it's the biggest)((NOPE, blank image first)) then thorax then head. In case head is invisible, this gives us the thorax
			icons[CHICKEN][0] = addImages(addImages(loadImage("chicken", 2, 3, 6, 6), loadImage("chicken", 16, 2, 4, 2), 1, 2, 6, 6), loadImage("chicken", 16, 6, 2, 2), 2, 4, 6, 6);
			//icons[COW][0] = loadImage("cow", 6, 6, 8, 8); // pre horns
			icons[COW][0] = addImages(addImages(addImages(blankImage("cow", 10, 10), loadImage("cow", 6, 6, 8, 8), 1, 1, 10, 10), loadImage("cow", 23, 1, 1, 3), 0, 0, 10, 10), loadImage("cow", 23, 1, 1, 3), 9, 0, 10, 10);
			icons[CREEPER][0] = loadImage("creeper", 8, 8, 8, 8);
			//icons[ENDERDRAGON][0] = addImages(addImages(loadImage("enderdragon/ender", 128, 46, 16, 16, 256, 256), loadImage("enderdragon/ender", 192, 60, 12, 5, 256, 256), 2, 7, 16, 16), loadImage("enderdragon/ender", 192, 81, 12, 4, 256, 256), 2, 12, 16, 16); // with jaws
			//icons[ENDERDRAGON][0] = addImages(addImages(addImages(addImages(loadImage("enderdragon/ender", 128, 46, 16, 16, 256, 256), loadImage("enderdragon/ender", 192, 60, 12, 5, 256, 256), 2, 7, 16, 16), loadImage("enderdragon/ender", 192, 81, 12, 4, 256, 256), 2, 12, 16, 16), loadImage("enderdragon/ender", 116, 4, 2, 2, 256, 256), 3, 5, 16, 16), flipHorizontal(loadImage("enderdragon/ender", 116, 4, 2, 2, 256, 256)), 11, 5, 16, 16); // nostrils (block eyes, not super visible)
			icons[ENDERDRAGON][0] = addImages(addImages(addImages(addImages(addImages(blankImage("enderdragon/ender", 16, 20, 256, 256), loadImage("enderdragon/ender", 128, 46, 16, 16, 256, 256), 0, 4, 16, 16), loadImage("enderdragon/ender", 192, 60, 12, 5, 256, 256), 2, 11, 16, 16), loadImage("enderdragon/ender", 192, 81, 12, 4, 256, 256), 2, 16, 16, 16), loadImage("enderdragon/ender", 6, 6, 2, 4, 256, 256), 3, 0, 16, 16), flipHorizontal(loadImage("enderdragon/ender", 6, 6, 2, 4, 256, 256)), 11, 0, 16, 16); // with head crests no nostrils.  base, head, upper jaw, lower jaw, left crest, right crest
			icons[ENDERMAN][0] = addImages(addImages(addImages(loadImage("enderman", 8, 8, 8, 8), loadImage("enderman", 8, 24, 8, 8), 0, 0, 8, 8), loadImage("enderman", 8, 8, 8, 8), 0, 0, 8, 8), loadImage("enderman_eyes", 8, 12, 8, 1), 0, 4, 8, 8); // head twice, once to establish size, then again because it goes over the jaw
			icons[GHAST][0] = loadImage("ghast", 16, 16, 16, 16);
			icons[GHASTATTACKING][0] = loadImage("ghast_fire", 16, 16, 16, 16);
			//icons[IRONGOLEM][0] = loadImage("villager_golem", 8, 8, 8, 10, 128, 128); // pre blank image for protruding nose
			icons[IRONGOLEM][0] = addImages(addImages(blankImage("villager_golem", 8, 12, 128, 128), loadImage("villager_golem", 8, 8, 8, 10, 128, 128), 0, 1, 8, 12), loadImage("villager_golem", 26, 2, 2, 4, 128, 128), 3, 8, 8, 12); // pre blank image for protruding nose
			icons[MAGMA][0] = addImages(addImages(loadImage("lava", 8, 8, 8, 8), loadImage("lava", 32, 18, 8, 1), 0, 3, 8, 8), loadImage("lava", 32, 27, 8, 1), 0, 4, 8, 8);
			icons[MOOSHROOM][0] = addImages(addImages(addImages(blankImage("redcow", 10, 10), loadImage("redcow", 6, 6, 8, 8), 1, 1, 10, 10), loadImage("redcow", 23, 1, 1, 3), 0, 0, 10, 10), loadImage("redcow", 23, 1, 1, 3), 9, 0, 10, 10);
			//icons[OCELOT][0] = addImages(loadImage("ozelot", 5, 5, 5, 5), loadImage("ozelot", 2, 26, 3, 2), 1, 2, 5, 5); // pre ears
			icons[OCELOT][0] = addImages(addImages(addImages(addImages(blankImage("ozelot", 5, 5), loadImage("ozelot", 5, 5, 5, 4), 0, 1, 5, 5), loadImage("ozelot", 2, 26, 3, 2), 1, 3, 5, 5), loadImage("ozelot", 2, 12, 1, 1), 1, 0, 5, 5), loadImage("ozelot", 8, 12, 1, 1), 3, 0, 5, 5);;
			icons[PIG][0] = addImages(loadImage("pig", 8, 8, 8, 8), loadImage("pig", 16, 17, 6, 3), 1, 4, 8, 8);
			icons[PIGZOMBIE][0] = addImages(loadImage("pigzombie", 8, 8, 8, 8), loadImage("pigzombie", 40, 8, 8, 8), 0, 0, 8, 8);
			icons[PLAYER][0] = addImages(loadImage("char", 8, 8, 8, 8), loadImage("char", 40, 8, 8, 8), 0, 0, 8, 8);
			icons[SHEEP][0] = loadImage("sheep", 8, 8, 6, 6);
			icons[SILVERFISH][0] = addImages(loadImage("silverfish", 22, 20, 6, 6), loadImage("silverfish", 2, 2, 3, 2), 2, 2, 6, 6);
			icons[SKELETON][0] = addImages(loadImage("skeleton", 8, 8, 8, 8), loadImage("skeleton", 40, 8, 8, 8), 0, 0, 8, 8);
			icons[SKELETONWITHER][0] = addImages(loadImage("skeleton_wither", 8, 8, 8, 8), loadImage("skeleton_wither", 40, 8, 8, 8), 0, 0, 8, 8);
			//icons[SLIME][0] = addImages(addImages(addImages(loadImage("slime", 8, 8, 8, 8), loadImage("slime", 6, 22, 6 ,6), 1, 1, 8, 8), loadImage("slime", 34, 6, 2, 2), 1, 2, 8, 8), loadImage("slime", 34, 2, 2, 2), 5, 2, 8, 8); // old one pre grab whitespace (and inside on top of outside)
			icons[SLIME][0] = addImages(addImages(addImages(addImages(addImages(blankImage("slime", 8, 8), loadImage("slime", 6, 22, 6 ,6), 1, 1, 8, 8), loadImage("slime", 34, 6, 2, 2), 5, 2, 8, 8), loadImage("slime", 34, 2, 2, 2), 1, 2, 8, 8), loadImage("slime", 33, 9, 1, 1), 4, 5, 8, 8), loadImage("slime", 8, 8, 8, 8), 0, 0, 8, 8); // blank template, inner, left eye, right eye, mouth, outer see through body
			icons[SNOWGOLEM][0] = loadImage("snowman", 8, 8, 8, 8, 64, 64);
			icons[SPIDER][0] = addImages(addImages(blankImage("spider", 8, 8), loadImage("spider", 6, 6, 6, 6), 1, 1, 8, 8), loadImage("spider", 40, 12, 8, 8), 0, 0, 8, 8);
			icons[SPIDERJOCKEY][0] = addImages(addImages(blankImage("skeleton", 8, 16), icons[SKELETON][0], 0, 0, 8, 16), icons[SPIDER][0], 0, 8, 8, 16);
			icons[SPIDERJOCKEYWITHER][0] = addImages(addImages(blankImage("skeleton_wither", 8, 16), icons[SKELETONWITHER][0], 0, 0, 8, 16), icons[SPIDER][0], 0, 8, 8, 16);
			icons[SQUID][0] = scaleImage(loadImage("squid", 12, 12, 12, 16), 0.5f); // squid is too big for what it is
			//icons[VILLAGER][0] = addImages(loadImage("villager/farmer", 8, 8, 8, 10, 64, 64), loadImage("villager/farmer", 26, 2, 2, 3, 64, 64), 3, 7, 8, 10); // pre blank image for protruding nose
			icons[VILLAGER][0] = addImages(addImages(blankImage("villager/farmer", 8, 12), loadImage("villager/farmer", 8, 8, 8, 10, 64, 64), 0, 1, 8, 12), loadImage("villager/farmer", 26, 2, 2, 4, 64, 64), 3, 8, 8, 12);
			//icons[WITCH][0] = addImages(addImages(loadImage("villager/witch", 8, 8, 8, 10, 64, 128), loadImage("villager/witch", 26, 2, 2, 3, 64, 128), 3, 7, 8, 10), loadImage("villager/witch", 11, 75, 8, 2, 64, 128), 0, 0, 8, 10); // old one pre grabbing whitespace first
			icons[WITCH][0] = addImages(addImages(addImages(addImages(addImages(blankImage("villager/witch", 10, 16, 64, 128), loadImage("villager/witch", 8, 8, 8, 10, 64, 128), 1, 5, 10, 16), loadImage("villager/witch", 26, 2, 2, 4, 64, 128), 4, 12, 10, 16), loadImage("villager/witch", 10, 74, 10, 3, 64, 128), 0, 4, 10, 16), loadImage("villager/witch", 7, 83, 7, 4, 64, 128), 1.5f, 0, 10, 16), loadImage("villager/witch", 1, 1, 1, 1, 64, 128), 5, 14, 10, 16); // base face nose hatbrim hatnext
			icons[WITHER][0] = addImages(addImages(addImages(blankImage("wither", 24, 10, 64, 64), loadImage("wither", 8, 8, 8, 8, 64, 64), 8, 0, 24, 10), loadImage("wither", 38, 6, 6, 6, 64, 64), 0, 2, 24, 10), loadImage("wither", 38, 6, 6, 6, 64, 64), 18, 2, 24, 10);
			icons[WITHERINVULNERABLE][0] = addImages(addImages(addImages(blankImage("wither_invul", 24, 10, 64, 64), loadImage("wither_invul", 8, 8, 8, 8, 64, 64), 8, 0, 24, 10), loadImage("wither_invul", 38, 6, 6, 6, 64, 64), 0, 2, 24, 10), loadImage("wither_invul", 38, 6, 6, 6, 64, 64), 18, 2, 24, 10);
			//icons[WOLF][0] = addImages(loadImage("wolf", 4, 4, 6, 6), loadImage("wolf", 4, 14, 3, 3), 1.5f, 3, 6, 6); // square before blankimage
			icons[WOLF][0] = addImages(addImages(addImages(addImages(blankImage("wolf", 6, 8), loadImage("wolf", 4, 4, 6, 6), 0, 2, 6, 8), loadImage("wolf", 4, 14, 3, 3), 1.5f, 5, 6, 8), loadImage("wolf", 17, 15, 2, 2), 0, 0, 6, 8), loadImage("wolf", 17, 15, 2, 2), 4, 0, 6, 8);
			icons[WOLFANGRY][0] = addImages(addImages(addImages(addImages(blankImage("wolf_angry", 6, 8), loadImage("wolf_angry", 4, 4, 6, 6), 0, 2, 6, 8), loadImage("wolf_angry", 4, 14, 3, 3), 1.5f, 5, 6, 8), loadImage("wolf_angry", 17, 15, 2, 2), 0, 0, 6, 8), loadImage("wolf_angry", 17, 15, 2, 2), 4, 0, 6, 8);
			icons[WOLFTAME][0] = addImages(addImages(addImages(addImages(blankImage("wolf_tame", 6, 8), loadImage("wolf_tame", 4, 4, 6, 6), 0, 2, 6, 8), loadImage("wolf_tame", 4, 14, 3, 3), 1.5f, 5, 6, 8), loadImage("wolf_tame", 17, 15, 2, 2), 0, 0, 6, 8), loadImage("wolf_tame", 17, 15, 2, 2), 4, 0, 6, 8);
			icons[ZOMBIE][0] = addImages(loadImage("zombie", 8, 8, 8, 8, 64, 64), loadImage("zombie", 40, 8, 8, 8, 64, 64), 0, 0, 8, 8); // height changed.  doesn't actually matter, scale is done by checking image width.  Just for consistency though
			//icons[ZOMBIEVILLAGER][0] = addImages(loadImage("zombie_villager", 8, 40, 8, 10, 64, 64), loadImage("zombie_villager", 26, 34, 2, 3, 64, 64), 3, 7, 8, 10); // pre blank image for protruding nose
			icons[ZOMBIEVILLAGER][0] = addImages(addImages(blankImage("zombie_villager", 8, 12, 64, 64), loadImage("zombie_villager", 8, 40, 8, 10, 64, 64), 0, 1, 8, 12), loadImage("zombie_villager", 26, 34, 2, 4, 64, 64), 3, 8, 8, 12); 
			
			int oldGenericPlayerRef = imageRef[PLAYER][1];
			for (int t = 0; t < icons.length; t++) {
				if (imageRef[t][0] != -1)
					glah(imageRef[t][0]);
				if (imageRef[t][1] != -1)
					glah(imageRef[t][1]);
				//icons[t]=into128(icons[t]);
				float scale = (float)(icons[t][0].getWidth())/(float)sizeBase[t];
				icons[t][1] = fillOutline(intoSquare(scaleImage(icons[t][0], 2f/scale)));
				icons[t][0] = fillOutline(intoSquare(scaleImage(icons[t][0], 1f/scale)));
				if (renderEngine!=null) { 
					imageRef[t][0]=this.tex(icons[t][0]);
					imageRef[t][1]=this.tex(icons[t][1]);
				}
				else {
					imageRef[t][0]=-1;
					imageRef[t][1]=-1;
				}
			}
			int newGenericPlayerRef = imageRef[PLAYER][1];
			replaceGenericPlayerRefs(oldGenericPlayerRef, newGenericPlayerRef);
			
			armorIcons[CLOTH][0] = loadImage("/armor/", "cloth_1", 8, 8, 8, 8);
			armorIcons[CLOTH+1][0] = loadImage("/armor/", "cloth_1", 40, 8, 8, 8);
			armorIcons[CLOTH+2][0] = loadImage("/armor/", "cloth_1_b", 8, 8, 8, 8);
			armorIcons[CLOTH+3][0] = loadImage("/armor/", "cloth_1_b", 40, 8, 8, 8);
			
			/*
			armorIcons[CHAIN][0] = loadImage("/armor/", "chain_1", 8, 8, 8, 8);
			armorIcons[CHAIN+1][0] = loadImage("/armor/", "chain_1", 40, 8, 8, 8);
			armorIcons[IRON][0] = loadImage("/armor/", "iron_1", 8, 8, 8, 8);
			armorIcons[IRON+1][0] = loadImage("/armor/", "iron_1", 40, 8, 8, 8);
			armorIcons[GOLD][0] = loadImage("/armor/", "gold_1", 8, 8, 8, 8);
			armorIcons[GOLD+1][0] = loadImage("/armor/", "gold_1", 40, 8, 8, 8);
			armorIcons[DIAMOND][0] = loadImage("/armor/", "diamond_1", 8, 8, 8, 8);
			armorIcons[DIAMOND+1][0] = loadImage("/armor/", "diamond_1", 40, 8, 8, 8);
			*/
			
			armorIcons[CHAIN][0] = addImages(loadImage("/armor/", "chain_1", 8, 8, 8, 8), loadImage("/armor/", "chain_1", 40, 8, 8, 8), 0, 0, 8, 8);;
			armorIcons[IRON][0] = addImages(loadImage("/armor/", "iron_1", 8, 8, 8, 8), loadImage("/armor/", "iron_1", 40, 8, 8, 8), 0, 0, 8, 8);;
			armorIcons[GOLD][0] = addImages(loadImage("/armor/", "gold_1", 8, 8, 8, 8), loadImage("/armor/", "gold_1", 40, 8, 8, 8), 0, 0, 8, 8);;
			armorIcons[DIAMOND][0] = addImages(loadImage("/armor/", "diamond_1", 8, 8, 8, 8), loadImage("/armor/", "diamond_1", 40, 8, 8, 8), 0, 0, 8, 8);;
			armorIcons[CHAIN+1][0] = icons[BLANK][0];
			armorIcons[IRON+1][0] = icons[BLANK][0];
			armorIcons[GOLD+1][0] = icons[BLANK][0];
			armorIcons[DIAMOND+1][0] = icons[BLANK][0];
			
			for (int t = 0; t < armorIcons.length; t++) {
				if (armorImageRef[t][0] != -1)
					glah(armorImageRef[t][0]);
				if (armorImageRef[t][1] != -1)
					glah(armorImageRef[t][1]);
				//icons[t]=into128(icons[t]);
				float scale = (float)(armorIcons[t][0].getWidth())/8f;
				armorIcons[t][1] = fillOutline(intoSquare(scaleImage(armorIcons[t][0], 2f/scale)), true, t);
				armorIcons[t][0] = fillOutline(intoSquare(scaleImage(armorIcons[t][0], 1f/scale)), true, t);
				if (renderEngine!=null) { 
					armorImageRef[t][0]=this.tex(armorIcons[t][0]);
					armorImageRef[t][1]=this.tex(armorIcons[t][1]);
				}
				else {
					armorImageRef[t][0]=-1;
					armorImageRef[t][1]=-1;
				}
			}
			
			completedLoading = true;

		}
		catch (Exception e) {
			System.out.println("Failed getting mobs " + e.getLocalizedMessage());
		}
		
	}
	
	private BufferedImage blankImage(String path, int w, int h) {
		return blankImage(path, w, h, 64, 32);
	}
	
	private BufferedImage blankImage(String path, int w, int h, int imageWidth, int imageHeight) {
		return blankImage(path, w, h, imageWidth, imageHeight, 0, 0, 0, 0);
	}
	
	private BufferedImage blankImage(String path, int w, int h, int r, int g, int b, int a) {
		return blankImage(path, w, h, 64, 32, r, g, b, a);
	}
	
	private BufferedImage blankImage(String path, int w, int h, int imageWidth, int imageHeight, int r, int g, int b, int a) {
		try {
			String fullPath = "/mob/" + path + ".png";
			InputStream is = pack.getResourceAsStream(fullPath);
			BufferedImage mobSkin = ImageIO.read(is);
			is.close();
			BufferedImage temp = new BufferedImage(w * mobSkin.getWidth() / imageWidth, h * mobSkin.getWidth() / imageWidth, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g2 = temp.createGraphics();
			g2.setColor(new Color(r, g, b, a));
			g2.fillRect(0, 0, temp.getWidth(), temp.getHeight());
			g2.dispose();
			return temp;
		}
		catch (Exception e) {
			System.out.println("Failed getting mob: " + path + " - " + e.getLocalizedMessage());
			return null;
		}
	}
	
	private BufferedImage addCharacter(BufferedImage image, String character) {
		Graphics2D g2 = image.createGraphics();
		g2.setColor(new Color(0, 0, 0, 255));
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, image.getHeight()));
	    java.awt.FontMetrics fm = g2.getFontMetrics();
	    int x = (image.getWidth() - fm.stringWidth("?")) / 2;
	    int y = (fm.getAscent() + (image.getHeight() - (fm.getAscent() + fm.getDescent())) / 2);
	    g2.drawString("?", x, y);
		g2.dispose();
		return image;
	}
	
	
	// load image with the default path (mob)
	private BufferedImage loadImage(String name, int x, int y, int w, int h, int imageWidth, int imageHeight) {
		return loadImage("/mob/", name, x, y, w, h, imageWidth, imageHeight);
	}
	
	// load image with the default dimensions (most common anyway, 64x32 in default)
	private BufferedImage loadImage(String path, String name, int x, int y, int w, int h) {
		return loadImage(path, name, x, y, w, h, 64, 32);
	}
	
	// load image with the default path (mob) and dimensions (most common anyway, 64x32 in default)
	private BufferedImage loadImage(String name, int x, int y, int w, int h) {
		return loadImage("/mob/", name, x, y, w, h, 64, 32);
	}
	
	private BufferedImage loadImage(String path, String name, int x, int y, int w, int h, int imageWidth, int imageHeight) {
		try {
			String fullPath = path + name + ".png";
			InputStream is = pack.getResourceAsStream(fullPath);
			BufferedImage mobSkin = ImageIO.read(is);
			is.close();
			
			// System.out.println(path + " is type " + mobSkin.getType());
			// convert to TYPE_4BYTE_ABGR if it isn't.  TYPE_BYTE_INDEXED break all over
			// actually do below in case we get passed something in a bad format
			return loadImage(mobSkin, x, y, w, h, imageWidth, imageHeight);

		}
		catch (Exception e) {
			System.out.println("Failed getting mob: " + name + " - " + e.getLocalizedMessage());
			return null;
		}
		
	}
	
	// load image with the default dimensions (most common anyway, 64x32 in default)
	private BufferedImage loadImage(BufferedImage mobSkin, int x, int y, int w, int h) {
		return loadImage(mobSkin, x, y, w, h, 64, 32);
	}
	
	// load bits from an already loaded bufferedImage (like the image RenderEngine returns for a multiplayer skin)
	private BufferedImage loadImage(BufferedImage mobSkin, int x, int y, int w, int h, int imageWidth, int imageHeight) {
		// make sure format is OK
		if (!(mobSkin.getType() == BufferedImage.TYPE_4BYTE_ABGR)) {
			BufferedImage temp = new BufferedImage(mobSkin.getWidth(), mobSkin.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g2 = temp.createGraphics();
			g2.drawImage(mobSkin, 0, 0, mobSkin.getWidth(), mobSkin.getHeight(), null);
			g2.dispose();
			mobSkin = temp;
		}
		
		float scale = mobSkin.getWidth(null) / imageWidth; // is float for dealing with lower res texture packs
		BufferedImage base = mobSkin.getSubimage((int)(x*scale), (int)(y*scale), (int)(w*scale), (int)(h*scale));
	//	if (scale != 1)  // scale down hidef (or up lodef?  haha) // scale at the end, after it's all added together
	//		base = scaleImage (base, 1/scale);
		return base;
	}
	
	private BufferedImage addImages(BufferedImage base, BufferedImage overlay, float x, int y, int baseWidth, int baseHeight) {
		int scale = base.getWidth()/baseWidth;
		Graphics gfx = base.getGraphics();
		gfx.drawImage(overlay, (int)(x*scale), y*scale, null); // float for x here simply allows us to center the wolf nose in double and higher resolution packs (witch hat too)
		gfx.dispose();
		return base;
	}
	
	private BufferedImage scaleImage(BufferedImage image, float scaleBy) {
		BufferedImage tmp = new BufferedImage((int)(image.getWidth()*scaleBy), (int)(image.getHeight()*scaleBy), image.getType());
		Graphics2D g2 = tmp.createGraphics();
		//g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2.drawImage(image, 0, 0, (int)(image.getWidth()*scaleBy), (int)(image.getHeight()*scaleBy), null);
		g2.dispose();
		image = tmp;
		return image;
	}
	
	private BufferedImage flipHorizontal(BufferedImage image) {
		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-image.getWidth(null), 0);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		return op.filter(image, null);
	}
	
	private BufferedImage into128(BufferedImage base) {
		BufferedImage frame = new BufferedImage(128, 128, base.getType());
		Graphics gfx = frame.getGraphics();
		gfx.drawImage(base, 64-base.getWidth()/2, 64-base.getHeight()/2, base.getWidth(), base.getHeight(), null);
		gfx.dispose();
		return frame;
	}
	
	private BufferedImage intoSquare(BufferedImage base) {
		int dim = Math.max(base.getWidth(), base.getHeight());
		int t = 0;
		while (Math.pow(2, t) <= dim)
			t++;
		int size = (int)Math.pow(2, t);
		
		BufferedImage frame = new BufferedImage(size, size, base.getType());
		Graphics gfx = frame.getGraphics();
		gfx.drawImage(base, (size-base.getWidth())/2, (size-base.getHeight())/2, base.getWidth(), base.getHeight(), null);
		gfx.dispose();
		return frame;
	}
	
	private BufferedImage fillOutline(BufferedImage image) {
		return fillOutline(image, false, 0);
	}
	
	private BufferedImage fillOutline(BufferedImage image, boolean armor, int entry) {
		if (this.outlines && entry != CLOTH+2 && entry != CLOTH+3)
			image = fillOutline(image, true, armor);
		image = fillOutline(image, false, armor);
		return image;
	}
	
	private BufferedImage fillOutline(BufferedImage image, boolean solid, boolean armor) {
		BufferedImage temp = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		java.awt.Graphics gfx = temp.getGraphics();
		gfx.drawImage(image, 0, 0, null);
		gfx.dispose();
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		for (int t = 0; t < image.getWidth(); t++) {
			for (int s = 0; s < image.getHeight(); s++) {
				int color = image.getRGB(s, t);
				if ((color >> 24 & 255) == 0) { // clear pixel
					int newColor = getNonTransparentPixel(s, t, image);
					if (newColor != -420) {
						if (solid) {
							if (!armor || t <= imageWidth/4 || t >= imageWidth-1-imageWidth/4 || s <= imageHeight/4 || s >= imageHeight-1-imageHeight/4)
								newColor = 255 << 24;
							else
								newColor = 0 << 24;
						}
						else {
							int alpha = (newColor >> 24 & 255);
							int red = (newColor >> 16 & 255);
							int green = (newColor >> 8 & 255);
							int blue = (newColor >> 0 & 255);
							newColor = (0) << 24 | (red & 255) << 16 | (green & 255) << 8 | blue & 255;
						}
						temp.setRGB(s, t, newColor);
					}
				}
			}
		}
		return temp;
	}

	
	
	/*// optional version to get more than one pixel out.  not needed if we run it after scaling
	private BufferedImage fillOutline(BufferedImage image) {
		BufferedImage orig = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics gfx = orig.getGraphics();
		gfx.drawImage(image, 0, 0, null);
		gfx.dispose();
		for (int iter = 0; iter < 6; iter++) {
			BufferedImage temp = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
			gfx = temp.getGraphics();
			gfx.drawImage(image, 0, 0, null);
			gfx.dispose();
			for (int t = 0; t < image.getWidth(); t++) {
				for (int s = 0; s < image.getHeight(); s++) {
					int color = image.getRGB(s, t);
					if ((color >> 24 & 255) == 0) { // clear pixel
						int newColor = getNonTransparentPixel(s, t, temp);
						if (newColor != -1) {
						//	int alpha = (newColor >> 24 & 255);
						//	int red = (newColor >> 16 & 255);
						//	int green = (newColor >> 8 & 255);
						//	int blue = (newColor >> 0 & 255);
						//	newColor = (255) << 24 | (red & 255) << 16 | (green & 255) << 8 | blue & 255;
							image.setRGB(s, t, newColor);
						}
					}
				}
			}
		}
		
		for (int t = 0; t < image.getWidth(); t++) {
			for (int s = 0; s < image.getHeight(); s++) {
				int color = image.getRGB(s, t);
				int origColor = orig.getRGB(s, t);
				int alpha = (origColor >> 24 & 255);
				int red = (color >> 16 & 255);
				int green = (color >> 8 & 255);
				int blue = (color >> 0 & 255);
				color = (alpha & 255) << 24 | (red & 255) << 16 | (green & 255) << 8 | blue & 255;
				image.setRGB(s, t, color);
			}
		}

		return image;
	}*/
	
	private int getNonTransparentPixel(int x, int y, BufferedImage image) {
		int color;
		if (x > 0) {
			color = image.getRGB(x-1, y);
			if ((color >> 24 & 255) > 50)
				return color;
		}
		if (x < image.getWidth()-1) {
			color = image.getRGB(x+1, y);
			if ((color >> 24 & 255) > 50)
				return color;
		}
		if (y > 0) {
			color = image.getRGB(x, y-1);
			if ((color >> 24 & 255) > 50)
				return color;
		}
		if (y < image.getHeight()-1) {
			color = image.getRGB(x, y+1);
			if ((color >> 24 & 255) > 50)
				return color;
		}
		
		if (x > 0 && y > 0) {
			color = image.getRGB(x-1, y-1);
			if ((color >> 24 & 255) > 50)
				return color;
		}
		if (x > 0 && y < image.getHeight()-1) {
			color = image.getRGB(x-1, y+1);
			if ((color >> 24 & 255) > 50)
				return color;
		}
		if (x < image.getWidth()-1 && y > 0) {
			color = image.getRGB(x+1, y-1);
			if ((color >> 24 & 255) > 50)
				return color;
		}
		if (x < image.getWidth()-1 && y < image.getHeight()-1) {
			color = image.getRGB(x+1, y+1);
			if ((color >> 24 & 255) > 50)
				return color;
		}
		return -420;
	}
	
	private void replaceGenericPlayerRefs(int oldRef, int newRef) {
		for(Map.Entry<String,Integer> entry : mpContacts.entrySet()) {
			if (entry.getValue().equals(oldRef))
				entry.setValue(newRef);
		}
	}
	
	public void setTexturePack(ITexturePack pack) {
		this.pack = pack;
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
	
	public void ldrawthree(double a, double b, double c, double d, double e)
	{
		tesselator.addVertexWithUV(a, b, c, d, e);
	}
	
	private void setMap(int x, int y) {
	//	ldrawthree(paramInt1 - 64.0D, 64.0D + 5.0D, 1.0D, 0.0D, 1.0D);
	//	ldrawthree(paramInt1, 64.0D + 5.0D, 1.0D, 1.0D, 1.0D);
	//	ldrawthree(paramInt1, 5.0D, 1.0D, 1.0D, 0.0D);
	//	ldrawthree(paramInt1 - 64.0D, 5.0D, 1.0D, 0.0D, 0.0D);
		setMap(x, y, 128); // do this with default image size of 128 (that everything was before I decided to stop padding 16px and 8px images out to 128)
	}

	private void setMap(int x, int y, int imageSize) {
		float scale = imageSize/4f; // 128 image is drawn from center - 32 to center + 32, as in the old setMap
		// 16 image is drawn from center - 4 to center + 4, quarter the size
		ldrawthree(minimap.scScale*(x-scale), minimap.scScale*(y+scale), 1.0D, 0.0D, 1.0D);
		ldrawthree(minimap.scScale*(x+scale), minimap.scScale*(y+scale), 1.0D, 1.0D, 1.0D);
		ldrawthree(minimap.scScale*(x+scale), minimap.scScale*(y-scale), 1.0D, 1.0D, 0.0D);
		ldrawthree(minimap.scScale*(x-scale), minimap.scScale*(y-scale), 1.0D, 0.0D, 0.0D);
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
	
	public void OnTickInGame(Minecraft mc) {
		if(game==null) game = mc;
		if(fontRenderer==null) fontRenderer = this.game.fontRenderer;
		if(renderEngine==null && completedLoading) {
			renderEngine = this.game.renderEngine;
			if (renderEngine!=null) { // once we get a render engine (and only once) allocate the images
				for (int t = 0; t < icons.length; t++) {
					imageRef[t][0]=this.tex(icons[t][0]);
					imageRef[t][1]=this.tex(icons[t][1]);
				}
				for (int t = 0; t < armorIcons.length; t++) {
					armorImageRef[t][0]=this.tex(armorIcons[t][0]);
					armorImageRef[t][1]=this.tex(armorIcons[t][1]);
				}
			}
		}

		if ((this.game.currentScreen instanceof GuiIngameMenu) || (Keyboard.isKeyDown(61)) /*|| (this.game.thePlayer.dimension==-1)*/)
			this.enabled=false;
		else this.enabled=true;
		
		//ScaledResolution scSize = new ScaledResolution(game.gameSettings, game.displayWidth, game.displayHeight);
		//int scWidth = scSize.getScaledWidth();
		//int scHeight = scSize.getScaledHeight();
		//int guiScale = scSize.getScaleFactor();
		
		int guiScale = 1;
        while (game.displayWidth / (guiScale + 1) >= 320 && game.displayHeight / (guiScale + 1) >= 240)
        {
            ++guiScale;
        }
       /*// needed if this is standalone 
        double scaledWidthD = (double)game.displayWidth / (double)guiScale;
        double scaledHeightD = (double)game.displayHeight / (double)guiScale;
        int scWidth = MathHelper.ceiling_double_int(scaledWidthD);
        int scHeight = MathHelper.ceiling_double_int(scaledHeightD);
        int xMap = 0;
        int yMap = 0;
		if (this.minimap.mapCorner == 0 || this.minimap.mapCorner == 3)
			xMap = 37;
		else
			xMap = scWidth - 37;
		if (this.minimap.mapCorner == 0 || this.minimap.mapCorner == 1) {
			yMap = 37;
		}
		else {
			yMap = scHeight - 37;
		} */
		
		guiScale = (guiScale>=4)?1:0;

		this.direction = this.game.thePlayer.rotationYaw + 180;

		if (this.direction >= 360.0f)
			while (this.direction >= 360.0f)
				this.direction -= 360.0f;

		if (this.direction < 0.0f) {
			while (this.direction < 0.0f)
				this.direction += 360.0f;
		}
		
		if ((!this.error.equals("")) && (this.ztimer == 0)) this.ztimer = 500;

		if (this.ztimer > 0) this.ztimer -= 1;

		if ((this.ztimer == 0) && (!this.error.equals(""))) this.error = "";
		
		if (this.enabled && !this.hide) {
			
			// don't recalculate mobs all the time.  doesn't seem to affect FPS, whatev
			if (this.timer>95) { // not multiple of 100 so doesn't happen at same time as map render
				calculateMobs();
				timer = 0;
			}
			timer++;

			if (completedLoading) {
		        double scaledWidthD = (double)game.displayWidth;// / (double)minimap.scScale;
		        double scaledHeightD = (double)game.displayHeight;// / (double)minimap.scScale;
		        GL11.glMatrixMode(GL11.GL_PROJECTION);
		        GL11.glPushMatrix();
		        GL11.glLoadIdentity();
		        GL11.glOrtho(0.0D, scaledWidthD, scaledHeightD, 0.0D, 1000.0D, 3000.0D);
		        GL11.glMatrixMode(GL11.GL_MODELVIEW);
		        GL11.glLoadIdentity();
		        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glDepthMask(false);
				renderMapMobs(this.minimap.mapX, this.minimap.mapY, guiScale);	
		        GL11.glMatrixMode(GL11.GL_PROJECTION);
		        GL11.glPopMatrix();
			}
			
			if (ztimer > 0)
				this.write(this.error, 20, 20, 0xffffff);
			
		//	if (ztimer > 0)
		//		this.write(this.error, 20, 20, 0xffffff);

		//	if (this.iMenu>0) showMenu(scWidth, scHeight);

			/*GL11.glDepthMask(true);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_DEPTH_TEST);*/ 
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		}
	} // end method OnTickInGame
	
	private void write(String paramStr, int paramInt1, int paramInt2, int paramInt3) {
		this.fontRenderer.drawStringWithShadow(paramStr, paramInt1, paramInt2, paramInt3); 
	}
	
	private int xCoord() {
		return (int)(this.game.thePlayer.posX < 0.0D ? this.game.thePlayer.posX - 1 : this.game.thePlayer.posX);
	}

	private int zCoord() {
		return (int)(this.game.thePlayer.posZ < 0.0D ? this.game.thePlayer.posZ - 1 : this.game.thePlayer.posZ);
	}

	private int yCoord() {
		return (int)this.game.thePlayer.posY - 1; // player is one higher than other entities, even other players (WTF)
	}
	
	public double xCoordDouble() {
		return (this.game.thePlayer.posX < 0.0D ? this.game.thePlayer.posX - 1 : this.game.thePlayer.posX);
	}

	public double zCoordDouble() {
		return (this.game.thePlayer.posZ < 0.0D ? this.game.thePlayer.posZ - 1 : this.game.thePlayer.posZ);
	}

	
	public void calculateMobs () {
		contacts.clear();
		double max = (Math.pow(2,minimap.lZoom) * 16 - 0);
		java.util.List entities = this.game.theWorld.getLoadedEntityList();
		for(int j = 0; j < entities.size(); j++) {
			Entity entity = (Entity)entities.get(j);
			try {
				if(  (showHostiles && isHostile(entity)) || (showPlayers && isPlayer(entity)) || (showNeutrals && isNeutral(entity)) ) {
					int wayX = this.xCoord() - (int)(entity.posX);
					int wayZ = this.zCoord() - (int)(entity.posZ);
					int wayY = this.yCoord() - (int)(entity.posY);
					// sqrt version
					//double hypot = Math.sqrt((wayX*wayX)+(wayZ*wayZ)+(wayY*wayY));
					//hypot = hypot/(Math.pow(2,minimap.lZoom)/2);
					//if (hypot < 31.0D) {
					
					// no sqrt version
					double hypot = ((wayX*wayX)+(wayZ*wayZ)+(wayY*wayY));
					hypot = hypot/((Math.pow(2,minimap.lZoom)/2)*(Math.pow(2,minimap.lZoom)/2));
					if (hypot < 961.0D /*31.0D squared - saves on sqrt ops*/) {
						//System.out.println("player: " + (int)this.game.thePlayer.posY + " mob: " + (int)(entity.posY));
						
						Contact contact;
						if (isPlayer(entity)) 
							contact = handleMPplayer(entity);
						else
							contact = new Contact(entity, /*(int)*/(entity.posX), /*(int)*/(entity.posZ), (int)(entity.posY), getContactType(entity));
						contact.angle = (float)Math.toDegrees(Math.atan2(wayX, wayZ));
						// pow2,0 is 1.  /2 is 1/2.  so if hypot max 16/.5 < 31.  pow2,1 is 2. /2 is 1.  hypot max 32/1 < 31.  pow2,2 is 4. /2 is 2.  hypot max 64/2 < 31  
						contact.distance = Math.sqrt((wayX*wayX)+(wayZ*wayZ))/(Math.pow(2,minimap.lZoom)/2);
						double adjustedDiff = max - Math.max((Math.abs(wayY) - 0), 0);
						contact.brightness = (float)Math.max(adjustedDiff / max, 0);
						contact.brightness *= contact.brightness;
						contacts.add(contact);

							//contacts.add(new Contact((int)(entity.posX), (int)(entity.posZ), (int)(entity.posY), getContactType(entity)));
					} // end if valid contact
				} // end if should be displayed
			} catch (Exception classNotFoundException) {
				this.error = "class not found";
			}
		} // end for loop contacts
		Collections.sort(contacts, new java.util.Comparator<Contact>() {
			public int compare(Contact contact1, Contact contact2) {
				return contact1.y - contact2.y;
			}
		});
		this.lastX = this.xCoordDouble();
		this.lastZ = this.zCoordDouble();
		this.lastY = this.yCoord();
		this.lastZoom = this.minimap.lZoom;
	}
	
	private Contact handleMPplayer(Entity entity) {
		String playerName = scrubCodes(((EntityOtherPlayerMP)entity).username);
		String skinURL = ((EntityOtherPlayerMP)entity).skinUrl;
		Contact mpContact = new Contact(entity, /*(int)*/(entity.posX), /*(int)*/(entity.posZ), (int)(entity.posY), getContactType(entity));
		mpContact.setName(playerName);
		Integer ref = mpContacts.get(playerName+0); // don't load if already done
		//System.out.println("***********CHECKING " + ref);
		if (ref == null) { // if we haven't encountered player yet, try to get MP skin
			ThreadDownloadImageData imageData = this.renderEngine.obtainImageData(skinURL, new ImageBufferDownload());
			if (imageData == null || imageData.image == null) { // failed to get 
				BufferedImage skinImage = loadSkin(playerName); // try to load icon saved to disk
				if (skinImage != null) { // if there is one, 128it and use
					//skinImage = intoSquare(skinImage); // actally square it.  actually don't bother should all be 8x8
					BufferedImage skinImageSmall = fillOutline(intoSquare(skinImage)); // add space around edge and make ready for filtering
					BufferedImage skinImageLarge = fillOutline(intoSquare(scaleImage(skinImage, 2)));
					int imageRef = this.tex(skinImageSmall);
					mpContacts.put(playerName+0, imageRef);
					//System.out.println("Loading " + playerName + " from disk: NEW REF " + imageRef);
					imageRef = this.tex(skinImageLarge);
					mpContacts.put(playerName+1, imageRef);
				}
				else { // else default
					mpContacts.put(playerName, -1); // so make image ref for this player the standard player icon.  Try for hidef (if it's the same as lodef, well, lodef will show)
					//System.out.println("***********DEFAULTING " + imageRef[PLAYER][1]);
				}
			}
			else { // we got a downloaded image
				BufferedImage skinImage = imageData.image;
				//skinImage = into128(addImages(loadImage(skinImage, 8, 8, 8, 8), loadImage(skinImage, 40, 8, 8, 8), 0, 0));
				skinImage = addImages(loadImage(skinImage, 8, 8, 8, 8), loadImage(skinImage, 40, 8, 8, 8), 0, 0, 8, 8);
				saveSkin(skinImage, playerName); // save for future use when skin server is down
				BufferedImage skinImageSmall = fillOutline(intoSquare(skinImage)); // add space around edge and make ready for filtering
				BufferedImage skinImageLarge = fillOutline(intoSquare(scaleImage(skinImage, 2)));
				int imageRef = this.tex(skinImageSmall);
				mpContacts.put(playerName+0, imageRef);
				//System.out.println("***********NEW REF " + imageRef);
				imageRef = this.tex(skinImageLarge);
				mpContacts.put(playerName+1, imageRef);
			}
			if (imageData != null)
				this.renderEngine.releaseImageData(skinURL); // if it was not null, the reference was incremented.  Decrement it!
		}
		if (showHelmets) {
			ItemStack stack = ((EntityOtherPlayerMP)entity).getCurrentArmor(3);
			Item helmet = null;
			if (stack != null && stack.stackSize > 0) 
				helmet = stack.getItem();
			if (helmet != null && helmet instanceof ItemArmor) {
				ItemArmor helmetArmor = (ItemArmor)helmet;
				EnumArmorMaterial material = helmetArmor.getArmorMaterial();
				mpContact.setArmor(this.getArmorType(material));
				if (mpContact.armorValue == CLOTH) 
					mpContact.setArmorColor(helmetArmor.getColorFromItemStack(((EntityOtherPlayerMP)entity).getCurrentArmor(3), 0));
			}
		}
        return mpContact;
	}
	
    private String scrubCodes(String string) {
    	string = string.replaceAll("(§.)", "");
        return string;
    }
	
	private void saveSkin(BufferedImage skinImage, String playerName) {
		try {
			String path = "minecraft/mods/zan/" + minimap.getServerName();
			File outFile = new File(Minecraft.getAppDir(path), playerName + ".png");
			outFile.createNewFile();
			ImageIO.write(skinImage, "png", outFile);
		}
		catch (Exception e) {
			System.out.println("playername: " + playerName + " - error saving skin image: " + e.getLocalizedMessage());
		}
	}
	
	private BufferedImage loadSkin(String playerName) {
		try {
			String path = "minecraft/mods/zan/" + minimap.getServerName();
			File inFile = new File(Minecraft.getAppDir(path), playerName + ".png");
			java.awt.Image icon = ImageIO.read(inFile);
			BufferedImage iconBuffered = new BufferedImage(icon.getWidth(null), icon.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics gfx = iconBuffered.createGraphics();
		    // Paint the image onto the buffered image
		    gfx.drawImage(icon, 0, 0, null);
		    gfx.dispose();
		    return iconBuffered;
		}
		catch (Exception e) {
			System.out.println("playername: " + playerName + " - error loading skin image: " + e.getLocalizedMessage());
			return null;
		}
	}
	
	private int getContactType(Entity entity) {
		if (entity instanceof EntityBat)
			return BAT;
		else if (entity instanceof EntityBlaze)
			return BLAZE;
		else if (entity instanceof EntityCaveSpider)
			return CAVESPIDER;
		else if (entity instanceof EntityChicken)
			return CHICKEN;
		else if (entity instanceof EntityMooshroom) // out of order, so we don't get cow for mooshroom (it's a subcow apparently)
			return MOOSHROOM;
		else if (entity instanceof EntityCow)
			return COW;
		else if (entity instanceof EntityCreeper)
			return CREEPER;
		else if (entity instanceof EntityDragon)
			return ENDERDRAGON;
		else if (entity instanceof EntityEnderman)
			return ENDERMAN;
		else if (entity instanceof EntityGhast) 
			return GHAST;			
		else if (entity instanceof EntityIronGolem)
			return IRONGOLEM;
		else if (entity instanceof EntityMagmaCube)
			return MAGMA;
		else if (entity instanceof EntityOcelot)
			return (((EntityOcelot)entity).getTexture().equals("/mob/ozelot.png"))?OCELOT:(((EntityOcelot)entity).getTexture().equals("/mob/cat_black.png"))?CATBLACK:(((EntityOcelot)entity).getTexture().equals("/mob/cat_red.png"))?CATRED:CATSIAMESE;
		else if (entity instanceof EntityPig)
			return PIG;
		else if (entity instanceof EntityPigZombie)
			return PIGZOMBIE;
		else if (entity instanceof EntityOtherPlayerMP)
			return PLAYER;
		else if (entity instanceof EntitySheep)
			return SHEEP;
		else if (entity instanceof EntitySilverfish)
			return SILVERFISH;
		else if (entity instanceof EntitySkeleton)
			return (((EntitySkeleton)entity).ridingEntity != null)?(((EntitySkeleton)entity).getTexture().equals("/mob/skeleton_wither.png"))?SPIDERJOCKEYWITHER:SPIDERJOCKEY : (((EntitySkeleton)entity).getTexture().equals("/mob/skeleton_wither.png"))?SKELETONWITHER:SKELETON;
		else if (entity instanceof EntitySlime)
			return SLIME;
		else if (entity instanceof EntitySnowman)
			return SNOWGOLEM;
		else if (entity instanceof EntitySpider)
			return (((EntitySpider)entity).riddenByEntity != null)?BLANK:SPIDER; // don't render if it's in a spiderjockey pair.  Let the skeleton render both.
		else if (entity instanceof EntitySquid)
			return SQUID;
		else if (entity instanceof EntityVillager)
			return VILLAGER;
		else if (entity instanceof EntityWitch)
			return WITCH;
		else if (entity instanceof EntityWither)
			return WITHER;
		else if (entity instanceof EntityWolf)
			return (((EntityWolf)entity).getTexture().equals("/mob/wolf_tame.png"))?WOLFTAME:(((EntityWolf)entity).getTexture().equals("/mob/wolf_angry.png"))?WOLFANGRY:WOLF;
		else if (entity instanceof EntityZombie) 
			return (((EntityZombie)entity).getTexture().equals("/mob/zombie_villager.png"))?ZOMBIEVILLAGER:ZOMBIE;
		// didn't match anything we know.  Probably a mod.  Hopefully they inherit from useful stuff like regular mobs
		else if (isHostile(entity))
			return BLANKHOSTILE;
		else if (entity instanceof EntityTameable && ((EntityTameable)entity).isTamed() && (this.game.isSingleplayer() || ((EntityTameable)entity).getOwnerName().equals(this.game.thePlayer.username)))
			return BLANKTAME;
		else
			return BLANKNEUTRAL;
		//return BLANK; // blank is a more sensible default than zombie
	}
	
	private int getArmorType(EnumArmorMaterial material) {
        switch (material)
        {
            case CLOTH:
                return this.CLOTH;
                
            case CHAIN:
            	return this.CHAIN;

            case IRON:
                return this.IRON;

            case GOLD:
                return this.GOLD;
                
            case DIAMOND:
                return this.DIAMOND;
                
            default:
            	return -1;
                
        }
	}

	
	private boolean singlePixelPer(int scale, int zoom) {
		if (scale > 4)
			return false;
		if (scale >= 4)
			return (zoom >= 3);
		if (scale >= 3) 
			return (zoom >= 3);
		if (scale >= 2)
			return (zoom >= 2);
		if (scale >= 1)
			return (zoom >= 1);
		return true;
	}
	
	public void renderMapMobs (int x, int y, int guiScale) {
		//final long startTime = System.nanoTime();
		// 0 is closest zoom.  16.  1 is out, 32. 2 is out 64
		// so max distance is pow(2,lzoom)*16
		double max = (Math.pow(2,minimap.lZoom) * 16 - 0);
		this.lastZoom = this.minimap.lZoom;
		for(int j = 0; j < contacts.size(); j++) {
			Contact contact = contacts.get(j);
			contact.updateLocation();
			double contactX = contact.x;
			double contactZ = contact.z;
			int contactY = contact.y;

			double wayX = this.minimap.lastXDouble - contactX;
			double wayZ = this.minimap.lastZDouble - contactZ;
			if (this.minimap.lastXDouble < 0)
				wayX++;
			if (this.minimap.lastZDouble < 0)
				wayZ++;
			int wayY = this.yCoord() - contactY;
			// zoom level 0 from 0 (12-12) to 4 (16-12)
			// zoom level 1 from 0 (12-12) to 20 (32-12)
			// max opaque at 0, min opaque at pow(2,lZoom)-12
			// so invert (make value max - value) max at pow(2,lZoom), min at 0;
			// then adjust out of 1: 1/max*level  aka level/max :)
			double adjustedDiff = max - Math.max((Math.abs(wayY) - 0), 0);
			contact.brightness = (float)Math.max(adjustedDiff / max, 0);
			contact.brightness *= contact.brightness;
			contact.angle = (float)Math.toDegrees(Math.atan2(wayX, wayZ));
			// pow2,0 is 1.  /2 is 1/2.  so if hypot max 16/.5 < 31.  pow2,1 is 2. /2 is 1.  hypot max 32/1 < 31.  pow2,2 is 4. /2 is 2.  hypot max 64/2 < 31  
			contact.distance = Math.sqrt((wayX*wayX)+(wayZ*wayZ))/(Math.pow(2,minimap.lZoom)/2)*minimap.scScale;
			//System.out.println("adjusted diff: " + adjustedDiff + " max: " + max + " brightness: " + brightness);

			/*
			if (wayY < 0)
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // fade out
			else
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ZERO); // blacken out.  I guess GL_ONE could brighten up?  test if mapY is + or - if we go that way
			 */
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // fade out
			if (wayY < 0)
				GL11.glColor4f(1.0F, 1.0F, 1.0F, contact.brightness);
			else
				GL11.glColor3f(1.0F*contact.brightness, 1.0F*contact.brightness, 1.0F*contact.brightness);	// not using alpha to darken.  This lets us apply linear filter if we want without creating an outline


			if ( (minimap.squareMap && Math.abs(wayX)/(Math.pow(2,this.minimap.lZoom)/2) <= 28.5 && Math.abs(wayZ)/(Math.pow(2,this.minimap.lZoom)/2) <= 28.5) || (!minimap.squareMap && contact.distance < 31*minimap.scScale) ) {
				try {
					GL11.glPushMatrix();
					if (contact.type == PLAYER) {
						Integer ref = mpContacts.get(contact.name + guiScale);
						if (ref == null || ref == -1)
							this.disp(imageRef[PLAYER][guiScale]); // display default icon if skin is not loaded yet.
						else 
							this.disp(ref); // there is a mapping from name to image (it could be to the default image, if loading finishes and it turns out there is no custom skin)
					} // end if other player
					else {
						if (contact.entity instanceof EntityGhast) 
							contact.type = (((EntityGhast)contact.entity).getTexture().equals("/mob/ghast_fire.png"))?GHASTATTACKING : GHAST;
						else if (contact.entity instanceof EntityWither) 
							contact.type = (((EntityWither)contact.entity).getTexture().equals("/mob/wither_invul.png"))?WITHERINVULNERABLE : WITHER;

						this.disp(imageRef[contact.type][guiScale]);
					}
					if (filtering) {
						GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
						GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
					}
					else {
						GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST); 
						GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);							
					}


					//GL11.glTranslatef(x, y, 0.0F);
					//		GL11.glRotatef(-contact.angle + (minimap.squareMap?minimap.northRotate:-this.direction), 0.0F, 0.0F, 1.0F); // + 90 w top, 0 n top
					//		GL11.glTranslated(0.0D,-contact.distance,0.0D);
					//		GL11.glRotatef(-(-contact.angle + (minimap.squareMap?minimap.northRotate:-this.direction)), 0.0F, 0.0F, 1.0F); // + 90 w top, 0 n top
					//GL11.glTranslated(0.0D,contact.distance,0.0D);
					//GL11.glTranslatef(-x, -y, 0.0F);
					//GL11.glTranslated(0.0D,-contact.distance,0.0D);

					wayX = Math.sin(Math.toRadians(-(-contact.angle + (minimap.squareMap?minimap.northRotate:-this.direction))))*contact.distance;
					wayZ = Math.cos(Math.toRadians(-(-contact.angle + (minimap.squareMap?minimap.northRotate:-this.direction))))*contact.distance;
					if (filtering)
						GL11.glTranslated(-wayX,-wayZ,0.0D);
					else
						GL11.glTranslated(Math.round(-wayX),Math.round(-wayZ),0.0D);
					//GL11.glTranslated((int)(-wayX),(int)(-wayZ),0.0D);


					drawPre();
					this.setMap(x, y, size[contact.type]);
					drawPost();

					if (showHelmets && contact.armorValue != -1) { // draw another picture over the previous one.  Could do this for helmets.  But for zombie villagers?  ugh.  head will stick out the top
						float red = 0;
						float green = 1;
						float blue = 1;
						if (contact.armorValue == CLOTH) {
							red = (contact.armorColor >> 16 & 255)/255f;
							green = (contact.armorColor >> 8 & 255)/255f;
							blue = (contact.armorColor >> 0 & 255)/255f;
							GL11.glColor3f(red, green, blue);
						}
						this.disp(armorImageRef[contact.armorValue][guiScale]);
						if (filtering) {
							GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
							GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
						}
						else {
							GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST); 
							GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);							
						}
						drawPre();
						this.setMap(x, y, 20);
						drawPost(); 

						if (contact.armorValue == CLOTH) {
							GL11.glColor3f(1f, 1f, 1f);
							this.disp(armorImageRef[CLOTH+2][guiScale]);
							if (filtering) {
								GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
								GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
							}
							else {
								GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST); 
								GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);							
							}
							drawPre();
							this.setMap(x, y, 20);
							drawPost();
							
							GL11.glColor3f(red, green, blue);
					//	}
						this.disp(armorImageRef[contact.armorValue+1][guiScale]);
						if (filtering) {
							GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
							GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
						}
						else {
							GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST); 
							GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);							
						}
						drawPre();
						this.setMap(x, y, 20);
						drawPost(); 
						
					//	if (contact.armorValue == CLOTH) {
							GL11.glColor3f(1f, 1f, 1f);
							this.disp(armorImageRef[CLOTH+3][guiScale]);
							if (filtering) {
								GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
								GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
							}
							else {
								GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST); 
								GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);							
							}
							drawPre();
							this.setMap(x, y, 20);
							drawPost();
						} // and if drawing cloth (overlay, outer, overlayouter
					} // end if show helmets
					
				} 
				catch (Exception localException) {
					this.error = "Error rendering mob icon! " + localException.getLocalizedMessage() + " contact type " + contact.type;
				} 
				finally {
					GL11.glPopMatrix();
				}
			}


		} // end for contacts

		//			this.write(" " + minDistance, scWidth*2-66, 70, 0xffffff);
		GL11.glPushMatrix();
		GL11.glScalef(0.5f, 0.5f, 1.0f);
		GL11.glPopMatrix(); 
		//System.out.println("time: " + (System.nanoTime()-startTime));
	}
	
	public void loadSettings(File settingsFile) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(settingsFile));
			String sCurrentLine;
			while ((sCurrentLine = in.readLine()) != null) {
				String[] curLine = sCurrentLine.split(":");
				if (curLine[0].equals("Hide Radar"))
					hide = Boolean.parseBoolean(curLine[1]);
				else if(curLine[0].equals("Show Hostiles"))
					showHostiles = Boolean.parseBoolean(curLine[1]);
				else if(curLine[0].equals("Show Players"))
					showPlayers = Boolean.parseBoolean(curLine[1]);
				else if(curLine[0].equals("Show Neutrals"))
					showNeutrals = Boolean.parseBoolean(curLine[1]);
				else if(curLine[0].equals("Filter Mob Icons"))
					filtering = Boolean.parseBoolean(curLine[1]);
				else if(curLine[0].equals("Outline Mob Icons"))
					outlines = Boolean.parseBoolean(curLine[1]);
				else if(curLine[0].equals("Show Helmets"))
					showHelmets = Boolean.parseBoolean(curLine[1]);
			}
			in.close();
		}
		catch (Exception e) {
		}
	}
	
	public void saveAll(PrintWriter out) {
		out.println("Hide Radar:" + Boolean.toString(hide));
		out.println("Show Hostiles:" + Boolean.toString(showHostiles));
		out.println("Show Players:" + Boolean.toString(showPlayers));
		out.println("Show Neutrals:" + Boolean.toString(showNeutrals));
		out.println("Filter Mob Icons:" + Boolean.toString(filtering));
		out.println("Outline Mob Icons:" + Boolean.toString(outlines));
		out.println("Show Helmets:" + Boolean.toString(showHelmets));
	}
	
	public void saveAll() {
		File settingsFile = new File(Minecraft.getAppDir("minecraft"), "radar.settings");

		try {
			PrintWriter out = new PrintWriter(new FileWriter(settingsFile));
			out.println("Hide Radar:" + Boolean.toString(hide));
			out.println("Show Hostiles:" + Boolean.toString(showHostiles));
			out.println("Show Players:" + Boolean.toString(showPlayers));
			out.println("Show Neutrals:" + Boolean.toString(showNeutrals));
			out.println("Filter Mob Icons:" + Boolean.toString(filtering));
			out.println("Outline Mob Icons:" + Boolean.toString(outlines));
			out.println("Show Helmets:" + Boolean.toString(showHelmets));
			out.close();
		} catch (Exception local) {
			minimap.chatInfo("§EError Saving Settings");
		}
	}
	
	private boolean isHostile(Entity entity) {
		if (entity instanceof EntityPigZombie)
			//return (((EntityPigZombie)entity).entityToAttack != null);
			return (((EntityPigZombie)entity).moveSpeed == 0.95F);
		//if (entity instanceof EntityEnderman) 
		//	return (((EntityEnderman)entity).entityToAttack != null);
		// TODO endermen like wolves
		if (entity instanceof EntityMob) // most mobs (including pigzombies, why I handled them first)
			return true;
		if (entity instanceof IMob) // ghast
			return true;
		if (entity instanceof IBossDisplayData) // dragon
			return true;
		if (entity instanceof EntityWolf) 
			return ((EntityWolf)entity).isAngry();
		return false;
	}
	private boolean isPlayer(Entity entity) {
		return (entity instanceof EntityOtherPlayerMP);
	}
	private boolean isNeutral(Entity entity) {
		if (entity instanceof EntityLiving) 
			return !(entity instanceof EntityPlayer || isHostile(entity)); // can't just check if it's not an MP player, could be THE player
		else
			return false;
	}
	
	// menu from here
	
    /**
     * Gets a key binding. // aka the text on the button?
     */
    public String getKeyText(EnumOptionsMinimap par1EnumOptions)
    {
        StringTranslate stringtranslate = StringTranslate.getInstance();
        String s = (new StringBuilder()).append(stringtranslate.translateKey(par1EnumOptions.getEnumString())).append(": ").toString(); // use if I ever do translations
        //String s = (new StringBuilder()).append(par1EnumOptions.getEnumString()).append(": ").toString();

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

        else
        {
            return s;
        }
    }
    
    public boolean getOptionBooleanValue(EnumOptionsMinimap par1EnumOptions)
    {
        switch (EnumOptionsHelperMinimap.enumOptionsMappingHelperArray[par1EnumOptions.ordinal()])
        {
            case 19:
                return this.hide;
                
            case 20:
            	return this.showHostiles;

            case 21:
                return this.showPlayers;

            case 22:
                return this.showNeutrals;
                
            case 23:
                return this.outlines;
                
            case 24:
                return this.filtering;
                
            case 25:
                return this.showHelmets;
        }

        return false;
    }
    
	public void setOptionValue(EnumOptionsMinimap par1EnumOptions, int i) {
        switch (par1EnumOptions.ordinal())
        {
            case 19:
                this.hide = !hide;
                break;
                
            case 20:
                this.showHostiles = !showHostiles;
                break;

            case 21:
                this.showPlayers = !showPlayers;
                break;

            case 22:
                this.showNeutrals = !showNeutrals;
                break;
                
            case 23:
                this.outlines = !outlines;
        		for(Map.Entry<String,Integer> entry : mpContacts.entrySet()) {
        			this.glah(entry.getValue());
        		}
                mpContacts.clear();
                this.loadTexturePackIcons();
                break;
                
            case 24:
                this.filtering = !filtering;
                break;
                
            case 25:
                this.showHelmets = !showHelmets;
                break;
        }
		this.timer = 500; // immediately show changes
        //this.saveAll(); // called from the gui when the gui screen closes.  done once per screen instead of per option set.  On the other hand, it's additional saves for times when no options are changed
	}

}
