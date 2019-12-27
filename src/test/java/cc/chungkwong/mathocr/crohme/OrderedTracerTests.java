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
import cc.chungkwong.mathocr.ui.*;
import com.fasterxml.jackson.databind.*;
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
 * Tests on stroke extractor
 *
 * @author Chan Chung Kwong
 */
public class OrderedTracerTests{
	/**
	 * Test by rendering the strokes and than do extraction
	 *
	 * @param iterator ground truth
	 * @param configuration configuration of stroke extraction
	 */
	public static void testByRender(Stream<TraceList> iterator,Extractor configuration){
		test(iterator.map((list)->{
			BoundBox boundBox=list.getBoundBox();
			int dx=boundBox.getLeft()-TraceListViewer.MARGIN_H;
			int dy=boundBox.getTop()-TraceListViewer.MARGIN_V;
			TraceList extracted=configuration.extract(TraceListViewer.renderImage(list),true);
			return new Pair<>(list,extracted.translate(dx,dy));
		}));
	}
	/**
	 * Test by extracting strokes from images
	 *
	 * @param inkmls ground truth
	 * @param images image to be extracted
	 * @param configuration configuration of stroke extraction
	 * @throws java.io.IOException
	 */
	public static void testImage(File inkmls,File images,Extractor configuration) throws IOException{
		test(Files.list(inkmls.toPath()).filter((path)->path.toString().endsWith(".inkml")).map((Path path)->{
			try{
				Ink ink=new Ink(path.toFile());
				BufferedImage image=ImageIO.read(new File(images,path.toFile().getName().replace(".inkml",".png")));
				//image=new AffineTransformOp(AffineTransform.getScaleInstance(0.5,0.5),AffineTransformOp.TYPE_BILINEAR).filter(image,null);
				TraceList extracted=configuration.extract(image,true);
//				if(extracted.getTraces().stream().mapToInt((t)->t.getBoundBox().getHeight()).max().orElse(0)<80){
//					image=new AffineTransformOp(AffineTransform.getScaleInstance(2,2),AffineTransformOp.TYPE_BILINEAR).filter(image,null);
//					preprocessed=Configuration.DEFAULT.getPreprocessor().apply(image,false);
//					extracted=configuration.getGraphTracer().trace(configuration.getTracer().trace(preprocessed));
//					System.out.println("scaled");
//				}
				TraceList groundtruth=ink.getTraceList();
				return new Pair<>(groundtruth.rescale(TracerTests.IMAGE_BOX),extracted);
			}catch(IOException|ParserConfigurationException|SAXException ex){
				Logger.getLogger(TracerTests.class.getName()).log(Level.SEVERE,null,ex);
				return new Pair<>(new TraceList(),new TraceList());
			}
		}));
	}
	/**
	 * Test by comparing InkML and JIIX
	 *
	 * @param inkmls ground truth
	 * @param jiixs strokes extracted in JIIX format
	 * @param configuration configuration of stroke extraction
	 * @throws java.io.IOException
	 */
	public static void testJiix(File inkmls,File jiixs,Extractor configuration) throws IOException{
		test(Files.list(inkmls.toPath()).filter((path)->path.toString().endsWith(".inkml")).map((Path path)->{
			try{
				Ink ink=new Ink(path.toFile());
				TraceList extracted=new TraceList();
				//String code=new String(Files.readAllBytes(new File(jiixs,path.toFile().getName().replace(".inkml",".jiix")).toPath()),StandardCharsets.UTF_8);
				String code=new String(Files.readAllBytes(new File(jiixs,path.toFile().getName().replace(".inkml",".jiix")).toPath()),StandardCharsets.UTF_8);
				collectJiixTrace((List<Map<String,Object>>)new ObjectMapper().readValue(code,Map.class).get("expressions"),extracted);
				TraceList groundtruth=ink.getTraceList();
				return new Pair<>(groundtruth.rescale(extracted.getBoundBox()),extracted);
			}catch(IOException|ParserConfigurationException|SAXException ex){
				Logger.getLogger(TracerTests.class.getName()).log(Level.SEVERE,null,ex);
				return new Pair<>(new TraceList(),new TraceList());
			}
		}));
	}
	private static void collectJiixTrace(List<Map<String,Object>> expressions,TraceList list){
		if(expressions!=null){
			for(Map<String,Object> expression:expressions){
				collectJiixTrace(expression,list);
			}
		}
	}
	private static void collectJiixTrace(Map<String,Object> code,TraceList list){
		List<Map<String,Object>> items=(List<Map<String,Object>>)code.get("items");
		if(items!=null){
			for(Map<String,Object> item:items){
				List<Number> xs=(List<Number>)item.get("X");
				List<Number> ys=(List<Number>)item.get("Y");
				ArrayList<TracePoint> points=new ArrayList<>(xs.size());
				for(int i=0;i<xs.size();i++){
					int x=(int)(xs.get(i).doubleValue()*100);
					int y=(int)(ys.get(i).doubleValue()*100);
					points.add(new TracePoint(x,y));
				}
				list.getTraces().add(new Trace(points));
			}
		}
		collectJiixTrace((List<Map<String,Object>>)code.get("operands"),list);
	}
	/**
	 * Test by comparing InkML and JIIX
	 *
	 * @param inkmls ground truth
	 * @param jsons strokes extracted in JSON format
	 * @param scale
	 * @throws java.io.IOException
	 */
	public static void testJson(File inkmls,File jsons,double scale) throws IOException{
		JsonFormat jsonFormat=new JsonFormat();
		test(Files.list(inkmls.toPath()).filter((path)->path.toString().endsWith(".inkml")).map((Path path)->{
			try{
				Ink ink=new Ink(path.toFile());
				TraceList extracted=jsonFormat.read(new File(jsons,path.toFile().getName().replace(".inkml",".json")));
				//String code=new String(Files.readAllBytes(new File(jiixs,path.toFile().getName().replace(".inkml",".jiix")).toPath()),StandardCharsets.UTF_8);
				TraceList groundtruth=ink.getTraceList();
				return new Pair<>(groundtruth.rescale(TracerTests.IMAGE_BOX.scale(scale)),extracted);
			}catch(IOException|ParserConfigurationException|SAXException ex){
				Logger.getLogger(TracerTests.class.getName()).log(Level.SEVERE,null,ex);
				return new Pair<>(new TraceList(),new TraceList());
			}
		}));
	}
	/**
	 * Compare ground truth and extracted strokes
	 *
	 * @param stream pairs of ground truth and extracted strokes
	 */
	public static void test(Stream<Pair<TraceList,TraceList>> stream){
		int exprCount=0, exact=0;
		long start=System.currentTimeMillis();
		for(Iterator<Pair<TraceList,TraceList>> iterator=stream.iterator();iterator.hasNext();){
			Pair<TraceList,TraceList> pair=iterator.next();
			TraceList list0=pair.getKey();
			TraceList list1=pair.getValue();
			++exprCount;
			if(list0.getTraces().size()==list1.getTraces().size()){
				++exact;
				for(int i=0;i<list0.getTraces().size();i++){
					if(TraceList.getDistance(list0.getTraces().get(i),list1.getTraces().get(i))>THREHOLD){
						--exact;
						break;
					}
				}
			}
		}
		System.out.format("Expr count:%d%n",exprCount);
		System.out.format("Exact:%d(%f)%n",exact,exact*1.0/exprCount);
		System.out.format("TIME:%dms%n",System.currentTimeMillis()-start);
	}
	private static final int THREHOLD=2*2*4*4;
	public static void main(String[] args) throws Exception{
//		testByRender(Crohme.getValidationStream2016().map((ink)->ink.getTraceList()),Configuration.DEFAULT);
//		testByRender(Crohme.getTestStream2016().map((ink)->ink.getTraceList()),Extractor.DEFAULT);
		testImage(new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task1_onlineRec/MainTask_formula/valid/TestEM2014GT_INKMLs"),
				new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task2_offlineRec/MainTask_formula/valid/data_png_TestEM2014GT_INKMLs"),
				Extractor.DEFAULT);
		testImage(new File(Crohme.DIRECTORY_2016+"/CROHME2016_data/Task-1-Formula/TEST2016_INKML_GT"),
				new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task2_offlineRec/MainTask_formula/valid/data_png_TEST2016_INKML_GT"),
				Extractor.DEFAULT);
		testImage(new File(Crohme.DIRECTORY_2019+"/../Task2_offlineRec/MainTask_formula/Inkmls_Test2019"),
				new File(Crohme.DIRECTORY_2019+"/../Task2_offlineRec/MainTask_formula/Images_Test2019"),
				Extractor.DEFAULT);
//		testJiix(new File(Crohme.DIRECTORY_2016+"/CROHME2016_data/Task-1-Formula/TEST2016_INKML_GT"),
//				new File(Crohme.DIRECTORY_RESULT+"/2016/result_jiix"),
//				Configuration.DEFAULT);
//		testJson(new File(Crohme.DIRECTORY_2016+"/CROHME2016_data/Task-1-Formula/TEST2016_INKML_GT"),
//				new File(Crohme.DIRECTORY_RESULT+"/test2016/result_stroke"),
//				1.5);
//		testJson(new File(Crohme.DIRECTORY_2016+"/CROHME2014_data/TestEM2014GT"),
//				new File(Crohme.DIRECTORY_RESULT+"/test2014/result_stroke"),
//				1.5);
	}
}
