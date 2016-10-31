package gui.strokeinput;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class ItemSelectionGroup extends JPanel{

	/**
	 *
	 */
	private static final long serialVersionUID = 7745724416915920070L;
	private List<VerticalItemSelectionPanel> panelList =
			new ArrayList<VerticalItemSelectionPanel>();
	private int selectedPanel = -1;

	public ItemSelectionGroup(String input, String[] characters, int columns){

		JPanel panel = new JPanel();
		//panel.setPreferredSize(
		//		new Dimension((int) (StrokeInput.SELECT_FONT_SIZE * 1.5),
		//				VerticalItemSelectionPanel.HEIGHT));
		add(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JLabel label = new JLabel(input);
		label.setFont(label.getFont().deriveFont(StrokeInput.SELECT_FONT_SIZE));
		panel.add(label, BorderLayout.WEST);

		for(int i = 0; i < columns; i++){

			VerticalItemSelectionPanel visp =
					new VerticalItemSelectionPanel(characters);
			panelList.add(visp);
			add(visp);
		}
	}

	public void updateCharacters(String[] characters){
		for(int i = 0; i < panelList.size(); i++){
			panelList.get(i).setAllTexts(characters);
		}
	}

	public VerticalItemSelectionPanel panelFromIndex(int index){
		return panelList.get(index);
	}

	public int panelCount(){
		return panelList.size();
	}

	public int selectedPanel(){
		return selectedPanel;
	}

	public void selectPanel(int panel, int index){
		if(panel < -1 || panel >= panelCount()){
			throw new ArrayIndexOutOfBoundsException(
					"Panel index out of bounds: " + panel);
		}
		int selected = selectedPanel();
		if(selected >= 0){
			panelList.get(selected).selectPanel(false, -1);
		}
		selectedPanel = panel;
		if(panel >= 0){
			panelList.get(panel).selectPanel(true, index);
		}
	}

}
