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
 * Frequency counters
 *
 * @author Chan Chung Kwong
 * @param <T> type of object to be counted
 */
public class FrequencyTable<T>{
	private final Map<T,Counter> frequencyTable=new HashMap<>();
	private static final Counter EMPTY_COUNTER=new Counter(0);
	/**
	 * Create a table
	 */
	public FrequencyTable(){
	}
	/**
	 * Count a object
	 *
	 * @param key the object
	 */
	public void advance(T key){
		Counter counter=frequencyTable.get(key);
		if(counter==null){
			frequencyTable.put(key,new Counter(1));
		}else{
			counter.advance();
		}
	}
	/**
	 * @param key a object
	 * @return count of the object
	 */
	public int getCount(T key){
		return frequencyTable.getOrDefault(key,EMPTY_COUNTER).getCount();
	}
	/**
	 *
	 * @return set of objects counted
	 */
	public Set<T> getKeys(){
		return frequencyTable.keySet();
	}
	@Override
	public String toString(){
		return frequencyTable.toString();
	}
	private static class Counter{
		private int count;
		public Counter(int count){
			this.count=count;
		}
		public void advance(){
			++count;
		}
		public int getCount(){
			return count;
		}
		@Override
		public String toString(){
			return Integer.toString(count);
		}
	}
}
