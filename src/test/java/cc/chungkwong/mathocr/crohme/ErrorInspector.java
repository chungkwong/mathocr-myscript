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
import cc.chungkwong.mathocr.online.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.regex.*;
import java.util.stream.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
/**
 * Generate HTML reports on recognition result
 *
 * @author Chan Chung Kwong
 */
public class ErrorInspector{
	/**
	 * Generate reports for expressions having different accuracy on stroke
	 * extraction
	 *
	 * @param directory Location of recognition results
	 * @param strokeDirectory location of stroke files
	 * @param scale scale
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void inspectByExtraction(File directory,File strokeDirectory,double scale) throws IOException,InterruptedException{
		double[] lower={0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
		double[] upper={0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0,1.1};
		HashMap<String,Double> fs=new HashMap<>();
		int threhold=(int)(TracerTests.THREHOLD*scale*scale);
		Files.lines(new File(directory,"list").toPath()).
				filter((line)->line.contains(", ")).
				forEach((line)->{
					try{
						String[] s=line.split(", ");
						TraceList groundTruth=new Ink(new File(s[0])).getTraceList();
						TraceList extracted=TracerTests.importTrace(new File(strokeDirectory,new File(s[1]).getName().replace("inkml","json")));
						fs.put(s[0],getF(TracerTests.rescale(groundTruth,TracerTests.IMAGE_BOX.scale(scale)),extracted,threhold));
					}catch(IOException|ParserConfigurationException|SAXException ex){
						Logger.getLogger(ErrorInspector.class.getName()).log(Level.SEVERE,null,ex);
					}
				});
		for(int i=0;i<11;i++){
			inspectByExtraction(directory,lower[i],upper[i],fs);
		}
	}
	/**
	 * Generate reports for expressions having different accuracy on stroke
	 * extraction
	 *
	 * @param directory Location of recognition results
	 * @throws IOException
	 * @throws InterruptedException
	 */
//	public static void inspectByExtraction(File directory) throws IOException,InterruptedException{
//		double[] lower={0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
//		double[] upper={0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0,1.1};
//		HashMap<String,Double> fs=new HashMap<>();
//		Files.lines(new File(directory,"list").toPath()).
//				filter((line)->line.contains(", ")).
//				forEach((line)->{
//					try{
//						String[] s=line.split(", ");
//						TraceList groundTruth=new Ink(new File(s[0])).getTraceList();
//						TraceList extracted=TracerTests.getJiixTraceList(new File(s[1].replace("inkml","jiix")));
//						fs.put(s[0],getF(TracerTests.rescale(groundTruth,TracerTests.IMAGE_BOX),extracted));
//					}catch(IOException|ParserConfigurationException|SAXException ex){
//						Logger.getLogger(ErrorInspector.class.getName()).log(Level.SEVERE,null,ex);
//					}
//				});
//		for(int i=0;i<11;i++){
//			inspectByExtraction(directory,lower[i],upper[i],fs);
//		}
//	}
	private static double getF(TraceList gt,TraceList re,int threhold){
		int actual=gt.getTraces().size();
		int found=re.getTraces().size();
		int matched=TracerTests.getMatchedCount(gt,re,threhold);
		//System.err.println(2*matched/(actual+found+0.0));
		return 2*matched/(actual+found+0.0);
	}
	private static void inspectByExtraction(File directory,double from,double to,HashMap<String,Double> fs) throws IOException,InterruptedException{
		inspect(directory,(gt,re)->{
			double f=fs.getOrDefault(gt,-1.0);
			return f>=from&&f<to;
		},from+"_error.html");
	}
	/**
	 * Generate reports for expressions having different number of symbols
	 *
	 * @param directory Location of recognition results
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void inspectByComplexity(File directory) throws IOException,InterruptedException{
		for(int i=2;i<25;i++){
			inspectByComplexity(directory,i);
		}
	}
	private static void inspectByComplexity(File directory,int complexity) throws IOException,InterruptedException{
		inspect(directory,(gt,re)->{
			try{
				return countSymbol(getMathML(gt))==complexity;
			}catch(IOException ex){
				Logger.getLogger(ErrorInspector.class.getName()).log(Level.SEVERE,null,ex);
				return false;
			}
		},complexity+"_error.html");
	}
	/**
	 * Count the number of symbols in a MathML expression
	 *
	 * @param mathml expression
	 * @return count
	 */
	public static int countSymbol(String mathml){
		return countSymbol(new MathmlFormat().decode(mathml));
	}
	/**
	 * Count the number of symbols in a expression
	 *
	 * @param expression expression
	 * @return count
	 */
	public static int countSymbol(Expression expression){
		if(expression==null){
			return 0;
		}else if(expression instanceof Expression.Symbol){
			return 1;
		}else if(expression instanceof Expression.Line){
			return ((Expression.Line)expression).getSpans().stream().mapToInt((e)->countSymbol(e)).sum();
		}else if(expression instanceof Expression.Subscript){
			return countSymbol(((Expression.Subscript)expression).getBase())+countSymbol(((Expression.Subscript)expression).getSubscript());
		}else if(expression instanceof Expression.Superscript){
			return countSymbol(((Expression.Superscript)expression).getBase())+countSymbol(((Expression.Superscript)expression).getSuperscript());
		}else if(expression instanceof Expression.Subsuperscript){
			return countSymbol(((Expression.Subsuperscript)expression).getBase())
					+countSymbol(((Expression.Subsuperscript)expression).getSubscript())
					+countSymbol(((Expression.Subsuperscript)expression).getSuperscript());
		}else if(expression instanceof Expression.Fraction){
			return 1+countSymbol(((Expression.Fraction)expression).getNumerator())+countSymbol(((Expression.Fraction)expression).getDenominator());
		}else if(expression instanceof Expression.Radical){
			return 1+countSymbol(((Expression.Radical)expression).getRadicand())+countSymbol(((Expression.Radical)expression).getPower());
		}else if(expression instanceof Expression.Matrix){
			return ((Expression.Matrix)expression).getRows().stream().flatMap((row)->row.stream()).mapToInt((e)->countSymbol(e)).sum();
		}else if(expression instanceof Expression.Over){
			return countSymbol(((Expression.Over)expression).getContent())
					+countSymbol(((Expression.Over)expression).getOver());
		}else if(expression instanceof Expression.Under){
			return countSymbol(((Expression.Under)expression).getContent())
					+countSymbol(((Expression.Under)expression).getUnder());
		}else if(expression instanceof Expression.UnderOver){
			return countSymbol(((Expression.UnderOver)expression).getContent())
					+countSymbol(((Expression.UnderOver)expression).getUnder())
					+countSymbol(((Expression.UnderOver)expression).getOver());
		}else{
			System.err.println("Unknown type:"+expression);
			return 1;
		}
	}
	/**
	 * Generate a report
	 *
	 * @param directory Location of recognition results
	 * @param filter only process expression with specified ground truth InkML
	 * and recognized InkML
	 * @param output output file
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void inspect(File directory,BiPredicate<String,String> filter,String output) throws IOException,InterruptedException{
		Path list=Files.createTempFile(null,null);
		Files.write(list,Files.lines(new File(directory,"list").toPath()).
				filter((line)->line.contains(", ")).
				filter((line)->{
					String[] s=line.split(", ");
					return filter.test(s[0],s[1]);
				}).
				collect(Collectors.toList()),StandardCharsets.UTF_8);
		inspect(list.toFile(),new File(directory,output));
		Files.delete(list);
	}
	/**
	 * Generate a report
	 *
	 * @param directory Location of recognition results
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void inspect(File directory) throws IOException,InterruptedException{
		inspect(new File(directory,"list"),new File(directory,"error.html"));
	}
	private static void inspect(File list,File error) throws IOException,InterruptedException{
		Map<String,String> gt=Files.lines(list.toPath()).
				filter((line)->line.contains(", ")).
				map((line)->line.split(", ")).
				collect(Collectors.toMap((b)->b[1],(b)->b[0]));
		String evaluator="/home/kwong/projects/datasets/TC11_package/evaluationTools/crohmelib/bin/evalInkml_v1.12.pl";
		String input=list.getCanonicalPath();
		Iterator<String> result=new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(evaluator+" -L "+input+" -V -R").getInputStream(),StandardCharsets.UTF_8)).lines().iterator();
		try(BufferedWriter lst=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(error),StandardCharsets.UTF_8))){
			Pattern fileLine=Pattern.compile("^(/[^\\s]+)\\s+:\\s+([0-9]+\\.[0-9]+)\\s+([0-9]+\\.[0-9]+)\\s+([0-9]+\\.[0-9]+)\\s+([0-9]+\\.[0-9]+)\\s+([0-9]+\\.[0-9]+)\\s*$");
			lst.write("<!DOCTYPE html>");
			lst.write("<html>");
			lst.write("<head>");
			lst.write("<meta charset='utf-8'>");
			lst.write("<title>");
			lst.write("Error inspector");
			lst.write("</title>");
			lst.write("</head>");
			lst.write("<body>");
			lst.write("<table>");
			lst.write("<thead>");
			lst.write("<tr><th>Correct</th><th>Ground truth</th><th>Recognized</th><th>Ground truth(MathML)</th><th>Recognized(MathML)</th><th>File</th></tr>");
			lst.write("</thead>");
			lst.write("<tbody>");
			while(result.hasNext()){
				String next=result.next();
				Matcher matcher=fileLine.matcher(next);
				if(matcher.matches()){
					lst.write("<tr>");
					lst.write("<td>");
					if(matcher.group(5).equals("100.00")){
						lst.write("Exact");
					}else if(matcher.group(6).equals("100.00")){
						lst.write("Structure");
					}else{
						lst.write("Wrong");
					}
					lst.write("</td>");
					String recognizedFile=matcher.group(1);
					String referenceFile=gt.get(recognizedFile);
					String recognized=getMathML(recognizedFile);
					String reference=getMathML(referenceFile);
					lst.write("<td><math>");
					lst.write(reference);
					lst.write("</math></td>");
					lst.write("<td><math>");
					lst.write(recognized);
					lst.write("</math></td>");
					lst.write("<td>");
					lst.write(escape(reference));
					lst.write("</td>");
					lst.write("<td>");
					lst.write(escape(recognized));
					lst.write("</td>");
					lst.write("<td>");
					lst.write(recognizedFile);
					lst.write("</td>");
					lst.write("</tr>");
				}else if(next.contains(" reco ")){
					break;
				}
			}
			lst.write("</tbody>");
			lst.write("</table>");
			lst.write("<ul>");
			while(result.hasNext()){
				String next=result.next();
				lst.write("<li>");
				lst.write(next);
				lst.write("</li>");
			}
			lst.write("</ul>");
			lst.write("</body>");
			lst.write("</html>");
		}
	}
	static String getMathML(String referenceFile) throws IOException{
		File file=new File(referenceFile);
		if(!file.exists()){
			System.err.println(referenceFile+" do not exist");
			return "<mrow></mrow>";
		}
		String inkml=new String(Files.readAllBytes(file.toPath()),StandardCharsets.UTF_8);
		int offset=inkml.indexOf("<math");
		if(offset==-1){
			System.err.println(referenceFile+" no MathML");
			return "<mrow></mrow>";
		}
		offset=inkml.indexOf('>',offset)+1;
		return inkml.substring(offset,inkml.indexOf("</math>",offset));
	}
	private static String escape(String string){
		return string.replace("&","&amp;").replace("<","&lt;");
	}
	public static void main(String[] args) throws Exception{
//		inspect(new File(Crohme.DIRECTORY_RESULT+"/big2016_crohme"));
//		inspect(new File(Crohme.DIRECTORY_RESULT+"/big2016_std"));
//		inspect(new File(Crohme.DIRECTORY_RESULT+"/big2014"));
//		inspect(new File(Crohme.DIRECTORY_RESULT+"/big2016"));
//		inspect(new File(Crohme.DIRECTORY_RESULT+"/raw2014"));
//		inspect(new File(Crohme.DIRECTORY_RESULT+"/raw2016"));
//		inspect(new File(Crohme.DIRECTORY_RESULT+"/test2014"));
//		inspect(new File(Crohme.DIRECTORY_RESULT+"/test2016"));
		//inspect(new File(Crohme.DIRECTORY_RESULT+"/img2016_small"));
		//inspect(new File(Crohme.DIRECTORY_RESULT+"/2016"));
		//inspectByExtraction(new File("../datasets/tracer/2016"));
		inspectByExtraction(new File(Crohme.DIRECTORY_RESULT+"/test2016"),new File(Crohme.DIRECTORY_RESULT+"/test2016/result_stroke"),1.5);
//		inspectByComplexity(new File(Crohme.DIRECTORY_RESULT+"/test2016"));
	}
}
