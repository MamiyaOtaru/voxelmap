package net.minecraft.src.mamiyaotaru;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

import net.minecraft.src.StringTranslate;
import net.minecraft.src.ZanMinimap;

public class MinimapTranslate {
	
	private ZanMinimap minimap;
	private StringTranslate stringtranslate = null;
    private String currentLanguage = "";
	
	public MinimapTranslate(ZanMinimap minimap) {
		this.minimap = minimap;
	}
	
	public void checkForChanges() {
		if (stringtranslate == null) {
			stringtranslate = StringTranslate.getInstance();
		}
		if (!currentLanguage.equals(stringtranslate.getCurrentLanguage())) {
			currentLanguage = stringtranslate.getCurrentLanguage();
			setLanguage(currentLanguage);
		}
	}
	
	public void setLanguage(String language) {
		Object propertiesObj = minimap.getPrivateFieldByType(stringtranslate, Properties.class);
		if (propertiesObj == null) {
			System.out.println("could not get translate table");
			return;
		}
		System.out.println("got translate table");
		Properties properties = (Properties)propertiesObj;
        try {
            this.loadLanguage(properties, "en_US"); // load default
        }
        catch (IOException e) {
        }
        if (!language.equals("en_US")) { // if language is not the default,
        	try {
        		this.loadLanguage(properties, language); // try to load it over the top
        	}
        	catch (IOException e) {
        	}
        }
	}
	
    private void loadLanguage(Properties par1Properties, String par2Str) throws IOException
    {
    	InputStream is = StringTranslate.class.getResourceAsStream("/mamiyaotaru/lang/" + par2Str + ".lang");
    	if (is == null)
    		return;
    	InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        BufferedReader in = new BufferedReader(isr);

        for (String line = in.readLine(); line != null; line = in.readLine())
        {
            line = line.trim();

            if (!line.startsWith("#"))
            {
                String[] pair = line.split("=");

                if (pair != null && pair.length == 2)
                {
                    par1Properties.setProperty(pair[0], pair[1]);
                }
            }
        }
    }

}
