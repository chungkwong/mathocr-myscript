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
import cc.chungkwong.mathocr.common.format.*;
import cc.chungkwong.mathocr.offline.extractor.*;
import java.io.*;
import java.nio.charset.*;
import javax.imageio.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
/**
 * Tools to test symbol recognition
 *
 * @author Chan Chung Kwong
 */
public class SymbolTest{
	public static void export(File images,File inkmls,File json,Extractor configuration) throws IOException,ParserConfigurationException,SAXException{
		JsonFormat jsonFormat=new JsonFormat();
		try(BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(json),StandardCharsets.UTF_8))){
			out.write("{\n");
			boolean first=true;
			int count=0;
			for(File imageFile:images.listFiles()){
				if(!imageFile.getName().endsWith(".png")){
					continue;
				}
				if(++count%100==0){
					System.out.println(count);
				}
				if(first){
					first=false;
				}else{
					out.write(',');
				}
				File inkmlFile=new File(inkmls,imageFile.getName().replace(".png",".inkml"));
				out.write('"');
				out.write(new Ink(inkmlFile).getMeta().get("UI"));
				out.write('"');
				out.write(':');
				jsonFormat.write(configuration.extract(ImageIO.read(imageFile)),out);
				out.write('\n');
			}
			out.write('}');
		}
	}
	public static void main(String[] args) throws Exception{
		export(new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task2_offlineRec/subTask_symbols/valid/data_png_testSymbols"),
				new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task1_onlineRec/subTask_symbols/valid/isolated_testSymbols2014/testSymbols"),
				new File(Crohme.DIRECTORY_RESULT+"/single2014.json"),Extractor.getDefault());
	}
}
