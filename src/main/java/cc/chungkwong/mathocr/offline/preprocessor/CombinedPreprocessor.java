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
import java.awt.image.*;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class CombinedPreprocessor implements Preprocessor{
	private final List<Preprocessor> preprocessors;
	public CombinedPreprocessor(){
		preprocessors=new ArrayList<>();
		preprocessors.add(new ToGrayscale());
		if(Settings.DEFAULT.getBoolean("DETECT_INVERT")==true){
			preprocessors.add(new Inverter(true));
		}
		if(Settings.DEFAULT.getBoolean("MEAN_FILTER")==true){
			preprocessors.add(new MeanFilter());
		}
		switch(Settings.DEFAULT.getString("BINARIZATION_METHOD").toUpperCase()){
			case "OTSU":
				preprocessors.add(new OtsuBinarizer());
				break;
			case "FIXED":
				preprocessors.add(new OtsuBinarizer());
				break;
			default:
				preprocessors.add(new SauvolaBinarizer());
				break;
		}
		if(Settings.DEFAULT.getBoolean("MEDIAN_FILTER")==true){
			preprocessors.add(new MedianFilter());
		}
		if(Settings.DEFAULT.getBoolean("NOISE_REMOVE")==true){
			preprocessors.add(new NoiseRemove());
		}
	}
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
