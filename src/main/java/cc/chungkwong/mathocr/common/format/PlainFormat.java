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
import cc.chungkwong.mathocr.common.*;
import cc.chungkwong.mathocr.common.Expression.Fraction;
import cc.chungkwong.mathocr.common.Expression.Line;
import cc.chungkwong.mathocr.common.Expression.Matrix;
import cc.chungkwong.mathocr.common.Expression.Over;
import cc.chungkwong.mathocr.common.Expression.Radical;
import cc.chungkwong.mathocr.common.Expression.Subscript;
import cc.chungkwong.mathocr.common.Expression.Subsuperscript;
import cc.chungkwong.mathocr.common.Expression.Superscript;
import cc.chungkwong.mathocr.common.Expression.Symbol;
import cc.chungkwong.mathocr.common.Expression.Under;
import cc.chungkwong.mathocr.common.Expression.UnderOver;
import java.util.*;
/**
 * Plain format
 *
 * @author Chan Chung Kwong
 */
public class PlainFormat implements Format{
	public PlainFormat(){
	}
	@Override
	public String encode(Expression expression){
		StringBuilder buf=new StringBuilder();
		encode(expression,buf);
		return buf.toString();
	}
	@Override
	public Expression decode(String code){
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	private void encode(Expression span,StringBuilder buf){
		if(span instanceof Symbol){
			buf.append(((Symbol)span).getName());
		}else if(span instanceof Line){
			((Line)span).getSpans().forEach((comp)->{
				encode(comp,buf);
			});
		}else if(span instanceof Subscript){
			encode(((Subscript)span).getBase(),buf);
			buf.append("_{");
			encode(((Subscript)span).getSubscript(),buf);
			buf.append('}');
		}else if(span instanceof Superscript){
			encode(((Superscript)span).getBase(),buf);
			buf.append("^{");
			encode(((Superscript)span).getSuperscript(),buf);
			buf.append('}');
		}else if(span instanceof Subsuperscript){
			encode(((Subsuperscript)span).getBase(),buf);
			buf.append("_{");
			encode(((Subsuperscript)span).getSubscript(),buf);
			buf.append('}');
			buf.append("^{");
			encode(((Subsuperscript)span).getSuperscript(),buf);
			buf.append('}');
		}else if(span instanceof Over){
			Over over=((Over)span);
			buf.append("|\n");
			encode(over.getOver(),buf);
			buf.append("\n");
			encode(over.getContent(),buf);
			buf.append("\n|");
		}else if(span instanceof Under){
			Under under=((Under)span);
			buf.append("|\n");
			encode(under.getContent(),buf);
			buf.append("\n");
			encode(under.getUnder(),buf);
			buf.append("\n|");
		}else if(span instanceof UnderOver){
			UnderOver underover=((UnderOver)span);
			buf.append("|\n");
			encode(underover.getOver(),buf);
			buf.append("\n");
			encode(underover.getContent(),buf);
			buf.append("\n");
			encode(underover.getUnder(),buf);
			buf.append("\n|");
		}else if(span instanceof Fraction){
			buf.append("(");
			encode(((Fraction)span).getNumerator(),buf);
			buf.append(")/(");
			encode(((Fraction)span).getDenominator(),buf);
			buf.append(')');
		}else if(span instanceof Radical){
			if(((Radical)span).getPower()!=null){
				buf.append('(');
				encode(((Radical)span).getPower(),buf);
				buf.append(')');
			}
			buf.append("√(");
			encode(((Radical)span).getRadicand(),buf);
			buf.append(')');
		}else if(span instanceof Matrix){
			int columnCount=((Matrix)span).getColumnCount();
			buf.append("\n");
			for(List<Expression> row:((Matrix)span).getRows()){
				int j=columnCount;
				for(Expression cell:row){
					encode(cell,buf);
					if(--j>0){
						buf.append("\t");
					}
				}
				buf.append("\n");
			}
		}else{
			buf.append(span.toString());
		}
	}
	@Override
	public boolean equals(Object obj){
		return obj!=null&&obj.getClass()==getClass();
	}
	@Override
	public int hashCode(){
		return getClass().hashCode();
	}
	@Override
	public String toString(){
		return "Plain";
	}
}
