package com.testSentiment.sentiment_analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneChatOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.Utterance;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.UtteranceAnalyses;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class App {
	private static StanfordCoreNLP pipeline;
	private static List<File> fileList = new LinkedList<>();
	private static Map<String, String> headers = new LinkedHashMap<String, String>();
	private static ToneAnalyzer service = null;

	public static void main(String[] args) throws Exception {

		getFile("transcriptions"); //transcriptions
		
		// tensorFlowAPI(null,null,null);
		processFiles();
	}

	private static void processFiles() throws Exception {
		/**
		 * Three choices of using Watch NLP, Naive Bayes NLP, and, Naive Bayes NLP using RNN
		 * 
		 **/
		 
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Analyse :\na.\tWatson NLP\nb.\tNaive Bayes NLP\nc.\tNaive Bayes NLP using RNN\nEnter your choice:\t");
		char c = (char)bufferedReader.read();
		stanfordNLPinit();
		watsonInit();
		System.out.println("Start processing files.");
		File dir = new File("Output");
		dir.mkdir();
		long startTime = System.currentTimeMillis();
		for (File file : fileList) {

			try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
				String line;
				List<String> chatListFemale = new LinkedList<String>();
				List<String> chatListMale = new LinkedList<String>();

				while ((line = br.readLine()) != null) {
					if (line != null || !line.equals("")) {
						if (line.contains("_F")) {
							chatListFemale.add(line);
						} else if (line.contains("_M")) {
							chatListMale.add(line);
						}
					}
				}
				
				/**
				 * Determine Female and Male output
				 * 
				 **/
				 
				
				
				if (chatListFemale != null) {
					// displayList(chatListFemale);
					switch(c) {
					case 'a':
					watsonNLP(new BufferedWriter(new FileWriter("./Output/outputWatsonFemale.txt", true)), file, chatListFemale);
					Thread.sleep(2500);
					break;
					case 'b':
					NLPLearn(new BufferedWriter(new FileWriter("./Output/outputNLPFemale.txt",true)), file, chatListFemale);
					break;
					case 'c':
					recursiveLearning(new BufferedWriter(new FileWriter("./Output/outputRRNFemale.txt",true)), file,
							chatListFemale);
					break;
					default:
						System.out.println("Wrong input.");
					}
				}
				if (chatListMale != null) {
					
					// displayList(chatListMale);
					switch(c) {
					case 'a':
					watsonNLP(new BufferedWriter(new FileWriter("./Output/outputWatsonMale.txt", true)), file, chatListMale);
					Thread.sleep(2500);
					break;
					case 'b':
					NLPLearn(new BufferedWriter(new FileWriter("./Output/outputNLPMale.txt",true)), file, chatListMale);
					break;
					case 'c':
					recursiveLearning(new BufferedWriter(new FileWriter("./Output/outputRRNMale.txt",true)), file,
							chatListMale);
					break;
					default:
						System.out.println("Wrong input.");
					}
				}
				br.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		long endTime = System.currentTimeMillis() - startTime;
		System.out.println("Time to process request: "+ endTime);
	}

	private static void watsonInit() {

		headers.put("X-Watson-Learning-Opt-Out", "true");
		service = new ToneAnalyzer("2016-05-19");
		service.setUsernameAndPassword("0a29e737-ebad-47ec-9bfb-2888b4452b5e", "UE6y2ly7AxCm");
		service.setDefaultHeaders(headers);

	}

	private static void displayList(List<String> chatList) {
		for (String chat : chatList) {
			String text = extractText(chat);
			System.out.println(text);

			System.out.println(chat.substring(0, chat.indexOf("[") - 1));
			break;
		}
	}

	private static String extractText(String line) {
		return line.substring(line.indexOf(":") + 2, line.length());
	}

	private static void watsonNLP(BufferedWriter fout, File file, List<String> text) throws Exception {
		Map<String, String> maps = convertToTextMap(text);
		List<String> userList = new LinkedList<>();
		List<String> textList = new LinkedList<>();
		fout.write(file.getName());

		for (Entry<String, String> e : maps.entrySet()) {
			userList.add(e.getKey());
			textList.add(e.getValue());
		}

		String[] texts = textList.toArray(new String[textList.size()]);

		String[] users = userList.toArray(new String[textList.size()]);

		List<Utterance> utterances = new LinkedList<>();
		try {
			for (int i = 0; i < texts.length; i++) {
				Utterance utterance = new Utterance.Builder().text(texts[i]).user(users[i]).build();
				utterances.add(utterance);
			}
			ToneChatOptions toneChatOptions = new ToneChatOptions.Builder().utterances(utterances).build();
			UtteranceAnalyses utterancesTone = service.toneChat(toneChatOptions).execute();

			fout.write(utterancesTone.toString());
			//System.out.println(utterancesTone);
			fout.newLine();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			fout.flush();
		}
		fout.close();
	}

	private static String extractUser(String text) {
		return text.substring(0, text.indexOf("[") - 1);
	}

	private static Map<String, String> convertToTextMap(List<String> text) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (String s : text) {
			if (s != null || !s.equals(""))
				map.put(extractUser(s), extractText(s));
		}
		return map;
	}

	private static void recursiveLearning(BufferedWriter fout, File file, List<String> chatList) throws Exception {
		// TODO Auto-generated method stub
		for (String s : chatList) {
			StringBuilder sb = new StringBuilder();
			String text = extractText(s);
			int i = findSentiment(text);
			sb.append(i + "=");
			if (i == 0) {
				sb.append("Very Negative");
			} else if (i == 1) {
				sb.append("Negative");
			} else if (i == 2) {
				sb.append("Neutral");
			} else if (i == 3) {
				sb.append("Positive");
			} else {
				sb.append("Very Positive");
			}
			sb.append("=" + text + "\n");
			fout.append(sb.toString());
			// System.out.println(i);
		
			sb = null;
			fout.flush();
		}
		
		fout.close();
	}

	// Input parameters: Input Output
	private static void getFile(String folders) throws Exception {
		File directory = new File(folders);
		// TODO
		if (directory.isDirectory()) {
			File[] files = directory.listFiles();
			for (File file : files) {
				fileList.add(file);
			}
		}
	}

	public static void NLPLearn(BufferedWriter fout, File file, List<String> chatList) throws Exception {
		for (String s : chatList) {
			Annotation annotation = pipeline.process(extractText(s));
			List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

			for (CoreMap sentence : sentences) {
				String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
				fout.append(file + ",=," + sentiment + ",=," + sentence+"\n");
				//System.out.println(file + "," + sentiment + "," + sentence);
			}
			
		}
		fout.flush();
		fout.close();
	}

	public static void stanfordNLPinit() {

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment");
		pipeline = new StanfordCoreNLP(props);
	}

	public static int findSentiment(String text) {

		int mainSentiment = 0;
		if (text != null && text.length() > 0) {
			int longest = 0;
			Annotation annotation = pipeline.process(text);
			for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
				Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
				int sentiment = RNNCoreAnnotations.getPredictedClass(tree);

				String partText = sentence.toString();
				if (partText.length() > longest) {
					mainSentiment = sentiment;
					longest = partText.length();
				}

			}
		}
		return mainSentiment;
	}

}
