package gui.strokeinput;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CharacterDatabase{

	private static class StringInteger{
		private String s;
		private int i;

		public StringInteger(String s, Integer i){
			this.s = s;
			this.i = i;
		}

		public int getInteger(){
			return i;
		}

		public String getString(){
			return s;
		}
	}

	private static class Possible{
		public List<String> exact = new ArrayList<String>();
		public List<String> partial = new ArrayList<String>();
	}

	private static final int MIN_PREDICT = 1;

	private static boolean SEPARATE_PHRASES = true;
	private static final String CHARACTER_DB_PATH =
			"characterdb" + File.separator;
	private static final String POSSIBLE_MAP = CHARACTER_DB_PATH + "table.txt";
	private static final String FREQUENCY_MAP = CHARACTER_DB_PATH + "freq2.txt";

	//http://www.sayjack.com/chinese/simplified-to-traditional-chinese-conversion-table/
	//http://www.loria.fr/~roegel/chinese/list-simp-char.pdf
	//http://humanum.arts.cuhk.edu.hk/Lexis/chifreq/
	private static final String TRADITIONAL_MAP =
			CHARACTER_DB_PATH + "traditional2.txt";

	private static final String PHRASE_MAP = CHARACTER_DB_PATH + "phrases.txt";

	private static Map<String, Map<String, Integer>> phraseMap =
			new HashMap<String, Map<String, Integer>>();

	private static Map<String, Possible> possibleMap;

	private static Map<String, Double> freq;
	private static Map<String, Double> phraseFreq =
			new HashMap<String, Double>();

	private static Map<String, String> traditional;

	private static final boolean TRADITIONAL_FOCUS = false;
	
	private static final String[] STROKES = 
			new String[]{"h", "s", "p", "n", "z"};

	public static void load(){
		if(TRADITIONAL_FOCUS){
			traditional = generateTraditionalMap(); //generate this first
		}
		possibleMap = generatePossibleMap();
		if(SEPARATE_PHRASES){
			phraseMap = phraseMap(phraseFreq);
		}
		freq = generateFrequencyMap(phraseMap, phraseFreq);
		System.out.println(freq);
	}

	public static String[] possibleCharactersFromStrokes(String currentStrokes){
		List<String> exacts = new ArrayList<String>();
		List<String> partials = new ArrayList<String>();
		
		List<String> permutations = permutations(currentStrokes);
		
		for(String permutation : permutations){
			Possible possible = possibleMap.get(permutation);
			if(possible != null){
				List<String> exact = new ArrayList<String>(possible.exact);
				exacts.addAll(exact);
				//exact.sort(CharacterDatabase::compareFrequencies);
				List<String> partial = new ArrayList<String>(possible.partial);
				//partial.sort(CharacterDatabase::compareFrequencies);
				//exact.addAll(partial);
				partials.addAll(partial);
				//return exact.toArray(new String[]{});
			}
		}
		
		exacts.sort(CharacterDatabase::compareFrequencies);
		partials.sort(CharacterDatabase::compareFrequencies);
		
		exacts.addAll(partials);
		
		return exacts.toArray(new String[]{});
	}
	
	public static List<String> permutations(String strokes){
		int j = strokes.indexOf(VerticalItemSelectionPanel.WILD_CARD);
		if(j < 0){
			List<String> l = new ArrayList<String>();
			l.add(strokes);
			return l;
		}
		else{
			List<String> total = new ArrayList<String>();
			for(String s : STROKES){
				String newStrokes = strokes.substring(0, j) + 
						s + strokes.substring(j + 1);
				total.addAll(permutations(newStrokes));
			}
			return total;
		}
	}

	public static String[] lastPhrasePredictions(String last){
		if(last.length() < 1){
			return new String[]{};
		}
		String lastOfLast = last.substring(last.length() - 1);
		String lastWithoutEnd = last.substring(0, last.length() - 1);
		Map<String, Integer> phrases = phraseMap.get(lastOfLast);
		if(phrases == null){
			return new String[]{};
		}
		else{
			List<StringInteger> phrasesSorted =
					new ArrayList<StringInteger>(phrases.entrySet().stream()
							.map(entry -> new StringInteger(entry.getKey(),
									entry.getValue()))
							.collect(Collectors.toList()));
			if(SEPARATE_PHRASES){
				phrasesSorted
						.sort(CharacterDatabase::compareFrequenciesPhrases);
			}
			else{
				phrasesSorted.sort(CharacterDatabase::compareFrequencies);
			}
			List<String> phraseStrings =
					phrasesSorted.stream().map(phrase -> phrase.getString())
							.collect(Collectors.toList());
			//System.out.println(phrasesSorted.stream()
			//		.map(x -> x.getString() + ":"
			//				+ phraseFreq.get(x.getString())
			//						* Math.pow(x.getInteger() + 1, 8))
			//		.collect(Collectors.toList()));
			List<String> allowed = new ArrayList<String>();
			for(String s : phraseStrings){
				String[] sa = s.split(lastOfLast, -1);
				String curr = "";
				for(int i = 0; i < sa.length - 1; i++){
					curr += sa[i];
					if(lastWithoutEnd.endsWith(curr)){
						if(sa[i + 1].length() > 0){
							allowed.add(sa[i + 1].substring(0, 1));
						}
					}
				}
			}
			return allowed.stream().distinct().toArray(String[]::new);
		}
	}

	private static int compareLength(String a, String b){
		int compare = Integer.compare(a.length(), b.length());
		if(compare != 0){
			return -compare;
		}
		else{
			return Integer.compare(a.hashCode(), b.hashCode());
		}
	}

	private static int compareFrequenciesPhrases(StringInteger a,
			StringInteger b){
		return compareFrequencies(a, b, phraseFreq);
	}

	private static int compareFrequencies(StringInteger a, StringInteger b){
		return compareFrequencies(a, b, freq);
	}

	private static int compareFrequencies(String a, String b){
		return compareFrequencies(a, b, freq);
	}

	private static int compareFrequencies(StringInteger ai, StringInteger bi,
			Map<String, Double> freq){
		int exp = 8;
		String a = ai.getString();
		String b = bi.getString();
		int a1 = ai.getInteger();
		int b1 = bi.getInteger();
		double aFreq = -1;
		double bFreq = -1;
		if(freq.containsKey(a)){
			aFreq = freq.get(a) * Math.pow(a1 + 1, exp);
		}
		if(freq.containsKey(b)){
			bFreq = freq.get(b) * Math.pow(b1 + 1, exp);
		}
		//if(aFreq >= 0 || bFreq >= 0){
		//	System.out.println(aFreq + ";" + bFreq);
		//}
		int compare = Double.compare(aFreq, bFreq);
		if(compare != 0){
			return -compare;
		}
		else{
			return Integer.compare(a.hashCode(), b.hashCode());
		}
	}

	private static int compareFrequencies(String a, String b,
			Map<String, Double> freq){
		int exp = 8;
		double aFreq = -1;
		double bFreq = -1;
		if(freq.containsKey(a)){
			aFreq = freq.get(a);
		}
		if(freq.containsKey(b)){
			bFreq = freq.get(b);
		}
		//if(aFreq >= 0 || bFreq >= 0){
		//	System.out.println(aFreq + ";" + bFreq);
		//}
		int compare = Double.compare(aFreq, bFreq);
		if(compare != 0){
			return -compare;
		}
		else{
			return Integer.compare(a.hashCode(), b.hashCode());
		}
	}

	private static Map<String, String> generateTraditionalMap(){
		Map<String, String> map = new HashMap<String, String>();
		try(BufferedReader br =
				new BufferedReader(new FileReader(TRADITIONAL_MAP))){
			String s = br.readLine();
			while(s != null){
				String[] sa = s.split("\\|");
				if(sa.length != 2){
					s = br.readLine();
					continue;
				}
				map.put(sa[0], sa[1]);
				s = br.readLine();
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return map;
	}

	private static Map<String, Possible> generatePossibleMap(){

		Map<String, Possible> map = new HashMap<String, Possible>();
		try(BufferedReader br =
				new BufferedReader(new InputStreamReader(new FileInputStream(POSSIBLE_MAP), 
						Charset.forName("UTF-8")))){
			String s = br.readLine();
			while(s != null){
				String[] sa = s.split("\t");
				if(sa.length != 2){
					s = br.readLine();
					continue;
				}
				String strokeString = sa[0];
				for(int i = 1; i <= strokeString.length(); i++){
					Possible possibleList =
							map.get(strokeString.substring(0, i));
					if(possibleList == null){
						possibleList = new Possible();
						map.put(strokeString.substring(0, i), possibleList);
					}
					if(i == strokeString.length()){
						possibleList.exact.add(sa[1]);
					}
					else{
						if(i >= MIN_PREDICT){
							possibleList.partial.add(sa[1]);
						}
					}
				}
				s = br.readLine();
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return map;
	}

	private static Map<String, Double> generateFrequencyMap(
			Map<String, Map<String, Integer>> phraseMapRef,
			Map<String, Double> phraseFreqMapRef){
		Map<String, Double> map = new HashMap<String, Double>();
		try(BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(FREQUENCY_MAP), Charset.forName("UTF-8")))){
			//utf-16 for freq.txt, utf-8 for freq2.txt
			String s = br.readLine();
			System.out.println(s);
			while(s != null){
				String[] sa = s.split(" ");
				if(sa.length < 2){
					s = br.readLine();
					continue;
				}
				String singleChar = sa[0];
				if(TRADITIONAL_FOCUS){
					String out = "";
					for(int i = 0; i < singleChar.length(); i++){
						String single = singleChar.substring(i, i + 1);
						String converted = traditional.get(single);
						if(converted != null && !converted.equals("_")){
							if(converted.contains(",")){
								out += converted;
							}
						}
						else{
							if(converted != null && converted.equals("_")){
								System.out.println(single);
							}
							out += single;
						}
						//System.out.println(single + "|" + converted);
					}
					singleChar = out;
				}
				double possibility;
				try{
					possibility = Double.parseDouble(sa[1]);
				}
				catch(NumberFormatException e1){
					s = br.readLine();
					continue;
				}
				if(singleChar.length() <= 1 || !SEPARATE_PHRASES){
					if(map.containsKey(singleChar)){
						//System.out.println(singleChar);
						map.put(singleChar, map.get(singleChar) + possibility);
					}
					else{
						map.put(singleChar, possibility);
					}
				}
				if(singleChar.length() > 1){
					for(int i = 0; i < singleChar.length(); i++){
						String sub = singleChar.substring(i, i + 1);
						Map<String, Integer> subs = phraseMapRef.get(sub);
						if(subs == null){
							subs = new HashMap<String, Integer>();
							phraseMapRef.put(sub, subs);
						}
						subs.put(singleChar, i);
					}
					if(SEPARATE_PHRASES){
						if(phraseFreqMapRef.containsKey(singleChar)){
							//System.out.println(singleChar);
							phraseFreqMapRef.put(singleChar,
									phraseFreqMapRef.get(singleChar)
											+ possibility);
						}
						else{
							phraseFreqMapRef.put(singleChar, possibility);
						}
					}
				}
				s = br.readLine();
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return map;
	}

	public static Map<String, Map<String, Integer>> phraseMap(
			Map<String, Double> phraseFreqMapRef){
		Map<String, Map<String, Integer>> phraseMapRef =
				new HashMap<String, Map<String, Integer>>();
		try(BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(PHRASE_MAP), Charset.forName("UTF-8")))){
			//utf-16 for freq.txt, utf-8 for freq2.txt
			String s = br.readLine();
			System.out.println(s);
			while(s != null){
				String[] sa = s.split(" ");
				if(sa.length < 2){
					s = br.readLine();
					continue;
				}
				String singleChar = sa[0];
				double possibility;
				try{
					possibility = Double.parseDouble(sa[1]);
				}
				catch(NumberFormatException e1){
					s = br.readLine();
					continue;
				}
				if(phraseFreqMapRef.containsKey(singleChar)){
					//System.out.println(singleChar);
					phraseFreqMapRef.put(singleChar,
							phraseFreqMapRef.get(singleChar) + possibility);
				}
				else{
					phraseFreqMapRef.put(singleChar, possibility);
				}
				for(int i = 0; i < singleChar.length(); i++){
					String sub = singleChar.substring(i, i + 1);
					Map<String, Integer> subs = phraseMapRef.get(sub);
					if(subs == null){
						subs = new HashMap<String, Integer>();
						phraseMapRef.put(sub, subs);
					}
					if(!subs.containsKey(singleChar)){
						subs.put(singleChar, i);
					}
				}
				s = br.readLine();
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return phraseMapRef;
	}

}
