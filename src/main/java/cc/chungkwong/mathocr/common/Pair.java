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
import java.io.*;
import java.util.*;
/**
 * Pair of objects
 *
 * @author Chan Chung Kwong
 * @param <K> type of first object
 * @param <V> type of second object
 */
public class Pair<K,V> implements Serializable{
	private final K key;
	private final V value;
	/**
	 * Create a pair
	 *
	 * @param key the first object
	 * @param value the second object
	 */
	public Pair(K key,V value){
		this.key=key;
		this.value=value;
	}
	/**
	 *
	 * @return the first object
	 */
	public K getKey(){
		return key;
	}
	/**
	 *
	 * @return the second object
	 */
	public V getValue(){
		return value;
	}
	@Override
	public boolean equals(Object obj){
		return obj instanceof Pair&&Objects.equals(key,((Pair)obj).key)&&Objects.equals(value,((Pair)obj).value);
	}
	@Override
	public int hashCode(){
		int hash=3;
		hash=97*hash+Objects.hashCode(this.key);
		hash=97*hash+Objects.hashCode(this.value);
		return hash;
	}
	@Override
	public String toString(){
		return "("+key+","+value+")";
	}
}
