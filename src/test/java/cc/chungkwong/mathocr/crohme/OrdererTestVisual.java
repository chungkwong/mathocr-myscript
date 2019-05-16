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
import cc.chungkwong.mathocr.offline.extractor.orderer.*;
import cc.chungkwong.mathocr.online.TraceList;
import cc.chungkwong.mathocr.ui.*;
import java.awt.*;
import java.util.*;
import java.util.stream.*;
import javax.swing.*;
/**
 * Tool for manual inspection of ordering
 *
 * @author Chan Chung Kwong
 */
public class OrdererTestVisual extends JPanel{
	private final JLabel label=new JLabel("");
	private final JButton good=new JButton("GOOD");
	private final JButton bad=new JButton("BAD");
	private final TraceListViewer viewer0=new TraceListViewer();
	private final TraceListViewer viewer1=new TraceListViewer();
	private final Iterator<TraceList> iterator;
	private final Orderer orderer;
	private int total=0;
	private int matched=0;
	/**
	 * Create a interactive widget
	 *
	 * @param inks ground truth
	 * @param orderer orderer
	 */
	public OrdererTestVisual(Stream<Ink> inks,Orderer orderer){
		super(new BorderLayout());
		this.orderer=orderer;
		iterator=inks.map(Ink::getTraceList).iterator();
		Box header=Box.createHorizontalBox();
		JSplitPane pane=new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(viewer0),new JScrollPane(viewer1));
		pane.setDividerLocation((int)GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getHeight()/2);
		add(pane,BorderLayout.CENTER);
		good.addActionListener((e)->next(true));
		header.add(good);
		bad.addActionListener((e)->next(false));
		header.add(bad);
		header.add(label);
		add(header,BorderLayout.NORTH);
		next(false);
	}
	private void next(boolean b){
		if(b){
			++matched;
		}
		while(iterator.hasNext()){
			++total;
			TraceList next=iterator.next();
			TraceList ordered=orderer.order(next);
			System.out.println(getStatus());
			if(next.getTraces().equals(ordered.getTraces())){
				++matched;
			}else{
				viewer0.setTraceList(next,Collections.emptyList());
				viewer1.setTraceList(ordered,Collections.emptyList());
				label.setText(getStatus());
				break;
			}
		}
		System.out.println(getStatus());
	}
	private String getStatus(){
		return matched+"/"+total+"="+matched*1.0/total;
	}
	public static void main(String[] args){
		JFrame f=new JFrame();
		f.getContentPane().add(new OrdererTestVisual(Crohme.getValidationStream2016(),new CutOrderer()),BorderLayout.CENTER);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		f.setVisible(true);
	}
}
