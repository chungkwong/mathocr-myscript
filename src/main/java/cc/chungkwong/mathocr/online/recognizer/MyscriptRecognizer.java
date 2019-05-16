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
package cc.chungkwong.mathocr.online.recognizer;
import cc.chungkwong.mathocr.*;
import cc.chungkwong.mathocr.common.*;
import cc.chungkwong.mathocr.common.format.*;
import cc.chungkwong.mathocr.online.*;
import com.fasterxml.jackson.core.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.security.*;
import java.util.logging.*;
import javax.crypto.*;
import javax.crypto.spec.*;
/**
 * MyScript Cloud based online mathematical expression recognizer
 *
 * @author Chan Chung Kwong
 */
public class MyscriptRecognizer implements OnlineRecognizer{
	private final String application, hmac, grammar;
	private final JsonFactory factory=new JsonFactory();
	private final int dpi;
	/**
	 * Create a instance using global settings
	 */
	public MyscriptRecognizer(){
		this.application=null;
		this.hmac=null;
		this.grammar=null;
		this.dpi=0;
	}
	/**
	 * Create a instance
	 *
	 * @param application application key of your MyScript account
	 * @param hmac hmac key of your Myscript account
	 * @param grammar an uploaded grammar of your Myscript account
	 * @param dpi dot per inch
	 */
	public MyscriptRecognizer(String application,String hmac,String grammar,int dpi){
		this.application=application;
		this.hmac=hmac;
		this.grammar=grammar;
		this.dpi=dpi;
	}
	@Override
	public EncodedExpression recognize(TraceList traceList){
		HttpURLConnection connection=null;
		try{
			String body=generateBody(traceList);
			Logger.getLogger(MyscriptRecognizer.class.getName()).finest(body);
			//int x=5/(4-4);
			connection=(HttpURLConnection)new URL("https://cloud.myscript.com/api/v4.0/iink/batch").openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type","application/json");
			connection.setRequestProperty("Accept","application/mathml+xml");
			connection.setRequestProperty("applicationKey",getApplication());
			connection.setRequestProperty("hmac",sign(body));
			connection.setDoOutput(true);
			connection.connect();
			try(OutputStream out=connection.getOutputStream()){
				out.write(body.getBytes(StandardCharsets.UTF_8));
			}
			StringBuilder result=new StringBuilder();
			try(Reader in=new InputStreamReader(connection.getInputStream(),StandardCharsets.UTF_8)){
				char[] buf=new char[4096];
				int c;
				while((c=in.read(buf))!=-1){
					result.append(buf,0,c);
				}
			}
			//System.out.println(result.toString());
			return new EncodedExpression(result.toString(),new MathmlFormat());
		}catch(IOException|NoSuchAlgorithmException|InvalidKeyException ex){
			if(connection!=null){
				try(Reader in=new InputStreamReader(connection.getErrorStream(),StandardCharsets.UTF_8)){
					Logger.getLogger(MyscriptRecognizer.class.getName()).log(Level.SEVERE,connection.getResponseMessage(),ex);
					char[] buf=new char[4096];
					int c;
					while((c=in.read(buf))!=-1){
						System.err.print(new String(buf,0,c));
					}
					System.err.println();
				}catch(IOException ex1){
					Logger.getLogger(MyscriptRecognizer.class.getName()).log(Level.SEVERE,null,ex1);
				}
			}else{
				Logger.getLogger(MyscriptRecognizer.class.getName()).log(Level.SEVERE,null,ex);
			}
			return null;
		}
	}
	private String sign(String content) throws NoSuchAlgorithmException,InvalidKeyException{
		String algorithm="HmacSHA512";
		Mac mac=Mac.getInstance(algorithm);
		mac.init(new SecretKeySpec((getApplication()+getHmac()).getBytes(StandardCharsets.UTF_8),algorithm));
		return encodeHexString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
	}
	private static final char[] DIGITS_LOWER
			={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	private static String encodeHexString(final byte[] data){
		final int l=data.length;
		final char[] out=new char[l<<1];
		for(int i=0, j=0;i<l;i++){
			out[j++]=DIGITS_LOWER[(0xF0&data[i])>>>4];
			out[j++]=DIGITS_LOWER[0x0F&data[i]];
		}
		return new String(out);
	}
	private String generateBody(TraceList traceList){
		StringWriter out=new StringWriter();
		int dpi=getDpi();
		String grammar=getGrammar();
		try(JsonGenerator generator=factory.createGenerator(out)){
			generator.writeStartObject();
			generator.writeNumberField("xDPI",getDpi());
			generator.writeNumberField("yDPI",getDpi());
			generator.writeStringField("contentType","Math");
			generator.writeObjectFieldStart("configuration");
			generator.writeBooleanField("math.solver.enable",false);
			if(grammar!=null&&!grammar.isEmpty()){
				generator.writeStringField("math.customGrammarId",grammar);
			}
			generator.writeBooleanField("export.jiix.strokes",false);
			generator.writeEndObject();
			generator.writeArrayFieldStart("strokeGroups");
			generator.writeStartObject();
			generator.writeArrayFieldStart("strokes");
			for(Trace trace:traceList.getTraces()){
				generator.writeStartObject();
				generator.writeFieldName("x");
				generator.writeArray(trace.getPoints().stream().mapToInt(TracePoint::getX).toArray(),0,trace.getPoints().size());
				generator.writeFieldName("y");
				generator.writeArray(trace.getPoints().stream().mapToInt(TracePoint::getY).toArray(),0,trace.getPoints().size());
				if(trace.getId()!=null){
					generator.writeStringField("id",trace.getId());
				}
				generator.writeEndObject();
			}
			generator.writeEndArray();
			generator.writeEndObject();
			generator.writeEndArray();
			generator.writeEndObject();
		}catch(IOException ex){
			Logger.getLogger(MyscriptRecognizer.class.getName()).log(Level.SEVERE,null,ex);
		}
		return out.toString();
	}
	private String getApplication(){
		return application!=null?application:Settings.DEFAULT.getString("MYSCRIPT_APPLICATION");
	}
	private String getHmac(){
		return hmac!=null?hmac:Settings.DEFAULT.getString("MYSCRIPT_HMAC");
	}
	private String getGrammar(){
		return grammar!=null?grammar:Settings.DEFAULT.getString("MYSCRIPT_GRAMMAR");
	}
	private int getDpi(){
		return dpi>0?dpi:Settings.DEFAULT.getInteger("DPI");
	}
	@Override
	public boolean equals(Object obj){
		return obj!=null&&obj.getClass()==getClass();
	}
	@Override
	public int hashCode(){
		return getClass().hashCode();
	}
	@Override
	public String toString(){
		return "myscript";
	}
}
