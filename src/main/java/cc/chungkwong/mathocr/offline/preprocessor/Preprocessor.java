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
public interface Preprocessor{
	/**
	 * Perform preprocess operation
	 *
	 * @param image input image
	 * @param inplace attempt to reuse input image or not
	 * @return processed image
	 */
	public BufferedImage apply(BufferedImage image,boolean inplace);
}
