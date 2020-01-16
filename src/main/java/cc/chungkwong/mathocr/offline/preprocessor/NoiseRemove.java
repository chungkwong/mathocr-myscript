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
import cc.chungkwong.mathocr.offline.*;
/**
 *
 * @author Chan Chung Kwong
 */
/**
 * A preprocessor that apply post-processing step of binarized image described
 * in Adaptive degraded document image binarization by B. Gatos , I. Pratikakis,
 * S.J. Perantonis
 */
public class NoiseRemove extends SimplePreprocessor{
	/**
	 * Construct a NoiseRemove
	 */
	public NoiseRemove(){
	}
	@Override
	public void preprocess(byte[] from,byte[] to,int width,int height){
		ComponentPool components=new ComponentPool(from,width,height);
//		int sw=0, black=0;
//		for(ConnectedComponent component:components.getComponents()){
//			int b=component.getWeight();
//			sw+=b*component.getWeight()/Math.max(component.getWidth(),component.getHeight());
//			black+=b;
//		}
//		sw/=black;
		int lh=(int)components.getComponents().stream().map((c)->c.getBox()).mapToInt((b)->Math.max(b.getWidth(),b.getHeight())).average().getAsDouble();
		//System.out.println(lh);
		int n=lh*3/20, ksh=n*n/10, ksw=n*n/20, dx=n/4, dy=n/4, ksw1=n*n*7/20;
//		int n=lh*3/20, ksh=n*sw/2, ksw=n*sw/4, dx=sw+1, dy=sw+1, ksw1=n*sw*7/20;
		int dl=(n+1)/2, dr=n/2, len=width*height;
		for(int i=0;i<len;i++){
			to[i]=(byte)(1-(from[i]&0x1));
		}
		long[][] intImg=ImageUtil.getIntegralImage(to,width,height);
		for(int i=0, ind=0;i<height;i++){
			for(int j=0;j<width;j++,ind++){
				if(to[ind]==1&&ImageUtil.windowValue(intImg,width,height,i,j,dl,dr)<ksh){
					to[ind]=0;
				}
			}
		}
		intImg=ImageUtil.getIntegralImage(to,width,height);
		for(int i=0, ind=0;i<height;i++){
			for(int j=0;j<width;j++,ind++){
				if(to[ind]==0&&ImageUtil.windowValue(intImg,width,height,i,j,dl,dr)>ksw
						&&Math.abs(ImageUtil.averageX(intImg,width,height,i,j,dl,dr)-j)<dx&&Math.abs(ImageUtil.averageY(intImg,width,height,i,j,dl,dr)-i)<dy){
					to[ind]=1;
				}
			}
		}
		intImg=ImageUtil.getIntegralImage(to,width,height);
		for(int i=0, ind=0;i<height;i++){
			for(int j=0;j<width;j++,ind++){
				if(to[ind]==1||ImageUtil.windowValue(intImg,width,height,i,j,dl,dr)>ksw1){
					to[ind]=0x00;
				}else{
					to[ind]=(byte)0xff;
				}
			}
		}
	}
}
