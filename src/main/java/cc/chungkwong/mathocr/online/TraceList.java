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
 * Sequence of strokes
 *
 * @author Chan Chung Kwong
 */
public class TraceList{
	private final List<Trace> traces;
	/**
	 * Create a empty sequence of strokes
	 */
	public TraceList(){
		this.traces=new ArrayList<>();
	}
	/**
	 * Create a sequence of strokes
	 *
	 * @param traces underlying list
	 */
	public TraceList(List<Trace> traces){
		this.traces=traces;
	}
	/**
	 *
	 * @return list of strokes
	 */
	public List<Trace> getTraces(){
		return traces;
	}
	/**
	 *
	 * @return bounding box of the strokes
	 */
	public BoundBox getBoundBox(){
		return BoundBox.union(traces.stream().map(Trace::getBoundBox).iterator());
	}
	@Override
	public String toString(){
		return traces.toString();
	}
}
