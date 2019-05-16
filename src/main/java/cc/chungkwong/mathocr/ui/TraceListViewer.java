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
import cc.chungkwong.mathocr.online.TracePoint;
import cc.chungkwong.mathocr.common.Pair;
import cc.chungkwong.mathocr.online.TraceList;
import cc.chungkwong.mathocr.online.Trace;
import cc.chungkwong.mathocr.common.BoundBox;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class TraceListViewer extends JPanel implements MouseMotionListener,ChangeListener{
	public static final int THICK=3, MARGIN_V=10, MARGIN_H=15;
	private static final int MAXIMUM_TIME=10000;
	private static final Color[] COLORS=new Color[]{Color.RED,Color.GREEN,Color.BLUE};
	private BoundBox boundBox;
	private TraceList traceList;
	private java.util.List<Pair<BoundBox,String>> annotions;
	private final JLabel canvas=new JLabel();
	private final JSpinner zoom=new JSpinner(new SpinnerNumberModel(100,25,400,25));
	private final JSlider time=new JSlider(JSlider.HORIZONTAL,0,MAXIMUM_TIME,MAXIMUM_TIME);
	public TraceListViewer(){
		super(new BorderLayout());
		canvas.addMouseMotionListener(this);
		add(canvas,BorderLayout.CENTER);
		time.addChangeListener(this);
		add(time,BorderLayout.SOUTH);
		zoom.addChangeListener((e)->setTraceList(traceList,annotions));
		add(zoom,BorderLayout.NORTH);
	}
	public void setTraceList(TraceList traceList,java.util.List<Pair<BoundBox,String>> annotions){
		if(traceList==null){
			return;
		}
		this.annotions=annotions;
		this.boundBox=padBoundBox(traceList.getBoundBox());
		this.traceList=traceList;
		setImage(renderColorImage(traceList,boundBox,time.getValue()));
		//setIcon(new ImageIcon(renderBoxedImage(traceList,boundBox)));
	}
	public TraceList getTraceList(){
		return traceList;
	}
	public static BufferedImage renderBoxedImage(TraceList list,BoundBox box){
		BufferedImage image=new BufferedImage(box.getWidth(),box.getHeight(),BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics=(Graphics2D)image.getGraphics();
		graphics.setBackground(Color.WHITE);
		graphics.clearRect(0,0,box.getWidth(),box.getHeight());
		graphics.translate(-box.getLeft(),-box.getTop());
		int count=0;
		for(Trace trace:list.getTraces()){
			BoundBox b=trace.getBoundBox();
			graphics.setColor(Color.BLACK);
			graphics.setStroke(new BasicStroke(THICK,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
			graphics.draw(toPath2D(trace));
			graphics.setStroke(new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
			graphics.setColor(Color.RED);
			graphics.drawRect(b.getLeft(),b.getTop(),b.getWidth(),b.getHeight());
			graphics.drawString(Integer.toString(count++),b.getLeft(),b.getTop()-1);
		}
		try{
			ImageIO.write(image,"PNG",new File("ordering.png"));
		}catch(IOException ex){
			Logger.getLogger(TraceListViewer.class.getName()).log(Level.SEVERE,null,ex);
		}
		return image;
	}
	public static BufferedImage renderColorImage(TraceList traceList){
		return renderColorImage(traceList,padBoundBox(traceList.getBoundBox()),MAXIMUM_TIME);
	}
	public static BufferedImage renderColorImage(TraceList list,BoundBox box,int time){
		BufferedImage image=new BufferedImage(box.getWidth(),box.getHeight(),BufferedImage.TYPE_INT_ARGB);
		renderColorImage(list,box,(Graphics2D)image.getGraphics(),time);
		return image;
	}
	public static void renderColorImage(TraceList list,BoundBox box,Graphics2D graphics){
		renderColorImage(list,box,graphics,MAXIMUM_TIME);
	}
	public static void renderColorImage(TraceList list,BoundBox box,Graphics2D graphics,int time){
		graphics.setBackground(Color.WHITE);
		graphics.clearRect(0,0,box.getWidth(),box.getHeight());
		graphics.translate(-box.getLeft(),-box.getTop());
		graphics.setXORMode(Color.WHITE);
		graphics.setColor(Color.BLACK);
		graphics.setStroke(new BasicStroke(THICK,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
		int color=-1;
		int length=list.getTraces().size()*time/MAXIMUM_TIME;
		for(Trace path:list.getTraces()){
			graphics.setColor(COLORS[(++color)%COLORS.length]);
			if(!path.getPoints().isEmpty()){
				graphics.drawString(Integer.toString(color),path.getStart().getX(),path.getStart().getY());
			}
			if(--length<0){
				try{
					int cut=path.getPoints().size()*(time%(MAXIMUM_TIME/list.getTraces().size()))/(MAXIMUM_TIME/list.getTraces().size());
					cut=Math.min(cut,path.getPoints().size());
					graphics.draw(toPath2D(new Trace(path.getPoints().subList(0,cut))));
				}catch(RuntimeException ex){
					Logger.getGlobal().log(Level.SEVERE,"",ex);
					graphics.draw(toPath2D(path));
				}
				break;
			}
			graphics.draw(toPath2D(path));
		}
	}
	public static BufferedImage renderImage(TraceList traceList){
		return renderImage(traceList,padBoundBox(traceList.getBoundBox()));
	}
	public static BufferedImage renderImage(TraceList list,BoundBox box){
		BufferedImage image=new BufferedImage(box.getWidth(),box.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D graphics=(Graphics2D)image.getGraphics();
		graphics.setBackground(Color.WHITE);
		graphics.clearRect(0,0,box.getWidth(),box.getHeight());
		graphics.translate(-box.getLeft(),-box.getTop());
		graphics.setColor(Color.BLACK);
		graphics.setStroke(new BasicStroke(THICK,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
		list.getTraces().forEach((path)->graphics.draw(toPath2D(path)));
		return image;
	}
	public static Shape toPath2D(Trace trace){
		Path2D.Double path=new Path2D.Double();
		java.util.List<TracePoint> points=trace.getPoints();
		if(!points.isEmpty()){
			Iterator<TracePoint> iterator=points.iterator();
			TracePoint next=iterator.next();
			if(points.size()>1){
				path.moveTo(next.getX(),next.getY());
				while(iterator.hasNext()){
					next=iterator.next();
					path.lineTo(next.getX(),next.getY());
				}
			}else{
				return new Ellipse2D.Double(next.getX()-THICK/2,next.getY()-THICK/2,THICK,THICK);
			}
		}
		return path;
	}
	private static BoundBox padBoundBox(BoundBox boundBox){
		return new BoundBox(boundBox.getLeft()-MARGIN_H,boundBox.getRight()+MARGIN_H,
				boundBox.getTop()-MARGIN_V,boundBox.getBottom()+MARGIN_V);
	}
	@Override
	public void mouseDragged(MouseEvent e){
	}
	@Override
	public void mouseMoved(MouseEvent e){
		if(boundBox!=null){
			int x=e.getX()+boundBox.getLeft();
			int y=e.getY()+boundBox.getTop();
			String remark="";
			double area=Double.MAX_VALUE;
			for(Pair<BoundBox,String> annotion:annotions){
				if(annotion.getKey().contains(x,y)&&(remark.isEmpty()||annotion.getKey().getArea()<area)){
					area=annotion.getKey().getArea();
					remark=annotion.getValue();
				}
			}
			if(remark.isEmpty()){
				remark=x+","+y;
			}
			setToolTipText(remark);
		}
	}
	@Override
	public void stateChanged(ChangeEvent e){
		if(traceList!=null){
			setImage(renderColorImage(traceList,boundBox,time.getValue()));
		}
	}
	public void setImage(BufferedImage image){
		int z=((Number)zoom.getValue()).intValue();
		image=z!=100?new AffineTransformOp(AffineTransform.getScaleInstance(z/100.0,z/100.0),AffineTransformOp.TYPE_BILINEAR).filter(image,null):image;
		canvas.setIcon(new ImageIcon(image));
	}
}
