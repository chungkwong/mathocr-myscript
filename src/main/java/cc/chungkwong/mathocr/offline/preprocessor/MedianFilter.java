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
/**
 *
 * @author Chan Chung Kwong
 */
public class MedianFilter extends SimplePreprocessor{
	/**
	 * Construct a MedianFilter
	 */
	public MedianFilter(){
	}
	@Override
	public void preprocess(byte[] from,byte[] to,int width,int height){
		byte[] prev=new byte[width], sort=new byte[9];
		System.arraycopy(from,0,prev,0,width);
		for(int i=1, start=width;i<height-1;i++){
			byte tmp=from[start], tmp2, tmp3;
			to[start++]=tmp;
			for(int j=1;j<width-1;j++,start++){
				sort[0]=prev[j-1];
				sort[1]=prev[j];
				sort[2]=prev[j+1];
				sort[3]=tmp;
				sort[4]=from[start];
				sort[5]=from[start+1];
				sort[6]=from[start+width-1];
				sort[7]=from[start+width];
				sort[8]=from[start+width+1];
				prev[j-1]=tmp;
				for(int k=0;k<5;k++){
					for(int l=k+1;l<9;l++){
						if((sort[l]&0xFF)<(sort[k]&0xFF)){
							tmp3=sort[k];
							sort[k]=sort[l];
							sort[l]=tmp3;
						}
					}
				}
				tmp2=sort[4];
				prev[j-1]=tmp;
				tmp=from[start];
				to[start]=tmp2;
			}
			prev[width-2]=tmp;
			tmp=prev[width-1]=from[start];
			to[start++]=tmp;
		}
	}
}
