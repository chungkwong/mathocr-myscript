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
package cc.chungkwong.mathocr.common.format;
import cc.chungkwong.mathocr.common.Expression;
/**
 * A format for mathematical expression
 *
 * @author Chan Chung Kwong
 */
public interface Format{
	/**
	 * Encode an expression
	 *
	 * @param expression to be encoded
	 * @return the code
	 */
	String encode(Expression expression);
	/**
	 * Decode
	 *
	 * @param code to be decoded
	 * @return the expression
	 */
	Expression decode(String code);
}
