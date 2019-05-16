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
public class OtsuBinarizer extends FixedBinarizer{
	public OtsuBinarizer(){
		super(127);
	}
	/**
	 * Get the threhold value
	 *
	 * @param pixels pixel array of the image
	 * @return the threhold value
	 */
	@Override
	protected int getThrehold(byte[] pixels){
		int[] histogram=getHistogram(pixels);
		double except=0, uT=0, bestVar=0;
		int acc=0, bestValue=0;
		for(int i=0;i<256;i++){
			uT+=i*histogram[i];
		}
		for(int t=0;t<255;t++){
			acc+=histogram[t];
			except+=histogram[t]*t;
			if(acc==0){
				continue;
			}
			if(pixels.length==acc){
				break;
			}
			double u0=except/acc;
			double u1=(uT-except)/(pixels.length-acc);
			double var=(u0-u1)*(u0-u1)*acc*(pixels.length-acc);
			if(var>bestVar){
				bestVar=var;
				bestValue=t;
			}
		}
		//System.out.println(bestValue);
		return bestValue;
	}
	/**
	 * Get the histogram of a image
	 *
	 * @param pixels pixel array of the image
	 * @return the histogram of the image
	 */
	private static int[] getHistogram(byte[] pixels){
		int[] histogram=new int[256];
		for(int i=0;i<pixels.length;i++){
			++histogram[pixels[i]&0xFF];
		}
		return histogram;
	}
}
