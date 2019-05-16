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
 * A data structure representing partition of objects
 */
public class Partition{
	private final ArrayList<Integer> parent;
	private final ArrayList<Integer> rank;
	private final Linkable work;
	/**
	 * Construct a Partition without any set
	 *
	 * @param work indicating addition work when linking two sets
	 */
	public Partition(Linkable work){
		parent=new ArrayList<>();
		rank=new ArrayList<>();
		this.work=work;
	}
	/**
	 * Construct a Partition with some sets
	 *
	 * @param work indicating addition work when linking two sets
	 * @param n number of set containing exactly one element at frist
	 */
	public Partition(Linkable work,int n){
		parent=new ArrayList<>(n);
		rank=new ArrayList<>(n);
		for(int i=0;i<n;i++){
			parent.add(null);
			rank.add(0);
		}
		this.work=work;
	}
	/**
	 * Make a new set containing exactly one element
	 */
	public void makeSet(){
		parent.add(null);
		rank.add(0);
	}
	/**
	 * Find the root of the set containing a element
	 *
	 * @param n the index of the element
	 * @return root
	 */
	public int findRoot(int n){
		LinkedList<Integer> stack=new LinkedList<>();
		while(parent.get(n)!=null){
			stack.push(n);
			n=parent.get(n);
		}
		while(!stack.isEmpty()){
			parent.set(stack.pop(),n);
		}
		return n;
	}
	/**
	 * Link two sets
	 *
	 * @param m root of the frist set
	 * @param n root of the second set
	 */
	private void link(int m,int n){
		if(m==n){
			return;
		}
		int rankm=rank.get(m), rankn=rank.get(n);
		if(rankm>rankn){
			parent.set(n,m);
			if(work!=null){
				work.link(n,m);
			}
		}else{
			parent.set(m,n);
			if(work!=null){
				work.link(m,n);
			}
			if(rankm==rankn){
				rank.set(n,rankn+1);
			}
		}
	}
	/**
	 * Combine two sets
	 *
	 * @param m an element contained in the first set
	 * @param n an element contained in the second set
	 */
	public void union(int m,int n){
		link(findRoot(m),findRoot(n));
	}
	/**
	 * Check if a element is root
	 *
	 * @param n the index the element
	 * @return
	 */
	public boolean isRoot(int n){
		return parent.get(n)==null;
	}
}
