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
package cc.chungkwong.mathocr.crohme;
import cc.chungkwong.mathocr.online.TracePoint;
import cc.chungkwong.mathocr.online.TraceList;
import cc.chungkwong.mathocr.online.Trace;
import cc.chungkwong.mathocr.common.Pair;
import cc.chungkwong.mathocr.common.BoundBox;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
/**
 * Ground truth of a expression
 *
 * @author Chan Chung Kwong
 */
public class Ink{
	private final File file;
	private final Map<String,String> meta=new HashMap<>();
	private final TraceList traceList;
	private final List<Pair<BoundBox,String>> annotions=new ArrayList<>();
	/**
	 * Wrap the ground truth of a expression
	 *
	 * @param in InkML file
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public Ink(File in) throws IOException,ParserConfigurationException,SAXException{
		this.file=in;
		Document doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
		Map<String,Trace> paths=new HashMap<>();
		NodeList traces=doc.getElementsByTagName("trace");
		for(int i=0;i<traces.getLength();i++){
			Node item=traces.item(i);
			Trace trace=new Trace(item.getAttributes().getNamedItem("id").getNodeValue());
			String[] points=item.getTextContent().trim().split(",");
			for(String point:points){
				String[] pair=point.trim().split(" ");
				int x=(int)(Double.parseDouble(pair[0])+0.5);
				int y=(int)(Double.parseDouble(pair[1])+0.5);
				trace.getPoints().add(new TracePoint(x,y));
			}
			paths.put(trace.getId(),trace);
		}
		NodeList annotations=doc.getElementsByTagName("annotation");
		for(int i=0;i<annotations.getLength();i++){
			Node item=annotations.item(i);
			if(item.getParentNode().getNodeName().equals("ink")){
				String key=item.getAttributes().getNamedItem("type").getNodeValue();
				String value=item.getTextContent();
				meta.put(key,value);
			}else if(item.getParentNode().getNodeName().equals("traceGroup")){
				String tip=item.getTextContent();
//				String group=item.getParentNode().getAttributes().getNamedItem("xml:id").getNodeValue();
				int left=Integer.MAX_VALUE, top=Integer.MAX_VALUE, right=0, bottom=0;
				while((item=item.getNextSibling())!=null){
					if(item.getNodeName().equals("traceView")){
						String ref=item.getAttributes().getNamedItem("traceDataRef").getNodeValue();
						if(paths.containsKey(ref)){
							BoundBox box=paths.get(ref).getBoundBox();
							if(box.getLeft()<left){
								left=box.getLeft();
							}
							if(box.getTop()<top){
								top=box.getTop();
							}
							if(box.getRight()>right){
								right=box.getRight();
							}
							if(box.getBottom()>bottom){
								bottom=box.getBottom();
							}
						}else{
							Logger.getGlobal().log(Level.FINE,"Illegal reference:{0}",ref);
						}
					}
				}
				if(left!=Integer.MAX_VALUE){
					annotions.add(new Pair<>(new BoundBox(left,right,top,bottom),tip));
				}
			}
		}
		this.traceList=new TraceList(new ArrayList<>(paths.values()));
	}
	/**
	 *
	 * @return the source InkML file
	 */
	public File getFile(){
		return file;
	}
	/**
	 *
	 * @return the metadata
	 */
	public Map<String,String> getMeta(){
		return meta;
	}
	/**
	 *
	 * @return the traces of the expression
	 */
	public TraceList getTraceList(){
		return traceList;
	}
	/**
	 *
	 * @return locations of symbols
	 */
	public List<Pair<BoundBox,String>> getAnnotions(){
		return annotions;
	}
	public String getSymbol(BoundBox box){
		int dist=Integer.MAX_VALUE;
		String best=null;
		for(Pair<BoundBox,String> annotion:annotions){
			int d=Math.abs(annotion.getKey().getLeft()-box.getLeft())
					+Math.abs(annotion.getKey().getRight()-box.getRight())
					+Math.abs(annotion.getKey().getTop()-box.getTop())
					+Math.abs(annotion.getKey().getBottom()-box.getBottom());
			if(d<dist){
				dist=d;
				best=annotion.getValue();
			}
		}
		return best;
	}
}
