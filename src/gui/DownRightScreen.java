package gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class DownRightScreen extends JFrame implements InputSelector{

	private JPanel contentPane;

	public List<JPanel> panels = new ArrayList<JPanel>();

	public static final int INPUT_COUNT = 2;
	protected int current;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args){
		EventQueue.invokeLater(new Runnable(){
			@Override
			public void run(){
				try{
					DownRightScreen frame = new DownRightScreen();
					frame.setVisible(true);
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
	public DownRightScreen(){
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 150, 150);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

		JPanel panel = new JPanel();
		contentPane.add(panel);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0), 0));
		panel.add(panel_2);

		panels.add(panel_2);

		JButton btnDown = new JButton("↓");
		panel_2.add(btnDown);

		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1);

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new LineBorder(new Color(0, 0, 0), 3));
		panel_1.add(panel_3);

		panels.add(panel_3);

		JButton btnRight = new JButton("→");
		panel_3.add(btnRight);
	}

	@Override
	public int getInputCount(){
		return INPUT_COUNT;
	}

	@Override
	public int getInput(){
		return current;
	}

	@Override
	public void setInput(int value){
		if(value < 0){
			throw new IllegalArgumentException(
					"Value " + value + " is negative");
		}
		if(value >= INPUT_COUNT){
			throw new IllegalArgumentException(
					"Value " + value + " more than max " + INPUT_COUNT);
		}
		for(int i = 0; i < panels.size(); i++){
			JPanel panel = panels.get(i);
			if(i == value){
				panel.setBorder(new LineBorder(new Color(0, 0, 0), 3));
			}
			else{
				panel.setBorder(new LineBorder(new Color(0, 0, 0), 0));
			}
		}
	}

}
