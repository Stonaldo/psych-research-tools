package stone.tatool.extensions.status;

import java.awt.Color;
import java.awt.Image;

import ch.tatool.core.display.swing.status.TextStatusPanel;

/**
 * modified version of the default TextStatusPanel that is supplied as part 
 * of Tatool. Not much going on here, simply supplies a version of the 
 * text status panel class that one can easily edit without going into 
 * the tatool source code. Pretty much just used for supplying ones own 
 * appearance enhancements, bg colors/bg images etc.
 * 
 * Uncomment the two blocks below and edit the necessary path to have the 
 * background of the panel be an image instead.
 * 
 * @author James Stone
 */

public class ModifiedTextStatusPanel extends TextStatusPanel{

	private static final long serialVersionUID = -8902723073228770879L;
	private Image img;
	
	/*
	 * creates a ModifiedTxtStatusPanel
	 */
	
	public ModifiedTextStatusPanel() {
		super();
		initModifications();
	}
	
	private void initModifications() {
		this.setBackground(new Color(84,177,239));
	    /*try {
	    	img = ImageIO.read(new File("src/main/resources/tatool/extension/panelbg.png"));
	    } catch(IOException e) {
	    	e.printStackTrace();
	    }*/
	}
	
	/*protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	    // paint the background image and scale it to fill the entire space
		g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
	}*/
	

}
