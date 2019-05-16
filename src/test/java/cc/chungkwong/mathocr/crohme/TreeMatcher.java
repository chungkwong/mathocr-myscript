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
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
/**
 * Matcher for MathML code
 *
 * @author Chan Chung Kwong
 */
public class TreeMatcher{
	private static final String EVALUATOR=Crohme.DIRECTORY_2016+"/evaluationTools/crohmelib/bin/evalInkml_v1.12.pl";
	/**
	 * Check if the two expression match in the sense of CROHME
	 *
	 * @param groundTruth Ground truth MathML
	 * @param recognized Recognized MathML
	 * @return result of matching
	 * @throws IOException
	 */
	public static MatchResult match(String groundTruth,String recognized) throws IOException{
		String status="Wrong";
		Path gt=createInkML(groundTruth);
		Path re=createInkML(recognized);
		Iterator<String> result=new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(EVALUATOR+" "+gt.toFile().getCanonicalPath()+" "+re.toFile().getCanonicalPath()+" -R").getInputStream(),StandardCharsets.UTF_8)).lines().iterator();
		while(result.hasNext()){
			String next=result.next();
			String[] parts=next.split("\\s+");
			if(parts.length>=7){
				Files.delete(gt);
				Files.delete(re);
				int labelingErrorCount=-1;
				if("100.00".equals(parts[3])){
					labelingErrorCount=0;
				}else if("100.00".equals(parts[4])){
					labelingErrorCount=1;
				}else if("100.00".equals(parts[5])){
					labelingErrorCount=2;
				}else if("100.00".equals(parts[6])){
					labelingErrorCount=3;
				}
				return new MatchResult("100.00".equals(parts[7]),labelingErrorCount);
			}
		}
		throw new RuntimeException("Check your crohmelib installion");
	}
	/**
	 * Check if the two expression match in the sense of CROHME and print the
	 * result to standard output
	 *
	 * @param groundTruth Ground truth MathML
	 * @param recognized Recognized MathML
	 * @throws IOException
	 */
	public static void check(String groundTruth,String recognized) throws IOException{
		System.out.println("Ground truth: "+groundTruth);
		System.out.println("Recognized: "+recognized);
		System.out.println(match(groundTruth,recognized));
		System.out.println();
	}
	private static Path createInkML(String content) throws IOException{
		Path file=Files.createTempFile(null,".inkml");
		String before="<ink xmlns=\"http://www.w3.org/2003/InkML\">\n<annotation type=\"UI\">junk</annotation>\n<annotationXML type=\"truth\" encoding=\"Content-MathML\">\n<math xmlns='http://www.w3.org/1998/Math/MathML'>";
		String after="</math></annotationXML></ink>";
		Files.write(file,Arrays.asList(before,content,after),StandardCharsets.UTF_8);
		return file;
	}
	/**
	 * Result of matching
	 */
	public static class MatchResult{
		private final boolean StructuralCorrect;
		private final int labelingErrorCount;
		/**
		 * Wrap result of matching
		 *
		 * @param StructuralCorrect if the structure is correct
		 * @param labelingErrorCount number of labeling errors
		 */
		public MatchResult(boolean StructuralCorrect,int labelingErrorCount){
			this.StructuralCorrect=StructuralCorrect;
			this.labelingErrorCount=labelingErrorCount;
		}
		/**
		 *
		 * @return if the structure is correct
		 */
		public int getLabelingErrorCount(){
			return labelingErrorCount;
		}
		/**
		 *
		 * @return number of labeling errors, -1 if unknown
		 */
		public boolean isStructuralCorrect(){
			return StructuralCorrect;
		}
		@Override
		public String toString(){
			if(labelingErrorCount==0){
				return "Exact match";
			}
			String string=StructuralCorrect?"Structural correct":"Structural incorrect";
			if(labelingErrorCount>0){
				string+=", number of labeling errors: "+labelingErrorCount;
			}
			return string;
		}
	}
	public static void main(String[] args) throws IOException{
		check("<mo>b</mo>","<mi>b</mi>");//Exact
		check("<mo>c</mo>","<mi>b</mi>");//1//
		check("<mn>c</mn>","<mi>c</mi>");//Exact
		check("<mn>c</mn>","<mi>d</mi>");//1
		check("<mrow><mi>b</mi></mrow>","<mi>b</mi>");//Exact
		check("<mrow><mi>b</mi></mrow>","<mrow><mo>a</mo><mi>a</mi></mrow>");//Wrong
		check("<mi>sin</mi>","<mi>\\sin</mi>");//1//
		check("<mi>sin</mi>","<mrow><mi>s</mi><mi>i</mi><mi>n</mi></mrow>");//Wrong
		check("<mrow><mi>s</mi><mi>i</mi><mi>n</mi></mrow>","<mrow><mi>s</mi><mrow><mi>i</mi><mi>n</mi></mrow></mrow>");//Exact
		check("<mrow><mi>s</mi><mi>i</mi><mi>n</mi></mrow>","<mrow><mrow><mi>s</mi><mi>i</mi></mrow><mi>n</mi></mrow>");//Exact
		check("<msqrt><mi>a</mi><mi>b</mi></msqrt>","<msqrt><mrow><mi>a</mi><mi>b</mi></mrow></msqrt>");//Exact
		check("<mrow><msup><mi>m</mi><mn>3</mn></msup></mrow>","<msup><mi>m</mi><mn>3</mn></msup>");//Exact
	}
}
