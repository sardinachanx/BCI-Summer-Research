package datacollect;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

import com.emotiv.edk.Edk;
import com.emotiv.edk.EdkErrorCode;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class DataCollect extends JFrame {

	private static final long serialVersionUID = -3478409869011707701L;
	private static final String PATH = "images/";
	private static final int DEFAULT_REPETITION = 20;
	private static final int INIT_CALIBRATION_TIME = 5000;
	private static final int EXPRESSION_TIME = 5000;
	private static final int CALM_TIME = 1000;
	private static final int DASH = 9;
	private static final int END = 10;
	private static final String[] NAMES = { "angry.jpg", "closeeye.jpg", "eyebrow.jpg", "mouthL.jpg", "mouthR.jpg",
			"smile.jpg", "surprise.jpg", "winkL.jpg", "winkR.jpg", "dash.jpg", "done.jpg" };

	private List<JLabel> images;

	private JPanel contentPane;
	private JTextField textField;
	private JPanel imageDisplay;
	private JButton startSession;
	private JLabel counter;
	// private ButtonGroup buttonGroup;
	private JRadioButton epoc;
	private JRadioButton neuroSky;
	private int currentImageNumber;
	private int totalImageNumber;
	private Socket socket;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		System.setProperty("jna.library.path",
				"C:\\Program Files (x86)\\Emotiv Education Edition SDK_v1.0.0.5-PREMIUM");

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					DataCollect frame = new DataCollect();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public DataCollect() {

		currentImageNumber = 0;
		totalImageNumber = DEFAULT_REPETITION;

		images = new ArrayList<JLabel>();
		init();

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
		startSession.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				currentImageNumber = 0;
				RecordData rd = null;
				try {
					totalImageNumber = Integer.parseInt(textField.getText());
					if (epoc.isSelected()) {
						rd = new RecordEpoc(totalImageNumber);
						rd.execute();
					} else if (neuroSky.isSelected()) {
						rd = new RecordNeuro(totalImageNumber);
						if (rd.readyToCollect) {
							rd.execute();
						} else {
							throw new Exception();
						}
						// System.out.println("it reached here: neurosky");
						// System.out.println("current: " + currentImageNumber +
						// " , total: " + totalImageNumber);
					} else {
						JOptionPane.showMessageDialog(contentPane, "Please select an EEG headset.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(contentPane, "Please input a number.", "Error",
							JOptionPane.ERROR_MESSAGE);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(contentPane, "Error starting up connection.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}

		});

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
		for (JLabel l : images) {
			imageDisplay.add(l);
		}
		imageDisplay.removeAll();

		JLabel label = new JLabel("");
		panel_3.add(label);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(epoc);
		buttonGroup.add(neuroSky);
	}

	private void init() {
		for (String s : NAMES) {
			addNewImage(s);
		}
	}

	private String updateCounter() {
		currentImageNumber++;
		return "Image " + currentImageNumber + " of " + totalImageNumber;
	}

	private void addNewImage(String name) {
		try {
			BufferedImage image = ImageIO.read(new File(PATH + name));
			JLabel label = new JLabel(new ImageIcon(image));
			images.add(label);
		} catch (IOException e) {
			System.out.println("Error: image initialization error");
			System.exit(1);
		}
	}

	private int genNextImage() {
		return (int) (Math.random() * (images.size() - 2));
	}

	private abstract class RecordData extends SwingWorker<Void, Data> {

		protected int cycles;
		List<List<String>> toWrite;
		boolean readyToCollect;

		public RecordData(int cycles) {
			this.cycles = cycles;
			toWrite = new ArrayList<List<String>>();
			readyToCollect = false;
		}

		@Override
		protected void process(List<Data> indexes) {
			for (Data d : indexes) {
				imageDisplay.removeAll();
				imageDisplay.add(images.get(d.index));
				repaint();
				counter.setText(d.message);
				System.out.println("processed " + d.index);
			}
		}

	}

	private class RecordNeuro extends RecordData {

		private BufferedReader br;
		private BufferedWriter bw;

		public RecordNeuro(int cycles) {
			super(cycles);
			try {
				socket = new Socket("127.0.0.1", 13854);
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
				br = new BufferedReader(new InputStreamReader(is));
				bw = new BufferedWriter(new OutputStreamWriter(os));
				bw.write("{\"enableRawOutput\":true,\"format\":\"Json\"}");
				bw.flush();
				readyToCollect = true;
			} catch (IOException e) {
				JOptionPane.showMessageDialog(contentPane, "Cannot connect to NeuroSky.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}

		@Override
		protected Void doInBackground() throws Exception {
			publish(new Data(DASH, "Calibrating for 20 seconds. Please keep calm."));
			List<String> current = new ArrayList<String>();
			current.add("=========CALIBRATION DATA=========");
			long time = System.currentTimeMillis();
			while (System.currentTimeMillis() - time < INIT_CALIBRATION_TIME) {
				String line = br.readLine();
				current.add(line);
				System.out.print("");
			}
			toWrite.add(current);
			for (int i = 0; i < cycles; i++) {
				int nextIndex = genNextImage();
				current = new ArrayList<String>();
				publish(new Data(nextIndex, updateCounter()));
				current.add("=========IMAGE " + currentImageNumber + ": " + NAMES[nextIndex] + "=========");
				time = System.currentTimeMillis();
				while (System.currentTimeMillis() - time < EXPRESSION_TIME) {
					current.add(br.readLine());
				}
				toWrite.add(current);
				time = System.currentTimeMillis();
				publish(new Data(DASH, ""));
				Thread.sleep(CALM_TIME);
			}
			publish(new Data(END, "Writing file"));
			Date date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
			File file = new File(dateFormat.format(date) + "-NeuroSky" + ".txt");
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			for (List<String> trials : toWrite) {
				for (String s : trials) {
					bw.write(s);
					bw.newLine();
				}
			}
			bw.close();
			publish(new Data(END, "File writing completed"));
			return null;
		}
	}

	private class RecordEpoc extends RecordData {

		private static final float BUFFER_SIZE = 0.25f;

		Pointer eEvent;
		Pointer hData;
		IntByReference userID;
		IntByReference nSamplesTaken;

		public RecordEpoc(int cycles) {
			super(cycles);
			eEvent = Edk.INSTANCE.EE_EmoEngineEventCreate();
			userID = new IntByReference();
			nSamplesTaken = new IntByReference();

			if (Edk.INSTANCE.EE_EngineConnect("Emotiv Systems-5") != EdkErrorCode.EDK_OK.ToInt()) {
				System.out.println("Emotiv Engine start up failed.");
				JOptionPane.showMessageDialog(contentPane, "Cannot connect to EPOC.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}

			hData = Edk.INSTANCE.EE_DataCreate();
			Edk.INSTANCE.EE_DataSetBufferSizeInSec(BUFFER_SIZE);
		}

		@Override
		protected Void doInBackground() throws Exception {
			List<String> current = new ArrayList<String>();
			long startTime = 0;
			int currentCycle = 0;
			int state = 0;
			int stage = 1;
			while (true) {
				state = Edk.INSTANCE.EE_EngineGetNextEvent(eEvent);
				
				if (state == EdkErrorCode.EDK_OK.ToInt()) {
					int eventType = Edk.INSTANCE.EE_EmoEngineEventGetType(eEvent);
					Edk.INSTANCE.EE_EmoEngineEventGetUserId(eEvent, userID);
					
					if (eventType == Edk.EE_Event_t.EE_UserAdded.ToInt()) {
						if (userID != null) {
							System.out.println("User added");
							Edk.INSTANCE.EE_DataAcquisitionEnable(userID.getValue(), true);
							readyToCollect = true;
						}
					}
				} else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
					System.out.println("Internal error in Emotiv Engine!");
					JOptionPane.showMessageDialog(contentPane, "Cannot connect to EPOC.", "Error",
							JOptionPane.ERROR_MESSAGE);
					break; 
				}
				if (readyToCollect) {
					switch (stage) {
					case 1:
						if (startTime == 0) {
							publish(new Data(DASH, "Calibrating for 20 seconds. Please keep calm."));
							current.add("=========CALIBRATION DATA=========");
							startTime = System.currentTimeMillis();
						} else if (System.currentTimeMillis() - startTime > INIT_CALIBRATION_TIME) {
							toWrite.add(current);
							current = new ArrayList<String>();
							stage++;
							currentCycle++;
							startTime = 0;
							System.out.println("moving on to stage 2");
							continue;
						}
						break;

					case 2:
						if (startTime == 0) {
							int nextIndex = genNextImage();
							publish(new Data(nextIndex, updateCounter()));
							current.add("=========IMAGE " + currentImageNumber + ": " + NAMES[nextIndex] + "=========");
							startTime = System.currentTimeMillis();
						} else if (System.currentTimeMillis() - startTime > EXPRESSION_TIME) {
							toWrite.add(current);
							current = new ArrayList<String>();
							currentCycle++;
							if (currentCycle > cycles) {
								stage++;
								System.out.println("moving on to stage 3");
								continue;
							} 
							else {
								startTime = 0;
								publish(new Data(DASH, ""));
								Thread.sleep(CALM_TIME);
							}
						}
						break;

					case 3:
						publish(new Data(END, "Writing file"));
						Date date = new Date();
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
						File file = new File(dateFormat.format(date) + "-EPOC" + ".txt");
						BufferedWriter bw = new BufferedWriter(new FileWriter(file));
						for (List<String> trials : toWrite) {
							for (String s : trials) {
								bw.write(s);
								bw.newLine();
							}
						}
						bw.close();
						publish(new Data(END, "File writing completed"));
						return null;
					}

					Edk.INSTANCE.EE_DataUpdateHandle(0, hData);
					Edk.INSTANCE.EE_DataGetNumberOfSample(hData, nSamplesTaken);
					if (nSamplesTaken != null && nSamplesTaken.getValue() != 0) {
						//System.out.print("Updated: ");
						//System.out.println(nSamplesTaken.getValue());
						double[] data = new double[nSamplesTaken.getValue()];
						for (int sampleIdx = 0; sampleIdx < nSamplesTaken.getValue(); ++sampleIdx) {
							for (int i = 0; i < 14; i++) {
								Edk.INSTANCE.EE_DataGet(hData, i, data, nSamplesTaken.getValue());
							}
						}
						current.add(Arrays.toString(data));
					}
				}
			}
			return null;
		}

	}

	private class Data {

		private int index;
		private String message;

		public Data(int index, String message) {
			this.index = index;
			this.message = message;
		}

	}

}
