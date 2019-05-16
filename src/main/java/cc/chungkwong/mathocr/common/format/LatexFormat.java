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
import static cc.chungkwong.mathocr.common.Expression.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
/**
 * LaTeX format
 *
 * @author Chan Chung Kwong
 */
public class LatexFormat implements Format{
	private static final Map<Integer,String> latexName;
	static{
		Properties math=new Properties();
		try(InputStream in=LatexFormat.class.getResourceAsStream("latex_symbol_math.properties")){
			math.load(in);
		}catch(IOException ex){
			Logger.getLogger(LatexFormat.class.getName()).log(Level.SEVERE,null,ex);
		}
		latexName=math.entrySet().stream().collect(Collectors.toMap((e)->Integer.parseInt(e.getKey().toString(),16),(e)->e.getValue().toString()));
	}
	public LatexFormat(){
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
			String name=((Symbol)span).getName();
			escape(name,buf);
		}else if(span instanceof Line){
			for(Expression comp:((Line)span).getSpans()){
				encode(comp,buf);
			}
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
			int codePoint=getCodePoint(over.getOver());
			if(codePoint>=0){
				String name=getHatName(codePoint);
				if(name!=null){
					buf.append('\\').append(name).append('{');
					encode(over.getContent(),buf);
					buf.append('}');
					return;
				}
			}
			buf.append("\\stackrel{");
			encode(over.getOver(),buf);
			buf.append("}{");
			encode(over.getContent(),buf);
			buf.append("}");
		}else if(span instanceof Under){
			Under under=((Under)span);
			int codePointU=getCodePoint(under.getUnder());
			if(codePointU>=0){
				String name=getLegName(codePointU);
				if(name!=null){
					buf.append('\\').append(name).append('{');
					encode(under.getContent(),buf);
					buf.append('}');
					return;
				}
			}
			int codePointC=getCodePoint(under.getContent());
			if(codePointC>=0&&isBigOperator(codePointC)){
				encode(under.getContent(),buf);
				buf.append("_{");
				encode(under.getUnder(),buf);
				buf.append('}');
				return;
			}
			buf.append("\\stackrel{");
			encode(under.getContent(),buf);
			buf.append("}{");
			encode(under.getUnder(),buf);
			buf.append("}");
		}else if(span instanceof UnderOver){
			UnderOver underover=((UnderOver)span);
			int codePoint=getCodePoint(underover.getContent());
			if(codePoint>=0){
				if(isBigOperator(codePoint)){
					encode(underover.getContent(),buf);
					buf.append("^{");
					encode(underover.getOver(),buf);
					buf.append("}_{");
					encode(underover.getUnder(),buf);
					buf.append('}');
					return;
				}else if(codePoint=='⏞'){
					buf.append("\\overbrace{");
					encode(underover.getUnder(),buf);
					buf.append("}^{");
					encode(underover.getOver(),buf);
					buf.append('}');
					return;
				}else if(codePoint=='⏟'){
					buf.append("\\underbrace{");
					encode(underover.getOver(),buf);
					buf.append("}_{");
					encode(underover.getUnder(),buf);
					buf.append('}');
					return;
				}
			}
			buf.append("\\begin{array}{c}");
			encode(underover.getOver(),buf);
			buf.append("\\\\");
			encode(underover.getContent(),buf);
			buf.append("\\\\");
			encode(underover.getUnder(),buf);
			buf.append("\\end{array}");
		}else if(span instanceof Fraction){
			buf.append("\\frac{");
			encode(((Fraction)span).getNumerator(),buf);
			buf.append("}{");
			encode(((Fraction)span).getDenominator(),buf);
			buf.append('}');
		}else if(span instanceof Radical){
			buf.append("\\sqrt");
			if(((Radical)span).getPower()!=null){
				buf.append('[');
				encode(((Radical)span).getPower(),buf);
				buf.append(']');
			}
			buf.append('{');
			encode(((Radical)span).getRadicand(),buf);
			buf.append('}');
		}else if(span instanceof Matrix){
			buf.append("\\begin{array}{");
			int columnCount=((Matrix)span).getColumnCount();
			for(int i=0;i<columnCount;i++){
				buf.append('c');//FIXME check alignment
			}
			buf.append("}\n");
			for(List<Expression> row:((Matrix)span).getRows()){
				int j=columnCount;
				for(Expression cell:row){
					encode(cell,buf);
					if(--j>0){
						buf.append(" & ");
					}
				}
				buf.append("\\\\\n");
			}
			if(!((Matrix)span).getRows().isEmpty()){
				buf.delete(buf.length()-3,buf.length()-1);
			}
			buf.append("\\end{array}\n");
		}else{
			escape(span.toString(),buf);
		}
	}
	private int getCodePoint(Expression expression){
		if(expression instanceof Expression.Line&&((Expression.Line)expression).getSpans().size()==1){
			return getCodePoint(((Expression.Line)expression).getSpans().get(0));
		}else if(expression instanceof Symbol){
			return ((Symbol)expression).getName().codePointAt(0);
		}else{
			return -1;
		}
	}
	private void escape(String str,StringBuilder buf){
		int len=str.length();
		for(int i=0;i<len;i=str.offsetByCodePoints(i,1)){
			int c=str.codePointAt(i);
			if(latexName.containsKey(c)){
				buf.append(latexName.get(c));
			}else{
				buf.appendCodePoint(c);
			}
		}
	}
	private static String getHatName(int codePoint){
		switch(codePoint){
			case '˙':
				return "dot";
			case '¨':
				return "ddot";
			case 'ˆ':
				return "hat";
			case '¯':
				return "overline";
			case '´':
				return "acute";
			case 'ˇ':
				return "check";
			case 'ˋ':
				return "grave";
			case '˘':
				return "breve";
			case '°':
				return "mathring";
			case '˜':
				return "tilde";
			case '⏞':
				return "overbrace";
			case '\u20D7':
				return "vec";
			default:
				return null;
		}
	}
	private static String getLegName(int codePoint){
		switch(codePoint){
			case '_':
				return "underline";
			case '⏟':
				return "underbrace";
			default:
				return null;
		}
	}
	private static boolean isBigOperator(int codePoint){
		return "∏∐∑∫∬∭∮∯∰∱∲∳⋀⋁⋂⋃⨀⨁⨂⨃⨄⨅⨆⨉".indexOf(codePoint)!=-1;
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
		return "LaTeX";
	}
}
