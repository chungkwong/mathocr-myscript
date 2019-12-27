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
import cc.chungkwong.mathocr.online.*;
import java.util.*;
/**
 * Mathematical expression
 *
 * @author Chan Chung Kwong
 */
public class Expression{
	public static class Matrix extends Expression{
		private final List<List<Expression>> rows;
		public Matrix(List<List<Expression>> rows){
			this.rows=rows;
		}
		public List<List<Expression>> getRows(){
			return rows;
		}
		public int getRowCount(){
			return rows.size();
		}
		public int getColumnCount(){
			return rows.stream().mapToInt((row)->row.size()).max().getAsInt();
		}
	}
	public static class Line extends Expression{
		private final List<Expression> spans;
		public Line(List<Expression> spans){
			this.spans=spans;
		}
		public Line(Expression... spans){
			this.spans=Arrays.asList(spans);
		}
		public List<Expression> getSpans(){
			return spans;
		}
	}
	public static class Symbol extends Expression{
		private final String name;
		private final List<Trace> primitives;
		public Symbol(String name){
			this.name=name;
			this.primitives=null;
		}
		public Symbol(String name,List<Trace> primitives){
			this.name=name;
			this.primitives=primitives;
		}
		public String getName(){
			return name;
		}
		public Object getPrimitives(){
			return primitives;
		}
	}
	public static class Fraction extends Expression{
		private final Expression numerator, denominator;
		private final List<Trace> primitives;
		public Fraction(Expression numerator,Expression denominator){
			this.numerator=numerator;
			this.denominator=denominator;
			this.primitives=null;
		}
		public Fraction(Expression numerator,Expression denominator,List<Trace> primitives){
			this.numerator=numerator;
			this.denominator=denominator;
			this.primitives=primitives;
		}
		public Expression getNumerator(){
			return numerator;
		}
		public Expression getDenominator(){
			return denominator;
		}
		public Object getPrimitives(){
			return primitives;
		}
	}
	public static class Radical extends Expression{
		private final Expression power, radicand;
		private final List<Trace> primitives;
		public Radical(Expression power,Expression radicand){
			this.power=power;
			this.radicand=radicand;
			this.primitives=null;
		}
		public Radical(Expression power,Expression radicand,List<Trace> primitives){
			this.power=power;
			this.radicand=radicand;
			this.primitives=primitives;
		}
		public Expression getPower(){
			return power;
		}
		public Expression getRadicand(){
			return radicand;
		}
		public Object getPrimitives(){
			return primitives;
		}
	}
	public static class Subscript extends Expression{
		private final Expression base, script;
		public Subscript(Expression base,Expression script){
			this.base=base;
			this.script=script;
		}
		public Expression getBase(){
			return base;
		}
		public Expression getSubscript(){
			return script;
		}
	}
	public static class Superscript extends Expression{
		private final Expression base, script;
		public Superscript(Expression base,Expression script){
			this.base=base;
			this.script=script;
		}
		public Expression getBase(){
			return base;
		}
		public Expression getSuperscript(){
			return script;
		}
	}
	public static class Subsuperscript extends Expression{
		private final Expression base, sub, sup;
		public Subsuperscript(Expression base,Expression sub,Expression sup){
			this.base=base;
			this.sub=sub;
			this.sup=sup;
		}
		public Expression getBase(){
			return base;
		}
		public Expression getSubscript(){
			return sub;
		}
		public Expression getSuperscript(){
			return sup;
		}
	}
	public static class Under extends Expression{
		private final Expression content, under;
		public Under(Expression content,Expression under){
			this.content=content;
			this.under=under;
		}
		public Expression getContent(){
			return content;
		}
		public Expression getUnder(){
			return under;
		}
	}
	public static class Over extends Expression{
		private final Expression content, over;
		public Over(Expression content,Expression over){
			this.content=content;
			this.over=over;
		}
		public Expression getContent(){
			return content;
		}
		public Expression getOver(){
			return over;
		}
	}
	public static class UnderOver extends Expression{
		private final Expression content, under, over;
		public UnderOver(Expression content,Expression under,Expression over){
			this.content=content;
			this.under=under;
			this.over=over;
		}
		public Expression getContent(){
			return content;
		}
		public Expression getUnder(){
			return under;
		}
		public Expression getOver(){
			return over;
		}
	}
}
