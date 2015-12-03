package com.github.mdjc.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Utils {
	public static String readContents(File file) throws FileNotFoundException, IOException {
		StringBuilder builder = new StringBuilder();

		try (BufferedReader bf = new BufferedReader(new FileReader(file))) {
			String line;

			while ((line = bf.readLine()) != null) {
				builder.append(line);
			}
		}

		return builder.toString();
	}
}
