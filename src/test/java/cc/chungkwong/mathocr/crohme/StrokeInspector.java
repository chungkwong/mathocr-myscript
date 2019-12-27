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
import cc.chungkwong.mathocr.common.format.*;
import cc.chungkwong.mathocr.offline.*;
import cc.chungkwong.mathocr.offline.extractor.*;
import cc.chungkwong.mathocr.offline.extractor.orderer.*;
import cc.chungkwong.mathocr.offline.extractor.tracer.*;
import cc.chungkwong.mathocr.offline.preprocessor.*;
import cc.chungkwong.mathocr.online.*;
import cc.chungkwong.mathocr.ui.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javax.imageio.*;
import javax.swing.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
/**
 * Tool to visualize stroke extraction
 *
 * @author Chan Chung Kwong
 */
public class StrokeInspector extends JSplitPane implements ActionListener{
	private final JFileChooser fileChooser=new JFileChooser();
	private final TraceListViewer preview=new TraceListViewer();
	private final JLabel thinViewer=new JLabel();
	private final JLabel graphViewer=new JLabel();
	private final TraceListViewer traceViewer=new TraceListViewer();
	private final JTextArea result=new JTextArea();
	private final JCheckBox details=new JCheckBox("Details",false);
	private cc.chungkwong.mathocr.common.format.Ink ink;
	/**
	 * Create a interactive inspector
	 */
	public StrokeInspector(){
		super(JSplitPane.HORIZONTAL_SPLIT);
		setLeftComponent(fileChooser);
		Box box=Box.createVerticalBox();
		result.setAlignmentX(0);
		thinViewer.setAlignmentX(0);
		graphViewer.setAlignmentX(0);
		traceViewer.setAlignmentX(0);
		preview.setAlignmentX(0);
		box.add(preview);
		box.add(thinViewer);
		box.add(graphViewer);
		box.add(traceViewer);
		box.add(result);
		box.add(details);
		setRightComponent(new JScrollPane(box));
		fileChooser.setCurrentDirectory(new File(Crohme.DIRECTORY_2016+"/CROHME2016_data/Task-1-Formula/TEST2016_INKML_GT"));
		fileChooser.addActionListener(this);
		setDividerLocation(400);
	}
	@Override
	public void actionPerformed(ActionEvent e){
		try{
			BufferedImage image;
			if(fileChooser.getSelectedFile().getName().endsWith(".inkml")){
				ink=new cc.chungkwong.mathocr.common.format.Ink(fileChooser.getSelectedFile());
				preview.setTraceList(ink.getTraceList(),ink.getAnnotions());
				image=TraceListViewer.renderImage(ink.getTraceList());
				result.setText(ink.getMeta().entrySet().stream().map((attr)->attr.getKey()+"\t"+attr.getValue()).collect(Collectors.joining("\n")));
			}else if(fileChooser.getSelectedFile().getName().endsWith(".json")){
				preview.setTraceList(new JsonFormat().read(fileChooser.getSelectedFile()),Collections.emptyList());
				image=TraceListViewer.renderImage(preview.getTraceList());
			}else if(fileChooser.getSelectedFile().getName().endsWith(".ascii")){
				TraceList traceList=new AsciiFormat().read(fileChooser.getSelectedFile());
				preview.setTraceList(traceList,Collections.emptyList());
				image=TraceListViewer.renderImage(traceList);
			}else{
				image=ImageIO.read(fileChooser.getSelectedFile());
				image=Extractor.DEFAULT.getPreprocessor().apply(image,true);
				preview.setImage(image);
			}
			thinViewer.setIcon(new ImageIcon(new Thinning().apply(image,false)));
			Bitmap bitmap=new Bitmap(image);
			Graph<Junction,Segment> graph=ThinTracer.buildRawGraph(bitmap);
			//System.out.println(graph);
			ThinTracer.simplifyGraph(graph);
			graphViewer.setIcon(new ImageIcon(Graph.visualize(graph,bitmap.getWidth(),bitmap.getHeight())));
			if(details.isSelected()){
				System.out.println(Graph.toString(graph));
			}
			//System.out.println(graph);
			TraceList traceList=Extractor.DEFAULT.getGraphTracer().trace(graph);
			traceList=new CutOrderer().order(traceList);
			traceViewer.setTraceList(traceList,Collections.emptyList());
		}catch(IOException|ParserConfigurationException|SAXException ex){
			Logger.getLogger(StrokeInspector.class.getName()).log(Level.SEVERE,null,ex);
		}
	}
	public static void main(String[] args){
		JFrame f=new JFrame();
		f.getContentPane().add(new StrokeInspector(),BorderLayout.CENTER);
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
