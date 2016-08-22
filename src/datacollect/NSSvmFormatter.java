package datacollect;

import java.util.List;

public class NSSvmFormatter {
	
	public static String[] format(NSTransform nst){
		List<double[]> transformed = nst.getTransformed();
		return formatLabelled(transformed, nst);
	}
	
	public static String[] formatAveraged(NSTransform nst){
		List<double[]> transformed = nst.getAveraged();
		return formatLabelled(transformed, nst);
	}
	
	public static String[] formatLabelled(List<double[]> transformed, NSTransform nst){
		String[] formatted = new String[transformed.size()];
		int label = nst.getLabel();
		for(int a = 0; a < transformed.size(); a++){
			double[] set = transformed.get(a);
			StringBuilder sb = new StringBuilder();
			sb.append((double)label + " ");
			sb.append(formatData(set));
			formatted[a] = sb.toString();
			//System.out.println(sb.toString());
		}
		return formatted;
	}
	
	public static String formatData(double[] set){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < set.length; i++){
			sb.append((i + 1) + ":" + set[i] + " ");
		}
		return sb.toString();
	}
	
	public static String formatTestData(double[] set){
		return "1 " + formatData(set);
	}
	
	public static String[] formatBinned(NSTransform nst){
		List<double[]> transformed = nst.getBinned();
		return formatLabelled(transformed, nst);
	}
}
