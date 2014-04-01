package net.minecraft.src;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import net.minecraft.src.Block;
import net.minecraft.src.ColorizerFoliage;
import net.minecraft.src.ColorizerGrass;
import net.minecraft.src.ITexturePack;
import net.minecraft.src.Icon;
import net.minecraft.src.Texture;
import net.minecraft.src.TexturePackCustom;
import net.minecraft.src.TexturePackDefault;
import net.minecraft.src.TexturePackFolder;
import net.minecraft.src.TextureStitched;
import net.minecraft.src.ZanMinimap;
import net.minecraft.src.mamiyaotaru.EnumOptionsHelperMinimap;

public class ZanColorManager {
	
	private ZanMinimap minimap;
	
	/*Current Texture Pack*/
	private ITexturePack pack = null;
	
	private BufferedImage terrainBuff = null;
	
	private BufferedImage terrainBuffTrans = null;
	
	/*color picker used by menu for waypoint color*/
	public BufferedImage colorPicker;
	
	/*reference to it so I don't continually allocate it, and can delete it*/
	public int mapImageInt = -1;

	/*Block colour array*/
	public int[] blockColors = new int[65536]; // 4096 good enough for 256 blocks.  for 4096 blocks need 65536
	//public String[] blockNames = new String[65536]; // 4096 good enough for 256 blocks.  for 4096 blocks need 65536

	
	private static int COLOR_NOT_LOADED = 0xffff00ff;
	
	public static int COLOR_FAILED_LOAD = 0xffff01ff;
	
	/* blocks that are rendered as a cross (from above).  mostly plants.  Also fire.  Keep fire?*/
	private Integer[] vegetationIDS = {6, 31, 32, 37, 38, 39, 40, 51, 59, 83, 104, 105, 115, 141, 142};
	
	/* blocks that are not full cubes and need to have their alpha reduced by some amount I'll and up just arbitrarily adding */
	private Integer[] shapedIDS = {63, 68, 64, 65, 71, 77, 85, 106, 107, 113, 139, 143};
	
	/* blocks that we ignore.  too small or I don't care.  buttons, levers, tripwire hook */
	//private Integer[] ignoreIDS = {69, 77, 131, 143};
	
	//public int[] waterColorTints = new int[BiomeGenBase.biomeList.length];
	
	public ArrayList<Integer> biomeTintsAvailable = new ArrayList<Integer>();

	
	public ZanColorManager(ZanMinimap minimap) {
		this.minimap = minimap;
		
		for(int i = 0; i<blockColors.length; i++)
			blockColors[i] = 0xff00ff;
	}
	
	public boolean checkForChanges() {
		if ((pack == null) || !(pack.equals(minimap.game.texturePackList.getSelectedTexturePack()))) {
			pack = minimap.game.texturePackList.getSelectedTexturePack();
			loadColorPicker();
			loadMapImage();
			try {
			//	new Thread(new Runnable() { // load in a thread so we aren't blocking, particularly for giant texture packs
			//		public void run() {
						loadTexturePackColors();
						getCTMcolors();
						getBiomeEnabledBlocks();
						minimap.doFullRender = true; // force rerender with texture pack colors now that they are loaded
						if (minimap.radar != null) {
							minimap.radar.setTexturePack(pack);
							minimap.radar.loadTexturePackIcons();
						}

			//		}
			//	}).start();
			}
			catch (Exception e) {
				//System.out.println("texture pack not ready yet");
			}
			return true;
		}
		return false;
	}
	
	
	// tp, colors, loading thereof

	private final int blockColorID(int blockid, int meta) {
		return (blockid) | (meta << 12);  //  8 is good enough for 256 blocks.  for 4096 blocks need to shift 12
	}

	public final int getBlockColor(int blockid, int meta, boolean transparency) {
		try {
			if (blockColors[blockColorID(blockid, meta)] == COLOR_NOT_LOADED)
				if (transparency)
					blockColors[blockColorID(blockid, meta)] = getColor(terrainBuffTrans, blockid, meta, true);
				else
					blockColors[blockColorID(blockid, meta)] = getColor(terrainBuff, blockid, meta);			
			int col = blockColors[blockColorID(blockid, meta)];
			if (col != COLOR_FAILED_LOAD) 
				return col;
			if (blockColors[blockColorID(blockid, 0)] == COLOR_NOT_LOADED) {
				if (transparency)
					blockColors[blockColorID(blockid, 0)] = getColor(terrainBuffTrans, blockid, 0, true);
				else
					blockColors[blockColorID(blockid, 0)] = getColor(terrainBuff, blockid, 0);
			}
			col = blockColors[blockColorID(blockid, 0)];
			if (col != COLOR_FAILED_LOAD) 
				return col;
			col = blockColors[0];
			if (col != COLOR_FAILED_LOAD) 
				return col;
		}
		catch (ArrayIndexOutOfBoundsException e) {
			//			System.err.println("BlockID: " + blockid + " - Meta: " + meta);
			throw e;
		}
		//		System.err.println("Unable to find a block color for blockid: " + blockid + " blockmeta: " + meta);
		return COLOR_FAILED_LOAD;
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
	
	private void loadMapImage() {
		if (this.mapImageInt != -1)
			minimap.glah(mapImageInt);
		try {
			InputStream is = pack.getResourceAsStream("/misc/mapbg.png");
			java.awt.Image tpMap = ImageIO.read(is);
			is.close();
			BufferedImage mapImage = new BufferedImage(tpMap.getWidth(null), tpMap.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			java.awt.Graphics2D gfx = mapImage.createGraphics();
			gfx.setColor(Color.DARK_GRAY);
			gfx.fillRect(0, 0, mapImage.getWidth(), mapImage.getHeight());
			// Paint the image onto the buffered image
			gfx.drawImage(tpMap, 0, 0, null);
			int border;
			//if (mapImage.getWidth() > 64) // bleed through with filtering fuuuu
			//	border = mapImage.getWidth()*7/128;
			//else
				border = mapImage.getWidth()*8/128;
			gfx.setComposite(AlphaComposite.Clear);
			gfx.fillRect(border, border, mapImage.getWidth()-border*2, mapImage.getHeight()-border*2);
			gfx.dispose();
			this.mapImageInt = minimap.tex(mapImage);
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
	
	private void loadTexturePackColors() {
		try {
			for(int i = 0; i<blockColors.length; i++)
				blockColors[i] = COLOR_NOT_LOADED;

			Icon icon = Block.blocksList[155].getBlockTextureFromSideAndMetadata(1, 0); // 1 is top
			Texture texture = ((TextureStitched)icon).field_94228_a;

			// gives name (minus /textures/blocks and .png) of the image used for this block/face/metadata
			// can use to load block color.  Either all at once, or whenever we encounter a new block (or both)
			//System.out.println(icon.func_94215_i()); 
			//System.out.println(((TextureStitched)icon).func_94211_a() /*x*/ + " " + ((TextureStitched)icon).func_94216_b() + " " + ((TextureStitched)icon).func_94212_f() + " " + ((TextureStitched)icon).func_94210_h());
			//icon = Block.blocksList[153].getBlockTextureFromSideAndMetadata(1, 0); // 1 is top
			//System.out.println(icon.func_94215_i());
			//System.out.println(((TextureStitched)icon).func_94211_a() /*x*/ + " " + ((TextureStitched)icon).func_94216_b() + " " + ((TextureStitched)icon).func_94212_f() + " " + ((TextureStitched)icon).func_94210_h());
			
			//Texture texture = ((TextureStitched)icon).field_94228_a;
			//ByteBuffer buffer = texture.func_94273_h();
			//GLBufferedImage bi = new GLBufferedImage(texture.func_94275_d(), texture.func_94276_e(), BufferedImage.TYPE_4BYTE_ABGR);
			//bi.setBuffer(buffer);
			

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
			//InputStream is = pack.getResourceAsStream("/terrain.png");
			//java.awt.Image terrain = ImageIO.read(is);
			//is.close();
			//terrain = terrain.getScaledInstance(16,16, java.awt.Image.SCALE_SMOOTH);
			
	        BufferedImage terrainStitched = new BufferedImage(texture.func_94275_d(), texture.func_94276_e(), BufferedImage.TYPE_4BYTE_ABGR);
	        ByteBuffer var3 = texture.func_94273_h();
	        byte[] var4 = new byte[texture.func_94275_d() * texture.func_94276_e() * 4];
	        var3.position(0);
	        var3.get(var4);

	        for (int var5 = 0; var5 < texture.func_94275_d(); ++var5)
	        {
	            for (int var6 = 0; var6 < texture.func_94276_e(); ++var6)
	            {
	                int var7 = var6 * texture.func_94275_d() * 4 + var5 * 4;
	                byte var8 = 0;
	                int var10 = var8 | (var4[var7 + 2] & 255) << 0;
	                var10 |= (var4[var7 + 1] & 255) << 8;
	                var10 |= (var4[var7 + 0] & 255) << 16;
	                var10 |= (var4[var7 + 3] & 255) << 24;
	                terrainStitched.setRGB(var5, var6, var10);
	            }
	        }
	        //java.awt.Image terrain = terrainStitched.getScaledInstance(32,16, java.awt.Image.SCALE_SMOOTH);

			
			terrainBuff = new BufferedImage(terrainStitched.getWidth(null), terrainStitched.getHeight(null), BufferedImage.TYPE_INT_RGB);
			java.awt.Graphics gfx = terrainBuff.createGraphics();
		    //Paint the image onto the buffered image
		    gfx.drawImage(terrainStitched, 0, 0, null);
		    gfx.dispose();

		    /**taking into account transparency in the bufferedimage makes stuff like the cobwebs look right.
		     * downside is it gets the actual RGB of water, which can be very bright.
		     * we sort of expect it to be darker (ocean deep, seeing the bottom through it etc)
		     * having non transparent bufferedimage bakes the transparency in, darkening the RGB  
		     * baking it in on cobweb makes it way too dark.  We want the actual pixel color there.
		     * experiment with leaves and ice - I think both look better with transparency baked in there too..
		     * shadows in leaves, stuff under the ice, etc.  So basically of the transparent blocks only cobwebs need real RGB 
		     */
		    terrainBuffTrans = new BufferedImage(terrainStitched.getWidth(null), terrainStitched.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
			gfx = terrainBuffTrans.createGraphics();
		    //Paint the image onto the buffered image
		    gfx.drawImage(terrainStitched, 0, 0, null);
		    gfx.dispose();

			//lily pad (grey in terrain, but always multiplied by same amount)
			blockColors[blockColorID(111, 0)] = colorMultiplier(getColor(terrainBuff, 2, 0), 2129968) | 0xFF000000;
			//tall grass - dead shrub
			blockColors[blockColorID(31, 0)] = colorMultiplier(getColor(terrainBuff, 31, 0), 16777215) | 0xFF000000;
			//cobweb, want terrainBuffTrans for this one
			blockColors[blockColorID(30, 0)] = getColor(terrainBuffTrans, 30, 0) | 0xFF000000;
			loadBiomeColors(minimap.biomes);
			loadLavaColor(terrainBuff);
			
			// kludgy way to load *everything*
			//for(int i = 0; i<blockColors.length; i++)
			//	blockColors[i] = getColor(terrainBuffTrans, i-((i >> 12) << 12), i >> 12, true);
			
		}
		catch (Exception e) {
			System.out.println("ERRRORRR " + e.getLocalizedMessage());
			e.printStackTrace();
		}
		
	}
	
	public void loadBiomeColors(boolean biomes) {

		if (biomes) {
			//grass
			blockColors[blockColorID(2, 0)] = getColor(terrainBuff, 2, 0);
			//leaves
			blockColors[blockColorID(18, 0)] = getColor(terrainBuff, 18, 0);
			blockColors[blockColorID(18, 1)] = getColor(terrainBuff, 18, 1);
			blockColors[blockColorID(18, 2)] = getColor(terrainBuff, 18, 2);
			blockColors[blockColorID(18, 3)] = getColor(terrainBuff, 18, 3);
			blockColors[blockColorID(18, 4)] = getColor(terrainBuff, 18, 4);
			blockColors[blockColorID(18, 5)] = getColor(terrainBuff, 18, 5);
			blockColors[blockColorID(18, 6)] = getColor(terrainBuff, 18, 6);
			blockColors[blockColorID(18, 7)] = getColor(terrainBuff, 18, 7);
			blockColors[blockColorID(18, 8)] = getColor(terrainBuff, 18, 8);
			blockColors[blockColorID(18, 9)] = getColor(terrainBuff, 18, 9);
			blockColors[blockColorID(18, 10)] = getColor(terrainBuff, 18, 10);
			blockColors[blockColorID(18, 11)] = getColor(terrainBuff, 18, 11);
			// tall grass
			blockColors[blockColorID(31, 1)] = getColor(terrainBuffTrans, 31, 1, true);
			blockColors[blockColorID(31, 2)] = getColor(terrainBuffTrans, 31, 2, true);
			//vines
			blockColors[blockColorID(106, 1)] = getColor(terrainBuffTrans, 106, 0, true);
			blockColors[blockColorID(106, 2)] = getColor(terrainBuffTrans, 106, 1, true);
			blockColors[blockColorID(106, 4)] = getColor(terrainBuffTrans, 106, 2, true);
			blockColors[blockColorID(106, 8)] = getColor(terrainBuffTrans, 106, 3, true);
			blockColors[blockColorID(106, 9)] = getColor(terrainBuffTrans, 106, 3, true);
		}
		else { // apply a default color
			//grass
			blockColors[blockColorID(2, 0)] = colorMultiplier(getColor(terrainBuff, 2, 0), ColorizerGrass.getGrassColor(0.7, 0.8)) | 0xFF000000;
			//leaves
			blockColors[blockColorID(18, 0)] = colorMultiplier(getColor(terrainBuff, 18, 0), ColorizerFoliage.getFoliageColor(0.7,  0.8)) | 0xFF000000;
			blockColors[blockColorID(18, 1)] = colorMultiplier(getColor(terrainBuff, 18, 1), ColorizerFoliage.getFoliageColorPine()) | 0xFF000000;
			blockColors[blockColorID(18, 2)] = colorMultiplier(getColor(terrainBuff, 18, 2), ColorizerFoliage.getFoliageColorBirch()) | 0xFF000000;
			blockColors[blockColorID(18, 3)] = colorMultiplier(getColor(terrainBuff, 18, 3), ColorizerFoliage.getFoliageColor(0.7,  0.8)) | 0xFF000000;
			blockColors[blockColorID(18, 4)] = colorMultiplier(getColor(terrainBuff, 18, 4), ColorizerFoliage.getFoliageColor(0.7,  0.8)) | 0xFF000000;
			blockColors[blockColorID(18, 5)] = colorMultiplier(getColor(terrainBuff, 18, 5), ColorizerFoliage.getFoliageColorPine()) | 0xFF000000;
			blockColors[blockColorID(18, 6)] = colorMultiplier(getColor(terrainBuff, 18, 6), ColorizerFoliage.getFoliageColorBirch()) | 0xFF000000;
			blockColors[blockColorID(18, 7)] = colorMultiplier(getColor(terrainBuff, 18, 7), ColorizerFoliage.getFoliageColor(0.7,  0.8)) | 0xFF000000;
			blockColors[blockColorID(18, 8)] = colorMultiplier(getColor(terrainBuff, 18, 8), ColorizerFoliage.getFoliageColor(0.7,  0.8)) | 0xFF000000;
			blockColors[blockColorID(18, 9)] = colorMultiplier(getColor(terrainBuff, 18, 9), ColorizerFoliage.getFoliageColorPine()) | 0xFF000000;
			blockColors[blockColorID(18, 10)] = colorMultiplier(getColor(terrainBuff, 18, 10), ColorizerFoliage.getFoliageColorBirch()) | 0xFF000000;
			blockColors[blockColorID(18, 11)] = colorMultiplier(getColor(terrainBuff, 18, 11), ColorizerFoliage.getFoliageColor(0.7,  0.8)) | 0xFF000000;
			//tall grass
			blockColors[blockColorID(31, 1)] = colorMultiplier(getColor(terrainBuffTrans, 31, 1, true), ColorizerGrass.getGrassColor(0.7, 0.8));
			blockColors[blockColorID(31, 2)] = colorMultiplier(getColor(terrainBuffTrans, 31, 2, true), ColorizerGrass.getGrassColor(0.7, 0.8));
			// vines
			blockColors[blockColorID(106, 1)] = colorMultiplier(getColor(terrainBuffTrans, 106, 0, true), ColorizerFoliage.getFoliageColor(0.7,  0.8));
			blockColors[blockColorID(106, 2)] = colorMultiplier(getColor(terrainBuffTrans, 106, 1, true), ColorizerFoliage.getFoliageColor(0.7,  0.8));
			blockColors[blockColorID(106, 4)] = colorMultiplier(getColor(terrainBuffTrans, 106, 2, true), ColorizerFoliage.getFoliageColor(0.7,  0.8));
			blockColors[blockColorID(106, 8)] = colorMultiplier(getColor(terrainBuffTrans, 106, 3, true), ColorizerFoliage.getFoliageColor(0.7,  0.8));
			blockColors[blockColorID(106, 9)] = colorMultiplier(getColor(terrainBuffTrans, 106, 3, true), ColorizerFoliage.getFoliageColor(0.7,  0.8));
		}
		
		loadWaterColor(minimap.waterTransparency, biomes);
	}
	
	public void loadWaterColor(boolean transparency, boolean biomes) {
		loadWaterColor(transparency?terrainBuffTrans:terrainBuff, biomes);
	}
	
    private void loadWaterColor(BufferedImage image, boolean biomes) {
    	try {
    		int waterBase;
    		int waterRGB = -1;
    		//int waterBase = -1;
    		InputStream is = pack.getResourceAsStream("/anim/custom_water_still.png");
    		if (is == null) { 
    			is = pack.getResourceAsStream("/custom_water_still.png");
    		}
    		if (is == null) {
    			if (minimap.waterTransparency) {
        			Icon icon = Block.blocksList[9].getBlockTextureFromSideAndMetadata(1, 0); // 1 is top
        		    int left = (int)(icon.func_94209_e()*image.getWidth());
        		    int right = (int)(icon.func_94212_f()*image.getWidth());
        		    int top = (int)(icon.func_94206_g()*image.getHeight());
        		    int bottom = (int)(icon.func_94210_h()*image.getHeight());
        		    		
        		    BufferedImage blockTexture = image.getSubimage(left, top, right-left, bottom-top);
        		    java.awt.Image singlePixel = blockTexture.getScaledInstance(1, 1, java.awt.Image.SCALE_SMOOTH);
        		    
        		    BufferedImage singlePixelBuff = new BufferedImage(1, 1, image.getType());
        			java.awt.Graphics gfx = singlePixelBuff.createGraphics();
        		    //Paint the image onto the buffered image
        		    gfx.drawImage(singlePixel, 0, 0, null);
        		    gfx.dispose();
        		    
    				waterBase = singlePixelBuff.getRGB(0, 0); // the and dumps the alpha, in hex the ff at the beginning
    				//waterAlpha = waterBase >> 24 & 255;
    				//waterBase = waterBase & 0x00FFFFFF;
    			}
    			else {
        			waterBase = getColor(image, 9, 0);
    				//waterAlpha = 180;
    			}
    		}
    		else {
    			java.awt.Image water = ImageIO.read(is);
    			is.close();
    			water = water.getScaledInstance(1,1, java.awt.Image.SCALE_SMOOTH);
    			BufferedImage waterBuff = new BufferedImage(water.getWidth(null), water.getHeight(null), image.getType());
    			java.awt.Graphics gfx = waterBuff.createGraphics();
    			// Paint the image onto the buffered image
    			gfx.drawImage(water, 0, 0, null);
    			gfx.dispose();
    			waterBase = waterBuff.getRGB(0, 0);
   				//waterAlpha = waterBase >> 24 & 255;
    			//waterBase = waterBase & 0x00FFFFFF; 
    		}
    		/*int t = 0;
    		while (BiomeGenBase.biomeList[t] != null) {
    			waterColorTints[t] = BiomeGenBase.biomeList[t].waterColorMultiplier;
    			t++;
    		}
    		int waterMult = -1;
    		BufferedImage waterColorBuff = null;
    		is = pack.getResourceAsStream("/misc/watercolorX.png");
    		if (is != null) {
    			java.awt.Image waterColor = ImageIO.read(is);
    			is.close();
    			waterColorBuff = new BufferedImage(waterColor.getWidth(null), waterColor.getHeight(null), BufferedImage.TYPE_INT_RGB);
    			java.awt.Graphics gfx = waterColorBuff.createGraphics();
    			// Paint the image onto the buffered image
    			gfx.drawImage(waterColor, 0, 0, null);
    			gfx.dispose();
    			t = 0;
        		while (BiomeGenBase.biomeList[t] != null) {
            		BiomeGenBase genBase = BiomeGenBase.biomeList[t];
            		double var1 = (double)MathHelper.clamp_float(genBase.getFloatTemperature(), 0.0F, 1.0F);
            		double var2 = (double)MathHelper.clamp_float(genBase.getFloatRainfall(), 0.0F, 1.0F);
            		var2=var2*var1;
            		var1=1D-var1;
            		var2=1D-var2;
        			waterMult = waterColorBuff.getRGB((int)((waterColorBuff.getWidth()-1)*var1), (int)((waterColorBuff.getHeight()-1)*var2)) & 0x00FFFFFF;
            		if (waterMult != -1 && waterMult != 0)
            			waterColorTints[t] = waterMult;
        			t++;
        		}
    		}
    		if (!minimap.biomes) // && != 0 cause some packs (ravands!) have completely transparent areas in watercolorX (ie no multiplier applied at all most of the time)
    			waterRGB = this.colorMultiplier(waterBase, waterColorTints[BiomeGenBase.forest.biomeID] | 0xFF000000);
    		*/
    		if (!biomes) {// && != 0 cause some packs (ravands!) have completely transparent areas in watercolorX (ie no multiplier applied at all most of the time)
    			int waterMult = -1;
        		BufferedImage waterColorBuff = null;
        		is = pack.getResourceAsStream("/misc/watercolorX.png");
        		if (is != null) {
        			java.awt.Image waterColor = ImageIO.read(is);
        			is.close();
        			waterColorBuff = new BufferedImage(waterColor.getWidth(null), waterColor.getHeight(null), BufferedImage.TYPE_INT_RGB);
        			java.awt.Graphics gfx = waterColorBuff.createGraphics();
        			// Paint the image onto the buffered image
        			gfx.drawImage(waterColor, 0, 0, null);
        			gfx.dispose();
            		BiomeGenBase genBase = BiomeGenBase.forest;
            		double var1 = (double)MathHelper.clamp_float(genBase.getFloatTemperature(), 0.0F, 1.0F);
            		double var2 = (double)MathHelper.clamp_float(genBase.getFloatRainfall(), 0.0F, 1.0F);
            		var2=var2*var1;
            		var1=1D-var1;
            		var2=1D-var2;
        			waterMult = waterColorBuff.getRGB((int)((waterColorBuff.getWidth()-1)*var1), (int)((waterColorBuff.getHeight()-1)*var2)) & 0x00FFFFFF;
        		}
        		if (waterMult != -1 && waterMult != 0)
        			waterRGB = this.colorMultiplier(waterBase,  waterMult | 0xFF000000);
        		else
        			waterRGB = this.colorMultiplier(waterBase, BiomeGenBase.forest.waterColorMultiplier | 0xFF000000);
    		}
    		else
    			waterRGB = waterBase;
    		for (int t = 0; t < 16; t++) {
    			blockColors[blockColorID(8, t)] = waterRGB;
    			blockColors[blockColorID(9, t)] = waterRGB;
    		}
    	} 
    	catch (Exception e) {
    		minimap.chatInfo("§EError Loading Water Color, using defaults");
    		for (int t = 0; t < 16; t++) {
    			blockColors[blockColorID(8, t)] = 0x2f51ff;
    			blockColors[blockColorID(9, t)] = 0x2f51ff;
    		}
    	}
    }
    
    private void loadLavaColor(BufferedImage terrainBuff) {
    	try {
    		int lavaRGB = -1;
    		InputStream is = pack.getResourceAsStream("/anim/custom_lava_still.png");
    		if (is == null) { 
    			is = pack.getResourceAsStream("/custom_lava_still.png");
    		}
    		if (is == null) {
    			lavaRGB = getColor(terrainBuff, 11, 0);
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
    			lavaRGB = lavaBuff.getRGB(0, 0) | 0xFF000000;
    		}
    		for (int t = 0; t < 16; t++) {
    			blockColors[blockColorID(10, t)] = lavaRGB;
    			blockColors[blockColorID(11, t)] = lavaRGB;
    		}
    	} 
    	catch (Exception e) {
    		minimap.chatInfo("§EError Loading Lava Color, using defaults");
    		for (int t = 0; t < 16; t++) {
    			blockColors[blockColorID(10, t)] = 0xecad41;
    			blockColors[blockColorID(11, t)] = 0xecad41;
    		}
    	}
    }
	
	// default to not retaining transparency
	private int getColor(BufferedImage image, int blockID, int metadata) {
		return getColor(image, blockID, metadata, false);
	}
	
	private int getColor(BufferedImage image, int blockID, int metadata, boolean retainTransparency) {
		try {
			int side = 1;
			if (Arrays.asList(vegetationIDS).contains(blockID)) // if this is a plant, there is no top
				side = 2;
			Icon icon = null;
			if (blockID == 64 || blockID == 71)
				icon = Block.blocksList[blockID].getBlockTexture(this.minimap.getWorld(), 10, 64, 10, 0);
			else if (blockID == 55) {
		        return (25 & 255) << 24 | ((30 + metadata*15) & 255) << 16 | (0 & 255) << 8 | 0 & 255;
				// TODO get redstone icon
			}
			// TODO get cauldron and hopper (need to add interior to top)
			else
				icon = Block.blocksList[blockID].getBlockTextureFromSideAndMetadata(side, metadata); // 1 is top
			
			/*if (icon != null) {
				((TextureStitched)icon).func_94219_l();
				Texture texture = ((TextureStitched)icon).field_94228_a;
				BufferedImage terrainStitched = new BufferedImage(texture.func_94275_d(), texture.func_94276_e(), BufferedImage.TYPE_4BYTE_ABGR);
				ByteBuffer var3 = texture.func_94273_h();
				byte[] var4 = new byte[texture.func_94275_d() * texture.func_94276_e() * 4];
				synchronized (var3) {
					int pos = var3.position();
					var3.position(0);
					var3.get(var4);
					var3.position(pos);
				}

				for (int var5 = 0; var5 < texture.func_94275_d(); ++var5)
				{
					for (int var6 = 0; var6 < texture.func_94276_e(); ++var6)
					{
						int var7 = var6 * texture.func_94275_d() * 4 + var5 * 4;
						byte var8 = 0;
						int var10 = var8 | (var4[var7 + 2] & 255) << 0;
						var10 |= (var4[var7 + 1] & 255) << 8;
						var10 |= (var4[var7 + 0] & 255) << 16;
						var10 |= (var4[var7 + 3] & 255) << 24;
						terrainStitched.setRGB(var5, var6, var10);
					}
				}

				//java.awt.Image terrain = terrainStitched.getScaledInstance(32,16, java.awt.Image.SCALE_SMOOTH);


				terrainBuff = new BufferedImage(terrainStitched.getWidth(null), terrainStitched.getHeight(null), BufferedImage.TYPE_INT_RGB);
				java.awt.Graphics gfx = terrainBuff.createGraphics();
				//Paint the image onto the buffered image
				gfx.drawImage(terrainStitched, 0, 0, null);
				gfx.dispose();
			}*/
			
			/*
			if (blockID == 17) {
				System.out.println("wood, icon is: " + icon.func_94215_i());
				System.out.println("wood, texture is: " + ((TextureStitched)icon).field_94228_a.func_94280_f());
			}*/
			
			//blockNames[blockColorID(blockID, metadata)] = icon.func_94215_i(); // TODO refactor
			
			int left = (int)(icon.func_94209_e()*image.getWidth());
			int right = (int)(icon.func_94212_f()*image.getWidth());
			int top = (int)(icon.func_94206_g()*image.getHeight());
			int bottom = (int)(icon.func_94210_h()*image.getHeight());

			BufferedImage blockTexture = image.getSubimage(left, top, right-left, bottom-top);
			//System.out.println(blockID + " " + metadata + " " + this.blockColorID(blockID, metadata));
			//System.out.println("dims: " + blockTexture.getWidth() + " " + blockTexture.getHeight());
			java.awt.Image singlePixel = blockTexture.getScaledInstance(1, 1, java.awt.Image.SCALE_SMOOTH);

			//System.out.println(blockID + " " + left + " " + top);

			BufferedImage singlePixelBuff = new BufferedImage(1, 1, image.getType());
			java.awt.Graphics gfx = singlePixelBuff.createGraphics();
			//Paint the image onto the buffered image
			gfx.drawImage(singlePixel, 0, 0, null);
			gfx.dispose();
			
			int color = singlePixelBuff.getRGB(0, 0);
			
			if (Arrays.asList(shapedIDS).contains(blockID)) // not a cube, we'll reduce its opacity accordingly
				color = applyShape(blockID, metadata, color);
			
			if (retainTransparency)
				return color;
			else
				return (color | 0xFF000000); // the or dumps the alpha, in hex the ff at the beginning (this sets it to 255, where the below sets it to 0)
				//return (color & 0x00FFFFFF); // the and dumps the alpha, in hex the ff at the beginning
		}
		catch (Exception e) {
			System.out.println("failed getting color: " + blockID + " " + metadata);
			return COLOR_FAILED_LOAD;
		}
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
		
		return (image.getRGB(texX, texY) | 0xFF000000); // the and dumps the alpha, in hex the ff at the beginning
	}
	
	private int applyShape(int blockID, int metadata, int color) {
    	int alpha = (color >> 24 & 255);
        int red = (color >> 16 & 255);
        int green = (color >> 8 & 255);
        int blue = (color >> 0 & 255);
		switch (blockID) {
            case 63: // sign
                alpha = 255/8;
                break;
                
            case 68: // wall sign
                alpha = 255/8;
                break;
                
            case 64: // wooden door
                alpha = (int)(255/5.33);
                break;
                
            case 65: // ladder
                alpha = 255/16;
                break;
                
            case 71: // iron door
                alpha = (int)(255/5.33);
                break;
                
            case 77: // stone button
                alpha = (int)(255/21.33);
                break;
                
            case 85: // fence
            	alpha = (int)(255/2.66);  // 6.4 strictly
                break;
                
            case 106: // vines
                alpha = 255/16;
                break;
            	
            case 107: // fence gate
            	alpha = (int)(255/2.75); // 8 strictly
                break;
            	
            case 113: // nether fence
            	alpha = (int)(255/2.66); // 6.4 strictly
                break;

            case 139: // cobblestone wall
            	alpha = (int)(255/1.66); //2.66 strictly
                break;
            	
            case 143: // wood button
                alpha = (int)(255/21.33);
                break;
        }
        color = (alpha & 255) << 24 | (red & 255) << 16 | (green & 255) << 8 | blue & 255;
		return color;
	}
	
    public int colorMultiplier(int color1, int color2)
    {
    	int alpha1 = (color1 >> 24 & 255);
        int red1 = (color1 >> 16 & 255);
        int green1 = (color1 >> 8 & 255);
        int blue1 = (color1 >> 0 & 255);

    	int alpha2 = (color2 >> 24 & 255);
        int red2 = (color2 >> 16 & 255);
        int green2 = (color2 >> 8 & 255);
        int blue2 = (color2 >> 0 & 255);
        
        int alpha = alpha1 * alpha2 / 255;
        int red = red1 * red2 / 255;
        int green = green1 * green2 / 255;
        int blue = blue1 * blue2 / 255;
        
        
        return (alpha & 255) << 24 | (red & 255) << 16 | (green & 255) << 8 | blue & 255;
       // this.red = (float)(var1 >> 16 & 255) * 0.003921569F * var2;
       // this.green = (float)(var1 >> 8 & 255) * 0.003921569F * var2;
       // this.blue = (float)(var1 >> 0 & 255) * 0.003921569F * var2;

    }
    
    public int colorAdder(int color1, int color2) {
    	int topAlpha = (int)((color1 >> 24 & 255));// * waterAlpha/256);
        int red1 = (int)((color1 >> 16 & 255) * topAlpha/255);
        int green1 = (int)((color1 >> 8 & 255) * topAlpha/255);
        int blue1 = (int)((color1 >> 0 & 255) * topAlpha/255);

        int red2 = (int)((color2 >> 16 & 255)* (255-topAlpha)/255);
        int green2 = (int)((color2 >> 8 & 255)* (255-topAlpha)/255);
        int blue2 = (int)((color2 >> 0 & 255)* (255-topAlpha)/255);
        
        int red = red1 + red2;// / 2;
        int green = green1 + green2;// / 2;
        int blue = blue1 + blue2;// / 2;
       
        return 255 << 24 | (red & 255) << 16 | (green & 255) << 8 | blue & 255;
       // this.red = (float)(var1 >> 16 & 255) * 0.003921569F * var2;
       // this.green = (float)(var1 >> 8 & 255) * 0.003921569F * var2;
       // this.blue = (float)(var1 >> 0 & 255) * 0.003921569F * var2;
	}
    
    // ctm stuff
    
    private void getCTMcolors() {
    	for (String s : listResources("/ctm", ".properties")) {
    		try {
    			loadCTM(s);
    		}
            catch (NumberFormatException e) {
            	// nothing, continue loop
            }
    		catch (IllegalArgumentException e) {
    			
    		}
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

    	int blockID = -1;
    	int metadataInt = 0;
    	String directory ="";
    	String blockName = "";
    	filePath = filePath.toLowerCase();
    	Pattern pattern = Pattern.compile(".*/block([\\d]+)[a-z]*.properties");
    	Matcher matcher = pattern.matcher(filePath);
    	if (matcher.find()) {
    		blockID = Integer.parseInt(matcher.group(1));
    		directory = filePath.substring(0, filePath.lastIndexOf("/")) + "/";
    		//System.out.println(filePath + " " + blockID);
    	}
 //   	else {
 //   		pattern = Pattern.compile(".*/(([a-z_]*)(?:_([\\d]+))?).properties");
 /*   		matcher = pattern.matcher(filePath);
    		if (matcher.find()) {
    			if (matcher.group(3) != null)
    				metadataInt = Integer.parseInt(matcher.group(3));
    			blockName = matcher.group(1);
    			System.out.println("find blockname " + blockName);
    			for (int t = 0; t < blockNames.length; t++) {
    				if (blockNames[t]!=null && blockName.equals(blockNames[t].toLowerCase())) {
    					metadataInt = (t >> 12);
    					blockID = t-((t >> 12) << 12);
    		    		filename = filePath.replaceAll(".properties", "/0.png");
    					System.out.println(blockName + " " + blockID + " " + metadataInt);
    				}
    				//blockID = t;
    			}
    		}

    	}*/
    	if (blockID == -1)
    		return;

        String method = properties.getProperty("method", "").trim().toLowerCase();
        String faces = properties.getProperty("faces", "").trim().toLowerCase();
        String metadata = properties.getProperty("metadata", "0").trim().toLowerCase();
        String tiles = properties.getProperty("tiles", "").trim().toLowerCase();
        
        if (metadataInt == 0) // might have got it from the filename
        	metadataInt = Integer.parseInt( metadata);
        
        int[] tilesInts = parseIntegerList(tiles, 0, 255);
        int tilesInt = 0;
        if (tilesInts.length > 0) {
        	tilesInt = tilesInts[0];
        }

        if (method.equals("sandstone") || method.equals("top") || faces.contains("top")) {
        	try {
        		InputStream is = pack.getResourceAsStream(directory + tilesInt + ".png");
        		java.awt.Image top = ImageIO.read(is);
        		is.close();
        		top = top.getScaledInstance(1,1, java.awt.Image.SCALE_SMOOTH);
        		BufferedImage topBuff = new BufferedImage(top.getWidth(null), top.getHeight(null), BufferedImage.TYPE_INT_RGB);
        		java.awt.Graphics gfx = topBuff.createGraphics();
        		// Paint the image onto the buffered image
        		gfx.drawImage(top, 0, 0, null);
        		gfx.dispose();
        		int topRGB = topBuff.getRGB(0, 0);
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
                } 
                else if (token.matches("^\\d+-\\d+$")) {
                    String[] t = token.split("-");
                    int min = Integer.parseInt(t[0]);
                    int max = Integer.parseInt(t[1]);
                    for (int i = min; i <= max; i++) {
                        tmpList.add(i);
                    }
                }
                else if (token.matches("^\\d+:\\d+$")) {
                	String[] t = token.split(":");
                	int id = Integer.parseInt(t[0]);
                	int metadata = Integer.parseInt(t[1]);
                	tmpList.add(id);
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
        	File root = new File(folder, directory);

            fileWalk(root, directory, suffix, resources); 
        }

        Collections.sort(resources);
        return resources.toArray(new String[resources.size()]);
    }

    private void fileWalk(File file, String folder, String suffix, ArrayList resources) {
        File[] children = file.listFiles();
        for (File child : children) {
            if ( child.isDirectory() ) 
                fileWalk(child, folder, suffix, resources);
            else if (child.getPath().endsWith(suffix)) {
            	String path = child.getPath().replace('\\', '/');
            	path = path.substring(path.indexOf(folder));
        		resources.add("/" + path);
            }
        }
    }
    
    private void getBiomeEnabledBlocks() {
        java.util.Properties properties = new java.util.Properties();
        InputStream input = pack.getResourceAsStream("/color.properties");
        try {
        	if (input != null) {
        		properties.load(input);
                input.close();
        	}
        } 
        catch (IOException e) {
        	return;
        }
        for (Enumeration e = properties.propertyNames() ; e.hasMoreElements() ;) {
        	String key = (String)e.nextElement();
        	if (key.startsWith("palette.block")) {
        		int[] ids = parseIntegerList(properties.getProperty(key), 0, 4096);
        		for (int t = 0; t < ids.length; t++) {
        			this.biomeTintsAvailable.add(ids[t]);
        		}
        	}
        }

        String method = properties.getProperty("method", "").trim().toLowerCase();
        
    }
    
}
