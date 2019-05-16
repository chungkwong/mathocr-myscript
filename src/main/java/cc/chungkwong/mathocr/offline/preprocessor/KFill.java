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
public class KFill extends SimplePreprocessor{
	private final int k;
	/**
	 * Construct a Kfill
	 *
	 * @param k length of side of a window, should be a odd number
	 */
	public KFill(int k){
		this.k=k;
	}
	/**
	 * Construct a Kfill with global settings
	 */
	public KFill(){
		this.k=Settings.DEFAULT.getInteger("KFILL_WINDOW");
	}
	/**
	 * @return length of size of window
	 */
	public int getK(){
		return k;
	}
	@Override
	public void preprocess(byte[] from,byte[] to,int width,int height){
		int hk=k/2, len=to.length, k3m4=k*3-4;
		byte[] pixels2=new byte[len];
		System.arraycopy(from,0,to,0,len);
		boolean changed=true;
		while(changed){
			changed=false;
			int ind=0;
			for(int i=0;i<hk;i++){
				for(int j=0;j<width;j++,ind++){
					pixels2[ind]=to[ind];
				}
			}
			for(int i=hk;i<height-hk;i++){
				for(int j=0;j<hk;j++,ind++){
					pixels2[ind]=to[ind];
				}
				for(int j=hk;j<width-hk;j++,ind++){
					if(to[ind]==0x00){
						int n=0, prev=-1, c=0, r=0;
						for(int k=-hk;k<=hk;k++){
							if(to[ind-hk*width+k]!=0x00){
								++n;
								if(prev==1){
									++c;
								}
								prev=0;
							}else{
								prev=1;
							}
						}
						for(int k=-hk+1;k<hk;k++){
							if(to[ind+k*width+hk]!=0x00){
								++n;
								if(prev==1){
									++c;
								}
								prev=0;
							}else{
								prev=1;
							}
						}
						for(int k=hk;k>=-hk;k--){
							if(to[ind+hk*width+k]!=0x00){
								++n;
								if(prev==1){
									++c;
								}
								prev=0;
							}else{
								prev=1;
							}
						}
						for(int k=hk-1;k>-hk;k--){
							if(to[ind+k*width-hk]!=0x00){
								++n;
								if(prev==1){
									++c;
								}
								prev=0;
							}else{
								prev=1;
							}
						}
						if(prev==1&&to[ind-hk*width-hk]!=0x00){
							++c;
						}
						if(to[ind-hk*width-hk]!=0x00){
							++r;
						}else if(to[ind-hk*width+hk]!=0x00){
							++r;
						}else if(to[ind+hk*width-hk]!=0x00){
							++r;
						}else if(to[ind+hk*width+hk]!=0x00){
							++r;
						}
						if(c==1&&(n>k3m4||(n==k3m4&&r==2))){
							changed=true;
							pixels2[ind]=(byte)0xFF;
						}else{
							pixels2[ind]=0x00;
						}
					}else{
						pixels2[ind]=(byte)0xFF;
					}
				}
				for(int j=width-hk;j<width;j++,ind++){
					pixels2[ind]=to[ind];
				}
			}
			for(;ind<len;ind++){
				pixels2[ind]=to[ind];
			}
			ind=0;
			for(int i=0;i<hk;i++){
				for(int j=0;j<width;j++,ind++){
					to[ind]=pixels2[ind];
				}
			}
			for(int i=hk;i<height-hk;i++){
				for(int j=0;j<hk;j++,ind++){
					to[ind]=pixels2[ind];
				}
				for(int j=hk;j<width-hk;j++,ind++){
					if(pixels2[ind]!=0x00){
						int n=0, prev=-1, c=0, r=0;
						for(int k=-hk;k<=hk;k++){
							if(pixels2[ind-hk*width+k]==0x00){
								++n;
								if(prev==1){
									++c;
								}
								prev=0;
							}else{
								prev=1;
							}
						}
						for(int k=-hk+1;k<hk;k++){
							if(pixels2[ind+k*width+hk]==0x00){
								++n;
								if(prev==1){
									++c;
								}
								prev=0;
							}else{
								prev=1;
							}
						}
						for(int k=hk;k>=-hk;k--){
							if(pixels2[ind+hk*width+k]==0x00){
								++n;
								if(prev==1){
									++c;
								}
								prev=0;
							}else{
								prev=1;
							}
						}
						for(int k=hk-1;k>-hk;k--){
							if(pixels2[ind+k*width-hk]==0x00){
								++n;
								if(prev==1){
									++c;
								}
								prev=0;
							}else{
								prev=1;
							}
						}
						if(prev==1&&pixels2[ind-hk*width-hk]==0x00){
							++c;
						}
						if(pixels2[ind-hk*width-hk]==0x00){
							++r;
						}else if(pixels2[ind-hk*width+hk]==0x00){
							++r;
						}else if(pixels2[ind+hk*width-hk]==0x00){
							++r;
						}else if(pixels2[ind+hk*width+hk]==0x00){
							++r;
						}
						if(c==1&&(n>k3m4||(n==k3m4&&r==2))){
							changed=true;
							to[ind]=0x00;
						}else{
							to[ind]=(byte)0xFF;
						}
					}else{
						to[ind]=0x00;
					}
				}
				for(int j=width-hk;j<width;j++,ind++){
					to[ind]=pixels2[ind];
				}
			}
			for(;ind<len;ind++){
				to[ind]=pixels2[ind];
			}
		}
	}
}
