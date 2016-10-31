package gui.strokeinput;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.List;

public class StrokeDisplay {
	public static void main(String[] args) throws IOException{
		String s = "脫穎而出\n"+
				"一絲不苟\n"+
				"司空見慣\n"+
				"半途而廢\n"+
				"不可思議\n"+
				"一鳴驚人\n"+
				"一竅不通\n"+
				"談何容易\n"+
				"自由自在\n"+
				"亂七八糟\n"+
				"一見鍾情\n"+
				"愛不釋手\n"+
				"一無所有\n"+
				"自相矛盾\n"+
				"傾盆大雨\n"+
				"畫蛇添足\n"+
				"守口如瓶\n"+
				"塞翁失馬\n"+
				"對牛彈琴\n"+
				"九牛一毛";
		String[] sa = s.split("\\n");
		for(String s0 : sa){
			for(int i = 0; i < s0.length(); i++){
				String single = s0.substring(i, i + 1);
				Stream<String> stream = Files.lines(Paths.get("characterdb" + File.separator + "table.txt"));
				List<String> matches = stream.filter(line -> line.contains(single)).collect(Collectors.toList());
				stream.close();
				//System.out.println(matches.size());
				if(matches.size() > 0 && matches.size() < 2){
					String s1 = matches.get(0);
					//System.out.println(s1);
					String[] sa1 = s1.split("\t");
					System.out.println(sa1[1] + ":" + applyReplacements(sa1[0]));
				}
				else{
					System.out.println("???");
				}
			}
			System.out.println();
		}
	}
	
	private static final String[] STROKES = new String[]{"\u4E00", "\uFF5C",
			"\u30CE", "\u3001", "\u4E5B"};
	
	private static String applyReplacements(String in){
		in = in.replace("h", STROKES[0]);
		in = in.replace("s", STROKES[1]);
		in = in.replace("p", STROKES[2]);
		in = in.replace("n", STROKES[3]);
		in = in.replace("z", STROKES[4]);
		String out = "";
		for(int i = 0; i < in.length(); i++){
			out += in.substring(i, i + 1);
			out += " ";
		}
		return out;
	}
}
