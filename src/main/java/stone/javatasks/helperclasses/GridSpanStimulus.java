package stone.javatasks.helperclasses;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * class for creating a panel that will be used to display stimuli in 
 * symmetry span and mastrix span tasks.
 * 
 * @author James Stone
 */

public class GridSpanStimulus extends JPanel {
	
	//enough buttons to go as far as an 8x8 grid//
	private JButton box_1 = new JButton();private JButton box_2 = new JButton();private JButton box_3 = new JButton();private JButton box_4 = new JButton();
	private JButton box_5 = new JButton();private JButton box_6 = new JButton();private JButton box_7 = new JButton();private JButton box_8 = new JButton();
	private JButton box_9 = new JButton();private JButton box_10 = new JButton();private JButton box_11 = new JButton();private JButton box_12 = new JButton();
	private JButton box_13 = new JButton();private JButton box_14 = new JButton();private JButton box_15 = new JButton();private JButton box_16 = new JButton();
	private JButton box_17 = new JButton();private JButton box_18 = new JButton();private JButton box_19 = new JButton();private JButton box_20 = new JButton();
	private JButton box_21 = new JButton();private JButton box_22 = new JButton();private JButton box_23 = new JButton();private JButton box_24 = new JButton();
	private JButton box_25 = new JButton();private JButton box_26 = new JButton();private JButton box_27 = new JButton();private JButton box_28 = new JButton();
	private JButton box_29 = new JButton();private JButton box_30 = new JButton();private JButton box_31 = new JButton();private JButton box_32 = new JButton();
	private JButton box_33 = new JButton();private JButton box_34 = new JButton();private JButton box_35 = new JButton();private JButton box_36 = new JButton();
	private JButton box_37 = new JButton();private JButton box_38 = new JButton();private JButton box_39 = new JButton();private JButton box_40 = new JButton();
	private JButton box_41 = new JButton();private JButton box_42 = new JButton();private JButton box_43 = new JButton();private JButton box_44 = new JButton();
	private JButton box_45 = new JButton();private JButton box_46 = new JButton();private JButton box_47 = new JButton();private JButton box_48 = new JButton();
	private JButton box_49 = new JButton();private JButton box_50 = new JButton();private JButton box_51 = new JButton();private JButton box_52 = new JButton();
	private JButton box_53 = new JButton();private JButton box_54 = new JButton();private JButton box_55 = new JButton();private JButton box_56 = new JButton();
	private JButton box_57 = new JButton();private JButton box_58 = new JButton();private JButton box_59 = new JButton();private JButton box_60 = new JButton();
	private JButton box_61 = new JButton();private JButton box_62 = new JButton();private JButton box_63 = new JButton();private JButton box_64 = new JButton();
	
	private ArrayList<JButton> buttons = new ArrayList<JButton>();
	
	/*constructor*/
	public GridSpanStimulus(int num_boxes, int panel_size) {
		super();
		setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		//set size of panel//
		this.setPreferredSize(new Dimension(panel_size + num_boxes, panel_size + num_boxes));
		this.setMaximumSize(new Dimension(panel_size + num_boxes, panel_size + num_boxes));
		this.setBackground(Color.WHITE);
		System.out.println(this.getLayout());
		
		//initialise the JButtons we need and add them to an arraylist//
		buttons.add(box_1);buttons.add(box_2);buttons.add(box_3);buttons.add(box_4);
		buttons.add(box_5);buttons.add(box_6);buttons.add(box_7);buttons.add(box_8);
		buttons.add(box_9);
		//if num_boxes is > 3 then we need at least 16 buttons//
		if (num_boxes > 3) {
			buttons.add(box_10);buttons.add(box_11);buttons.add(box_12);buttons.add(box_13);
			buttons.add(box_14);buttons.add(box_15);buttons.add(box_16);
		}
		if(num_boxes > 4) {
			buttons.add(box_17);buttons.add(box_18);buttons.add(box_19);buttons.add(box_20);
			buttons.add(box_21);buttons.add(box_22);buttons.add(box_23);buttons.add(box_24);buttons.add(box_25);
		}
		if(num_boxes > 5) {
			buttons.add(box_26);buttons.add(box_27);buttons.add(box_28);buttons.add(box_29);
			buttons.add(box_30);buttons.add(box_31);buttons.add(box_32);buttons.add(box_33);
			buttons.add(box_34);buttons.add(box_35);buttons.add(box_36);
		}
		if(num_boxes > 6) {
			buttons.add(box_37);buttons.add(box_38);buttons.add(box_39);buttons.add(box_40);
			buttons.add(box_41);buttons.add(box_42);buttons.add(box_43);buttons.add(box_44);
			buttons.add(box_45);buttons.add(box_46);buttons.add(box_47);buttons.add(box_48);
			buttons.add(box_49);
		}
		if(num_boxes > 7) {
			buttons.add(box_50);buttons.add(box_51);buttons.add(box_52);buttons.add(box_53);
			buttons.add(box_54);buttons.add(box_55);buttons.add(box_56);buttons.add(box_57);
			buttons.add(box_58);buttons.add(box_59);buttons.add(box_60);buttons.add(box_61);
			buttons.add(box_62);buttons.add(box_63);buttons.add(box_64);
		}
		
		//construct and set values for buttons//
		
		for (JButton item : buttons) {
			item.setPreferredSize(new Dimension(panel_size / num_boxes, panel_size / num_boxes));
			item.setBackground(Color.WHITE);
			this.add(item);
		}
		
		makeActive(false);
		
	}
	
	public void makeActive(boolean b) {
		for (JButton item : buttons) {
			item.setEnabled(b);
		}
	}
	
	public void fillButton(int f, Color c) {
		buttons.get(f).setBackground(c);
	}
	
	public void fillListButtons(int[] list, Color c){
		for (int j : list) {
			buttons.get(j).setBackground(c);
		}
	}
	
	public void fillListButtons(ArrayList<Integer> list, Color c){
		for (int j : list) {
			buttons.get(j).setBackground(c);
		}
	}
	
	public ArrayList<Integer> getPattern(boolean symmetry, int n) {
		Random rand;
		rand = new Random();
		
		ArrayList<Integer> returnList = new ArrayList<Integer>();;
		
		int[] half_one = {1,2,3,4,9,10,11,12,17,18,19,20,25,26,27,28,33,34,35,36,41,42,43,44,49,50,51,52,57,58,59,60};
		int[] half_two_symm_counterparts = {8,7,6,5,16,15,14,13,24,23,22,21,32,31,30,29,40,39,38,37,48,47,46,45,56,55,54,53,64,63,62,61};
		ArrayList<Integer> halfOneAL = new ArrayList<Integer>();
		ArrayList<Integer> halfTwoAL = new ArrayList<Integer>();
		for (int i : half_one) {
			halfOneAL.add(i - 1);
		}
		for (int i : half_two_symm_counterparts) {
			halfTwoAL.add(i - 1);
		}
		
		
		//if we want a symmetrical pattern then take random numbers from half_one and SAME indice numbers from half_two//
		int[] indicesToFill = new int[n];;
		ArrayList<Integer> used = new ArrayList<Integer>();
		
		for (int i = 0; i < n; i++) {
			int tmp = rand.nextInt(half_one.length);
			if (!used.contains(tmp)) {
				indicesToFill[i] = tmp;
				used.add(tmp);
			} else {
				i--; //number already used so need to go again.
			}			
		}

		ArrayList<Integer> values_half_one = new ArrayList<Integer>();
		ArrayList<Integer> values_half_two = new ArrayList<Integer>();
		
		for (int i : indicesToFill) {
			values_half_one.add(halfOneAL.get(i));
		}

		if (symmetry) {
			//get the exact counterparts for half two
			for (int i : indicesToFill) {
				values_half_two.add(halfTwoAL.get(i));
			}
		} else {
			// not symmetrical //
			// so need half_two to be different to the exact counterparts //
			// just get a a new rando set of indices but with a check to ensure that we do not stumble on 
			// a symmetrical pattern by accident
			int[] indicesToFill_two = new int[n];
			ArrayList<Integer> used_two = new ArrayList<Integer>();
			for (int i = 0; i < n; i++) {
				int tmp = rand.nextInt(half_two_symm_counterparts.length);
				if (!used_two.contains(tmp)) {
					indicesToFill_two[i] = tmp;
					used_two.add(tmp);
				} else {
					i--; //number already used so need to go again.
				}			
			}
			
			for (int i : indicesToFill_two) {
				values_half_two.add(halfTwoAL.get(i));
			}			
		}
		
		returnList.addAll(values_half_one);
		returnList.addAll(values_half_two);
		
		return returnList;
	}

	
}
