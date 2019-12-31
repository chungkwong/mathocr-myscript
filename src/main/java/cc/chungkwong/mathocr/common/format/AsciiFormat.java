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
package cc.chungkwong.mathocr.common.format;
import cc.chungkwong.mathocr.online.*;
import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.stream.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class AsciiFormat implements TraceListFormat{
	private final double distThreshold;
	private final double cosThreshold;
	public AsciiFormat(){
		this(-1);
	}
	public AsciiFormat(double distThreholdPre){
		this(distThreholdPre,DEFAULT_COS_THRESHOLD_PRE);
	}
	public AsciiFormat(double distThresholdPre,double cosThresholdPre){
		this.distThreshold=distThresholdPre>=0?distThresholdPre*distThresholdPre:-1;
		this.cosThreshold=cosThresholdPre*cosThresholdPre;
	}
	@Override
	public String getSuffix(){
		return "ascii";
	}
	@Override
	public TraceList read(File file) throws IOException{
		TraceList traceList=new TraceList();
		Trace trace=new Trace();
		for(Iterator<String> iterator=Files.lines(file.toPath()).iterator();iterator.hasNext();){
			String[] line=iterator.next().split(" ");
			trace.getPoints().add(new TracePoint(
					(int)(Double.parseDouble(line[0])*REFERENCE_SCALE+0.5),
					(int)(Double.parseDouble(line[1])*REFERENCE_SCALE+0.5)));
			if((int)(Double.parseDouble(line[line.length-1]))==1){
				traceList.getTraces().add(trace);
				trace=new Trace();
			}
		}
		return traceList;
	}
	public static final int REFERENCE_SCALE=150;
	@Override
	public void write(TraceList traceList,File file) throws IOException{
		double[][] features=getFeature(simplify(traceList));
		if(file!=null){
			Files.write(file.toPath(),Arrays.stream(features).map((line)->Arrays.stream(line).mapToObj((n)->DECIMAL_FORMAT.format(n)).collect(Collectors.joining(" "))).collect(Collectors.toList()));
		}
	}
	public void write(TraceList traceList,Writer out) throws IOException{
		double[][] features=getFeature(simplify(traceList));
		if(out!=null){
			out.write(Arrays.stream(features).map((line)->Arrays.stream(line).mapToObj((n)->DECIMAL_FORMAT.format(n)).collect(Collectors.joining(" "))).collect(Collectors.joining("\n")));
		}
	}
	private TraceList simplify(TraceList traceList){
		double distThreshold;
		if(this.distThreshold>=0){
			distThreshold=this.distThreshold;
		}else{
			double distThreholdsPre=0.08*traceList.getTraces().stream().
					map(Trace::getBoundBox).mapToInt((b)->Math.max(b.getWidth(),b.getHeight())).
					sorted().skip(traceList.getTraces().size()*3/4).findFirst().orElse(300);
			distThreshold=distThreholdsPre*distThreholdsPre;
		}
		TraceList simplified=new TraceList(new ArrayList<>(traceList.getTraces().size()));
		for(Trace trace:traceList.getTraces()){
			Trace simplifiedTrace=new Trace(new ArrayList<>(trace.getPoints().size()));
			Iterator<TracePoint> iterator=trace.getPoints().iterator();
			if(iterator.hasNext()){
				TracePoint lastlast=iterator.next();
				simplifiedTrace.getPoints().add(lastlast);
				if(iterator.hasNext()){
					TracePoint last=iterator.next();
					while(iterator.hasNext()){
						TracePoint next=iterator.next();
						double dxForward=next.getX()-last.getX();
						double dxBackward=last.getX()-lastlast.getX();
						double dyForward=next.getY()-last.getY();
						double dyBackward=last.getY()-lastlast.getY();
						double innerProduct=dxForward*dxBackward+dyForward*dyBackward;
						if(innerProduct<=0
								||innerProduct*innerProduct<cosThreshold
								*(dxForward*dxForward+dyForward*dyForward)
								*(dxBackward*dxBackward+dyBackward*dyBackward)){
							if(dxBackward*dxBackward+dyBackward*dyBackward>distThreshold){
								simplifiedTrace.getPoints().add(last);
								lastlast=last;
							}
						}
						last=next;
					}
					simplifiedTrace.getPoints().add(last);
				}
				simplified.getTraces().add(simplifiedTrace);
			}
		}
//		System.out.println(distThreshold+"\t"+simplified.getTraces().stream().mapToInt((t)->t.getPoints().size()).average().getAsDouble());
		return simplified;
	}
	private static final double DEFAULT_COS_THRESHOLD_PRE=0.99;
	private static final double DEFAULT_DIST_THRESHOLD_PRE=10;
	private double[][] getFeature(TraceList traceList) throws IOException{
		double xSum=0, ySum=0, xxSum=0, xxCSum=0, length=0;
		int count=0;
		for(Trace trace:traceList.getTraces()){
			Iterator<TracePoint> iterator=trace.getPoints().iterator();
			if(iterator.hasNext()){
				TracePoint last=iterator.next();
				while(iterator.hasNext()){
					TracePoint curr=iterator.next();
					double len=Math.hypot(curr.getX()-last.getX(),curr.getY()-last.getY());
					xSum+=(last.getX()+curr.getX())*len;
					ySum+=(last.getY()+curr.getY())*len;
					xxSum+=(last.getX()*last.getX()+curr.getX()*curr.getX())*len;
					xxCSum+=(last.getX()*curr.getX())*len;
					length+=len;
					++count;
					last=curr;
				}
			}
		}
		double centerX=xSum*0.5/length;
		double centerY=ySum*0.5/length;
		double var=Math.sqrt(((xxSum+xxCSum)/3+centerX*centerX*length-centerX*xSum)/length);
		double[][] features=new double[count+traceList.getTraces().size()][8];
		int index=0;
		for(Trace trace:traceList.getTraces()){
			Iterator<TracePoint> iterator=trace.getPoints().iterator();
			while(iterator.hasNext()){
				TracePoint curr=iterator.next();
				features[index][0]=(curr.getX()-centerX)/var;
				features[index][1]=(curr.getY()-centerY)/var;
				if(iterator.hasNext()){
					features[index][6]=1.0;
				}else{
					features[index][7]=1.0;
				}
				++index;
			}
		}
		for(int i=0, imax=index-1;i<imax;i++){
			features[i][2]=features[i+1][0]-features[i][0];
			features[i][3]=features[i+1][1]-features[i][1];
		}
		for(int i=0, imax=index-2;i<imax;i++){
			features[i][4]=features[i+2][0]-features[i][0];
			features[i][5]=features[i+2][1]-features[i][1];
		}
		return features;
	}
	private static final DecimalFormat DECIMAL_FORMAT=new DecimalFormat("0.000000");
}
