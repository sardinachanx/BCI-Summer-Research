package gui;

import java.awt.BorderLayout;
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

public class StartingScreen extends JFrame implements InputSelector{

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
					StartingScreen frame = new StartingScreen();
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
	public StartingScreen(){
		setTitle("開始");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 150, 151);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

		JPanel panel_4 = new JPanel();
		contentPane.add(panel_4);

		JPanel panel = new JPanel();
		contentPane.add(panel);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0), 3));
		panel.add(panel_2);

		panels.add(panel_2);

		JButton btnInputScreen = new JButton("輸入\n");
		panel_2.add(btnInputScreen);

		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1);

		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3);

		panels.add(panel_3);

		JButton button = new JButton("遙控");
		panel_3.add(button);

		TimeBasedInputSwitcher switcher =
				new TimeBasedInputSwitcher(this, 5000);
		switcher.execute();

		JPanel panel_5 = new JPanel();
		panel_4.add(panel_5);

		JPanel pie = switcher.newPanel();

		panel_5.setLayout(new BorderLayout(0, 0));
		panel_5.add(pie, BorderLayout.CENTER);
		panel_5.setPreferredSize(pie.getPreferredSize());
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
