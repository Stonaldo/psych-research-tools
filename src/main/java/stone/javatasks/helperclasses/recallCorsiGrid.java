package stone.javatasks.helperclasses;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

public class recallCorsiGrid extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8272333208650982071L;
	private JButton square_1 = new JButton();
	private JButton square_2 = new JButton();
	private JButton square_3 = new JButton();
	private JButton square_4 = new JButton();
	private JButton square_5 = new JButton();
	private JButton square_6 = new JButton();
	private JButton square_7 = new JButton();
	private JButton square_8 = new JButton();
	private JButton square_9 = new JButton();	
	
	private ArrayList<JButton> grids = new ArrayList<JButton>();
	
	//private JLabel gridText;
	
	
	public recallCorsiGrid() {
		
		super();
		
		final Font gridTextFont = new Font("Source Code Pro", 1, 16);
		
		//gridText = new JLabel("");
		//gridText.setFont(gridTextFont);
		
		grids.add(square_1); grids.add(square_2); grids.add(square_3); 
		grids.add(square_4); grids.add(square_5); grids.add(square_6); 
		grids.add(square_7); grids.add(square_8); grids.add(square_9);
		
		this.setPreferredSize(new Dimension(120, 150));
		this.setBackground(Color.WHITE);

		for (int i = 0; i < 9; i++) {
			grids.get(i).setPreferredSize(new Dimension(25,25));
			grids.get(i).setBackground(Color.LIGHT_GRAY);
			this.add(grids.get(i));
		}
		
		//this.add(gridText);
		
		this.setBorder(BorderFactory.createTitledBorder("recall"));

	}
	
	public void setOneButtonBackground(int gridNum, Color gridCol) {
		grids.get(gridNum).setBackground(gridCol);
	}
	
	public Color getOneButtonBackground(int gridNum) {
		return grids.get(gridNum).getBackground();
	}
	/*
	public String getText() {
		return gridText.getText();
	}
	
	public void setGridText(String x) {
		//gridText.setText(x);
		this.revalidate();
	}*/
	public String getBorderText() {
		return this.getBorderText();
	}
	public void setBorderText(String x) {
		
		StringBuilder sb = new StringBuilder("Selection ");
		sb.append(x);
		this.setBorder(BorderFactory.createTitledBorder(sb.toString()));
	}
	
}
