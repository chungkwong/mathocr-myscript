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
package cc.chungkwong.mathocr.offline.extractor;
import cc.chungkwong.mathocr.common.*;
import cc.chungkwong.mathocr.offline.*;
import cc.chungkwong.mathocr.offline.extractor.orderer.*;
import cc.chungkwong.mathocr.offline.extractor.tracer.*;
import cc.chungkwong.mathocr.offline.preprocessor.*;
import cc.chungkwong.mathocr.online.*;
import cc.chungkwong.mathocr.online.recognizer.*;
import java.awt.image.*;
import java.util.*;
/**
 * Offline recognizer based on online recognizer
 *
 * @author Chan Chung Kwong
 */
public class Extractor implements OfflineRecognizer{
	/**
	 * MyScript based offline recognizer
	 */
	public static final Extractor DEFAULT=new Extractor(new MyscriptRecognizer());
	private final Preprocessor preprocessor;
	private final SkeletonTracer tracer;
	private final GraphTracer graphTracer;
	private final Orderer orderer;
	private final OnlineRecognizer recognizer;
	/**
	 * Create a offline recognizer
	 *
	 * @param recognizer
	 */
	public Extractor(OnlineRecognizer recognizer){
		this(new CombinedPreprocessor(Arrays.asList(new ToGrayscale(),new SauvolaBinarizer(0.11,128))),
				//			new CombinedPreprocessor(Arrays.asList(new ToGrayscale(),new OtsuBinarizer())),
				//			new CombinedPreprocessor(Arrays.asList(new ToGrayscale(),new FixedBinarizer(195))),
				new ThinTracer(),new GreedyGraphTracer(),new CutOrderer(),recognizer);
	}
	/**
	 * Create a offline recognizer
	 *
	 * @param preprocessor
	 * @param tracer
	 * @param graphTracer
	 * @param orderer
	 * @param recognizer
	 */
	public Extractor(Preprocessor preprocessor,SkeletonTracer tracer,GraphTracer graphTracer,Orderer orderer,OnlineRecognizer recognizer){
		this.preprocessor=preprocessor;
		this.tracer=tracer;
		this.graphTracer=graphTracer;
		this.orderer=orderer;
		this.recognizer=recognizer;
	}
	/**
	 *
	 * @return preprocessor
	 */
	public Preprocessor getPreprocessor(){
		return preprocessor;
	}
	/**
	 *
	 * @return skeleton tracer
	 */
	public SkeletonTracer getTracer(){
		return tracer;
	}
	/**
	 *
	 * @return graph tracer
	 */
	public GraphTracer getGraphTracer(){
		return graphTracer;
	}
	/**
	 *
	 * @return stroke orderer
	 */
	public Orderer getOrderer(){
		return orderer;
	}
	/**
	 *
	 * @return underlying online recognizer
	 */
	public OnlineRecognizer getRecognizer(){
		return recognizer;
	}
	/**
	 * Extract strokes from a image
	 *
	 * @param image the image
	 * @return the strokes
	 */
	public TraceList extract(BufferedImage image){
		return extract(image,true);
	}
	/**
	 * Preprocess a image
	 *
	 * @param image the image
	 * @return binary image
	 */
	public Bitmap preprocess(BufferedImage image){
		return new Bitmap(preprocessor.apply(image,true));
	}
	/**
	 * Extract strokes from a image
	 *
	 * @param image the image
	 * @param reorder if stroke order normalization should be applied
	 * @return the strokes
	 */
	public TraceList extract(BufferedImage image,boolean reorder){
		TraceList list=graphTracer.trace(tracer.trace(preprocess(image)));
		if(reorder){
			list=orderer.order(list);
		}
		return list;
	}
	@Override
	public EncodedExpression recognize(Bitmap image){
		return recognize(graphTracer.trace(tracer.trace(image)),true);
	}
	/**
	 * Offline recognition
	 *
	 * @param image to be recognized
	 * @return recognition result
	 */
	public EncodedExpression recognize(BufferedImage image){
		return recognize(extract(image,false),true);
	}
	/**
	 * Online recognition
	 *
	 * @param traceList strokes to be recognized
	 * @param reorder if stroke order normalization should be applied
	 * @return recognition result
	 */
	public EncodedExpression recognize(TraceList traceList,boolean reorder){
		if(reorder){
			traceList=orderer.order(traceList);
		}
		return recognize(traceList);
	}
	/**
	 * Online recognition
	 *
	 * @param traceList strokes to be recognized
	 * @return recognition result
	 */
	public EncodedExpression recognize(TraceList traceList){
		return recognizer.recognize(traceList);
	}
}
