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
package cc.chungkwong.mathocr.ui;
import cc.chungkwong.mathocr.common.*;
import cc.chungkwong.mathocr.common.format.*;
import cc.chungkwong.mathocr.offline.extractor.*;
import cc.chungkwong.mathocr.offline.extractor.orderer.*;
import cc.chungkwong.mathocr.offline.extractor.tracer.*;
import cc.chungkwong.mathocr.online.*;
import cc.chungkwong.mathocr.online.recognizer.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;
import javax.swing.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class Demo extends JPanel{
	private final ResourceBundle bundle=ResourceBundle.getBundle("cc.chungkwong.mathocr.message");
	private JComboBox<SkeletonTracer> tracers;
	private JComboBox<GraphTracer> graphTracers;
	private JComboBox<Orderer> orderers;
	private JComboBox<OnlineRecognizer> recognizers;
	private JComboBox<Format> formats;
	private JCheckBox realtime;
	private JButton recognize;
	private JButton debug;
	private JButton snapshot;
	private JFileChooser fileChooser;
	private TraceListViewer strokes;
	private WritingPad writingPad;
	private final JTextArea code=new JTextArea();
	private Object input;
	private EncodedExpression output;
	public Demo(){
		super(new BorderLayout());
		JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.setLeftComponent(getInputPanel());
		split.setRightComponent(getOutputPanel());
		add(split,BorderLayout.CENTER);
		add(getSettingPanel(),BorderLayout.SOUTH);
	}
	private JComponent getInputPanel(){
		JTabbedPane panel=new JTabbedPane();
		JPanel filePanel=new JPanel(new BorderLayout());
		fileChooser=new JFileChooser();
		fileChooser.addActionListener((e)->{
			input=fileChooser.getSelectedFile();
			refresh(false);
		});
		filePanel.add(fileChooser,BorderLayout.CENTER);
		panel.addTab(bundle.getString("FILE"),filePanel);
		JPanel writingPanel=new JPanel(new BorderLayout());
		writingPad=new WritingPad();
		writingPad.addActionListener((e)->{
			input=writingPad.getTraceList();
			refresh(false);
		});
		writingPanel.add(writingPad,BorderLayout.CENTER);
		JButton clear=new JButton(bundle.getString("CLEAR"));
		clear.addActionListener((e)->writingPad.clear());
		writingPanel.add(clear,BorderLayout.SOUTH);
		panel.addTab(bundle.getString("HANDWRITTING"),writingPanel);
		return panel;
	}
	private JComponent getOutputPanel(){
		Box pane=Box.createVerticalBox();
		strokes=new TraceListViewer();
		pane.add(strokes);
		pane.add(code);
		Box toolbar=Box.createHorizontalBox();
		debug=new JButton("Debug");
		debug.addActionListener((e)->refresh(true));
		toolbar.add(debug);
		debug=new JButton("Snapshot");
		debug.addActionListener((ActionEvent e)->{
			JFileChooser jfc=new JFileChooser();
			if(jfc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){
				try{
					ImageIO.write(TraceListViewer.renderColorImage(strokes.getTraceList()),"PNG",jfc.getSelectedFile());
				}catch(IOException ex){
					Logger.getLogger(Demo.class.getName()).log(Level.SEVERE,null,ex);
				}
			}
		});
		toolbar.add(debug);
		pane.add(toolbar);
		return new JScrollPane(pane);
	}
	private JComponent getSettingPanel(){
		JPanel settings=new JPanel(new FlowLayout());
		tracers=getServiceChooser(SkeletonTracer.class,Extractor.DEFAULT.getTracer());
		tracers.addActionListener((e)->refresh(true));
		settings.add(tracers);
		graphTracers=getServiceChooser(GraphTracer.class,Extractor.DEFAULT.getGraphTracer());
		graphTracers.addActionListener((e)->refresh(true));
		settings.add(graphTracers);
		orderers=getServiceChooser(Orderer.class,Extractor.DEFAULT.getOrderer());
		orderers.addActionListener((e)->refresh(true));
		settings.add(orderers);
		recognizers=getServiceChooser(OnlineRecognizer.class,Extractor.DEFAULT.getRecognizer());
		recognizers.addActionListener((e)->refresh(true));
		settings.add(recognizers);
		formats=getServiceChooser(Format.class,new LatexFormat());
		formats.addActionListener((e)->reformat());
		settings.add(formats);
		realtime=new JCheckBox(bundle.getString("REALTIME"),false);
		settings.add(realtime);
		recognize=new JButton(bundle.getString("RECOGNIZE"));
		recognize.addActionListener((e)->recognize());
		settings.add(recognize);
		return settings;
	}
	private <S> JComboBox<S> getServiceChooser(Class<S> cls,S def){
		JComboBox<S> box=new JComboBox<>();
		boolean found=def==null;
		box.addItem(null);
		Iterator<S> iterator=ServiceLoader.load(cls).iterator();
		while(iterator.hasNext()){
			S service=iterator.next();
			if(Objects.equals(service,def)){
				found=true;
			}
			box.addItem(service);
		}
		if(!found){
			box.addItem(def);
		}
		box.setSelectedItem(def);
		return box;
	}
	private void refresh(boolean debug){
		SkeletonTracer tracer=(SkeletonTracer)tracers.getSelectedItem();
		GraphTracer graphTracer=(GraphTracer)graphTracers.getSelectedItem();
		Orderer orderer=(Orderer)orderers.getSelectedItem();
		TraceList list;
		if(input instanceof File){
			try{
				BufferedImage image=ImageIO.read((File)input);
				Graph<Junction,Segment> graph=tracer.trace(Extractor.DEFAULT.preprocess(image));
				if(debug){
					System.out.println(Graph.toString(graph));
				}
				list=graphTracer.trace(graph);
			}catch(IOException ex){
				Logger.getLogger(Demo.class.getName()).log(Level.SEVERE,null,ex);
				list=new TraceList();
			}
		}else if(input instanceof TraceList){
			list=(TraceList)input;
			if(tracer!=null){
				Graph<Junction,Segment> graph=tracer.trace(Extractor.DEFAULT.preprocess(TraceListViewer.renderImage(list)));
				if(debug){
					System.out.println(Graph.toString(graph));
				}
				list=graphTracer.trace(graph);
			}
		}else{
			list=new TraceList();
		}
		if(orderer!=null){
			list=orderer.order(list);
		}
		if(!list.getTraces().isEmpty()){
			strokes.setTraceList(list,Collections.emptyList());
			if(realtime.isSelected()){
				recognize();
			}
		}
	}
	private void recognize(){
		OnlineRecognizer recognizer=(OnlineRecognizer)recognizers.getSelectedItem();
		if(recognizer==null){
			return;
		}
		output=recognizer.recognize(strokes.getTraceList());
		reformat();
	}
	private void reformat(){
		if(output!=null){
			code.setText(output.getCodes((Format)formats.getSelectedItem()));
		}
	}
	public static void main(String[] args){
		JFrame f=new JFrame();
		f.getContentPane().add(new Demo());
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
