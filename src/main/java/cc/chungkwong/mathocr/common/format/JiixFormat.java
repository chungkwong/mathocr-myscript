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
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
/**
 * JIIX format
 *
 * @author Chan Chung Kwong
 */
public class JiixFormat implements Format{
	private final ObjectMapper mapper=new ObjectMapper();
	private final JsonFactory factory=new JsonFactory();
	public JiixFormat(){
	}
	@Override
	public String encode(Expression expression){
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	@Override
	public Expression decode(String code){
		try{
			return decodeExpressions((List<Map<String,Object>>)mapper.readValue(code,Map.class).get("expressions"));
		}catch(IOException ex){
			Logger.getLogger(JiixFormat.class.getName()).log(Level.SEVERE,null,ex);
			return new Expression.Line(new ArrayList<>());
		}
	}
	private Expression decodeExpressions(List<Map<String,Object>> expressions){
		if(expressions.size()==1){
			return decode(expressions.get(0));
		}else{
			List<List<Expression>> rows=new ArrayList<>(expressions.size());
			for(Map<String,Object> expression:expressions){
				rows.add(Arrays.asList(decode(expression)));
			}
			return new Expression.Matrix(rows);
		}
	}
	private Expression decode(Map<String,Object> code){
		String type=(String)code.get("type");
		String label=(String)code.get("label");
		List<Map<String,Object>> operands=(List<Map<String,Object>>)code.get("operands");
		switch(type){
			case "number":
			case "symbol":
				if(label.length()<=1||label.equals("lim")){
					return new Expression.Symbol(label);
				}else{
					String literal=label.endsWith("…")&&code.containsKey("value")?code.get("value").toString():label;
					return new Expression.Line(literal.codePoints().mapToObj((c)->new Expression.Symbol(new String(new int[]{c},0,1))).collect(Collectors.toList()));
				}
			case "function":
				if(label==null){
					System.out.println(code);
				}
				if(operands.isEmpty()){
					return new Expression.Symbol(label);
				}else if("log".equals(label)&&operands.size()==2){
					return new Expression.Line(new Expression.Subscript(new Expression.Symbol(label),decode(operands.get(0))),decode(operands.get(1)));
				}else{
					ArrayList<Expression> spans=new ArrayList<>(operands.size()+1);
					spans.add(new Expression.Symbol(label));
					for(Map<String,Object> operand:operands){
						spans.add(decode(operand));
					}
					return new Expression.Line(spans);
				}
			case "group":
				return decodeGroup(operands);
			case "fraction":
				return new Expression.Fraction(decode(operands.get(0)),decode(operands.get(1)));
			case "power":
				if(operands.size()>=2){
					return fixFunctionSquare(new Expression.Superscript(decode(operands.get(0)),decode(operands.get(1))));
				}else{
					return new Expression.Superscript(new Expression.Line(),decode(operands.get(0)));
				}
			case "!":
				return new Expression.Line(decode(operands.get(0)),new Expression.Symbol("!"));
			case "percentage":
				return new Expression.Line(decode(operands.get(0)),new Expression.Symbol((String)code.get("operator")),decode(operands.get(1)),new Expression.Symbol("%"));
			case "square root":
				if(operands.size()==2){
					return new Expression.Radical(decode(operands.get(0)),decode(operands.get(1)));
				}else{
					return new Expression.Radical(null,decode(operands.get(0)));
				}
			case "system":
				return decodeExpressions((List<Map<String,Object>>)code.get("expressions"));
			case "matrix": {
				List<Map<String,Object>> rows=(List<Map<String,Object>>)code.get("rows");
				List<List<Expression>> matrix=new ArrayList<>(rows.size());
				for(Map<String,Object> row:rows){
					matrix.add(((List<Map<String,Object>>)row.get("cells")).stream().map((o)->decode(o)).collect(Collectors.toList()));
				}
				return new Expression.Matrix(matrix);
			}
			case "fence":
				if(code.get("open symbol")==null){
					return new Expression.Line(decode(operands.get(0)),new Expression.Symbol((String)code.get("close symbol")));
				}else if(code.get("close symbol")==null){
					return new Expression.Line(new Expression.Symbol((String)code.get("open symbol")),decode(operands.get(0)));
				}else{
					return new Expression.Line(new Expression.Symbol((String)code.get("open symbol")),decode(operands.get(0)),new Expression.Symbol((String)code.get("close symbol")));
				}
			case "underscript":
				return fixIntegral(new Expression.Under(decode(operands.get(0)),decode(operands.get(1))));
			case "overscript":
				return new Expression.Over(decode(operands.get(0)),decode(operands.get(1)));
			case "underoverscript":
				return fixIntegral(new Expression.UnderOver(decode(operands.get(0)),decode(operands.get(1)),decode(operands.get(2))));
			case "superscript":
				return fixFunctionSquare(new Expression.Superscript(decode(operands.get(0)),decode(operands.get(1))));
			case "subscript":
				return new Expression.Subscript(decode(operands.get(0)),decode(operands.get(1)));
			case "subsuperscript":
				return new Expression.Subsuperscript(decode(operands.get(0)),decode(operands.get(1)),decode(operands.get(2)));
			case "presuperscript":
				return new Expression.Line(new Expression.Superscript(new Expression.Line(),decode(operands.get(1))),decode(operands.get(0)));
			case "presubscript":
				return new Expression.Line(new Expression.Subscript(new Expression.Line(),decode(operands.get(1))),decode(operands.get(0)));
			case "presubsuperscript":
				return new Expression.Line(new Expression.Subsuperscript(new Expression.Line(),decode(operands.get(1)),decode(operands.get(2))),decode(operands.get(0)));
			case "partialfractionnumerator":
				return new Expression.Fraction(decode(operands.get(0)),new Expression.Line());
			case "partialfractiondenominator":
				return new Expression.Fraction(new Expression.Line(),decode(operands.get(0)));
			case "slantedfraction":
				return new Expression.Line(decode(operands.get(0)),new Expression.Symbol("/"),decode(operands.get(1)));
			default:
				if(operands.isEmpty()){
					return new Expression.Symbol(type);
				}else if(operands.size()==1){
					return new Expression.Line(new Expression.Symbol(type),decode(operands.get(0)));
				}else{
					ArrayList<Expression> spans=new ArrayList<>(operands.size()*2-1);
					if(!operands.isEmpty()){
						Iterator<Map<String,Object>> iterator=operands.iterator();
						spans.add(decode(iterator.next()));
						while(iterator.hasNext()){
							spans.add(new Expression.Symbol(type));
							spans.add(decode(iterator.next()));
						}
					}
					return new Expression.Line(spans);
				}
		}
	}
	private Expression.Line decodeGroup(List<Map<String,Object>> operands){
		List<Expression> spans=new ArrayList<>(operands.size());
		for(Map<String,Object> operand:operands){
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
		return new Expression.Line(spans);
	}
	private Expression fixFunctionSquare(Expression.Superscript superscript){
		if(superscript.getBase() instanceof Expression.Line){
			List<Expression> spans=((Expression.Line)superscript.getBase()).getSpans();
			if(!spans.isEmpty()&&spans.get(0) instanceof Expression.Symbol){
				String symbolName=((Expression.Symbol)spans.get(0)).getName();
				if("sin".equals(symbolName)||"cos".equals(symbolName)||"tan".equals(symbolName)){
					List<Expression> fixed=new ArrayList<>();
					fixed.add(new Expression.Superscript(spans.get(0),superscript.getSuperscript()));
					fixed.addAll(spans.subList(1,spans.size()));
					System.err.println("fixed function square");
					return new Expression.Line(fixed);
				}
			}
		}
		return superscript;
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
		return "JIIX";
	}
	private Expression fixIntegral(Expression.Under under){
		if(under.getContent() instanceof Expression.Symbol&&"∫".equals(((Expression.Symbol)under.getContent()).getName())){
			return new Expression.Subscript(under.getContent(),under.getUnder());
		}else{
			return under;
		}
	}
	private Expression fixIntegral(Expression.UnderOver underOver){
		if(underOver.getContent() instanceof Expression.Symbol&&"∫".equals(((Expression.Symbol)underOver.getContent()).getName())){
			return new Expression.Subsuperscript(underOver.getContent(),underOver.getUnder(),underOver.getOver());
		}else{
			return underOver;
		}
	}
}
