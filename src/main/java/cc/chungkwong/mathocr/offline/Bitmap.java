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
package cc.chungkwong.mathocr.offline;
import java.awt.image.*;
import java.util.*;
/**
 * Binary bitmap image
 *
 * @author Chan Chung Kwong
 */
public class Bitmap{
	private final byte[] data;
	private final int width;
	private final int height;
	/**
	 * Create a image
	 *
	 * @param data pixels of the image where black is marked 0
	 * @param width width
	 * @param height height
	 */
	public Bitmap(byte[] data,int width,int height){
		this.data=data;
		this.width=width;
		this.height=height;
	}
	/**
	 * Create a image
	 *
	 * @param image source of pixels
	 */
	public Bitmap(BufferedImage image){
		int width=image.getWidth();
		int height=image.getHeight();
		byte[] rgb=((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		if(isPaddingNeeded(rgb,width,height)){
			System.out.println("padding");
			data=new byte[((width+2)*(height+2))];
			Arrays.fill(data,(byte)0xFF);
			for(int i=0, k=0, ind=width+2;i<height;i++){
				++ind;
				for(int j=0;j<width;j++,ind++,k++){
					if(rgb[k]==0){
						data[ind]=0;
					}
				}
				++ind;
			}
			this.width=width+2;
			this.height=height+2;
		}else{
			data=new byte[(width*height)];
			Arrays.fill(data,(byte)0xFF);
			for(int i=0, ind=0;i<height;i++){
				for(int j=0;j<width;j++,ind++){
					if(rgb[ind]==0x0){
						data[ind]=0;
					}
				}
			}
			this.width=width;
			this.height=height;
		}
	}
	private boolean isPaddingNeeded(byte[] rgb,int width,int height){
		for(int j=0;j<width;j++){
			if(rgb[j]==0){
				return true;
			}
		}
		for(int i=1, ind=width;i<height;i++,ind+=width){
			if(rgb[ind]==0||rgb[ind-1]==0){
				return true;
			}
		}
		for(int j=1, ind=width*height-1;j<width;j++,--ind){
			if(rgb[ind]==0){
				return true;
			}
		}
		return false;
	}
	/**
	 *
	 * @return pixels
	 */
	public byte[] getData(){
		return data;
	}
	/**
	 *
	 * @return height
	 */
	public int getHeight(){
		return height;
	}
	/**
	 *
	 * @return width
	 */
	public int getWidth(){
		return width;
	}
	@Override
	public String toString(){
		StringBuilder buf=new StringBuilder();
		for(int i=0, k=0;i<height;i++){
			for(int j=0;j<width;j++,k++){
				buf.append(data[k]==0?'x':'o');
			}
			buf.append('\n');
		}
		return buf.toString();
	}
}
