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
 * From a stroke to the nearest stroke
 *
 * @author Chan Chung Kwong
 */
public class GreedyOrderer implements Orderer{
	@Override
	public TraceList order(TraceList traceList){
		Set<Trace> traces=new HashSet<>(traceList.getTraces());
		List<Trace> result=new ArrayList<>();
		TracePoint[] last=new TracePoint[]{new TracePoint(0,0)};
		while(!traces.isEmpty()){
			Trace trace=traces.stream().min(Comparator.comparing((t)->TracePoint.getDistanceSquare(last[0],t.getStart()))).get();
			traces.remove(trace);
			result.add(trace);
			last[0]=trace.getEnd();
		}
		return new TraceList(result);
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
		return ResourceBundle.getBundle("cc.chungkwong.mathocr.message").getString("GREEDY");
	}
}
