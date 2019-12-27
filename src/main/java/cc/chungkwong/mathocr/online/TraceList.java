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
import cc.chungkwong.mathocr.common.*;
import java.util.*;
import java.util.stream.*;
/**
 * Sequence of strokes
 *
 * @author Chan Chung Kwong
 */
public class TraceList{
	private final List<Trace> traces;
	/**
	 * Create a empty sequence of strokes
	 */
	public TraceList(){
		this.traces=new ArrayList<>();
	}
	/**
	 * Create a sequence of strokes
	 *
	 * @param traces underlying list
	 */
	public TraceList(List<Trace> traces){
		this.traces=traces;
	}
	/**
	 *
	 * @return list of strokes
	 */
	public List<Trace> getTraces(){
		return traces;
	}
	/**
	 *
	 * @return bounding box of the strokes
	 */
	public BoundBox getBoundBox(){
		return BoundBox.union(traces.stream().map(Trace::getBoundBox).iterator());
	}
	/**
	 * Rescale the coordinates to fit in a given rectangle
	 *
	 * @param toBox the rectangle
	 * @return scaled trace list
	 */
	public TraceList rescale(BoundBox toBox){
		BoundBox fromBox=getBoundBox();
		TraceList rescaled=new TraceList(new ArrayList<>(getTraces().size()));
		int scaleTo, scaleFrom, dx, dy;
		if(fromBox.getWidth()*toBox.getHeight()>=toBox.getWidth()*fromBox.getHeight()){
			scaleFrom=fromBox.getWidth();
			scaleTo=toBox.getWidth();
			dx=toBox.getLeft();
			dy=toBox.getTop()+toBox.getHeight()/2-fromBox.getHeight()*scaleTo/(2*scaleFrom);
		}else{
			scaleFrom=fromBox.getHeight();
			scaleTo=toBox.getHeight();
			dx=toBox.getLeft()+toBox.getWidth()/2-fromBox.getWidth()*scaleTo/(2*scaleFrom);
			dy=toBox.getTop();
		}
		for(Trace trace:getTraces()){
			rescaled.getTraces().add(new Trace(trace.getPoints().stream().
					map((point)->new TracePoint(
					(point.getX()-fromBox.getLeft())*scaleTo/scaleFrom+dx,
					(point.getY()-fromBox.getTop())*scaleTo/scaleFrom+dy)).
					collect(Collectors.toList())));
		}
		return rescaled;
	}
	/**
	 * Translate the traces
	 *
	 * @param dx offset
	 * @param dy offset
	 * @return transformed trace list
	 */
	public TraceList translate(int dx,int dy){
		TraceList rescaled=new TraceList(new ArrayList<>(getTraces().size()));
		for(Trace trace:getTraces()){
			rescaled.getTraces().add(new Trace(trace.getPoints().stream().
					map((point)->new TracePoint(point.getX()+dx,point.getY()+dy)).
					collect(Collectors.toList())));
		}
		return rescaled;
	}
	/**
	 *
	 * @param trace0 a trace
	 * @param trace1 another trace
	 * @return the Hausdorff distance between the two traces
	 */
	public static int getDistance(Trace trace0,Trace trace1){
		return Math.max(getSideDistance(trace0,trace1),getSideDistance(trace1,trace0));
	}
	/**
	 *
	 * @param trace0
	 * @param trace1
	 * @return the minimum radius r such that trace0 is a subset of the
	 * r-neighborhood of trace1
	 */
	public static int getSideDistance(Trace trace0,Trace trace1){
		int distance=0;
		for(TracePoint point0:trace0.getPoints()){
			int d=Integer.MAX_VALUE;
			for(TracePoint point1:trace1.getPoints()){
				int tmp=TracePoint.getDistanceSquare(point0,point1);
				if(tmp<d){
					d=tmp;
				}
			}
			Iterator<TracePoint> iterator=trace1.getPoints().iterator();
			TracePoint last=iterator.next();
			while(iterator.hasNext()){
				TracePoint next=iterator.next();
				int dy0=next.getY()-last.getY();
				int dx0=next.getX()-last.getX();
				if(dx0==0&&dy0==0){
					continue;
				}
				int dy=point0.getY()-last.getY();
				int dx=point0.getX()-last.getX();
				int dd=dx0*dx0+dy0*dy0;
				int t=(dx*dx0+dy*dy0)*10000/dd;
				if(t>0&&t<10000){
					int s=(dx*dy0-dy*dx0);
					s*=s;
					s/=dd;
					if(s<d){
						d=s;
					}
				}
				last=next;
			}
			if(d>distance){
				distance=d;
			}
		}
		return distance;
	}
	@Override
	public String toString(){
		return traces.toString();
	}
}
