package gui.strokeinput;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import experimental.CommandLineConvert.PredictionListener;

public class KeyboardSelectionController{

	private StrokeInput binding;
	private PredictionListener tester;

	public KeyboardSelectionController(StrokeInput binding){
		this.binding = binding;
	}

	public void setPredictionListener(PredictionListener test){
		tester = test;
	}

	public PredictionListener getPredictionListener(){
		return tester;
	}

	public void bind(){
		binding.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
		binding.getRootPane().getActionMap().put("left", new AbstractAction(){

			private static final long serialVersionUID = 7059681151726648882L;

			@Override
			public void actionPerformed(ActionEvent e){
				System.out.println("left");
				binding.selectLeft();
			}

		});

		binding.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
		binding.getRootPane().getActionMap().put("right", new AbstractAction(){

			private static final long serialVersionUID = 7059681151726648882L;

			@Override
			public void actionPerformed(ActionEvent e){
				System.out.println("right");
				binding.selectRight();
			}

		});

		binding.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
		binding.getRootPane().getActionMap().put("up", new AbstractAction(){

			private static final long serialVersionUID = 7059681151726648882L;

			@Override
			public void actionPerformed(ActionEvent e){
				System.out.println("up");
				binding.selectUp();
			}

		});

		binding.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
		binding.getRootPane().getActionMap().put("down", new AbstractAction(){

			private static final long serialVersionUID = 7059681151726648882L;

			@Override
			public void actionPerformed(ActionEvent e){
				System.out.println("down");
				if(tester != null){
					tester.inputPredicted(0, Arrays.asList("2", "2"));
				}
				else{
					binding.selectDown();
				}
			}

		});

		binding.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
		binding.getRootPane().getActionMap().put("enter", new AbstractAction(){

			private static final long serialVersionUID = 7059681151726648882L;

			@Override
			public void actionPerformed(ActionEvent e){
				System.out.println("enter");
				if(tester != null){
					tester.blinkDetected(100);
					tester.blinkDetected(100);
				}
				else{
					binding.selectEnter();
				}
			}

		});

		binding.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0), "slash");
		binding.getRootPane().getActionMap().put("slash", new AbstractAction(){

			private static final long serialVersionUID = 7059681151726648882L;

			@Override
			public void actionPerformed(ActionEvent e){
				System.out.println("page");
				if(tester != null){
					tester.inputPredicted(0, Arrays.asList("5", "5"));
				}
				else{
					binding.nextPage();
				}
			}

		});
	}
}
