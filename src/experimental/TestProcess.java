package experimental;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import datacollect.DataConstants;
import datacollect.NSSvmFormatter;
import datacollect.NSTransform;

public class TestProcess{

	public static void main(String[] args){
		boolean print = false;
		boolean randomTest = false;

		double total = 0;
		double totalSquared = 0;
		int trials = 100;

		Random random = new Random();

		System.out.println("Start!");

		for(int z = 0; z < (randomTest ? trials : 1); z++){
			String fileName = "serena1";
			String[] datasets = new String[]{"20161025000307", "20161025000702",
					"20161025001847", "20161025002100", "20161025002308",
					"20161025002705", "20161025002838", "20161025003108",
					//};
					//String[] datasets = new String[]{
					"20161025033135", "20161025033223", "20161025033319",
					"20161025033539", "20161025033613", "20161025033647",
					"20161025033741", "20161025034022", "20161025034238",
					"20161025034418", "20161025091729"};
			boolean exp = true;
			boolean bin = true;
			boolean log = false;

			int first = 2;
			int second = 3;
			int third = 4;
			int fourth = 5;

			if(print){
				System.out.println(
						"[exp:" + exp + ",bin:" + bin + ",log:" + log + "]");
				System.out.println("[" + DataConstants.NAMES[first] + ","
						+ DataConstants.NAMES[second] + ","
						+ DataConstants.NAMES[third] + ","
						+ DataConstants.NAMES[fourth] + "]");
			}

			BufferedReader br = null;
			BufferedWriter bw = null;
			BufferedWriter bw2 = null;
			List<String[]> toWrite = new ArrayList<String[]>();
			Map<Integer, Integer> labelCount =
					new LinkedHashMap<Integer, Integer>();
			labelCount.put(first, 0);
			labelCount.put(second, 0);
			labelCount.put(third, 0);
			labelCount.put(fourth, 0);
			try{
				for(String s0 : datasets){
					String dir = "data/neurosky/process/" + s0;
					String[] folders = new File(dir).list();
					//System.out.println(Arrays.toString(folders));
					for(String folder : folders){
						if(folder.equals(".DS_Store")){
							continue;
						}
						String[] vals =
								new File(dir + File.separator + folder).list();
						int[] valsInt = Arrays.stream(vals)
								.filter(s -> s.endsWith(".json"))
								.map(s -> s.split("\\.")[0])
								.map(s -> Integer.parseInt(s)).mapToInt(i -> i)
								.toArray();
						if(valsInt.length % 4 != 0){
							int[] newVals = new int[valsInt.length
									- valsInt.length % 4];
							System.arraycopy(valsInt, 0, newVals, 0,
									newVals.length);
							valsInt = newVals;
						}
						//System.out.println(Arrays.toString(vals));
						//for(int json : valsInt){
						String folderLocation = dir + File.separator + folder;
						NSTransform nst =
								new NSTransform(folderLocation, 256, valsInt);
						if(nst.getLabel() != first && nst.getLabel() != second
								&& nst.getLabel() != third
								&& nst.getLabel() != fourth){
							continue;
						}
						if(nst.getLabel() == third){
							continue;
						}
						labelCount.put(nst.getLabel(),
								labelCount.get(nst.getLabel()) + nst.getSize());
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
						//}
					}
				}
				if(print){
					System.out.println("done");
					System.out.println(labelCount);
				}
				bw = new BufferedWriter(
						new FileWriter(new File("svm/" + fileName)));
				bw2 = new BufferedWriter(new FileWriter(
						new File("svm/" + fileName + "_testing")));
				for(String[] set : toWrite){
					for(String s : set){
						if(!randomTest || random.nextDouble() > 0.25){
							bw.write(s);
							bw.newLine();
						}
						else{
							bw2.write(s);
							bw2.newLine();
						}
					}
				}
				bw.close();
				bw2.close();
				Process process = Runtime.getRuntime().exec(
						"python easy.py " + fileName, null, new File("svm"));
				br = new BufferedReader(
						new InputStreamReader(process.getInputStream()));
				String line = br.readLine();
				while(line != null && !line.startsWith("Output model")){
					if(print){
						System.out.println(line);
					}
					line = br.readLine();
				}
				if(randomTest){
					double val = CommandLineConvert.process(fileName,
							fileName + "_testing", 1, null);
					System.out.println(val);
					total += val;
					totalSquared += val * val;
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
				if(bw2 != null){
					try{
						bw2.close();
					}
					catch(IOException e){
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("Done!");
		System.out.println();

		if(randomTest){
			double average = total / trials;
			double averageMinusOne = total / (trials - 1);
			double stddev = 0;
			if(trials > 1){
				stddev = Math.sqrt(
						totalSquared / (trials - 1) - average * averageMinusOne);
			}
			System.out.println(average);
			System.out.println(stddev);
		}
	}
}
