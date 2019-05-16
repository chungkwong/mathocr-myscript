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
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class Crohme{
	/**
	 * Location of the recursively unzipped CROHME 2016 package, a `readme.txt`
	 * file should be contained in the directory, it can be downloaded from
	 * http://tc11.cvc.uab.es/datasets/ICFHR-CROHME-2016_1
	 */
	public static final String DIRECTORY_2016="../datasets/TC11_package";//FIXME if you put the package elsewhere
	/**
	 * Location of the recursively CROHME 2019 package, it can be downloaded
	 * from https://www.cs.rit.edu/~crohme2019/downloads/Task1_and_Task2.zip and
	 * than run `Task1_and_Task2/Task2_offlineRec/ImgGenerator`
	 */
	public static final String DIRECTORY_2019="../datasets/crohme2019/Task1_and_Task2";//FIXME if you put the package elsewhere
	public static final String DIRECTORY_RESULT="../datasets/results";//FIXME if you put the recognition result elsewhere
	private static final List<String> CROHME2016_TEST=Arrays.asList(
			DIRECTORY_2016+"/CROHME2016_data/Task-1-Formula/TEST2016_INKML_GT");
	private static final List<String> CROHME2019_TEST=Arrays.asList(
			DIRECTORY_2019+"/../Task1_onlineRec/MainTask_formula/TestSet2019");
	private static final List<String> CROHME2016_VALIDATION=Arrays.asList(
			DIRECTORY_2016+"/CROHME2014_data/TestEM2014GT");
	private static final List<String> CROHME2016_TRAIN=Arrays.asList(
			DIRECTORY_2016+"/CROHME2013_data/TrainINKML/expressmatch",
			DIRECTORY_2016+"/CROHME2013_data/TrainINKML/extension",
			DIRECTORY_2016+"/CROHME2013_data/TrainINKML/HAMEX",
			DIRECTORY_2016+"/CROHME2013_data/TrainINKML/KAIST",
			DIRECTORY_2016+"/CROHME2013_data/TrainINKML/MathBrush",
			DIRECTORY_2016+"/CROHME2013_data/TrainINKML/MfrDB");
	/**
	 *
	 * @return all expressions in CROHME 2016
	 */
	public static Stream<Ink> getFullStream2016(){
		return Stream.of(getTrainStream2016(),getValidationStream2016(),getTestStream2016()).flatMap((s)->s);
	}
	/**
	 *
	 * @return all expressions in the train set of CROHME 2016
	 */
	public static Stream<Ink> getTrainStream2016(){
		return getStream(CROHME2016_TRAIN);
	}
	/**
	 *
	 * @return all expressions in the validation set of CROHME 2016(Test set of
	 * CROHME 2014)
	 */
	public static Stream<Ink> getValidationStream2016(){
		return getStream(CROHME2016_VALIDATION);
	}
	/**
	 *
	 * @return all expressions in the test set of CROHME 2016
	 */
	public static Stream<Ink> getTestStream2016(){
		return getStream(CROHME2016_TEST);
	}
	/**
	 *
	 * @return all expressions in the test set of CROHME 2016
	 */
	public static Stream<Ink> getTestStream2019(){
		return getStream(CROHME2019_TEST);
	}
	private static Stream<Ink> getStream(List<String> directory){
		return directory.stream().flatMap((d)->{
			try{
				return Files.list(new File(d).toPath());
			}catch(IOException ex){
				Logger.getLogger(OrdererTests.class.getName()).log(Level.SEVERE,null,ex);
				return Stream.empty();
			}
		}).filter((p)->p.getFileName().toString().endsWith(".inkml")).
				map((p)->{
					try{
						//System.err.println(p);
						return new Ink(p.toFile());
					}catch(IOException|ParserConfigurationException|SAXException ex){
						Logger.getLogger(TracerTests.class.getName()).log(Level.SEVERE,null,ex);
						return null;
					}
				}).filter((i)->i!=null);
	}
}
