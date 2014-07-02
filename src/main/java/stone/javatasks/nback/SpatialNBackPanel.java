package stone.javatasks.nback;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import stone.javatasks.helperclasses.RoundButton;


/**
 * This class will create and manage a JPanel that can be used 
 * to manage a single spatial n-back task. 
 * 
 * @author James Stone
 *
 */
public class SpatialNBackPanel extends JPanel{

	private ImageIcon red = new ImageIcon(getClass().getResource("/stimuli/imgs/buttons/button_round_red_alpha_100.png"));;
	private ImageIcon green = new ImageIcon(getClass().getResource("/stimuli/imgs/buttons/button_round_green_alpha_100.png"));
	private ArrayList<RoundButton> locations;
	private ArrayList<int[]> coords;
	
	
	/**
	 * Default constructor for SpatialNBackPanel
	 */
	
	public SpatialNBackPanel() {
		super();
		this.setLayout(null);
		
		this.setPreferredSize(new Dimension(800,600));
		this.setMaximumSize(new Dimension(800, 600));
		
		locations = new ArrayList<RoundButton>();
		coords = new ArrayList<int[]>();
		
		for (int i = 0; i < 9; i++) {
			locations.add(new RoundButton(red));
		}
		
		//initialise coordinates
		int[] c1 = { 40, 40 };
		int[] c2 = { 300, 70 };
		int[] c3 = { 560, 65 };
		int[] c4 = { 660, 185 };
		int[] c5 = { 75, 340 };
		int[] c6 = { 235, 200 };
		int[] c7 = { 455, 300 };
		int[] c8 = { 300, 400 };
		int[] c9 = { 600, 450 };
		
		coords.add(c1); coords.add(c2); coords.add(c3);
		coords.add(c4); coords.add(c5); coords.add(c6); 
		coords.add(c7); coords.add(c8); coords.add(c9);
		
		Dimension sizeOfLocation = locations.get(0).getPreferredSize();
		
		for (int i = 0; i < 9; i++) {
			locations.get(i).setBounds(coords.get(i)[0], coords.get(i)[1], sizeOfLocation.width, sizeOfLocation.height);;
			add(locations.get(i));
		}
		
		repaint();
	}
	
	public void light(int loci) {
		locations.get(loci).setIcon(green);
	}
	
	public void unlight(int loci) {
		locations.get(loci).setIcon(red);
	}
	
	
	
}
