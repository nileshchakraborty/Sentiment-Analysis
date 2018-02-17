
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneChatOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.Utterance;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.UtteranceAnalyses;

public class App {

	private static List<File> fileList = new LinkedList<>();
	private static Map<String, String> headers = new LinkedHashMap<String, String>();
	private static ToneAnalyzer service = null;

	public static void main(String[] args) throws Exception {

		getFile("transcriptions");

		processFiles();
	}

	private static void processFiles() throws Exception {

		watsonInit();
		System.out.println("Start processing files.");
		File dir = new File("Output");
		dir.mkdir();
		long startTime = System.currentTimeMillis();
		for (File file : fileList) {

			try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))) {

				watsonNLP(new BufferedWriter(new FileWriter("./Output/outputWatson.txt", true)), file, br);
				Thread.sleep(100);
				br.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		long endTime = System.currentTimeMillis() - startTime;
		System.out.println("Time to process request: " + endTime);
	}

	private static void watsonInit() {
		headers.put("X-Watson-Learning-Opt-Out", "true");
		service = new ToneAnalyzer("2016-05-19");
		service.setUsernameAndPassword("65aafb00-3460-4c8c-ad72-c685ca60fe21", "61CRipJX1kMN");
		service.setDefaultHeaders(headers);

	}

	static <T> List<List<T>> chopped(List<T> list, final int L) {
		List<List<T>> parts = new LinkedList<List<T>>();
		final int N = list.size();
		for (int i = 0; i < N; i += L) {
			parts.add(new LinkedList<T>(list.subList(i, Math.min(N, i + L))));
		}
		return parts;
	}

	private static void watsonNLP(BufferedWriter fout, File file, BufferedReader br) throws Exception {
		int count = 1;
		String currentID = null;
		String line = null;
		List<String> textList = new LinkedList<>();
		List<Utterance> utterancesList = new LinkedList<>();
		// fout.write(file.getName());
		try {
			while ((line = br.readLine()) != null) {
				// System.out.println(count++);
				currentID = file.getName().toString().substring(0, file.getName().indexOf(".")) + "_" + count;
				String newId = currentID + ": " + line;
				textList.add(newId);
				count++;

				try {
					Utterance utterance = new Utterance.Builder().text(line).user(currentID).build();
					utterancesList.add(utterance);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			try {
				List<List<Utterance>> tempList = chopped(utterancesList, 50);
				for (List<Utterance> l : tempList) {
					ToneChatOptions toneChatOptions = new ToneChatOptions.Builder().utterances(l).build();
					UtteranceAnalyses utterancesTone = service.toneChat(toneChatOptions).execute();
					fout.write(utterancesTone.toString());

					fout.newLine();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				fout.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
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
			System.out.println(fileList.toString());

		}

	}

}
