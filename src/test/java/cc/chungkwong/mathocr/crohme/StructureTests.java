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
import com.fasterxml.jackson.databind.*;
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
public class StructureTests{
	public static void exportTrace(File inkmls,File jsons,Extractor config) throws IOException,ParserConfigurationException,SAXException{
		jsons.mkdirs();
		for(File inkmlFile:inkmls.listFiles()){
			if(!inkmlFile.getName().endsWith(".inkml")){
				continue;
			}
			File strokeFile=new File(jsons,inkmlFile.getName().replace(".inkml",".json"));
			TracerTests.exportTrace(tidy(new Ink(inkmlFile),config),strokeFile);
		}
	}
	private static TraceList tidy(Ink ink,Extractor config){
//		List<Pair<BoundBox,String>> annotions=ink.getAnnotions();
		Map<Trace,String> symbols=ink.getAnnotions().stream().collect(Collectors.toMap(
				(pair)->new Trace(Arrays.asList(
						new TracePoint(pair.getKey().getLeft(),pair.getKey().getTop()),
						new TracePoint(pair.getKey().getRight(),pair.getKey().getBottom())
				)),(pair)->pair.getValue()));
		TraceList order=config.getOrderer().order(new TraceList(new ArrayList<>(symbols.keySet())));
		TraceList extracted=new TraceList();
		order.getTraces().forEach((t)->append(t.getBoundBox(),symbols.get(t),extracted));
		return TracerTests.rescale(extracted,TracerTests.IMAGE_BOX);
	}
	private static void append(BoundBox box,String symbol,TraceList list){
		TraceList sample=SAMPLES.get(symbol);
		if(sample==null){
			sample=SAMPLES.get("L");
			System.err.println("Unknown symbol:"+symbol);
		}
		if(symbol.equals("\\sqrt")){
			int x1=box.getLeft()+sample.getBoundBox().getWidth()*box.getHeight()/sample.getBoundBox().getHeight();
			Trace trace=TracerTests.rescale(sample,new BoundBox(box.getLeft(),x1,box.getTop(),box.getBottom())).getTraces().get(0);
			int x2=box.getRight();
			int y=box.getTop();
			for(int i=0;i<100;i++){
				trace.getPoints().add(new TracePoint(x1+(x2-x1)*i/100,y));
			}
			list.getTraces().add(trace);
		}else if(symbol.equals("-")){
			int x1=box.getLeft();
			Trace trace=new Trace(new ArrayList<>(100));
			int x2=box.getRight();
			int y=(box.getTop()+box.getBottom())/2;
			for(int i=0;i<100;i++){
				trace.getPoints().add(new TracePoint(x1+(x2-x1)*i/100,y));
			}
			list.getTraces().add(trace);
		}else{
			list.getTraces().addAll(TracerTests.rescale(sample,box).getTraces());
		}
	}
	private static final Map<String,TraceList> SAMPLES=new HashMap<>();
	static{
		SAMPLES.put("!",createTraceList("EX","EX1"));
		SAMPLES.put("(",createTraceList("LP"));
		SAMPLES.put(")",createTraceList("RP"));
		SAMPLES.put("+",createTraceList("PLUS","PLUS1"));
		SAMPLES.put("-",createTraceList("MINUS"));
		SAMPLES.put(".",createTraceList("DOT"));
		SAMPLES.put("/",createTraceList("SLASH"));
		SAMPLES.put("0",createTraceList("0"));
		SAMPLES.put("1",createTraceList("1"));
		SAMPLES.put("2",createTraceList("2"));
		SAMPLES.put("3",createTraceList("3"));
		SAMPLES.put("4",createTraceList("4","41"));
		SAMPLES.put("5",createTraceList("5","51"));
		SAMPLES.put("6",createTraceList("6"));
		SAMPLES.put("7",createTraceList("7"));
		SAMPLES.put("8",createTraceList("8"));
		SAMPLES.put("9",createTraceList("9"));
		SAMPLES.put("=",createTraceList("EQ","EQ1"));
		SAMPLES.put("A",createTraceList("A","A1","A2"));
		SAMPLES.put("B",createTraceList("B","B1"));
		SAMPLES.put("C",createTraceList("C"));
		SAMPLES.put(",",createTraceList("COMMA"));
		SAMPLES.put("E",createTraceList("E","E1","E2"));
		SAMPLES.put("F",createTraceList("F","F1","F2"));
		SAMPLES.put("G",createTraceList("G","G1","G2"));
		SAMPLES.put("H",createTraceList("H","H1","H2"));
		SAMPLES.put("I",createTraceList("I","I1","I2"));
		SAMPLES.put("L",createTraceList("L"));
		SAMPLES.put("M",createTraceList("M","M1"));
		SAMPLES.put("N",createTraceList("N","N1"));
		SAMPLES.put("P",createTraceList("P","P1"));
		SAMPLES.put("R",createTraceList("R","R1"));
		SAMPLES.put("S",createTraceList("S"));
		SAMPLES.put("T",createTraceList("T","T1"));
		SAMPLES.put("V",createTraceList("V"));
		SAMPLES.put("X",createTraceList("X","X1"));
		SAMPLES.put("Y",createTraceList("Y","Y1"));
		SAMPLES.put("[",createTraceList("LB","LB1"));
		SAMPLES.put("\\Delta",createTraceList("Delta","Delta1","Delta2"));
		SAMPLES.put("\\alpha",createTraceList("alpha"));
		SAMPLES.put("\\beta",createTraceList("beta"));
		SAMPLES.put("\\cos",createTraceList("cos","cos1","cos2"));
		SAMPLES.put("\\div",createTraceList("div","div1","div2"));
		SAMPLES.put("\\exists",createTraceList("exists","exists1","exists2"));
		SAMPLES.put("\\forall",createTraceList("forall","forall1"));
		SAMPLES.put("\\gamma",createTraceList("gamma"));
		SAMPLES.put("\\geq",createTraceList("geq","geq1"));
		SAMPLES.put("\\gt",createTraceList("gt"));
		SAMPLES.put("\\in",createTraceList("in","in1"));
		SAMPLES.put("\\infty",createTraceList("infty"));
		SAMPLES.put("\\int",createTraceList("int"));
		SAMPLES.put("\\lambda",createTraceList("lambda","lambda1"));
		SAMPLES.put("\\ldots",createTraceList("ldots","ldots1","ldots2"));
		SAMPLES.put("\\leq",createTraceList("leq","leq1"));
		SAMPLES.put("\\lim",createTraceList("lim","lim1","lim2","lim3"));
		SAMPLES.put("\\log",createTraceList("log","log1","log2"));
		SAMPLES.put("\\lt",createTraceList("lt"));
		SAMPLES.put("\\mu",createTraceList("mu","mu1"));
		SAMPLES.put("\\neq",createTraceList("neq","neq1","neq2"));
		SAMPLES.put("\\phi",createTraceList("phi"));
		SAMPLES.put("\\pi",createTraceList("pi","pi1","pi2"));
		SAMPLES.put("\\pm",createTraceList("pm","pm1","pm2"));
		SAMPLES.put("\\prime",createTraceList("prime"));
		SAMPLES.put("\\rightarrow",createTraceList("rightarrow","rightarrow1"));
		SAMPLES.put("\\sigma",createTraceList("sigma"));
		SAMPLES.put("\\sin",createTraceList("sin","sin1","sin2","sin3"));
		SAMPLES.put("\\sqrt",createTraceList("sqrt"));
		SAMPLES.put("\\sum",createTraceList("sum","sum1"));
		SAMPLES.put("\\tan",createTraceList("tan","tan1","tan2","tan3"));
		SAMPLES.put("\\theta",createTraceList("theta","theta1"));
		SAMPLES.put("\\times",createTraceList("times","times1"));
		SAMPLES.put("\\{",createTraceList("LBB"));
		SAMPLES.put("\\}",createTraceList("RBB"));
		SAMPLES.put("]",createTraceList("RB","RB1"));
		SAMPLES.put("a",createTraceList("a"));
		SAMPLES.put("b",createTraceList("b","b1"));
		SAMPLES.put("c",createTraceList("c"));
		SAMPLES.put("d",createTraceList("d","d1"));
		SAMPLES.put("e",createTraceList("e"));
		SAMPLES.put("f",createTraceList("f","f1"));
		SAMPLES.put("g",createTraceList("g"));
		SAMPLES.put("h",createTraceList("h"));
		SAMPLES.put("i",createTraceList("i","i1"));
		SAMPLES.put("j",createTraceList("j","j1"));
		SAMPLES.put("k",createTraceList("k","k1"));
		SAMPLES.put("l",createTraceList("l"));
		SAMPLES.put("m",createTraceList("m"));
		SAMPLES.put("n",createTraceList("n"));
		SAMPLES.put("o",createTraceList("o"));
		SAMPLES.put("p",createTraceList("p","p1"));
		SAMPLES.put("q",createTraceList("q"));
		SAMPLES.put("r",createTraceList("r"));
		SAMPLES.put("s",createTraceList("s"));
		SAMPLES.put("t",createTraceList("t","t1"));
		SAMPLES.put("u",createTraceList("u"));
		SAMPLES.put("v",createTraceList("v"));
		SAMPLES.put("w",createTraceList("w"));
		SAMPLES.put("x",createTraceList("x","x1"));
		SAMPLES.put("y",createTraceList("y","y1"));
		SAMPLES.put("z",createTraceList("z","z1"));
		SAMPLES.put("|",createTraceList("VERT"));
	}
	private static TraceList createTraceList(String... code){
		ResourceBundle bundle=ResourceBundle.getBundle("cc.chungkwong.mathocr.sample");
		return new TraceList(Arrays.stream(code).map((c)->craeteTrace(bundle.getString(c))).collect(Collectors.toList()));
	}
	private static Trace craeteTrace(String code){
		int[] cord=Arrays.stream(code.split(",")).mapToInt((s)->Integer.parseInt(s)).toArray();
		List<TracePoint> points=new ArrayList<>(cord.length/2);
		for(int i=0;i<cord.length;i+=2){
			points.add(new TracePoint(cord[i],cord[i+1]));
		}
		return new Trace(points);
	}
	/**
	 * Regenerate InkML for recognized JIIX
	 *
	 * @param directory the location of recognition result
	 * @param lg if the output is used to generate LG file
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static void generate(File directory,boolean lg) throws IOException,ParserConfigurationException,SAXException{
		Map<String,String> gt=Files.lines(new File(directory,"list").toPath()).
				filter((line)->line.contains(", ")).
				map((line)->line.split(", ")).
				collect(Collectors.toMap((b)->b[1],(b)->b[0]));
		File inkDirectory0=new File(directory,"result_inkml");
		File inkDirectory=new File(directory,lg?"result_mml":"result_inkml");
		inkDirectory.mkdirs();
		File jiixDirectory=new File(directory,"result_jiix");
		jiixDirectory.mkdirs();
		for(File jiixFile:jiixDirectory.listFiles()){
			File inkfile0=new File(inkDirectory0,jiixFile.getName().replace(".jiix",".inkml"));
			File inkfile=new File(inkDirectory,jiixFile.getName().replace(".jiix",lg?".mml":".inkml"));
			String reference=gt.get(inkfile0.getCanonicalPath());
			Ink ink=new Ink(new File(reference));
			String ui=ink.getMeta().get("UI");
			String jiix=new String(Files.readAllBytes(jiixFile.toPath()),StandardCharsets.UTF_8);
			Files.write(inkfile.toPath(),Arrays.asList(convert(jiix,ui,lg,ink)),StandardCharsets.UTF_8);
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
	 * @param ink ink
	 * @return
	 */
	public static String convert(String jiix,String name,boolean lg,Ink ink){
		StringBuilder buf=new StringBuilder();
		buf.append("<ink xmlns=\"http://www.w3.org/2003/InkML\">\n<annotation type=\"UI\">");
		buf.append(name);
		buf.append("</annotation>\n<annotationXML type=\"truth\" encoding=\"Content-MathML\">\n<math xmlns='http://www.w3.org/1998/Math/MathML'>");
		try{
			Expression expr=new MyJiixFormat(ink).decode(jiix);
			InkmlGenerator.encodeMathml(expr,buf,lg?InkmlGenerator.RENAME_LG:InkmlGenerator.RENAME);
		}catch(Exception e){
			Logger.getGlobal().log(Level.INFO,name,e);
		}
		buf.append("</math></annotationXML>");
		buf.append("</ink>");
		return buf.toString();
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
	public static void main(String[] args) throws Exception{
//		exportTrace(new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task1_onlineRec/MainTask_formula/valid/TestEM2014GT_INKMLs"),
//				new File(Crohme.DIRECTORY_RESULT+"/struct2014/result_stroke"),
//				Extractor.DEFAULT);
//		for(String string:SAMPLES.keySet()){
//			TracerTests.exportTrace(SAMPLES.get(string),
//					new File(Crohme.DIRECTORY_RESULT+"/struct2014/"+string.replace('\\','_')+".json"));
//		}
		generate(new File(Crohme.DIRECTORY_RESULT+"/struct2014"),false);
	}
}
class MyJiixFormat implements Format{
	private final ObjectMapper mapper=new ObjectMapper();
	private final BoundBox inkBox;
	private final Ink ink;
	private double eX=Double.MAX_VALUE, eY=Double.MAX_VALUE, eB=-Double.MAX_VALUE, eR=-Double.MAX_VALUE, eWidth, eHeight;
	public MyJiixFormat(Ink ink){
		Iterator<Pair<BoundBox,String>> iterator=ink.getAnnotions().iterator();
		BoundBox box=iterator.next().getKey();
		while(iterator.hasNext()){
			box=BoundBox.union(box,iterator.next().getKey());
		}
		inkBox=box;
		this.ink=ink;
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
			fixBoundBox(expressions.get(0));
			eWidth=eR-eX;
			eHeight=eB-eY;
			return decode(expressions.get(0));
		}else{
			List<List<Expression>> rows=new ArrayList<>(expressions.size());
			for(Map<String,Object> expression:expressions){
				rows.add(Arrays.asList(decode(expression)));
			}
			return new Expression.Matrix(rows);
		}
	}
	private void fixBoundBox(Map<String,Object> code){
		Map<String,Number> box=(Map<String,Number>)code.get("bounding-box");
		if(box!=null){
			double x=box.get("x").doubleValue();
			double y=box.get("y").doubleValue();
			double r=x+box.get("width").doubleValue();
			double b=y+box.get("height").doubleValue();
			if(x<eX){
				eX=x;
			}
			if(y<eY){
				eY=y;
			}
			if(r>eR){
				eR=r;
			}
			if(b>eB){
				eB=b;
			}
		}
		List<Map<String,Object>> operands=(List<Map<String,Object>>)code.get("operands");
		if(operands!=null){
			for(Map<String,Object> operand:operands){
				fixBoundBox(operand);
			}
		}
	}
	private String getSymbol(Map<String,Object> code,String def){
		Map<String,Number> box=(Map<String,Number>)code.get("bounding-box");
		if(box==null||"-".equals(def)){
			return def;
		}
		double x=box.get("x").doubleValue();
		double y=box.get("y").doubleValue();
		double width=box.get("width").doubleValue();
		double height=box.get("height").doubleValue();
		BoundBox location=new BoundBox(
				(int)(inkBox.getLeft()+(x-eX)*inkBox.getWidth()/eWidth),
				(int)(inkBox.getLeft()+(x+width-eX)*inkBox.getWidth()/eWidth),
				(int)(inkBox.getTop()+(y-eY)*inkBox.getHeight()/eHeight),
				(int)(inkBox.getTop()+(y+height-eY)*inkBox.getHeight()/eHeight));
		String symbol=ink.getSymbol(location);
		return symbol!=null?symbol:def;
	}
	private Expression decode(Map<String,Object> code){
		String type=(String)code.get("type");
		String label=(String)code.get("label");
		List<Map<String,Object>> operands=(List<Map<String,Object>>)code.get("operands");
		switch(type){
			case "number":
			case "symbol":
				if(label.length()<=1||label.equals("lim")||label.equals("log")||label.equals("sin")||label.equals("cos")||label.equals("tan")){
					return new Expression.Symbol(getSymbol(code,label));
				}else{
					return new Expression.Line(label.codePoints().mapToObj((c)->new Expression.Symbol(new String(new int[]{c},0,1))).collect(Collectors.toList()));
				}
			case "function":
				if(label==null){
					System.out.println(code);
				}
				if(operands.isEmpty()){
					return new Expression.Symbol(getSymbol(code,label));
				}else{
					ArrayList<Expression> spans=new ArrayList<>(operands.size()+1);
					spans.add(new Expression.Symbol(getSymbol(code,label)));
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
				return new Expression.Line(decode(operands.get(0)),new Expression.Symbol(getSymbol(code,"!")));
			case "percentage":
				return new Expression.Line(decode(operands.get(0)),new Expression.Symbol(getSymbol(code,(String)code.get("operator"))),decode(operands.get(1)),new Expression.Symbol("%"));
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
				return new Expression.Under(decode(operands.get(0)),decode(operands.get(1)));
			case "overscript":
				return new Expression.Over(decode(operands.get(0)),decode(operands.get(1)));
			case "underoverscript":
				return new Expression.UnderOver(decode(operands.get(0)),decode(operands.get(1)),decode(operands.get(2)));
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
				return new Expression.Line(decode(operands.get(0)),new Expression.Symbol(getSymbol(code,"/")),decode(operands.get(1)));
			default:
				if(operands.isEmpty()){
					return new Expression.Symbol(getSymbol(code,type));
				}else if(operands.size()==1){
					return new Expression.Line(new Expression.Symbol(getSymbol(code,type)),decode(operands.get(0)));
				}else{
					ArrayList<Expression> spans=new ArrayList<>(operands.size()*2-1);
					if(!operands.isEmpty()){
						Iterator<Map<String,Object>> iterator=operands.iterator();
						spans.add(decode(iterator.next()));
						while(iterator.hasNext()){
							spans.add(new Expression.Symbol(getSymbol(code,type)));
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
}
