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
public class MeanFilter extends SimplePreprocessor{
	/**
	 * Construct a MeanFilter
	 */
	public MeanFilter(){
	}
	@Override
	public void preprocess(byte[] from,byte[] to,int width,int height){
		byte[] prev=new byte[width];
		System.arraycopy(from,0,prev,0,width);
		for(int i=1, start=width;i<height-1;i++){
			byte tmp=from[start], tmp2;
			to[start++]=tmp;
			for(int j=1;j<width-1;j++,start++){
				tmp2=(byte)(((prev[j-1]&0xFF)+(prev[j]&0xFF)+(prev[j+1]&0xFF)
						+(tmp&0xFF)+(from[start]&0xFF)+(from[start+1]&0xFF)
						+(from[start+width-1]&0xFF)+(from[start+width]&0xFF)+(from[start+width+1]&0xFF))/9);
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
