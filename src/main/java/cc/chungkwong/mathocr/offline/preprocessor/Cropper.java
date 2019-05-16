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
public class Cropper implements Preprocessor{
	private final int left, right, top, bottom;
	/**
	 * Construct a Crop
	 *
	 * @param left minimum x-coordinate of bounding box of the area to be kept
	 * @param right maximum x-coordinate of bounding box of the area to be kept
	 * @param top minimum y-coordinate of bounding box of the area to be kept
	 * @param bottom maximum y-coordinate of bounding box of the area to be kept
	 */
	public Cropper(int left,int right,int top,int bottom){
		this.left=Math.max(left,0);
		this.right=right;
		this.top=Math.max(top,0);
		this.bottom=bottom;
	}
	/**
	 * Perform preprocess operation
	 *
	 * @param image input image
	 * @return processed image
	 */
	@Override
	public BufferedImage apply(BufferedImage image,boolean inplace){
		return image.getSubimage(left,top,Math.min(right,image.getWidth()-1)-left,Math.min(bottom,image.getHeight()-1)-top);
	}
}
