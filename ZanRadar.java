package net.minecraft.src;

import java.awt.Color;
import java.awt.Graphics2D; //
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream; //
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.src.mamiyaotaru.Contact;
import net.minecraft.src.mamiyaotaru.EnumOptionsHelperMinimap;
import net.minecraft.src.mamiyaotaru.EnumOptionsMinimap;

public class ZanRadar {
	
	private Minecraft game; 
	
	private Tessellator lDraw = Tessellator.instance;
	
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
	private int[] size = {2, 8, 8, 8, 16, 8, 8, 8, 8, 8, 8, 16, 8, 32, 8, 16, 16, 16, 8, 16, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 16, 16, 8, 16, 16, 32, 32, 8, 8, 8, 8, 16};//new int[26]; 
	// bat is 8 without ears 16 with.  cow (and mooshroom) is 8 without horns 16 with.  dragon is 16 without crests 32 with
	
	public ZanRadar(ZanMinimap minimap) {
		this.minimap = minimap;
		this.renderEngine = minimap.renderEngine;
		for (int t = 0; t < icons.length; t++) {
			imageRef[t][0]=-1;
			imageRef[t][1]=-1;
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
			icons[PLAYER][0] = loadImage("char", 8, 8, 8, 8);
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
			
			for (int t = 0; t < icons.length; t++) {
				if (imageRef[t][0] != -1)
					glah(imageRef[t][0]);
				if (imageRef[t][1] != -1 && imageRef[t][1] != imageRef[t][0])
					glah(imageRef[t][1]);
				//icons[t]=into128(icons[t]);
				icons[t][0]=intoSquare(icons[t][0]); // this is actually faster.  180000 to 140000 or so
				icons[t][1]=icons[t][0];
				float scale = icons[t][0].getWidth()/size[t];
				if (scale>2)
					icons[t][1] = scaleImage(icons[t][0], (1/scale)*2); // if icons are more than double, reduce to double for hidef icons
				if (scale>1)
					icons[t][0] = scaleImage(icons[t][0], (1/scale)); // if icons are more than default, reduce to default
				if (renderEngine!=null) { 
					imageRef[t][0]=this.tex(icons[t][0]);
					if (icons[t][1].equals(icons[t][0]))
						imageRef[t][1] = imageRef[t][0]; // if hidef is the same as lodef (8px and lower texture pack) use same reference
					else
						imageRef[t][1]=this.tex(icons[t][1]);
				}
				else {
					imageRef[t][0]=-1;
					imageRef[t][1]=-1;
				}
			}
			
		/*	String[] names = new String[25];
			names[BLAZE] = "fire";
			names[CAVESPIDER] = "cavespider";
			names[CHICKEN] = "chicken";
			names[COW] = "cow";
			names[CREEPER] = "creeper";
			names[ENDERDRAGON] = "enderdragon/ender";
			names[ENDERMAN] = "enderman";
			names[GHAST] = "ghast";
			names[IRONGOLEM] = "villager_golem";
			names[MAGMA] = "lava";
			names[MOOSHROOM] = "redcow";
			names[OCELOT] = "ozelot";
			names[PIG] = "pig";
			names[PIGZOMBIE] = "pigzombie";
			names[PLAYER] = "char";
			names[SHEEP] = "sheep";
			names[SILVERFISH] = "silverfish";
			names[SKELETON] = "skeleton";
			names[SLIME] = "slime";
			names[SNOWGOLEM] = "snowman";
			names[SPIDER] = "cavespider";
			names[SQUID] = "squid";
			names[VILLAGER] = "villager/farmer";
			names[WOLF] = "wolf";
			names[ZOMBIE] = "zombie";

			for (int t = 0; t < 25; t++) {
				File outFile = new File("j:/" + names[t] + ".png");
				outFile.createNewFile();
				ImageIO.write(icons[t], "png", outFile);
			}*/
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
	
	// load image with the default dimensions (most common anyway, 64x32 in default)
	private BufferedImage loadImage(String path, int x, int y, int w, int h) {
		return loadImage(path, x, y, w, h, 64, 32);
	}
	
	private BufferedImage loadImage(String path, int x, int y, int w, int h, int imageWidth, int imageHeight) {
		try {
			String fullPath = "/mob/" + path + ".png";
			InputStream is = pack.getResourceAsStream(fullPath);
			BufferedImage mobSkin = ImageIO.read(is);
			is.close();
			
			// System.out.println(path + " is type " + mobSkin.getType());
			// convert to TYPE_4BYTE_ABGR if it isn't.  TYPE_BYTE_INDEXED break all over
			// actually do below in case we get passed something in a bad format
			return loadImage(mobSkin, x, y, w, h, imageWidth, imageHeight);

		}
		catch (Exception e) {
			System.out.println("Failed getting mob: " + path + " - " + e.getLocalizedMessage());
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
		java.awt.Graphics gfx = base.getGraphics();
		gfx.drawImage(overlay, (int)(x*scale), y*scale, null); // float for x here simply allows us to center the wolf nose in double and higher resolution packs (witch hat too)
		gfx.dispose();
		return base;
	}
	
	private BufferedImage scaleImage(BufferedImage image, float scaleBy) {
		BufferedImage tmp = new BufferedImage((int)(image.getWidth()*scaleBy), (int)(image.getHeight()*scaleBy), image.getType());
		Graphics2D g2 = tmp.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
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
		java.awt.Graphics gfx = frame.getGraphics();
		gfx.drawImage(base, 64-base.getWidth()/2, 64-base.getHeight()/2, base.getWidth(), base.getHeight(), null);
		gfx.dispose();
		return frame;
	}
	
	private BufferedImage intoSquare(BufferedImage base) {
		int dim = Math.max(base.getWidth(), base.getHeight());
		int t = 0;
		while (Math.pow(2, t) < dim)
			t++;
		int size = (int)Math.pow(2, t);
		
		BufferedImage frame = new BufferedImage(size, size, base.getType());
		java.awt.Graphics gfx = frame.getGraphics();
		gfx.drawImage(base, (size-base.getWidth())/2, (size-base.getHeight())/2, base.getWidth(), base.getHeight(), null);
		gfx.dispose();
		return frame;
	}
	
	public void setTexturePack(ITexturePack pack) {
		this.pack = pack;
	}
	
	public void drawPre()
	{
		lDraw.startDrawingQuads();
	}
	
	public void drawPost()
	{
		lDraw.draw();
	}
	
	public void glah(int g)
	{
		renderEngine.deleteTexture(g);
	}
	
	public void ldrawthree(double a, double b, double c, double d, double e)
	{
		lDraw.addVertexWithUV(a, b, c, d, e);
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
	
	public void OnTickInGame(Minecraft mc) {
		if(game==null) game = mc;
		if(fontRenderer==null) fontRenderer = this.game.fontRenderer;
		if(renderEngine==null && completedLoading) {
			renderEngine = this.game.renderEngine;
			if (renderEngine!=null) { // once we get a render engine (and only once) allocate the images
				for (int t = 0; t < icons.length; t++) {
					imageRef[t][0]=this.tex(icons[t][0]);
					if (icons[t][1].equals(icons[t][0]))
						imageRef[t][1] = imageRef[t][0]; // if hidef is the same as lodef (8px and lower texture pack) use same reference
					else
						imageRef[t][1]=this.tex(icons[t][1]);
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

		this.direction = -this.game.thePlayer.rotationYaw;

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
		
		if (this.enabled && !this.hide && !minimap.hide  && !minimap.fullscreenMap) {
			
			// don't recalculate mobs all the time.  doesn't seem to affect FPS, whatev
			if (this.timer>95) { // not multiple of 100 so doesn't happen at same time as map render
				calculateMobs();
				timer = 0;
			}
			timer++;
			// commented out block only needed if this is split out into separate mod
			/*GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDepthMask(false);*/ 
			// blendfunc and color are set for each icon, don't need to do it here
		//	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ZERO);
		//	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

			if (completedLoading) renderMapMobs(this.minimap.mapX, this.minimap.mapY, guiScale);	
			
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

	
	public void calculateMobs () {
		contacts.clear();
		int multi = (int)Math.pow(2, minimap.lZoom);
		java.util.List entities = this.game.theWorld.getLoadedEntityList();
		for(int j = 0; j < entities.size(); j++) {
			Entity entity = (Entity)entities.get(j);
			try {
				if(  (showHostiles && isHostile(entity)) || (showPlayers && isPlayer(entity)) || (showNeutrals && isNeutral(entity)) ) {
					int wayX = this.xCoord() - (int)(entity.posX);
					int wayZ = this.zCoord() - (int)(entity.posZ);
					int wayY = this.yCoord() - (int)(entity.posY);
					double hypot = Math.sqrt((wayX*wayX)+(wayZ*wayZ)+(wayY*wayY));
					hypot = hypot/(Math.pow(2,minimap.lZoom)/2);
					//if (hypot < 31.0D && Math.abs(this.zCoord() - (int)(entity.posY)) < 12) {					
					if (hypot < 31.0D) {
						//System.out.println("player: " + (int)this.game.thePlayer.posY + " mob: " + (int)(entity.posY));

						if (isPlayer(entity)) 
							contacts.add(handleMPplayer(entity));
						else  {
							Contact contact = new Contact((int)(entity.posX), (int)(entity.posZ), (int)(entity.posY), getContactType(entity));
							if (contact.type == GHAST || contact.type == WITHER) {
								contact.setEntity(entity); // allows us to display firing vs non firing icons for ghasts, invulnerable vs normal wither.  Need reference to the actual entity
							}
							contacts.add(contact);
						}
							//contacts.add(new Contact((int)(entity.posX), (int)(entity.posZ), (int)(entity.posY), getContactType(entity)));
					} // end if valid contact
				} // end if should be displayed
			} catch (Exception ClassNotFoundException) {
				this.error = "class not found";
			}
		} // end for loop contacts
		Collections.sort(contacts, new java.util.Comparator<Contact>() {
			public int compare(Contact contact1, Contact contact2) {
				return contact1.y - contact2.y;
			}
		});
	}
	
	private Contact handleMPplayer(Entity entity) {
		String playerName = scrubCodes(((EntityOtherPlayerMP)entity).username);
		String skinURL = ((EntityOtherPlayerMP)entity).skinUrl;
		Contact mpContact = new Contact((int)(entity.posX), (int)(entity.posZ), (int)(entity.posY), getContactType(entity));
		mpContact.setName(playerName);
		Integer ref = mpContacts.get(playerName); // don't load if already done
		//System.out.println("***********CHECKING " + ref);
		if (ref == null) { // if we haven't encountered player yet, try to get MP skin
			ThreadDownloadImageData imageData = this.renderEngine.obtainImageData(skinURL, new ImageBufferDownload());
			if (imageData == null || imageData.image == null) { // failed to get 
				BufferedImage skinImage = loadSkin(playerName); // try to load icon saved to disk
				if (skinImage != null) { // if there is one, 128it and use
					//skinImage = intoSquare(skinImage); // actally square it.  actually don't bother should all be 8x8
					int imageRef = this.tex(skinImage);
					System.out.println("Loading " + playerName + " from disk: NEW REF " + imageRef);
					mpContacts.put(playerName, imageRef);
				}
				else { // else default
					mpContacts.put(playerName, imageRef[PLAYER][1]); // so make image ref for this player the standard player icon.  Try for hidef (if it's the same as lodef, well, lodef will show)
					//System.out.println("***********DEFAULTING " + imageRef[PLAYER]);
				}
			}
			else { // we got a downloaded image
				BufferedImage skinImage = imageData.image;
				//skinImage = into128(addImages(loadImage(skinImage, 8, 8, 8, 8), loadImage(skinImage, 40, 8, 8, 8), 0, 0));
				skinImage = addImages(loadImage(skinImage, 8, 8, 8, 8), loadImage(skinImage, 40, 8, 8, 8), 0, 0, 8, 8);
				saveSkin(skinImage, playerName); // save for future use when skin server is down
				//skinImage = intoSquare(skinImage);
				int imageRef = this.tex(skinImage);
				//System.out.println("***********NEW REF " + imageRef);
				mpContacts.put(playerName, imageRef);
			}
			if (imageData != null)
				this.renderEngine.releaseImageData(skinURL); // if it was not null, the reference was incremented.  Decrement it!
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
			java.awt.Graphics gfx = iconBuffered.createGraphics();
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
	
	public void renderMapMobs (int x, int y, int guiScale) {
		//final long startTime = System.nanoTime();
		for(int j = 0; j < contacts.size(); j++) {
			Contact contact = contacts.get(j);
			int contactX = contact.x;
			int contactZ = contact.z;
			int contactY = contact.y;
			int wayX = this.xCoord() - contactX;
			int wayZ = this.zCoord() - contactZ;
			int wayY = this.yCoord() - contactY;
			float locate = (float)Math.toDegrees(Math.atan2(wayX, wayZ));
			float differenceDegrees = locate - this.direction;
			if (differenceDegrees < -180)
				differenceDegrees += 360;
			else if (differenceDegrees > 180)
				differenceDegrees -= 360;
			// 0 is closest zoom.  16.  1 is out, 32. 2 is out 64
			// so max distance is pow(2,lzoom)*16
			// zoom level 0 from 0 (12-12) to 4 (16-12)
			// zoom level 1 from 0 (12-12) to 20 (32-12)
			// max opaque at 0, min opaque at pow(2,lZoom)-12
			// so invert (make value max - value) max at pow(2,lZoom), min at 0;
			// then adjust out of 1: 1/max*level  aka level/max :)
			double max = (Math.pow(2,minimap.lZoom) * 16 - 0);
			double adjustedDiff = max - Math.max((Math.abs(wayY) - 0), 0);
			float brightness = (float)Math.max(adjustedDiff / max, 0);
			brightness *= brightness;
			//System.out.println("adjusted diff: " + adjustedDiff + " max: " + max + " brightness: " + brightness);
			if (wayY < 0)
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // fade out
			else
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ZERO); // blacken out.  I guess GL_ONE could brighten up?  test if mapY is + or - if we go that way
			GL11.glColor4f(1.0F, 1.0F, 1.0F, brightness);		

			// pow2,0 is 1.  /2 is 1/2.  so if hypot max 16/.0 < 31.  pow2,1 is 2. /2 is 1.  hypot max 32/1 < 31.  pow2,2 is 4. /2 is 2.  hypot max 64/2 < 31  
			double hypot = Math.sqrt((wayX*wayX)+(wayZ*wayZ))/(Math.pow(2,minimap.lZoom)/2);

			if ( (minimap.squareMap && Math.abs(wayX)/(Math.pow(2,this.minimap.lZoom)/2) <= 31 && Math.abs(wayZ)/(Math.pow(2,this.minimap.lZoom)/2) <= 31) || (!minimap.squareMap && hypot < 31) ) {
				try {
					GL11.glPushMatrix();
					if (contact.type == PLAYER) {
						Integer ref = mpContacts.get(contact.name);
						if (ref == null)
							this.disp(imageRef[PLAYER][1]); // display default icon if skin is not loaded yet.  Try for hidef.  If there is none, it just shows lodef
						else 
							this.disp(mpContacts.get(contact.name)); // there is a mapping from name to image (it could be to the default image, if loading finishes and it turns out there is no custom skin)
					}
					else  {
						if (contact.entity != null) {
							if (contact.entity instanceof EntityGhast) 
								contact.type = (((EntityGhast)contact.entity).getTexture().equals("/mob/ghast_fire.png"))?GHASTATTACKING : GHAST;
							else if (contact.entity instanceof EntityWither) 
								contact.type = (((EntityWither)contact.entity).getTexture().equals("/mob/wither_invul.png"))?WITHERINVULNERABLE : WITHER;
						}
						this.disp(imageRef[contact.type][guiScale]);
					}
					//GL11.glTranslated(-wayX/(Math.pow(2,this.zoom)/2),-wayY/(Math.pow(2,this.zoom)/2),0.0D); //y -x W at top, -x -y N at top
					// from here
					GL11.glTranslatef(x, y, 0.0F);
					GL11.glRotatef(-locate + (minimap.squareMap?90.0F - minimap.northRotate:this.direction + 180.0F), 0.0F, 0.0F, 1.0F); // + 90 w top, 0 n top
					GL11.glTranslated(0.0D,-hypot,0.0D);
					GL11.glRotatef(-(-locate + (minimap.squareMap?90.0F - minimap.northRotate:this.direction + 180.0F)), 0.0F, 0.0F, 1.0F); // + 90 w top, 0 n top
					GL11.glTranslated(0.0D,hypot,0.0D);
					GL11.glTranslatef(-x, -y, 0.0F);
					GL11.glTranslated(0.0D,-hypot,0.0D);
					// to here only necessary with variable north, and no if/else statements in mapcalc.  otherwise uncomment the translated above this block
					drawPre();
					this.setMap(x, y, size[contact.type]);
					drawPost();
					/*this.disp(imageRef[contact.type-1][guiScale]);
					drawPre();
					this.setMap(scWidth, size[contact.type-1]);
					drawPost();*/ // draw another picture over the previous one.  Could do this for helmets.  But for zombie villagers?  ugh.  head will stick out the top


				} 
				catch (Exception localException) {
					this.error = "Error rendering mob icons! " + localException.getLocalizedMessage() + " contact type " + contact.type;
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
	
	public void saveAll(PrintWriter out) {
		out.println("Hide Radar:" + Boolean.toString(hide));
		out.println("Show Hostiles:" + Boolean.toString(showHostiles));
		out.println("Show Players:" + Boolean.toString(showPlayers));
		out.println("Show Neutrals:" + Boolean.toString(showNeutrals));
	}
	
	public void saveAll() {
		File settingsFile = new File(Minecraft.getAppDir("minecraft"), "radar.settings");

		try {
			PrintWriter out = new PrintWriter(new FileWriter(settingsFile));
			out.println("Hide Radar:" + Boolean.toString(hide));
			out.println("Show Hostiles:" + Boolean.toString(showHostiles));
			out.println("Show Players:" + Boolean.toString(showPlayers));
			out.println("Show Neutrals:" + Boolean.toString(showNeutrals));
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
//      String s = (new StringBuilder()).append(stringtranslate.translateKey(par1EnumOptions.getEnumString())).append(": ").toString(); // use if I ever do translations
        String s = (new StringBuilder()).append(par1EnumOptions.getEnumString()).append(": ").toString();

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
            case 13:
                return this.hide;
                
            case 14:
            	return this.showHostiles;

            case 15:
                return this.showPlayers;

            case 16:
                return this.showNeutrals;
        }

        return false;
    }
    
	public void setOptionValue(EnumOptionsMinimap par1EnumOptions, int i) {
        switch (par1EnumOptions.ordinal())
        {
            case 13:
                this.hide = !hide;
                break;
                
            case 14:
                this.showHostiles = !showHostiles;
                break;

            case 15:
                this.showPlayers = !showPlayers;
                break;

            case 16:
                this.showNeutrals = !showNeutrals;
                break;
        }
		this.timer = 500; // immediately show changes
        //this.saveAll(); // called from the gui when the gui screen closes.  done once per screen instead of per option set.  On the other hand, it's additional saves for times when no options are changed
	}

}
