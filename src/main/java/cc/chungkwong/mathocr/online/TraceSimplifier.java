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
 * Stroke simplifier
 *
 * @author Chan Chung Kwong
 */
public class TraceSimplifier{
	/**
	 * Simplify strokes by only keeping one point in every step points
	 *
	 * @param traceList to be simplified
	 * @param step
	 * @return simplified
	 */
	public static TraceList simplify(TraceList traceList,int step){
		TraceList simplified=new TraceList(new ArrayList<>(traceList.getTraces().size()));
		for(Trace trace:traceList.getTraces()){
			simplified.getTraces().add(simplify(trace,step));
		}
		return simplified;
	}
	private static Trace simplify(Trace trace,int step){
		Trace simplified=new Trace(new ArrayList<>(trace.getPoints().size()/step+1));
		List<TracePoint> reducedPoints=simplified.getPoints();
		int i=0;
		for(TracePoint next:trace.getPoints()){
			if((i++)%step==0){
				reducedPoints.add(next);
			}
		}
		if((i-1)%3!=0){
			reducedPoints.add(trace.getPoints().get(i-1));
		}
		return simplified;
	}
}
