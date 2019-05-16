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
import cc.chungkwong.mathocr.online.TraceList;
import cc.chungkwong.mathocr.common.EncodedExpression;
/**
 * Online mathematical expression recognizer
 *
 * @author Chan Chung Kwong
 */
public interface OnlineRecognizer{
	/**
	 * Recognizer an expression
	 *
	 * @param traceList strokes to be recognized
	 * @return recognition result
	 */
	EncodedExpression recognize(TraceList traceList);
}
