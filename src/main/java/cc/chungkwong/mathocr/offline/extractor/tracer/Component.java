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
import cc.chungkwong.mathocr.online.*;
/**
 * Connected points
 *
 * @author Chan Chung Kwong
 */
public class Component{
	private final Trace trace;
	private int thick;
	/**
	 * Create a component
	 *
	 * @param trace points
	 */
	public Component(Trace trace){
		this.trace=trace;
	}
	/**
	 *
	 * @return points
	 */
	public Trace getTrace(){
		return trace;
	}
	/**
	 *
	 * @return thick
	 */
	public int getThick(){
		return thick;
	}
	/**
	 * Set thickness
	 *
	 * @param thick thickness
	 */
	public void setThick(int thick){
		this.thick=thick;
	}
}
