package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;
import javax.swing.SwingWorker;

//Parameter 2 is the state to switch to
public class TimeBasedInputSwitcher extends SwingWorker<Void, Integer>{

	protected InputSelector selector;
	protected long delay;
	protected long lastWait = 0;

	private int current;

	public TimeBasedInputSwitcher(InputSelector selector, long delay){
		this.selector = selector;
		this.delay = delay;
	}

	@Override
	public void process(List<Integer> values){
		selector.setInput(values.get(values.size() - 1));
	}

	@Override
	protected Void doInBackground(){
		try{
			while(true){
				selector.setInput(current);
				current++;
				current = current % selector.getInputCount();
				lastWait = System.currentTimeMillis();
				Thread.sleep(delay);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public JPanel newPanel(){
		return this.new TimeBasedInputSwitcherPanel();
	}

	private class TimeBasedInputSwitcherPanel extends JPanel{

		private static final long serialVersionUID = 7234269093243174022L;

		public TimeBasedInputSwitcherPanel(){
			//setBackground(Color.gray);
			setPreferredSize(new Dimension(25, 25));
			setMinimumSize(getPreferredSize());
			new Timer().scheduleAtFixedRate(new TimerTask(){

				@Override
				public void run(){
					repaint();
				}

			}, 0, 10);
		}

		@Override
		public void paint(Graphics g){
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(Color.black);
			g2d.fillArc(0, 0, getWidth() - 2, getHeight() - 2, 90,
					(int) (getPart() * 360.0));
			g2d.drawOval(0, 0, getWidth() - 2, getHeight() - 2);
		}

		protected double getPart(){
			return (System.currentTimeMillis() - lastWait) / (double) delay;
		}
	}

}
