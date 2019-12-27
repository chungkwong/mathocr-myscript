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
package cc.chungkwong.mathocr.common.format;
import cc.chungkwong.mathocr.online.*;
import java.io.*;
import java.util.logging.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class InkmlFormat implements TraceListFormat{
	@Override
	public String getSuffix(){
		return "inkml";
	}
	@Override
	public TraceList read(File file) throws IOException{
		try{
			return new Ink(file).getTraceList();
		}catch(ParserConfigurationException|SAXException ex){
			Logger.getLogger(InkmlFormat.class.getName()).log(Level.SEVERE,null,ex);
			return new TraceList();
		}
	}
	@Override
	public void write(TraceList traceList,File file) throws IOException{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
