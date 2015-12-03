package com.github.mdjc.videogenerator.helpers;

import gui.ava.html.image.generator.HtmlImageGenerator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

public class ImageHelper {
	public static File generatePNG(File outputDir, String filename, String html, int fontSize, int wrapWidth) {
		String formattedHtml = format(html, wrapWidth, fontSize);
		HtmlImageGenerator hig = new HtmlImageGenerator();
		hig.loadHtml(formattedHtml);
		File outputImg = new File(outputDir, filename + ".png");
		hig.saveAsImage(outputImg);
		return outputImg;
	}

	private static String format(String html, int wrapWidth, int fontSize) {
		StringBuilder builder = new StringBuilder(html);
		builder.insert(0, String.format("<table><tr><td width='%d'>", wrapWidth));
		builder.insert(builder.length(), "</td></tr></table>");
		builder.insert(builder.indexOf("</h1>") + 5, String.format("<font size='%d'>", fontSize));
		builder.append("</font>");
		return builder.toString();
	}

	public static BufferedImage resizeDivisibleBy2(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		int newWidth = width % 2 == 0 ? width : width - 1;
		int newHeight = height % 2 == 0 ? height : height - 1;
		return Scalr.resize(img, Scalr.Method.BALANCED, Scalr.Mode.FIT_EXACT, newWidth, newHeight);
	}

	public static File save(BufferedImage img, File outputFile) throws IOException {
		ImageIO.write(img, "png", outputFile);
		return outputFile;
	}

}
