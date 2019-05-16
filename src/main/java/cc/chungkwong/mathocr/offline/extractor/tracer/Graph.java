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
import cc.chungkwong.mathocr.online.TracePoint;
import cc.chungkwong.mathocr.common.Pair;
import cc.chungkwong.mathocr.common.BoundBox;
import java.awt.image.*;
import java.util.*;
/**
 * Graph
 *
 * @author Chan Chung Kwong
 * @param <V> type of vertex
 * @param <E> type of edge
 */
public class Graph<V,E>{
	private final Map<E,Pair<V,V>> ends=new HashMap<>();
	private final Map<V,Set<E>> edges=new HashMap<>();
	private final Set<V> vertexs=new HashSet<>();
	/**
	 * Create a empty graph
	 */
	public Graph(){
	}
	/**
	 * Added a edge
	 *
	 * @param edge the edge
	 * @param start the start vertex
	 * @param end the end vertex
	 */
	public void add(E edge,V start,V end){
		ends.put(edge,new Pair<>(start,end));
		add(start,edge);
		add(end,edge);
	}
	private void add(V start,E edge){
		if(!edges.containsKey(start)){
			edges.put(start,new HashSet<>());
		}
		if(!vertexs.contains(start)){
			vertexs.add(start);
		}
		edges.get(start).add(edge);
	}
	/**
	 * Remove a edge
	 *
	 * @param edge to be removed
	 */
	public void remove(E edge){
		Pair<V,V> pair=ends.remove(edge);
		edges.get(pair.getKey()).remove(edge);
		edges.get(pair.getValue()).remove(edge);
	}
	/**
	 * Get a edge that link two given vertexes
	 *
	 * @param start a vertex
	 * @param end another vertex
	 * @return the edge or null
	 */
	public E get(V start,V end){
		if(edges.get(end).size()<edges.get(start).size()){
			V tmp=end;
			end=start;
			start=tmp;
		}
		for(E e:edges.get(start)){
			Pair<V,V> pair=ends.get(e);
			if((pair.getKey()==start&&pair.getValue()==end)||(pair.getValue()==start&&pair.getKey()==end)){
				return e;
			}
		}
		return null;
	}
	/**
	 * Merge two edge into one
	 *
	 * @param replacement new edge
	 * @param replace0 to be replaced
	 * @param replace1 to be replaced
	 * @param vertex the vertex between two edges
	 */
	public void merge(E replacement,E replace0,E replace1,V vertex){
		edges.get(vertex).remove(replace0);
		edges.get(vertex).remove(replace1);
		Pair<V,V> tmp=ends.remove(replace0);
		V start=Objects.equals(tmp.getKey(),vertex)?tmp.getValue():tmp.getKey();
		tmp=ends.remove(replace1);
		V end=Objects.equals(tmp.getKey(),vertex)?tmp.getValue():tmp.getKey();
		edges.get(start).remove(replace0);
		edges.get(end).remove(replace1);
		edges.get(start).add(replacement);
		edges.get(end).add(replacement);
		ends.put(replacement,new Pair<>(start,end));
	}
	/**
	 *
	 * @return all vertexes
	 */
	public Set<V> getVertexs(){
		return vertexs;
	}
	/**
	 *
	 * @return all edges
	 */
	public Set<E> getEdges(){
		return ends.keySet();
	}
	/**
	 *
	 * @param vertex
	 * @return all vertexes adjoint to a given vertex
	 */
	public Set<E> getEdges(V vertex){
		return edges.get(vertex);
	}
	/**
	 *
	 * @param edge
	 * @return the start vertex of a edge
	 */
	public V getStart(E edge){
		return ends.get(edge).getKey();
	}
	/**
	 *
	 * @param edge
	 * @return the end vertex of a edge
	 */
	public V getEnd(E edge){
		return ends.get(edge).getValue();
	}
	/**
	 *
	 * @return the connected components
	 */
	public Iterator<Graph<V,E>> getComponents(){
		return new Iterator<Graph<V,E>>(){
			private final HashSet<V> unvisited=new HashSet<>(vertexs);
			@Override
			public boolean hasNext(){
				return !unvisited.isEmpty();
			}
			@Override
			public Graph<V,E> next(){
				Graph<V,E> component=new Graph<>();
				LinkedList<V> found=new LinkedList<>();
				V joint=unvisited.iterator().next();
				found.push(joint);
				component.getVertexs().add(joint);
				while(!found.isEmpty()){
					joint=found.pop();
					for(E edge:getEdges(joint)){
						V start=getStart(edge);
						V end=getEnd(edge);
						if(!component.getVertexs().contains(start)){
							found.push(start);
						}
						if(!component.getVertexs().contains(end)){
							found.push(end);
						}
						component.add(edge,start,end);
					}
				}
				unvisited.removeAll(component.getVertexs());
				return component;
			}
		};
	}
	private static final int[] EDGE_COLORS={0xFFFF0000,0xFF00FF00,0xFF0000FF,0xFFFF00FF,0xFF00FFFF,0xFFFFFF00};
	public static BufferedImage visualize(Graph<Junction,Segment> graph,int width,int height){
		BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		int[] rgb=image.getRGB(0,0,width,height,null,0,width);
		Arrays.fill(rgb,0xFFFFFFFF);
		int colorIndex=0;
		for(Segment edge:graph.getEdges()){
			for(TracePoint point:edge.getTrace().getPoints()){
				rgb[point.toIndex(width)]=EDGE_COLORS[colorIndex%EDGE_COLORS.length];
			}
			++colorIndex;
		}
		for(Junction vertex:graph.getVertexs()){
			for(TracePoint point:vertex.getTrace().getPoints()){
				rgb[point.toIndex(width)]=0xFF000000;
			}
			++colorIndex;
		}
		image.setRGB(0,0,width,height,rgb,0,width);
		return image;
	}
	public static String toString(Graph<Junction,Segment> graph){
		int left=Integer.MAX_VALUE, top=Integer.MAX_VALUE;
		int right=Integer.MIN_VALUE, bottom=Integer.MIN_VALUE;
		for(Junction vertex:graph.getVertexs()){
			BoundBox box=vertex.getTrace().getBoundBox();
			if(box.getLeft()<left){
				left=box.getLeft();
			}
			if(box.getTop()<top){
				top=box.getTop();
			}
			if(box.getRight()>right){
				right=box.getRight();
			}
			if(box.getBottom()>bottom){
				bottom=box.getBottom();
			}
		}
		for(Segment edge:graph.getEdges()){
			BoundBox box=edge.getTrace().getBoundBox();
			if(box.getLeft()<left){
				left=box.getLeft();
			}
			if(box.getTop()<top){
				top=box.getTop();
			}
			if(box.getRight()>right){
				right=box.getRight();
			}
			if(box.getBottom()>bottom){
				bottom=box.getBottom();
			}
		}
		if(right==Integer.MIN_VALUE){
			return "";
		}
		int width=right-left+1;
		int height=bottom-top+1;
		StringBuilder buf=new StringBuilder();
		char[] type=new char[width*height];
		HashMap<Segment,Character> segmentName=new HashMap<>();
		HashMap<Junction,Character> jointName=new HashMap<>();
		Arrays.fill(type,'。');
		int colorIndex=0;
		for(Segment edge:graph.getEdges()){
			char name=(char)('人'+colorIndex);
			segmentName.put(edge,name);
			for(TracePoint point:edge.getTrace().getPoints()){
				type[(point.getY()-top)*width+point.getX()-left]=name;
			}
			++colorIndex;
		}
		for(Junction vertex:graph.getVertexs()){
			char name=(char)('口'+colorIndex);
			jointName.put(vertex,name);
			for(TracePoint point:vertex.getTrace().getPoints()){
				type[(point.getY()-top)*width+point.getX()-left]=name;
			}
			++colorIndex;
		}
		for(int i=0, ind=0;i<height;i++){
			for(int j=0;j<width;j++,ind++){
				buf.append(type[ind]);
			}
			buf.append('\n');
		}
		buf.append('\n');
		for(Segment s:graph.getEdges()){
			buf.append(segmentName.get(s)).append(':').
					append(jointName.get(graph.getStart(s))).
					append(jointName.get(graph.getEnd(s))).append('\t').
					append(s.getAngleBegin()*180/Math.PI).append('\t').
					append(s.getAngleEnd()*180/Math.PI).append('\n');
		}
		return buf.toString();
	}
	@Override
	public String toString(){
		return "|V|="+getVertexs().size()+",|E|="+getEdges().size();
	}
	@Override
	public Graph<V,E> clone(){
		Graph<V,E> spare=new Graph<>();
		spare.vertexs.addAll(vertexs);
		spare.ends.putAll(ends);
		for(Map.Entry<V,Set<E>> entry:edges.entrySet()){
			V key=entry.getKey();
			Set<E> value=entry.getValue();
			spare.edges.put(key,new HashSet<>(value));
		}
		return spare;
	}
}
