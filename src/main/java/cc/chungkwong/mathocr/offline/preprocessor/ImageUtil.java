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
public class ImageUtil{
	/**
	 * Create a integral image
	 *
	 * @param	pixels the input grayscale picture
	 * @param width the width of the picture
	 * @param height the height of the picture
	 * @return integral image
	 */
	public static final long[][] getIntegralImage(byte[] pixels,int width,int height){
		long[][] intImg=new long[height][width];
		int ind=0;
		intImg[0][0]=pixels[0]&0xFF;
		for(int j=1;j<width;j++){
			intImg[0][j]=intImg[0][j-1]+(pixels[++ind]&0xFF);
		}
		for(int i=1;i<height;i++){
			intImg[i][0]=intImg[i-1][0]+(pixels[++ind]&0xFF);
			for(int j=1;j<width;j++){
				intImg[i][j]=intImg[i-1][j]+intImg[i][j-1]+(pixels[++ind]&0xFF)-intImg[i-1][j-1];
			}
		}
		return intImg;
	}
	/**
	 * Create a squared integral image
	 *
	 * @param	pixels the input grayscale picture
	 * @param width the width of the picture
	 * @param height the height of the picture
	 * @return squared integral image
	 */
	public static final long[][] getSquaredIntegralImage(byte[] pixels,int width,int height){
		long[][] intImg=new long[height][width];
		int ind=0;
		intImg[0][0]=square(pixels[0]&0xFF);
		for(int j=1;j<width;j++){
			intImg[0][j]=intImg[0][j-1]+square(pixels[++ind]&0xFF);
		}
		for(int i=1;i<height;i++){
			intImg[i][0]=intImg[i-1][0]+square(pixels[++ind]&0xFF);
			for(int j=1;j<width;j++){
				intImg[i][j]=intImg[i-1][j]+intImg[i][j-1]+square(pixels[++ind]&0xFF)-intImg[i-1][j-1];
			}
		}
		return intImg;
	}
	/**
	 * Compute square of a integer
	 *
	 * @param	k the number to be squared
	 * @return square of k
	 */
	private static int square(int k){
		return k*k;
	}
	/**
	 * Compute the sum of pixels in a window
	 *
	 * @param intImg the integral image
	 * @param width the width of the picture
	 * @param height the height of the picture
	 * @param i the x coordinate
	 * @param j the y coordinate
	 * @param dl left offset
	 * @param dr right offset
	 * @return the sum
	 */
	public static final long windowValue(long[][] intImg,int width,int height,int i,int j,int dl,int dr){
		long mm=i-dl>=0&&j-dl>=0?intImg[i-dl][j-dl]:0;
		long MM=intImg[Math.min(height-1,i+dr)][Math.min(width-1,j+dr)];
		long mM=i-dl>=0?intImg[i-dl][Math.min(width-1,j+dr)]:0;
		long Mm=j-dl>=0?intImg[Math.min(height-1,i+dr)][j-dl]:0;
		return mm+MM-mM-Mm;
	}
	/**
	 * Compute the average x coordinate of nonzero pixels
	 *
	 * @param intImg the integral image
	 * @param width the width of the picture
	 * @param height the height of the picture
	 * @param i the x coordinate
	 * @param j the y coordinate
	 * @param dl left offset
	 * @param dr right offset
	 * @return the sum
	 */
	public static final int averageX(long[][] intImg,int width,int height,int i,int j,int dl,int dr){
		int count=0, sum=0, xstart=Math.max(0,j-dl), xend=Math.min(width-1,j+dr), ystart=Math.max(-1,i-dl), yend=Math.min(height-1,i+dr);
		for(int k=xstart;k<xend;k++){
			int tmp=(int)(intImg[yend][k]-(ystart!=-1?intImg[ystart][k]:0)+(k!=0?-intImg[yend][k-1]+(ystart!=-1?intImg[ystart][k-1]:0):0));
			count+=tmp;
			sum+=tmp*k;
		}
		return count==0?Integer.MAX_VALUE:sum/count;
	}
	/**
	 * Compute the average y coordinate of nonzero pixels
	 *
	 * @param intImg the integral image
	 * @param width the width of the picture
	 * @param height the height of the picture
	 * @param i the x coordinate
	 * @param j the y coordinate
	 * @param dl left offset
	 * @param dr right offset
	 * @return the sum
	 */
	public static final int averageY(long[][] intImg,int width,int height,int i,int j,int dl,int dr){
		int count=0, sum=0, xstart=Math.max(-1,j-dl), xend=Math.min(width-1,j+dr), ystart=Math.max(0,i-dl), yend=Math.min(height-1,i+dr);
		for(int k=ystart;k<yend;k++){
			int tmp=(int)(intImg[k][xend]-(xstart!=-1?intImg[k][xstart]:0)+(k!=0?-intImg[k-1][xend]+(xstart!=-1?intImg[k-1][xstart]:0):0));
			count+=tmp;
			sum+=tmp*k;
		}
		return count==0?Integer.MAX_VALUE:sum/count;
	}
}
