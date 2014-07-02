package testing;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import stone.javatasks.helperclasses.RoundButton;

public class TestingRoundButtons extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private RoundButton but1;
	private RoundButton but2;
	private RoundButton but3;
	private RoundButton but4;
	private RoundButton but5;
	private RoundButton but6;
	
	private ImageIcon blue = new ImageIcon(TestingRoundButtons.class.getResource("button_round_blue_alpha_100.png"));
	private ImageIcon blueAlpha = new ImageIcon(TestingRoundButtons.class.getResource("button_round_blue_alpha_200.png"));
	private ImageIcon red = new ImageIcon(TestingRoundButtons.class.getResource("button_round_red_alpha_100.png"));
	private ImageIcon redAlpha = new ImageIcon(TestingRoundButtons.class.getResource("button_round_red_alpha_200.png"));
	private ImageIcon green = new ImageIcon(TestingRoundButtons.class.getResource("button_round_green_alpha_100.png"));
	private ImageIcon greenAlpha = new ImageIcon(TestingRoundButtons.class.getResource("button_round_green_alpha_200.png"));
	
	public TestingRoundButtons() {
		super();
		this.setBorder(BorderFactory.createTitledBorder("Round Buttonzzz"));
		this.setPreferredSize(new Dimension(1400,600));
		
		but1 = new RoundButton(blue);
		but2 = new RoundButton(blueAlpha);
		but3 = new RoundButton(green);
		but4 = new RoundButton(greenAlpha);
		but5 = new RoundButton(red);
		but6 = new RoundButton(redAlpha);
		
		but1.setPressedIcon(red);
		but2.setPressedIcon(green);
		but3.setPressedIcon(blue);
		
		but1.setRolloverIcon(green);
		but2.setRolloverIcon(blue);
		but3.setRolloverIcon(red);
		
		but1.setBounds(20,20,200,200);
		
		this.add(but1);
		this.add(but2);
		this.add(but3);
		this.add(but4);
		this.add(but5);
		this.add(but6);
	}
	
	
	public static void createAndShowGUI() {
		JFrame frame = new JFrame("TestingRoundButtons");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JComponent newContentPane = new TestingRoundButtons();
		newContentPane.setOpaque(true);
		frame.setContentPane(newContentPane);
		
		frame.setBackground(Color.WHITE);
		
		frame.pack();
		frame.setVisible(true);
	}
		
	
	
	public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });		
	}

}
