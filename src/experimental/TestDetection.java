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
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import datacollect.NSSvmFormatter;
import datacollect.NSTransform;

public class TestDetection{

	private static final boolean USE_COMMAND_LINE = true;
	private static final boolean WINDOWS = false;

	public static class BackgroundListener implements Closeable{

		// If you want to set a max limit size queue size,
		// all additional data will be discarded.
		// This is useful if processing is slower than input data.
		public BlockingQueue<List<String>> queue = new LinkedBlockingQueue<List<String>>();

		private boolean closed = false;
		private Thread thread;
		private ExecutorService service;
		private Random random = new Random();
		private long processID = random.nextLong();
		private AtomicInteger runCount = new AtomicInteger();

		public BackgroundListener(){

		}

		public BackgroundListener(ExecutorService executor){
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
					System.out.println("Blocking interrupted, moving on!");
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

		public void process(List<String> current) throws IOException, InterruptedException{
			List<Integer> data = NSTransform.parse(current);
			if(data == null){
				return;
			}
			double[] fft = NSTransform.fftSingle(data, data.size());
			for(int i = 0; i < fft.length; i++){
				fft[i] = Math.log10(fft[i]);
			}
			fft = NSTransform.linearBin(fft, 60);
			String s = NSSvmFormatter.formatTestData(fft);
			if(USE_COMMAND_LINE){
				processSVMCommandLine(s);
			}
			else{
				processSVMJava(s);
			}
		}

		public void processSVMJava(String svmData){

		}

		public void processSVMCommandLine(String svmData) throws IOException, InterruptedException{
			int currentRun = runCount.getAndIncrement();
			System.out.println("Initiating run " + currentRun + "...");
			File svm = new File("svm");
			svm.mkdirs();
			BufferedWriter bw = null;
			String modelName = "test.model";
			String testName = "test_" + currentRun;
			String range = "test.range";
			String testNameScaled = testName + "_scaled";
			String testNamePredicted = testName + "_predicted";
			//System.out.println("Writing output...");
			try{
				bw = new BufferedWriter(new FileWriter(new File("svm" + File.separator + testName)));
				bw.write(svmData);
			}
			finally{
				if(bw != null){
					bw.close();
				}
			}
			//System.out.println("Start scaling...");
			String svmScale;
			if(WINDOWS){
				svmScale = "C:\\Users\\CS\\Documents\\BCI-Summer-Research\\svm\\svm-scale.exe";
			}
			else{
				svmScale = "./svm-scale";
			}
			Process process = Runtime.getRuntime().exec(svmScale + " -r " + range + " " + testName, null, svm);
			//System.out.println("Reading scaling...");
			StringBuilder sb = new StringBuilder();
			BufferedReader bry = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while(true){
				int i = bry.read();
				if(i >= 0){
					sb.append((char) i);
					// System.out.print((char) i);
				}
				else{
					break;
				}
			}
			process.waitFor();
			//System.out.println("Done scaling, exit code: " + process.exitValue());
			if(process.getErrorStream().available() > 0){
				System.out.println("Scaling errors: ");
				BufferedReader brx = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				while(brx.ready()){
					int i = brx.read();
					if(i >= 0){
						System.out.print((char) i);
					}
				}
				System.out.println();
			}

			//System.out.println("Writing scaling...");
			String scaled = sb.toString();
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File("svm" + File.separator + testNameScaled)));
			try{
				bw2.write(scaled);
			}
			finally{
				if(bw2 != null){
					bw2.close();
				}
			}
			String svmPredict;
			if(WINDOWS){
				svmPredict = "C:\\Users\\CS\\Documents\\BCI-Summer-Research\\svm\\svm-predict.exe";
			}
			else{
				svmPredict = "./svm-predict";
			}
			//System.out.println("Start predicting...");
			Process process2 = Runtime.getRuntime()
					.exec(svmPredict + " " + testName + " " + modelName + " " + testNamePredicted, null, svm);
			process2.waitFor();
			//System.out.println("Done predicting, exit code: " + process2.exitValue());
			if(process2.getErrorStream().available() > 0){
				System.out.println("Predicting errors: ");
				BufferedReader brx = new BufferedReader(new InputStreamReader(process2.getErrorStream()));
				while(brx.ready()){
					int i = brx.read();
					if(i >= 0){
						System.out.print((char) i);
					}
				}
				System.out.println();
			}

			//System.out.println("Reading prediction results...");
			BufferedReader br = new BufferedReader(
					new FileReader(new File("svm" + File.separator + testNamePredicted)));
			List<String> predictedInput = new ArrayList<String>();
			try{
				String s = br.readLine();
				while(s != null){
					predictedInput.add(s);
					s = br.readLine();
				}
			}
			finally{
				if(br != null){
					br.close();
				}
			}
			// Replace this with whatever you want
			System.out.println(predictedInput);
			for(String s : predictedInput){
				if(s.equals("2")){
					System.out.println("Detected: close eye");
				}
				else if(s.equals("4")){
					System.out.println("Detected: left wink");
				}
				else if(s.equals("5")){
					System.out.println("Detected: right wink");
				}
			}
			//System.out.println("Done!");
		}

		public BlockingQueue<List<String>> getBlockingQueue(){
			return queue;
		}

		@Override
		public void close(){
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

	}

	// testing code
	public static void test(String[] args){
		BackgroundListener listener = new BackgroundListener();
		try{
			listener.processSVMCommandLine(Files.lines(Paths.get("/Users/mfeng17/Desktop/a1a")).reduce("",
					(s1, s2) -> s1.length() == 0 ? s2 : s1 + "\n" + s2));
		}
		catch(IOException | InterruptedException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		listener.close();
	}

	public static void main(String[] args){
		BackgroundListener listener = new BackgroundListener();
		Socket socket = null;
		BufferedReader br = null;
		BufferedWriter bw = null;
		try{
			socket = new Socket("127.0.0.1", 13854);
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
			System.out.println("START");
			listener.startListening();
			while(true){
				int counter = 0;
				final List<String> current = new ArrayList<String>();
				while(counter < 256){
					line = br.readLine();
					if(line.startsWith("{\"raw")){
						counter++;
					}
					current.add(line);
				}
				// Adds a new item to be processed
				listener.getBlockingQueue().offer(current);
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			listener.close();
		}
	}

}
