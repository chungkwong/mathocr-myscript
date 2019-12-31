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
import com.fasterxml.jackson.core.*;
import java.io.*;
import java.nio.charset.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class JsonFormat implements TraceListFormat{
	@Override
	public String getSuffix(){
		return "json";
	}
	@Override
	public TraceList read(File file) throws IOException{
		TraceList list;
		try(JsonParser parser=new JsonFactory().createParser(file)){
			list=new TraceList();
			parser.nextToken();
			while(parser.nextToken().isStructStart()){
				Trace trace=new Trace();
				JsonToken token=parser.nextToken();
				while(token.isNumeric()){
					int x=parser.getValueAsInt();
					parser.nextToken();
					int y=parser.getValueAsInt();
					trace.getPoints().add(new TracePoint(x,y));
					token=parser.nextToken();
				}
				list.getTraces().add(trace);
			}
		}
		return list;
	}
	@Override
	public void write(TraceList traceList,File file) throws IOException{
		try(Writer out=new BufferedWriter(new FileWriter(file,StandardCharsets.UTF_8))){
			write(traceList,out);
		}
	}
	public void write(TraceList traceList,Writer out) throws IOException{
		out.write('[');
		boolean outComma=false;
		for(Trace trace:traceList.getTraces()){
			if(outComma){
				out.write(',');
			}else{
				outComma=true;
			}
			out.write('[');
			boolean inComma=false;
			for(TracePoint point:trace.getPoints()){
				if(inComma){
					out.write(',');
				}else{
					inComma=true;
				}
				out.write(Integer.toString(point.getX()));
				out.write(',');
				out.write(Integer.toString(point.getY()));
			}
			out.write(']');
		}
		out.write(']');
		out.flush();
	}
}
