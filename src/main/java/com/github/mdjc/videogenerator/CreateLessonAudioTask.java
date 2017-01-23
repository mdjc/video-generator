package com.github.mdjc.videogenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mdjc.videogenerator.helpers.MediaHelper;

public class CreateLessonAudioTask implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppRunner.class);

	private static final FileFilter MP3_FILTER = pathname -> pathname.isFile()
			&& pathname.getName().toLowerCase().endsWith(".mp3");

	private static final int INITIAL_PAUSE_SECONDS = 0;
	private static final int MP3_REPETION_COUNT = 5;

	protected final int mediaId;
	protected final String title;
	protected final File inputDir;
	protected final File outputDir;
	protected final File tempDir;

	public CreateLessonAudioTask(int audioId, String title, File inputDir, File outputDir) {
		this.mediaId = audioId;
		this.title = title;
		this.inputDir = inputDir;
		this.outputDir = outputDir;
		tempDir = new File(outputDir, "temp");
	}

	@Override
	public void run() {
		LOGGER.info("Processing Lesson {}", inputDir);
		try {
			String mergedAudioBasename = String.format("%d_%s.wav", mediaId, title);
			generateMergedAudioFile(new File(outputDir, mergedAudioBasename));
		} catch (Exception e) {
			LOGGER.info("Exception processsion lesson ", e.getCause());
		}
	}

	protected File generateMergedAudioFile(File outputFile)
			throws IOException, InterruptedException {
		LOGGER.info("generating merged audio file");
		File audioListFile = generateAudioListFile();
		return MediaHelper.generateMergedAudio(audioListFile, outputFile);
	}

	private File generateAudioListFile() throws IOException, InterruptedException {
		LOGGER.info("generating audio list file");
		String audioListBasename = String.format("video%d_audio_list.txt", mediaId);
		File audioListFile = new File(tempDir, audioListBasename);
		File oneSecSilentMP3File = new File("resources\\1sec.mp3");

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(audioListFile))) {
			addAudioEntry(bw, oneSecSilentMP3File, INITIAL_PAUSE_SECONDS);
			int mp3Count = 0;

			for (File lessonFile : inputDir.listFiles(MP3_FILTER)) {
				processMp3File(bw, lessonFile, mp3Count++);
			}

			bw.flush();
		}

		return audioListFile;
	}

	private static void addAudioEntry(BufferedWriter bw, File audioFile, int times) throws IOException {
		for (int i = 0; i < times; i++) {
			String entry = String.format("file '%s'", audioFile.getPath());
			bw.write(entry);
			bw.newLine();
		}
	}

	private void processMp3File(BufferedWriter bw, File file, int mp3Count)
			throws IOException, InterruptedException {
		LOGGER.info("processing mp3 file");
		File muteMp3 = generateMuteMp3(file, mp3Count);

		for (int i = 0; i < MP3_REPETION_COUNT; i++) {
			addAudioEntry(bw, file, 1);
			addAudioEntry(bw, muteMp3, 2);
		}
	}

	private File generateMuteMp3(File file, int mp3Count) throws IOException, InterruptedException {
		LOGGER.info("generating mute mp3 file");
		String muteBasename = String.format("video%d_mute%d.mp3", mediaId, mp3Count);
		File muteMp3 = new File(tempDir, muteBasename);
		MediaHelper.generateMuteAudio(file, muteMp3);
		return muteMp3;
	}
}
