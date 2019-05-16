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
package cc.chungkwong.mathocr.offline.preprocessor;
import cc.chungkwong.mathocr.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class Inverter extends SimplePreprocessor{
	private final boolean autoDetect;
	/**
	 * Construct a ColorInvert
	 */
	public Inverter(){
		this.autoDetect=Settings.DEFAULT.getBoolean("DETECT_INVERT");
	}
	/**
	 * Construct a ColorInvert
	 *
	 * @param autoDetect color will only be inverted when the image seem to be
	 * white on black if autoDetect is true, or else color will always be
	 * inverted
	 */
	public Inverter(boolean autoDetect){
		this.autoDetect=autoDetect;
	}
	/**
	 * Check if auto detection of white on black is enabled
	 *
	 * @return enabled or not
	 */
	public boolean isAutoDetect(){
		return autoDetect;
	}
	/**
	 * Check if a image seem to be white on black
	 *
	 * @param pixels the pixels array of the image
	 * @return test result
	 */
	public boolean checkWhiteOnBlack(byte[] pixels){
		int count=0;
		for(byte pix:pixels){
			if((pix&0xFF)<=0x80){
				++count;
			}
		}
		return count>pixels.length/2;
	}
	@Override
	public void preprocess(byte[] from,byte[] to,int width,int height){
		if(!autoDetect||checkWhiteOnBlack(from)){
			for(int i=0;i<to.length;i++){
				to[i]=(byte)(~from[i]);
			}
		}
	}
}
