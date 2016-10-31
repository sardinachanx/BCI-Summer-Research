package datacollect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.jtransforms.fft.DoubleFFT_1D;

public class NSTransform{
	
	public static final int DEFAULT_K = 2;

	private static final int DEFAULT_RESOLUTION = 256;

	private int resolution;
	private List<List<Integer>> toTransform = new ArrayList<List<Integer>>();
	private List<double[]> transformed = null;
	private List<double[]> averaged = null;
	private List<double[]> binned = null;
	private int label;
	private int size;

	/*
	 * public NSTransform(String filePath) { this(new File(filePath)); }
	 *
	 * public NSTransform(File file) { this.file = file; }
	 *
	 * public void transformFile() { parseFile(); transform(); }
	 *
	 * private void parseFile() { BufferedReader br = null; try { br = new
	 * BufferedReader(new FileReader(file)); String line = br.readLine();
	 * List<Integer> current = new ArrayList<Integer>(); line = br.readLine();
	 * while (line != null) { if (line.startsWith("===")) {
	 * toTransform.add(current); current = new ArrayList<Integer>(); String s =
	 * line.replaceAll("=", ""); String[] split = s.split(" |\\(|\\)");
	 * labels.add(Integer.parseInt(split[3])); } else { JSONObject o = new
	 * JSONObject(line); if (o.has("rawEeg")) { current.add(o.getInt("rawEeg"));
	 * } } line = br.readLine(); } } catch (IOException e) {
	 * e.printStackTrace(); } finally { if (br != null) { try { br.close(); }
	 * catch (IOException e) { e.printStackTrace(); } } } }
	 */
	public NSTransform(String folderLocation){
		this(folderLocation, DEFAULT_RESOLUTION);
	}

	public NSTransform(String folderLocation, int resolution){
		this(folderLocation, resolution, null);
	}

	public NSTransform(String folderLocation, int resolution, int[] vals){
		this.resolution = resolution;
		BufferedReader br = null;
		if(vals == null){
			vals = new int[20];
			for(int i = 0; i < 20; i++){
				vals[i] = i + 1;
			}
		}
		size = vals.length;
		for(int i : vals){
			File file = new File(folderLocation + File.separator + i + ".json");
			List<Integer> current = new ArrayList<Integer>();
			try{
				br = new BufferedReader(new FileReader(file));
				String line = br.readLine();
				while(line != null){
					JSONObject o = new JSONObject(line);
					if(o.has("rawEeg")){
						current.add(o.getInt("rawEeg"));
					}
					line = br.readLine();
				}
				toTransform.add(current);
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
			}
		}
		File file = new File(folderLocation + File.separator + "name.txt");
		try{
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			String[] split = line.split(" ");
			String s = split[1].substring(1, 2);
			label = Integer.parseInt(s);

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
		}
	}

	public NSTransform(List<List<String>> fromRaw){
		for(List<String> single : fromRaw){
			toTransform.add(parse(single));
		}
		resolution = toTransform.get(0).size();
	}

	public static List<Integer> parse(List<String> raw){
		List<Integer> data = new ArrayList<Integer>();
		for(String line : raw){
			JSONObject o = new JSONObject(line);
			if(o.has("rawEeg")){
				data.add(o.getInt("rawEeg"));
			}
		}
		return data;
	}

	public int getLabel(){
		return label;
	}

	public List<List<Integer>> getToTransform(){
		return toTransform;
	}

	public List<double[]> getTransformed(){
		return transformed;
	}

	public void transform(){
		transformed = new ArrayList<double[]>();
		for(List<Integer> list : toTransform){
			transformed.add(fftSingle(list, resolution));
		}
	}

	@Deprecated
	public static double[] fftSingleOld(List<Integer> list){
		double[] toTransform = toDoubleArray(list);
		DoubleFFT_1D fft = new DoubleFFT_1D(toTransform.length);
		double[] transformed = new double[toTransform.length * 2];
		System.arraycopy(toTransform, 0, transformed, 0, toTransform.length);
		fft.realForwardFull(transformed);
		return transformed;
	}

	public static double[] fftSingle(List<Integer> list, int resolution){
		double[] toTransform = toDoubleArray(list);
		return fftSingle(toTransform, resolution);
	}

	public static double[] fftSingle(double[] toTransform, int resolution){
		DoubleFFT_1D fft = new DoubleFFT_1D(resolution);
		double[] transformed = new double[toTransform.length];
		System.arraycopy(toTransform, 0, transformed, 0, toTransform.length);
		fft.realForward(transformed);
		double[] realTransformed = new double[resolution / 2];
		for(int i = 0; i < resolution / 2; i++){
			realTransformed[i] = transformed[2 * i];
		}
		for(int i = 0; i < realTransformed.length; i++){
			realTransformed[i] = realTransformed[i] * realTransformed[i];
		}
		return realTransformed;

	}

	public static double[] toDoubleArray(List<Integer> list){
		double[] array = new double[list.size()];
		for(int i = 0; i < list.size(); i++){
			array[i] = list.get(i);
		}
		return array;
	}

	public void average(int k){
		averaged = kAverage(transformed, k);
	}

	public static List<double[]> kAverage(List<double[]> list, int k){
		List<double[]> toReturn = new ArrayList<double[]>();
		for(int i = 0; i < list.size(); i += k){
			double[] summed = new double[list.get(i).length];
			for(int j = i; j < i + k; j++){
				double[] current = list.get(j);
				//System.out.println(j + ": " + Arrays.toString(current));
				for(int a = 0; a < summed.length; a++){
					summed[a] += current[a];
				}
			}
			for(int j = 0; j < summed.length; j++){
				summed[j] /= k;
				summed[j] = Math.log10(summed[j]);
			}
			//System.out.println(Arrays.toString(summed));
			toReturn.add(summed);
		}
		return toReturn;
	}

	public List<double[]> getAveraged(){
		return averaged;
	}

	public void linearBin(){
		binned = new ArrayList<double[]>();
		for(double[] average : averaged){
			binned.add(linearBin(average, 60));
		}
	}

	public void modifiedLogBin(){
		binned = new ArrayList<double[]>();
		for(double[] average : averaged){
			binned.add(modifiedLogBin(average, 60));
		}
	}

	public List<double[]> getBinned(){
		return binned;
	}

	public void setResolution(int resolution){
		this.resolution = resolution;
	}

	public void skipAverage(){
		averaged = transformed;
	}

	@Deprecated
	public static double[][] linearBinOld(double[] data, int b){
		double[][] dataWL = new double[2][b];
		int dataLength = data.length;
		float binSize = (float) dataLength / b;
		for(int i = 0; i < b; i++){
			float min = binSize * i;
			float max = binSize * (i + 1);
			int start = (int) min;
			if(start != min){
				start += 1;
			}
			int end = (int) max;
			if(end != max){
				end += 1;
			}
			double sum = 0;
			for(int a = start; a < end; a++){
				sum += data[a];
			}
			sum /= end - start;
			double midPoint = (max + min) / 2;
			dataWL[0][i] = sum;
			dataWL[1][i] = midPoint;
		}

		return dataWL;
	}

	public static double[] linearBin(double[] data, int b){
		double[] binnedData = new double[b];
		int dataLength = data.length;
		float binSize = (float) dataLength / b;
		for(int i = 0; i < b; i++){
			float min = binSize * i;
			float max = binSize * (i + 1);
			int start = (int) min;
			if(start != min){
				start += 1;
			}
			int end = (int) max;
			if(end != max){
				end += 1;
			}
			double sum = 0;
			for(int a = start; a < end; a++){
				sum += data[a];
			}
			sum /= end - start;
			binnedData[i] = sum;
		}
		return binnedData;
	}

	@Deprecated
	public static double[][] modifiedLogBinOld(double[] data, int b){
		double[][] dataWL = new double[2][b];
		for(int i = 0; i < 40; i++){
			dataWL[0][i] = data[i];
			dataWL[1][i] = i;
		}
		b -= 40;
		double max = Math.log10(data.length);
		double min = Math.log10(40);
		double expLogBinSize = (max - min) / b;
		for(int i = 0; i < b; i++){
			double cExpMin = min + expLogBinSize * i;
			double cExpMax = min + expLogBinSize * (i + 1);
			double cMin = Math.pow(10, cExpMin);
			double cMax = Math.pow(10, cExpMax);
			int start = (int) cMin;
			if(cMin != start){
				start += 1;
			}
			int end = (int) cMax;
			if(cMax != end){
				end += 1;
			}
			double sum = 0;
			for(int a = start; a < end; a++){
				sum += data[a];
			}
			sum /= end - start;
			double midPoint = (cMin - cMax) / 2;
			dataWL[0][i] = sum;
			dataWL[1][i] = midPoint;
		}
		return dataWL;
	}

	public static double[] modifiedLogBin(double[] data, int b){
		double[] dataWL = new double[b];
		for(int i = 0; i < 40; i++){
			dataWL[i] = data[i];
		}
		b -= 40;
		double max = Math.log10(data.length);
		double min = Math.log10(40);
		double expLogBinSize = (max - min) / b;
		for(int i = 0; i < b; i++){
			double cExpMin = min + expLogBinSize * i;
			double cExpMax = min + expLogBinSize * (i + 1);
			double cMin = Math.pow(10, cExpMin);
			double cMax = Math.pow(10, cExpMax);
			int start = (int) cMin;
			if(cMin != start){
				start += 1;
			}
			int end = (int) cMax;
			if(cMax != end){
				end += 1;
			}
			double sum = 0;
			for(int a = start; a < end; a++){
				sum += data[a];
			}
			sum /= end - start;
			dataWL[40 + i] = sum;
		}
		return dataWL;
	}

	public static void main(String[] args){
		double[] sV = {0, 1, 2, 3, 4, 5, 6, 7};
		System.out.println(Arrays.toString(sV));
		System.out.println(Arrays.toString(fftSingle(sV, 8)));

	}

	public int getSize(){
		return size;
	}

	/*
	 * public static void main(String[] args) { // double[] sV = new
	 * double[256]; // for(int i = 0; i < sV.length; i++){ // sV[i] = 0; // } //
	 * System.out.println(Arrays.toString(sV)); double[] sV = { 0.0,
	 * 0.024541228522912288, 0.049067674327418015, 0.07356456359966743,
	 * 0.0980171403295606, 0.1224106751992162, 0.14673047445536175,
	 * 0.17096188876030122, 0.19509032201612825, 0.2191012401568698,
	 * 0.24298017990326387, 0.26671275747489837, 0.29028467725446233,
	 * 0.3136817403988915, 0.33688985339222005, 0.3598950365349881,
	 * 0.3826834323650898, 0.40524131400498986, 0.4275550934302821,
	 * 0.44961132965460654, 0.47139673682599764, 0.49289819222978404,
	 * 0.5141027441932217, 0.5349976198870972, 0.5555702330196022,
	 * 0.5758081914178453, 0.5956993044924334, 0.6152315905806268,
	 * 0.6343932841636455, 0.6531728429537768, 0.6715589548470183,
	 * 0.6895405447370668, 0.7071067811865475, 0.7242470829514669,
	 * 0.740951125354959, 0.7572088465064845, 0.7730104533627369,
	 * 0.7883464276266062, 0.8032075314806448, 0.8175848131515837,
	 * 0.8314696123025451, 0.844853565249707, 0.8577286100002721,
	 * 0.8700869911087113, 0.8819212643483549, 0.8932243011955153,
	 * 0.9039892931234433, 0.9142097557035307, 0.9238795325112867,
	 * 0.9329927988347388, 0.9415440651830208, 0.9495281805930367,
	 * 0.9569403357322089, 0.9637760657954398, 0.970031253194544,
	 * 0.9757021300385286, 0.9807852804032304, 0.9852776423889412,
	 * 0.989176509964781, 0.99247953459871, 0.9951847266721968,
	 * 0.9972904566786902, 0.9987954562051724, 0.9996988186962042, 1.0,
	 * 0.9996988186962042, 0.9987954562051724, 0.9972904566786902,
	 * 0.9951847266721969, 0.99247953459871, 0.989176509964781,
	 * 0.9852776423889412, 0.9807852804032304, 0.9757021300385286,
	 * 0.970031253194544, 0.9637760657954398, 0.9569403357322089,
	 * 0.9495281805930367, 0.9415440651830208, 0.9329927988347388,
	 * 0.9238795325112867, 0.9142097557035307, 0.9039892931234434,
	 * 0.8932243011955152, 0.881921264348355, 0.8700869911087115,
	 * 0.8577286100002721, 0.8448535652497072, 0.8314696123025455,
	 * 0.8175848131515837, 0.8032075314806449, 0.7883464276266063,
	 * 0.7730104533627371, 0.7572088465064848, 0.740951125354959,
	 * 0.724247082951467, 0.7071067811865476, 0.689540544737067,
	 * 0.6715589548470186, 0.6531728429537766, 0.6343932841636455,
	 * 0.6152315905806269, 0.5956993044924335, 0.5758081914178454,
	 * 0.5555702330196022, 0.5349976198870972, 0.5141027441932218,
	 * 0.49289819222978415, 0.4713967368259978, 0.4496113296546069,
	 * 0.42755509343028203, 0.40524131400498986, 0.38268343236508984,
	 * 0.35989503653498833, 0.3368898533922203, 0.3136817403988914,
	 * 0.29028467725446233, 0.2667127574748985, 0.24298017990326404,
	 * 0.21910124015687002, 0.19509032201612858, 0.1709618887603012,
	 * 0.1467304744553618, 0.12241067519921635, 0.09801714032956084,
	 * 0.07356456359966775, 0.04906767432741797, 0.024541228522912326,
	 * 1.2246467991473532e-16, -0.024541228522912083, -0.049067674327417724,
	 * -0.0735645635996675, -0.09801714032956059, -0.1224106751992161,
	 * -0.14673047445536158, -0.17096188876030097, -0.19509032201612836,
	 * -0.21910124015686983, -0.24298017990326382, -0.26671275747489825,
	 * -0.29028467725446216, -0.3136817403988912, -0.3368898533922201,
	 * -0.3598950365349881, -0.38268343236508967, -0.4052413140049897,
	 * -0.4275550934302818, -0.44961132965460665, -0.47139673682599764,
	 * -0.4928981922297839, -0.5141027441932216, -0.5349976198870969,
	 * -0.555570233019602, -0.5758081914178453, -0.5956993044924332,
	 * -0.6152315905806267, -0.6343932841636453, -0.6531728429537765,
	 * -0.6715589548470184, -0.6895405447370668, -0.7071067811865475,
	 * -0.7242470829514667, -0.7409511253549588, -0.7572088465064842,
	 * -0.7730104533627367, -0.7883464276266059, -0.803207531480645,
	 * -0.8175848131515837, -0.8314696123025452, -0.8448535652497071,
	 * -0.857728610000272, -0.8700869911087113, -0.8819212643483549,
	 * -0.8932243011955152, -0.9039892931234431, -0.9142097557035305,
	 * -0.9238795325112865, -0.932992798834739, -0.9415440651830208,
	 * -0.9495281805930367, -0.9569403357322088, -0.9637760657954398,
	 * -0.970031253194544, -0.9757021300385285, -0.9807852804032303,
	 * -0.9852776423889411, -0.9891765099647809, -0.9924795345987101,
	 * -0.9951847266721969, -0.9972904566786902, -0.9987954562051724,
	 * -0.9996988186962042, -1.0, -0.9996988186962042, -0.9987954562051724,
	 * -0.9972904566786902, -0.9951847266721969, -0.9924795345987101,
	 * -0.9891765099647809, -0.9852776423889412, -0.9807852804032304,
	 * -0.9757021300385286, -0.970031253194544, -0.96377606579544,
	 * -0.9569403357322089, -0.9495281805930368, -0.9415440651830209,
	 * -0.9329927988347391, -0.9238795325112866, -0.9142097557035306,
	 * -0.9039892931234433, -0.8932243011955153, -0.881921264348355,
	 * -0.8700869911087115, -0.8577286100002722, -0.8448535652497073,
	 * -0.8314696123025456, -0.817584813151584, -0.8032075314806453,
	 * -0.7883464276266061, -0.7730104533627369, -0.7572088465064846,
	 * -0.7409511253549592, -0.7242470829514671, -0.7071067811865477,
	 * -0.6895405447370672, -0.6715589548470187, -0.6531728429537771,
	 * -0.6343932841636459, -0.6152315905806274, -0.5956993044924332,
	 * -0.5758081914178452, -0.5555702330196022, -0.5349976198870973,
	 * -0.5141027441932219, -0.49289819222978426, -0.4713967368259979,
	 * -0.449611329654607, -0.42755509343028253, -0.4052413140049904,
	 * -0.3826834323650904, -0.359895036534988, -0.33688985339222,
	 * -0.3136817403988915, -0.29028467725446244, -0.2667127574748986,
	 * -0.24298017990326418, -0.21910124015687016, -0.19509032201612872,
	 * -0.17096188876030177, -0.1467304744553624, -0.12241067519921603,
	 * -0.09801714032956052, -0.07356456359966743, -0.04906767432741809,
	 * -0.02454122852291245 }; sV = new double[256]; for (int i = 0; i <
	 * sV.length; i++) { sV[i] = 40; } System.out.println(Arrays.toString(sV));
	 * System.out.println(Arrays.toString(fftSingle(sV, 256))); }
	 */

}