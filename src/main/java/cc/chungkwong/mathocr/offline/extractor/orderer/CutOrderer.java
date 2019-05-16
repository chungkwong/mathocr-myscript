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
package cc.chungkwong.mathocr.offline.extractor.orderer;
import cc.chungkwong.mathocr.online.Trace;
import cc.chungkwong.mathocr.online.TraceList;
import cc.chungkwong.mathocr.online.TracePoint;
import java.util.*;
/**
 * Recursive projection
 *
 * @author Chan Chung Kwong
 */
public class CutOrderer implements Orderer{
	private final Orderer based=new TopologicalOrderer();
	@Override
	public TraceList order(TraceList traceList){
		flip(traceList.getTraces());
		ArrayList<Trace> traces=new ArrayList<>(traceList.getTraces());
		hSort(traces);
		return new TraceList(traces);
	}
	private void hSort(List<Trace> traces){
		Collections.sort(traces,Comparator.comparingInt((p)->p.getBoundBox().getLeft()));
		for(int i=0;i<traces.size();i++){
			int lastx=traces.get(i).getBoundBox().getRight();
			int j=i+1;
			while(j<traces.size()&&traces.get(j).getBoundBox().getLeft()<=lastx){
				lastx=Math.max(lastx,traces.get(j).getBoundBox().getRight());
				++j;
			}
			if(j>i+1){
				vSort(traces.subList(i,j));
			}
			i=j-1;
		}
	}
	private void vSort(List<Trace> traces){
		Collections.sort(traces,Comparator.comparingInt((p)->p.getBoundBox().getTop()));
		for(int i=0;i<traces.size();i++){
			int lasty=traces.get(i).getBoundBox().getBottom();
			int j=i+1;
			while(j<traces.size()&&traces.get(j).getBoundBox().getTop()<=lasty){
				lasty=Math.max(lasty,traces.get(j).getBoundBox().getBottom());
				++j;
			}
			if(j>i+1){
				if(i!=0||j!=traces.size()){
					hSort(traces.subList(i,j));
				}else{
					List<Trace> order=based.order(new TraceList(traces.subList(i,j))).getTraces();
					for(int k=0;k<order.size();k++){
						traces.set(i+k,order.get(k));
					}
				}
			}
			i=j-1;
		}
	}
	private void flip(List<Trace> traces){
		for(Trace trace:traces){
			List<TracePoint> points=trace.getPoints();
			if(points.isEmpty()){
				continue;
			}
			TracePoint first=points.get(0);
			TracePoint last=points.get(points.size()-1);
			if(2*last.getX()+3*last.getY()<2*first.getX()+3*first.getY()){
				Collections.reverse(points);
			}
		}
	}
	@Override
	public boolean equals(Object obj){
		return obj!=null&&obj.getClass()==getClass();
	}
	@Override
	public int hashCode(){
		return getClass().hashCode();
	}
	@Override
	public String toString(){
		return ResourceBundle.getBundle("cc.chungkwong.mathocr.message").getString("XY_CUT");
	}
}
