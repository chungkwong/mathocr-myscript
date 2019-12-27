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
import cc.chungkwong.mathocr.common.*;
import cc.chungkwong.mathocr.online.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javax.xml.parsers.*;
import org.w3c.dom.Node;
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
	private Expression expression;
	private NodeList math;
	private Map<String,List<Trace>> groups;
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
		Map<String,Trace> paths=new LinkedHashMap<>();
		NodeList annotations=doc.getElementsByTagName("annotation");
		for(int i=0;i<annotations.getLength();i++){
			Node item=annotations.item(i);
			if(item.getParentNode().getNodeName().equals("ink")){
				String key=item.getAttributes().getNamedItem("type").getNodeValue();
				String value=item.getTextContent();
				meta.put(key,value);
			}
		}
		groups=new HashMap<>();
		NodeList traces=doc.getElementsByTagName("trace");
		boolean small=meta.get("UI").startsWith("2011_IVC_")||meta.get("UI").startsWith("2012_IVC_")||meta.get("UI").startsWith("2013_IVC_CROHME_F0");
		for(int i=0;i<traces.getLength();i++){
			Node item=traces.item(i);
			Trace trace=new Trace(item.getAttributes().getNamedItem("id").getNodeValue());
			String[] points=item.getTextContent().trim().split(",");
			if(small){
				for(String point:points){
					String[] pair=point.trim().split(" ");
					int x=(int)(Double.parseDouble(pair[0])*1000+0.5);
					int y=(int)(Double.parseDouble(pair[1])*1000+0.5);
					trace.getPoints().add(new TracePoint(x,y));
				}
			}else{
				for(String point:points){
					String[] pair=point.trim().split(" ");
					int x=(int)(Double.parseDouble(pair[0])+0.5);
					int y=(int)(Double.parseDouble(pair[1])+0.5);
					trace.getPoints().add(new TracePoint(x,y));
				}
			}
			paths.put(trace.getId(),trace);
		}
		for(int i=0;i<annotations.getLength();i++){
			Node item=annotations.item(i);
			if(item.getParentNode().getNodeName().equals("traceGroup")){
				String group=((Element)item.getParentNode()).getAttribute("xml:id");
				String tip=item.getTextContent();
//				String group=item.getParentNode().getAttributes().getNamedItem("xml:id").getNodeValue();
				int left=Integer.MAX_VALUE, top=Integer.MAX_VALUE, right=0, bottom=0;
				List<Trace> refs=new ArrayList<>(1);
				String truth=((Element)item).getTextContent().trim();
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
							refs.add(paths.get(ref));
						}else{
							Logger.getGlobal().log(Level.FINE,"Illegal reference:{0} in {1}",new Object[]{ref,file});
						}
					}else if(item.getNodeName().equals("annotationXML")){
						group=((Element)item).getAttribute("href");
					}
				}
				if(left!=Integer.MAX_VALUE){
					annotions.add(new Pair<>(new BoundBox(left,right,top,bottom),tip));
					groups.put(group,refs);
					meta.put(group,truth);
				}
			}
		}
		math=doc.getElementsByTagName("math");
		this.traceList=new TraceList(new ArrayList<>(paths.values()));
	}
	private Expression decode(Element element,Map<String,List<Trace>> traces){
		List<Element> children=new ArrayList<>();
		NodeList childNodes=element.getChildNodes();
		for(int i=0;i<childNodes.getLength();i++){
			if(childNodes.item(i).getNodeType()==Node.ELEMENT_NODE){
				children.add((Element)childNodes.item(i));
			}
		}
		switch(element.getTagName()){
			case "mi":
			case "mn":
			case "mo":
				String name;
				String ref=element.getAttribute("xml:id");
				if(ref!=null){
					name=meta.get(ref);
					if(name==null){
						System.err.println(file.getName()+"Missing reference to xml:id "+ref);
						name=element.getTextContent().trim();
					}
				}else{
					System.err.println("Missing xml:id");
					name=element.getTextContent().trim();
				}
				return new Expression.Symbol(name,getReference(element,traces));
			case "mrow":
			case "mtr":
			case "mtd":
			case "mstyle":
			case "math":
				return new Expression.Line(decode(children,traces));
			case "mtable":
				return new Expression.Matrix(decode(children,traces).stream().map((row)->((Expression.Line)row).getSpans()).collect(Collectors.toList()));
			case "mfrac":
				return new Expression.Fraction(decode(children.get(0),traces),decode(children.get(1),traces),getReference(element,traces));
			case "msqrt":
				return new Expression.Radical(null,new Expression.Line(decode(children,traces)),getReference(element,traces));
			case "mroot":
				return new Expression.Radical(decode(children.get(1),traces),decode(children.get(0),traces),getReference(element,traces));
			case "msub":
				return new Expression.Subscript(decode(children.get(0),traces),decode(children.get(1),traces));
			case "msup":
				return new Expression.Superscript(decode(children.get(0),traces),decode(children.get(1),traces));
			case "msubsup":
				return new Expression.Subsuperscript(decode(children.get(0),traces),decode(children.get(1),traces),decode(children.get(2),traces));
			case "munder":
				return new Expression.Under(decode(children.get(0),traces),decode(children.get(1),traces));
			case "mover":
				return new Expression.Over(decode(children.get(0),traces),decode(children.get(1),traces));
			case "munderover":
				return new Expression.UnderOver(decode(children.get(0),traces),decode(children.get(1),traces),decode(children.get(2),traces));
			case "mfenced": {
				String left=element.getAttribute("open");
				String right=element.getAttribute("close");
				if(left==null||left.isEmpty()){
					left="(";
				}
				if(right==null||right.isEmpty()){
					right=")";
				}
				List<Expression> list=decode(children,traces);
				list.add(0,new Expression.Symbol(left));
				list.add(new Expression.Symbol(right));
				return new Expression.Line(list);
			}
			default:
				System.err.println("Unknown:"+element.getTagName());
				return new Expression.Line();
		}
	}
	private List<Trace> getReference(Element element,Map<String,List<Trace>> traces){
		List<Trace> ref=traces.get(element.getAttribute("xml:id"));
		if(ref==null){
			Logger.getGlobal().log(Level.FINE,"Illegal reference:{0} in {1}",new Object[]{ref,file});
		}
		return ref;
	}
	private List<Expression> decode(List<Element> elements,Map<String,List<Trace>> traces){
		List<Expression> spans=new ArrayList<>(elements.size());
		for(Element operand:elements){
			Expression current=decode(operand,traces);
			if(!spans.isEmpty()){
				if(current instanceof Expression.Symbol&&((Expression.Symbol)current).getName().equals("'")){
					System.out.println("Fixed prime");
					spans.set(spans.size()-1,new Expression.Superscript(spans.get(spans.size()-1),current));
					continue;
				}
			}
			spans.add(current);
		}
		return spans;
	}
	/**
	 * Wrap the ground truth of a expression
	 *
	 * @param in InkML file
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public Ink(InputStream in) throws IOException,ParserConfigurationException,SAXException{
		this.file=null;
		Document doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
		Map<String,Trace> paths=new LinkedHashMap<>();
		NodeList annotations=doc.getElementsByTagName("annotation");
		for(int i=0;i<annotations.getLength();i++){
			Node item=annotations.item(i);
			if(item.getParentNode().getNodeName().equals("ink")){
				String key=item.getAttributes().getNamedItem("type").getNodeValue();
				String value=item.getTextContent();
				meta.put(key,value);
			}
		}
		boolean small=meta.get("UI").startsWith("2011_IVC_")||meta.get("UI").startsWith("2012_IVC_")||meta.get("UI").startsWith("2013_IVC_CROHME_F0");
		NodeList traces=doc.getElementsByTagName("trace");
		for(int i=0;i<traces.getLength();i++){
			Node item=traces.item(i);
			Trace trace=new Trace(item.getAttributes().getNamedItem("id").getNodeValue());
			String[] points=item.getTextContent().trim().split(",");
			if(small){
				for(String point:points){
					String[] pair=point.trim().split(" ");
					int x=(int)(Double.parseDouble(pair[0])*500+0.5);
					int y=(int)(Double.parseDouble(pair[1])*500+0.5);
					trace.getPoints().add(new TracePoint(x,y));
				}
			}else{
				for(String point:points){
					String[] pair=point.trim().split(" ");
					int x=(int)(Double.parseDouble(pair[0])+0.5);
					int y=(int)(Double.parseDouble(pair[1])+0.5);
					trace.getPoints().add(new TracePoint(x,y));
				}
			}
			paths.put(trace.getId(),trace);
		}
		for(int i=0;i<annotations.getLength();i++){
			Node item=annotations.item(i);
			if(item.getParentNode().getNodeName().equals("traceGroup")){
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
	public Expression getExpression(){
		if(expression==null){
			if(math.getLength()>0){
				expression=decode((Element)math.item(0),groups);
			}else{
				expression=new Expression.Line();
				Logger.getGlobal().log(Level.FINE,"Missing ground truth:{0}",file);
			}
			groups=null;
			math=null;
		}
		return expression;
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
