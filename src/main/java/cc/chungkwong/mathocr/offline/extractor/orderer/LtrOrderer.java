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
package cc.chungkwong.mathocr.offline.extractor.orderer;
import cc.chungkwong.mathocr.online.TraceList;
import java.util.*;
import java.util.stream.*;
/**
 * From left to right
 *
 * @author Chan Chung Kwong
 */
public class LtrOrderer implements Orderer{
	@Override
	public TraceList order(TraceList traceList){
		return new TraceList(traceList.getTraces().stream().
				sorted(Comparator.comparingInt((p)->p.getBoundBox().getLeft())).
				collect(Collectors.toList()));
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
		return ResourceBundle.getBundle("cc.chungkwong.mathocr.message").getString("LR");
	}
}
