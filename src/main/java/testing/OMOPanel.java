package testing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import stone.javatasks.helperclasses.RoundButton;

public class OMOPanel extends JPanel {

	
	private JPanel omoPanel;
	private JPanel holderPanel;
	private JPanel homeButtonPanel;
	private ArrayList<RoundButton> buttons;
	private JLabel cheese;
	
	private ImageIcon blue = new ImageIcon(TestingRoundButtons.class.getResource("button_round_blue_alpha_100.png"));
	private ImageIcon red = new ImageIcon(TestingRoundButtons.class.getResource("button_round_red_alpha_100.png"));
	private ImageIcon green = new ImageIcon(TestingRoundButtons.class.getResource("button_round_green_alpha_200.png"));
	
	public OMOPanel() {
		super();
		//setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		cheese = new JLabel("");
		
		holderPanel = new JPanel();
		holderPanel.setLayout(new GridLayout(3,1));
		
		homeButtonPanel = new JPanel();
		
		omoPanel = new JPanel();
		omoPanel.setPreferredSize(new Dimension(890,120));
		//omoPanel.setBorder(BorderFactory.createTitledBorder("Odd Man Out"));
		//omoPanel.setLayout(new GridLayout(1,8));
		
		buttons = new ArrayList<RoundButton>() {{
			add(new RoundButton(blue));
			add(new RoundButton(blue));
			add(new RoundButton(blue));
			add(new RoundButton(blue));
			add(new RoundButton(blue));
			add(new RoundButton(blue));
			add(new RoundButton(blue));
			add(new RoundButton(blue));
		}};
		
		for (RoundButton x : buttons) {
			x.setPressedIcon(red);
			omoPanel.add(x);
		}
		
		RoundButton homeButton = new RoundButton(green);
		
		RoundButton tempBut = new RoundButton(red);
		
		holderPanel.add(cheese);
		homeButtonPanel.add(homeButton);
		holderPanel.add(omoPanel);
		holderPanel.add(homeButtonPanel);
		
		this.add(holderPanel);
		
		buttons.get(4).setIcon(red);
	}
	
	
	
	public static void createAndShowGUI() {
		JFrame frame = new JFrame("TestingRoundButtons");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JComponent newContentPane = new OMOPanel();
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
