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
package cc.chungkwong.mathocr.crohme;
import cc.chungkwong.mathocr.common.*;
import cc.chungkwong.mathocr.common.format.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class Mathml2Inkml{
	/**
	 * Regenerate InkML for recognized JIIX
	 *
	 * @param directory the location of recognition result
	 * @param lg if the output is used to generate LG file
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static void generate(File directory,boolean lg,boolean latex) throws IOException,ParserConfigurationException,SAXException{
		Path content=new File(directory,"list").toPath();
		Map<String,String> gt=Files.exists(content)?Files.lines(content).
				filter((line)->line.contains(", ")).
				map((line)->line.split(", ")).
				collect(Collectors.toMap((b)->b[1],(b)->b[0])):new HashMap<>();
		File inkDirectory0=new File(directory,"result_inkml");
		File inkDirectory=new File(directory,lg?"result_mml":"result_inkml");
		inkDirectory.mkdirs();
		File jiixDirectory=new File(directory,"result_jiix");
		jiixDirectory.mkdirs();
		for(File jiixFile:jiixDirectory.listFiles()){
			File inkfile0=new File(inkDirectory0,jiixFile.getName().replace(".jiix",".inkml"));
			File inkfile=new File(inkDirectory,jiixFile.getName().replace(".jiix",lg?".mml":".inkml"));
			String reference=gt.get(inkfile0.getCanonicalPath());
			String ui=reference!=null?new Ink(new File(reference)).getMeta().get("UI"):jiixFile.getName().replace(".jiix","");
			String jiix=new String(Files.readAllBytes(jiixFile.toPath()),StandardCharsets.UTF_8);
			jiix=jiix.replaceFirst("<math xmlns='http://www.w3.org/1998/Math/MathML'>","<mrow>");
			jiix=jiix.replaceFirst("</math>","</mrow>");
			//System.out.println(jiix);
			//convert(jiix,ui,lg);
			Files.write(inkfile.toPath(),Arrays.asList(convert(jiix,ui,lg,latex)),StandardCharsets.UTF_8);
		}
//		if(lg){
//			String mml2symlg=Crohme.DIRECTORY_2016+"/evaluationTools/mathmleval/mml2symlg";
//			try{
//				Runtime.getRuntime().exec(mml2symlg+" "+inkDirectory.getCanonicalPath()+" "+new File(directory,"result_lg").getCanonicalPath()).waitFor();
//			}catch(InterruptedException ex){
//				Logger.getLogger(InkmlGenerator.class.getName()).log(Level.SEVERE,null,ex);
//			}
//		}
	}
	/**
	 * Convert JIIX to InkML
	 *
	 * @param jiix JIIX code
	 * @param name UI of the expression
	 * @param lg if the output is used to generate LG file
	 * @return
	 */
	public static String convert(String jiix,String name,boolean lg,boolean latex){
		StringBuilder buf=new StringBuilder();
		buf.append("<ink xmlns=\"http://www.w3.org/2003/InkML\">\n<annotation type=\"UI\">");
		buf.append(name);
		buf.append("</annotation>\n<annotationXML type=\"truth\" encoding=\"Content-MathML\">\n<math xmlns='http://www.w3.org/1998/Math/MathML'>");
		try{
			Expression expr=new MathmlFormat().decode(jiix);
			encodeMathml(expr,buf,lg?RENAME_LG:RENAME,latex);
		}catch(Exception e){
			Logger.getGlobal().log(Level.INFO,name,e);
		}
		buf.append("</math></annotationXML>");
		buf.append("</ink>");
		return buf.toString();
	}
	static final Map<String,String> RENAME=new HashMap<>();
	static final Map<String,String> RENAME_LG=new HashMap<>();
	static{
		Map<String,String> common=new HashMap<>();
		common.put("0","0");
		common.put("1","1");
		common.put("2","2");
		common.put("3","3");
		common.put("4","4");
		common.put("5","5");
		common.put("6","6");
		common.put("7","7");
		common.put("8","8");
		common.put("9","9");
		common.put("a","a");
		common.put("A","A");
		common.put("b","b");
		common.put("B","B");
		common.put("c","c");
		common.put("C","C");
		common.put("d","d");
		common.put("e","e");
		common.put("E","E");
		common.put("f","f");
		common.put("F","F");
		common.put("g","g");
		common.put("g","g");
		common.put("G","G");
		common.put("H","H");
		common.put("h","h");
		common.put("i","i");
		common.put("I","I");
		common.put("j","j");
		common.put("k","k");
		common.put("l","l");
		common.put("L","L");
		common.put("m","m");
		common.put("M","M");
		common.put("n","n");
		common.put("N","N");
		common.put("o","o");
		common.put("p","p");
		common.put("P","P");
		common.put("q","q");
		common.put("r","r");
		common.put("R","R");
		common.put("s","s");
		common.put("S","S");
		common.put("t","t");
		common.put("T","T");
		common.put("u","u");
		common.put("v","v");
		common.put("V","V");
		common.put("w","w");
		common.put("x","x");
		common.put("X","X");
		common.put("y","y");
		common.put("Y","Y");
		common.put("z","z");
		common.put("(","(");
		common.put(")",")");
		common.put(".",".");
		common.put("[","[");
		common.put("]","]");
		common.put("|","|");
		common.put("=","=");
		common.put("!","!");
		common.put("-","-");
		common.put("+","+");
		common.put("/","/");
		RENAME.putAll(common);
		RENAME_LG.putAll(common);
		RENAME.put("∑","sum");
		RENAME.put("→","rarr");
		RENAME.put("∫","int");
		RENAME.put("π","pi");
		RENAME.put("≤","leq");
		RENAME.put("≥","ge");
		RENAME.put("∞","infin");
		RENAME.put("'","prime");
		RENAME.put("×","times");
		RENAME.put("α","alpha");
		RENAME.put("β","beta");
		RENAME.put("γ","gamma");
		RENAME.put("λ","lambda");
		RENAME.put("θ","theta");
		RENAME.put("µ","mu");
		RENAME.put("σ","sigma");
		RENAME.put("φ","phi");
		RENAME.put("ϕ","phi");
		RENAME.put("Δ","Delta");
		RENAME.put("±","pm");
		RENAME.put("÷","div");
		RENAME.put("∃","exists");
		RENAME.put("∀","forall");
		RENAME.put("≠","ne");
		RENAME.put("∈","in");
		RENAME.put("…","hellip");
		RENAME.put("·",".");
		RENAME.put("{","{");
		RENAME.put("}","}");
		RENAME.put(",",",");
		RENAME.put("cos","cos");
		RENAME.put(">",">");
		RENAME.put("lim","lim");
		RENAME.put("log","log");
		RENAME.put("<","<");
		RENAME.put("sin","sin");
		RENAME.put("tan","tan");
		RENAME_LG.put("{","\\{");
		RENAME_LG.put("}","\\}");
		RENAME_LG.put("α","\\alpha");
		RENAME_LG.put("β","\\beta");
		RENAME_LG.put(",","COMMA");
		RENAME_LG.put("cos","\\cos");
		RENAME_LG.put("Δ","\\Delta");
		RENAME_LG.put("÷","\\div");
		RENAME_LG.put("∃","\\exists");
		RENAME_LG.put("∀","\\forall");
		RENAME_LG.put("γ","\\gamma");
		RENAME_LG.put("≥","\\geq");
		RENAME_LG.put(">","\\gt");
		RENAME_LG.put("∈","\\in");
		RENAME_LG.put("∞","\\infty");
		RENAME_LG.put("∫","\\int");
		RENAME_LG.put("λ","\\lambda");
		RENAME_LG.put("…","\\ldots");
		RENAME_LG.put("≤","\\leq");
		RENAME_LG.put("lim","\\lim");
		RENAME_LG.put("log","\\log");
		RENAME_LG.put("<","\\lt");
		RENAME_LG.put("µ","\\mu");
		RENAME_LG.put("≠","\\neq");
		RENAME_LG.put("φ","\\phi");
		RENAME_LG.put("ϕ","\\phi");
		RENAME_LG.put("π","\\pi");
		RENAME_LG.put("±","\\pm");
		RENAME_LG.put("'","\\prime");
		RENAME_LG.put("→","\\rightarrow");
		RENAME_LG.put("σ","\\sigma");
		RENAME_LG.put("sin","\\sin");
		RENAME_LG.put("∑","\\sum");
		RENAME_LG.put("tan","\\tan");
		RENAME_LG.put("θ","\\theta");
		RENAME_LG.put("×","\\times");
		RENAME_LG.put("·",".");
	}
	static void encodeMathml(Expression span,StringBuilder buf,Map<String,String> rename,boolean latex){
		if(span instanceof Expression.Symbol){
			String name=((Expression.Symbol)span).getName();
			if(name.codePointCount(0,name.length())==1||"sin".equals(name)
					||"cos".equals(name)||"tan".equals(name)||"log".equals(name)||"lim".equals(name)){
				buf.append("<mi>");
				if(!rename.containsKey(name)){
					System.out.println("Unknown symbol: "+name);
				}
				escape(rename.getOrDefault(name,"1"),buf);
				buf.append("</mi>");
			}else{
				System.out.println("Splited:"+name);
				buf.append("<mrow>");
				name.codePoints().forEachOrdered((c)->{
					buf.append("<mi>");
					String nam=new String(new int[]{c},0,1);
					if(!rename.containsKey(nam)){
						System.out.println("Unknown symbol: "+nam);
					}
					escape(rename.getOrDefault(nam,"1"),buf);
					buf.append("</mi>");
				});
				buf.append("</mrow>");
			}
		}else if(span instanceof Expression.Line){
			buf.append("<mrow>");
			for(Expression comp:((Expression.Line)span).getSpans()){
				encodeMathml(comp,buf,rename,latex);
			}
			buf.append("</mrow>");
		}else if(span instanceof Expression.Subscript){
			buf.append("<msub>");
			encodeMathml(((Expression.Subscript)span).getBase(),buf,rename,latex);
			encodeMathml(((Expression.Subscript)span).getSubscript(),buf,rename,latex);
			buf.append("</msub>");
		}else if(span instanceof Expression.Superscript){
			buf.append("<msup>");
			encodeMathml(((Expression.Superscript)span).getBase(),buf,rename,latex);
			encodeMathml(((Expression.Superscript)span).getSuperscript(),buf,rename,latex);
			buf.append("</msup>");
		}else if(span instanceof Expression.Subsuperscript){
			buf.append("<msubsup>");
			encodeMathml(((Expression.Subsuperscript)span).getBase(),buf,rename,latex);
			encodeMathml(((Expression.Subsuperscript)span).getSubscript(),buf,rename,latex);
			encodeMathml(((Expression.Subsuperscript)span).getSuperscript(),buf,rename,latex);
			buf.append("</msubsup>");
		}else if(span instanceof Expression.Fraction){
			buf.append("<mfrac>");
			encodeMathml(((Expression.Fraction)span).getNumerator(),buf,rename,latex);
			encodeMathml(((Expression.Fraction)span).getDenominator(),buf,rename,latex);
			buf.append("</mfrac>");
		}else if(span instanceof Expression.Radical){
			if(((Expression.Radical)span).getPower()!=null){
				buf.append("<mroot>");
				encodeMathml(((Expression.Radical)span).getRadicand(),buf,rename,latex);
				encodeMathml(((Expression.Radical)span).getPower(),buf,rename,latex);
				buf.append("</mroot>");
			}else{
				buf.append("<msqrt>");
				encodeMathml(((Expression.Radical)span).getRadicand(),buf,rename,latex);
				buf.append("</msqrt>");
			}
		}else if(span instanceof Expression.Matrix){
			buf.append("<mtable>");
			for(List<Expression> row:((Expression.Matrix)span).getRows()){
				buf.append("<mtr>");
				for(Expression cell:row){
					buf.append("<mtd>");
					encodeMathml(cell,buf,rename,latex);
					buf.append("</mtd>");
				}
				buf.append("</mtr>");
			}
			buf.append("</mtable>");
		}else if(span instanceof Expression.Over){
			buf.append("<mover>");
			encodeMathml(((Expression.Over)span).getContent(),buf,rename,latex);
			encodeMathml(((Expression.Over)span).getOver(),buf,rename,latex);
			buf.append("</mover>");
		}else if(span instanceof Expression.Under){
			boolean s=latex;
			if(((Expression.Under)span).getContent() instanceof Expression.Line){
				Expression.Line line=(Expression.Line)((Expression.Under)span).getContent();
				if(line.getSpans().size()==1&&line.getSpans().get(0) instanceof Expression.Symbol){
					s="∫".equals(((Expression.Symbol)line.getSpans().get(0)).getName());
				}
			}
			buf.append(s?"<msub>":"<munder>");
			encodeMathml(((Expression.Under)span).getContent(),buf,rename,latex);
			encodeMathml(((Expression.Under)span).getUnder(),buf,rename,latex);
			buf.append(s?"</msub>":"</munder>");
		}else if(span instanceof Expression.UnderOver){
			boolean s=latex;
			if(((Expression.UnderOver)span).getContent() instanceof Expression.Line){
				Expression.Line line=(Expression.Line)((Expression.UnderOver)span).getContent();
				if(line.getSpans().size()==1&&line.getSpans().get(0) instanceof Expression.Symbol){
					s="∫".equals(((Expression.Symbol)line.getSpans().get(0)).getName());
				}
			}
			buf.append(s?"<msubsup>":"<munderover>");
			encodeMathml(((Expression.UnderOver)span).getContent(),buf,rename,latex);
			encodeMathml(((Expression.UnderOver)span).getUnder(),buf,rename,latex);
			encodeMathml(((Expression.UnderOver)span).getOver(),buf,rename,latex);
			buf.append(s?"</msubsup>":"</munderover>");
		}else{
			System.out.println("Unknown type:"+span);
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
	public static void main(String[] args) throws IOException,ParserConfigurationException,SAXException{
//		generate(new File(Crohme.DIRECTORY_RESULT+"/raw2014"),true,true);
//		generate(new File(Crohme.DIRECTORY_RESULT+"/raw2016"),true,false);
//		generate(new File(Crohme.DIRECTORY_RESULT+"/offline2014"),true,true);
//		generate(new File(Crohme.DIRECTORY_RESULT+"/offline2016"),true,false);
		generate(new File(Crohme.DIRECTORY_RESULT+"/old2016"),true,false);
	}
}
