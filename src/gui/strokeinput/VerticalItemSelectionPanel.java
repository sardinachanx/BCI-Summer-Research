package gui.strokeinput;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class VerticalItemSelectionPanel extends JPanel{

	/**
	 *
	 */
	private static final long serialVersionUID = -3131899346432256071L;

	public static final String WILD_CARD = "*";
	
	public static final String LEFT_ARROW = "\u2190";

	public static final String RIGHT_ARROW = "\u2192";

	public static final String DELETE = "\u2612";

	public static final String[] PLACEHOLDER_CHARACTERS =
			new String[]{LEFT_ARROW, "", "", "", "", "", StrokeInput.RIGHT_ARROW ? RIGHT_ARROW : ""};

	public static final String[] STROKES = new String[]{"\u4E00", "\uFF5C",
			"\u30CE", "\u3001", "\u4E5B", WILD_CARD, DELETE};

	public static final String[] STROKE_SYMBOLS =
			new String[]{"h", "s", "p", "n", "z", WILD_CARD, "?"};

	//public static final String[] COMMON_CHARACTERS =
	//		new String[]{"涓�", "濂�", "浣�", "骞�", "鍠�"};

	public static final String[] PUNCTUATION = new String[]{LEFT_ARROW,
			"\uFF0C", "\u3002", "\uFF1F", "\uFF01", "\uFF1A", RIGHT_ARROW};

	//public static final String[] COMMANDS =
	//		new String[]{"鍒瓧", "鍒瓎鍔�", "鍌抽��", "鍓嶉��", "寰岄��"};

	public static final int NUM_ROWS = 7;
	public static final int HEIGHT =
			(int) (StrokeInput.LARGE_CHARACTER_FONT_SIZE * NUM_ROWS * 1.5);

	private JLabel[] labels = new JLabel[NUM_ROWS];

	private int selectedIndex;
	private boolean selected;

	/**
	 * Create the panel.
	 */
	public VerticalItemSelectionPanel(String[] characters){
		setBackground(Color.gray);

		for(int i = 0; i < NUM_ROWS; i++){
			JPanel panel = new JPanel();
			int panelSize =
					(int) (StrokeInput.LARGE_CHARACTER_FONT_SIZE * 1.35);
			panel.setPreferredSize(new Dimension(panelSize, panelSize));
			panel.setBackground(Color.lightGray);
			add(panel);
			JLabel label = new JLabel(characters[i]);
			label.setFont(label.getFont()
					.deriveFont(StrokeInput.LARGE_CHARACTER_FONT_SIZE));
			panel.add(label);
			labels[i] = label;
		}

		setPreferredSize(new Dimension(HEIGHT,
				(int) (StrokeInput.LARGE_CHARACTER_FONT_SIZE * 1.5)));

	}

	public String getText(int index){
		return labels[index].getText();
	}

	public String[] getAllTexts(){
		String[] texts = new String[NUM_ROWS];
		for(int i = 0; i < NUM_ROWS; i++){
			texts[i] = getText(i);
		}
		return texts;
	}

	public void setText(String text, int index){
		labels[index].setText(text);
	}

	//null means don't override text
	public void setAllTexts(String[] texts){
		if(texts.length != NUM_ROWS){
			throw new IllegalArgumentException("Illegal array size!");
		}
		for(int i = 0; i < NUM_ROWS; i++){
			setText(texts[i], i);
		}
	}

	public void selectPanel(boolean isSelected, int index){
		if(isSelected == selected){
			return;
		}
		selected = isSelected;
		if(isSelected){
			setBackground(Color.black);
			for(JLabel label : labels){
				label.getParent().setBackground(Color.white);
				label.getParent().repaint();
			}
		}
		else{
			setBackground(Color.gray);
			for(JLabel label : labels){
				label.getParent().setBackground(Color.lightGray);
				label.getParent().repaint();
			}
		}
		if(isSelected && index >= 0){
			labels[index].getParent().setBackground(Color.red);
		}
		repaint();
	}

}
