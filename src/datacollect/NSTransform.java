package datacollect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.jtransforms.fft.DoubleFFT_1D;

public class NSTransform {

	private File file;
	private List<List<Integer>> toTransform = new ArrayList<List<Integer>>();
	private List<double[]> transformed = new ArrayList<double[]>();
	private List<Integer> labels = new ArrayList<Integer>();;

	public NSTransform(String filePath) {
		this(new File(filePath));
	}

	public NSTransform(File file) {
		this.file = file;
	}

	public void transformFile() {
		parseFile();
		transform();
	}

	private void parseFile() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			List<Integer> current = new ArrayList<Integer>();
			line = br.readLine();
			while (line != null) {
				if (line.startsWith("===")) {
					toTransform.add(current);
					current = new ArrayList<Integer>();
					String s = line.replaceAll("=", "");
					String[] split = s.split(" |\\(|\\)");
					labels.add(Integer.parseInt(split[3]));
				} else {
					JSONObject o = new JSONObject(line);
					if (o.has("rawEeg")) {
						current.add(o.getInt("rawEeg"));
					}
				}
				line = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public List<Integer> getLabels() {
		return labels;
	}

	public List<List<Integer>> getToTransform() {
		return toTransform;
	}

	public List<double[]> getTransformed() {
		return transformed;
	}

	private void transform() {
		for (List<Integer> list : toTransform) {
			transformed.add(fftSingle(list));
		}
	}

	public static double[] fftSingle(List<Integer> list) {
		double[] toTransform = toDoubleArray(list);
		DoubleFFT_1D fft = new DoubleFFT_1D(toTransform.length);
		double[] transformed = new double[toTransform.length * 2];
		System.arraycopy(toTransform, 0, transformed, 0, toTransform.length);
		fft.realForwardFull(transformed);
		return transformed;
	}

	public static double[] toDoubleArray(List<Integer> list) {
		double[] array = new double[list.size()];
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}
		return array;
	}
}