package datacollect;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.json.JSONObject;

public class DataCollect extends JFrame{

	private static final long serialVersionUID = -3478409869011707701L;
	private static final String PATH = "images/";
	private static final int DEFAULT_REPETITION = 20;
	private static final int DASH = 9;
	private static final int END = 10;
	private static final String[] NAMES = {"angry.jpg", "closeeye.jpg", "eyebrow.jpg", "mouthL.jpg", "mouthR.jpg",
			"smile.jpg", "surprise.jpg", "winkL.jpg", "winkR.jpg", "dash.jpg", "done.jpg"};

	private List<JLabel> images;

	private JPanel contentPane;
	private JTextField textField;
	private JPanel imageDisplay;
	private JButton startSession;
	private JLabel counter;
	//private ButtonGroup buttonGroup;
	private JRadioButton epoc;
	private JRadioButton neuroSky;
	private int currentImageNumber;
	private int totalImageNumber;
	private Socket socket;

	private boolean started;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args){
		EventQueue.invokeLater(new Runnable(){
			@Override
			public void run(){
				try{
					DataCollect frame = new DataCollect();
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
	public DataCollect(){
		currentImageNumber = 0;
		totalImageNumber = DEFAULT_REPETITION;
		started = false;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.NORTH);

		JLabel lblRepetitions = new JLabel("Repetitions:");
		panel_1.add(lblRepetitions);

		textField = new JTextField();
		textField.setText(Integer.toString(DEFAULT_REPETITION));
		panel_1.add(textField);
		textField.setColumns(5);

		JButton btnStartSession = new JButton("Start");
		startSession = btnStartSession;
		startSession.addActionListener(new ActionListener(){

			private DataCollect dc;

			@Override
			public void actionPerformed(ActionEvent e){
				currentImageNumber = 0;
				try{
					//TODO: integrate SwingWorker
					totalImageNumber = Integer.parseInt(textField.getText());
					started = true;
					if(epoc.isSelected()){
						//TODO: implement epoc
					}
					else if(neuroSky.isSelected()){
						//TODO: finish neurosky implementation
						SwingWorker<Void, Integer> rn = new RecordNeuro<Void, Integer>(dc, totalImageNumber);
						//System.out.println("it reached here: neurosky");
						//System.out.println("current: " + currentImageNumber + " , total: " + totalImageNumber);
					}
					else{
						JOptionPane.showMessageDialog(contentPane, "Please select an EEG headset.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
					imageDisplay.add(images.get(DASH));
					counter.setText("Calibrating for 10 seconds. Please keep calm.");
				}
				catch(NumberFormatException ex){
					JOptionPane.showMessageDialog(contentPane, "Please input a number.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}

			private ActionListener init(DataCollect dc){
				this.dc = dc;
				return this;
			}
		}.init(this));

		epoc = new JRadioButton("EPOC");
		panel_1.add(epoc);

		neuroSky = new JRadioButton("NeuroSky");
		panel_1.add(neuroSky);
		panel_1.add(btnStartSession);

		JPanel panel = new JPanel();
		panel_1.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.SOUTH);

		JLabel lblImageOf = new JLabel("Image " + currentImageNumber + " of " + totalImageNumber);
		counter = lblImageOf;
		panel_2.add(lblImageOf);

		JPanel panel_3 = new JPanel();
		contentPane.add(panel_3, BorderLayout.CENTER);
		imageDisplay = panel_3;

		JLabel label = new JLabel("");
		panel_3.add(label);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(epoc);
		buttonGroup.add(neuroSky);

		images = new ArrayList<JLabel>();
		init();

		try{
			socket = new Socket("127.0.0.1", 80);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(contentPane, "Cannot connect to NeuroSky.", "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void init(){
		for(String s : NAMES){
			addNewImage(s);
		}
	}

	private void updateCounter(){
		counter.setText("Image " + currentImageNumber + " of " + totalImageNumber);
	}

	private void addNewImage(String name){
		try{
			BufferedImage image = ImageIO.read(new File(PATH + name));
			JLabel label = new JLabel(new ImageIcon(image));
			images.add(label);
		}
		catch(IOException e){
			System.out.println("Error: image initialization error");
			System.exit(1);
		}
	}

	private int genNextImage(){
		return (int) Math.random() * (images.size() - 2);
	}

	public abstract class RecordData<T, V>extends SwingWorker<Void, Integer>{

		private DataCollect parent;
		private int cycles;

		public RecordData(DataCollect parent){
			this(parent, -1);
		}

		public RecordData(DataCollect parent, int cycles){
			this.parent = parent;
			this.cycles = cycles;
		}

		public void start() throws Exception{
			if(cycles == -1){
				//TODO: Handle uninitialized
				return;
			}
			doInBackground();
		}
	}

	public class RecordNeuro<T, V>extends RecordData<Void, Integer>{

		private DataCollect parent;
		private BufferedReader br;
		private BufferedWriter bw;
		private int cycles;

		public RecordNeuro(DataCollect parent){
			this(parent, -1);
		}

		public RecordNeuro(DataCollect parent, int cycles){
			super(parent, cycles);
			try{
				InputStream is = parent.socket.getInputStream();
				OutputStream os = parent.socket.getOutputStream();
				br = new BufferedReader(new InputStreamReader(is));
				bw = new BufferedWriter(new OutputStreamWriter(os));
				@SuppressWarnings("serial")
				Map<String, String> settings = new LinkedHashMap<String, String>(){

					{
						put("enableRawOutput", "true");
						put("format", "Json");
					}
				};
				JSONObject settingsJ = new JSONObject(settings);
				String s = settingsJ.toString();
				if(s != null){
					bw.write(s);
				}
				else{
					System.out.println("oops");
					bw.write("{\"enableRawOutput\":true,\"format\":\"Json\"}");
				}
			}
			catch(IOException e){
				//TODO: show error box
			}
		}

		@Override
		protected Void doInBackground() throws Exception{
			int nextImage = (int) (Math.random() * 9);
			publish(nextImage);
			parent.updateCounter();
			Thread.sleep(500);
			return null;
		}

	}

	public class RecordEpoc<T, V>extends RecordData<Void, Integer>{

		public RecordEpoc(DataCollect parent){
			this(parent, -1);
		}

		public RecordEpoc(DataCollect parent, int cycles){
			super(parent, cycles);
		}

		@Override
		protected Void doInBackground() throws Exception{
			// TODO Auto-generated method stub
			return null;
		}

	}

}
