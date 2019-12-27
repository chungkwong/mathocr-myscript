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
import cc.chungkwong.mathocr.offline.extractor.tracer.*;
import cc.chungkwong.mathocr.online.*;
import java.util.*;
import java.util.stream.*;
/**
 * Tests for stroke direction detector
 *
 * @author Chan Chung Kwong
 */
public class StrokeDirectionTests{
	/**
	 * Start tests
	 */
	public void test(){
		System.out.println("Test set 2016:");
		test(Crohme.getTestStream2016());
		System.out.println("Validation set 2016:");
		test(Crohme.getValidationStream2016());
	}
	/**
	 * Start test
	 *
	 * @param input ground truth
	 */
	public void test(Stream<Ink> input){
		long[] count=new long[6];
		long[] dd={0,0};
		input.flatMap((ink)->ink.getTraceList().getTraces().stream()).forEach((trace)->{
			++count[0];
			if(checkCoordinateSum(trace)){
				++count[1];
			}
			if(checkCoordinateSumSq(trace)){
				++count[2];
			}
			if(checkCoordinateX(trace)){
				++count[3];
			}
			if(checkCoordinateY(trace)){
				++count[4];
			}
			if(checkAngle(trace)){
				++count[5];
			}
			dd[0]+=trace.getEnd().getY()-trace.getStart().getY();
			dd[1]+=trace.getEnd().getX()-trace.getStart().getX();
		});
		String[] names={"2x+3y","x^2+y^2","x","y","angle"};
		for(int i=1;i<count.length;i++){
			System.out.println(names[i-1]+":"+count[i]+"/"+count[0]+"="+count[i]*1.0/count[0]);
		}
		System.out.println(dd[0]+";"+dd[1]);
	}
	private boolean checkCoordinateSum(Trace trace){
		int x1=trace.getStart().getX();
		int y1=trace.getStart().getY();
		int x2=trace.getEnd().getX();
		int y2=trace.getEnd().getY();
		return 2*x1+3*y1<=2*x2+3*y2;
	}
	private boolean checkCoordinateSumSq(Trace trace){
		int x1=trace.getStart().getX();
		int y1=trace.getStart().getY();
		int x2=trace.getEnd().getX();
		int y2=trace.getEnd().getY();
		return x1*x1+y1*y1<=x2*x2+y2*y2;
	}
	private boolean checkCoordinateX(Trace trace){
		int x1=trace.getStart().getX();
		int x2=trace.getEnd().getX();
		return x1<=x2;
	}
	private boolean checkCoordinateY(Trace trace){
		int y1=trace.getStart().getY();
		int y2=trace.getEnd().getY();
		return y1<=y2;
	}
	private static final double P[]={
		0.40967453742522886,
		0.08002675753787052,
		0.20598641372692375,
		0.08741192680513464,
		0.052301943554836604,
		0.022941116864798133,
		0.07203845366622273,
		0.06961885041898477};
	private boolean checkAngle(Trace trace){
		Segment substroke=new Segment(trace,1);
		int angleBegin=toDirection(substroke.getAngleBegin());
		int angleEnd=toDirection(substroke.getAngleEnd());
		double prop1;
		if(angleBegin!=8){
			double p=P[angleBegin];
			double q=P[(angleBegin+4)%8];
			prop1=p/(p+q);
		}else{
			prop1=0.5;
		}
		double prop2;
		if(angleEnd!=8){
			double p=P[angleEnd];
			double q=P[(angleEnd+4)%8];
			prop2=p/(p+q);
		}else{
			prop2=0.5;
		}
		return Math.sqrt(prop1*prop2)>0.5;
	}
	private static void printAngleHistogram(){
		long[] directionCount=new long[9];
		long[] count={0,0};
		Crohme.getFullStream().flatMap((ink)->ink.getTraceList().getTraces().stream()).forEach((trace)->{
			//for(int i=1;i<trace.getPoints().size();i++){
			//	++directionCount[getDirection(trace.getPoints().get(i-1),trace.getPoints().get(i))];
			//}
			++directionCount[toDirection(new Segment(trace,1).getAngleBegin())];
			if(trace.getPoints().size()>1){
				++count[1];
				int x1=trace.getStart().getX();
				int y1=trace.getStart().getY();
				int x2=trace.getEnd().getX();
				int y2=trace.getEnd().getY();
				if(x2>x1){
					++count[0];
				}
			}
		});
		double sum=Arrays.stream(directionCount).sum();
		for(int i=0;i<8;i++){
			System.out.println(directionCount[i]/sum);
		}
		System.out.println(count[0]+"/"+count[1]+"="+(count[0]*1.0/count[1]));
	}
	private static int toDirection(TracePoint p,TracePoint q){
		return toDirection(Math.atan2(q.getY()-p.getY(),q.getX()-p.getX()));
	}
	private static int toDirection(double angle){
		angle*=4/Math.PI;
		angle+=8;
		return Double.isNaN(angle)?8:(int)(angle+0.5)%8;
	}
	public static void main(String[] args){
		new StrokeDirectionTests().test();
	}
}
