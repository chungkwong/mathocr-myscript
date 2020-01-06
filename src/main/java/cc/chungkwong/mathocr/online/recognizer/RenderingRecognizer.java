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
package cc.chungkwong.mathocr.online.recognizer;
import cc.chungkwong.mathocr.common.*;
import cc.chungkwong.mathocr.offline.*;
import cc.chungkwong.mathocr.offline.extractor.*;
import cc.chungkwong.mathocr.online.*;
import cc.chungkwong.mathocr.ui.*;
/**
 * Online recognizer based on an offline recognizer
 *
 * @author Chan Chung Kwong
 */
public class RenderingRecognizer implements OnlineRecognizer{
	private final OfflineRecognizer base;
	/**
	 * Create a online recognizer
	 *
	 * @param base underlying offline recognizer
	 */
	public RenderingRecognizer(OfflineRecognizer base){
		this.base=base;
	}
	/**
	 *
	 * @return underlying offline recognizer
	 */
	public OfflineRecognizer getBase(){
		return base;
	}
	@Override
	public EncodedExpression recognize(TraceList traceList){
		return base.recognize(Extractor.getDefault().preprocess(TraceListViewer.renderImage(traceList)));
	}
}
