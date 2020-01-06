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
/**
 * Tool to visualize stroke extraction from image
 *
 * @author Chan Chung Kwong
 */
public class StrokeInspector2 extends JSplitPane implements ActionListener{
	private static final File IMAGES=new File(Crohme.DIRECTORY_2019+"/../Task2_offlineRec/MainTask_formula/Images_Test2019");
	private final JFileChooser fileChooser=new JFileChooser();
	private final TraceListViewer preview=new TraceListViewer();
	private final JLabel thinViewer=new JLabel();
	private final JLabel graphViewer=new JLabel();
	private final TraceListViewer traceViewer=new TraceListViewer();
	private final JTextArea result=new JTextArea();
	private Ink ink;
	public StrokeInspector2(){
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
		setRightComponent(new JScrollPane(box));
		fileChooser.setCurrentDirectory(new File(Crohme.DIRECTORY_2019+"/../Task2_offlineRec/MainTask_formula/Inkmls_Test2019"));
		fileChooser.addActionListener(this);
		setDividerLocation(400);
	}
	@Override
	public void actionPerformed(ActionEvent e){
		try{
			BufferedImage image;
			ink=new Ink(fileChooser.getSelectedFile());
			preview.setTraceList(ink.getTraceList(),ink.getAnnotions());
			image=ImageIO.read(new File(IMAGES,fileChooser.getSelectedFile().getName().replace(".inkml",".png")));
			//image=new AffineTransformOp(AffineTransform.getScaleInstance(3,3),AffineTransformOp.TYPE_BILINEAR).filter(image,null);
			image=Extractor.getDefault().getPreprocessor().apply(image,true);
			result.setText(ink.getMeta().entrySet().stream().map((attr)->attr.getKey()+"\t"+attr.getValue()).collect(Collectors.joining("\n")));
			//thinViewer.setIcon(new ImageIcon(new Thinning().apply(image,false)));
			Graph<Junction,Segment> graph=ThinTracer.buildRawGraph(new Bitmap(image));
			//System.out.println(graph);
			ThinTracer.simplifyGraph(graph);
			//graphViewer.setIcon(new ImageIcon(Graph.visualize(graph,image.getWidth(),image.getHeight())));
			//System.out.println(Graph.toString(graph));
			//System.out.println(graph);
			TraceList extracted=Extractor.getDefault().getGraphTracer().trace(graph);
			extracted=new CutOrderer().order(extracted);
			traceViewer.setTraceList(extracted.rescale(ink.getTraceList().getBoundBox()),Collections.emptyList());
		}catch(Exception ex){
			Logger.getLogger(StrokeInspector.class.getName()).log(Level.SEVERE,null,ex);
		}
	}
	public static void main(String[] args){
		JFrame f=new JFrame();
		f.getContentPane().add(new StrokeInspector2(),BorderLayout.CENTER);
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
