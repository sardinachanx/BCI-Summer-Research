package gui.strokeinput;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.List;

import experimental.CommandLineConvert;
import experimental.CommandLineConvert.PredictionListener;

public class NeuroskySelectionController implements PredictionListener{

	private static int RIGHT_DELAY = 1000;
	private boolean closed;
	private StrokeInput binding;
	private String model;

	private static final String EYES_CLOSED = "2";
	private static final String EYES_OPEN = "3";
	private static final String LEFT_WINK = "4";
	private static final String RIGHT_WINK = "5";

	private long lastBlinkTime = 0;
	private static final long BLINK_THRESHOLD = 1000;
	private static final int STRENGTH_THRESHOLD = 65;
	
	private boolean redundancyOn = false;

	public NeuroskySelectionController(StrokeInput binding, String modelFile){
		this.binding = binding;
		model = modelFile;
	}

	private long delayEnd;

	public void bind(){
		try{
			Runnable realTime = () -> CommandLineConvert.realTime(model,
					Arrays.asList(this));
			Thread t0 = new Thread(realTime);
			t0.start();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		Runnable runnable = () -> {
			while(!closed){
				//System.out.println("tick");
				delayEnd = System.currentTimeMillis() + RIGHT_DELAY;
				try{
					while(System.currentTimeMillis() < delayEnd){
						Thread.sleep(10);
					}
				}
				catch(InterruptedException e){
					System.out.println("Interrupted!");
				}
				binding.selectRight();
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();

	}

	public void close(){
		closed = true;
	}

	String lastPredicted = null;

	@Override
	public void inputPredicted(int currentRun, List<String> predictedInput){
		EventQueue.invokeLater(() -> {
			try{
				if(predictedInput.size() == 1 && redundancyOn){
					String predicted = predictedInput.get(0);
					if(lastPredicted == null || !lastPredicted.equals(predicted)){
						lastPredicted = predicted;
						return;
					}
					lastPredicted = predicted;
					if(predicted.equals(EYES_CLOSED)){
						binding.selectDown();
						lastPredicted = null;
						delayEnd = System.currentTimeMillis() + RIGHT_DELAY * 2;
					}
					else if(predicted.equals(EYES_OPEN)){
						//Do nothing
					}
					else if(predicted.equals(LEFT_WINK)){
						//Do nothing
					}
					else if(predicted.equals(RIGHT_WINK)){
						binding.nextPage();
						lastPredicted = null;
						delayEnd = System.currentTimeMillis() + RIGHT_DELAY * 2;
					}
					else{
						System.out.println("Predicted not found!");
					}
				}
				else{
					String predicted;
					if(predictedInput.size() == 2){
						String predicted0 = predictedInput.get(0);
						String predicted1 = predictedInput.get(1);
						if(!predicted0.equals(predicted1)){
							return;
						}
						predicted = predicted0;
					}
					else{
						predicted = predictedInput.get(0);
					}
					//if(lastPredicted == null || !lastPredicted.equals(predicted)){
					//	lastPredicted = predicted;
					//	return;
					//}
					lastPredicted = predicted;
					if(predicted.equals(EYES_CLOSED)){
						binding.selectDown();
						lastPredicted = null;
						delayEnd = System.currentTimeMillis() + RIGHT_DELAY * 2;
					}
					else if(predicted.equals(EYES_OPEN)){
						//Do nothing
					}
					else if(predicted.equals(LEFT_WINK)){
						//Do nothing
					}
					else if(predicted.equals(RIGHT_WINK)){
						boolean b = binding.nextPage();
						lastPredicted = null;
						if(b){ 
							delayEnd = System.currentTimeMillis() + RIGHT_DELAY * 2;
						}
					}
					else{
						System.out.println("Predicted not found!");
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		});
	}

	@Override
	public void blinkDetected(int strength){
		if(strength < STRENGTH_THRESHOLD){
			return;
		}
		long time = System.currentTimeMillis();
		if(time - lastBlinkTime < BLINK_THRESHOLD){
			binding.selectEnter();
			delayEnd = System.currentTimeMillis() + RIGHT_DELAY * 2;
			lastBlinkTime = 0;
		}
		else{
			lastBlinkTime = time;
		}
	}

}
