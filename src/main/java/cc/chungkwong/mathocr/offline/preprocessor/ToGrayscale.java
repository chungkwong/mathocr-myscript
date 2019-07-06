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
import java.awt.image.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class ToGrayscale implements Preprocessor{
	private final int wR, wG, wB, divisor;
	/**
	 * Construct a Grayscale
	 */
	public ToGrayscale(){
		this(316,624,84);
	}
	/**
	 * Construct a Grayscale
	 *
	 * @param wR coefficient of red component
	 * @param wG coefficient of green component
	 * @param wB coefficient of blue component
	 */
	public ToGrayscale(int wR,int wG,int wB){
		this.wR=wR;
		this.wG=wG;
		this.wB=wB;
		this.divisor=wR+wG+wB;
	}
	@Override
	public BufferedImage apply(BufferedImage image,boolean inplace){
		if(image.getType()==BufferedImage.TYPE_BYTE_GRAY){
			return image;
		}
		int width=image.getWidth(), height=image.getHeight();
		BufferedImage result=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_GRAY);
		//		new ColorConvertOp(null).filter(image,result);
		byte[] buf=((DataBufferByte)result.getRaster().getDataBuffer()).getData();
		if(image.getType()==BufferedImage.TYPE_INT_ARGB){
			int[] data=((DataBufferInt)image.getRaster().getDataBuffer()).getData();
			for(int i=0;i<buf.length;i++){
				int pixel=data[i];
				int alpha=(pixel>>>24)&0xff, red=(pixel>>>16)&0xff, green=(pixel>>>8)&0xff, blue=pixel&0xff;
				buf[i]=(byte)(255-(255-(red*wR+green*wG+blue*wB)/divisor)*alpha/255);
			}
		}else if(image.getType()==BufferedImage.TYPE_INT_RGB){
			int[] data=((DataBufferInt)image.getRaster().getDataBuffer()).getData();
			for(int i=0;i<buf.length;i++){
				int pixel=data[i];
				int red=(pixel>>>16)&0xff, green=(pixel>>>8)&0xff, blue=pixel&0xff;
				buf[i]=(byte)((red*wR+green*wG+blue*wB)/divisor);
			}
		}else{
			int[] row=new int[width];
			for(int i=0, ind=0;i<height;i++){
				image.getRGB(0,i,width,1,row,0,width);
				for(int j=0;j<width;j++,ind++){
					int pixel=row[j];
					int alpha=(pixel>>>24)&0xff, red=(pixel>>>16)&0xff, green=(pixel>>>8)&0xff, blue=pixel&0xff;
//					buf[i]=COLOR[(255-(255-(red*wR+green*wG+blue*wB)/divisor)*alpha/255)];
					buf[ind]=(byte)(255-(255-(red*wR+green*wG+blue*wB)/divisor)*alpha/255);
				}
			}
		}
		return result;
	}
}
