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
import cc.chungkwong.mathocr.online.*;
import java.util.*;
import javax.swing.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class RecognitionPane extends JSplitPane{
	private final ResourceBundle bundle=ResourceBundle.getBundle("cc.chungkwong.mathocr.message");
	private final TraceListViewer input=new TraceListViewer();
	private final JCheckBox realtime=new JCheckBox(bundle.getString("REALTIME"),false);
	private final JButton recognize=new JButton(bundle.getString("RECOGNIZE"));
	private final JComboBox<ExpressionFormat> formats=new JComboBox<>();
	private final JTextArea code=new JTextArea();
	private EncodedExpression expression;
	private boolean recognizing=false;
	public RecognitionPane(){
		super(JSplitPane.VERTICAL_SPLIT);
		Box output=Box.createVerticalBox();
		Box toolbar=Box.createHorizontalBox();
		recognize.setEnabled(false);
		recognize.addActionListener((e)->recognize());
		toolbar.add(recognize);
		toolbar.add(realtime);
		//ServiceLoader.load(Format.class).iterator().forEachRemaining((format)->formats.addItem(format));
		formats.addItem(new LatexFormat());
		formats.addItem(new MathmlFormat());
		formats.addActionListener((e)->format());
		toolbar.add(formats);
		output.add(toolbar);
		output.add(code);
		setTopComponent(new JScrollPane(input));
		setBottomComponent(new JScrollPane(output));
		setOneTouchExpandable(true);
	}
	public void setTraceList(TraceList traceList){
		input.setTraceList(traceList,Collections.emptyList());
		recognize.setEnabled(true);
		if(realtime.isSelected()){
			recognize();
		}
	}
	public void recognize(){
		if(recognizing){
			return;
		}
		recognizing=true;
		code.setText("Recognizing...");
		new Thread(()->{
			try{
				expression=Extractor.getDefault().recognize(input.getTraceList(),false);
				SwingUtilities.invokeLater(()->{
					recognizing=false;
					format();
				});
			}catch(RuntimeException ex){
				SwingUtilities.invokeLater(()->{
					recognizing=false;
					code.setText(ex.getLocalizedMessage());
				});
			}
		}).start();
	}
	public void format(){
		if(expression!=null){
			code.setText(expression.getCodes((ExpressionFormat)formats.getSelectedItem()));
		}
	}
}
