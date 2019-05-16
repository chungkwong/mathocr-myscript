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
import cc.chungkwong.mathocr.offline.extractor.orderer.Orderer;
import cc.chungkwong.mathocr.online.TraceList;
import cc.chungkwong.mathocr.common.Pair;
import cc.chungkwong.mathocr.offline.extractor.orderer.*;
import java.io.*;
import java.util.*;
import java.util.stream.*;
/**
 * Tests on stroke orderer
 *
 * @author Chan Chung Kwong
 */
public class OrdererTests{
	private static final int HIST_LENGTH=5;
	/**
	 * Test by permuting strokes randomly and than order them
	 *
	 * @param stream ground truth
	 * @param orderer the orderer tested
	 */
	public static void testByPermute(Stream<TraceList> stream,Orderer orderer){
		test(stream.map((traceList)->{
			TraceList permuted=new TraceList(new ArrayList<>(traceList.getTraces()));
			Collections.shuffle(permuted.getTraces());
			return new Pair<>(traceList,permuted);
		}),orderer);
	}
	/**
	 * Test by comparing two ordering
	 *
	 * @param stream pairs of ground truth and recovered order
	 * @param orderer the orderer tested
	 */
	public static void test(Stream<Pair<TraceList,TraceList>> stream,Orderer orderer){
		int exprCount=0, traceCount=0, lcs=0, exact=0;
		int[] hist=new int[HIST_LENGTH];
		int[] nGramCount=new int[N];
		int[] matchedNGramCount=new int[N];
		long start=System.currentTimeMillis();
		for(Iterator<Pair<TraceList,TraceList>> iterator=stream.iterator();iterator.hasNext();){
			Pair<TraceList,TraceList> next=iterator.next();
			TraceList list0=next.getKey();
			TraceList list1=next.getValue();
			list1=orderer.order(list1);
			++exprCount;
			traceCount+=list0.getTraces().size();
			if(list0.getTraces().equals(list1.getTraces())){
				++exact;
			}
			int lcsl=getLcsLength(list0.getTraces(),list1.getTraces());
			int tmp=list0.getTraces().size()-lcsl;
			if(tmp<HIST_LENGTH){
				++hist[tmp];
			}
			lcs+=lcsl;
			updateBleu(list0.getTraces().stream().map((trace)->trace.getId()).toArray(String[]::new),
					list1.getTraces().stream().map((trace)->trace.getId()).toArray(String[]::new),nGramCount,matchedNGramCount);
		}
		System.out.println(orderer.toString());
		System.out.format("Expr count:%d%n",exprCount);
		System.out.format("Trace count:%d%n",traceCount);
		for(int i=0;i<HIST_LENGTH;i++){
			System.out.format("Hist%d:%d(%f)%n",i,hist[i],hist[i]*1.0/exprCount);
		}
		System.out.format("Exact:%d(%f)%n",exact,exact*1.0/exprCount);
		System.out.format("LCS:%d(%f)%n",lcs,lcs*1.0/traceCount);
		System.out.format("BLEU:%f%n",getBleu(nGramCount,matchedNGramCount,traceCount));
		System.out.format("TIME:%dms%n",System.currentTimeMillis()-start);
	}
	private static <E> int getLcsLength(List<E> list0,List<E> list1){
		if(list1.size()>list0.size()){
			List<E> tmp=list0;
			list0=list1;
			list1=tmp;
		}
		int len0=list0.size(), len1=list1.size();
		int[] lastRow=new int[len1+1];
		int[] currRow=new int[len1+1];
		for(int i=0;i<len0;i++){
			for(int j=0;j<len1;j++){
				currRow[j+1]=Math.max(currRow[j],lastRow[j+1]);
				if(Objects.equals(list0.get(i),list1.get(j))){
					currRow[j+1]=Math.max(lastRow[j]+1,currRow[j+1]);
				}
			}
			int[] tmp=lastRow;
			lastRow=currRow;
			currRow=tmp;
		}
		return lastRow[len1];
	}
	private static final int N=4;
	private static void updateBleu(String[] predicted,String[] actual,int[] nGramCount,int[] matchedNGramCount){
		for(int i=0;i<N;i++){
			int found=Math.max(0,predicted.length-i);
			int expected=Math.max(0,actual.length-i);
			nGramCount[i]+=found;
			BitSet used=new BitSet(expected);
			for(int j=0;j<found;j++){
				out:
				for(int k=0;k<expected;k++){
					if(!used.get(k)){
						for(int l=0;l<=i;l++){
							if(!Objects.equals(predicted[j+l],actual[k+l])){
								continue out;
							}
						}
						used.set(k);
						++matchedNGramCount[i];
						break;
					}
				}
			}
		}
	}
	private static double getBleu(int[] nGramCount,int[] matchedNGramCount,int actualLength){
		double[] p=new double[N];
		for(int i=0;i<N;i++){
			p[i]=matchedNGramCount[i]*1.0/nGramCount[i];
			System.out.println("p_"+i+"="+p[i]);
		}
		double penalty=nGramCount[0]>=actualLength?1:Math.exp(1-actualLength*1.0/nGramCount[0]);
		return penalty*Math.exp(Arrays.stream(p).map((q)->Math.log(q)).sum()/N);
	}
	public static void main(String[] args) throws IOException{
		testByPermute(Crohme.getTestStream2016().map((ink)->ink.getTraceList()),new BypassOrderer());
		testByPermute(Crohme.getTestStream2016().map((ink)->ink.getTraceList()),new LtrOrderer());
		testByPermute(Crohme.getTestStream2016().map((ink)->ink.getTraceList()),new TopologicalOrderer());
		testByPermute(Crohme.getTestStream2016().map((ink)->ink.getTraceList()),new CutOrderer());
		testByPermute(Crohme.getValidationStream2016().map((ink)->ink.getTraceList()),new BypassOrderer());
		testByPermute(Crohme.getValidationStream2016().map((ink)->ink.getTraceList()),new LtrOrderer());
		testByPermute(Crohme.getValidationStream2016().map((ink)->ink.getTraceList()),new TopologicalOrderer());
		testByPermute(Crohme.getValidationStream2016().map((ink)->ink.getTraceList()),new CutOrderer());
//		testByPermute(Crohme.getValidationStream2016().map((ink)->ink.getTraceList()),new DistanceOrderer());
		//test(CrohmeUtil.getFullStream().map((ink)->ink.getTraceList()).iterator(),new DistanceOrderer());
	}
}
