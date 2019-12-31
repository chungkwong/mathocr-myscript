/*
 * Copyright (C) 2019 Chan Chung Kwong
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cc.chungkwong.mathocr.ui;
import cc.chungkwong.mathocr.*;
import cc.chungkwong.mathocr.common.format.*;
import cc.chungkwong.mathocr.offline.extractor.*;
import cc.chungkwong.mathocr.online.*;
import cc.chungkwong.mathocr.online.recognizer.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.logging.*;
import java.util.prefs.*;
import java.util.stream.*;
import javax.imageio.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.*;
import javax.swing.filechooser.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class Main extends JFrame{
	private final ResourceBundle bundle=ResourceBundle.getBundle("cc.chungkwong.mathocr.message");
	private final JTabbedPane tabs=new JTabbedPane();
	public Main(){
		setTitle(bundle.getString("MATHOCR_O2O"));
		try{
			setIconImage(ImageIO.read(Main.class.getResourceAsStream("icon.png")));
		}catch(IOException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
		}
		setJMenuBar(createMenuBar());
		getContentPane().add(tabs);
		addDrawingTab();
		addImageFileTab();
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	private JMenuBar createMenuBar(){
		JMenuBar bar=new JMenuBar();
		JMenu recognize=new JMenu(bundle.getString("RECOGNIZE"));
		JMenuItem file=new JMenuItem(bundle.getString("IMAGE_FILE"));
		file.addActionListener((e)->addImageFileTab());
		recognize.add(file);
		JMenuItem clipboard=new JMenuItem(bundle.getString("CLIPBOARD"));
		clipboard.addActionListener((e)->addClipboardTab());
		recognize.add(clipboard);
		JMenuItem drawing=new JMenuItem(bundle.getString("DRAWING"));
		drawing.addActionListener((e)->addDrawingTab());
		recognize.add(drawing);
		bar.add(recognize);
		JMenu help=new JMenu(bundle.getString("HELP"));
		JMenuItem settings=new JMenuItem(bundle.getString("SETTINGS"));
		settings.addActionListener((e)->addSettingsTab());
		help.add(settings);
		help.addSeparator();
		JMenuItem manual=new JMenuItem(bundle.getString("MANUAL"));
		manual.addActionListener((e)->addHelpTab("MANUAL","MANUAL_PATH"));
		help.add(manual);
		JMenuItem license=new JMenuItem(bundle.getString("LICENSE"));
		license.addActionListener((e)->addHelpTab("LICENSE","LICENSE_PATH"));
		help.add(license);
		JMenuItem about=new JMenuItem(bundle.getString("ABOUT"));
		about.addActionListener((e)->addHelpTab("ABOUT","ABOUT_PATH"));
		help.add(about);
		bar.add(help);
		return bar;
	}
	private void addImageFileTab(){
		JFileChooser fileChooser=new JFileChooser();
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image",ImageIO.getReaderFileSuffixes()));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JSON","json"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("InkML","inkml"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("TAP ASCII","ascii"));
		RecognitionPane result=new RecognitionPane();
		fileChooser.addActionListener((e)->{
			File selected=fileChooser.getSelectedFile();
			if(selected!=null){
				try{
					result.setTraceList(TraceListFormat.readFrom(selected));
				}catch(IOException ex){
					Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				}
			}
		});
		JSplitPane pane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,fileChooser,result);
		pane.setOneTouchExpandable(true);
		addTab("IMAGE_FILE",pane);
		result.setResizeWeight(0.5);
		pane.setResizeWeight(0.5);
	}
	private void addClipboardTab(){
		try{
			RecognitionPane result=new RecognitionPane();
			Clipboard clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
			Object image=clipboard.getData(DataFlavor.imageFlavor);
			if(image instanceof BufferedImage){
				result.setTraceList(Extractor.DEFAULT.extract((BufferedImage)image));
			}
			addTab("CLIPBOARD",result);
			result.setResizeWeight(0.5);
		}catch(UnsupportedFlavorException|IOException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
		}
	}
	private void addDrawingTab(){
		JPanel input=new JPanel(new BorderLayout());
		WritingPad pad=new WritingPad();
		input.add(pad,BorderLayout.CENTER);
		Box toolbar=Box.createHorizontalBox();
		JCheckBox reorder=new JCheckBox(bundle.getString("REORDER"),true);
		toolbar.add(reorder);
		JCheckBox extract=new JCheckBox(bundle.getString("EXTRACT"),false);
		toolbar.add(extract);
		JButton undo=new JButton(bundle.getString("UNDO"));
		undo.setEnabled(false);
		toolbar.add(undo);
		JButton clear=new JButton(bundle.getString("CLEAR"));
		toolbar.add(clear);
		input.add(toolbar,BorderLayout.SOUTH);
		RecognitionPane result=new RecognitionPane();
		Runnable refresh=()->{
			TraceList traceList=pad.getTraceList();
			if(extract.isSelected()){
				traceList=Extractor.DEFAULT.extract(TraceListViewer.renderImage(traceList),false);
			}
			if(reorder.isSelected()){
				traceList=Extractor.DEFAULT.getOrderer().order(traceList);
			}
			result.setTraceList(traceList);
			undo.setEnabled(!traceList.getTraces().isEmpty());
		};
		pad.addActionListener((e)->refresh.run());
		reorder.addActionListener((e)->refresh.run());
		extract.addActionListener((e)->refresh.run());
		undo.addActionListener((e)->{
			java.util.List<Trace> traces=pad.getTraceList().getTraces();
			traces.remove(traces.size()-1);
			pad.repaint();
			refresh.run();
		});
		clear.addActionListener((e)->{
			pad.clear();
			refresh.run();
		});
		JSplitPane pane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,input,result);
		pane.setOneTouchExpandable(true);
		addTab("DRAWING",pane);
		result.setResizeWeight(0.5);
		pane.setResizeWeight(0.5);
	}
	private void addSettingsTab(){
		JPanel settings=new JPanel(new BorderLayout());
		JPanel pairs=new JPanel();
		GroupLayout layout=new GroupLayout(pairs);
		pairs.setLayout(layout);
		GroupLayout.SequentialGroup hGroup=layout.createSequentialGroup();
		GroupLayout.SequentialGroup vGroup=layout.createSequentialGroup();
		GroupLayout.ParallelGroup keys=layout.createParallelGroup();
		GroupLayout.ParallelGroup values=layout.createParallelGroup();
		Map<String,JCheckBox> booleanPairs=new HashMap<>();
		Map<String,JSpinner> integerPairs=new HashMap<>();
		Map<String,JSpinner> doublePairs=new HashMap<>();
		Map<String,JTextField> stringPairs=new HashMap<>();
		for(String key:Settings.DEFAULT.getBooleanKeySet()){
			JLabel label=new JLabel(key);
			JCheckBox input=new JCheckBox("",Settings.DEFAULT.getBoolean(key));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
					addComponent(label).addComponent(input));
			keys.addComponent(label);
			values.addComponent(input);
			booleanPairs.put(key,input);
		}
		for(String key:Settings.DEFAULT.getIntegerKeySet()){
			JLabel label=new JLabel(key);
			JSpinner input=new JSpinner(new SpinnerNumberModel((int)Settings.DEFAULT.getInteger(key),0,1000,1));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
					addComponent(label).addComponent(input));
			keys.addComponent(label);
			values.addComponent(input);
			integerPairs.put(key,input);
		}
		for(String key:Settings.DEFAULT.getDoubleKeySet()){
			JLabel label=new JLabel(key);
			JSpinner input=new JSpinner(new SpinnerNumberModel((double)Settings.DEFAULT.getDouble(key),-10,10,0.1));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
					addComponent(label).addComponent(input));
			keys.addComponent(label);
			values.addComponent(input);
			doublePairs.put(key,input);
		}
		for(String key:Settings.DEFAULT.getStringKeySet()){
			JLabel label=new JLabel(key);
			JTextField input=new JTextField(Settings.DEFAULT.getString(key));
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
					addComponent(label).addComponent(input));
			keys.addComponent(label);
			values.addComponent(input);
			stringPairs.put(key,input);
		}
		hGroup.addGroup(keys);
		hGroup.addGroup(values);
		layout.setHorizontalGroup(hGroup);
		layout.setVerticalGroup(vGroup);
		settings.add(pairs,BorderLayout.CENTER);
		Box actions=Box.createHorizontalBox();
		JButton apply=new JButton(bundle.getString("APPLY"));
		apply.addActionListener((e)->{
			for(Map.Entry<String,JCheckBox> entry:booleanPairs.entrySet()){
				Settings.DEFAULT.setBoolean(entry.getKey(),entry.getValue().isSelected());
			}
			for(Map.Entry<String,JSpinner> entry:integerPairs.entrySet()){
				Settings.DEFAULT.setInteger(entry.getKey(),((Number)entry.getValue().getValue()).intValue());
			}
			for(Map.Entry<String,JSpinner> entry:doublePairs.entrySet()){
				Settings.DEFAULT.setDouble(entry.getKey(),((Number)entry.getValue().getValue()).doubleValue());
			}
			for(Map.Entry<String,JTextField> entry:stringPairs.entrySet()){
				Settings.DEFAULT.setString(entry.getKey(),entry.getValue().getText());
			}
		});
		actions.add(apply);
		JButton reset=new JButton(bundle.getString("RESET"));
		reset.addActionListener((e)->{
			try{
				Settings.DEFAULT.clearPreference();
			}catch(BackingStoreException ex){
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
			}
			for(Map.Entry<String,JCheckBox> entry:booleanPairs.entrySet()){
				entry.getValue().setSelected(Settings.DEFAULT.getBoolean(entry.getKey()));
			}
			for(Map.Entry<String,JSpinner> entry:integerPairs.entrySet()){
				entry.getValue().setValue(Settings.DEFAULT.getInteger(entry.getKey()));
			}
			for(Map.Entry<String,JSpinner> entry:doublePairs.entrySet()){
				entry.getValue().setValue(Settings.DEFAULT.getDouble(entry.getKey()));
			}
			for(Map.Entry<String,JTextField> entry:stringPairs.entrySet()){
				entry.getValue().setText(Settings.DEFAULT.getString(entry.getKey()));
			}
		});
		actions.add(reset);
		settings.add(actions,BorderLayout.SOUTH);
		addTab("SETTINGS",settings);
	}
	private void addHelpTab(String title,String path){
		String text=new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream(bundle.getString(path)),StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
		JTextArea area=new JTextArea(text);
		area.setEditable(false);
		addTab(title,new JScrollPane(area));
	}
	private void addTab(String title,JComponent component){
		int index=tabs.getTabCount();
		Box header=Box.createHorizontalBox();
		header.add(new JLabel(bundle.getString(title)));
		JButton close=new JButton("x");
		close.setMargin(new Insets(0,0,0,0));
		close.addActionListener((e)->tabs.remove(component));
		header.add(close);
		tabs.add(component,index);
		tabs.setTabComponentAt(index,header);
		tabs.setSelectedIndex(index);
	}
	private static boolean batchProcess(String[] args){
		boolean processed=false;
		if(args.length>=1){
			int i=0;
			String format="tex";
			if(args[0].startsWith("-")){
				format=args[0].substring(1).toLowerCase();
				++i;
			}
			for(;i<args.length;i++){
				File input=new File(args[i]);
				System.err.println("Processing "+input);
				try{
					TraceList traceList=TraceListFormat.readFrom(input);
					switch(format){
						case "json":
							new JsonFormat().write(traceList,new PrintWriter(System.out));
							break;
						case "ascii":
							new AsciiFormat().write(traceList,new PrintWriter(System.out));
							break;
						case "mathml":
							System.out.println(new MyscriptRecognizer().recognize(traceList).getCodes(new MathmlFormat()));
							break;
						case "tex":
						default:
							System.out.println(new MyscriptRecognizer().recognize(traceList).getCodes(new LatexFormat()));
							break;
					}
					processed=true;
				}catch(IOException ex){
					Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				}
			}
		}
		if(!processed){
			System.err.println("Usage:");
			System.err.println("    java -jar mathocr-myscript.jar [-format] image-file");
			System.err.println("Format available: tex, mathml, json, ascii");
		}
		return processed;
	}
	public static void main(String[] args) throws IOException{
		if(!batchProcess(args)){
			new Main();
		}
	}
}
