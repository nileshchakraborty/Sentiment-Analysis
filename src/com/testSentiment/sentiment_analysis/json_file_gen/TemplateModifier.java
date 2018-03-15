package com.testSentiment.sentiment_analysis.json_file_gen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class TemplateModifier {

	private static Set<File> fileList = new LinkedHashSet<File>();

	public static void main(String[] args) throws Exception {

		getFileList("transcriptions");
		processFile();

	}

	private static void processFile() throws Exception {

		BufferedWriter bw = null;
		for (File file : fileList) {
			File newFile = new File("Output/" + file.getName());
			if (!newFile.exists())
				newFile.createNewFile();

			try {
				bw = new BufferedWriter(new FileWriter(newFile));
			} catch (Exception e) {
			}
			generateKeyInFile(file, newFile, bw);
			bw.flush();
			bw.close();

		}

	}

	private static void generateKeyInFile(File file, File newFile, BufferedWriter bw)
			throws FileNotFoundException, IOException {
		String fileName = file.getName().toString().substring(0, file.getName().indexOf("."));
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line = null;
			int counter = 0001;
			while ((line = br.readLine()) != null) {
				StringBuilder sb = new StringBuilder();
				sb.append(fileName + "_" + String.format("%04d", counter) + "," + line+"\n");
				counter++;
				bw.write(sb.toString());
			}
		}

	}

	private static void getFileList(String folders) throws Exception {

		File directory = new File(folders);
		if (directory.isDirectory()) {
			File[] files = directory.listFiles();
			for (File file : files) {
				fileList.add(file);
			}
		}
	}

}
