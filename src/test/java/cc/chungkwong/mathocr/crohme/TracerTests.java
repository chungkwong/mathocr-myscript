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
import java.awt.geom.*;
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
 * Tests on stroke tracer
 *
 * @author Chan Chung Kwong
 */
public class TracerTests{
	/**
	 * Test by rendering strokes into image and than extract
	 *
	 * @param iterator ground truth
	 * @param configuration configuration of tracer
	 */
	public static void testByRender(Stream<TraceList> iterator,Extractor configuration){
		test(iterator.map((list)->{
			BoundBox boundBox=list.getBoundBox();
			int dx=boundBox.getLeft()-TraceListViewer.MARGIN_H;
			int dy=boundBox.getTop()-TraceListViewer.MARGIN_V;
			TraceList extracted=configuration.extract(TraceListViewer.renderImage(list),false);
			return new Pair<>(list,extracted.translate(dx,dy));
		}),TraceListViewer.THICK*TraceListViewer.THICK*16);
	}
	/**
	 * Test by rendering strokes into image, scale the image and than extract
	 *
	 * @param iterator ground truth
	 * @param configuration configuration of tracer
	 * @param scale scale
	 */
	public static void testByRender(Stream<TraceList> iterator,Extractor configuration,double scale){
		AffineTransformOp op=new AffineTransformOp(AffineTransform.getScaleInstance(scale,scale),AffineTransformOp.TYPE_BILINEAR);
		test(iterator.map((list)->{
			BufferedImage image=op.filter(TraceListViewer.renderImage(list),null);
			TraceList extracted=configuration.extract(image,false);
			return new Pair<>(list.rescale(list.getBoundBox().scale(scale)),extracted);
		}),(int)(TraceListViewer.THICK*TraceListViewer.THICK*16*scale*scale));
	}
	/**
	 * Compare extracted strokes from image with ground truth
	 *
	 * @param inkmls directory containing ground truth
	 * @param images directory containing images
	 * @param configuration configuration of tracer
	 * @throws IOException
	 */
	public static void testImage(File inkmls,File images,Extractor configuration) throws IOException{
		test(Files.list(inkmls.toPath()).filter((path)->path.toString().endsWith(".inkml")).map((Path path)->{
			try{
				Ink ink=new Ink(path.toFile());
				BufferedImage image=ImageIO.read(new File(images,path.toFile().getName().replace(".inkml",".png")));
				//image=new AffineTransformOp(AffineTransform.getScaleInstance(0.5,0.5),AffineTransformOp.TYPE_BILINEAR).filter(image,null);
				TraceList extracted=configuration.extract(image,false);
//				if(extracted.getTraces().stream().mapToInt((t)->t.getBoundBox().getHeight()).max().orElse(0)<80){
//					image=new AffineTransformOp(AffineTransform.getScaleInstance(2,2),AffineTransformOp.TYPE_BILINEAR).filter(image,null);
//					preprocessed=Configuration.DEFAULT.getPreprocessor().apply(image,false);
//					extracted=configuration.getGraphTracer().trace(configuration.getTracer().trace(preprocessed));
//					System.out.println("scaled");
//				}
				TraceList groundtruth=ink.getTraceList();
				return new Pair<>(groundtruth.rescale(IMAGE_BOX.scale(1.5)),extracted);
			}catch(IOException|ParserConfigurationException|SAXException ex){
				Logger.getLogger(TracerTests.class.getName()).log(Level.SEVERE,null,ex);
				return new Pair<>(new TraceList(),new TraceList());
			}
		}),(int)(THREHOLD*1.5*1.5));
	}
	/**
	 * Compare extracted strokes from image with ground truth
	 *
	 * @param inkmls directory containing ground truth
	 * @param images directory containing images
	 * @param configuration configuration of tracer
	 * @param scale
	 * @return accuracy
	 * @throws IOException
	 */
	public static double testImage(File inkmls,File images,Extractor configuration,double scale) throws IOException{
		return test(Files.list(inkmls.toPath()).filter((path)->path.toString().endsWith(".inkml")).map((Path path)->{
			try{
				Ink ink=new Ink(path.toFile());
				BufferedImage image=ImageIO.read(new File(images,path.toFile().getName().replace(".inkml",".png")));
				if(scale!=1.0){
					image=new AffineTransformOp(AffineTransform.getScaleInstance(scale,scale),AffineTransformOp.TYPE_BILINEAR).filter(image,null);
				}
				TraceList extracted=configuration.extract(image,false);
//				if(extracted.getTraces().stream().mapToInt((t)->t.getBoundBox().getHeight()).max().orElse(0)<80){
//					image=new AffineTransformOp(AffineTransform.getScaleInstance(2,2),AffineTransformOp.TYPE_BILINEAR).filter(image,null);
//					preprocessed=Configuration.DEFAULT.getPreprocessor().apply(image,false);
//					extracted=configuration.getGraphTracer().trace(configuration.getTracer().trace(preprocessed));
//					System.out.println("scaled");
//				}
				TraceList groundtruth=ink.getTraceList();
				return new Pair<>(groundtruth.rescale(IMAGE_BOX.scale(scale)),extracted);
			}catch(IOException|ParserConfigurationException|SAXException ex){
				Logger.getLogger(TracerTests.class.getName()).log(Level.SEVERE,null,ex);
				return new Pair<>(new TraceList(),new TraceList());
			}
		}),(int)(THREHOLD*scale*scale));
	}
	/**
	 * Compare extracted strokes from image with ground truth
	 *
	 * @param inkmls directory containing ground truth
	 * @param jiixs directory containing extracted stroke
	 * @param configuration configuration of tracer
	 * @throws IOException
	 */
	public static void testJiix(File inkmls,File jiixs,Extractor configuration) throws IOException{
		test(Files.list(inkmls.toPath()).filter((path)->path.toString().endsWith(".inkml")).map((Path path)->{
			try{
				Ink ink=new Ink(path.toFile());
				TraceList extracted=getJiixTraceList(new File(jiixs,path.toFile().getName().replace(".inkml",".jiix")));
				TraceList groundtruth=ink.getTraceList();
				return new Pair<>(groundtruth.rescale(extracted.getBoundBox()),extracted);
			}catch(IOException|ParserConfigurationException|SAXException ex){
				Logger.getLogger(TracerTests.class.getName()).log(Level.SEVERE,null,ex);
				return new Pair<>(new TraceList(),new TraceList());
			}
		}),THREHOLD);
	}
	static TraceList getJiixTraceList(File jiix) throws IOException{
		TraceList extracted=new TraceList();
		String code=new String(Files.readAllBytes(jiix.toPath()),StandardCharsets.UTF_8);
		collectJiixTrace((List<Map<String,Object>>)new ObjectMapper().readValue(code,Map.class).get("expressions"),extracted);
		return extracted;
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
	 * Compare extracted strokes from image with ground truth
	 *
	 * @param inkmls directory containing ground truth
	 * @param jsons directory containing extracted stroke
	 * @param configuration configuration of tracer
	 * @throws IOException
	 */
	public static void testJson(File inkmls,File jsons,Extractor configuration) throws IOException{
		test(Files.list(inkmls.toPath()).filter((path)->path.toString().endsWith(".inkml")).map((Path path)->{
			try{
				Ink ink=new Ink(path.toFile());
				TraceList extracted=new JsonFormat().read(new File(jsons,path.toFile().getName().replace(".inkml",".json")));
				TraceList groundtruth=ink.getTraceList();
				return new Pair<>(groundtruth.rescale(IMAGE_BOX),extracted);
			}catch(IOException|ParserConfigurationException|SAXException ex){
				Logger.getLogger(TracerTests.class.getName()).log(Level.SEVERE,null,ex);
				return new Pair<>(new TraceList(),new TraceList());
			}
		}),THREHOLD);
	}
	/**
	 * Compare extracted strokes from image with ground truth
	 *
	 * @param inkmls directory containing ground truth
	 * @param jsons directory containing extracted stroke
	 * @param configuration configuration of tracer
	 * @param scale
	 * @throws IOException
	 */
	public static void testJson(File inkmls,File jsons,Extractor configuration,double scale) throws IOException{
		test(Files.list(inkmls.toPath()).filter((path)->path.toString().endsWith(".inkml")).map((Path path)->{
			try{
				Ink ink=new Ink(path.toFile());
				TraceList extracted=new JsonFormat().read(new File(jsons,path.toFile().getName().replace(".inkml",".json")));
				TraceList groundtruth=ink.getTraceList();
				return new Pair<>(groundtruth.rescale(IMAGE_BOX.scale(scale)),extracted);
			}catch(IOException|ParserConfigurationException|SAXException ex){
				Logger.getLogger(TracerTests.class.getName()).log(Level.SEVERE,null,ex);
				return new Pair<>(new TraceList(),new TraceList());
			}
		}),(int)(THREHOLD*scale*scale));
	}
	/**
	 * Start test
	 *
	 * @param stream pairs of ground truth and extracted strokes
	 * @param threhold maximal Hausdorff distance to be counted matched
	 * @return accuracy
	 */
	public static double test(Stream<Pair<TraceList,TraceList>> stream,int threhold){
		int exprCount=0, actualTraceCount=0, resultTraceCount=0, matchTraceCount=0, exact=0;
		long start=System.currentTimeMillis();
		for(Iterator<Pair<TraceList,TraceList>> iterator=stream.iterator();iterator.hasNext();){
//			System.err.println(++exprCount);
			++exprCount;
			Pair<TraceList,TraceList> pair=iterator.next();
			TraceList list0=pair.getKey();
			TraceList list1=pair.getValue();
			int actualCount=list0.getTraces().size();
			actualTraceCount+=actualCount;
			int resultCount=list1.getTraces().size();
			resultTraceCount+=resultCount;
			int matchedCount=getMatchedCount(list0,list1,threhold);
			matchTraceCount+=matchedCount;
			if(matchedCount==actualCount&&matchedCount==resultCount){
				++exact;
			}
		}
		System.out.format("Expr count:%d%n",exprCount);
		System.out.format("Actual trace count:%d%n",actualTraceCount);
		System.out.format("Result trace count:%d%n",resultTraceCount);
		System.out.format("Matched trace count:%d%n",matchTraceCount);
		System.out.format("Exact:%d(%f)%n",exact,exact*1.0/exprCount);
		System.out.format("Recall:%f%n",matchTraceCount*1.0/actualTraceCount);
		System.out.format("Precision:%f%n",matchTraceCount*1.0/resultTraceCount);
		System.out.format("TIME:%dms%n",System.currentTimeMillis()-start);
		return exact*1.0/exprCount;
	}
	//private static final int THREHOLD=TraceListViewer.THICK*TraceListViewer.THICK*2*2*4;
	static final int THREHOLD=2*2*4*4;
	static int getMatchedCount(TraceList list0,TraceList list1,int threhold){
		int matched=0;
		for(Trace trace0:list0.getTraces()){
			int distance=Integer.MAX_VALUE;
			Trace match=null;
			for(Trace trace1:list1.getTraces()){
				int d=TraceList.getDistance(trace0,trace1);
				if(d<distance){
					distance=d;
					match=trace1;
				}
			}
			if(distance<=threhold){
				list1.getTraces().remove(match);
				++matched;
			}
		}
		return matched;
	}
	static final BoundBox IMAGE_BOX=new BoundBox(5,1004,5,1004);
	/**
	 * Store ground truth strokes
	 *
	 * @param inkmls directory containing images
	 * @param jsons directory to place stroke data
	 * @throws IOException
	 * @throws javax.xml.parsers.ParserConfigurationException
	 * @throws org.xml.sax.SAXException
	 */
	public static void exportTrace(File inkmls,File jsons) throws IOException,ParserConfigurationException,SAXException{
		jsons.mkdirs();
		for(File imageFile:inkmls.listFiles()){
			if(!imageFile.getName().endsWith(".inkml")){
				continue;
			}
			File strokeFile=new File(jsons,imageFile.getName().replace(".inkml",".json"));
			exportTrace(new Ink(imageFile).getTraceList(),strokeFile);
		}
	}
	/**
	 * Store extracted strokes
	 *
	 * @param images directory containing images
	 * @param jsons directory to place stroke data
	 * @param configuration configuration of tracer
	 * @throws IOException
	 */
	public static void exportTrace(File images,File jsons,Extractor configuration) throws IOException{
		jsons.mkdirs();
		for(File imageFile:images.listFiles()){
			if(!imageFile.getName().endsWith(".png")){
				continue;
			}
			BufferedImage image=ImageIO.read(imageFile);
			TraceList extracted=configuration.extract(image);
			File strokeFile=new File(jsons,imageFile.getName().replace(".png",".json"));
			exportTrace(extracted,strokeFile);
		}
	}
	/**
	 * Store extracted strokes
	 *
	 * @param images directory containing images
	 * @param jsons directory to place stroke data
	 * @param configuration configuration of tracer
	 * @param scale
	 * @throws IOException
	 */
	public static void exportTrace(File images,File jsons,Extractor configuration,double scale) throws IOException{
		jsons.mkdirs();
		AffineTransformOp affine=new AffineTransformOp(AffineTransform.getScaleInstance(scale,scale),AffineTransformOp.TYPE_BILINEAR);
		int count=0;
		for(File imageFile:images.listFiles()){
			if(!imageFile.getName().endsWith(".png")){
				continue;
			}
			System.out.println(++count);
			BufferedImage image=ImageIO.read(imageFile);
			image=affine.filter(image,null);
			TraceList extracted=configuration.extract(image);
			File strokeFile=new File(jsons,imageFile.getName().replace(".png",".json"));
			exportTrace(extracted,strokeFile);
		}
	}
	/**
	 * Save trace list into a JSON file
	 *
	 * @param extracted the trace list
	 * @param strokeFile the file
	 * @throws IOException
	 */
	public static void exportTrace(TraceList extracted,File strokeFile) throws IOException{
		new JsonFormat().write(extracted,strokeFile);
	}
	public static void main(String[] args) throws Exception{
//		testByRender(Crohme.getValidationStream2016().map((ink)->ink.getTraceList()),Extractor.getDefault());
//		testByRender(Crohme.getTestStream2016().map((ink)->ink.getTraceList()),Extractor.getDefault());
//		testImage(new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task1_onlineRec/MainTask_formula/valid/TestEM2014GT_INKMLs"),
//				new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task2_offlineRec/MainTask_formula/valid/data_png_TestEM2014GT_INKMLs"),
//				Extractor.getDefault(),1.0);
//		testImage(new File(Crohme.DIRECTORY_2016+"/CROHME2016_data/Task-1-Formula/TEST2016_INKML_GT"),
//				new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task2_offlineRec/MainTask_formula/valid/data_png_TEST2016_INKML_GT"),
//				Extractor.getDefault(),1.0);
//		testImage(new File(Crohme.DIRECTORY_2019+"/../Task2_offlineRec/MainTask_formula/Inkmls_Test2019"),
//				new File(Crohme.DIRECTORY_2019+"/../Task2_offlineRec/MainTask_formula/Images_Test2019"),
//				Extractor.getDefault(),1.0);
//		int bestI=-1;
//		double bestK=0;
//		double bestAcc=0;
//		for(int i=21;i<22;i+=2){
//			for(double k=0.35;k<0.45;k+=0.01){
////			if(i%8==0){
////				continue;
////			}
//				System.out.println(i+","+k);
//				Extractor extractor=new Extractor(
//						new CombinedPreprocessor(Arrays.asList(new ToGrayscale(),new LocalBinarizer(LocalBinarizer.getSauvola(k),i))),
//						new ThinTracer(),new GreedyGraphTracer(),new CutOrderer(),new MyscriptRecognizer());
//				double tmp=testImage(new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task1_onlineRec/MainTask_formula/valid/TestEM2014GT_INKMLs"),
//						new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task2_offlineRec/MainTask_formula/valid/data_png_TestEM2014GT_INKMLs"),
//						extractor,1.0);
//				if(tmp>bestAcc){
//					bestAcc=tmp;
//					bestI=i;
//					bestK=k;
//				}
////			testImage(new File(Crohme.DIRECTORY_2016+"/CROHME2016_data/Task-1-Formula/TEST2016_INKML_GT"),
////					new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task2_offlineRec/MainTask_formula/valid/data_png_TEST2016_INKML_GT"),
////					extractor,1.0);
//			}
//		}
//		System.out.println(bestAcc+" : "+bestI+" , "+bestK);
//		testJiix(new File(Crohme.DIRECTORY_2016+"/CROHME2016_data/Task-1-Formula/TEST2016_INKML_GT"),
//				new File(Crohme.DIRECTORY_RESULT+"/2016/result_jiix"),
//				Extractor.getDefault());
//		testJson(new File(Crohme.DIRECTORY_2016+"/CROHME2016_data/Task-1-Formula/TEST2016_INKML_GT"),
//				new File(Crohme.DIRECTORY_RESULT+"/test2016/result_stroke"),
//				Extractor.getDefault(),1.5);
//		testJson(new File(Crohme.DIRECTORY_2016+"/CROHME2014_data/TestEM2014GT"),
//				new File(Crohme.DIRECTORY_RESULT+"/test2014/result_stroke"),
//				Extractor.getDefault(),1.5);
//		exportTrace(new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task2_offlineRec/MainTask_formula/valid/data_png_TestEM2014GT_INKMLs_1000"),
//				new File(Crohme.DIRECTORY_RESULT+"/otsu2014/result_stroke"),
//				Extractor.getDefault(),1.5);
//		exportTrace(new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task2_offlineRec/MainTask_formula/valid/data_png_TEST2016_INKML_GT"),
//				new File(Crohme.DIRECTORY_RESULT+"/otsu2016/result_stroke"),
//				Extractor.getDefault(),1.0);
////		Export online data
//		exportTrace(new File(DIRECTORY_2019+"/Task1_and_Task2/Task1_onlineRec/MainTask_formula/Test"),
//				new File(Crohme.DIRECTORY_RESULT+"/raw2019/result_stroke"));
//		exportTrace(new File(Crohme.DIRECTORY_2016+"/CROHME2016_data/Task-1-Formula/TEST2016_INKML_GT"),
//				new File(Crohme.DIRECTORY_RESULT+"/raw2016/result_stroke"));
//		exportTrace(new File(DIRECTORY_2019+"/Task1_and_Task2/Task1_onlineRec/MainTask_formula/valid/TestEM2014GT_INKMLs"),
//				new File(Crohme.DIRECTORY_RESULT+"/raw2014/result_stroke"));
////		Export offline data
//		exportTrace(new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task2_offlineRec/MainTask_formula/valid/data_png_TestEM2014GT_INKMLs"),
//				new File(Crohme.DIRECTORY_RESULT+"/offline2014/result_stroke"),Extractor.getDefault());
//		exportTrace(new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task2_offlineRec/MainTask_formula/valid/data_png_TEST2016_INKML_GT"),
//				new File(Crohme.DIRECTORY_RESULT+"/offline2016/result_stroke"),Extractor.getDefault());
//		exportTrace(new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task2_offlineRec/MainTask_formula/Test/Images_Test2019"),
//				new File(Crohme.DIRECTORY_RESULT+"/offline2019/result_stroke"),Extractor.getDefault());
	}
}
