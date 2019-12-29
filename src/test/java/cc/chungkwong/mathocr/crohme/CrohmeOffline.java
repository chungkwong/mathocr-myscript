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
import cc.chungkwong.mathocr.common.*;
import cc.chungkwong.mathocr.common.format.*;
import cc.chungkwong.mathocr.ui.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javax.imageio.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class CrohmeOffline{
	/**
	 *
	 * @return all expressions in CROHME
	 */
	public static Stream<Pair<Ink,BufferedImage>> getFullStream(){
		return Stream.of(getTrainStream2019(),getValidationStream2016(),getTestStream2016(),getTestStream2019()).flatMap((s)->s);
	}
	/**
	 *
	 * @return all expressions in the train set of CROHME 2016/2014
	 */
	public static Stream<Pair<Ink,BufferedImage>> getTrainStream2016(){
		return getStream(Crohme.getTrainStream2016(),new File(Crohme.DIRECTORY_2019,
				"Task1_and_Task2/Task2_offlineRec/MainTask_formula/Train/IMGS/data_png_Train_2014"));
	}
	/**
	 *
	 * @return all expressions in the validation set of CROHME 2016/2019(Test
	 * set of CROHME 2014)
	 */
	public static Stream<Pair<Ink,BufferedImage>> getValidationStream2016(){
		return getStream(Crohme.getValidationStream2016(),new File(Crohme.DIRECTORY_2019,
				"Task1_and_Task2/Task2_offlineRec/MainTask_formula/valid/data_png_TestEM2014GT_INKMLs"));
	}
	/**
	 *
	 * @return all expressions in the validation set of CROHME 2014(Test set of
	 * CROHME 2013)
	 */
	public static Stream<Pair<Ink,BufferedImage>> getValidationStream2014(){
		return getStream(Crohme.getValidationStream2014(),new File(Crohme.DIRECTORY_2019,
				"Task1_and_Task2/Task2_offlineRec/MainTask_formula/Train/IMGS/data_png_TestINKMLGT_2013"));
	}
	/**
	 *
	 * @return all expressions in the test set of CROHME 2016
	 */
	public static Stream<Pair<Ink,BufferedImage>> getTestStream2016(){
		return getStream(Crohme.getTestStream2016(),new File(Crohme.DIRECTORY_2019,
				"Task1_and_Task2/Task2_offlineRec/MainTask_formula/valid/data_png_TEST2016_INKML_GT"));
	}
	/**
	 *
	 * @return all expressions in the train set of CROHME 2019
	 */
	public static Stream<Pair<Ink,BufferedImage>> getTrainStream2019(){
		return getStream(Crohme.getTrainStream2019(),new File(Crohme.DIRECTORY_2019,
				"Task1_and_Task2/Task2_offlineRec/MainTask_formula/Train/IMGS/data_png_TestINKMLGT_2012"),new File(Crohme.DIRECTORY_2019,
				"Task1_and_Task2/Task2_offlineRec/MainTask_formula/Train/IMGS/data_png_TestINKMLGT_2013"),new File(Crohme.DIRECTORY_2019,
				"Task1_and_Task2/Task2_offlineRec/MainTask_formula/Train/IMGS/data_png_Train_2014"));
	}
	/**
	 *
	 * @return all expressions in the test set of CROHME 2019
	 */
	public static Stream<Pair<Ink,BufferedImage>> getTestStream2019(){
		return getStream(Crohme.getTestStream2019(),new File(Crohme.DIRECTORY_2019,
				"../Task1_onlineRec/MainTask_formula/imgdata_png_TestSet2019"));
	}
	private static Stream<Pair<Ink,BufferedImage>> getStream(Stream<Ink> inks,File... imageDirectory){
		if(imageDirectory.length==0){
			return inks.map((ink)->new Pair<>(ink,TraceListViewer.renderImage(ink.getTraceList().rescale(CONTENT_BOX),PADDED_BOX,RENDER_THICK)));
		}
		return inks.map((ink)->{
			String name=ink.getFile().getName().replaceFirst(".inkml",".png");
			File imageFile=Arrays.stream(imageDirectory).map((dir)->new File(dir,name)).filter(File::exists).findFirst().get();
			try{
				return new Pair<>(ink,ImageIO.read(imageFile));
			}catch(IOException ex){
				Logger.getLogger(CrohmeOffline.class.getName()).log(Level.SEVERE,null,ex);
				return null;
			}
		}).filter((p)->p!=null);
	}
	/**
	 *
	 * @return all expressions in CROHME
	 */
	public static Stream<BufferedImage> getFullStreamImage(){
		return Stream.of(getValidationStream2016Image(),getTestStream2016Image(),getTestStream2019Image()).flatMap((s)->s);
	}
	/**
	 *
	 * @return all expressions in the train set of CROHME 2016/2014
	 */
	public static Stream<BufferedImage> getTrainStream2016Image(){
		return getStream(new File(Crohme.DIRECTORY_2019,
				"Task1_and_Task2/Task2_offlineRec/MainTask_formula/Train/IMGS/data_png_Train_2014"));
	}
	/**
	 *
	 * @return all expressions in the validation set of CROHME 2016/2019(Test
	 * set of CROHME 2014)
	 */
	public static Stream<BufferedImage> getValidationStream2016Image(){
		return getStream(new File(Crohme.DIRECTORY_2019,
				"Task1_and_Task2/Task2_offlineRec/MainTask_formula/valid/data_png_TestEM2014GT_INKMLs"));
	}
	/**
	 *
	 * @return all expressions in the validation set of CROHME 2014(Test set of
	 * CROHME 2013)
	 */
	public static Stream<BufferedImage> getValidationStream2014Image(){
		return getStream(new File(Crohme.DIRECTORY_2019,
				"Task1_and_Task2/Task2_offlineRec/MainTask_formula/Train/IMGS/data_png_TestINKMLGT_2013"));
	}
	/**
	 *
	 * @return all expressions in the test set of CROHME 2016
	 */
	public static Stream<BufferedImage> getTestStream2016Image(){
		return getStream(new File(Crohme.DIRECTORY_2019,
				"Task1_and_Task2/Task2_offlineRec/MainTask_formula/valid/data_png_TEST2016_INKML_GT"));
	}
	/**
	 *
	 * @return all expressions in the train set of CROHME 2019
	 */
	public static Stream<BufferedImage> getTrainStream2019Image(){
		return getStream(new File(Crohme.DIRECTORY_2019,
				"Task1_and_Task2/Task2_offlineRec/MainTask_formula/Train/IMGS/data_png_TestINKMLGT_2012"),new File(Crohme.DIRECTORY_2019,
				"Task1_and_Task2/Task2_offlineRec/MainTask_formula/Train/IMGS/data_png_TestINKMLGT_2013"),new File(Crohme.DIRECTORY_2019,
				"Task1_and_Task2/Task2_offlineRec/MainTask_formula/Train/IMGS/data_png_Train_2014"));
	}
	/**
	 *
	 * @return all expressions in the test set of CROHME 2019
	 */
	public static Stream<BufferedImage> getTestStream2019Image(){
		return getStream(new File(Crohme.DIRECTORY_2019,
				"../Task1_onlineRec/MainTask_formula/imgdata_png_TestSet2019"));
	}
	private static Stream<BufferedImage> getStream(File... imageDirectory){
		Path[] paths=Arrays.stream(imageDirectory).flatMap((d)->{
			try{
				return Files.list(d.toPath());
			}catch(IOException ex){
				Logger.getLogger(OrdererTests.class.getName()).log(Level.SEVERE,null,ex);
				return Stream.empty();
			}
		}).toArray(Path[]::new);
		return Arrays.stream(paths).map((p)->{
			try{
				return ImageIO.read(p.toFile());
			}catch(IOException ex){
				Logger.getLogger(CrohmeOffline.class.getName()).log(Level.SEVERE,null,ex);
				return null;
			}
		}).filter((p)->p!=null);
	}
	private static final int RENDER_WIDTH=1000, RENDER_HEIGHT=1000;
	private static final int RENDER_PADDING=5;
	private static final int RENDER_THICK=1;
	public static final BoundBox CONTENT_BOX=new BoundBox(RENDER_PADDING,RENDER_WIDTH+RENDER_PADDING-1,RENDER_PADDING,RENDER_HEIGHT+RENDER_PADDING-1);
	public static final BoundBox PADDED_BOX=new BoundBox(0,RENDER_WIDTH+RENDER_PADDING*2-1,0,RENDER_HEIGHT+RENDER_PADDING*2-1);
}
