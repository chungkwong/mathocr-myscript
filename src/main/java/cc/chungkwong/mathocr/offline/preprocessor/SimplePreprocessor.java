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
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public abstract class SimplePreprocessor implements Preprocessor{
	/**
	 * Perform preprocess operation
	 *
	 * @param image input image
	 * @return processed image
	 */
	@Override
	public BufferedImage apply(BufferedImage image,boolean inplace){
		int width=image.getWidth(), height=image.getHeight();
		BufferedImage result;
		byte[] from=((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		byte[] to;
		if(inplace){
			result=image;
			to=from;
		}else{
			result=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_GRAY);
			to=((DataBufferByte)result.getRaster().getDataBuffer()).getData();
			Arrays.fill(to,(byte)0xFF);
		}
		preprocess(from,to,width,height);
		return result;
	}
	/**
	 * Perform preprocess operation
	 *
	 * @param from pixel array of the input image
	 * @param to pixel array of the output image
	 * @param width width of the input image
	 * @param height height of the input image
	 */
	public abstract void preprocess(byte[] from,byte[] to,int width,int height);
}
