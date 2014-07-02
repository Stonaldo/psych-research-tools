package stone.javatasks.nback;

import javax.swing.JFrame;

public class TestSingleNBackPanel extends JFrame {
	
	/**
	 * testing the panel
	 */
	
	public TestSingleNBackPanel() {
		SpatialNBackPanel thisPanel = new SpatialNBackPanel();
		this.add(thisPanel);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		thisPanel.light(5);
	}
	
	
	public static void main(String[] args) {
		new TestSingleNBackPanel();
		
	}
}
