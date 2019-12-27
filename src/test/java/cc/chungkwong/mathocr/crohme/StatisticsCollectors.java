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
import cc.chungkwong.mathocr.offline.extractor.*;
import cc.chungkwong.mathocr.online.*;
import cc.chungkwong.mathocr.ui.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javax.imageio.*;
/**
 * Collect statistics of the strokes
 *
 * @author Chan Chung Kwong
 */
public class StatisticsCollectors{
	/**
	 * Print statistics of some trace lists
	 *
	 * @param stream the trace lists
	 * @throws IOException
	 */
	public static void collectGroundTruth(Stream<cc.chungkwong.mathocr.common.format.Ink> stream) throws IOException{
		FrequencyTable<Integer> symbolCounter=new FrequencyTable<>();
		FrequencyTable<Integer> traceCounter=new FrequencyTable<>();
		Statistic symbols=new Statistic("Symbol");
		Statistic traces=new Statistic("Trace");
		Statistic tracesPerSymbol=new Statistic("Trace/symbol");
		Statistic points=new Statistic("Point");
		Statistic width=new Statistic("Width");
		Statistic height=new Statistic("Height");
		Statistic traceWidth=new Statistic("Trace width");
		Statistic traceHeight=new Statistic("Trace height");
		Statistic traceLength=new Statistic("Trace length");
		for(Iterator<cc.chungkwong.mathocr.common.format.Ink> iterator=stream.iterator();iterator.hasNext();){
			cc.chungkwong.mathocr.common.format.Ink ink=iterator.next();
			TraceList traceList=ink.getTraceList();
			try{
				int symbolCount=ErrorInspector.countSymbol(ErrorInspector.getMathML(ink.getFile().getCanonicalPath()));
				symbolCounter.advance(symbolCount);
				symbols.addSample(symbolCount);
			}catch(RuntimeException ex){
				Logger.getGlobal().log(Level.SEVERE,ink.getFile().getCanonicalPath());
				Logger.getGlobal().log(Level.SEVERE,"",ex);
			}
			traceCounter.advance(traceList.getTraces().size());
			traces.addSample(traceList.getTraces().size());
			ink.getAnnotions().stream().collect(Collectors.groupingBy((pair)->pair.getValue(),Collectors.counting())).forEach((k,v)->tracesPerSymbol.addSample((int)(long)v));
			traceList.getTraces().forEach((t)->{
				points.addSample(t.getPoints().size());
				traceWidth.addSample(t.getBoundBox().getWidth());
				traceHeight.addSample(t.getBoundBox().getHeight());
				traceLength.addSample((int)SpeedNormalizer.getLength(t));
			});
			BoundBox boundBox=traceList.getBoundBox();
			width.addSample(boundBox.getWidth());
			height.addSample(boundBox.getHeight());
		}
		System.out.println("Complexity:");
		System.out.println(symbolCounter);
		System.out.println("Trace:");
		System.out.println(traceCounter);
		System.out.println(symbols);
		System.out.println(traces);
		System.out.println(tracesPerSymbol);
		System.out.println(points);
		System.out.println(width);
		System.out.println(height);
		System.out.println(traceWidth);
		System.out.println(traceHeight);
		System.out.println(traceLength);
	}
	/**
	 * Print statistics of some trace lists
	 *
	 * @param stream the trace lists to be rendered and then extracted
	 * @param configuration extractor
	 * @throws IOException
	 */
	public static void collectByRender(Stream<cc.chungkwong.mathocr.common.format.Ink> stream,Extractor configuration) throws IOException{
		collectExtracted(stream.map((ink)->{
			return configuration.extract(TraceListViewer.renderImage(ink.getTraceList()),false);
		}));
	}
	/**
	 * Print statistics of extracted trace lists
	 *
	 * @param directory the images
	 * @param configuration the extractor
	 * @throws IOException
	 */
	public static void collectFromImage(File directory,Extractor configuration) throws IOException{
		collectExtracted(Files.list(directory.toPath()).filter((p)->p.toFile().getName().endsWith(".png")).map((f)->{
			try{
				return configuration.extract(ImageIO.read(f.toFile()),false);
			}catch(IOException ex){
				Logger.getLogger(StatisticsCollectors.class.getName()).log(Level.SEVERE,null,ex);
				return new TraceList();
			}
		}));
	}
	/**
	 * Print statistics of some trace lists
	 *
	 * @param stream the trace lists
	 * @throws IOException
	 */
	public static void collectExtracted(Stream<TraceList> stream) throws IOException{
		FrequencyTable<Integer> traceCounter=new FrequencyTable<>();
		Statistic traces=new Statistic("Trace");
		Statistic points=new Statistic("Point");
		Statistic width=new Statistic("Width");
		Statistic height=new Statistic("Height");
		Statistic traceWidth=new Statistic("Trace width");
		Statistic traceHeight=new Statistic("Trace height");
		Statistic length=new Statistic("Trace length");
		for(Iterator<TraceList> iterator=stream.iterator();iterator.hasNext();){
			TraceList traceList=iterator.next();
			traceCounter.advance(traceList.getTraces().size());
			traces.addSample(traceList.getTraces().size());
			traceList.getTraces().forEach((t)->{
				points.addSample(t.getPoints().size());
				traceWidth.addSample(t.getBoundBox().getWidth());
				traceHeight.addSample(t.getBoundBox().getHeight());
				length.addSample((int)SpeedNormalizer.getLength(t));
			});
			BoundBox boundBox=traceList.getBoundBox();
			width.addSample(boundBox.getWidth());
			height.addSample(boundBox.getHeight());
		}
		System.out.println("Trace:");
		System.out.println(traceCounter);
		System.out.println(traces);
		System.out.println(points);
		System.out.println(width);
		System.out.println(height);
		System.out.println(traceWidth);
		System.out.println(traceHeight);
		System.out.println(length);
	}
	public static void main(String[] args) throws IOException{
		//collectByRender(Crohme.getTestStream2016(),Extractor.DEFAULT);
		collectFromImage(new File(Crohme.DIRECTORY_2019+"/Task1_and_Task2/Task2_offlineRec/MainTask_formula/valid/data_png_TEST2016_INKML_GT"),Extractor.DEFAULT);
//		collectGroundTruth(Crohme.getTrainStream2019());
//		collectGroundTruth(Crohme.getTestStream2019());
		collectGroundTruth(Crohme.getTestStream2016());
//		collectGroundTruth(Crohme.getValidationStream2016());
	}
	private static class Statistic{
		private final String name;
		private int sum=0;
		private int min=Integer.MAX_VALUE;
		private int max=Integer.MIN_VALUE;
		private int count=0;
		public Statistic(String name){
			this.name=name;
		}
		public void addSample(int i){
			sum+=i;
			++count;
			if(i<min){
				min=i;
			}
			if(i>max){
				max=i;
			}
		}
		@Override
		public String toString(){
			return name+"\t"+min+"\t"+max+"\t"+sum*1.0/count+"\t"+count;
		}
	}
}
