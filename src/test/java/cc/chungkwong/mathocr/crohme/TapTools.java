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
import cc.chungkwong.mathocr.offline.extractor.*;
import cc.chungkwong.mathocr.online.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
/**
 * Converter to the format of TAP
 *
 * @author Chan Chung Kwong
 */
public class TapTools{
	/**
	 * Convert data to the format of TAP
	 *
	 * @param stream data
	 * @param directory output directory
	 * @throws IOException
	 */
	public static void toTap(Stream<cc.chungkwong.mathocr.common.format.Ink> stream,File directory) throws IOException{
		File asciiDirectory=new File(directory,"on-ascii");
		asciiDirectory.mkdirs();
		File alignDirectory=new File(directory,"on-align");
		alignDirectory.mkdirs();
		HashMap<String,String> captions=new HashMap<>();
		for(Iterator<cc.chungkwong.mathocr.common.format.Ink> iterator=stream.iterator();iterator.hasNext();){
			cc.chungkwong.mathocr.common.format.Ink next=iterator.next();
			String key=next.getFile().getName();
			key=key.substring(0,key.indexOf('.'));
			Expression expression=next.getExpression();
			if(expression!=null){
				List<Pair<String,List<Trace>>> pairs=normalizeLatex(expression);
				String latex=pairs.stream().map(Pair<String,List<Trace>>::getKey).collect(Collectors.joining(" "));
				if(latex.endsWith(" DROPPED")){
					System.out.println(latex+key);
				}else{
					captions.put(key,latex);
//					TraceList traceList=new CutOrderer().order(next.getTraceList());
					TraceList traceList=next.getTraceList();
					writeTraceList(traceList,new File(asciiDirectory,key+".ascii"),true);
					try(BufferedWriter out=Files.newBufferedWriter(new File(alignDirectory,key+".align").toPath())){
						writeAlign(traceList,pairs,out);
					}
				}
			}else{
				writeTraceList(next.getTraceList(),new File(asciiDirectory,key+".ascii"),true);
			}
		}
		Files.write(new File(directory,"caption.txt").toPath(),captions.entrySet().stream().map((e)->e.getKey()+'\t'+e.getValue()).collect(Collectors.toList()));
	}
	/**
	 * Convert data to the format of TAP
	 *
	 * @param stream data
	 * @param directory output directory
	 * @throws IOException
	 */
	public static void toTapOffline(Stream<Pair<cc.chungkwong.mathocr.common.format.Ink,BufferedImage>> stream,File directory) throws IOException{
		File asciiDirectory=new File(directory,"on-ascii");
		asciiDirectory.mkdirs();
		File alignDirectory=new File(directory,"on-align");
		alignDirectory.mkdirs();
		HashMap<String,String> captions=new HashMap<>();
		Extractor extractor=Extractor.getDefault();
		for(Iterator<Pair<cc.chungkwong.mathocr.common.format.Ink,BufferedImage>> iterator=stream.iterator();iterator.hasNext();){
			Pair<cc.chungkwong.mathocr.common.format.Ink,BufferedImage> pair=iterator.next();
			cc.chungkwong.mathocr.common.format.Ink next=pair.getKey();
			System.out.println(next.getFile().getName());
			String key=next.getFile().getName();
			key=key.substring(0,key.indexOf('.'));
			Expression expression=next.getExpression();
			if(expression!=null){
				List<Pair<String,List<Trace>>> pairs=normalizeLatex(expression);
				String latex=pairs.stream().map(Pair<String,List<Trace>>::getKey).collect(Collectors.joining(" "));
				if(latex.endsWith(" DROPPED")){
					System.out.println(latex+key);
				}else{
					captions.put(key,latex);
					TraceList extractedTraceList=extractor.extract(pair.getValue(),true);
					match(extractedTraceList,next.getTraceList(),pairs);
					writeTraceList(extractedTraceList,new File(asciiDirectory,key+".ascii"),false);
					try(BufferedWriter out=Files.newBufferedWriter(new File(alignDirectory,key+".align").toPath())){
						writeAlign(extractedTraceList,pairs,out);
					}
				}
			}else{
				writeTraceList(next.getTraceList(),new File(asciiDirectory,key+".ascii"),false);
			}
		}
		Files.write(new File(directory,"caption.txt").toPath(),captions.entrySet().stream().map((e)->e.getKey()+'\t'+e.getValue()).collect(Collectors.toList()));
	}
	private static void match(TraceList extracted,TraceList actual,List<Pair<String,List<Trace>>> annotation){
		TraceList rescaledActual=actual.rescale(extracted.getBoundBox());
		Map<Trace,List<Trace>> matched=new IdentityHashMap<>();
		for(Trace trace0:extracted.getTraces()){
			int distance=Integer.MAX_VALUE;
			int match=-1;
			for(ListIterator<Trace> iterator=rescaledActual.getTraces().listIterator();iterator.hasNext();){
				Trace trace1=iterator.next();
				int d=TraceList.getDistance(trace0,trace1);
				if(d<distance){
					distance=d;
					match=iterator.previousIndex();
				}
			}
			Trace found=actual.getTraces().get(match);
			if(!matched.containsKey(found)){
				ArrayList<Trace> list=new ArrayList<>(1);
				list.add(trace0);
				matched.put(found,list);
			}else{
				matched.get(found).add(trace0);
			}
		}
		for(Pair<String,List<Trace>> pair:annotation){
			if(pair.getValue()==null){
				continue;
			}
			for(ListIterator<Trace> iterator=pair.getValue().listIterator();iterator.hasNext();){
				Trace next=iterator.next();
				if(matched.containsKey(next)){
					List<Trace> list=matched.get(next);
					iterator.set(list.get(0));
					list.subList(1,list.size()).forEach((t)->iterator.add(t));
				}else{
					iterator.remove();
				}
			}
			if(pair.getValue().isEmpty()){
				System.out.println(pair.getKey()+" has no trace");
			}
		}
	}
	private static final Map<String,String> COMMANDS=new HashMap<>();
	private static final HashSet<String> COMMANDS_VALUE;
	static{
		COMMANDS.put("0","0");
		COMMANDS.put("1","1");
		COMMANDS.put("2","2");
		COMMANDS.put("3","3");
		COMMANDS.put("4","4");
		COMMANDS.put("5","5");
		COMMANDS.put("6","6");
		COMMANDS.put("7","7");
		COMMANDS.put("8","8");
		COMMANDS.put("9","9");
		COMMANDS.put("a","a");
		COMMANDS.put("A","A");
		COMMANDS.put("b","b");
		COMMANDS.put("B","B");
		COMMANDS.put("c","c");
		COMMANDS.put("C","C");
		COMMANDS.put("d","d");
		COMMANDS.put("e","e");
		COMMANDS.put("E","E");
		COMMANDS.put("f","f");
		COMMANDS.put("F","F");
		COMMANDS.put("g","g");
		COMMANDS.put("g","g");
		COMMANDS.put("G","G");
		COMMANDS.put("H","H");
		COMMANDS.put("h","h");
		COMMANDS.put("i","i");
		COMMANDS.put("I","I");
		COMMANDS.put("j","j");
		COMMANDS.put("k","k");
		COMMANDS.put("l","l");
		COMMANDS.put("L","L");
		COMMANDS.put("m","m");
		COMMANDS.put("M","M");
		COMMANDS.put("n","n");
		COMMANDS.put("N","N");
		COMMANDS.put("o","o");
		COMMANDS.put("p","p");
		COMMANDS.put("P","P");
		COMMANDS.put("q","q");
		COMMANDS.put("r","r");
		COMMANDS.put("R","R");
		COMMANDS.put("s","s");
		COMMANDS.put("S","S");
		COMMANDS.put("t","t");
		COMMANDS.put("T","T");
		COMMANDS.put("u","u");
		COMMANDS.put("v","v");
		COMMANDS.put("V","V");
		COMMANDS.put("w","w");
		COMMANDS.put("x","x");
		COMMANDS.put("X","X");
		COMMANDS.put("y","y");
		COMMANDS.put("Y","Y");
		COMMANDS.put("z","z");
		COMMANDS.put("(","(");
		COMMANDS.put(")",")");
		COMMANDS.put(".",".");
		COMMANDS.put("[","[");
		COMMANDS.put("]","]");
		COMMANDS.put("|","|");
		COMMANDS.put("=","=");
		COMMANDS.put("!","!");
		COMMANDS.put("-","-");
		COMMANDS.put("+","+");
		COMMANDS.put("/","/");
		COMMANDS.put("∑","\\sum");
		COMMANDS.put("→","\\rightarrow");
		COMMANDS.put("∫","\\int");
		COMMANDS.put("π","\\pi");
		COMMANDS.put("≤","\\leq");
		COMMANDS.put("≥","\\geq");
		COMMANDS.put("∞","\\infty");
		COMMANDS.put("'","'");
		COMMANDS.put("×","\\times");
		COMMANDS.put("α","\\alpha");
		COMMANDS.put("β","\\beta");
		COMMANDS.put("γ","\\gamma");
		COMMANDS.put("λ","\\lambda");
		COMMANDS.put("θ","\\theta");
		COMMANDS.put("µ","\\mu");
		COMMANDS.put("σ","\\sigma");
		COMMANDS.put("φ","\\phi");
		COMMANDS.put("ϕ","\\phi");
		COMMANDS.put("Δ","\\Delta");
		COMMANDS.put("±","\\pm");
		COMMANDS.put("÷","\\div");
		COMMANDS.put("∃","\\exists");
		COMMANDS.put("∀","\\forall");
		COMMANDS.put("≠","\\neq");
		COMMANDS.put("∈","\\in");
		COMMANDS.put("…","\\ldots");
		COMMANDS.put("·",".");
		COMMANDS.put("{","\\{");
		COMMANDS.put("}","\\}");
		COMMANDS.put(",",",");
		COMMANDS.put("cos","\\cos");
		COMMANDS.put(">",">");
		COMMANDS.put("lim","\\lim");
		COMMANDS.put("log","\\log");
		COMMANDS.put("<","<");
		COMMANDS.put("sin","\\sin");
		COMMANDS.put("tan","\\tan");
		COMMANDS.put("\\lt","<");
		COMMANDS.put("\\gt",">");
		COMMANDS.put("\\prime","'");
		COMMANDS_VALUE=new HashSet<>(COMMANDS.values());
	}
	private static List<Pair<String,List<Trace>>> normalizeLatex(Expression expr){
		List<Pair<String,List<Trace>>> buf=new ArrayList<>();
		boolean droped=normalizeLatex(expr,buf);
		if(droped){
			buf.add(new Pair<>(" DROPPED",null));
		}
//		return buf.toString().replace("varphi","phi").replace("^ { ' }","'");
		return buf;
	}
	private static boolean normalizeLatex(Expression span,List<Pair<String,List<Trace>>> buf){
		boolean dropped=false;
		if(span instanceof Expression.Symbol){
			Expression.Symbol symbol=(Expression.Symbol)span;
			String name=COMMANDS.get(symbol.getName());
			if(name!=null){
				buf.add(new Pair<>(name,(List<Trace>)symbol.getPrimitives()));
			}else{
				buf.add(new Pair<>(symbol.getName(),(List<Trace>)symbol.getPrimitives()));
				if(!COMMANDS_VALUE.contains(symbol.getName())){
					System.out.println(symbol.getName());
					dropped=true;
				}
			}
		}else if(span instanceof Expression.Line){
			for(Expression comp:((Expression.Line)span).getSpans()){
				dropped|=normalizeLatex(comp,buf);
			}
		}else if(span instanceof Expression.Subscript){
			Expression.Subscript subscript=(Expression.Subscript)span;
			dropped|=normalizeLatex(subscript.getBase(),buf);
			buf.add(new Pair<>("_",null));
			buf.add(new Pair<>("{",null));
			dropped|=normalizeLatex(subscript.getSubscript(),buf);
			buf.add(new Pair<>("}",null));
		}else if(span instanceof Expression.Superscript){
			Expression.Superscript superscript=(Expression.Superscript)span;
			dropped|=normalizeLatex(superscript.getBase(),buf);
			if(isSymbol(superscript.getSuperscript(),"'")){
				dropped|=normalizeLatex(superscript.getSuperscript(),buf);
			}else{
				buf.add(new Pair<>("^",null));
				buf.add(new Pair<>("{",null));
				dropped|=normalizeLatex(superscript.getSuperscript(),buf);
				buf.add(new Pair<>("}",null));
			}
		}else if(span instanceof Expression.Subsuperscript){
			Expression.Subsuperscript subsuperscript=(Expression.Subsuperscript)span;
			dropped|=normalizeLatex(subsuperscript.getBase(),buf);
			buf.add(new Pair<>("_",null));
			buf.add(new Pair<>("{",null));
			dropped|=normalizeLatex(subsuperscript.getSubscript(),buf);
			buf.add(new Pair<>("}",null));
			if(isSymbol(subsuperscript.getSuperscript(),"'")){
				dropped|=normalizeLatex(subsuperscript.getSuperscript(),buf);
			}else{
				buf.add(new Pair<>("^",null));
				buf.add(new Pair<>("{",null));
				dropped|=normalizeLatex(subsuperscript.getSuperscript(),buf);
				buf.add(new Pair<>("}",null));
			}
		}else if(span instanceof Expression.Over){
			Expression.Over over=((Expression.Over)span);
			dropped|=normalizeLatex(over.getContent(),buf);
			buf.add(new Pair<>("^",null));
			buf.add(new Pair<>("{",null));
			dropped|=normalizeLatex(over.getOver(),buf);
			buf.add(new Pair<>("}",null));
			dropped=true;
		}else if(span instanceof Expression.Under){
			Expression.Under under=((Expression.Under)span);
			dropped|=normalizeLatex(under.getContent(),buf);
			buf.add(new Pair<>("_",null));
			buf.add(new Pair<>("{",null));
			dropped|=normalizeLatex(under.getUnder(),buf);
			buf.add(new Pair<>("}",null));
			if(!isSymbol(under.getContent(),"\\lim")&&!isSymbol(under.getContent(),"\\sum")&&!isSymbol(under.getContent(),"\\int")){
				dropped=true;
			}
		}else if(span instanceof Expression.UnderOver){
			Expression.UnderOver underover=((Expression.UnderOver)span);
			dropped|=normalizeLatex(underover.getContent(),buf);
			buf.add(new Pair<>("_",null));
			buf.add(new Pair<>("{",null));
			dropped|=normalizeLatex(underover.getUnder(),buf);
			buf.add(new Pair<>("}",null));
			buf.add(new Pair<>("^",null));
			buf.add(new Pair<>("{",null));
			dropped|=normalizeLatex(underover.getOver(),buf);
			buf.add(new Pair<>("}",null));
			if(!isSymbol(underover.getContent(),"\\lim")&&!isSymbol(underover.getContent(),"\\sum")&&!isSymbol(underover.getContent(),"\\int")){
				dropped=true;
			}
		}else if(span instanceof Expression.Fraction){
			Expression.Fraction fraction=(Expression.Fraction)span;
			buf.add(new Pair<>("\\frac",(List<Trace>)fraction.getPrimitives()));
			buf.add(new Pair<>("{",null));
			dropped|=normalizeLatex(fraction.getNumerator(),buf);
			buf.add(new Pair<>("}",null));
			buf.add(new Pair<>("{",null));
			dropped|=normalizeLatex(fraction.getDenominator(),buf);
			buf.add(new Pair<>("}",null));
		}else if(span instanceof Expression.Radical){
			Expression.Radical radical=(Expression.Radical)span;
			buf.add(new Pair<>("\\sqrt",(List<Trace>)radical.getPrimitives()));
			if(radical.getPower()!=null){
				buf.add(new Pair<>("[",null));
				dropped|=normalizeLatex(radical.getPower(),buf);
				buf.add(new Pair<>("]",null));
			}
			buf.add(new Pair<>("{",null));
			dropped|=normalizeLatex(radical.getRadicand(),buf);
			buf.add(new Pair<>("}",null));
		}else{
			buf.add(new Pair<>(Objects.toString(span),null));
			dropped=true;
		}
		for(int i=buf.size()-3;i>=0;i--){
			if(buf.get(i).getKey().equals(".")&&buf.get(i+1).getKey().equals(".")&&buf.get(i+2).getKey().equals(".")){
				List<Trace> traces=new ArrayList<>(3);
				traces.addAll(buf.get(i).getValue());
				traces.addAll(buf.get(i+1).getValue());
				traces.addAll(buf.get(i+2).getValue());
				buf.remove(i+2);
				buf.remove(i+1);
				buf.set(i,new Pair<>("\\ldots",traces));
				System.out.println("Fixed ... to \\ldots");
			}
		}
		return dropped;
	}
	private static boolean isSymbol(Expression expression,String name){
		if(expression instanceof Expression.Line&&((Expression.Line)expression).getSpans().size()==1){
			return isSymbol(((Expression.Line)expression).getSpans().get(0),name);
		}
		return expression instanceof Expression.Symbol
				&&name.equals(((Expression.Symbol)expression).getName());
	}
	private static void writeTraceList(TraceList traceList,File out,boolean online) throws IOException{
		double distThresholdPre;
		if(online){
			BoundBox boundBox=traceList.getBoundBox();
//			distThresholdPre=0.01*Math.max(boundBox.getWidth(),boundBox.getHeight());
			distThresholdPre=-1;
		}else{
			distThresholdPre=-1;
		}
		new AsciiFormat(distThresholdPre).write(traceList,out);
	}
	private static void writeAlign(TraceList traceList,List<Pair<String,List<Trace>>> pairs,BufferedWriter out) throws IOException{
		for(Iterator<Pair<String,List<Trace>>> iterator=pairs.iterator();iterator.hasNext();){
			Pair<String,List<Trace>> pair=iterator.next();
			out.write(pair.getKey());
			List<Trace> align=pair.getValue();
			if(align==null||align.isEmpty()){
				out.write(" -1");
			}else{
				for(Trace trace:align){
					out.write(' ');
					out.write(Integer.toString(traceList.getTraces().indexOf(trace)));
				}
			}
			if(iterator.hasNext()){
				out.write('\n');
			}
		}
	}
	public static void main(String[] args) throws IOException{
		toTap(Crohme.getTrainStream2016(),new File("../other/TAP-master/data/on/train"));
//		toTap(Crohme.getTrainStream2019(),new File("../other/TAP-master/data/train"));
		toTap(Crohme.getValidationStream2016(),new File("../other/TAP-master/data/on/valid"));
		toTap(Crohme.getTestStream2016(),new File("../other/TAP-master/data/on/test"));
//		toTapOffline(CrohmeOffline.getTrainStream2016(),new File("../other/TAP-master/data/train"));
////		toTap(Crohme.getTrainStream2019(),new File("../other/TAP-master/data/train"));
//		toTapOffline(CrohmeOffline.getValidationStream2016(),new File("../other/TAP-master/data/valid"));
//		toTapOffline(CrohmeOffline.getTestStream2016(),new File("../other/TAP-master/data/test"));
	}
}
