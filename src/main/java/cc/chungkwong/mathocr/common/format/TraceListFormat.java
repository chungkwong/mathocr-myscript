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
import cc.chungkwong.mathocr.offline.extractor.*;
import cc.chungkwong.mathocr.online.*;
import cc.chungkwong.mathocr.ui.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
/**
 * Format of trace list
 *
 * @author Chan Chung Kwong
 */
public interface TraceListFormat{
	String getSuffix();
	/**
	 * Read trace list from a file
	 *
	 * @param file the file
	 * @return the trace list
	 * @throws IOException
	 */
	TraceList read(File file) throws IOException;
	/**
	 * Save trace list to a file
	 *
	 * @param traceList trace list
	 * @param file the file
	 * @throws IOException
	 */
	void write(TraceList traceList,File file) throws IOException;
	/**
	 * Read trace list from a file
	 *
	 * @param file the file
	 * @return the trace list
	 * @throws IOException
	 */
	public static TraceList readFrom(File file) throws IOException{
		String fileName=file.getName();
		String formatName=fileName.contains(".")?fileName.substring(fileName.indexOf('.')+1):"png";
		for(TraceListFormat next:ServiceLoader.load(TraceListFormat.class)){
			if(formatName.endsWith(next.getSuffix())){
				return next.read(file);
			}
		}
		return Extractor.DEFAULT.extract(ImageIO.read(file));
	}
	/**
	 * Save trace list to a file
	 *
	 * @param traceList trace list
	 * @param file the file
	 * @throws IOException
	 */
	public static void writeTo(TraceList traceList,File file) throws IOException{
		String fileName=file.getName();
		String formatName=fileName.contains(".")?fileName.substring(fileName.indexOf('.')+1):"png";
		for(TraceListFormat next:ServiceLoader.load(TraceListFormat.class)){
			if(formatName.endsWith(next.getSuffix())){
				next.write(traceList,file);
				return;
			}
		}
		ImageIO.write(TraceListViewer.renderColorImage(traceList),formatName,file);
	}
}
