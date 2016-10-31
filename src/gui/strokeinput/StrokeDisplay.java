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
		String s = "Ó�f����\n"+
				"һ�z����\n"+
				"˾��Ҋ�T\n"+
				"��;���U\n"+
				"����˼�h\n"+
				"һ�Q�@��\n"+
				"һ�[��ͨ\n"+
				"Մ������\n"+
				"��������\n"+
				"�y�߰���\n"+
				"һҊ�R��\n"+
				"�۲����\n"+
				"һ�o����\n"+
				"����ì��\n"+
				"�A�����\n"+
				"��������\n"+
				"�ؿ���ƿ\n"+
				"����ʧ�R\n"+
				"��ţ����\n"+
				"��ţһë";
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
