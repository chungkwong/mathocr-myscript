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
package cc.chungkwong.mathocr.offline.extractor.tracer;
import cc.chungkwong.mathocr.online.*;
import java.util.*;
/**
 * Segment
 *
 * @author Chan Chung Kwong
 */
public class Segment extends Component{
	private double angleBegin, angleEnd;
	/**
	 * Create a empty segment
	 */
	public Segment(){
		super(new Trace(new LinkedList<>()));
	}
	/**
	 * Create a segment
	 *
	 * @param trace points
	 * @param thick thickness
	 */
	public Segment(Trace trace,int thick){
		this(trace,thick,estimateStartAngle(trace,thick),estimateEndAngle(trace,thick));
	}
	/**
	 * Create a segment
	 *
	 * @param trace points
	 * @param thick thickness
	 * @param angleBegin start angle
	 * @param angleEnd end angle
	 */
	public Segment(Trace trace,int thick,double angleBegin,double angleEnd){
		super(trace);
		setThick(thick);
		this.angleBegin=angleBegin;
		this.angleEnd=angleEnd;
	}
	/**
	 *
	 * @return start angle
	 */
	public double getAngleBegin(){
		return angleBegin;
	}
	/**
	 *
	 * @return end angle
	 */
	public double getAngleEnd(){
		return angleEnd;
	}
	/**
	 * Set start angle
	 *
	 * @param angleBegin
	 */
	public void setAngleBegin(double angleBegin){
		this.angleBegin=angleBegin;
	}
	/**
	 * Set end angle
	 *
	 * @param angleEnd
	 */
	public void setAngleEnd(double angleEnd){
		this.angleEnd=angleEnd;
	}
	void updateAngles(){
		this.angleBegin=estimateStartAngle(getTrace(),getThick());
		this.angleEnd=estimateEndAngle(getTrace(),getThick());
	}
//	private static final int RADIUS=5;
	static double estimateStartAngle(Trace trace,double thick){
//		int mid=trace.getPoints().size();
//		while(mid>=3&&calculateAngle(trace.getPoints().get(0),trace.getPoints().get(mid/2),trace.getPoints().get(mid-1))<TURN_ANGLE_THREHOLD){
//			mid/=2;
//		}
//		return calculateAngle(trace.getPoints().get(0),trace.getPoints().get(mid-1));
//		if(trace.getPoints().size()<9){
//			return calculateAngle(trace.getStart(),trace.getEnd());
//		}else{
//			TracePoint start=trace.getStart();
//			TracePoint mid=trace.getPoints().get(4);
//			TracePoint end=trace.getPoints().get(8);
//			return Math.atan2((3*start.getY()+end.getY()-4*mid.getY())*0.5,(3*start.getX()+end.getX()-4*mid.getX())*0.5)+Math.PI;
//		}
		int r=(int)(Math.sqrt(thick)*3);
		TracePoint start=trace.getStart();
		TracePoint end=trace.getPoints().size()<=r?trace.getEnd():trace.getPoints().get(r);
		return calculateAngle(start,end);
	}
//	private static final double TURN_ANGLE_THREHOLD=Math.PI*5/6;
	static double estimateEndAngle(Trace trace,double thick){
//		int len=trace.getPoints().size();
//		int mid=trace.getPoints().size();
//		while(mid>=3&&calculateAngle(trace.getPoints().get(len-mid),trace.getPoints().get(len-1-mid/2),trace.getPoints().get(len-1))<TURN_ANGLE_THREHOLD){
//			mid/=2;
//		}
//		return calculateAngle(trace.getPoints().get(len-mid),trace.getPoints().get(len-1));
		int r=(int)(Math.sqrt(thick)*3);
		TracePoint start=trace.getPoints().size()<=r?trace.getStart():trace.getPoints().get(trace.getPoints().size()-r);
		TracePoint end=trace.getEnd();
		return calculateAngle(start,end);
	}
//	private static double calculateAngle(TracePoint p,TracePoint q,TracePoint r){
//		double a=Math.hypot(r.getX()-p.getX(),r.getY()-p.getY());
//		double b=Math.hypot(q.getX()-p.getX(),q.getY()-p.getY());
//		double c=Math.hypot(r.getX()-q.getX(),r.getY()-q.getY());
//		return Math.acos((b*b+c*c-a*a)/(2*b*c));
//	}
	private static double calculateAngle(TracePoint p,TracePoint q){
		int dx=q.getX()-p.getX();
		int dy=q.getY()-p.getY();
		return Math.atan2(dy,dx);
	}
}
