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
import cc.chungkwong.mathocr.common.BoundBox;
import java.util.*;
import java.util.List;
/**
 * Stroke
 *
 * @author Chan Chung Kwong
 */
public class Trace{
	private final List<TracePoint> points;
	private final String id;
	private BoundBox box;
	/**
	 * Create a empty stroke
	 */
	public Trace(){
		this(new ArrayList<>());
	}
	/**
	 * Create a stroke
	 *
	 * @param points underlying points
	 */
	public Trace(List<TracePoint> points){
		this(points,null);
	}
	/**
	 * Create a empty stroke
	 *
	 * @param id ID
	 */
	public Trace(String id){
		this(new ArrayList<>(),id);
	}
	/**
	 * Create a stroke
	 *
	 * @param points underlying points
	 * @param id ID
	 */
	public Trace(List<TracePoint> points,String id){
		this.points=points;
		this.id=id;
	}
	/**
	 *
	 * @return underlying points
	 */
	public List<TracePoint> getPoints(){
		return points;
	}
	/**
	 *
	 * @return first point
	 */
	public TracePoint getStart(){
		return points.get(0);
	}
	/**
	 *
	 * @return last point
	 */
	public TracePoint getEnd(){
		return points.get(points.size()-1);
	}
	/**
	 *
	 * @return ID
	 */
	public String getId(){
		return id;
	}
	/**
	 *
	 * @return bounding box of the stroke(cached)
	 */
	public BoundBox getBoundBox(){
		if(box==null){
			int maxX=Integer.MIN_VALUE, minX=Integer.MAX_VALUE;
			int maxY=Integer.MIN_VALUE, minY=Integer.MAX_VALUE;
			for(TracePoint point:getPoints()){
				int x=point.getX();
				int y=point.getY();
				if(x<minX){
					minX=x;
				}
				if(y<minY){
					minY=y;
				}
				if(x>maxX){
					maxX=x;
				}
				if(y>maxY){
					maxY=y;
				}
			}
			box=new BoundBox(minX,maxX,minY,maxY);
		}
		return box;
	}
	/**
	 * Clear cached bounding box
	 */
	public void invalidBoundBox(){
		box=null;
	}
	@Override
	public String toString(){
		return points.toString();
	}
}
