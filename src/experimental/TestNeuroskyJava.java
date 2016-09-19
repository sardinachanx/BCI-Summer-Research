package experimental;

public class TestNeuroskyJava{

	public static void main(String[] args){

		PrintWriter writeFile = null;
		Socket socket = null;

		try{
			socket = new Socket("127.0.0.1", 13854);
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
			writeFile = new PrintWriter(new FileWriter(new File("test.txt")));

			bw.write("{\"enableRawOutput\":true,\"format\":\"Json\"}\n");
			bw.flush();
			System.out.println("wrote preferences");

			// File file = new File("test.txt");
			// Scanner scan = new Scanner(System.in);
			// String c = scan.next();
			/*
			 * while(true){ //System.out.print('a'); String line =
			 * br.readLine(); System.out.println(line); Thread.sleep(500);
			 * //Thread.sleep(100); //writeFile.println(line); }
			 */
			/*bw.write("{\"startRecording\":{\"rawEeg\":true,\"poorSignalLevel\":true,\"eSense\":true,"
					+ "eegPower\":true,\"blinkStrength\":true},\"applicationName\":\"ExampleApp\"}");
			bw.flush();*/

			String line = br.readLine();
			while(line.startsWith("{\"poorSignalLevel\":200,")){
				line = br.readLine();
			}

			System.out.println("Start Recording:");

			Thread.sleep(10000);
			bw.write("{\"stopRecording\":\"ExampleApp\"}");
			bw.flush();
			System.out.println("End Recording");

			bw.write("{\"getSessionIds\":\"ExampleApp\"}");

			while(true){
				System.out.print(br.read());
			}

		}
		catch(UnknownHostException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(InterruptedException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		finally{
			if(writeFile != null){
				writeFile.close();
			}
			if(socket != null){
				try{
					socket.close();
				}
				catch(IOException e){
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
