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
package cc.chungkwong.mathocr.common;
import java.util.*;
/**
 * Bounding box
 *
 * @author Chan Chung Kwong
 */
public class BoundBox{
	private final int left, right, top, bottom;
	/**
	 * Create a bounding box
	 *
	 * @param left min X
	 * @param right max X
	 * @param top min Y
	 * @param bottom max Y
	 */
	public BoundBox(int left,int right,int top,int bottom){
		this.left=left;
		this.right=right;
		this.top=top;
		this.bottom=bottom;
	}
	/**
	 * @return min X
	 */
	public int getLeft(){
		return left;
	}
	/**
	 * @return max X
	 */
	public int getRight(){
		return right;
	}
	/**
	 * @return min Y
	 */
	public int getTop(){
		return top;
	}
	/**
	 * @return max Y
	 */
	public int getBottom(){
		return bottom;
	}
	/**
	 * @return width
	 */
	public int getWidth(){
		return right-left+1;
	}
	/**
	 * @return height
	 */
	public int getHeight(){
		return bottom-top+1;
	}
	/**
	 * @return area
	 */
	public int getArea(){
		int width=getWidth();
		int height=getHeight();
		return width>0&&height>0?width*height:0;
	}
	/**
	 * Check if a point is in the box
	 *
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return
	 */
	public boolean contains(int x,int y){
		return x>=left&&x<=right&&y>=top&&y<=bottom;
	}
	/**
	 * Zoom this box
	 *
	 * @param factor the scale
	 * @return new bounding box
	 */
	public BoundBox scale(double factor){
		return new BoundBox((int)(left*factor),(int)(right*factor),(int)(top*factor),(int)(bottom*factor));
	}
	/**
	 * Get the intersection of two bounding boxes
	 *
	 * @param a a bounding box
	 * @param b another bounding box
	 * @return intersection
	 */
	public static BoundBox intersect(BoundBox a,BoundBox b){
		return new BoundBox(Math.max(a.getLeft(),b.getLeft()),Math.min(a.getRight(),b.getRight()),
				Math.max(a.getTop(),b.getTop()),Math.min(a.getBottom(),b.getBottom()));
	}
	/**
	 * Get the smallest box that contains two bounding boxes
	 *
	 * @param a a bounding box
	 * @param b another bounding box
	 * @return the smallest box that contains them
	 */
	public static BoundBox union(BoundBox a,BoundBox b){
		return new BoundBox(Math.min(a.getLeft(),b.getLeft()),Math.max(a.getRight(),b.getRight()),
				Math.min(a.getTop(),b.getTop()),Math.max(a.getBottom(),b.getBottom()));
	}
	/**
	 * Get the smallest box that contains all given bounding boxes
	 *
	 * @param box given bounding boxes
	 * @return the smallest box that contains them
	 */
	public static BoundBox union(Iterator<BoundBox> box){
		int left=Integer.MAX_VALUE, right=Integer.MIN_VALUE, top=Integer.MAX_VALUE, bottom=Integer.MIN_VALUE;
		while(box.hasNext()){
			BoundBox next=box.next();
			if(next.left<left){
				left=next.left;
			}
			if(next.top<top){
				top=next.top;
			}
			if(next.right>right){
				right=next.right;
			}
			if(next.bottom>bottom){
				bottom=next.bottom;
			}
		}
		return new BoundBox(left,right,top,bottom);
	}
	@Override
	public boolean equals(Object obj){
		return obj instanceof BoundBox
				&&((BoundBox)obj).left==left
				&&((BoundBox)obj).right==right
				&&((BoundBox)obj).top==top
				&&((BoundBox)obj).bottom==bottom;
	}
	@Override
	public int hashCode(){
		int hash=7;
		hash=73*hash+this.left;
		hash=73*hash+this.right;
		hash=73*hash+this.top;
		hash=73*hash+this.bottom;
		return hash;
	}
	@Override
	public String toString(){
		return left+","+right+","+top+","+bottom;
	}
}
