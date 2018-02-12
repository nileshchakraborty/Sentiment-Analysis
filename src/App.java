

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
				
				watsonNLP(new BufferedWriter(new FileWriter("./Output/outputWatson.txt", true)), file,br);
				Thread.sleep(100);
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

	private static void watsonNLP(BufferedWriter fout, File file, BufferedReader br) throws Exception {
		int count = 0;
		String currentID = null;
		String line = null;
		List<String> textList = new LinkedList<>();
		List<Utterance> utterances = new LinkedList<>();
		//fout.write(file.getName());
		try {
			while((line = br.readLine()) != null) {
				currentID = file.getName().toString().substring(0, file.getName().indexOf("."))+"_"+count;
				String newId = currentID + ": "+ line;	
				textList.add(newId);
				count++;
				Utterance utterance = new Utterance.Builder().text(line).user(currentID).build();
				utterances.add(utterance);
				
				try {
					
					ToneChatOptions toneChatOptions = new ToneChatOptions.Builder().utterances(utterances).build();
					UtteranceAnalyses utterancesTone = service.toneChat(toneChatOptions).execute();

					fout.write(utterancesTone.toString());
					
					fout.newLine();

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					fout.flush();
				}
				utterances.clear();
			}
		}
		catch(Exception e) {
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
