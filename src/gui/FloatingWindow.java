package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

public class FloatingWindow extends JFrame{

	private JPanel contentPane;
	private JLabel lblOff;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args){
		EventQueue.invokeLater(new Runnable(){
			@Override
			public void run(){
				try{
					FloatingWindow frame = new FloatingWindow();
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
	public FloatingWindow(){
		//TODO change this later
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		lblOff = new JLabel("Off");
		contentPane.add(lblOff, BorderLayout.CENTER);
		SwingWorker<Void, String> worker = new SwingWorker<Void, String>(){

			@Override
			public void process(List<String> items){
				if(lblOff.getText().equals("Off")){
					lblOff.setText("On");
				}
				else{
					lblOff.setText("Off");
				}
			}

			@Override
			protected Void doInBackground() throws Exception{
				while(true){
					publish("");
					Thread.sleep(2000);
					if(Math.random() == 0.5){
						break;
					}
				}
				return null;
			}

		};
		worker.execute();
	}

}
