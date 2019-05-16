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
/**
 * Point
 *
 * @author Chan Chung Kwong
 */
public class TracePoint{
	private final int x, y;
	/**
	 * Create a point
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public TracePoint(int x,int y){
		this.x=x;
		this.y=y;
	}
	/**
	 *
	 * @return the x coordinate
	 */
	public int getX(){
		return x;
	}
	/**
	 *
	 * @return the y coordinate
	 */
	public int getY(){
		return y;
	}
	/**
	 *
	 * @param width width of the image
	 * @return index of the point in the image
	 */
	public int toIndex(int width){
		return y*width+x;
	}
	/**
	 *
	 * @param index index of the point in the image
	 * @param width width of the image
	 * @return the point
	 */
	public static TracePoint fromIndex(int index,int width){
		return new TracePoint(index%width,index/width);
	}
	@Override
	public boolean equals(Object obj){
		return obj instanceof TracePoint&&((TracePoint)obj).x==x&&((TracePoint)obj).y==y;
	}
	@Override
	public int hashCode(){
		int hash=5;
		hash=47*hash+this.x;
		hash=47*hash+this.y;
		return hash;
	}
	@Override
	public String toString(){
		return "("+x+","+y+")";
	}
	public static int getDistanceSquare(TracePoint p,TracePoint q){
		int dx=p.getX()-q.getX();
		int dy=p.getY()-q.getY();
		return dx*dx+dy*dy;
	}
}
