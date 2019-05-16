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
import cc.chungkwong.mathocr.offline.StrokeWidthTransform;
import cc.chungkwong.mathocr.offline.Bitmap;
import cc.chungkwong.mathocr.offline.preprocessor.Thinning;
import cc.chungkwong.mathocr.online.Trace;
import cc.chungkwong.mathocr.online.TracePoint;
import java.util.*;
/**
 * Skeleton tracer that use thinning
 *
 * @author Chan Chung Kwong
 */
public class ThinTracer implements SkeletonTracer{
	@Override
	public Graph<Junction,Segment> trace(Bitmap image){
		Graph<Junction,Segment> graph=buildRawGraph(image);
		simplifyGraph(graph);
		return graph;
	}
	public static Graph<Junction,Segment> buildRawGraph(Bitmap image){
		StrokeWidthTransform.StrokeSpace strokeSpace=StrokeWidthTransform.transform(image);
		int length=strokeSpace.getThicknessH().length;
		int[] thicknessSq=new int[length];
		for(int i=0;i<length;i++){
			thicknessSq[i]=Math.min(square(strokeSpace.getThicknessH()[i]),2*square(strokeSpace.getThicknessS()[i]));
		}
		Thinning.thin(image);//FIXME Changed input
		return buildRawGraph(image,thicknessSq);
	}
	private static Graph<Junction,Segment> buildRawGraph(Bitmap bitmap,int[] thicknessSq){
		int width=bitmap.getWidth();
		int height=bitmap.getHeight();
		List<Segment> segments=new ArrayList<>();
		Map<Junction,Set<Segment>> vertexs=new HashMap<>();
		Component[] index=new Component[width*height];
		followEdge(index,segments,bitmap,thicknessSq);
		followJoint(index,vertexs,bitmap,thicknessSq);
		return buildRawGraph(segments,vertexs);
	}
	private static void followEdge(Component[] index,List<Segment> edges,Bitmap bitmap,int[] thicknessSq){
		int width=bitmap.getWidth();
		byte[] bits=bitmap.getData();
		for(int found=0;found<bits.length;found++){
			if(bits[found]!=0||index[found]!=null){
				continue;
			}
			if(isEdge(found,bitmap)){
				Segment tracing=new Segment();
				List<TracePoint> points=tracing.getTrace().getPoints();
				points.add(TracePoint.fromIndex(found,width));
				index[found]=tracing;
				int[] init=getNeighbors(found,bitmap);
				int last=found, curr=init[0];
				while(index[curr]==null&&isEdge(curr,bitmap)){
					points.add(TracePoint.fromIndex(curr,width));
					index[curr]=tracing;
					int[] cand=getNeighbors(curr,bitmap);
					if(cand[1]==-1){
						break;
					}
					int tmp=curr;
					curr=last!=cand[0]?cand[0]:cand[1];
					last=tmp;
				}
				if(init[1]!=-1){
					last=found;
					curr=init[1];
					while(index[curr]==null&&isEdge(curr,bitmap)){
						points.add(0,TracePoint.fromIndex(curr,width));
						index[curr]=tracing;
						int[] cand=getNeighbors(curr,bitmap);
						if(cand[1]==-1){
							break;
						}
						int tmp=curr;
						curr=last!=cand[0]?cand[0]:cand[1];
						last=tmp;
					}
				}
				//tracing.setThick(points.stream().mapToInt((p)->thicknessSq[p.toIndex(width)]).sorted().skip(points.size()/2).findFirst().getAsInt());
				tracing.setThick((int)(points.stream().mapToInt((p)->thicknessSq[p.toIndex(width)]).average().getAsDouble()+0.5));
				tracing.updateAngles();
				edges.add(tracing);
			}
		}
	}
	private static boolean isEdge(int ind,Bitmap bitmap){
		int width=bitmap.getWidth();
		byte[] pixels=bitmap.getData();
		boolean[] neighbor={
			pixels[ind-width]==0,
			pixels[ind-width+1]==0,
			pixels[ind+1]==0,
			pixels[ind+width+1]==0,
			pixels[ind+width]==0,
			pixels[ind+width-1]==0,
			pixels[ind-1]==0,
			pixels[ind-width-1]==0
		};
		boolean last=neighbor[7];
		int components=0;
		int points=0;
		for(int i=0;i<8;i++){
			if(neighbor[i]){
				++points;
				if(!last){
					++components;
				}
			}
			last=neighbor[i];
		}
		return components==2&&points==2;
	}
	private static int[] getNeighbors(int ind,Bitmap bitmap){
		int width=bitmap.getWidth();
		byte[] pixels=bitmap.getData();
		int[] neighbor={
			ind+1,ind+width,ind+width-1,ind+width+1,ind-width,ind-width+1,ind-1,ind-width-1
		};
		int[] neighbors={-1,-1};
		int k=0;
		for(int i=0;i<8;i++){
			if(pixels[neighbor[i]]==0){
				neighbors[k++]=neighbor[i];
			}
		}
		return neighbors;
	}
	private static void followJoint(Component[] index,Map<Junction,Set<Segment>> neighborhood,Bitmap bitmap,int[] thicknessSq){
		int width=bitmap.getWidth();
		int[] offsets={1,-width+1,-width,-width-1,-1,width-1,width,width+1};
		byte[] bits=bitmap.getData();
		for(int found=0;found<bits.length;found++){
			if(bits[found]!=0||index[found]!=null){
				continue;
			}
			Junction tracing=new Junction(new Trace(new LinkedList<>()));
			HashSet<Segment> neighbors=new HashSet<>();
			int thick=thicknessSq[found];
			neighborhood.put(tracing,neighbors);
			List<TracePoint> points=tracing.getTrace().getPoints();
			points.add(TracePoint.fromIndex(found,width));
			index[found]=tracing;
			LinkedList<Integer> toTrace=new LinkedList<>();
			toTrace.push(found);
			while(!toTrace.isEmpty()){
				Integer pop=toTrace.pop();
				for(int offset:offsets){
					int curr=pop+offset;
					if(bits[curr]==0){
						if(index[curr]==null){
							points.add(TracePoint.fromIndex(curr,width));
							index[curr]=tracing;
							toTrace.push(curr);
							if(thicknessSq[curr]>thick){
								thick=thicknessSq[curr];
							}
						}else if(index[curr] instanceof Segment){
							neighbors.add((Segment)index[curr]);
						}
					}
				}
			}
			tracing.setThick(thick);
		}
	}
	private static Graph<Junction,Segment> buildRawGraph(List<Segment> segments,Map<Junction,Set<Segment>> vertexs){
		Graph<Junction,Segment> graph=new Graph<>();
		Map<Segment,List<Junction>> ends=new HashMap<>();
		for(Segment segment:segments){
			ends.put(segment,new ArrayList<>(2));
		}
		for(Map.Entry<Junction,Set<Segment>> entry:vertexs.entrySet()){
			Junction vertex=entry.getKey();
			for(Segment substroke:entry.getValue()){
				ends.get(substroke).add(vertex);
			}
			graph.getVertexs().add(vertex);
		}
		for(Map.Entry<Segment,List<Junction>> entry:ends.entrySet()){
			Segment key=entry.getKey();
			List<Junction> value=entry.getValue();
			TracePoint segmentStart=key.getTrace().getStart();
			if(value.isEmpty()){
				Junction joint=new Junction(new Trace(new LinkedList<>()));
				joint.getTrace().getPoints().add(segmentStart);
				graph.add(key,joint,joint);
			}else if(value.size()==1){
				graph.add(key,value.get(0),value.get(0));
			}else{
				Junction jointStart;
				Junction jointEnd;
				if(value.size()!=2){
					throw new RuntimeException();
				}
				if(isNeighbor(value.get(0).getTrace().getPoints(),segmentStart)){
					jointStart=value.get(0);
					jointEnd=value.get(1);
				}else{
					jointStart=value.get(1);
					jointEnd=value.get(0);
				}
				graph.add(key,jointStart,jointEnd);
			}
		}
		return graph;
	}
	private static boolean isNeighbor(List<TracePoint> p,TracePoint q){
		return p.stream().anyMatch((r)->isNeighbor(r,q));
	}
	private static boolean isNeighbor(TracePoint p,TracePoint q){
		int dx=p.getX()-q.getX();
		int dy=p.getY()-q.getY();
		return dx>=-1&&dx<=1&&dy>=-1&&dy<=1;
	}
//	public static TraceList trace(Graph<Junction,Segment> graph){
//
	private static final double DOT_THREHOLD=0.5;
	public static void simplifyGraph(Graph<Junction,Segment> graph){
		double thick=graph.getEdges().stream().mapToInt((s)->s.getThick()).average().orElse(Double.MAX_VALUE);
		boolean changed=true;
		while(changed){
			changed=false;
			for(Segment edge:graph.getEdges()){
				int t=square(edge.getTrace().getPoints().size());
				if(t<=edge.getThick()||t<=thick||t<=9){
					removeEdge(edge,graph);
					changed=true;
					break;
				}
			}
		}
		thick=graph.getEdges().stream().mapToInt((s)->s.getThick()).average().orElse(0);
		int minDotSize=(int)(thick/4);
		graph.getVertexs().removeIf((v)->graph.getEdges(v)!=null&&graph.getEdges(v).isEmpty()&&v.getThick()<minDotSize);
	}
	private static void removeEdge(Segment edge,Graph<Junction,Segment> graph){
		Junction joint1=graph.getStart(edge);
		Junction joint2=graph.getEnd(edge);
		graph.remove(edge);
		if(joint1==joint2){
			return;
		}
		joint1.getTrace().getPoints().addAll(joint2.getTrace().getPoints());
		joint1.getTrace().getPoints().addAll(edge.getTrace().getPoints());
		List<Segment> affected=new ArrayList<>(graph.getEdges(joint2));
		for(Segment substroke:affected){
			Junction start=graph.getStart(substroke);
			Junction end=graph.getEnd(substroke);
			graph.remove(substroke);
			if(start==joint2){
				start=joint1;
			}
			if(end==joint2){
				end=joint1;
			}
			graph.add(substroke,start,end);
		}
		graph.getVertexs().remove(joint2);
	}
	private static int square(int i){
		return i*i;
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
		return "thinning";
	}
}
