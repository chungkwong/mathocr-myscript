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
public class CombinedPreprocessor implements Preprocessor{
	private final List<Preprocessor> preprocessors;
	/**
	 * Construct a CombinedPreprocessor
	 *
	 * @param preprocessors a sequence of preprocessors
	 */
	public CombinedPreprocessor(List<Preprocessor> preprocessors){
		this.preprocessors=preprocessors;
	}
	/**
	 * Perform preprocess operation
	 *
	 * @param image input image
	 * @return processed image
	 */
	@Override
	public BufferedImage apply(BufferedImage image,boolean inplace){
		Iterator<Preprocessor> iterator=preprocessors.iterator();
		if(iterator.hasNext()){
			image=iterator.next().apply(image,inplace);
			while(iterator.hasNext()){
				image=iterator.next().apply(image,true);
			}
		}
		return image;
	}
}
