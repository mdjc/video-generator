package com.github.mdjc.videogenerator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.mdjc.common.Utils;

@Component
public class AppRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppRunner.class);
	private static final FileFilter DIRECTORY_FILTER = pathname -> pathname.isDirectory();

	private static final int N_THREADS = Runtime.getRuntime().availableProcessors();
	private static final int CAPACITY = 800;

	@Value("${input.dir}")
	private File inputDir;

	@Value("${output.dir}")
	private File outputDir;

	@Value("${output.audio.only}")
	private boolean outputOnlyAudio;

	private File tempDir;

	private AtomicInteger videoCount;
	private ThreadPoolExecutor executor;

	public void run() throws IOException, InterruptedException {
		long startTime = System.nanoTime();
		executor = new ThreadPoolExecutor(N_THREADS + 1, N_THREADS + 1, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<>(CAPACITY));
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

		videoCount = new AtomicInteger(-1);
		tempDir = new File(outputDir, "temp");

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

		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		LOGGER.info("Video count is {} ", videoCount.get());
		long estimatedTime = System.nanoTime() - startTime;
		System.out
				.println(String.format("Completed all activities with %d threads, in %f", executor.getMaximumPoolSize(),
						estimatedTime / 1000000000.0));
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
				if (outputOnlyAudio) {
					executor.execute(
							new CreateLessonAudioTask(videoCount.incrementAndGet(), title, lessonDir, outputDir));
					continue;
				}

				executor.execute(new CreateLessonVideoTask(videoCount.incrementAndGet(), title, lessonDir, outputDir));
			} catch (Exception e) {
				LOGGER.error("Error processing lesson", e);
			}

		}
	}
}
