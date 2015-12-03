package com.github.mdjc.videogenerator.helpers;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mdjc.common.StreamGobbler;

public class CommandHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandHelper.class);

	public static void execute(String... commandTokens) throws IOException, InterruptedException {
		LOGGER.info("Executing command {}", Arrays.toString(commandTokens).replaceAll(",", ""));

		Process process = new ProcessBuilder(commandTokens).start();
		StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "PROCESS stderror");
		StreamGobbler inputGobbler = new StreamGobbler(process.getInputStream(), "PROCESS stdoutput");
		errorGobbler.start();
		inputGobbler.start();
		int exitCode = process.waitFor();
		LOGGER.info("exitCode: {}", exitCode);

		if (exitCode != 0) {
			throw new RuntimeException(String.format("Command %s returned exit code %d ", commandTokens, exitCode));
		}
	}
}
