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
package cc.chungkwong.mathocr;
import cc.chungkwong.mathocr.common.*;
import cc.chungkwong.mathocr.common.format.*;
import cc.chungkwong.mathocr.offline.extractor.*;
import cc.chungkwong.mathocr.online.recognizer.*;
import java.io.*;
import javax.imageio.*;
/**
 * A example showing the usage of API
 *
 * @author Chan Chung Kwong
 */
public class Example{
	public static void main(String[] args) throws IOException{
		String applicationKey="your application key for MyScript";
		String hmacKey="hmac key of your Myscript account";
		String grammarId="an uploaded grammar of your Myscript account";
		int dpi=96;
		MyscriptRecognizer myscriptRecognizer=new MyscriptRecognizer(applicationKey,hmacKey,grammarId,dpi);
		Extractor extractor=new Extractor(myscriptRecognizer);
		File file=new File("Path to file to be recognized");
		EncodedExpression expression=extractor.recognize(ImageIO.read(file));
		String latexCode=expression.getCodes(new LatexFormat());
		System.out.println(latexCode);
	}
}
