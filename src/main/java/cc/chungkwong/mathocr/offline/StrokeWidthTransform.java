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
package cc.chungkwong.mathocr.offline;
import cc.chungkwong.mathocr.offline.preprocessor.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;
import javax.swing.*;
/**
 * Stroke width transformation
 *
 * @author Chan Chung Kwong
 */
public class StrokeWidthTransform{
	public static final byte HORIZONTAL=1, VERTICAL=2, THROW=4, PRESS=8;
	/**
	 * Transform a bitmap image
	 *
	 * @param bitmap to be transform
	 * @return stroke width space
	 */
	public static StrokeSpace transform(Bitmap bitmap){
		int width=bitmap.getWidth();
		int height=bitmap.getHeight();
		byte[] pixels=bitmap.getData();
		short[] thicknessH=new short[width*height];
		short[] thicknessS=new short[width*height];
		byte[] direction=new byte[width*height];
		int[] nC=new int[width];
		int[] nwC=new int[width+1];
		int[] neC=new int[width+1];
		for(int i=0, ind=0;i<height;i++){
			int wC=0;
			int nwCp=0;
			for(int j=0;j<width;j++,ind++){
				if(pixels[ind]==0){
					++wC;
					++nC[j];
					int tmp=nwC[j+1];
					nwC[j+1]=nwCp+1;
					nwCp=tmp;
					neC[j]=neC[j+1]+1;
				}else{
					if(wC>0){
						int t=wC;
						for(int k=1, ind0=ind-1;k<=t;k++,ind0--){
							if((direction[ind0]&3)==0||t<thicknessH[ind0]){
								direction[ind0]=(byte)((direction[ind0]&~HORIZONTAL)|VERTICAL);
								thicknessH[ind0]=(short)t;
							}
						}
						wC=0;
					}
					if(nC[j]>0){
						int t=nC[j];
						for(int k=1, ind0=ind-width;k<=nC[j];k++,ind0-=width){
							if((direction[ind0]&3)==0||t<thicknessH[ind0]){
								direction[ind0]=(byte)((direction[ind0]&~VERTICAL)|HORIZONTAL);
								thicknessH[ind0]=(short)t;
							}
						}
						nC[j]=0;
					}
					if(nwCp>0){
						int t=nwCp;
						int d=width+1;
						for(int k=1, ind0=ind-d;k<=t;k++,ind0-=d){
							if((direction[ind0]&12)==0||t<thicknessS[ind0]){
								direction[ind0]=(byte)((direction[ind0]&~PRESS)|THROW);
								thicknessS[ind0]=(short)t;
							}
						}
					}
					nwCp=nwC[j+1];
					nwC[j+1]=0;
					if(neC[j+1]>0){
						int t=neC[j+1];
						int d=width-1;
						for(int k=1, ind0=ind-d;k<=t;k++,ind0-=d){
							if((direction[ind0]&12)==0||t<thicknessS[ind0]){
								direction[ind0]=(byte)((direction[ind0]&~THROW)|PRESS);
								thicknessS[ind0]=(short)t;
							}
						}
					}
					neC[j]=0;
				}
			}
		}
		return new StrokeSpace(direction,thicknessH,thicknessS,width,height);
	}
	/**
	 * Visualize stroke direction in colored image
	 *
	 * @param image input image
	 * @return colored image
	 */
	public static BufferedImage visualizeStrokes(BufferedImage image){
		image=new ToGrayscale().apply(image,true);
		image=new SauvolaBinarizer().apply(image,true);
		StrokeSpace strokes=transform(new Bitmap(image));
		return visualize(strokes);
	}
	/**
	 * Visualize stroke direction in colored image
	 *
	 * @param strokes space of stroke width
	 * @return colored image
	 */
	public static BufferedImage visualize(StrokeSpace strokes){
		byte[] direction=strokes.getDirection();
		int h=strokes.getHeight();
		int w=strokes.getWidth();
		BufferedImage colered=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
		int[] dat=new int[w*h];
		Arrays.fill(dat,0xFFFFFFFF);
		for(int i=0;i<direction.length;i++){
			if((direction[i]&HORIZONTAL)!=0){
				dat[i]=0xFF000000;
			}else if((direction[i]&VERTICAL)!=0){
				dat[i]=0xFF0000FF;
			}
			if((direction[i]&THROW)!=0){
				dat[i]|=0xFFFF0000;
			}else if((direction[i]&PRESS)!=0){
				dat[i]|=0xFF00FF00;
			}
		}
		colered.setRGB(0,0,w,h,dat,0,w);
		return colered;
	}
	/**
	 * Space of stroke width
	 */
	public static class StrokeSpace{
		private final byte[] direction;
		private final short[] thicknessH;
		private final short[] thicknessS;
		private final int width, height;
		/**
		 * Create a space of stroke width
		 *
		 * @param direction direction(bit or)
		 * @param thicknessH thickness in horizontal or vertical direction
		 * @param thicknessS thickness in throwing or pressing direction
		 * @param width width of the image
		 * @param height height of the image
		 */
		public StrokeSpace(byte[] direction,short[] thicknessH,short[] thicknessS,int width,int height){
			this.direction=direction;
			this.thicknessH=thicknessH;
			this.thicknessS=thicknessS;
			this.width=width;
			this.height=height;
		}
		/**
		 *
		 * @return direction(bit or)
		 */
		public byte[] getDirection(){
			return direction;
		}
		/**
		 *
		 * @return thickness in horizontal or vertical direction
		 */
		public short[] getThicknessH(){
			return thicknessH;
		}
		/**
		 *
		 * @return thickness in throwing or pressing direction
		 */
		public short[] getThicknessS(){
			return thicknessS;
		}
		/**
		 *
		 * @return width of the image
		 */
		public int getWidth(){
			return width;
		}
		/**
		 *
		 * @return height of the image
		 */
		public int getHeight(){
			return height;
		}
	}
	public static void main(String[] args){
		JFileChooser fileChooser=new JFileChooser();
		JLabel preview=new JLabel();
		//JCheckBox skel=new JCheckBox("Skeleton");
		JSplitPane pane=new JSplitPane();
		pane.setLeftComponent(fileChooser);
		Box box=Box.createVerticalBox();
		box.add(preview);
		pane.setRightComponent(new JScrollPane(box));
		fileChooser.addActionListener((e)->{
			try{
				BufferedImage input=visualizeStrokes(ImageIO.read(fileChooser.getSelectedFile()));
				//BufferedImage input=SKEW4.visualizeStrokes(ImageIO.read(fileChooser.getSelectedFile()),skel.isSelected());
				//BufferedImage input=REGULAR8.visualizeStrokes(ImageIO.read(fileChooser.getSelectedFile()),skel.isSelected());
				preview.setIcon(new ImageIcon(input));
			}catch(IOException ex){
				Logger.getLogger(StrokeWidthTransform.class.getName()).log(Level.SEVERE,null,ex);
			}
		});
		pane.setDividerLocation(400);
		JFrame f=new JFrame();
		f.getContentPane().add(pane,BorderLayout.CENTER);
		//f.getContentPane().add(skel,BorderLayout.SOUTH);
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
