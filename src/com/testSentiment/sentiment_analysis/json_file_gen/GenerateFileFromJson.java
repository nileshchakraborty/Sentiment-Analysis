package com.testSentiment.sentiment_analysis.json_file_gen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GenerateFileFromJson {

	private static List<File> fileList = new LinkedList<>();
	private static Set<String> statementsOfSentiments = new HashSet<>();
	private static Map<String, Map<String, String>> sentimentExtractionMap = new TreeMap<>();
	private static Map<String, Map<String, String>> sentimentMap = new LinkedHashMap<>();
	private static Map<String, String> mapUserAndStatement = new LinkedHashMap<>();


	public static void main(String[] args) throws Exception {

		getFileList("Input");
		processFile();

	}

	private static void processFile() throws Exception {

		readJson();

		createStatementSentimentMap();
		BufferedWriter bw = null;

		File newFile = new File("Output/groundTruthNEW.csv");
		if (!newFile.exists())
			newFile.createNewFile();

		try {
			bw = new BufferedWriter(new FileWriter(newFile));
		} catch (Exception e) {
		}

		writeHeader(bw);

		mapFileToSentiment();

		writeTable(bw);
		bw.flush();
		bw.close();
	}

	private static void createStatementSentimentMap() {
		for (Entry<String, Map<String, String>> s : sentimentExtractionMap.entrySet()) {
			Map<String, String> m = s.getValue();
			for (Entry<String, String> e : m.entrySet()) {
				if (!e.getKey().equals(""))
					statementsOfSentiments.add(e.getKey());
			}

		}
	}

	private static void readJson() throws FileNotFoundException, IOException, ParseException, InterruptedException {
		File dir = new File("Output");
		if (!dir.exists())
			dir.mkdir();
		JSONParser parser = new JSONParser();

		for (File file : fileList) {
			JSONObject obj = (JSONObject) parser.parse(new FileReader(file));
			generateMaps(obj);
		}
	}

	private static void generateMaps(JSONObject obj) {

		JSONArray jsonArray = (JSONArray) obj.get("sentiments");

		Iterator iterator = jsonArray.iterator();

		while (iterator.hasNext()) {
			Iterator iterator1 = ((Map) iterator.next()).entrySet().iterator();
			
			while (iterator1.hasNext()) {
System.out.println(iterator1.toString());
				Entry iteratorPair = (Entry) iterator1.next();

				JSONArray jsonArray2 = (JSONArray) iteratorPair.getValue();

				Iterator iterator3 = jsonArray2.iterator();

				while (iterator3.hasNext()) {
					Iterator iterator4 = ((Map) iterator3.next()).entrySet().iterator();
					StringBuilder utterance_text = null;

					while (iterator4.hasNext()) {
						Entry iteratorPair1 = (Entry) iterator4.next();

						if (iteratorPair1.getValue() instanceof String) {
							utterance_text = new StringBuilder();
							if (iteratorPair1.getKey().equals("utterance_text")) {
								String text = (String) iteratorPair1.getValue();
								utterance_text.append(text);
							}
						} else

						if (iteratorPair1.toString().equals("tones=[]"))
							if (sentimentExtractionMap.containsKey(utterance_text.toString())) {
								Map<String, String> map = sentimentExtractionMap.get(utterance_text.toString());
								map.put("", "");
								sentimentExtractionMap.put(utterance_text.toString(), map);
							} else {
								Map<String, String> map = new LinkedHashMap<>();
								map.put("", "");
								sentimentExtractionMap.put(utterance_text.toString(), map);
							}
						else {
							JSONArray jsonArray3 = (JSONArray) iteratorPair1.getValue();
							Iterator iterator5 = jsonArray3.iterator();
							while (iterator5.hasNext()) {

								Iterator iterator6 = ((Map) iterator5.next()).entrySet().iterator();

								StringBuilder tonescore = new StringBuilder();
								while (iterator6.hasNext()) {

									Entry iteratorPair5 = (Entry) iterator6.next();
									if (iteratorPair5.getKey().equals("score"))
										tonescore.append(iteratorPair5.getValue());

									if (iteratorPair5.getKey().equals("tone_name") && tonescore.toString() != null
											&& iteratorPair5.getValue() != null) {
										Map<String, String> map = null;
										if (sentimentExtractionMap.containsKey(utterance_text.toString())) {
											map = sentimentExtractionMap.get(utterance_text.toString());
											if (map == null)
												map = new LinkedHashMap<>();
											map.put((String) iteratorPair5.getValue(), tonescore.toString());
											sentimentExtractionMap.put(utterance_text.toString(), map);
										} else {
											map = new LinkedHashMap<>();
											map.put((String) iteratorPair5.getValue(), tonescore.toString());
											sentimentExtractionMap.put(utterance_text.toString(), map);
										}
									}
								}
							}

						}
					}
				}
			}
		}
	}

	private static void mapFileToSentiment() throws Exception {
		fileList.clear();
		getFileList("transcriptions");

		File dir = new File("Output");
		if (!dir.exists())
			dir.mkdir();

		for (File file : fileList)
			try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
				String line;
				while ((line = br.readLine()) != null)
					if ((line != null || !line.equals("")))

						try {
							mapUserAndStatement.put(extractText(line), extractUser(line));
						} catch (Exception e) {
						}

			}
	}

	private static String extractUser(String text) {
		return text.substring(0, text.indexOf("[") - 1);
	}

	private static String extractText(String line) {
		return line.substring(line.indexOf(":") + 2, line.length());
	}

	private static void writeTable(BufferedWriter bw) throws IOException {

		writeEmotions(mapUserAndStatement, bw);

	}

	private static void writeEmotions(Map<String, String> mapUserAndStatement, BufferedWriter bw) throws IOException {
		for (Entry<String, String> s : mapUserAndStatement.entrySet()) {
			String sentence = s.getKey();
			String utterance_id = s.getValue();

			if (sentimentExtractionMap.containsKey(sentence))
				sentimentMap.put(utterance_id, sentimentExtractionMap.get(sentence));

		}

		for (Entry<String, Map<String, String>> e1 : sentimentMap.entrySet()) {
			String utterance_id = e1.getKey();
			Map<String, String> map = e1.getValue();
			bw.write(utterance_id);
			for (String st : statementsOfSentiments) {
				if (!st.equals("")) {
					bw.write(",");
					boolean flag = false;
					for (Entry<String, String> e2 : map.entrySet()) {
						String polarity = e2.getValue();

						if (polarity != null && st.equals(e2.getKey())) {
							bw.write(polarity);
							flag = true;
						}

					}
					if (!flag)
						bw.write("0");
				}
			}
			bw.write("\n");
		}

		sentimentMap.clear();

	}

	private static void writeHeader(BufferedWriter bw) throws IOException {

		bw.write("utterance_id");
		for (String s : statementsOfSentiments) {
			bw.write("," + s);
		}
		bw.write("\n");

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
