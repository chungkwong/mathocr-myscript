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
import cc.chungkwong.mathocr.ui.*;
import java.awt.image.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javax.imageio.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
/**
 * Batch recognition
 *
 * @author Chan Chung Kwong
 */
public class RecognizerTests{
	/**
	 * Render strokes and than recognize
	 *
	 * @param input ground truth
	 * @param directory location of results
	 * @param configuration configuration of recognizer
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws javax.xml.parsers.ParserConfigurationException
	 * @throws org.xml.sax.SAXException
	 */
	public static void recognizeByRender(Stream<Ink> input,File directory,Extractor configuration) throws IOException,InterruptedException,ParserConfigurationException,SAXException{
		recognize(input.map((ink)->new Pair<>(TraceListViewer.renderImage(ink.getTraceList()),ink.getFile())),directory,configuration);
	}
	/**
	 * Recognize images
	 *
	 * @param images directory of images
	 * @param inkmls directory of InkML
	 * @param directory location of output
	 * @param configuration configuration of recognizer
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static void recognizeImage(File images,File inkmls,File directory,Extractor configuration) throws IOException,InterruptedException,ParserConfigurationException,SAXException{
		//AffineTransformOp op=new AffineTransformOp(AffineTransform.getScaleInstance(1.5,1.5),AffineTransformOp.TYPE_BILINEAR);
		recognize(Files.list(inkmls.toPath()).filter((path)->path.getFileName().toString().endsWith(".inkml")).map((path)->{
			File imageFile=new File(images,path.getFileName().toString().replace(".inkml",".png"));
			try{
				return new Pair<>(ImageIO.read(imageFile),path.toFile());
				//return new Pair<>(op.filter(ImageIO.read(imageFile),null),path.toFile());
			}catch(IOException ex){
				Logger.getLogger(RecognizerTests.class.getName()).log(Level.SEVERE,null,ex);
				throw new RuntimeException();
				//return new Pair<>(new BufferedImage(10,10,BufferedImage.TYPE_BYTE_BINARY),path.toFile());
			}
		}),directory,configuration);
	}
	/**
	 * Recognize images
	 *
	 * @param images directory of images
	 * @param directory location of output
	 * @param configuration configuration of recognizer
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static void recognizeImage(File images,File directory,Extractor configuration) throws IOException,InterruptedException,ParserConfigurationException,SAXException{
		//AffineTransformOp op=new AffineTransformOp(AffineTransform.getScaleInstance(1.5,1.5),AffineTransformOp.TYPE_BILINEAR);
		recognize(Files.list(images.toPath()).filter((path)->path.getFileName().toString().endsWith(".png")).map((path)->{
			File imageFile=path.toFile();
			try{
				return new Pair<>(ImageIO.read(imageFile),path.toFile());
				//return new Pair<>(op.filter(ImageIO.read(imageFile),null),path.toFile());
			}catch(IOException ex){
				Logger.getLogger(RecognizerTests.class.getName()).log(Level.SEVERE,null,ex);
				throw new RuntimeException();
				//return new Pair<>(new BufferedImage(10,10,BufferedImage.TYPE_BYTE_BINARY),path.toFile());
			}
		}),directory,configuration);
	}
	/**
	 * Recognize images
	 *
	 * @param input pair of image and InkML file
	 * @param directory location of output
	 * @param configuration configuration of recognizer
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static void recognize(Stream<Pair<BufferedImage,File>> input,File directory,Extractor configuration) throws IOException,InterruptedException,ParserConfigurationException,SAXException{
		ExpressionFormat jiix=new JiixFormat();
		File inkDirectory=new File(directory,"result_inkml");
		inkDirectory.mkdirs();
		File jiixDirectory=new File(directory,"result_jiix");
		jiixDirectory.mkdirs();
		try(BufferedWriter lst=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(directory,"list")),StandardCharsets.UTF_8))){
			int count=0;
			for(Iterator<Pair<BufferedImage,File>> iterator=input.iterator();iterator.hasNext();){
				Pair<BufferedImage,File> next=iterator.next();
				BufferedImage image=next.getKey();
				File src=next.getValue();
				System.out.println(count+++":"+src.getName());
				String fileName=src.getName().substring(0,src.getName().indexOf('.'));
				File jiixfile=new File(jiixDirectory,fileName+".jiix");
				File inkfile=new File(inkDirectory,fileName+".inkml");
				lst.write(src.getCanonicalPath());
				lst.write(", ");
				lst.write(inkfile.getCanonicalPath());
				lst.write("\n");
				if(jiixfile.exists()){
					continue;
				}
				EncodedExpression formula=configuration.recognize(image);
				Files.write(jiixfile.toPath(),Arrays.asList(formula.getCodes(jiix)),StandardCharsets.UTF_8);
				String ui=src.getName().endsWith(".inkml")?new Ink(src).getMeta().get("UI"):"null";
				Files.write(inkfile.toPath(),Arrays.asList(InkmlGenerator.convert(formula.getCodes(jiix),ui,false)),StandardCharsets.UTF_8);
				Thread.sleep((long)(5000*Math.random()));
			}
		}
	}
	public static void main(String[] args) throws Exception{
		recognizeByRender(Crohme.getTestStream2016(),new File("../datasets/tracer/2016"),Extractor.getDefault());
//		recognizeImage(new File("../datasets/crohme2019/Task1_and_Task2/Task1_and_Task2/Task2_offlineRec/MainTask_formula/valid/data_png_TestEM2014GT_INKMLs"),
//				new File("../datasets/TC11_package/CROHME2014_data/TestEM2014GT"),
//				new File("../datasets/tracer/img2014"),
//				Configuration.DEFAULT);
	}
}
