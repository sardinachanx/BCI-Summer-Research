package gui.strokeinput;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SoundPlayer {
	
	private BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
	private boolean closed = false;
	private static final int PLAY_TIME = 250;
	
	//int runs = 0;
	//int RUN_THRESHOLD = 20;
	
	private static Object audioLock = new Object();
	
	public SoundPlayer(){
		
	}
	
	public void start(){
		Thread t = new Thread(this.new SoundPlayerRunner());
		t.start();
	}
	
	public void play(int offset){
		queue.add(offset);
	}
	
	private class SoundPlayerRunner implements Runnable{
		
		@Override
		public void run(){
			SourceDataLine line = null;
			while(!closed){
				try{
					final AudioFormat af =
				            new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, true);
				        line = AudioSystem.getSourceDataLine(af);
				        line.open(af, Note.SAMPLE_RATE);
				        line.start();
					while(!closed){
						try{
							int offset = queue.take();
							//runs++;
							//if(runs > RUN_THRESHOLD){
						    //    runs = 0;
							//}
							//System.out.println("a:" + offset);
							synchronized(audioLock){
						        //line.drain();
								play(line, note(offset), PLAY_TIME);
								Thread.sleep(PLAY_TIME);
								line.drain();
							}
						}
						catch(InterruptedException e){
							e.printStackTrace();
						}
					}
					line.close();
				} catch (LineUnavailableException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				finally{
					if(line != null){
						line.close();
					}
				}
			}
		}
	}
	
	public void close(){
		closed = true;
	}

    public static void main0(String[] args) throws LineUnavailableException {
        final AudioFormat af =
            new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, true);
        SourceDataLine line = AudioSystem.getSourceDataLine(af);
        line.open(af, Note.SAMPLE_RATE);
        line.start();
        for  (Note n : Note.values()) {
            play(line, n, 500);
            play(line, Note.REST, 10);
        }
        for(int i = 0; i < 100; i++){
        	play(line, note(i), 500);
        }
        line.drain();
        line.close();
    }

    private static void play(SourceDataLine line, Note note, int ms) {
        play(line, note, ms, true);
    }
    
    private static void play(SourceDataLine line, Note note, int ms, 
    		boolean padRest){
    	ms = Math.min(ms, Note.SECONDS * 1000);
        int length = Note.SAMPLE_RATE * ms / 1000;
        int count = line.write(note.data(), 0, length);
        if(padRest){
        	play(line, Note.REST, 10, false);
        }
    }
    
    private static void play(SourceDataLine line, byte[] note, int ms) {
        ms = Math.min(ms, Note.SECONDS * 1000);
        int length = Note.SAMPLE_RATE * ms / 1000;
        int count = line.write(note, 0, length);
    }
    
    public static final int SAMPLE_RATE = 16 * 1024; // ~16KHz
    public static final int SECONDS = 2;
    
    private static byte[] note(int offset){
    	byte[] sin = new byte[SECONDS * SAMPLE_RATE];
    	int n = offset;
    	double exp = ((double) n) / 12d;
        double f = 440d * Math.pow(2d, exp);
        for (int i = 0; i < sin.length; i++) {
            double period = (double)SAMPLE_RATE / f;
            double angle = 2.0 * Math.PI * i / period;
            sin[i] = (byte)(Math.sin(angle) * 127f);
        }
        return sin;
    }
}

enum Note {

    REST, A4, A4$, B4, C4, C4$, D4, D4$, E4, F4, F4$, G4, G4$, A5;
    public static final int SAMPLE_RATE = 16 * 1024; // ~16KHz
    public static final int SECONDS = 2;
    private byte[] sin = new byte[SECONDS * SAMPLE_RATE];

    Note() {
        int n = this.ordinal();
        if (n > 0) {
            double exp = ((double) n - 1) / 12d;
            double f = 440d * Math.pow(2d, exp);
            for (int i = 0; i < sin.length; i++) {
                double period = (double)SAMPLE_RATE / f;
                double angle = 2.0 * Math.PI * i / period;
                sin[i] = (byte)(Math.sin(angle) * 127f);
            }
        }
    }

    public byte[] data() {
        return sin;
    }
}
