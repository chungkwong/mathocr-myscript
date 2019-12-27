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
public abstract class GlobalBinarizer extends SimplePreprocessor{
	@Override
	public void preprocess(byte[] from,byte[] to,int width,int height){
		int lim=getThrehold(from);
		int len=width*height;
		for(int i=0;i<len;i++){
			to[i]=(from[i]&0xFF)<=lim?0x00:(byte)0xff;
		}
	}
	/*
	 * Get the threhold value
	 * @param pixels pixel array of the input image
	 * @return threhold value
	 */
	protected abstract int getThrehold(byte[] pixels);
}
