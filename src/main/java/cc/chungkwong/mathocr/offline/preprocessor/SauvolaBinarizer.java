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
 * Sauvola's threholding
 *
 * @author Chan Chung Kwong
 */
public class SauvolaBinarizer extends SimplePreprocessor{
	private final double k, k2;
	private final int windowWidth;
	private final int windowHeight;
	/**
	 * Create an binarizer using global settings
	 */
	public SauvolaBinarizer(){
		this(Settings.DEFAULT.getDouble("SAUVOLA_WEIGHT"),
				Settings.DEFAULT.getInteger("SAUVOLA_WINDOW"));
	}
	/**
	 * Create an binarizer
	 *
	 * @param weight weight
	 * @param window side of the sliding windows
	 */
	public SauvolaBinarizer(double weight,int window){
		this.windowWidth=window;
		this.windowHeight=window;
		if(weight<0){
			throw new java.lang.IllegalArgumentException("Weight should be positive");
		}
		this.k=weight;
		this.k2=weight*weight/128/128;
	}
	/**
	 *
	 * @return Weight
	 */
	public double getWeight(){
		return k;
	}
	/**
	 *
	 * @return side of sliding windows
	 */
	public int getWindow(){
		return windowWidth;
	}
	@Override
	public void preprocess(byte[] from,byte[] to,int width,int height){
		if(from==to){
			preprocessSafe(to,width,height);
		}else{
			preprocessUnsafe(from,to,width,height);
		}
	}
	private void preprocessSafe(byte[] to,int width,int height){
		int[] integral=new int[width+1];
		int[] integralSquare=new int[width+1];
		int l=(windowWidth+1)/2, r=windowWidth/2;
		int o=(windowHeight+1)/2, u=windowHeight/2;
		for(int i=0, ind=0, imax=Math.min(height,u);i<imax;i++){
			for(int j=1;j<=width;j++,ind++){
				int pixel=(to[ind])&0xFF;
				integral[j]+=pixel;
				integralSquare[j]+=pixel*pixel;
			}
		}
		int dr1=Math.min(r,width);
		int dr2=Math.max(width-r+1,1);
		byte[][] old=new byte[o][width+1];
		for(int i=0, ind=0, curr=-1;i<height;i++){
			int winTop=Math.max(i-o,-1), winBottom=Math.min(height-1,i+u);
			if(++curr==o){
				curr=0;
			}
			if(i>=o){
				for(int j=1;j<=width;j++){
					int pixel=old[curr][j]&0xFF;
					integral[j]-=pixel;
					integralSquare[j]-=pixel*pixel;
				}
			}
			if(i+u<height){
				for(int j=1, index=winBottom*width;j<=width;j++,index++){
					int pixel=(to[index])&0xFF;
					integral[j]+=pixel;
					integralSquare[j]+=pixel*pixel;
				}
			}
			int sum=0;
			int squareSum=0;
			for(int j=1;j<=dr1;j++){
				sum+=integral[j];
				squareSum+=integralSquare[j];
			}
			for(int j=1;j<=width-r;j++,ind++){
				int winLeft=Math.max(j-l,0), winRight=j+r;
				int count=(winBottom-winTop)*(winRight-winLeft);
				sum+=integral[winRight]-integral[winLeft];
				squareSum+=integralSquare[winRight]-integralSquare[winLeft];
				old[curr][j]=to[ind];
				to[ind]=isForeground(to[ind]&0xFF,sum,squareSum,count)?0x00:(byte)0xff;
			}
			for(int j=dr2;j<=width;j++,ind++){
				int winLeft=Math.max(j-l,0), winRight=width;
				int count=(winBottom-winTop)*(winRight-winLeft);
				sum-=integral[winLeft];
				squareSum-=integralSquare[winLeft];
				old[curr][j]=to[ind];
				to[ind]=isForeground(to[ind]&0xFF,sum,squareSum,count)?0x00:(byte)0xff;
			}
		}
	}
	private void preprocessUnsafe(byte[] from,byte[] to,int width,int height){
		int[] integral=new int[width+1];
		int[] integralSquare=new int[width+1];
		int l=(windowWidth+1)/2, r=windowWidth/2;
		int o=(windowHeight+1)/2, u=windowHeight/2;
		for(int i=0, ind=0, imax=Math.min(height,u);i<imax;i++){
			for(int j=1;j<=width;j++,ind++){
				int pixel=(from[ind])&0xFF;
				integral[j]+=pixel;
				integralSquare[j]+=pixel*pixel;
			}
		}
		int dr1=Math.min(r,width);
		int dr2=Math.max(width-r+1,1);
		for(int i=0, ind=0;i<height;i++){
			int winTop=Math.max(i-o,-1), winBottom=Math.min(height-1,i+u);
			if(i>=l){
				for(int j=1, index=winTop*width;j<=width;j++,index++){
					int pixel=from[index]&0xFF;
					integral[j]-=pixel;
					integralSquare[j]-=pixel*pixel;
				}
			}
			if(i+r<height){
				for(int j=1, index=winBottom*width;j<=width;j++,index++){
					int pixel=(from[index])&0xFF;
					integral[j]+=pixel;
					integralSquare[j]+=pixel*pixel;
				}
			}
			int sum=0;
			int squareSum=0;
			for(int j=1;j<=dr1;j++){
				sum+=integral[j];
				squareSum+=integralSquare[j];
			}
			for(int j=1;j<=width-r;j++,ind++){
				int winLeft=Math.max(j-l,0), winRight=j+r;
				int count=(winBottom-winTop)*(winRight-winLeft);
				sum+=integral[winRight]-integral[winLeft];
				squareSum+=integralSquare[winRight]-integralSquare[winLeft];
				to[ind]=isForeground(from[ind]&0xFF,sum,squareSum,count)?0x00:(byte)0xff;
			}
			for(int j=dr2;j<=width;j++,ind++){
				int winLeft=Math.max(j-l,0), winRight=width;
				int count=(winBottom-winTop)*(winRight-winLeft);
				sum-=integral[winLeft];
				squareSum-=integralSquare[winLeft];
				to[ind]=isForeground(from[ind]&0xFF,sum,squareSum,count)?0x00:(byte)0xff;
			}
		}
	}
	private boolean isForeground(int pixel,int sum,int squareSum,int count){
		double mean=((double)sum)/count;
		double variance=((double)squareSum)/count-mean*mean;
		double tmp=(pixel+mean*(k-1));
		return tmp<=0||tmp*tmp<=mean*mean*k2*variance;
	}
}
