package com.github.mdjc.videogenerator;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.mdjc.common.Utils;
import com.github.mdjc.videogenerator.helpers.ImageHelper;
import com.github.mdjc.videogenerator.helpers.MediaHelper;

@Component
public class AppRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppRunner.class);

	private static final FileFilter MP3_FILTER = pathname -> pathname.isFile()
			&& pathname.getName().toLowerCase().endsWith(".mp3");

	private static final FileFilter DIRECTORY_FILTER = pathname -> pathname.isDirectory();

	private static final int HTML_FONT_SIZE = 6;
	private static final int HTML_WRAP_WIDTH = 600;
	private static final int MP3_REPETION_COUNT = 5;
	private static final int INITIAL_PAUSE_SECONDS = 0;

	@Value("${input.dir}")
	private File inputDir;

	@Value("${output.dir}")
	private File outputDir;

	@Value("${output.audio.only}")
	private boolean outputOnlyAudio;

	private File tempDir;

	private File oneSecSilentMP3File;
	private int videoCount = -1;

	public void run() throws IOException, InterruptedException {
		tempDir = new File(outputDir, "temp");

		// TODO: copy mp3 in a temp folder and move it to java resources
		oneSecSilentMP3File = new File("resources\\1sec.mp3");

		if (tempDir.exists()) {
			FileUtils.deleteDirectory(tempDir);
		}

		if (tempDir.exists()) {
			LOGGER.warn("FileUtils.deleteDirectory failed");
		}

		if (!tempDir.exists()) {
			tempDir.mkdir();
		}

		for (File subjectDir : inputDir.listFiles(DIRECTORY_FILTER)) {
			proccessSubjectDir(subjectDir);
		}

		LOGGER.info("Video count is {} ", videoCount);
		LOGGER.info("The end");
	}

	private void proccessSubjectDir(File subjectDir) throws IOException, InterruptedException {
		for (File sectiontDir : subjectDir.listFiles(DIRECTORY_FILTER)) {
			processSectiontDir(sectiontDir);
		}
	}

	private void processSectiontDir(File sectionDir) throws FileNotFoundException, IOException {
		String title = Utils.readContents(new File(sectionDir, "title.txt"));
		for (File lessonDir : sectionDir.listFiles(DIRECTORY_FILTER)) {
			try {
				videoCount++;
				if (outputOnlyAudio) {
					processLessonDirOnlyAudio(lessonDir, title);
					continue;
				}

				processLessonDir(lessonDir, title);
			} catch (Exception e) {
				LOGGER.error("Error processing lesson", e);
			}

		}
	}

	private void processLessonDir(File lessonDir, String sectionTitle) throws IOException, InterruptedException {
		LOGGER.info("Processing Lesson {}", lessonDir);
		File image = generateImage(lessonDir);
		String mergedAudioBasename = String.format("video%d_merge.wav", videoCount);
		File mergedAudioFile = generateMergedAudioFile(lessonDir, new File(tempDir, mergedAudioBasename));
		String prefix = String.format("%03d_%s", videoCount, sectionTitle);
		MediaHelper.generateVideo(image, mergedAudioFile, prefix, outputDir);
	}

	private void processLessonDirOnlyAudio(File lessonDir, String sectionTitle) throws IOException,
			InterruptedException {
		LOGGER.info("Processing Lesson {}", lessonDir);
		String mergedAudioBasename = String.format("%d_%s.wav", videoCount, sectionTitle);
		generateMergedAudioFile(lessonDir, new File(outputDir, mergedAudioBasename));
	}

	private File generateImage(File lessonDir) throws FileNotFoundException, IOException {
		LOGGER.info("generating image from htmlfile");
		File htmlFile = new File(lessonDir, "body.html");
		String html = Utils.readContents(htmlFile);
		String imageName = String.format("video%d", videoCount);
		File imageFile = ImageHelper.generatePNG(tempDir, imageName, html, HTML_FONT_SIZE, HTML_WRAP_WIDTH);
		BufferedImage buffImg = ImageIO.read(imageFile);
		BufferedImage scaledImage = ImageHelper.resizeDivisibleBy2(buffImg);
		return ImageHelper.save(scaledImage, imageFile);
	}

	private File generateMergedAudioFile(File lessonDir, File outputFile) throws IOException, InterruptedException {
		LOGGER.info("generating merged audio file");
		File audioListFile = generateAudioListFile(lessonDir);
		return MediaHelper.generateMergedAudio(audioListFile, outputFile);
	}

	private File generateAudioListFile(File lessonDir) throws IOException, InterruptedException {
		LOGGER.info("generating audio list file");
		String audioListBasename = String.format("video%d_audio_list.txt", videoCount);
		File audioListFile = new File(tempDir, audioListBasename);

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(audioListFile))) {
			addAudioEntry(bw, oneSecSilentMP3File, INITIAL_PAUSE_SECONDS);
			int mp3Count = 0;

			for (File lessonFile : lessonDir.listFiles(MP3_FILTER)) {
				processMp3File(bw, lessonFile, mp3Count++);
			}

			bw.flush();
		}

		return audioListFile;
	}

	private void processMp3File(BufferedWriter bw, File file, int mp3Count) throws IOException, InterruptedException {
		LOGGER.info("processing mp3 file");
		File muteMp3 = generateMuteMp3(file, mp3Count);

		for (int i = 0; i < MP3_REPETION_COUNT; i++) {
			addAudioEntry(bw, file, 1);
			addAudioEntry(bw, muteMp3, 2);
		}
	}

	private File generateMuteMp3(File file, int mp3Count) throws IOException, InterruptedException {
		LOGGER.info("generating mute mp3 file");
		String muteBasename = String.format("video%d_mute%d.mp3", videoCount, mp3Count);
		File muteMp3 = new File(tempDir, muteBasename);
		MediaHelper.generateMuteAudio(file, muteMp3);
		return muteMp3;
	}

	private static void addAudioEntry(BufferedWriter bw, File audioFile, int times) throws IOException {
		for (int i = 0; i < times; i++) {
			String entry = String.format("file '%s'", audioFile.getPath());
			bw.write(entry);
			bw.newLine();
		}
	}
}
