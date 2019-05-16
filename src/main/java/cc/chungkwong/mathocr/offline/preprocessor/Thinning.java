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
package cc.chungkwong.mathocr.offline.preprocessor;
import cc.chungkwong.mathocr.offline.*;
import java.io.*;
import java.util.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class Thinning extends SimplePreprocessor{
	public static void thin(Bitmap bitmap){
		thin(bitmap.getData(),bitmap.getWidth()-2,bitmap.getHeight()-2);
	}
	public static void thin(byte[] foreground,int width,int height){
		byte[] backup=new byte[(height+2)*(width+2)];
		boolean[] ignore=new boolean[height+2];
		boolean[] ignore2=new boolean[height+2];
		Arrays.fill(ignore2,true);
		while(thin(foreground,backup,ignore,ignore2,width,height,true)
				|thin(foreground,backup,ignore,ignore2,width,height,false)){
			boolean[] tmp=ignore2;
			ignore2=ignore;
			ignore=tmp;
			Arrays.fill(ignore2,true);
		}
	}
	private static boolean thin(byte[] pixels,byte[] backup,boolean[] ignore,boolean[] ignore2,
			int width,int height,boolean firstStep){
		System.arraycopy(pixels,0,backup,0,backup.length);
		boolean chanaged=false;
		boolean[] neighbor=new boolean[8];
		for(int i=1, ind=width+2;i<=height;i++){
			if(ignore[i-1]&&ignore[i+1]&&ignore[i]){
				ind+=(width+2);
				continue;
			}
			++ind;
			for(int j=0;j<width;j++,ind++){
				if(pixels[ind]==0){
					neighbor[0]=backup[ind-width-2]==0;
					neighbor[1]=backup[ind-width-1]==0;
					neighbor[2]=backup[ind+1]==0;
					neighbor[3]=backup[ind+width+3]==0;
					neighbor[4]=backup[ind+width+2]==0;
					neighbor[5]=backup[ind+width+1]==0;
					neighbor[6]=backup[ind-1]==0;
					neighbor[7]=backup[ind-width-3]==0;
					boolean toDelete;
					if(firstStep){
						toDelete=isUselessFirst(neighbor);
					}else{
						toDelete=isUselessSecond(neighbor);
					}
					if(toDelete){
						chanaged=true;
						ignore[i]=false;
						ignore2[i]=false;
						pixels[ind]=(byte)0xFF;
					}
				}
			}
			++ind;
		}
		return chanaged;
	}
	private static boolean isUselessFirst(boolean[] p){
		return isNeighborCountUseless(p)&&isComponentCountUseless(p)&&(!p[0]||!p[2]||!p[4])&&(!p[2]||!p[4]||!p[6]);
	}
	private static boolean isUselessSecond(boolean[] p){
		return isNeighborCountUseless(p)&&isComponentCountUseless(p)&&(!p[0]||!p[2]||!p[6])&&(!p[0]||!p[4]||!p[6]);
	}
	private static boolean isNeighborCountUseless(boolean[] p){
		int count=0;
		for(int i=0;i<8;i++){
			if(p[i]){
				++count;
			}
		}
		return count>=2&&count<=6;
	}
	private static boolean isComponentCountUseless(boolean[] p){
		boolean last=p[7];
		int count=0;
		for(int i=0;i<8;i++){
			if(p[i]&&!last){
				++count;
			}
			last=p[i];
		}
		return count==1||(p[4]&&p[6]&&!p[0]&&!p[1]&&!p[2]&&!p[5])||(p[0]&&p[6]&&!p[2]&&!p[3]&&!p[4]&&!p[7]);
	}
	public static void main(String[] args) throws IOException{
//		JFileChooser fileChooser=new JFileChooser();
//		JLabel preview=new JLabel();
//		JSplitPane pane=new JSplitPane();
//		pane.setLeftComponent(fileChooser);
//		Box box=Box.createVerticalBox();
//		box.add(preview);
//		pane.setRightComponent(new JScrollPane(box));
//		fileChooser.addActionListener((e)->{
//			try{
//				BufferedImage image=ImageIO.read(fileChooser.getSelectedFile());
//				image=new ToGrayscale().apply(image,true);
//				image=new SauvolaBinarizer(0.1,128).apply(image,true);
//				image=new Thinning().apply(image,true);
//				preview.setIcon(new ImageIcon(image));
//			}catch(IOException ex){
//				Logger.getLogger(StrokeWidthTransform.class.getName()).log(Level.SEVERE,null,ex);
//			}
//		});
//		pane.setDividerLocation(400);
//		JFrame f=new JFrame();
//		f.getContentPane().add(pane,BorderLayout.CENTER);
//		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
//		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		f.setVisible(true);
	}
	@Override
	public void preprocess(byte[] from,byte[] to,int width,int height){
		byte[] foreground=new byte[((width+2)*(height+2))];
		Arrays.fill(foreground,(byte)0xFF);
		for(int i=0, k=0, ind=width+2;i<height;i++){
			++ind;
			for(int j=0;j<width;j++,ind++,k++){
				if(from[k]==0){
					foreground[ind]=0;
				}
			}
			++ind;
		}
		thin(foreground,width,height);
		//print(foreground,width,height);
		for(int i=0, k=0, ind=width+2;i<height;i++){
			++ind;
			for(int j=0;j<width;j++,ind++,k++){
				to[k]=foreground[ind]==0?0x00:(byte)0xFF;
			}
			++ind;
		}
	}
}
