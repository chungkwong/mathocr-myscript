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
import cc.chungkwong.mathocr.online.*;
import java.util.*;
import java.util.stream.*;
/**
 * Minimize intra stroke distance
 *
 * @author Chan Chung Kwong
 */
public class DistanceOrderer implements Orderer{
	@Override
	public TraceList order(TraceList traceList){
		List<Trace> traces=traceList.getTraces();
		int n=traces.size();
		if(n>9){
			return new GreedyOrderer().order(traceList);
		}
		double[][] distance=new double[n+1][n];
		int j=0;
		for(Trace second:traces){
			TracePoint start=second.getStart();
			int i=0;
			for(Trace first:traces){
				TracePoint end=first.getEnd();
				distance[i][j]=Math.hypot(start.getX()-end.getX(),start.getY()-end.getY());
				++i;
			}
			distance[i][j]=Math.hypot(start.getX(),start.getY());
			++j;
		}
		LinkedList<PrimitiveIterator.OfInt> stack=new LinkedList<>();
		LinkedList<Integer> tmp=new LinkedList<>();
		ArrayList<Integer> result=new ArrayList<>(n+1);
		LinkedList<Double> dist=new LinkedList<>();
		double d=Double.MAX_VALUE;
		tmp.push(n);
		dist.push(0.0);
		stack.push(IntStream.range(0,n).iterator());
		while(!stack.isEmpty()){
			if(stack.peek().hasNext()){
				//System.out.println(tmp);
				int next=stack.peek().nextInt();
				if(tmp.size()==n+1){
					if(dist.peek()<d){
						d=dist.peek();
						result.clear();
						result.addAll(tmp);
					}
				}else if(!tmp.contains(next)){
					dist.push(dist.peek()+distance[tmp.peek()][next]);
					tmp.push(next);
					stack.push(IntStream.range(0,n).iterator());
				}
			}else{
				stack.pop();
				tmp.pop();
				dist.pop();
			}
		}
		Collections.reverse(result);
		return new TraceList(result.stream().skip(1).map((i)->traces.get(i)).collect(Collectors.toList()));
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
		return ResourceBundle.getBundle("cc.chungkwong.mathocr.message").getString("DISTANCE");
	}
}
