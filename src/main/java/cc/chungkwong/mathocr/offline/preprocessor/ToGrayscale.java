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
import java.awt.*;
import java.awt.image.*;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class ToGrayscale implements Preprocessor{
	private final int wR, wG, wB, divisor;
	private final boolean linear;
	/**
	 * Construct a Grayscale
	 */
	public ToGrayscale(){
		this(true);
	}
	/**
	 * Construct a Grayscale
	 *
	 * @param linear linear color space
	 */
	public ToGrayscale(boolean linear){
		this.linear=linear;
		this.wR=316;
		this.wG=624;
		this.wB=84;
		this.divisor=wR+wG+wB;
	}
	/**
	 * Construct a Grayscale
	 *
	 * @param wR coefficient of red component
	 * @param wG coefficient of green component
	 * @param wB coefficient of blue component
	 */
	public ToGrayscale(int wR,int wG,int wB){
		this.linear=false;
		this.wR=wR;
		this.wG=wG;
		this.wB=wB;
		this.divisor=wR+wG+wB;
	}
	private static final ColorConvertOp TO_GRAYSCALE;
	static{
		Map<RenderingHints.Key,Object> config=new HashMap<>();
		config.put(RenderingHints.KEY_DITHERING,RenderingHints.VALUE_DITHER_DISABLE);
		config.put(RenderingHints.KEY_COLOR_RENDERING,RenderingHints.VALUE_COLOR_RENDER_SPEED);
		TO_GRAYSCALE=new ColorConvertOp(new RenderingHints(config));
	}
	@Override
	public BufferedImage apply(BufferedImage image,boolean inplace){
		if(image.getType()==BufferedImage.TYPE_BYTE_GRAY){
			return image;
		}
		int width=image.getWidth(), height=image.getHeight();
		BufferedImage result=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_GRAY);
		if(linear){
			TO_GRAYSCALE.filter(image,result);
		}else{
			byte[] buf=((DataBufferByte)result.getRaster().getDataBuffer()).getData();
			int[] row=new int[width];
			for(int i=0, ind=0;i<height;i++){
				image.getRGB(0,i,width,1,row,0,width);
				for(int j=0;j<width;j++,ind++){
					int pixel=row[j];
					int alpha=(pixel>>>24)&0xff, red=(pixel>>>16)&0xff, green=(pixel>>>8)&0xff, blue=pixel&0xff;
					buf[ind]=(byte)(255-(255-(red*wR+green*wG+blue*wB)/divisor)*alpha/255);
				}
			}
		}
		return result;
	}
}
