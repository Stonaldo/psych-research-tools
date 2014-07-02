package testing;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import stone.javatasks.helperclasses.GridSpanStimulus;

public class TestingGridSpanStimulus {
	
	public static void main(String[] args) {
		
		System.out.println("lookandfeel: " + UIManager.getLookAndFeel());
		
		GridSpanStimulus testingPanel = new GridSpanStimulus(8,400);
		
		JFrame myFrame = new JFrame();
		myFrame.setSize(1200,1000);
		
		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myFrame.setVisible(true);
		
		JPanel tep = new JPanel();
		tep.setSize(new Dimension(1200,1000));
		tep.setBackground(Color.BLACK);
		tep.add(testingPanel);
		
		myFrame.add(tep);
		
		testingPanel.fillListButtons(testingPanel.getPattern(true, 12), Color.BLACK);
	}

}
