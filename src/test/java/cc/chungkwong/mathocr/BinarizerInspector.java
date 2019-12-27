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
package cc.chungkwong.mathocr;
import cc.chungkwong.mathocr.offline.preprocessor.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.logging.*;
import javax.imageio.*;
import javax.swing.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class BinarizerInspector extends JFrame{
	private final JTabbedPane tabs=new JTabbedPane();
	public BinarizerInspector() throws HeadlessException{
		setTitle("Binarize - MathOCR");
		setJMenuBar(createMenuBar());
		getContentPane().add(tabs);
		addBinarizeTab();
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	private JMenuBar createMenuBar(){
		JMenuBar bar=new JMenuBar();
		JMenu file=new JMenu("File");
		JMenuItem binarize=new JMenuItem("Binarize");
		binarize.addActionListener((e)->addBinarizeTab());
		file.add(binarize);
		bar.add(file);
		return bar;
	}
	private void addBinarizeTab(){
		JPanel output=new JPanel(new BorderLayout());
		JLabel viewer=new JLabel();
		output.add(new JScrollPane(viewer),BorderLayout.CENTER);
		Box toolbar=Box.createHorizontalBox();
		JButton save=new JButton("Save");
		save.addActionListener((e)->{
			JFileChooser saveFile=new JFileChooser();
			if(saveFile.showSaveDialog(null)==JFileChooser.APPROVE_OPTION){
				BufferedImage image=(BufferedImage)((ImageIcon)viewer.getIcon()).getImage();
				File file=saveFile.getSelectedFile();
				String format=file.getName().contains(".")
						?file.getName().substring(file.getName().lastIndexOf('.')+1).toUpperCase():"PNG";
				try{
					ImageIO.write(image,format,file);
				}catch(IOException ex){
					Logger.getLogger(BinarizerInspector.class.getName()).log(Level.SEVERE,null,ex);
				}
			}
		});
		toolbar.add(save);
		output.add(toolbar,BorderLayout.SOUTH);
		JPanel input=new JPanel(new BorderLayout());
		class MethodChooser extends JPanel{
			JRadioButton fixed=new JRadioButton("Fixed",false);
			JRadioButton otsu=new JRadioButton("Otsu",false);
			JRadioButton sauvola=new JRadioButton("Sauvola",true);
			JTextField sauvolaWeight=new JTextField("0.5");
			JTextField sauvolaWindow=new JTextField("21");
			JTextField fixedThreshold=new JTextField("195");
			public MethodChooser(){
				setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
				ButtonGroup group=new ButtonGroup();
				Box fixedBar=Box.createHorizontalBox();
				fixedBar.add(fixed);
				fixedBar.add(new JLabel("Threshold:"));
				fixedBar.add(fixedThreshold);
				group.add(fixed);
				fixedBar.setAlignmentX(0);
				add(fixedBar);
				group.add(otsu);
				otsu.setAlignmentX(0);
				add(otsu);
				Box sauvolaBar=Box.createHorizontalBox();
				sauvolaBar.add(sauvola);
				sauvolaBar.add(new JLabel("Window size:"));
				sauvolaBar.add(sauvolaWindow);
				sauvolaBar.add(new JLabel("Weight:"));
				sauvolaBar.add(sauvolaWeight);
				group.add(sauvola);
				sauvolaBar.setAlignmentX(0);
				add(sauvolaBar);
			}
			public Preprocessor getBinarizer(){
				if(otsu.isSelected()){
					return new OtsuBinarizer();
				}else if(fixed.isSelected()){
					return new FixedBinarizer(Integer.parseInt(fixedThreshold.getText()));
				}else if(sauvola.isSelected()){
					return new SauvolaBinarizer(Double.parseDouble(sauvolaWeight.getText()),
							Integer.parseInt(sauvolaWindow.getText()));
				}else{
					return new OtsuBinarizer();
				}
			}
		}
		MethodChooser methodChooser=new MethodChooser();
		input.add(methodChooser,BorderLayout.SOUTH);
		JFileChooser fileChooser=new JFileChooser();
		fileChooser.addActionListener((e)->{
			File selected=fileChooser.getSelectedFile();
			try{
				BufferedImage image=ImageIO.read(selected);
				image=new ToGrayscale().apply(image,true);
				image=methodChooser.getBinarizer().apply(image,true);
				viewer.setIcon(new ImageIcon(image));
			}catch(IOException ex){
				Logger.getLogger(BinarizerInspector.class.getName()).log(Level.SEVERE,null,ex);
			}
		});
		input.add(fileChooser,BorderLayout.CENTER);
		JSplitPane pane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,input,output);
		pane.setOneTouchExpandable(true);
		addTab("Binarize",pane);
		pane.setResizeWeight(0.5);
	}
	private void addTab(String title,JComponent component){
		int index=tabs.getTabCount();
		Box header=Box.createHorizontalBox();
		header.add(new JLabel(title));
		JButton close=new JButton("x");
		close.setMargin(new Insets(0,0,0,0));
		close.addActionListener((e)->tabs.remove(component));
		header.add(close);
		tabs.add(component,index);
		tabs.setTabComponentAt(index,header);
		tabs.setSelectedIndex(index);
	}
	public static void main(String[] args){
		new BinarizerInspector();
	}
}
