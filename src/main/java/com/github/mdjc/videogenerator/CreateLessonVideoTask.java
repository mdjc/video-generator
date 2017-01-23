package com.github.mdjc.videogenerator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mdjc.common.Utils;
import com.github.mdjc.videogenerator.helpers.ImageHelper;
import com.github.mdjc.videogenerator.helpers.MediaHelper;

public class CreateLessonVideoTask extends CreateLessonAudioTask {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppRunner.class);

	private static final int HTML_FONT_SIZE = 6;
	private static final int HTML_WRAP_WIDTH = 600;

	public CreateLessonVideoTask(int audioId, String title, File inputDir, File outputDir) {
		super(audioId, title, inputDir, outputDir);
	}

	@Override
	public void run() {
		LOGGER.info("Processing Lesson {}", inputDir);
		String mergedAudioBasename = String.format("video%d_merge.wav", mediaId);
		File mergedAudioFile;
		try {
			File image = generateImage();
			mergedAudioFile = generateMergedAudioFile(new File(tempDir, mergedAudioBasename));
			String prefix = String.format("%03d_%s", mediaId, title);
			MediaHelper.generateVideo(image, mergedAudioFile, prefix, outputDir);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private File generateImage() throws FileNotFoundException, IOException {
		LOGGER.info("generating image from htmlfile");
		File htmlFile = new File(inputDir, "body.html");
		String html = Utils.readContents(htmlFile);
		String imageName = String.format("video%d", mediaId);
		File imageFile = ImageHelper.generatePNG(tempDir, imageName, html, HTML_FONT_SIZE, HTML_WRAP_WIDTH);
		BufferedImage buffImg = ImageIO.read(imageFile);
		BufferedImage scaledImage = ImageHelper.resizeDivisibleBy2(buffImg);
		return ImageHelper.save(scaledImage, imageFile);
	}

}
