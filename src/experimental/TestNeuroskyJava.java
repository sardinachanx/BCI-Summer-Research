package experimental;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TestNeuroskyJava {
	
	public static void main(String[]args){
		
		PrintWriter writeFile = null;
		
		try {
			Socket socket = new Socket("127.0.0.1", 13854);
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
			writeFile = new PrintWriter(new FileWriter(new File("test.txt")));
			
			bw.write("{\"enableRawOutput\":true,\"format\":\"Json\"}\n");
			bw.flush();
			System.out.println("wrote preferences");
			
			//File file = new File("test.txt");
			//Scanner scan = new Scanner(System.in);
			//String c = scan.next();
			while(true){
				//System.out.print('a');
				String line = br.readLine();
				System.out.println(line);
				//Thread.sleep(100);
				//writeFile.println(line);
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		finally{
			if(writeFile != null){
				writeFile.close();
			}
		}
	}
}
