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
package cc.chungkwong.mathocr.common;
import cc.chungkwong.mathocr.common.format.Format;
import java.util.*;
/**
 * Encoded mathematical expression
 *
 * @author Chan Chung Kwong
 */
public class EncodedExpression{
	private final Map<Format,String> codes;
	private Expression expression;
	/**
	 * Create a encoded expression
	 *
	 * @param codes code in various formats
	 */
	public EncodedExpression(Map<Format,String> codes){
		this.codes=codes;
	}
	/**
	 * Create a encoded expression
	 *
	 * @param code code
	 * @param format format
	 */
	public EncodedExpression(String code,Format format){
		this.codes=new LinkedHashMap<>();
		codes.put(format,code);
	}
	/**
	 * Create from a expression
	 *
	 * @param expression the expression
	 */
	public EncodedExpression(Expression expression){
		this.codes=new HashMap<>();
		this.expression=expression;
	}
	/**
	 *
	 * @param format a format
	 * @return expression encoded in the given format
	 */
	public String getCodes(Format format){
		if(!codes.containsKey(format)){
			if(expression==null){
				Map.Entry<Format,String> entry=codes.entrySet().iterator().next();
				expression=entry.getKey().decode(entry.getValue());
			}
			codes.put(format,format.encode(expression));
		}
		return codes.get(format);
	}
	@Override
	public String toString(){
		return codes.toString();
	}
}
