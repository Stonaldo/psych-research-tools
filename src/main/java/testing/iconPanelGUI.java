package testing;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import stone.javatasks.helperclasses.RotatedIcon;
import stone.javatasks.helperclasses.iconPanel;


public class iconPanelGUI extends JFrame {
	
	private ImageIcon arrowLong;
	private ImageIcon arrowShort;	

	public iconPanelGUI() {
		
		super("testing iconPanel");
		
		JPanel tep = new JPanel();
		tep.setSize(new Dimension(400,150));
		tep.setBackground(Color.BLACK);
		

		arrowShort = new ImageIcon(getClass().getResource("UP_SHORT.png"));
		arrowLong = new ImageIcon(getClass().getResource("UP_LONG.png"));
		
		
		iconPanel tmp = new iconPanel(arrowShort,0,100,100,false);
		iconPanel tmp2 = new iconPanel(arrowLong,0,100,100,false);
		iconPanel tmp3 = new iconPanel(arrowShort,45,100,100,false);
		iconPanel tmp4 = new iconPanel(arrowLong,45,100,100,false);
		iconPanel tmp5 = new iconPanel(arrowShort,135,100,100,false);
		iconPanel tmp6 = new iconPanel(arrowLong,135,100,100,false);
		
		
		tep.add(tmp);
		tep.add(tmp2);
		tep.add(tmp3);
		tep.add(tmp4);		
		tep.add(tmp5);
		tep.add(tmp6);
		
		this.add(tep);
	}
		
}

