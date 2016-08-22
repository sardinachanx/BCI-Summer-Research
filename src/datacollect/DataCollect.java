package datacollect;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
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

import com.emotiv.edk.Edk;
import com.emotiv.edk.EdkErrorCode;
import com.emotiv.edk.EmoState;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class DataCollect extends JFrame {

	private static final long serialVersionUID = -3478409869011707701L;
	// Modify the following constants as needed
	private static final String HOME_PATH = "data/neurosky/";
	private static final String IMAGE_PATH = "resources/";
	private static final String FREQUENCY_PATH = HOME_PATH + "frequency.dat";
	private static final String EXP_FREQUENCY_PATH = HOME_PATH + "experimental/frequency.dat";
	private static final String CATALOG_PATH = HOME_PATH + "catalog.dat";
	private static final String EXP_CATALOG_PATH = HOME_PATH + "experimental/catalog.dat";
	private static final int DEFAULT_REPETITION = 10;
	private static final int INIT_WAIT_TIME = 2000;
	private static final int INIT_CALIBRATION_TIME = 10000;
	private static final int EXPRESSION_TIME = 5000;
	private static final int CALM_TIME = 2000;
	private static final boolean EXPERIMENTAL = true;

	private List<JLabel> images = new ArrayList<JLabel>();;

	private JPanel contentPane;
	private JTextField textField;
	private JPanel imageDisplay;
	private JButton startSession;
	private JLabel counter;
	private JRadioButton epoc;
	private JRadioButton neuroSky;
	private int currentImageNumber;
	private int totalImageNumber;
	private Socket socket;
	private JSONObject frequency;
	private JSONObject list;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		System.setProperty("jna.library.path",
				"C:\\Program Files (x86)\\Emotiv Education Edition SDK_v1.0.0.5-PREMIUM");
		// Change the second arg (the path) to where the .dll libraries are
		// located.
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					DataCollect frame = new DataCollect("Data Collection");
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
	public DataCollect(String name) {

		super(name);
		currentImageNumber = 0;
		totalImageNumber = DEFAULT_REPETITION;
		init();

		try {
			String s;
			if (EXPERIMENTAL) {
				s = readFrequencyData(new File(EXP_FREQUENCY_PATH));
			} else {
				s = readFrequencyData(new File(FREQUENCY_PATH));
			}
			frequency = new JSONObject(s);
			for (int i = 0; i < DataConstants.EFT_LENGTH; i++) {
				if (!frequency.has(DataConstants.NAMES[i])) {
					frequency.put(DataConstants.NAMES[i], 0);
				}
			}
		} catch (IOException e) {
			Map<String, Integer> newFrequency = new HashMap<String, Integer>();
			for (int i = 0; i < DataConstants.EFT_LENGTH; i++) {
				newFrequency.put(DataConstants.NAMES[i], 0);
			}
			frequency = new JSONObject(newFrequency);
		}

		try {
			String s;
			if (EXPERIMENTAL) {
				s = readFrequencyData(new File(EXP_CATALOG_PATH));
			} else {
				s = readFrequencyData(new File(CATALOG_PATH));
			}
			list = new JSONObject(s);
		} catch (IOException e) {
			list = new JSONObject();
		}

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
						rd = new NewRecordNeuro(totalImageNumber);
						if (rd.readyToCollect) {
							rd.execute();
						} else {
							throw new IllegalStateException();
						}
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

		if (satisfied()) {
			JOptionPane.showMessageDialog(contentPane, "All images already have 20 trials!", "Notification",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void init() {
		for (String s : DataConstants.NAMES) {
			addNewImage(s);
		}
	}

	private String updateCounter() {
		currentImageNumber++;
		return "Image " + currentImageNumber + " of " + totalImageNumber;
	}

	private void addNewImage(String name) {
		try {
			BufferedImage image = ImageIO.read(new File(IMAGE_PATH + name));
			JLabel label = new JLabel(new ImageIcon(image));
			images.add(label);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(contentPane, "Image initialization error.", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	private int genNextImage() {
		if (satisfied()) {
			JOptionPane.showMessageDialog(contentPane, "All images have 20 samples already!", "Notification",
					JOptionPane.INFORMATION_MESSAGE);
			return (int) (Math.random() * (DataConstants.EFT_LENGTH));
		}
		int random = (int) (Math.random() * (DataConstants.EFT_LENGTH));
		while (frequency.getInt(DataConstants.NAMES[random]) >= 20) {
			random = (int) (Math.random() * (DataConstants.EFT_LENGTH));
		}
		return random;
	}

	private boolean satisfied() {
		int count = 0;
		for (int i = 0; i < DataConstants.EFT_LENGTH; i++) {
			if (frequency.getInt(DataConstants.NAMES[i]) >= 20) {
				count++;
			}
		}
		if (count == DataConstants.EFT_LENGTH) {
			return true;
		}
		return false;
	}

	private String readFrequencyData(File file) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			return br.readLine();
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

	private abstract class RecordData extends SwingWorker<Void, Data> {

		protected int cycles;
		List<List<String>> toWrite = new ArrayList<List<String>>();
		boolean readyToCollect = false;

		public RecordData(int cycles) {
			this.cycles = cycles;
		}

		@Override
		protected void process(List<Data> indexes) {
			for (Data d : indexes) {
				imageDisplay.removeAll();
				imageDisplay.add(images.get(d.index));
				repaint();
				counter.setText(d.message);
			}
		}
	}

	/*
	 * @SuppressWarnings("unused") private class RecordNeuro extends RecordData
	 * {
	 * 
	 * private BufferedReader br; private BufferedWriter bw;
	 * 
	 * public RecordNeuro(int cycles) { super(cycles); try { socket = new
	 * Socket("127.0.0.1", 13854); InputStream is = socket.getInputStream();
	 * OutputStream os = socket.getOutputStream(); br = new BufferedReader(new
	 * InputStreamReader(is)); bw = new BufferedWriter(new
	 * OutputStreamWriter(os));
	 * bw.write("{\"enableRawOutput\":true,\"format\":\"Json\"}"); bw.flush();
	 * readyToCollect = true; } catch (IOException e) {
	 * JOptionPane.showMessageDialog(contentPane, "Cannot connect to NeuroSky.",
	 * "Error", JOptionPane.ERROR_MESSAGE); } }
	 * 
	 * @Override protected Void doInBackground() throws Exception { publish(new
	 * Data(DataConstants.DASH, "Calibrating for " + INIT_CALIBRATION_TIME /
	 * 1000 + " seconds. Please keep calm.")); List<String> current = new
	 * ArrayList<String>();
	 * current.add("==================CALIBRATION DATA=================="); long
	 * time = System.currentTimeMillis(); while (System.currentTimeMillis() -
	 * time < INIT_CALIBRATION_TIME) { String line = br.readLine();
	 * current.add((System.currentTimeMillis() - time) + line);
	 * System.out.print(""); } toWrite.add(current); for (int i = 0; i < cycles;
	 * i++) { int nextIndex = genNextImage(); current = new ArrayList<String>();
	 * publish(new Data(nextIndex, updateCounter()));
	 * current.add("==================IMAGE " + currentImageNumber + ": " +
	 * DataConstants.NAMES[nextIndex] + " (" + nextIndex + ")" +
	 * "=================="); time = System.currentTimeMillis(); while
	 * (System.currentTimeMillis() - time < EXPRESSION_TIME) {
	 * current.add(br.readLine()); } toWrite.add(current); time =
	 * System.currentTimeMillis(); publish(new Data(DataConstants.DASH, ""));
	 * Thread.sleep(CALM_TIME); } publish(new Data(DataConstants.DASH,
	 * "Writing file")); Date date = new Date(); SimpleDateFormat dateFormat =
	 * new SimpleDateFormat("yyyy-MM-dd HHmmss"); File file = new
	 * File(dateFormat.format(date) + "-NeuroSky" + ".txt"); BufferedWriter bw =
	 * new BufferedWriter(new FileWriter(file)); for (List<String> trials :
	 * toWrite) { for (String s : trials) { bw.write(s); bw.newLine(); } }
	 * bw.close(); publish(new Data(DataConstants.END,
	 * "File writing completed")); return null; } }
	 */

	private class NewRecordNeuro extends RecordData {

		private BufferedReader br;
		private BufferedWriter bw;
		private Runnable audio;

		public NewRecordNeuro(int cycles) {
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

		private void getAudio() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
			AudioInputStream ais = AudioSystem.getAudioInputStream(new File(IMAGE_PATH + "beep.wav"));
			Clip beep = AudioSystem.getClip();
			beep.open(ais);
			audio = new Runnable() {
				@Override
				public void run() {
					beep.start();
				}
			};
		}

		@Override
		protected Void doInBackground() throws Exception {
			publish(new Data(DataConstants.DASH, "Waiting..."));
			String line = br.readLine();
			while (!line.startsWith("{\"raw")) {
				line = br.readLine();
			}
			long time = System.currentTimeMillis();
			while (System.currentTimeMillis() - time < INIT_WAIT_TIME) {
				line = br.readLine();
			}
			publish(new Data(DataConstants.DASH, "Calibrating..."));
			List<String> name = new ArrayList<String>();
			name.add("calibration");
			toWrite.add(name);
			for (int i = 0; i < 20; i++) {
				int counter = 0;
				List<String> current = new ArrayList<String>();
				while (counter < 256) {
					line = br.readLine();
					if (line.startsWith("{\"raw")) {
						counter++;
					}
					current.add(line);
				}
				toWrite.add(current);
			}
			for (int times = 0; times < cycles; times++) {
				getAudio();
				audio.run();
				int nextIndex = genNextImage();
				publish(new Data(nextIndex, updateCounter()));
				frequency.increment(DataConstants.NAMES[nextIndex]);
				String photoNumber = DataConstants.NAMES[nextIndex] + " (" + nextIndex + ")";
				name = new ArrayList<String>();
				name.add(photoNumber);
				toWrite.add(name);
				for (int i = 0; i < 20; i++) {
					int counter = 0;
					List<String> current = new ArrayList<String>();
					while (counter < 256) {
						line = br.readLine();
						if (line.startsWith("{\"raw")) {
							counter++;
						}
						current.add(line);
					}
					toWrite.add(current);
				}
				getAudio();
				audio.run();
				publish(new Data(DataConstants.DASH, ""));
				time = System.currentTimeMillis();
				while (System.currentTimeMillis() - time < CALM_TIME) {
					br.readLine();
				}
			}
			publish(new Data(DataConstants.DASH, "Writing file"));
			Date date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			list.put(dateFormat.format(date), cycles);
			String sp = File.separator;
			String location;
			if (EXPERIMENTAL) {
				location = "data" + sp + "neurosky" + sp + "experimental" + sp + dateFormat.format(date) + sp;
			} else {
				location = "data" + sp + "neurosky" + sp + dateFormat.format(date) + sp;
			}
			BufferedWriter fw = null;
			for (int i = 0; i <= cycles; i++) {
				for (int j = 0; j < 21; j++) {
					List<String> write = toWrite.get(i * 21 + j);
					// System.out.println(write);
					File file = null;
					if (j == 0) {
						if (i == 0) {
							file = new File(location + "calib" + sp + "name.txt");
						} else {
							file = new File(location + i + sp + "name.txt");
						}

					} else {
						if (i == 0) {
							file = new File(location + "calib" + sp + j + ".json");
						} else {
							file = new File(location + i + sp + j + ".json");
						}
					}
					file.getParentFile().mkdirs();
					fw = new BufferedWriter(new FileWriter(file));
					for (String s : write) {
						fw.write(s);
						fw.newLine();
					}
					fw.close();
				}
			}
			if (EXPERIMENTAL) {
				fw = new BufferedWriter(new FileWriter(new File(EXP_FREQUENCY_PATH)));
			} else {
				fw = new BufferedWriter(new FileWriter(new File(FREQUENCY_PATH)));
			}
			fw.write(frequency.toString());
			fw.close();
			if (EXPERIMENTAL) {
				fw = new BufferedWriter(new FileWriter(new File(EXP_CATALOG_PATH)));
			} else {
				fw = new BufferedWriter(new FileWriter(new File(CATALOG_PATH)));
			}
			fw.write(list.toString());
			fw.close();
			publish(new Data(DataConstants.END, "File writing completed"));
			return null;
		}
	}

	private class RecordEpoc extends RecordData {

		private static final float BUFFER_SIZE = 0.25f;

		Pointer eEvent;
		Pointer eState;
		Pointer hData;
		IntByReference userID;
		IntByReference nSamplesTaken;
		List<List<String>> toWriteSuite = new ArrayList<List<String>>();

		public RecordEpoc(int cycles) {
			super(cycles);
			eEvent = Edk.INSTANCE.EE_EmoEngineEventCreate();
			eState = Edk.INSTANCE.EE_EmoStateCreate();
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
			String none = "null,0,";
			List<String> current = new ArrayList<String>();
			current.add(
					"COUNTER,INTERPOLATED,RAW_CQ,AF3,F7,F3,FC5,T7,P7,O1,O2,P8,T8,FC6,F4,F8,AF4,GYROX,GYROY,TIMESTAMP,ES_TIMESTAMPFUNC_ID,FUNC_VALUE,MARKER,SYNC_SIGNAL,IMAGE");
			List<String> currentSuite = new ArrayList<String>();
			currentSuite.add(
					"TIME,BLINK,EYES_OPEN,LEFT_WINK,LOOKING_DOWN,LOOKING_LEFT,LOOKING_RIGHT,LOOKING_UP,RIGHT_WINK,UPPER_FACE,UPPER_POWER,LOWER_FACE,LOWER_POWER,ENGAGEMENTBOREDOM,EXCITEMENT_SHORT,EXCITEMENT_LONG,FRUSTRATION,MEDITATION,COG_ACTION,COG_POWER,IMAGE");
			long startTime = 0;
			int currentCycle = 0;
			int state = 0;
			int stage = 1;
			int currentImage = DataConstants.DASH;
			/*
			 * Optional: User profile loading; replace "userID" with a valid
			 * user id and fileLocation with the profile path int userLoading =
			 * Edk.INSTANCE.EE_LoadUserProfile(userID, fileLocation);
			 * if(userLoading != EdkErrorCode.EDK_OK.ToInt()){
			 * JOptionPane.showMessageDialog(contentPane,
			 * "Cannot load specified user profile.", "Error",
			 * JOptionPane.ERROR_MESSAGE); }
			 */
			while (true) {
				state = Edk.INSTANCE.EE_EngineGetNextEvent(eEvent);
				if (state == EdkErrorCode.EDK_OK.ToInt()) {
					int eventType = Edk.INSTANCE.EE_EmoEngineEventGetType(eEvent);
					Edk.INSTANCE.EE_EmoEngineEventGetUserId(eEvent, userID);
					if (eventType == Edk.EE_Event_t.EE_UserAdded.ToInt()) {
						if (userID != null) {
							Edk.INSTANCE.EE_DataAcquisitionEnable(userID.getValue(), true);
							readyToCollect = true;
						}
					}
					if (eventType == Edk.EE_Event_t.EE_EmoStateUpdated.ToInt()) {
						StringBuilder sb = new StringBuilder();
						sb.append(EmoState.INSTANCE.ES_GetTimeFromStart(eState) + ",");
						Edk.INSTANCE.EE_EmoEngineEventGetEmoState(eEvent, eState);
						if (EmoState.INSTANCE.ES_ExpressivIsBlink(eState) == 1) {
							sb.append("True,");
						} else {
							sb.append("False,");
						}
						if (EmoState.INSTANCE.ES_ExpressivIsEyesOpen(eState) == 1) {
							sb.append("True,");
						} else {
							sb.append("False,");
						}
						if (EmoState.INSTANCE.ES_ExpressivIsLeftWink(eState) == 1) {
							sb.append("True,");
						} else {
							sb.append("False,");
						}
						if (EmoState.INSTANCE.ES_ExpressivIsLookingDown(eState) == 1) {
							sb.append("True,");
						} else {
							sb.append("False,");
						}
						if (EmoState.INSTANCE.ES_ExpressivIsLookingLeft(eState) == 1) {
							sb.append("True,");
						} else {
							sb.append("False,");
						}
						if (EmoState.INSTANCE.ES_ExpressivIsLookingRight(eState) == 1) {
							sb.append("True,");
						} else {
							sb.append("False,");
						}
						if (EmoState.INSTANCE.ES_ExpressivIsLookingUp(eState) == 1) {
							sb.append("True,");
						} else {
							sb.append("False,");
						}
						if (EmoState.INSTANCE.ES_ExpressivIsRightWink(eState) == 1) {
							sb.append("True,");
						} else {
							sb.append("False,");
						}

						String upperState = reverseLookupExpressiv(
								EmoState.INSTANCE.ES_ExpressivGetUpperFaceAction(eState));
						if (upperState != null) {
							sb.append(upperState + "," + EmoState.INSTANCE.ES_ExpressivGetUpperFaceActionPower(eState)
									+ ",");
						} else {
							sb.append(none);
						}
						String lowerState = reverseLookupExpressiv(
								EmoState.INSTANCE.ES_ExpressivGetLowerFaceAction(eState));
						if (lowerState != null) {
							sb.append(lowerState + "," + EmoState.INSTANCE.ES_ExpressivGetLowerFaceActionPower(eState)
									+ ",");
						} else {
							sb.append(none);
						}
						sb.append(EmoState.INSTANCE.ES_AffectivGetEngagementBoredomScore(eState) + ",");
						sb.append(EmoState.INSTANCE.ES_AffectivGetExcitementShortTermScore(eState) + ",");
						sb.append(EmoState.INSTANCE.ES_AffectivGetExcitementLongTermScore(eState) + ",");
						sb.append(EmoState.INSTANCE.ES_AffectivGetFrustrationScore(eState) + ",");
						sb.append(EmoState.INSTANCE.ES_AffectivGetMeditationScore(eState) + ",");

						String action = reverseLookupCognitiv(EmoState.INSTANCE.ES_CognitivGetCurrentAction(eState));
						if (action != null) {
							sb.append(action + "," + EmoState.INSTANCE.ES_CognitivGetCurrentActionPower(eState) + ",");
						} else {
							sb.append(none);
						}
						if (currentImage == DataConstants.DASH) {
							sb.append(0);
						} else {
							int imageNo = currentImage + 1;
							sb.append(imageNo);
						}
						currentSuite.add(sb.toString());
					}
				} else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
					JOptionPane.showMessageDialog(contentPane, "Cannot connect to EPOC. Check your device.", "Error",
							JOptionPane.ERROR_MESSAGE);
					break;
				}
				if (readyToCollect) {
					switch (stage) {
					case 1:
						if (startTime == 0) {
							publish(new Data(DataConstants.DASH, "Calibrating for 20 seconds. Please keep calm."));
							startTime = System.currentTimeMillis();
						} else if (System.currentTimeMillis() - startTime > INIT_CALIBRATION_TIME) {
							toWrite.add(current);
							toWriteSuite.add(currentSuite);
							current = new ArrayList<String>();
							currentSuite = new ArrayList<String>();
							stage++;
							currentCycle++;
							startTime = 0;
							continue;
						}
						break;
					case 2:
						if (startTime == 0) {
							currentImage = genNextImage();
							publish(new Data(currentImage, updateCounter()));
							startTime = System.currentTimeMillis();
						} else if (System.currentTimeMillis() - startTime > EXPRESSION_TIME) {
							toWrite.add(current);
							toWriteSuite.add(currentSuite);
							current = new ArrayList<String>();
							currentSuite = new ArrayList<String>();
							currentCycle++;
							if (currentCycle > cycles) {
								stage++;
							} else {
								startTime = 0;
								publish(new Data(DataConstants.DASH, ""));
								Thread.sleep(CALM_TIME);
							}
							continue;
						}
						break;
					case 3:
						publish(new Data(DataConstants.DASH, "Writing file"));
						Date date = new Date();
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
						File file = new File("data" + File.separator + "epoc" + File.separator + dateFormat.format(date)
								+ File.separator + "EPOC_RAW" + ".csv");
						file.getParentFile().mkdirs();
						BufferedWriter bw = new BufferedWriter(new FileWriter(file));
						for (List<String> trials : toWrite) {
							for (String s : trials) {
								bw.write(s);
								bw.newLine();
							}
						}
						file = new File("data" + File.separator + "epoc" + File.separator + dateFormat.format(date)
								+ File.separator + "EPOC_SUITE" + ".csv");
						file.getParentFile().mkdirs();
						bw = new BufferedWriter(new FileWriter(file));
						for (List<String> trials : toWriteSuite) {
							for (String s : trials) {
								bw.write(s);
								bw.newLine();
							}
						}
						bw.close();
						publish(new Data(DataConstants.END, "File writing completed"));
						return null;
					}
					Edk.INSTANCE.EE_DataUpdateHandle(0, hData);
					Edk.INSTANCE.EE_DataGetNumberOfSample(hData, nSamplesTaken);
					if (nSamplesTaken != null && nSamplesTaken.getValue() != 0) {
						double[] data = new double[nSamplesTaken.getValue()];
						for (int sampleIdx = 0; sampleIdx < nSamplesTaken.getValue(); ++sampleIdx) {
							List<Double> set = new ArrayList<Double>();
							for (int i = 0; i < 24; i++) {
								Edk.INSTANCE.EE_DataGet(hData, i, data, nSamplesTaken.getValue());
								set.add(data[sampleIdx]);
							}
							StringBuilder sb = new StringBuilder();
							sb.append(rawEEGToString(set));
							if (currentImage == DataConstants.DASH) {
								sb.append(0);
							} else {
								int imageNo = currentImage + 1;
								sb.append(imageNo);
							}
							current.add(sb.toString());
						}
					}
				}
			}
			return null;
		}

		private String reverseLookupExpressiv(int value) {
			for (EmoState.EE_ExpressivAlgo_t state : EmoState.EE_ExpressivAlgo_t.values()) {
				if (value == state.ToInt()) {
					return state.name();
				}
			}
			return null;
		}

		private String reverseLookupCognitiv(int value) {
			for (EmoState.EE_CognitivAction_t action : EmoState.EE_CognitivAction_t.values()) {
				if (value == action.ToInt()) {
					return action.name();
				}
			}
			return null;
		}

		private String rawEEGToString(List<Double> list) {
			String s = "";
			for (int i = 0; i < list.size(); i++) {
				s += list.get(i) + "";
			}
			return s;
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