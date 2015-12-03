package com.github.mdjc.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamGobbler extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(StreamGobbler.class);

	private final InputStream is;
	private final String type;

	public StreamGobbler(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}

	@Override
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			String line = null;

			while ((line = br.readLine()) != null) {
				LOGGER.debug("{} > {}", type, line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
