package gui.strokeinput;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class StrokeInput extends JFrame implements SelectionSystem{

	private static final long serialVersionUID = -1807520861414765425L;
	public static final float LARGE_CHARACTER_FONT_SIZE = 72f; //48f;
	public static final float SELECT_FONT_SIZE = 18f;

	private JPanel contentPane;

	private List<ItemSelectionGroup> groups =
			new ArrayList<ItemSelectionGroup>();

	private int currentIndex = 0;

	private JEditorPane textInput;
	private JLabel currentInputLabel;

	private String currentStrokes = "";
	private ItemSelectionGroup strokeGroup;
	private ItemSelectionGroup possibleCharactersGroup;

	private String cachedCurrentStrokes = "";
	private String[] cachedPossibilities = new String[]{};  
	int index = 0;

	private static final int FREE_SPACES = 5;
	public static final boolean RIGHT_ARROW = false;
	
	public static SoundPlayer player;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args){
		CharacterDatabase.load();
		player = new SoundPlayer();
		player.start();
		EventQueue.invokeLater(new Runnable(){
			@Override
			public void run(){
				try{
					StrokeInput frame = new StrokeInput();
					frame.setVisible(true);
					KeyboardSelectionController ksc =
							new KeyboardSelectionController(frame);
					ksc.bind();
					NeuroskySelectionController nsc =
							new NeuroskySelectionController(frame, "serena1");
					nsc.bind();
					ksc.setPredictionListener(nsc);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public StrokeInput(){
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

		setTitle("\u7E41\u9AD4\u4E2D\u6587\u8F38\u5165\u6CD5");

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "\u6587\u5B57\u5340",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(panel);
		panel.setLayout(new BorderLayout(0, 0));

		textInput = new JEditorPane();
		panel.add(textInput, BorderLayout.CENTER);
		textInput.setFont(textInput.getFont().deriveFont(72.0f));

		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_1.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, null, null),
				"\u5DF2\u8F38\u5165\u7B46\u5283", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		contentPane.add(panel_1);

		JLabel label = new JLabel("");
		label.setFont(label.getFont().deriveFont(LARGE_CHARACTER_FONT_SIZE));
		panel_1.add(label);
		currentInputLabel = label;

		JPanel panel_2 = new JPanel();
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));
		panel_2.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, null, null),
				"\u7B46\u5283\u8F38\u5165\uFF0F\u64CD\u4F5C",
				TitledBorder.LEADING, TitledBorder.TOP, null,
				new Color(0, 0, 0)));
		contentPane.add(panel_2);

		//JPanel panel_4 = new JPanel();
		//panel_2.add(panel_4);
		//panel_4.setLayout(new BorderLayout(0, 0));

		ItemSelectionGroup group2 =
				new ItemSelectionGroup("<html>\u7B46<br/>\u5283</html>",
						VerticalItemSelectionPanel.STROKES, 1);
		groups.add(group2);
		panel_2.add(group2);
		strokeGroup = group2;

		ItemSelectionGroup group1 = new ItemSelectionGroup(
				"<html>\u5019<br/>\u9078<br/>\u5B57</html>",
				VerticalItemSelectionPanel.PLACEHOLDER_CHARACTERS, 1);
		groups.add(group1);
		panel_2.add(group1);
		possibleCharactersGroup = group1;

		//ItemSelectionGroup group3 =
		//		new ItemSelectionGroup("<html>\u5E38<br/>\u7528<br/>\u5B57</html>",
		//				VerticalItemSelectionPanel.COMMON_CHARACTERS, 1);
		//groups.add(group3);
		//panel_2.add(group3);

		ItemSelectionGroup group4 =
				new ItemSelectionGroup("<html>\u6A19<br/>\u9EDE</html>",
						VerticalItemSelectionPanel.PUNCTUATION, 1);
		groups.add(group4);
		panel_2.add(group4);

		//ItemSelectionGroup group5 = new ItemSelectionGroup(
		//		"<html>鎿�<br/>浣�</html>", VerticalItemSelectionPanel.COMMANDS, 1);
		//groups.add(group5);
		//panel_2.add(group5);

		//JPanel panel_3 = new JPanel();
		//panel_3.setBorder(new TitledBorder(null, "\u50B3\u9001\u8A0A\u606F",
		//		TitledBorder.LEADING, TitledBorder.TOP, null, null));
		//contentPane.add(panel_3);

		addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e){
				requestFocus();
			}

			@Override
			public void mousePressed(MouseEvent e){
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseReleased(MouseEvent e){
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e){
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e){
				// TODO Auto-generated method stub

			}

		});

		group2.selectPanel(0, 0);
	}

	@Override
	public void selectUp(){
		currentIndex = 0;
		ItemSelectionGroup lastGroup = groups.get(groups.size() - 1);
		for(ItemSelectionGroup group : groups){
			int selected = group.selectedPanel();
			if(selected > 0){
				group.selectPanel(selected - 1, currentIndex);
				break;
			}
			else if(selected == 0){
				group.selectPanel(-1, -1);
				lastGroup.selectPanel(lastGroup.panelCount() - 1, currentIndex);
				break;
			}
			lastGroup = group;
		}
	}

	@Override
	public void selectDown(){
		currentIndex = 0;
		StrokeInput.player.play(0);
		StrokeInput.player.play(12);
		ItemSelectionGroup lastGroup = groups.get(0);
		List<ItemSelectionGroup> reverseGroups =
				new ArrayList<ItemSelectionGroup>(groups);
		Collections.reverse(reverseGroups);
		for(ItemSelectionGroup group : reverseGroups){
			int selected = group.selectedPanel();
			if(selected >= 0 && selected < group.panelCount() - 1){
				group.selectPanel(selected + 1, currentIndex);
				break;
			}
			else if(selected >= 0 && selected == group.panelCount() - 1){
				group.selectPanel(-1, -1);
				lastGroup.selectPanel(0, currentIndex);
				break;
			}
			lastGroup = group;
		}
	}

	@Override
	public void selectLeft(){
		currentIndex--;
		if(currentIndex < 0){
			currentIndex = VerticalItemSelectionPanel.NUM_ROWS - 1;
		}
		updateSelection();
	}

	@Override
	public void selectRight(){
		currentIndex++;
		if(currentIndex >= VerticalItemSelectionPanel.NUM_ROWS){
			currentIndex = 0;
		}
		updateSelection();
	}

	private void updateSelection(){
		for(ItemSelectionGroup group : groups){
			int selected = group.selectedPanel();
			if(selected >= 0){
				group.selectPanel(selected, currentIndex);
				break;
			}
		}
	}

	@Override
	public void selectEnter(){
		player.play(0);
		String current = currentlySelectedString();
		if(current.equals(VerticalItemSelectionPanel.DELETE)){
			if(currentStrokes.length() > 0){
				currentStrokes = currentStrokes.substring(0,
						currentStrokes.length() - 1);
			}
			else{
				if(textInput.getText().length() > 0){
					textInput.setText(textInput.getText().substring(0,
							textInput.getText().length() - 1));
				}
			}
		}
		else if(current.equals(VerticalItemSelectionPanel.LEFT_ARROW)){
			index -= FREE_SPACES;
			if(index < 0){
				index = 0;
			}
		}
		else if(current.equals(VerticalItemSelectionPanel.RIGHT_ARROW)){
			index += FREE_SPACES;
			if(index > cachedPossibilities.length - FREE_SPACES){
				index = cachedPossibilities.length - FREE_SPACES;
			}
			if(index < 0){
				index = 0;
			}
		}
		else if(strokeGroup.selectedPanel() >= 0){
			currentStrokes += current;
			index = 0;
		}
		else{
			textInput.setText(textInput.getText() + current);
			index = 0;
			currentStrokes = "";
		}
		if(currentStrokes.equals("")
				|| !currentStrokes.equals(cachedCurrentStrokes)){
			cachedCurrentStrokes = currentStrokes;
			if(currentStrokes.equals("")){
				cachedPossibilities = CharacterDatabase
						.lastPhrasePredictions(textInput.getText());
			}
			else{
				cachedPossibilities =
						CharacterDatabase.possibleCharactersFromStrokes(
								selectedStringSymbol(currentStrokes));
			}
		}
		updatePossibleCharacters();
		currentInputLabel.setText(currentStrokes.replace(" ", "    "));
		//currentIndex = 0;
		//updateSelection();
	}
	
	private void updatePossibleCharacters(){
		possibleCharactersGroup.updateCharacters(
				pad(newArray(cachedPossibilities, index, FREE_SPACES + (RIGHT_ARROW ? 0 : 1)),
						VerticalItemSelectionPanel.LEFT_ARROW,
						RIGHT_ARROW ? VerticalItemSelectionPanel.RIGHT_ARROW : null));
	}

	private static String[] pad(String[] original, String padStart,
			String padEnd){
		String[] output;
		if(padStart != null && padEnd != null){
			output = new String[original.length + 2];
			System.arraycopy(original, 0, output, 1, original.length);
			output[0] = padStart;
			output[output.length - 1] = padEnd;
		}
		else if(padStart != null){
			output = new String[original.length + 1];
			System.arraycopy(original, 0, output, 1, original.length);
			output[0] = padStart;
		}
		else if(padEnd != null){
			output = new String[original.length + 1];
			System.arraycopy(original, 0, output, 0, original.length);
			output[output.length - 1] = padEnd;
		}
		else{
			output = original;
		}
		return output;
	}

	private static String[] newArray(String[] original, int offset, int length){
		String[] output = new String[length];
		System.arraycopy(original, offset, output, 0,
				Math.min(length, original.length - offset));
		for(int i = 0; i < length; i++){
			if(output[i] == null){
				output[i] = "";
			}
		}
		return output;
	}

	public static String selectedStringSymbol(String selectedString){
		String out = "";
		for(char c : selectedString.toCharArray()){
			for(int i = 0; i < VerticalItemSelectionPanel.STROKES.length; i++){
				if(String.valueOf(c)
						.equals(VerticalItemSelectionPanel.STROKES[i])){
					out += VerticalItemSelectionPanel.STROKE_SYMBOLS[i];
				}
			}
		}
		return out;
	}

	private String currentlySelectedString(){
		for(ItemSelectionGroup group : groups){
			int selected = group.selectedPanel();
			if(selected >= 0){
				return group.panelFromIndex(selected).getText(currentIndex);
			}
		}
		return null;
	}

	@Override
	public boolean nextPage(){
		if(possibleCharactersGroup.selectedPanel() >= 0){
			player.play(0);
			player.play(3);
			
			index += FREE_SPACES;
			if(index > cachedPossibilities.length - FREE_SPACES){
				index = cachedPossibilities.length - FREE_SPACES;
			}
			if(index < 0){
				index = 0;
			}
			updatePossibleCharacters();
			currentIndex = 0;
			updateSelection();
			return true;
		}
		return false;
	}
}
