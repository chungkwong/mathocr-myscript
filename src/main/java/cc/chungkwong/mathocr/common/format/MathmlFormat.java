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
import java.nio.charset.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.w3c.dom.Node;
import org.xml.sax.*;
/**
 * MathML format
 *
 * @author Chan Chung Kwong
 */
public class MathmlFormat implements Format{
	public MathmlFormat(){
	}
	@Override
	public String encode(Expression expression){
		StringBuilder buf=new StringBuilder();
		encode(expression,buf);
		return buf.toString();
	}
	@Override
	public Expression decode(String code){
		try{
			Document document=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8)));
			return decode(document.getDocumentElement());
		}catch(ParserConfigurationException|SAXException|IOException ex){
			Logger.getLogger(MathmlFormat.class.getName()).log(Level.SEVERE,null,ex);
		}
		return new Expression.Line();
	}
	private Expression decode(Element element){
		List<Element> children=new ArrayList<>();
		NodeList childNodes=element.getChildNodes();
		for(int i=0;i<childNodes.getLength();i++){
			if(childNodes.item(i).getNodeType()==Node.ELEMENT_NODE){
				children.add((Element)childNodes.item(i));
			}
		}
		switch(element.getTagName()){
			case "mi":
			case "mn":
			case "mo":
				return new Expression.Symbol(element.getTextContent().trim());
			case "mrow":
			case "mtr":
			case "mtd":
			case "math":
				return new Expression.Line(decode(children));
			case "mtable":
				return new Expression.Matrix(decode(children).stream().map((row)->((Expression.Line)row).getSpans()).collect(Collectors.toList()));
			case "mfrac":
				return new Expression.Fraction(decode(children.get(0)),decode(children.get(1)));
			case "msqrt":
				return new Expression.Radical(null,new Expression.Line(decode(children)));
			case "mroot":
				return new Expression.Radical(decode(children.get(1)),decode(children.get(0)));
			case "msub":
				return new Expression.Subscript(decode(children.get(0)),decode(children.get(1)));
			case "msup":
				return new Expression.Superscript(decode(children.get(0)),decode(children.get(1)));
			case "msubsup":
				return new Expression.Subsuperscript(decode(children.get(0)),decode(children.get(1)),decode(children.get(2)));
			case "munder":
				return new Expression.Under(decode(children.get(0)),decode(children.get(1)));
			case "mover":
				return new Expression.Over(decode(children.get(0)),decode(children.get(1)));
			case "munderover":
				return new Expression.UnderOver(decode(children.get(0)),decode(children.get(1)),decode(children.get(2)));
			case "mfenced": {
				String left=element.getAttribute("open");
				String right=element.getAttribute("close");
				if(left==null||left.isEmpty()){
					left="(";
				}
				if(right==null||right.isEmpty()){
					right=")";
				}
				List<Expression> list=decode(children);
				list.add(0,new Expression.Symbol(left));
				list.add(new Expression.Symbol(right));
				return new Expression.Line(list);
			}
			default:
				System.err.println("Unknown:"+element.getTagName());
				return new Expression.Line();
		}
	}
	private List<Expression> decode(List<Element> elements){
		List<Expression> spans=new ArrayList<>(elements.size());
		for(Element operand:elements){
			Expression current=decode(operand);
			if(!spans.isEmpty()){
				if(current instanceof Expression.Symbol&&((Expression.Symbol)current).getName().equals("'")){
					System.out.println("Fixed prime");
					spans.set(spans.size()-1,new Expression.Superscript(spans.get(spans.size()-1),current));
					continue;
				}
			}
			spans.add(current);
		}
		return spans;
	}
	public static void encode(Expression span,StringBuilder buf){
		if(span instanceof Symbol){
			buf.append("<mi>");
			escape(((Symbol)span).getName(),buf);
			buf.append("</mi>");
		}else if(span instanceof Line){
			buf.append("<mrow>");
			for(Expression comp:((Line)span).getSpans()){
				encode(comp,buf);
			}
			buf.append("</mrow>");
		}else if(span instanceof Subscript){
			buf.append("<msub>");
			encode(((Subscript)span).getBase(),buf);
			encode(((Subscript)span).getSubscript(),buf);
			buf.append("</msub>");
		}else if(span instanceof Superscript){
			buf.append("<msup>");
			encode(((Superscript)span).getBase(),buf);
			encode(((Superscript)span).getSuperscript(),buf);
			buf.append("</msup>");
		}else if(span instanceof Subsuperscript){
			buf.append("<msubsup>");
			encode(((Subsuperscript)span).getBase(),buf);
			encode(((Subsuperscript)span).getSubscript(),buf);
			encode(((Subsuperscript)span).getSuperscript(),buf);
			buf.append("</msubsup>");
		}else if(span instanceof Fraction){
			buf.append("<mfrac>");
			encode(((Fraction)span).getNumerator(),buf);
			encode(((Fraction)span).getDenominator(),buf);
			buf.append("</mfrac>");
		}else if(span instanceof Radical){
			if(((Radical)span).getPower()!=null){
				buf.append("<mroot>");
				encode(((Radical)span).getRadicand(),buf);
				encode(((Radical)span).getPower(),buf);
				buf.append("</mroot>");
			}else{
				buf.append("<msqrt>");
				encode(((Radical)span).getRadicand(),buf);
				buf.append("</msqrt>");
			}
		}else if(span instanceof Matrix){
			buf.append("<mtable>");
			for(List<Expression> row:((Matrix)span).getRows()){
				buf.append("<mtr>");
				for(Expression cell:row){
					buf.append("<mtd>");
					encode(cell,buf);
					buf.append("</mtd>");
				}
				buf.append("</mtr>");
			}
			buf.append("</mtable>");
		}else if(span instanceof Over){
			buf.append("<mover>");
			encode(((Over)span).getContent(),buf);
			encode(((Over)span).getOver(),buf);
			buf.append("</mover>");
		}else if(span instanceof Under){
			buf.append("<munder>");
			encode(((Under)span).getContent(),buf);
			encode(((Under)span).getUnder(),buf);
			buf.append("</munder>");
		}else if(span instanceof UnderOver){
			buf.append("<munderover>");
			encode(((UnderOver)span).getContent(),buf);
			encode(((UnderOver)span).getUnder(),buf);
			encode(((UnderOver)span).getOver(),buf);
			buf.append("</munderover>");
		}else{
			buf.append("<mi>");
			escape(span.toString(),buf);
			buf.append("</mi>");
		}
	}
	private static void escape(String str,StringBuilder latex){
		if(str==null){
			System.err.println("NULL SYMBOL");
			return;
		}
		int len=str.length();
		for(int i=0;i<len;i++){
			char c=str.charAt(i);
			switch(c){
				case '<':
					latex.append("&lt;");
					break;
				case '&':
					latex.append("&amp;");
					break;
				default:
					latex.append(c);
					break;
			}
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
		return "MathML";
	}
}
