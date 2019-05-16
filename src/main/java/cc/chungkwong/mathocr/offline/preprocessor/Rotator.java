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
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
/**
 *
 * @author Chan Chung Kwong
 */
public class Rotator implements Preprocessor{
	private final double angle;
	/**
	 * Construct a SkewCorrect
	 *
	 * @param angle the angle from current x-axis to document
	 * baseline(clockwise)
	 */
	public Rotator(double angle){
		this.angle=angle;
	}
	/**
	 * Rotate image anti-clockwise by skew angle
	 *
	 * @param src the document image
	 */
	@Override
	public BufferedImage apply(BufferedImage src,boolean inplace){
		double s=Math.sin(angle), c=Math.cos(angle);
		int w=src.getWidth(), h=src.getHeight();
		int left=(int)Math.floor(Math.min(Math.min(0,h*s),Math.min(w*c,w*c+h*s)));
		int right=(int)Math.ceil(Math.max(Math.max(0,h*s),Math.max(w*c,w*c+h*s)));
		int top=(int)Math.floor(Math.min(Math.min(0,h*c),Math.min(-w*s,-w*s+h*c)));
		int bottom=(int)Math.ceil(Math.max(Math.max(0,h*c),Math.max(-w*s,-w*s+h*c)));
		int width=right-left+1, height=bottom-top+1;
		int type=src.getType();
		if(type==BufferedImage.TYPE_CUSTOM){
			type=BufferedImage.TYPE_INT_ARGB;
		}
		BufferedImage dst=new BufferedImage(width,height,type);
		Graphics2D g2d=(Graphics2D)dst.getGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0,width,height);
		g2d.drawImage(src,new AffineTransformOp(new AffineTransform(c,-s,s,c,-left,-top),AffineTransformOp.TYPE_BILINEAR),0,0);
		return dst;
	}
}
