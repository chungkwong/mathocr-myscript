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
import cc.chungkwong.mathocr.common.*;
import cc.chungkwong.mathocr.online.*;
import java.util.*;
import java.util.stream.*;
/**
 * Greedy graph tracer
 *
 * @author Chan Chung Kwong
 */
public class GreedyGraphTracer implements GraphTracer{
	@Override
	public TraceList trace(Graph<Junction,Segment> graph){
		double thick=graph.getEdges().stream().mapToInt((s)->s.getThick()).average().orElse(Double.MAX_VALUE);
//		System.out.println(thick);
		List<Trace> traces=new ArrayList<>();
		traceDot(graph,traces);
		for(Iterator<Graph<Junction,Segment>> iterator=graph.getComponents();iterator.hasNext();){
			Graph<Junction,Segment> component=iterator.next();
			Graph<Junction,Segment> componentBackup=component.clone();
			traceThrough(component);
			Map<Trace,Pair<Junction,Junction>> pretrace=traceBend(component);
			fixDouble(pretrace,componentBackup,thick);
			List<Trace> list=pretrace.keySet().stream().sorted(Comparator.comparingInt((t)->t.getPoints().size())).collect(Collectors.toList());
			if(list.size()>=2&&list.get(0).getPoints().size()<=list.get(1).getPoints().size()/16){
				traces.addAll(list.subList(1,list.size()));
			}else{
				traces.addAll(list);
			}
//			traces.addAll(pretrace.keySet());
		}
		return new TraceList(traces);
	}
	private static void traceDot(Graph<Junction,Segment> graph,List<Trace> traces){
		for(Iterator<Junction> iterator=graph.getVertexs().iterator();iterator.hasNext();){
			Junction vertex=iterator.next();
			if(graph.getEdges(vertex)==null||graph.getEdges(vertex).isEmpty()){
				int x=(int)vertex.getTrace().getPoints().stream().mapToInt((p)->p.getX()).average().getAsDouble();
				int y=(int)vertex.getTrace().getPoints().stream().mapToInt((p)->p.getY()).average().getAsDouble();
				Trace trace=new Trace(new LinkedList<>());
				trace.getPoints().add(new TracePoint(x,y));
				traces.add(trace);
				iterator.remove();
			}
		}
	}
	private static void traceThrough(Graph<Junction,Segment> graph){
		class Ray{
			private final Segment segment;
			private final boolean forward;
			public Ray(Segment segment,boolean forward){
				this.segment=segment;
				this.forward=forward;
			}
			public Segment getSegment(){
				return segment;
			}
			public boolean isForward(){
				return forward;
			}
			public Junction getStart(Graph<Junction,Segment> graph){
				return forward?graph.getStart(segment):graph.getEnd(segment);
			}
//			public Junction getEnd(Graph<Junction,Segment> graph){
//				return forward?graph.getEnd(segment):graph.getStart(segment);
//			}
			public double getStartAngle(){
				return forward?segment.getAngleBegin():segment.getAngleEnd()+Math.PI;
			}
			public double getEndAngle(){
				return forward?segment.getAngleEnd():segment.getAngleBegin()+Math.PI;
			}
			public Ray reverse(){
				return new Ray(segment,!forward);
			}
			@Override
			public boolean equals(Object obj){
				return obj instanceof Ray&&segment==((Ray)obj).segment&&forward==((Ray)obj).forward;
			}
			@Override
			public int hashCode(){
				int hash=3;
				hash=97*hash+Objects.hashCode(this.segment);
				hash=97*hash+(this.forward?1:0);
				return hash;
			}
			@Override
			public String toString(){
				return "("+segment+","+forward+")";
			}
		}
		class Turn{
			private final Ray start, end;
			private final double angle;
			public Turn(Ray start,Ray end){
				this.start=start;
				this.end=end;
				this.angle=Math.abs(normalize(end.getStartAngle()+Math.PI-start.getStartAngle()));
			}
			public Ray getStart(){
				return start;
			}
			public Ray getEnd(){
				return end;
			}
			public double getAngle(){
				return angle;
			}
		}
		HashMap<Ray,Ray> status=new HashMap<>();
		HashMap<Ray,Ray> ends=new HashMap<>();
		List<Turn> turns=new ArrayList<>();
		for(Junction joint:graph.getVertexs()){
			List<Ray> rays=new ArrayList<>();
			for(Segment edge:graph.getEdges(joint)){
				if(graph.getStart(edge)==joint){
					Ray ray=new Ray(edge,true);
					rays.add(ray);
					status.put(ray,ray);
					ends.put(ray,ray.reverse());
				}
				if(graph.getEnd(edge)==joint){
					Ray ray=new Ray(edge,false);
					rays.add(ray);
					status.put(ray,ray);
					ends.put(ray,ray.reverse());
				}
			}
			for(int i=0;i<rays.size();i++){
				for(int j=i+1;j<rays.size();j++){
					if(rays.get(i).segment!=rays.get(j).segment){
						turns.add(new Turn(rays.get(i),rays.get(j)));
					}
				}
			}
		}
		Collections.sort(turns,Comparator.comparingDouble((t)->-t.getAngle()));
		while(!turns.isEmpty()){
			Turn turn=turns.remove(turns.size()-1);
			Ray startRaw=turn.getStart();
			Ray endRaw=turn.getEnd();
			Ray start=status.get(startRaw);
			Ray end=status.get(endRaw);
			if(start==null||end==null||start.getSegment()==end.getSegment()){
				continue;
			}
			Segment concat=new Segment(
					new Trace(new ArrayList<>(start.getSegment().getTrace().getPoints().size()+end.getSegment().getTrace().getPoints().size())),
					Math.max(start.getSegment().getThick(),end.getSegment().getThick()),
					start.getEndAngle()+Math.PI,
					end.getEndAngle()
			);
			List<TracePoint> points=concat.getTrace().getPoints();
			points.addAll(start.getSegment().getTrace().getPoints());
			if(start.isForward()){
				Collections.reverse(points);
			}
			points.addAll(end.getSegment().getTrace().getPoints());
			if(!end.isForward()){
				Collections.reverse(points.subList(points.size()-end.getSegment().getTrace().getPoints().size(),points.size()));
			}
			status.remove(endRaw);
			status.remove(startRaw);
			Ray forward=new Ray(concat,true);
			Ray backward=new Ray(concat,false);
			ends.put(forward,ends.get(end));
			ends.put(backward,ends.get(start));
			status.put(ends.get(end),backward);
			status.put(ends.get(start),forward);
			graph.merge(concat,start.getSegment(),end.getSegment(),end.getStart(graph));
			//System.out.println(Graph.toString(graph));
		}
	}
	private static Map<Trace,Pair<Junction,Junction>> traceBend(Graph<Junction,Segment> graph){
		Map<Trace,Pair<Junction,Junction>> traceEnds=new HashMap<>();
		for(Segment edge:graph.getEdges()){
			traceEnds.put(edge.getTrace(),new Pair<>(graph.getStart(edge),graph.getEnd(edge)));
		}
		return traceEnds;
	}
	private static void fixDouble(Map<Trace,Pair<Junction,Junction>> traceEnds,Graph<Junction,Segment> graph,double thick){
		Map<Junction,Integer> degree=graph.getVertexs().stream().collect(Collectors.toMap((vertex)->vertex,
				(vertex)->graph.getEdges(vertex).stream().mapToInt((e)->graph.getStart(e)==graph.getEnd(e)?2:1).sum()));
		Map<Junction,Trace> jt=new HashMap<>();
		for(Map.Entry<Trace,Pair<Junction,Junction>> entry:traceEnds.entrySet()){
			Trace key=entry.getKey();
			Pair<Junction,Junction> value=entry.getValue();
			if(jt.containsKey(value.getKey())){
				jt.put(value.getKey(),null);
			}else{
				jt.put(value.getKey(),key);
			}
			if(jt.containsKey(value.getValue())){
				jt.put(value.getValue(),null);
			}else{
				jt.put(value.getValue(),key);
			}
		}
		Map<Segment,Double> turning=new HashMap<>();
		List<Segment> linkages=graph.getEdges().stream().filter((edge)->{
			Junction start=graph.getStart(edge);
			Junction end=graph.getEnd(edge);
			if(start==end||degree.get(start)%2==0||degree.get(end)%2==0){
				return false;
			}
			Trace startTrace=jt.get(start);
			Trace endTrace=jt.get(end);
			if(startTrace!=null&&endTrace!=null&&startTrace!=endTrace){
				turning.put(edge,getTurning(startTrace,edge,endTrace,traceEnds,graph,thick));
				return true;
			}else{
				return false;
			}
		}).sorted(Comparator.comparingInt((Segment edge)->-degree.get(graph.getStart(edge))-degree.get(graph.getEnd(edge))).thenComparingDouble((edge)->turning.get(edge)))
				.collect(Collectors.toList());
		for(Segment edge:linkages){
			Junction start=graph.getStart(edge);
			Junction end=graph.getEnd(edge);
			Trace startTrace=jt.get(start);
			Trace endTrace=jt.get(end);
			if(startTrace==null||endTrace==null||traceEnds.get(startTrace)==null||traceEnds.get(endTrace)==null){
				continue;
			}
			boolean startForward=traceEnds.get(startTrace).getValue()==start;
			boolean endForward=traceEnds.get(endTrace).getKey()==end;
			Junction traceStart=startForward?traceEnds.get(startTrace).getKey():traceEnds.get(startTrace).getValue();
			Junction traceEnd=endForward?traceEnds.get(endTrace).getValue():traceEnds.get(endTrace).getKey();
			if(isRightAngle(startTrace,startForward,edge.getTrace(),true,thick)
					||isRightAngle(edge.getTrace(),true,endTrace,endForward,thick)){
				continue;
			}
			if(isLine(startTrace)&&isLine(endTrace)&&isLine(edge.getTrace())){
				continue;
			}
			Trace joined=new Trace(new ArrayList<>(startTrace.getPoints().size()+edge.getTrace().getPoints().size()+endTrace.getPoints().size()));
			joined.getPoints().addAll(startTrace.getPoints());
			if(!startForward){
				Collections.reverse(joined.getPoints());
			}
			joined.getPoints().addAll(edge.getTrace().getPoints());
			joined.getPoints().addAll(endTrace.getPoints());
			if(!endForward){
				Collections.reverse(joined.getPoints().subList(joined.getPoints().size()-endTrace.getPoints().size(),joined.getPoints().size()));
			}
			traceEnds.remove(startTrace);
			traceEnds.remove(endTrace);
			traceEnds.put(joined,new Pair<>(traceStart,traceEnd));
			jt.remove(start);
			jt.remove(end);
			if(jt.get(traceStart)!=null){
				jt.put(traceStart,joined);
			}
			if(jt.get(traceEnd)!=null){
				jt.put(traceEnd,joined);
			}
		}
	}
	private static double getTurning(Trace from,Segment bridge,Trace to,Map<Trace,Pair<Junction,Junction>> traceEnds,Graph<Junction,Segment> graph,double thick){
		double angle0=graph.getStart(bridge)==traceEnds.get(from).getValue()
				?Segment.estimateEndAngle(from,thick):Segment.estimateStartAngle(from,thick)+Math.PI;
		double angle1=bridge.getAngleBegin();
		double angle2=bridge.getAngleEnd();
		double angle3=graph.getEnd(bridge)==traceEnds.get(to).getKey()
				?Segment.estimateStartAngle(to,thick):Segment.estimateEndAngle(to,thick)+Math.PI;
		return Math.abs(normalize(angle1-angle0))+Math.abs(normalize(angle3-angle2));
	}
	private static boolean isRightAngle(Trace start,boolean startForward,Trace end,boolean endForward,double thick){
		double startAngle=startForward?Segment.estimateEndAngle(start,thick):Segment.estimateStartAngle(start,thick)+Math.PI;
		double endAngle=endForward?Segment.estimateStartAngle(end,thick):Segment.estimateEndAngle(end,thick)+Math.PI;
		return Math.abs(Math.abs(normalize(endAngle-startAngle))-Math.PI/2)<Math.PI/8;
	}
	private static boolean isLine(Trace trace){
		long sumX=0, sumY=0, sumXX=0, sumXY=0, sumYY=0, n=trace.getPoints().size();
		if(n<=10){
			return false;
		}
		int xmin=Integer.MAX_VALUE, ymin=Integer.MAX_VALUE, xmax=Integer.MIN_VALUE, ymax=Integer.MIN_VALUE;
		for(TracePoint point:trace.getPoints()){
			int x=point.getX();
			int y=point.getY();
			sumX+=x;
			sumY+=y;
			sumXX+=x*x;
			sumXY+=x*y;
			sumYY+=y*y;
			if(x<xmin){
				xmin=x;
			}
			if(y<ymin){
				ymin=y;
			}
			if(x>xmax){
				xmax=x;
			}
			if(y>ymax){
				ymax=y;
			}
		}
		long vx=n*sumXX-sumX*sumX;
		long vy=n*sumYY-sumY*sumY;
		if(vx==0||vy==0){
			return true;
		}
		long c=n*sumXY-sumX*sumY;
		long d=Math.min(vy-c*c/vx,vx-c*c/vy);
		long dd=(xmax-xmin+1)*(xmax-xmin+1)+(ymax-ymin+1)*(ymax-ymin+1);
		return d/n/n<dd/4096;
	}
	private static final double TWO_PI=2*Math.PI;
	private static double normalize(double angle){
		if(Double.isNaN(angle)){
			System.out.println("NaN");
			return Math.PI;
		}else{
			return angle-Math.round(angle/TWO_PI)*TWO_PI;
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
		return "greedy";
	}
}
