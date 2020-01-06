/*
 * Copyright (C) 2019 Chan Chung Kwong
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cc.chungkwong.mathocr;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.prefs.*;
import javax.swing.*;
/**
 * Global settings
 */
public class Settings{
	/**
	 * Environment that contains currrent global settings
	 */
	public static final Settings DEFAULT;
	private final ResourceBundle translation=ResourceBundle.getBundle("cc.chungkwong.mathocr.message");
	private final HashMap<String,Boolean> boolMap=new HashMap<>();
	private final HashMap<String,Integer> intMap=new HashMap<>();
	private final HashMap<String,Double> doubleMap=new HashMap<>();
	private final HashMap<String,String> strMap=new HashMap<>();
	private boolean changed=false;
	/**
	 * Initialize Environment
	 */
	private Settings(){
	}
	/**
	 * Get the local name corresponding to a code
	 *
	 * @param code the code
	 * @return the local name
	 */
	public String getTranslation(String code){
		try{
			return translation.getString(code);
		}catch(MissingResourceException ex){
			Logger.getGlobal().log(Level.INFO,"",ex);
			return code;
		}
	}
	/**
	 * Get the keys of the boolean properties
	 *
	 * @return the keys
	 */
	public Set<String> getBooleanKeySet(){
		return Collections.unmodifiableSet(boolMap.keySet());
	}
	/**
	 * Get the value of a boolean property
	 *
	 * @param key the key
	 * @return the value
	 */
	public Boolean getBoolean(String key){
		if(!boolMap.containsKey(key)){
			setBoolean(key,Boolean.valueOf(JOptionPane.showInputDialog(key)));
		}
		return boolMap.get(key);
	}
	/**
	 * Set the value of a boolean property
	 *
	 * @param key the key
	 * @param val the value
	 */
	public void setBoolean(String key,Boolean val){
		boolMap.put(key,val);
		changed=true;
	}
	/**
	 * Get the keys of the integer properties
	 *
	 * @return the keys
	 */
	public Set<String> getIntegerKeySet(){
		return Collections.unmodifiableSet(intMap.keySet());
	}
	/**
	 * Get the value of a integer property
	 *
	 * @param key the key
	 * @return the value
	 */
	public Integer getInteger(String key){
		if(!intMap.containsKey(key)){
			setInteger(key,Integer.valueOf(JOptionPane.showInputDialog(key)));
		}
		return intMap.get(key);
	}
	/**
	 * Set the value of a integer property
	 *
	 * @param key the key
	 * @param val the value
	 */
	public void setInteger(String key,Integer val){
		intMap.put(key,val);
		changed=true;
	}
	/**
	 * Get the keys of the doubleing point properties
	 *
	 * @return the keys
	 */
	public Set<String> getDoubleKeySet(){
		return Collections.unmodifiableSet(doubleMap.keySet());
	}
	/**
	 * Get the value of a doubleing point property
	 *
	 * @param key the key
	 * @return the value
	 */
	public Double getDouble(String key){
		if(!doubleMap.containsKey(key)){
			setDouble(key,Double.valueOf(JOptionPane.showInputDialog(key)));
		}
		return doubleMap.get(key);
	}
	/**
	 * Set the value of a doubleing point property
	 *
	 * @param key the key
	 * @param val the value
	 */
	public void setDouble(String key,Double val){
		doubleMap.put(key,val);
		changed=true;
	}
	/**
	 * Get the keys of the string properties
	 *
	 * @return the keys
	 */
	public Set<String> getStringKeySet(){
		return Collections.unmodifiableSet(strMap.keySet());
	}
	/**
	 * Get the value of a string property
	 *
	 * @param key the key
	 * @return the value
	 */
	public String getString(String key){
		if(!strMap.containsKey(key)){
			setString(key,JOptionPane.showInputDialog(key));
		}
		return strMap.get(key);
	}
	/**
	 * Set the value of a string property
	 *
	 * @param key the key
	 * @param val the value
	 */
	public void setString(String key,String val){
		strMap.put(key,val);
		changed=true;
	}
	/**
	 * Restore saved preference to original setting
	 *
	 * @throws java.util.prefs.BackingStoreException
	 */
	public void clearPreference() throws BackingStoreException{
		Preferences pref=Preferences.userNodeForPackage(Settings.class);
		pref.removeNode();
		pref.flush();
		loadFromPreference();
	}
	private void loadFromPreference(){
		try(InputStream in=Settings.class.getResourceAsStream("secret.properties")){
			Properties secret=new Properties();
			secret.load(in);
			secret.entrySet().forEach((e)->strMap.put(Objects.toString(e.getKey()),Objects.toString(e.getValue())));
		}catch(Exception ex){
			Logger.getLogger(Settings.class.getName()).log(Level.SEVERE,null,ex);
		}
		Preferences pref=Preferences.userNodeForPackage(Settings.class);
//		boolMap.put("DETECT_SKEW",pref.getBoolean("DETECT_INVERT",false));
		boolMap.put("DETECT_INVERT",pref.getBoolean("DETECT_INVERT",false));
		boolMap.put("MEDIAN_FILTER",pref.getBoolean("MEDIAN_FILTER",false));
		boolMap.put("MEAN_FILTER",pref.getBoolean("MEAN_FILTER",false));
		boolMap.put("NOISE_REMOVE",pref.getBoolean("NOISE_REMOVE",false));
		intMap.put("KFILL_WINDOW",pref.getInt("KFILL_WINDOW",3));
		intMap.put("NOISE_THREHOLD",pref.getInt("NOISE_THREHOLD",2));
		intMap.put("MANUAL_THREHOLD_LIMIT",pref.getInt("MANUAL_THREHOLD_LIMIT",195));
		intMap.put("SAUVOLA_WINDOW",pref.getInt("SAUVOLA_WINDOW",21));
		intMap.put("DPI",pref.getInt("DPI",384));
		doubleMap.put("SAUVOLA_WEIGHT",pref.getDouble("SAUVOLA_WEIGHT",0.5));
//		strMap.put("SKEW_DETECT_METHOD",pref.get("SKEW_DETECT_METHOD","PP"));
		strMap.put("BINARIZATION_METHOD",pref.get("BINARIZATION_METHOD","SAUVOLA"));
		strMap.putIfAbsent("MYSCRIPT_GRAMMAR",pref.get("MYSCRIPT_GRAMMAR",null));
		String appId=pref.get("MYSCRIPT_APPLICATION",null);
		if(appId!=null){
			strMap.put("MYSCRIPT_APPLICATION",appId);
		}
		String hmac=pref.get("MYSCRIPT_HMAC",null);
		if(hmac!=null){
			strMap.put("MYSCRIPT_HMAC",hmac);
		}
	}
	/**
	 * Save settings as preference
	 */
	public void saveAsPreference(){
		Preferences pref=Preferences.userNodeForPackage(Settings.class);
		for(Map.Entry<String,Boolean> entry:boolMap.entrySet()){
			pref.putBoolean(entry.getKey(),entry.getValue());
		}
		for(Map.Entry<String,Integer> entry:intMap.entrySet()){
			pref.putInt(entry.getKey(),entry.getValue());
		}
		for(Map.Entry<String,Double> entry:doubleMap.entrySet()){
			pref.putDouble(entry.getKey(),entry.getValue());
		}
		for(Map.Entry<String,String> entry:strMap.entrySet()){
			pref.put(entry.getKey(),entry.getValue());
		}
		try{
			pref.flush();
		}catch(Exception ex){
			Logger.getGlobal().log(Level.SEVERE,"",ex);
		}
	}
	static{
		DEFAULT=new Settings();
		DEFAULT.loadFromPreference();
		Runtime.getRuntime().addShutdownHook(new Thread(()->{
			if(DEFAULT.changed){
				DEFAULT.saveAsPreference();
			}
		}));
	}
}
