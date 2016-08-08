package datacollect;

import java.util.Arrays;

public class NSDataProcessor {
	
	public static double[] dataBin(double[] data, int numBins){
		double[] newData = new double[numBins];
		int binSize = data.length / numBins;
		for(int i = 0; i < numBins; i++){
			int start = i * binSize;
			int sum = 0;
			for(int a = start; a < binSize; a++){
				sum += data[a];
			}
			sum /= binSize;

			
			newData[i] = sum;
		}
		return newData;
	}
	public static double[] logBin(double[] data, int numBins){
		double[] logBinned = new double[numBins];
		int[] binSizes = numLogBins(numBins, data.length);
		int j = 0;
		for(int i = 0; i < numBins; i++){
			int length = binSizes[i];
			int sum = 0;
			for(int a = j; a < j + length; a++){
				sum += data[a];
			}
			j += length;
			logBinned[i] = (double) sum / length;
		}
		return logBinned;
	}
	
	public static int[] numLogBins(int numBins, int dataLength){
		int minExp = 0;
		double maxExp = Math.log10(dataLength);
		double expBinSize = (maxExp - minExp) / numBins;
		int[] sizeBins = new int[numBins];
		for(int i = 0; i < numBins; i++){
			double currMax = expBinSize * (i + 1);
			double currMin = expBinSize * i;
			int start = (int) Math.pow(10, currMin);
			int end = (int) Math.pow(10, currMax);
			int length = end - start;
			
			sizeBins[i] = length;
		}
		System.out.println(Arrays.toString(sizeBins));
		if(!checkSize(sizeBins, dataLength)){
			throw new IllegalStateException("something's wrong with the bin division");
		}
		return sizeBins;
	}
	
	private static boolean checkSize(int[] array, int expected){
		int sum = 0;
		for(int i : array){
			if(i == 0){
				return false;
			}
			sum += i;
		}
		return expected == sum;
	}
	
	
}
