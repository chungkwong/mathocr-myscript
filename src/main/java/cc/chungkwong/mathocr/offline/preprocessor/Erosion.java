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
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class Erosion extends SimplePreprocessor{
	private final int windowSize;
	private final BitSet template;
	public Erosion(BitSet template){
		this.template=template;
		int size=1;
		while(size*size<=template.size()){
			size+=2;
		}
		this.windowSize=size-2;
	}
	@Override
	public void preprocess(byte[] from,byte[] to,int width,int height){
		int r=windowSize/2;
		for(int i=r;i<height-r;i++){
			outer:
			for(int j=r, ind=i*width+j;j<width-r;j++,ind++){
				for(int k=-r, tind=0;k<=r;k++){
					for(int l=-r;l<=r;l++,tind++){
						if(template.get(tind)&&from[ind+k*width+l]!=0){
							continue outer;
						}
					}
				}
				to[ind]=0x00;
			}
		}
	}
}
