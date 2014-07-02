package stone.javatasks.helperclasses.swing.demo.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import stone.javatasks.helperclasses.swing.custom.component.panel.GlossyCurvedPanel;
import stone.javatasks.helperclasses.swing.custom.component.panel.StandardCurvedPanel;
import stone.javatasks.helperclasses.swing.custom.component.panel.StandardPanel;
import stone.javatasks.helperclasses.swing.util.PanelType;
import stone.javatasks.helperclasses.swing.util.Theme;
import stone.javatasks.helperclasses.swing.util.ThemesList;

public class Test {
	public static void main(String[] args) {
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch (Exception e) {
			// TODO: handle exception
		}
		//StandardPanel rectangulerPanel = new StandardPanel();
		
		//StandardPanel rectangulerPanel = new StandardPanel(Theme.STANDARD_BLUE_THEME);

		
		StandardCurvedPanel rectangulerPanel = new StandardCurvedPanel(Theme.GLOSSY_LIGHTGREEN_THEME);
		StandardCurvedPanel rectangulerPanel2 = new StandardCurvedPanel(Theme.GLOSSY_RED_THEME);
		StandardCurvedPanel rectangulerPanel3 = new StandardCurvedPanel(Theme.GLOSSY_BLACK_THEME);

		//rectangulerPanel.setTitle("StandardCurvedPanel");
		
		StandardPanel roundRectPanel = new StandardPanel(Theme.STANDARD_RED_THEME,PanelType.PANEL_ROUNDED_RECTANGLUR);

		StandardPanel ellipsePanel = new StandardPanel(Theme.STANDARD_ORANGE_THEME,PanelType.PANEL_ELLIPSE);
		
		StandardPanel roundedPanel = new StandardPanel(Theme.STANDARD_GREEN_THEME,PanelType.PANEL_ROUNDED);

		StandardPanel circularPanel = new StandardPanel(Theme.STANDARD_GOLD_THEME,PanelType.PANEL_CIRCULAR);

		JPanel standardCPanel = new JPanel();
		JPanel golssyCPanel = new JPanel();

		JPanel mainPanel = new JPanel(new BorderLayout());
		List<Integer> themes = ThemesList.createThemesList();
		JPanel rootPanel = new JPanel();
		ThemeTest test = new ThemeTest();
		test.GlossyThemes();

		 ArrayList<String> themeLbl = test.getThemesList();
		 int count=0;
		for (Integer theme : themes) {
			StandardCurvedPanel panel = new StandardCurvedPanel(theme);
			panel.setFONT(new Font("Thoma", 1, 14));
			//panel.repaint();
			panel.setPreferredSize(new Dimension(250, 100));
			panel.setTitle(themeLbl.get(count++));
			GlossyCurvedPanel gpanel = new GlossyCurvedPanel(theme);
			gpanel.setPreferredSize(new Dimension(250, 200));

			standardCPanel.add(panel);
			golssyCPanel.add(gpanel);
		}
		
		standardCPanel.setPreferredSize(new Dimension(200, 5000));
		golssyCPanel.setPreferredSize(new Dimension(200, 5000));

		// mainPanel.add(new JScrollPane(rootPanel));
		standardCPanel.setBackground(new Color(0, 0, 0));
		//golssyCPanel.setBackground(new Color(0, 0, 0));
	
		rectangulerPanel.setPreferredSize(new  Dimension(300,200));
		rectangulerPanel2.setPreferredSize(new  Dimension(300,200));
		rectangulerPanel3.setPreferredSize(new  Dimension(300,200));

		roundRectPanel.setPreferredSize(new Dimension(300,100));
		roundedPanel.setPreferredSize(new Dimension(300,100));
		ellipsePanel.setPreferredSize(new Dimension(300,100));
		circularPanel.setPreferredSize(new Dimension(200,200));

		JFrame frame = new JFrame();
		//frame.setLayout(new FlowLayout());
		frame.add(new JScrollPane(standardCPanel));
		//frame.add(rectangulerPanel2);
		//frame.add(rectangulerPanel3);

		frame.getContentPane().setBackground(Color.white);
		//frame.add(roundRectPanel);
		//frame.add(ellipsePanel);
		//frame.add(roundedPanel);
		//frame.add(circularPanel);
		frame.getContentPane().setBackground(Color.BLACK);
		frame.setVisible(true);
	}
}
