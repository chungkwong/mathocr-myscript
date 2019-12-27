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
package cc.chungkwong.mathocr.online;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class SpeedNormalizer{
	public static Trace normalize(Trace trace,int samples){
		return normalize(trace,samples,getLength(trace));
	}
	public static Trace normalize(Trace trace,int samples,double length){
		ArrayList<TracePoint> normalized=new ArrayList<>(samples);
		double pass=0;
		Iterator<TracePoint> iterator=trace.getPoints().iterator();
		TracePoint last=iterator.next();
		TracePoint next=iterator.hasNext()?iterator.next():last;
		double d=Math.hypot(next.getX()-last.getX(),next.getY()-last.getY());
		--samples;
		for(int i=0;i<=samples;i++){
			while((pass+d)*samples<=length*i&&iterator.hasNext()){
				last=next;
				next=iterator.next();
				pass+=d;
				d=Math.hypot(next.getX()-last.getX(),next.getY()-last.getY());
			}
			double b=d>0?(length*i/samples-pass)/d:1.0;
			double a=1-b;
			int x=(int)(a*last.getX()+b*next.getX()+0.5);
			int y=(int)(a*last.getY()+b*next.getY()+0.5);
			normalized.add(new TracePoint(x,y));
		}
		return new Trace(normalized);
	}
	public static void normalize(Trace trace,int samples,double[] x,double[] y){
		normalize(trace,samples,getLength(trace),x,y);
	}
	public static void normalize(Trace trace,int samples,double length,double[] x,double[] y){
		double pass=0;
		Iterator<TracePoint> iterator=trace.getPoints().iterator();
		TracePoint last=iterator.next();
		TracePoint next=iterator.hasNext()?iterator.next():last;
		double d=Math.hypot(next.getX()-last.getX(),next.getY()-last.getY());
		--samples;
		for(int i=0;i<=samples;i++){
			while((pass+d)*samples<=length*i&&iterator.hasNext()){
				last=next;
				next=iterator.next();
				pass+=d;
				d=Math.hypot(next.getX()-last.getX(),next.getY()-last.getY());
			}
			double b=d>0?(length*i/samples-pass)/d:1.0;
			double a=1-b;
			x[i]=a*last.getX()+b*next.getX()+0.5;
			y[i]=a*last.getY()+b*next.getY()+0.5;
		}
	}
	public static double getLength(Trace trace){
		double length=0;
		Iterator<TracePoint> iterator=trace.getPoints().iterator();
		TracePoint last=iterator.next();
		while(iterator.hasNext()){
			TracePoint next=iterator.next();
			length+=Math.hypot(next.getX()-last.getX(),next.getY()-last.getY());
			last=next;
		}
		return length;
	}
}
