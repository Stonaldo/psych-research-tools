package testing;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class TestingLayeredPanelForInspectionTime extends JPanel {
	
	private JLabel labelOne;
	private JLabel labelTwo;
	private JLayeredPane layeredPane;
	
	
	public TestingLayeredPanelForInspectionTime() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		final ImageIcon ITstim = createImageIcon("inspection_time_left.png");
		final ImageIcon occluder = createImageIcon("occlude.png");
		
		layeredPane = new JLayeredPane();
		layeredPane.setBorder(BorderFactory.createTitledBorder("Inspection Time"));
		layeredPane.setPreferredSize(new Dimension(320, 550));
		
		labelOne = new JLabel(ITstim);
		//labelOne.setBorder(BorderFactory.createLineBorder(Color.black));
		
		labelTwo = new JLabel(occluder);
		//labelTwo.setPreferredSize(new Dimension(320, 250));
		//labelTwo.setBackground(Color.WHITE);
		//labelTwo.setBorder(BorderFactory.createLineBorder(Color.black));
		
		labelOne.setBounds(10,25,300,500);
		labelTwo.setBounds(0,300,320,250);
		
		layeredPane.add(labelOne);
		layeredPane.add(labelTwo);
		
		labelTwo.setOpaque(true);
		
		layeredPane.moveToFront(labelTwo);
		layeredPane.moveToBack(labelOne);
		layeredPane.setBackground(Color.WHITE);
		
		add(layeredPane);
		
	}
	
	public static void createAndShowGUI() {
		JFrame frame = new JFrame("InspectionTimeDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JComponent newContentPane = new TestingLayeredPanelForInspectionTime();
		newContentPane.setOpaque(true);
		frame.setContentPane(newContentPane);
		
		frame.setBackground(Color.WHITE);
		
		frame.pack();
		frame.setVisible(true);
		
	}
	

	
    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = TestingLayeredPanelForInspectionTime.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }	

	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}

}
