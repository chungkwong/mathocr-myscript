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
import cc.chungkwong.mathocr.common.*;
import cc.chungkwong.mathocr.online.*;
import java.util.*;
/**
 * Topological sort
 *
 * @author Chan Chung Kwong
 */
public class TopologicalOrderer implements Orderer{
	@Override
	public TraceList order(TraceList traceList){
		HashMap<Trace,Integer> incoming=new HashMap<>();
		HashMap<Trace,HashSet<Trace>> outgoing=new HashMap<>();
		HashSet<Trace> beginning=new HashSet<>();
		for(Trace trace:traceList.getTraces()){
			outgoing.put(trace,new HashSet<>());
		}
		BoundBox fullBox=traceList.getBoundBox();
		int[] buf=new int[Math.max(fullBox.getBottom(),fullBox.getRight())+1];
		for(Trace trace1:traceList.getTraces()){
			incoming.put(trace1,0);
			for(Trace trace0:traceList.getTraces()){
				if(trace0==trace1){
					continue;
				}
				if(isPrecede(trace0,trace1,buf)){
					incoming.put(trace1,incoming.get(trace1)+1);
					outgoing.get(trace0).add(trace1);
				}
			}
			if(incoming.get(trace1)==0){
				beginning.add(trace1);
				incoming.remove(trace1);
			}
		}
		List<Trace> result=new ArrayList<>(traceList.getTraces().size());
		//int lastx=0, lasty=0;
		while(result.size()<traceList.getTraces().size()){
			Trace trace;
			if(!beginning.isEmpty()){
				Iterator<Trace> iterator=beginning.iterator();
				trace=iterator.next();
				while(iterator.hasNext()){
					Trace next=iterator.next();
					if(isPrecedeLoose(next,trace,buf)){
						trace=next;
					}
				}
				beginning.remove(trace);
			}else{
				Iterator<Map.Entry<Trace,Integer>> iterator=incoming.entrySet().iterator();
				Map.Entry<Trace,Integer> entry=iterator.next();
				trace=entry.getKey();
				int in=entry.getValue();
//				int dist=Integer.MAX_VALUE;
				while(iterator.hasNext()){
					entry=iterator.next();
					//int x=entry.getPoints().get(0).getX();
					//int y=entry.getPoints().get(0).getY();
					//int d=(x-lastx)*(x-lastx)+(y-lasty)*(y-lasty);
					if(entry.getValue()<in||(entry.getValue()==in&&isPrecedeConflict(entry.getKey(),trace,buf))){
						//if(entry.getBoundBox().getLeft()<trace.getBoundBox().getLeft()){
						trace=entry.getKey();
						in=entry.getValue();
						//dist=d;
					}
				}
				incoming.remove(trace);
			}
			result.add(trace);
			/*if(!trace.getPoints().isEmpty()){
				TracePoint last=trace.getPoints().get(trace.getPoints().size()-1);
				lastx=last.getX();
				lasty=last.getY();
			}*/
			for(Trace next:outgoing.get(trace)){
				if(incoming.containsKey(next)&&incoming.put(next,incoming.get(next)-1)==1){
					beginning.add(next);
					incoming.remove(next);
				}
			}
		}
		return new TraceList(result);
	}
	private static boolean isPrecede(Trace trace0,Trace trace1,int[] buf){
		BoundBox box0=trace0.getBoundBox();
		BoundBox box1=trace1.getBoundBox();
		boolean overlapX=box0.getLeft()<=box1.getRight()&&box1.getLeft()<=box0.getRight();
		boolean overlapY=box0.getTop()<=box1.getBottom()&&box1.getTop()<=box0.getBottom();
		if(overlapY){
			if(overlapX){
				if(isLefter(trace0,trace1,buf)){
					if(isOver(trace1,trace0,buf)){
						int intersectY=Math.min(box0.getBottom(),box1.getBottom())-Math.max(box0.getTop(),box1.getTop())+1;
						int intersectX=Math.min(box0.getRight(),box1.getRight())-Math.max(box0.getLeft(),box1.getLeft())+1;
						return intersectY>=intersectX;
					}else{
						return true;
					}
				}else if(isLefter(trace1,trace0,buf)){
					return false;
				}else if(isOver(trace0,trace1,buf)){
					return true;
				}/*else if(isOver(trace1,trace0,buf)){
					return false;
				}*/else{
					return false;
				}
			}else{
				return box0.getLeft()<box1.getLeft();
			}
		}else if(overlapX){
			return box0.getTop()<box1.getTop()
					&&Math.min(box0.getRight(),box1.getRight())-Math.max(box0.getLeft(),box1.getLeft())+1>=Math.min(box0.getWidth(),box1.getWidth())/4;
		}else{
			return false;
		}
	}
	private static boolean isLefter(Trace trace0,Trace trace1,int[] buf){
		int top0=trace0.getBoundBox().getTop();
		int bottom0=trace0.getBoundBox().getBottom();
		int top1=trace1.getBoundBox().getTop();
		int bottom1=trace1.getBoundBox().getBottom();
		int top=Math.max(top0,top1);
		int bottom=Math.min(bottom0,bottom1);
		Arrays.fill(buf,top0,bottom0+1,Integer.MIN_VALUE);
		for(TracePoint point:trace0.getPoints()){
			int x=point.getX();
			int y=point.getY();
			if(x>buf[y]){
				buf[y]=x;
			}
		}
		interpolate(top0,bottom0,buf,Integer.MIN_VALUE);
		boolean nonempty=false;
		for(TracePoint point:trace1.getPoints()){
			int x=point.getX();
			int y=point.getY();
			if(y>=top&&y<=bottom){
				if(x<=buf[y]){
					return false;
				}else{
					nonempty=true;
				}
			}
		}
		return nonempty;
	}
	private static boolean isOver(Trace trace0,Trace trace1,int[] buf){
		int left0=trace0.getBoundBox().getLeft();
		int right0=trace0.getBoundBox().getRight();
		int left1=trace1.getBoundBox().getLeft();
		int right1=trace1.getBoundBox().getRight();
		int left=Math.max(left0,left1);
		int right=Math.min(right0,right1);
		Arrays.fill(buf,left0,right0+1,Integer.MIN_VALUE);
		for(TracePoint point:trace0.getPoints()){
			int x=point.getX();
			int y=point.getY();
			if(y>buf[x]){
				buf[x]=y;
			}
		}
		interpolate(left0,right0,buf,Integer.MIN_VALUE);
		boolean nonempty=false;
		for(TracePoint point:trace1.getPoints()){
			int x=point.getX();
			int y=point.getY();
			if(x>=left&&x<=right){
				if(y<=buf[x]){
					return false;
				}else{
					nonempty=true;
				}
			}
		}
		return nonempty;
	}
	private static boolean isPrecedeLoose(Trace trace0,Trace trace1,int[] buf){
		BoundBox box0=trace0.getBoundBox();
		BoundBox box1=trace1.getBoundBox();
//		return trace0.getBoundBox().getLeft()+trace0.getBoundBox().getTop()
//				<trace1.getBoundBox().getLeft()+trace1.getBoundBox().getTop();
		boolean overlapX=box0.getLeft()<=box1.getRight()&&box1.getLeft()<=box0.getRight();
		boolean overlapY=box0.getTop()<=box1.getBottom()&&box1.getTop()<=box0.getBottom();
		if(overlapY){
			if(overlapX){
				if(fixVBar(trace0,trace1,buf)){
					return true;
				}else if(fixHBar(trace0,trace1,buf)){
					return !fixVBar(trace1,trace0,buf);
				}else{
					return !fixVBar(trace1,trace0,buf)&&!fixHBar(trace1,trace0,buf)
							&&box0.getLeft()+box0.getTop()
							<box1.getLeft()+box1.getTop();
				}
			}else{
				return box0.getLeft()<box1.getLeft();
			}
		}else if(overlapX){
			if(Math.min(box0.getRight(),box1.getRight())-Math.max(box0.getLeft(),box1.getLeft())+1>=Math.min(box0.getWidth(),box1.getWidth())/4){
				return box0.getTop()<box1.getTop();
			}else{
				return box0.getLeft()<box1.getLeft();
			}
		}else{
			return box0.getLeft()<box1.getLeft();
		}
	}
	private static boolean isPrecedeConflict(Trace trace0,Trace trace1,int[] buf){
		return isPrecedeLoose(trace0,trace1,buf);
	}
	private static boolean fixVBar(Trace trace0,Trace trace1,int[] buf){
		BoundBox box0=trace0.getBoundBox();
		BoundBox box1=trace1.getBoundBox();
		if(box0.getWidth()>=8*box0.getHeight()&&Math.min(box0.getRight(),box1.getRight())-Math.max(box0.getLeft(),box1.getLeft())>=box1.getWidth()*3/4){
			if(box1.getBottom()-box0.getBottom()>=4*(box0.getTop()-box1.getTop())){
				return true;
			}
		}
		if(box1.getWidth()>=8*box1.getHeight()&&Math.min(box0.getRight(),box1.getRight())-Math.max(box0.getLeft(),box1.getLeft())>=box0.getWidth()*3/4){
			if(box1.getTop()-box0.getTop()>=4*(box0.getBottom()-box1.getBottom())){
				return true;
			}
		}
		return false;
	}
	private static boolean fixHBar(Trace trace0,Trace trace1,int[] buf){
		BoundBox box0=trace0.getBoundBox();
		BoundBox box1=trace1.getBoundBox();
		if(box0.getHeight()>=8*box0.getWidth()&&Math.min(box0.getBottom(),box1.getBottom())-Math.max(box0.getTop(),box1.getTop())>=box1.getHeight()*3/4){
			if(box1.getRight()-box0.getRight()>=4*(box0.getLeft()-box1.getLeft())){
				return true;
			}
		}
		if(box1.getHeight()>=8*box1.getWidth()&&Math.min(box0.getBottom(),box1.getBottom())-Math.max(box0.getTop(),box1.getTop())>=box0.getHeight()*3/4){
			if(box1.getLeft()-box0.getLeft()>=4*(box0.getRight()-box1.getRight())){
				return true;
			}
		}
		return false;
	}
	public static void interpolate(int top,int bottom,int[] buf,int nan){
		for(int y=top;y<=bottom;y++){
			if(buf[y]==nan){
				int y1=y-1;
				int y2=y+1;
				while(buf[y2]==nan){
					++y2;
				}
				for(;y<y2;y++){
					buf[y]=(buf[y1]*(y2-y)+buf[y2]*(y-y1))/(y2-y1);
				}
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
		return ResourceBundle.getBundle("cc.chungkwong.mathocr.message").getString("TOPOLOGICAL");
	}
}
