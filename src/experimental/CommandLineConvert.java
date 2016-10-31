package experimental;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.json.JSONObject;

import datacollect.NSSvmFormatter;
import datacollect.NSTransform;

public class CommandLineConvert{

	private static final boolean USE_COMMAND_LINE = true;
	private static final boolean WINDOWS = true;
	private static final boolean DELETE_TEMP = false;
	private static final boolean SUPPRESS_DEBUG_OUTPUT = true;
	private static final int PORT = 13854; //13854;
	private static final boolean VIRTUAL_NEUROSKY = false;
	private static final boolean PRINT_ON_CLOSE = false;
	private static final boolean SUPPRESS_STANDARD_OUTPUT = false;
	private static final boolean SUPPRESS_ERROR_OUTPUT = false;

	public static class BackgroundListener implements Closeable{

		//If you want to set a max limit size queue size,
		//all additional data will be discarded.
		//This is useful if processing is slower than input data.
		public BlockingQueue<List<String>> queue =
				new LinkedBlockingQueue<List<String>>();

		public Map<Integer, List<String>> data =
				new ConcurrentSkipListMap<Integer, List<String>>();

		private boolean closed = false;
		private Thread thread;
		private ExecutorService service;
		private Random random = new Random();
		private long processID = random.nextLong();
		private AtomicInteger runCount = new AtomicInteger();
		public DataAnalysisResult analysis = new DataAnalysisResult();
		private String modelName;

		private List<PredictionListener> listeners =
				new ArrayList<PredictionListener>();

		public BackgroundListener(){

		}

		public BackgroundListener(ExecutorService executor){
			service = executor;
		}

		public BackgroundListener(String modelName){
			this.modelName = modelName;
		}

		public BackgroundListener(String modelName, ExecutorService executor){
			this.modelName = modelName;
			service = executor;
		}

		public void startListening(){
			Runnable inner = () -> {
				try{
					while(!closed){
						List<String> toBeProcessed = queue.take();
						process(toBeProcessed);
					}
				}
				catch(InterruptedException e){
					printerr("Blocking interrupted, moving on!");
				}
				catch(Exception e){
					e.printStackTrace();
				}
			};
			if(service == null){
				thread = new Thread(inner);
				thread.start();
			}
			else{
				service.execute(inner);
			}
		}

		private static final int BUFFER_SIZE = 4;

		private List<List<String>> buffer = new ArrayList<List<String>>();

		public void process(List<String> current)
				throws IOException, InterruptedException{
			boolean log = false;
			boolean bin = true;
			String s;

			buffer.add(current);
			if(buffer.size() >= BUFFER_SIZE){
				NSTransform nst =
						new NSTransform(new ArrayList<List<String>>(buffer));
				buffer.clear();
				nst.transform();
				nst.average(NSTransform.DEFAULT_K);
				if(log){
					nst.modifiedLogBin();
				}
				else{
					nst.linearBin();
				}
				if(bin){
					s = String.join("\n", NSSvmFormatter.formatBinned(nst));
				}
				else{
					s = String.join("\n", NSSvmFormatter.format(nst));
				}
				/*List<Integer> data = NSTransform.parse(current);
				double[] fft = NSTransform.fftSingle(data, data.size());
				for(int i = 0; i < fft.length; i++){
					fft[i] = Math.log10(fft[i]);
				}
				NSTransform.linearBin(fft, 60);
				String s = NSSvmFormatter.formatData(fft);*/
				if(USE_COMMAND_LINE){
					processSVMCommandLine(modelName, s);
				}
				else{
					processSVMJava(s);
				}
			}
		}

		public void processSVMJava(String svmData){

		}

		public void processSVMCommandLine(String name, String svmData)
				throws IOException, InterruptedException{
			processSVMCommandLine(name, null, null, svmData);
		}
		
		int twosInARow = 0;
		boolean enableCalibration = true;

		public void processSVMCommandLine(String name, String label,
				String expected, String svmData)
						throws IOException, InterruptedException{
			int currentRun = runCount.getAndIncrement();
			String modelName = name + ".model";
			String range = name + ".range";
			String testName = "test_" + currentRun;
			String testNameScaled = testName + "_scaled";
			String testNamePredicted = testName + "_predicted";
			try{
				if(!SUPPRESS_DEBUG_OUTPUT){
					println("Initiating run " + currentRun + "...");
				}
				File svm = new File("svm");
				svm.mkdirs();
				BufferedWriter bw = null;
				if(!SUPPRESS_DEBUG_OUTPUT){
					println("Writing output...");
				}
				try{
					bw = new BufferedWriter(new FileWriter(
							new File("svm" + File.separator + testName)));
					bw.write(svmData);
				}
				finally{
					if(bw != null){
						bw.close();
					}
				}
				if(!SUPPRESS_DEBUG_OUTPUT){
					System.out.println("Start scaling...");
				}
				Runtime runtime = Runtime.getRuntime();
				String svmScale;
				if(WINDOWS){
					svmScale = System.getProperty("user.dir")
							+ "\\svm\\svm-scale.exe";
				}
				else{
					svmScale = "./svm-scale";
				}
				Process process = runtime.exec(
						svmScale + " -r " + range + " " + testName, null, svm);
				if(!SUPPRESS_DEBUG_OUTPUT){
					println("Reading scaling...");
				}
				StringBuilder sb = new StringBuilder();
				BufferedReader bry = new BufferedReader(
						new InputStreamReader(process.getInputStream()));
				while(true){
					int i = bry.read();
					if(i >= 0){
						sb.append((char) i);
						//System.out.print((char) i);
					}
					else{
						break;
					}
				}
				process.waitFor();
				if(!SUPPRESS_DEBUG_OUTPUT){
					println("Done scaling, exit code: " + process.exitValue());
				}
				if(process.getErrorStream().available() > 0)

				{
					printerr("Scaling errors: ");
					BufferedReader brx = new BufferedReader(
							new InputStreamReader(process.getErrorStream()));
					while(brx.ready()){
						int i = brx.read();
						if(i >= 0){
							printerrchar((char) i);
						}
					}
					printerr();
				}

				if(!SUPPRESS_DEBUG_OUTPUT){
					println("Writing scaling...");
				}
				String scaled = sb.toString();
				BufferedWriter bw2 = new BufferedWriter(new FileWriter(
						new File("svm" + File.separator + testNameScaled)));
				try

				{
					bw2.write(scaled);
				}
				finally

				{
					if(bw2 != null){
						bw2.close();
					}
				}
				String svmPredict;
				if(WINDOWS){
					svmPredict = System.getProperty("user.dir")
							+ "\\svm\\svm-predict.exe";
				}
				else{
					svmPredict = "./svm-predict";
				}
				if(!SUPPRESS_DEBUG_OUTPUT){
					println("Start predicting...");
				}
				Process process2 =
						runtime.exec(
								svmPredict + " " + testNameScaled + " "
										+ modelName + " " + testNamePredicted,
								null, svm);
				process2.waitFor();
				if(!SUPPRESS_DEBUG_OUTPUT){
					println("Done predicting, exit code: "
							+ process2.exitValue());
				}
				if(process2.getErrorStream().available() > 0)

				{
					printerr("Predicting errors: ");
					BufferedReader brx = new BufferedReader(
							new InputStreamReader(process2.getErrorStream()));
					while(brx.ready()){
						int i = brx.read();
						if(i >= 0){
							printerrchar((char) i);
						}
					}
					printerr();
				}

				if(!SUPPRESS_DEBUG_OUTPUT){
					println("Reading prediction results...");
				}
				BufferedReader br = new BufferedReader(new FileReader(
						new File("svm" + File.separator + testNamePredicted)));
				List<String> predictedInput = new ArrayList<String>();
				try

				{
					String s = br.readLine();
					while(s != null){
						predictedInput.add(s);
						s = br.readLine();
					}
				}
				finally

				{
					if(br != null){
						br.close();
					}
				}
				if(enableCalibration){
					if(twosInARow >= 0){
						System.out.println("(Calibrating: " + predictedInput + ")");
						if(predictedInput.stream().filter(s -> s.equals("2")).count() 
								== predictedInput.size()){	
							twosInARow++;
						}
						else{
							twosInARow = 0;
						}
						if(twosInARow < 3){
							return;
						}
						else{
							twosInARow = -1;
						}
					}
				}
				//Replace this with whatever you want
				if(label != null){
					println("(Run " + currentRun + ") " + label
							+ predictedInput);
				}
				else{
					println(currentRun + ": " + predictedInput);
				}
				if(expected != null){
					analysis.addRun(
							new DataAnalysisRun(expected, predictedInput));
				}

				data.put(currentRun, predictedInput);
				for(PredictionListener listener : listeners){
					listener.inputPredicted(currentRun, predictedInput);
				}
				if(!SUPPRESS_DEBUG_OUTPUT){
					println("Done!");
				}

			}
			finally{
				if(DELETE_TEMP){
					new File("svm" + File.separator + testName).delete();
					new File("svm" + File.separator + testNameScaled).delete();
					new File("svm" + File.separator + testNamePredicted)
							.delete();
				}
			}
		}

		public BlockingQueue<List<String>> getBlockingQueue(){
			return queue;
		}

		@Override
		public void close(){
			if(PRINT_ON_CLOSE){
				for(List<String> list : data.values()){
					for(String s : list){
						System.out.print(s + ",");
					}
				}
				System.out.println();
			}
			closed = true;
			if(service == null){
				if(thread != null){
					thread.interrupt();
				}
			}
			else{
				service.shutdownNow();
			}
		}

		public static class DataAnalysisRun{
			private String expected;
			private List<String> result;

			public DataAnalysisRun(String expected, List<String> result){
				this.expected = expected;
				this.result = result;
			}

			public String getExpected(){
				return expected;
			}

			public List<String> getResult(){
				return result;
			}
		}

		public static class DataAnalysisResult{
			public Map<String, List<DataAnalysisRun>> runs =
					new HashMap<String, List<DataAnalysisRun>>();

			public void addRun(DataAnalysisRun run){
				List<DataAnalysisRun> list = runs.get(run.expected);
				if(list == null){
					list = new ArrayList<DataAnalysisRun>();
					runs.put(run.expected, list);
				}
				list.add(run);
			}

			//Analysis schemes (for now):
			//Found (correct if any is correct)
			//Majority (correct if mode is correct)
			//More than half (correct if more than half are correct)
			//All correct (correct if at least all are correct)

			public double analyze(){
				int total = 0;
				int found = 0;
				int majority = 0;
				int moreThanHalf = 0;
				int all = 0;
				for(Map.Entry<String, List<DataAnalysisRun>> entry : runs
						.entrySet()){
					int currTotal = 0;
					int currFound = 0;
					int currMajority = 0;
					int currMoreThanHalf = 0;
					int currAll = 0;
					for(DataAnalysisRun run : entry.getValue()){
						int size = run.getResult().size();
						total++;
						currTotal++;
						Map<String, List<String>> map = run.getResult().stream()
								.collect(Collectors.groupingBy(s -> s));
						List<Map.Entry<String, Integer>> list = map.entrySet()
								.stream()
								.map(en -> new AbstractMap.SimpleImmutableEntry<String, Integer>(
										en.getKey(), en.getValue().size()))
								.collect(Collectors.toList());
						list.sort((x, y) -> -Integer.compare(x.getValue(),
								y.getValue()));
						if(list.stream().anyMatch(
								en -> en.getKey().equals(entry.getKey()))){
							found++;
							currFound++;
							Map.Entry<String, Integer> maxEntry = list.get(0);
							if(maxEntry.getKey().equals(entry.getKey())){
								majority++;
								currMajority++;
								if(maxEntry.getValue() > size / 2){
									moreThanHalf++;
									currMoreThanHalf++;
									if(maxEntry.getValue() == size){
										all++;
										currAll++;
									}
								}
							}
						}
					}
					println(entry.getKey() + " found: "
							+ formatPercent(currFound, currTotal));
					println(entry.getKey() + " majority: "
							+ formatPercent(currMajority, currTotal));
					println(entry.getKey() + " more than half: "
							+ formatPercent(currMoreThanHalf, currTotal));
					println(entry.getKey() + " all correct: "
							+ formatPercent(currAll, currTotal));
				}
				println("Overall found: " + formatPercent(found, total));
				println("Overall majority: " + formatPercent(majority, total));
				println("Overall more than half: "
						+ formatPercent(moreThanHalf, total));
				println("Overall all correct: " + formatPercent(all, total));
				return majority / (double) total;
			}

			private static String formatPercent(int numerator, int denominator){
				return round((double) numerator / denominator * 100, 2) + "% ("
						+ numerator + "/" + denominator + ")";
			}

			private static double round(double d, int places){
				return Math.floor(d * Math.pow(10, places) + 0.5)
						/ Math.pow(10, places);
			}
		}

		public void addPredictionListener(PredictionListener pl){
			listeners.add(pl);
		}

		public void removePredictionListener(PredictionListener pl){
			listeners.remove(pl);
		}

		public void clearPredictionListeners(){
			listeners.clear();
		}
	}

	public interface PredictionListener{
		void inputPredicted(int currentRun, List<String> predictedInput);

		void blinkDetected(int blinkStrength);
	}

	//testing code
	public static void test(String[] args){
		BackgroundListener listener = new BackgroundListener();
		try{
			listener.processSVMCommandLine("test",
					Files.lines(Paths.get("/Users/mfeng17/Downloads/test_1"))
							.reduce("", (s1, s2) -> s1.length() == 0 ? s2
									: s1 + "\n" + s2));
		}
		catch(IOException | InterruptedException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		listener.close();
	}

	public static void startVirtualNeurosky(BackgroundListener listener){
		Thread thread = new Thread(() -> {
			try{
				hostVirtual(listener, "20160808142331");
			}
			catch(Exception e){
				e.printStackTrace();
			}
		});
		thread.start();
	}

	public static void hostVirtual(BackgroundListener listener, String dataset)
			throws IOException{
		ServerSocket server = new ServerSocket(PORT);
		Socket client = server.accept();
		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(client.getOutputStream()));
		try{
			String datasetDir = "neurosky" + File.separator + dataset;
			for(int i = 1; i <= 10; i++){
				for(int j = 1; j <= 20; j++){
					if(!SUPPRESS_DEBUG_OUTPUT){
						println(i + ";" + j);
					}
					String currentFile = datasetDir + File.separator + i
							+ File.separator + j + ".json";
					File file = new File(currentFile);
					BufferedReader br =
							new BufferedReader(new FileReader(file));
					try{
						String s = br.readLine();
						while(s != null){
							writer.write(s + "\n");
							writer.flush();
							s = br.readLine();
						}
					}
					finally{
						br.close();
					}
				}
			}
			while(!listener.getBlockingQueue().isEmpty()){
				try{
					Thread.sleep(100);
				}
				catch(InterruptedException e){
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
			}
		}
		finally{
			if(writer != null){
				writer.close();
			}
			if(server != null){
				server.close();
			}
		}
	}

	public static void main(String[] args){
		realTime("serena1", null);
	}

	public static void realTime(String fileName,
			List<PredictionListener> listeners){
		BackgroundListener listener = new BackgroundListener(fileName);
		if(listeners != null){
			for(PredictionListener pl : listeners){
				listener.addPredictionListener(pl);
			}
		}
		if(VIRTUAL_NEUROSKY){
			startVirtualNeurosky(listener);
		}
		Socket socket = null;
		BufferedReader br = null;
		BufferedWriter bw = null;
		try{
			socket = new Socket("127.0.0.1", PORT);
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			br = new BufferedReader(new InputStreamReader(is));
			bw = new BufferedWriter(new OutputStreamWriter(os));
			bw.write("{\"enableRawOutput\":true,\"format\":\"Json\"}");
			bw.flush();
			String line = br.readLine();
			while(!line.startsWith("{\"raw")){
				line = br.readLine();
			}
			listener.startListening();
			while(true){
				int counter = 0;
				final List<String> current = new ArrayList<String>();
				boolean blinked = false;
				while(counter < 256){
					line = br.readLine();
					if(line.startsWith("{\"raw")){
						counter++;
					}
					if(line.startsWith("{\"blinkStrength")){
						JSONObject obj = new JSONObject(line);
						Object o = obj.get("blinkStrength");
						int i = (Integer) o;
						if(listeners != null){
							for(PredictionListener pl : listeners){
								pl.blinkDetected(i);
							}
						}
						System.out.println("Blink: " + i);
						blinked = true;
						/*
						String[] sa = line.split("\\:");
						if(sa.length > 1){
							String num = sa[1];
							try{
								int i = Integer.parseInt(num.substring(0, num.length() - 1));
								if(listeners != null){
									for(PredictionListener pl : listeners){
										pl.blinkDetected(i);
									}
								}
							}
							catch(NumberFormatException e){
								System.out.println(
										"Expected number, got: " + num);
							}
						}*/
					}
					current.add(line);
				}
				if(!blinked){
					//Adds a new item to be processed
					listener.getBlockingQueue().offer(current);
				}
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			listener.close();
		}
	}

	public static void mainpredata(String[] args){
		process("cweak-openclosewinklr-avebinned-new", "_set2", 5, null);
	}

	public static double process(String fileName, String edited, int groupCount,
			List<PredictionListener> listeners){
		BackgroundListener listener = new BackgroundListener();
		if(listeners != null){
			for(PredictionListener pl : listeners){
				listener.addPredictionListener(pl);
			}
		}
		boolean exp = true;
		boolean bin = true;
		boolean log = false;
		int first = 3;
		int second = 3;
		int third = 4;
		int fourth = 4;
		println("[exp:" + exp + ",bin:" + bin + ",log:" + log + "]");
		BufferedReader br = null;
		BufferedWriter bw = null;
		try{
			br = new BufferedReader(
					new FileReader("svm" + File.separator + edited));
			String label = "";
			int count = 0;
			StringBuilder current = new StringBuilder();
			String s = br.readLine();
			while(s != null){
				if(label.equals("")){
					label = s.substring(0, 1);
				}
				current.append(s + "\n");
				count++;
				if(count >= groupCount){
					listener.processSVMCommandLine(fileName,
							"{\"expected\": " + label + "} Classifier: ", label,
							current.toString());
					label = "";
					current = new StringBuilder();
					count = 0;
				}
				s = br.readLine();
			}
			return listener.analysis.analyze();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(br != null){
				try{
					br.close();
				}
				catch(IOException e){
					e.printStackTrace();
				}
			}
			if(bw != null){
				try{
					bw.close();
				}
				catch(IOException e){
					e.printStackTrace();
				}
			}
			listener.close();
		}
		return -1;
	}

	//Automatically test data with simple analysis
	public static void mainy(String[] args){
		BackgroundListener listener = new BackgroundListener();
		String fileName = "cweak-openclosewinklr-avebinned";
		boolean exp = true;
		boolean bin = true;
		boolean log = false;
		int first = 3;
		int second = 3;
		int third = 4;
		int fourth = 4;
		println("[exp:" + exp + ",bin:" + bin + ",log:" + log + "]");
		BufferedReader br = null;
		BufferedWriter bw = null;
		try{
			/*if(!exp){
				br = new BufferedReader(
						new FileReader(new File("data/neurosky/catalog.dat")));
			}
			else{
				br = new BufferedReader(new FileReader(
						new File("data/neurosky/experimental/catalog.dat")));
			}
			String line = br.readLine();
			JSONObject catalog = new JSONObject(line);
			String[] folders = JSONObject.getNames(catalog);
			List<String[]> toWrite = new ArrayList<String[]>();
			println(Arrays.toString(folders));*/
			//File file = new File("neurosky");
			File file = new File("data" + File.separator + "neurosky"
					+ File.separator + "experimental");
			File[] subdirs = file.listFiles();
			for(File subdir : subdirs){
				if(!subdir.isDirectory()){
					continue;
				}
				int cycles = 10;
				for(int i = 1; i <= cycles; i++){
					String folderLocation =
							subdir.getPath() + File.separator + i;
					NSTransform nst = new NSTransform(folderLocation);
					if(nst.getLabel() != first && nst.getLabel() != second
							&& nst.getLabel() != third
							&& nst.getLabel() != fourth){
						continue;
					}
					nst.transform();
					nst.average(NSTransform.DEFAULT_K);
					if(log){
						nst.modifiedLogBin();
					}
					else{
						nst.linearBin();
					}
					String[] output;
					// println("transformed!");
					if(bin){
						output = NSSvmFormatter.formatBinned(nst);
					}
					else{
						output = NSSvmFormatter.format(nst);
					}
					StringBuilder out = new StringBuilder();
					for(String s1 : output){
						out.append(s1 + "\n");
					}
					println(out);
					println();
					println();
					listener.processSVMCommandLine(fileName,
							"{\"directory\": " + subdir.getName()
									+ ", \"subdirectory\": " + i
									+ ", \"expected\": " + nst.getLabel()
									+ "} Classifier: ",
							String.valueOf(nst.getLabel()), out.toString());
				}
			}
			/*bw = new BufferedWriter(
					new FileWriter(new File("svm/" + fileName)));
			for(String[] set : toWrite){
				for(String s : set){
					bw.write(s);
					bw.newLine();
				}
			}
			bw.close();
			Process process = Runtime.getRuntime()
					.exec("python easy.py " + fileName, null, new File("svm"));
			br = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			line = br.readLine();
			while(!line.startsWith("Output model")){
				line = br.readLine();
				System.out.println(line);
			}*/
			listener.analysis.analyze();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(br != null){
				try{
					br.close();
				}
				catch(IOException e){
					e.printStackTrace();
				}
			}
			if(bw != null){
				try{
					bw.close();
				}
				catch(IOException e){
					e.printStackTrace();
				}
			}
			listener.close();
		}
	}

	private static void println(){
		if(!SUPPRESS_STANDARD_OUTPUT){
			System.out.println();
		}
	}

	private static void println(Object o){
		if(!SUPPRESS_STANDARD_OUTPUT){
			System.out.println(o);
		}
	}

	private static void println(int i){
		if(!SUPPRESS_STANDARD_OUTPUT){
			System.out.println(i);
		}
	}

	private static void println(double d){
		if(!SUPPRESS_STANDARD_OUTPUT){
			System.out.println(d);
		}
	}

	private static void println(boolean b){
		if(!SUPPRESS_STANDARD_OUTPUT){
			System.out.println(b);
		}
	}

	private static void println(long l){
		if(!SUPPRESS_STANDARD_OUTPUT){
			System.out.println(l);
		}
	}

	private static void printerr(Object o){
		if(!SUPPRESS_ERROR_OUTPUT){
			System.out.println(o);
		}
	}

	private static void printerr(int i){
		if(!SUPPRESS_ERROR_OUTPUT){
			System.out.println(i);
		}
	}

	private static void printerr(double d){
		if(!SUPPRESS_ERROR_OUTPUT){
			System.out.println(d);
		}
	}

	private static void printerr(boolean b){
		if(!SUPPRESS_ERROR_OUTPUT){
			System.out.println(b);
		}
	}

	private static void printerr(long l){
		if(!SUPPRESS_ERROR_OUTPUT){
			System.out.println(l);
		}
	}

	private static void printerr(){
		if(!SUPPRESS_ERROR_OUTPUT){
			System.out.println();
		}
	}

	private static void printerrchar(char c){
		if(!SUPPRESS_ERROR_OUTPUT){
			System.out.print(c);
		}
	}
}
