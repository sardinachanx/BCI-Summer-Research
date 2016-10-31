package experimental;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import datacollect.DataConstants;
import datacollect.NSSvmFormatter;
import datacollect.NSTransform;

public class TestSVM{

	public static void main(String[] args){
		String fileName = "serena1";
		boolean exp = true;
		boolean bin = true;
		boolean log = false;

		int first = 2;
		int second = 3;
		int third = 4;
		int fourth = 5;

		System.out.println("[exp:" + exp + ",bin:" + bin + ",log:" + log + "]");
		System.out.println("[" + DataConstants.NAMES[first] + ","
				+ DataConstants.NAMES[second] + "," + DataConstants.NAMES[third]
				+ "," + DataConstants.NAMES[fourth] + "]");

		BufferedReader br = null;
		BufferedWriter bw = null;
		try{
			if(!exp){
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
			System.out.println(Arrays.toString(folders));
			for(String s : folders){
				int cycles = catalog.getInt(s);
				for(int i = 1; i <= cycles; i++){
					String folderLocation;
					if(exp){
						folderLocation = "data/neurosky/experimental/" + s
								+ File.separator + i;
					}
					else{
						folderLocation =
								"data/neurosky/" + s + File.separator + i;
					}
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
					// System.out.println("transformed!");
					if(bin){
						toWrite.add(NSSvmFormatter.formatBinned(nst));
					}
					else{
						toWrite.add(NSSvmFormatter.format(nst));
					}
				}
			}
			bw = new BufferedWriter(
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
			while(line != null && !line.startsWith("Output model")){
				System.out.println(line);
				line = br.readLine();
			}
		}
		catch(IOException e){
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
		}
		System.out.println("Done!");
	}
}
