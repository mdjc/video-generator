package com.github.mdjc.videogenerator.helpers;

import java.io.File;
import java.io.IOException;

public class MediaHelper {
	public static File generateMergedAudio(File inputListFile, File outputFile) throws IOException,
			InterruptedException {
		CommandHelper.execute(
				"ffmpeg",
				"-y",
				"-f", "concat",
				"-i", inputListFile.getPath(),
				"-c", "copy",
				outputFile.getPath());
		return outputFile;
	}

	public static void generateVideo(File imageFile, File audioFile, String prefix, File outputDir)
			throws IOException, InterruptedException {
		String basename = String.format("%s.mp4", prefix);
		File outputFile = new File(outputDir, basename);
		CommandHelper.execute(
				"ffmpeg",
				"-y",
				"-loop", "1",
				"-i", imageFile.getPath(),
				"-i", audioFile.getPath(),
				"-c:v", "libx264",
				"-vf", "fps=25",
				"-pix_fmt", "yuv420p",
				"-shortest",
				outputFile.getPath());
	}

	public static void generateMuteAudio(File mp3, File outputFile) throws IOException, InterruptedException {
		CommandHelper.execute(
				"ffmpeg",
				"-y",
				"-vol", "0",
				"-i", mp3.getPath(),
				outputFile.getPath());
	}
}