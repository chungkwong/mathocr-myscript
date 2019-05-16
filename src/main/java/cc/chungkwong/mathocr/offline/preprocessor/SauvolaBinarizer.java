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
import java.awt.color.*;
/**
 * Sauvola's threholding
 *
 * @author Chan Chung Kwong
 */
public class SauvolaBinarizer extends SimplePreprocessor{
	private final double weight;
	private final int window;
	/**
	 * Create an binarizer using global settings
	 */
	public SauvolaBinarizer(){
		this.weight=Settings.DEFAULT.getDouble("SAUVOLA_WEIGHT");
		this.window=Settings.DEFAULT.getInteger("SAUVOLA_WINDOW");
	}
	/**
	 * Create an binarizer
	 *
	 * @param weight weight
	 * @param window side of the sliding windows
	 */
	public SauvolaBinarizer(double weight,int window){
		this.weight=weight;
		this.window=window;
	}
	/**
	 *
	 * @return Weight
	 */
	public double getWeight(){
		return weight;
	}
	/**
	 *
	 * @return side of sliding windows
	 */
	public int getWindow(){
		return window;
	}
	@Override
	public void preprocess(byte[] from,byte[] to,int width,int height){
		//byte[] pixels=((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		for(int i=0;i<to.length;i++){
			to[i]=COLOR[from[i]&0xFF];
		}
		int[] integral=new int[width+1];
		int[] integralSquare=new int[width+1];
		int dl=(window+1)/2, dr=window/2;
		int[][] old=new int[dl][width+1];
		for(int i=0, ind=0, imax=Math.min(height,dr+1);i<imax;i++){
			for(int j=1;j<=width;j++,ind++){
				int pixel=(to[ind])&0xFF;
				integral[j]+=pixel;
				integralSquare[j]+=pixel*pixel;
			}
		}
		int dr1=Math.min(dr,width);
		int dr2=Math.max(width-dr+1,1);
		for(int i=0, ind=0, curr=0;i<height;i++){
			int winTop=Math.max(i-dl,-1), winBottom=Math.min(height-1,i+dr);
			int sum=0;
			int squareSum=0;
			for(int j=1;j<=dr1;j++){
				sum+=integral[j];
				squareSum+=integralSquare[j];
			}
			for(int j=1;j<=width-dr;j++,ind++){
				int winLeft=Math.max(j-dl,0), winRight=j+dr;
				sum+=integral[winRight]-integral[winLeft];
				squareSum+=integralSquare[winRight]-integralSquare[winLeft];
				int pixel=to[ind]&0xFF;
				old[curr][j]=pixel;
				if(pixel!=0xFF&&pixel!=0x00){
					int area=((winBottom-winTop)*(winRight-winLeft));
					double factor=1.0/area;
					double mean=sum*factor;
					double s=Math.sqrt(squareSum*factor-mean*mean);
					int lim=(int)(mean*(1+weight*(s/128-1)));
					to[ind]=pixel<=lim?0x00:(byte)0xff;
				}
			}
			for(int j=dr2;j<=width;j++,ind++){
				int winLeft=Math.max(j-dl,0), winRight=width;
				sum-=integral[winLeft];
				squareSum-=integralSquare[winLeft];
				int pixel=to[ind]&0xFF;
				old[curr][j]=pixel;
				if(pixel!=0xFF&&pixel!=0x00){
					int area=((winBottom-winTop)*(winRight-winLeft));
					double factor=1.0/area;
					double mean=sum*factor;
					double s=Math.sqrt(squareSum*factor-mean*mean);
					int lim=(int)(mean*(1+weight*(s/128-1)));
					to[ind]=pixel<=lim?0x00:(byte)0xff;
				}
			}
			if(++curr==dl){
				curr=0;
			}
			if(i>=dl-1){
				for(int j=1, index=(winTop+1)*width;j<=width;j++,index++){
					int pixel=old[curr][j];
					integral[j]-=pixel;
					integralSquare[j]-=pixel*pixel;
				}
			}
			if(winBottom+1<height){
				for(int j=1, index=(winBottom+1)*width;j<=width;j++,index++){
					int pixel=(to[index])&0xFF;
					integral[j]+=pixel;
					integralSquare[j]+=pixel*pixel;
				}
			}
		}
	}
	private static byte[] COLOR=new byte[256];
	static{
		ColorSpace space=ColorSpace.getInstance(ColorSpace.CS_GRAY);
		for(int i=0;i<256;i++){
			COLOR[i]=(byte)(space.toRGB((new float[]{i/255.0f}))[0]*255);
		}
	}
}
