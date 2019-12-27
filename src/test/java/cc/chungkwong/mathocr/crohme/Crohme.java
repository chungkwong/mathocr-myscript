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
import cc.chungkwong.mathocr.online.*;
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
	/**
	 * Default location of results
	 */
	public static final String DIRECTORY_RESULT="../datasets/result";//FIXME if you put the recognition result elsewhere
	private static final List<String> CROHME2016_TEST=Arrays.asList(
			DIRECTORY_2016+"/CROHME2016_data/Task-1-Formula/TEST2016_INKML_GT");
	private static final List<String> CROHME2019_TEST=Arrays.asList(
			DIRECTORY_2019+"/Task1_and_Task2/Task1_onlineRec/MainTask_formula/Test");
	private static final List<String> CROHME2016_VALIDATION=Arrays.asList(
			DIRECTORY_2019+"/Task1_and_Task2/Task1_onlineRec/MainTask_formula/valid/TestEM2014GT_INKMLs");
	private static final List<String> CROHME2016_TRAIN=Arrays.asList(
			DIRECTORY_2019+"/Task1_and_Task2/Task1_onlineRec/MainTask_formula/Train/INKMLs/Train_2014");
	private static final List<String> CROHME2019_TRAIN=Arrays.asList(
			DIRECTORY_2019+"/Task1_and_Task2/Task1_onlineRec/MainTask_formula/Train/INKMLs/Train_2014",
			DIRECTORY_2019+"/Task1_and_Task2/Task1_onlineRec/MainTask_formula/Train/INKMLs/TestINKMLGT_2013",
			DIRECTORY_2019+"/Task1_and_Task2/Task1_onlineRec/MainTask_formula/Train/INKMLs/TestINKMLGT_2012");
	private static final List<String> CROHME2014_VALIDATION=Arrays.asList(
			DIRECTORY_2019+"/Task1_and_Task2/Task1_onlineRec/MainTask_formula/Train/INKMLs/TestINKMLGT_2013");
	/**
	 *
	 * @return all expressions in CROHME
	 */
	public static Stream<Ink> getFullStream(){
		return Stream.of(getTrainStream2019(),getValidationStream2016(),getTestStream2016(),getTestStream2019()).flatMap((s)->s);
	}
	/**
	 *
	 * @return all expressions in the train set of CROHME 2016/2014
	 */
	public static Stream<Ink> getTrainStream2016(){
		return getStream(CROHME2016_TRAIN);
	}
	/**
	 *
	 * @return all expressions in the validation set of CROHME 2016/2019(Test
	 * set of CROHME 2014)
	 */
	public static Stream<Ink> getValidationStream2016(){
		return getStream(CROHME2016_VALIDATION);
	}
	/**
	 *
	 * @return all expressions in the validation set of CROHME 2014(Test set of
	 * CROHME 2013)
	 */
	public static Stream<Ink> getValidationStream2014(){
		return getStream(CROHME2014_VALIDATION);
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
	 * @return all expressions in the train set of CROHME 2019
	 */
	public static Stream<Ink> getTrainStream2019(){
		return getStream(CROHME2019_TRAIN);
	}
	/**
	 *
	 * @return all expressions in the test set of CROHME 2016
	 */
	public static Stream<Ink> getTestStream2019(){
		return getStream(CROHME2019_TEST);
	}
	private static Stream<Ink> getStream(List<String> directory){
		Path[] paths=directory.stream().flatMap((d)->{
			try{
				return Files.list(new File(d).toPath());
			}catch(IOException ex){
				Logger.getLogger(OrdererTests.class.getName()).log(Level.SEVERE,null,ex);
				return Stream.empty();
			}
		}).filter((p)->p.getFileName().toString().endsWith(".inkml")).toArray(Path[]::new);
		return Arrays.stream(paths).
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
	/**
	 * Data augmentation by generating sub-expressions
	 *
	 * @param ink
	 * @param maxLength
	 * @return
	 */
	public static Stream<Pair<TraceList,Expression>> generateSmallExamples(Ink ink,int maxLength){
		List<Pair<TraceList,Expression>> samples=new ArrayList<>();
		Expression expression=ink.getExpression();
		if(expression!=null){
			collect(expression,maxLength,samples);
		}
		return samples.stream();
	}
	/**
	 * Get the trace list with normalized stroke order
	 *
	 * @param ink
	 * @return
	 */
	public static TraceList normalizeTraceList(Ink ink){
		Expression expression=ink.getExpression();
		if(expression!=null){
			return collect(expression);
		}else{
			return new TraceList();
		}
	}
	private static TraceList collect(Expression expression){
		TraceList list;
		if(expression instanceof Expression.Symbol){
			Expression.Symbol e=(Expression.Symbol)expression;
			list=new TraceList((List<Trace>)e.getPrimitives());
		}else if(expression instanceof Expression.Line){
			Expression.Line e=(Expression.Line)expression;
			List<Expression> spans=e.getSpans();
			list=new TraceList(spans.stream().flatMap((span)->collect(span).getTraces().stream()).collect(Collectors.toList()));
		}else if(expression instanceof Expression.Fraction){
			Expression.Fraction e=(Expression.Fraction)expression;
			list=new TraceList();
			TraceList p=collect(e.getNumerator());
			list.getTraces().addAll(p.getTraces());
			list.getTraces().addAll((List<Trace>)e.getPrimitives());
			p=collect(e.getDenominator());
			list.getTraces().addAll(p.getTraces());
		}else if(expression instanceof Expression.Radical){
			Expression.Radical e=(Expression.Radical)expression;
			list=new TraceList();
			TraceList p;
			if(e.getPower()!=null){
				p=collect(e.getPower());
				list.getTraces().addAll(p.getTraces());
			}
			list.getTraces().addAll((List<Trace>)e.getPrimitives());
			p=collect(e.getRadicand());
			list.getTraces().addAll(p.getTraces());
		}else if(expression instanceof Expression.Subscript){
			Expression.Subscript e=(Expression.Subscript)expression;
			list=new TraceList();
			TraceList p=collect(e.getBase());
			list.getTraces().addAll(p.getTraces());
			p=collect(e.getSubscript());
			list.getTraces().addAll(p.getTraces());
		}else if(expression instanceof Expression.Superscript){
			Expression.Superscript e=(Expression.Superscript)expression;
			list=new TraceList();
			TraceList p=collect(e.getBase());
			list.getTraces().addAll(p.getTraces());
			p=collect(e.getSuperscript());
			list.getTraces().addAll(p.getTraces());
		}else if(expression instanceof Expression.Subsuperscript){
			Expression.Subsuperscript e=(Expression.Subsuperscript)expression;
			list=new TraceList();
			TraceList p=collect(e.getBase());
			list.getTraces().addAll(p.getTraces());
			p=collect(e.getSuperscript());
			list.getTraces().addAll(p.getTraces());
			p=collect(e.getSubscript());
			list.getTraces().addAll(p.getTraces());
		}else if(expression instanceof Expression.Under){
			Expression.Under e=(Expression.Under)expression;
			list=new TraceList();
			TraceList p=collect(e.getContent());
			list.getTraces().addAll(p.getTraces());
			p=collect(e.getUnder());
			list.getTraces().addAll(p.getTraces());
		}else if(expression instanceof Expression.Over){
			Expression.Over e=(Expression.Over)expression;
			list=new TraceList();
			TraceList p=collect(e.getOver());
			list.getTraces().addAll(p.getTraces());
			p=collect(e.getContent());
			list.getTraces().addAll(p.getTraces());
		}else if(expression instanceof Expression.UnderOver){
			Expression.UnderOver e=(Expression.UnderOver)expression;
			list=new TraceList();
			TraceList p=collect(e.getOver());
			list.getTraces().addAll(p.getTraces());
			p=collect(e.getContent());
			list.getTraces().addAll(p.getTraces());
			p=collect(e.getUnder());
			list.getTraces().addAll(p.getTraces());
		}else{
			list=new TraceList(Collections.emptyList());
			Logger.getLogger(Crohme.class.getName()).log(Level.INFO,expression.getClass().getCanonicalName());
		}
		return list;
	}
	private static Pair<TraceList,Integer> collect(Expression expression,int maxLength,List<Pair<TraceList,Expression>> samples){
		int count=0;
		TraceList list;
		if(expression instanceof Expression.Symbol){
			Expression.Symbol e=(Expression.Symbol)expression;
			list=new TraceList((List<Trace>)e.getPrimitives());
			count=1;
		}else if(expression instanceof Expression.Line){
			Expression.Line e=(Expression.Line)expression;
			List<Expression> spans=e.getSpans();
			list=new TraceList();
			List<Pair<TraceList,Integer>> ps=spans.stream().map((span)->collect(span,maxLength,samples)).collect(Collectors.toList());
			for(Pair<TraceList,Integer> p:ps){
				list.getTraces().addAll(p.getKey().getTraces());
				count+=p.getValue();
			}
			for(int i=0;i<spans.size()-1;i++){
				int c=ps.get(i).getValue();
				if(c<maxLength){
					List<Trace> subtraces=new ArrayList<>(maxLength);
					subtraces.addAll(ps.get(i).getKey().getTraces());
					for(int j=i+1;j<spans.size();j++){
						c+=ps.get(j).getValue();
						if(c>maxLength){
							break;
						}
						subtraces.addAll(ps.get(j).getKey().getTraces());
						if(j-i+1<spans.size()){
							samples.add(new Pair<>(new TraceList(new ArrayList<>(subtraces)),new Expression.Line(spans.subList(i,j+1))));
						}
					}
				}
			}
		}else if(expression instanceof Expression.Fraction){
			Expression.Fraction e=(Expression.Fraction)expression;
			list=new TraceList();
			Pair<TraceList,Integer> p=collect(e.getNumerator(),maxLength,samples);
			count+=p.getValue();
			list.getTraces().addAll(p.getKey().getTraces());
			++count;
			list.getTraces().addAll((List<Trace>)e.getPrimitives());
			p=collect(e.getDenominator(),maxLength,samples);
			count+=p.getValue();
			list.getTraces().addAll(p.getKey().getTraces());
		}else if(expression instanceof Expression.Radical){
			Expression.Radical e=(Expression.Radical)expression;
			list=new TraceList();
			Pair<TraceList,Integer> p;
			if(e.getPower()!=null){
				p=collect(e.getPower(),maxLength,samples);
				count+=p.getValue();
				list.getTraces().addAll(p.getKey().getTraces());
			}
			++count;
			list.getTraces().addAll((List<Trace>)e.getPrimitives());
			p=collect(e.getRadicand(),maxLength,samples);
			count+=p.getValue();
			list.getTraces().addAll(p.getKey().getTraces());
		}else if(expression instanceof Expression.Subscript){
			Expression.Subscript e=(Expression.Subscript)expression;
			list=new TraceList();
			Pair<TraceList,Integer> p=collect(e.getBase(),maxLength,samples);
			count+=p.getValue();
			list.getTraces().addAll(p.getKey().getTraces());
			p=collect(e.getSubscript(),maxLength,samples);
			count+=p.getValue();
			list.getTraces().addAll(p.getKey().getTraces());
		}else if(expression instanceof Expression.Superscript){
			Expression.Superscript e=(Expression.Superscript)expression;
			list=new TraceList();
			Pair<TraceList,Integer> p=collect(e.getBase(),maxLength,samples);
			count+=p.getValue();
			list.getTraces().addAll(p.getKey().getTraces());
			p=collect(e.getSuperscript(),maxLength,samples);
			count+=p.getValue();
			list.getTraces().addAll(p.getKey().getTraces());
		}else if(expression instanceof Expression.Subsuperscript){
			Expression.Subsuperscript e=(Expression.Subsuperscript)expression;
			list=new TraceList();
			Pair<TraceList,Integer> p=collect(e.getBase(),maxLength,samples);
			count+=p.getValue();
			list.getTraces().addAll(p.getKey().getTraces());
			p=collect(e.getSubscript(),maxLength,samples);
			count+=p.getValue();
			list.getTraces().addAll(p.getKey().getTraces());
			p=collect(e.getSuperscript(),maxLength,samples);
			count+=p.getValue();
			list.getTraces().addAll(p.getKey().getTraces());
		}else if(expression instanceof Expression.Under){
			Expression.Under e=(Expression.Under)expression;
			list=new TraceList();
			Pair<TraceList,Integer> p=collect(e.getContent(),maxLength,samples);
			count+=p.getValue();
			list.getTraces().addAll(p.getKey().getTraces());
			p=collect(e.getUnder(),maxLength,samples);
			count+=p.getValue();
			list.getTraces().addAll(p.getKey().getTraces());
		}else if(expression instanceof Expression.Over){
			Expression.Over e=(Expression.Over)expression;
			list=new TraceList();
			Pair<TraceList,Integer> p=collect(e.getContent(),maxLength,samples);
			count+=p.getValue();
			list.getTraces().addAll(p.getKey().getTraces());
			p=collect(e.getOver(),maxLength,samples);
			count+=p.getValue();
			list.getTraces().addAll(p.getKey().getTraces());
		}else if(expression instanceof Expression.UnderOver){
			Expression.UnderOver e=(Expression.UnderOver)expression;
			list=new TraceList();
			Pair<TraceList,Integer> p=collect(e.getContent(),maxLength,samples);
			count+=p.getValue();
			list.getTraces().addAll(p.getKey().getTraces());
			p=collect(e.getUnder(),maxLength,samples);
			count+=p.getValue();
			list.getTraces().addAll(p.getKey().getTraces());
			p=collect(e.getOver(),maxLength,samples);
			count+=p.getValue();
			list.getTraces().addAll(p.getKey().getTraces());
		}else{
			list=new TraceList(Collections.emptyList());
			Logger.getLogger(Crohme.class.getName()).log(Level.INFO,expression.getClass().getCanonicalName());
		}
		if(count<=maxLength){
			samples.add(new Pair<>(list,expression));
		}
		return new Pair<>(list,count);
	}
}
