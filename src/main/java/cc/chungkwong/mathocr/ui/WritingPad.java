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
import cc.chungkwong.mathocr.online.Trace;
import cc.chungkwong.mathocr.online.TraceList;
import cc.chungkwong.mathocr.online.TracePoint;
import cc.chungkwong.mathocr.common.BoundBox;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class WritingPad extends JLabel implements MouseListener,MouseMotionListener{
	private TraceList traceList=new TraceList();
	private Trace trace;
	private final LinkedList<ActionListener> listeners=new LinkedList<>();
	public WritingPad(){
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	public void addActionListener(ActionListener listener){
		listeners.add(listener);
	}
	public void removeActionListener(ActionListener listener){
		listeners.remove(listener);
	}
	public void clear(){
		traceList=new TraceList();
		trace=null;
		repaint();
	}
	public TraceList getTraceList(){
		return traceList;
	}
	@Override
	public void paint(Graphics g){
		super.paint(g);
		Graphics2D g2d=(Graphics2D)g;
		TraceListViewer.renderColorImage(traceList,new BoundBox(0,getWidth(),0,getHeight()),g2d);
		if(trace!=null){
			g2d.setColor(Color.YELLOW);
			g2d.draw(TraceListViewer.toPath2D(trace));
		}
	}
	@Override
	public void mouseClicked(MouseEvent e){
	}
	@Override
	public void mousePressed(MouseEvent e){
		trace=new Trace();
		trace.getPoints().add(new TracePoint(e.getX(),e.getY()));
	}
	@Override
	public void mouseReleased(MouseEvent e){
		if(trace!=null){
			traceList.getTraces().add(trace);
			trace=null;
			repaint();
		}
		ActionEvent event=new ActionEvent(this,0,"");
		listeners.forEach((listener)->{
			listener.actionPerformed(event);
		});
	}
	@Override
	public void mouseEntered(MouseEvent e){
	}
	@Override
	public void mouseExited(MouseEvent e){
	}
	@Override
	public void mouseDragged(MouseEvent e){
		trace.getPoints().add(new TracePoint(e.getX(),e.getY()));
		repaint();
	}
	@Override
	public void mouseMoved(MouseEvent e){
	}
}
