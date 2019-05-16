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
import java.util.*;
/**
 * Generate html page to show result
 *
 * @author Chan Chung Kwong
 */
public class ResultInspector{
	public static void inspect(File images,File mml,File report) throws IOException{
		try(BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(report),StandardCharsets.UTF_8))){
			Map<String,String> code=new TreeMap<>();
			for(File image:images.listFiles()){
				code.put("file://"+image.getCanonicalPath(),ErrorInspector.getMathML(new File(mml,image.getName().replace(".png",".mml")).getCanonicalPath()));
			}
			out.write("<!DOCTYPE html>\n"
					+"<html>\n"
					+"	<head>\n"
					+"		<title>Result viewer</title>\n"
					+"		<meta charset=\"UTF-8\">\n"
					+"		<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
					+"		<script>\n"
					+"			var imagePaths = [");
			boolean remain=false;
			for(String string:code.keySet()){
				if(remain){
					out.write(',');
				}else{
					remain=true;
				}
				out.write('"');
				out.write(escape(string));
				out.write('"');
			}
			out.write("];\n"
					+"			var results = [");
			remain=false;
			for(Map.Entry<String,String> string:code.entrySet()){
				if(remain){
					out.write(',');
				}else{
					remain=true;
				}
				out.write('"');
				out.write(escape(string.getValue()));
				out.write('"');
			}
			out.write("];\n"
					+"			var index = -1;\n"
					+"			var correct = 0;\n"
					+"			var next = function (c) {\n"
					+"				if (index >= imagePaths.length) {\n"
					+"					return;\n"
					+"				}\n"
					+"				document.getElementById('total').textContent = ++index;\n"
					+"				if (c) {\n"
					+"					document.getElementById('correct').textContent = ++correct;\n"
					+"				}\n"
					+"				if (index >= imagePaths.length) {\n"
					+"					return;\n"
					+"				}\n"
					+"				document.getElementById('expression').innerHTML = results[index];\n"
					+"				document.getElementById('image').src = imagePaths[index];\n"
					+"			};\n"
					+"		</script>\n"
					+"	</head>\n"
					+"	<body>\n"
					+"		<a onclick=\"next(true)\">Correct</a>(<span id=\"correct\">0</span>/<span id=\"total\">0</span>)\n"
					+"		<a onclick=\"next(false)\">Wrong</a>\n"
					+"		<div><math id=\"expression\"></math></div>\n"
					+"		<img id=\"image\" style=\"max-width: 100vw;max-height: 100vh;\">\n"
					+"		<script>\n"
					+"			next(false);\n"
					+"		</script>\n"
					+"	</body>\n"
					+"</html>\n");
		}
	}
	public static void inspectMml(File mml,File report) throws IOException{
		try(BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(report),StandardCharsets.UTF_8))){
			out.write("<!DOCTYPE html>\n"
					+"<html>\n"
					+"	<head>\n"
					+"		<title>Result viewer</title>\n"
					+"		<meta charset=\"UTF-8\">\n"
					+"		<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
					+"  </head><body><table>");
			for(File file:mml.listFiles()){
				out.write("<tr>");
				out.write("<td><math>");
				out.write(ErrorInspector.getMathML(file.getCanonicalPath()));
				out.write("</math></td>");
				out.write("<td>");
				out.write(escape(file.getName()));
				out.write("</td>");
				out.write("</tr>");
			}
			out.write("</table></body>\n"
					+"</html>\n");
		}
	}
	public static void diff(File images,File as,File bs,File report) throws IOException{
		try(BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(report),StandardCharsets.UTF_8))){
			out.write("<!DOCTYPE html>\n"
					+"<html>\n"
					+"	<head>\n"
					+"		<title>Result viewer</title>\n"
					+"		<meta charset=\"UTF-8\">\n"
					+"		<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
					+"  </head><body><table>");
			for(File af:as.listFiles()){
				String a=ErrorInspector.getMathML(af.getCanonicalPath());
				String b=ErrorInspector.getMathML(new File(bs,af.getName()).getCanonicalPath());
				if(!a.equals(b)){
//				if(TreeMatcher.match(a,b).getLabelingErrorCount()>0){
					out.write("<tr>");
					out.write("<td><img width='250' src='");
					out.write(escape(new File(images,af.getName().replace(".mml",".png")).getCanonicalPath()));
					out.write("'></td>");
					out.write("<td><math>");
					out.write(a);
					out.write("</math></td>");
					out.write("<td><math>");
					out.write(b);
					out.write("</math></td>");
					out.write("<td>");
					out.write(escape(af.getName()));
					out.write("</td>");
					out.write("</tr>");
				}
			}
			out.write("</table></body>\n"
					+"</html>\n");
		}
	}
	private static String escape(String string){
		return string.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n");
	}
	public static void main(String[] args) throws Exception{
//		inspect(new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task2_offlineRec/MainTask_formula/valid/data_png_TestEM2014GT_INKMLs"),
//				new File(Crohme.DIRECTORY_RESULT+"/img2014/result_mml"),
//				new File(Crohme.DIRECTORY_RESULT+"/img2014/checker.html"));
//		inspect(new File(Crohme.DIRECTORY_2019+"/../Task2_offlineRec/MainTask_formula/Images_Test2019"),
//				new File(Crohme.DIRECTORY_RESULT+"/2019off/result_mml"),
//				new File(Crohme.DIRECTORY_RESULT+"/2019off/checker.html"));
//		inspectMml(
//				new File(Crohme.DIRECTORY_RESULT+"/2019mml/result_mml"),
//				new File(Crohme.DIRECTORY_RESULT+"/2019mml/checker.html"));
//		inspect(new File(Crohme.DIRECTORY_2019+"/../Task2_offlineRec/MainTask_formula/Images_Test2019"),
//				new File(Crohme.DIRECTORY_RESULT+"/test2019/result_mml"),
//				new File(Crohme.DIRECTORY_RESULT+"/test2019/checker.html"));
//		inspectMml(
//				new File(Crohme.DIRECTORY_RESULT+"/test2019/result_mml"),
//				new File(Crohme.DIRECTORY_RESULT+"/test2019/list.html"));
		diff(new File(Crohme.DIRECTORY_2019+"/../Task2_offlineRec/MainTask_formula/Images_Test2019"),
				new File(Crohme.DIRECTORY_RESULT+"/2019off/result_mml"),
				new File(Crohme.DIRECTORY_RESULT+"/off2019/result_mml"),
				new File(Crohme.DIRECTORY_RESULT+"/off2019/diff.html"));
	}
}
